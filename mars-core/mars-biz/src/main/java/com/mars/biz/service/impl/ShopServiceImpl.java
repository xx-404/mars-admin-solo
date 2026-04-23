package com.mars.biz.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.biz.dto.req.ShopQueryReq;
import com.mars.biz.dto.rsp.ActivationCodeRsp;
import com.mars.biz.dto.rsp.AuthUrlRsp;
import com.mars.biz.dto.rsp.ExternalRsp;
import com.mars.biz.dto.rsp.ShopAuthInfoRsp;
import com.mars.biz.entity.Shop;
import com.mars.biz.enums.PlatformType;
import com.mars.biz.mapper.ShopMapper;
import com.mars.biz.service.ActivationCodeService;
import com.mars.biz.service.ShopExternalService;
import com.mars.biz.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 店铺 Service 实现
 *
 * @author Mars
 * @date 2026-04-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    private final ShopMapper shopMapper;
    private final ShopExternalService shopExternalService;
    private final ActivationCodeService activationCodeService;

    @Override
    public Page<Shop> page(Integer page, Integer pageSize, ShopQueryReq req) {
        Page<Shop> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();

        if (req != null) {
            if (req.getUserId() != null) {
                wrapper.eq(Shop::getUserId, req.getUserId());
            }
            if (StrUtil.isNotBlank(req.getPlatform())) {
                wrapper.eq(Shop::getPlatform, req.getPlatform().toUpperCase());
            }
            if (StrUtil.isNotBlank(req.getShopName())) {
                wrapper.like(Shop::getShopName, req.getShopName());
            }
            if (req.getAuthStatus() != null) {
                wrapper.eq(Shop::getAuthStatus, req.getAuthStatus());
            }
            if (req.getActivationStatus() != null) {
                wrapper.eq(Shop::getActivationStatus, req.getActivationStatus());
            }
            if (StrUtil.isNotBlank(req.getShopIdExternal())) {
                wrapper.eq(Shop::getShopIdExternal, req.getShopIdExternal());
            }
        }

        wrapper.orderByDesc(Shop::getCreateTime);
        return shopMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public List<Shop> listByUserId(Long userId) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getUserId, userId);
        wrapper.orderByDesc(Shop::getCreateTime);
        return shopMapper.selectList(wrapper);
    }

    @Override
    public List<Shop> listByConditions(Long userId, String platform, Integer authStatus, Integer activationStatus) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getUserId, userId);
        if (StrUtil.isNotBlank(platform)) {
            wrapper.eq(Shop::getPlatform, platform.toUpperCase());
        }
        if (authStatus != null) {
            wrapper.eq(Shop::getAuthStatus, authStatus);
        }
        if (activationStatus != null) {
            wrapper.eq(Shop::getActivationStatus, activationStatus);
        }
        wrapper.orderByDesc(Shop::getCreateTime);
        return shopMapper.selectList(wrapper);
    }

    @Override
    public Shop getById(Long id) {
        return shopMapper.selectById(id);
    }

    @Override
    public Shop getByIdAndUserId(Long id, Long userId) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getId, id);
        wrapper.eq(Shop::getUserId, userId);
        return shopMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Shop create(Shop shop) {
        // 自动生成 shop_code（6 位随机数字和大小写字母组合）
        shop.setShopCode(generateShopCode());

        // 初始状态：未授权、未激活
        shop.setAuthStatus(Shop.AUTH_STATUS_UNAUTHORIZED);
        shop.setActivationStatus(Shop.ACTIVATION_STATUS_INACTIVE);
        shopMapper.insert(shop);
        return shop;
    }

    /**
     * 生成 6 位随机店铺编码（数字 + 大小写字母）
     */
    private String generateShopCode() {
        return RandomUtil.randomStringUpper(6);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Shop shop) {
        shopMapper.updateById(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAuthStatus(Long id, String shopName, String shopIdExternal, String authToken, String remark) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setShopName(shopName);
        shop.setShopIdExternal(shopIdExternal);
        shop.setAuthToken(authToken);
        shop.setAuthStatus(Shop.AUTH_STATUS_AUTHORIZED);
        shop.setRemark(remark);
        shopMapper.updateById(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivationStatus(Long id, Integer activationStatus, LocalDateTime expiresAt) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setActivationStatus(activationStatus);
        shop.setExpiresAt(expiresAt);
        if (activationStatus == Shop.ACTIVATION_STATUS_ACTIVE && shopMapper.selectById(id).getActivatedAt() == null) {
            shop.setActivatedAt(LocalDateTime.now());
        }
        shopMapper.updateById(shop);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        shopMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIdAndUserId(Long id, Long userId) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getId, id);
        wrapper.eq(Shop::getUserId, userId);
        shopMapper.delete(wrapper);
    }

    // ==================== 授权 ====================

    @Override
    public ExternalRsp<AuthUrlRsp> getAuthUrl(Long id, Long userId) {
        Shop shop = getByIdAndUserId(id, userId);
        if (shop == null) {
            return ExternalRsp.fail("店铺不存在或无权访问");
        }

        PlatformType platform = PlatformType.fromCode(shop.getPlatform());
        if (platform == null) {
            return ExternalRsp.fail("不支持的平台类型");
        }

        // 获取授权链接
        ExternalRsp<AuthUrlRsp> response = shopExternalService.getAuthUrl(platform, String.valueOf(id));
        if (response.isSuccess()) {
            // 保存state到备注
            JSONObject remarkJson = parseRemark(shop.getRemark());
            remarkJson.set("authState", response.getData().getState());
            remarkJson.set("authStateExpire", LocalDateTime.now().plusHours(1).toString());
            remarkJson.set("authTimestamp", System.currentTimeMillis());

            Shop updateShop = new Shop();
            updateShop.setId(id);
            updateShop.setRemark(remarkJson.toString());
            shopMapper.updateById(updateShop);
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExternalRsp<ShopAuthInfoRsp> handleAuthCallback(Long id, String platform, String state) {
        Shop shop = getById(id);
        if (shop == null) {
            return ExternalRsp.fail("店铺不存在");
        }

        // 验证state
        JSONObject remarkJson = parseRemark(shop.getRemark());
        String savedState = remarkJson.getStr("authState");
        String expireTime = remarkJson.getStr("authStateExpire");

        if (StrUtil.isBlank(state) || !state.equals(savedState)) {
            return ExternalRsp.fail("授权验证失败，请重新授权");
        }

        if (StrUtil.isNotBlank(expireTime) && LocalDateTime.parse(expireTime).isBefore(LocalDateTime.now())) {
            return ExternalRsp.fail("授权已过期，请重新授权");
        }

        // 调用外部接口获取店铺信息
        PlatformType platformType = PlatformType.fromCode(platform);
        ExternalRsp<ShopAuthInfoRsp> response = shopExternalService.handleAuthCallback(platformType, state);

        if (response.isSuccess()) {
            ShopAuthInfoRsp authInfo = response.getData();

            // 更新店铺授权信息
            JSONObject newRemark = parseRemark(shop.getRemark());
            newRemark.remove("authState");
            newRemark.remove("authStateExpire");
            newRemark.set("authorizedAt", LocalDateTime.now().toString());

            updateAuthStatus(id, authInfo.getShopName(), authInfo.getOwnerId(), authInfo.getAuthToken(), newRemark.toString());

            log.info("店铺授权成功: id={}, shopName={}, ownerId={}", id, authInfo.getShopName(), authInfo.getOwnerId());
        }

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExternalRsp<ShopAuthInfoRsp> bindDouyinShop(Long id, Long userId, String bindCode, String shopName, String state) {
        Shop shop = getByIdAndUserId(id, userId);
        if (shop == null) {
            return ExternalRsp.fail("店铺不存在或无权访问");
        }

        if (!PlatformType.DOUYIN.getCode().equalsIgnoreCase(shop.getPlatform())) {
            return ExternalRsp.fail("仅抖音平台支持关联码绑定");
        }

        // 验证 state（必填，用于 CSRF 防护）
        if (StrUtil.isBlank(state)) {
            log.warn("抖音店铺授权缺少 state 参数：id={}, userId={}", id, userId);
            return ExternalRsp.fail("缺少授权 state 参数");
        }

        JSONObject remarkJson = parseRemark(shop.getRemark());
        String savedState = remarkJson.getStr("authState");
        String expireTime = remarkJson.getStr("authStateExpire");

        if (!state.equals(savedState)) {
            log.warn("抖音店铺授权 state 验证失败：id={}, provided={}, expected={}", id, state, savedState);
            return ExternalRsp.fail("授权验证失败，请重新授权");
        }

        if (StrUtil.isNotBlank(expireTime) && LocalDateTime.parse(expireTime).isBefore(LocalDateTime.now())) {
            log.warn("抖音店铺授权 state 已过期：id={}, expireTime={}", id, expireTime);
            return ExternalRsp.fail("授权已过期，请重新授权");
        }

        // 调用外部接口绑定店铺
        ExternalRsp<ShopAuthInfoRsp> response = shopExternalService.bindDouyinShop(bindCode, shopName);

        if (response.isSuccess()) {
            ShopAuthInfoRsp authInfo = response.getData();

            // 使用之前解析的 remarkJson，清除临时 state
            remarkJson.set("authorizedAt", LocalDateTime.now().toString());
            remarkJson.set("bindCode", bindCode);
            remarkJson.remove("authState");
            remarkJson.remove("authStateExpire");

            updateAuthStatus(id, shopName, authInfo.getOwnerId(), bindCode, remarkJson.toString());

            log.info("抖音店铺绑定成功: id={}, shopName={}, ownerId={}", id, shopName, authInfo.getOwnerId());
        }

        return response;
    }

    @Override
    public ShopAuthInfoRsp getAuthStatus(Long id, Long userId) {
        Shop shop = getByIdAndUserId(id, userId);
        if (shop == null) {
            return null;
        }

        return ShopAuthInfoRsp.builder()
                .shopId(String.valueOf(shop.getId()))
                .shopName(shop.getShopName())
                .ownerId(shop.getShopIdExternal())
                .authToken(shop.getAuthToken())
                .build();
    }

    @Override
    public ExternalRsp<AuthUrlRsp> refreshAuth(Long id, Long userId) {
        return getAuthUrl(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExternalRsp<ShopAuthInfoRsp> activate(Long id, Long userId, String activationCode) {
        Shop shop = getByIdAndUserId(id, userId);
        if (shop == null) {
            return ExternalRsp.fail("店铺不存在或无权访问");
        }

        if (!shop.isAuthorized()) {
            return ExternalRsp.fail("店铺未授权，请先完成授权后再激活");
        }

        // 验证并使用激活码
        ActivationCodeRsp codeRsp = activationCodeService.activate(activationCode, userId, id);

        // 根据激活码类型计算过期时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(codeRsp.getDurationDays());

        // 如果店铺已激活且在有效期内，累加时长
        if (shop.getExpiresAt() != null && shop.getExpiresAt().isAfter(now) && shop.isActivated()) {
            expiresAt = shop.getExpiresAt().plusDays(codeRsp.getDurationDays());
        }

        // 更新激活状态
        updateActivationStatus(id, Shop.ACTIVATION_STATUS_ACTIVE, expiresAt);

        ShopAuthInfoRsp result = ShopAuthInfoRsp.builder()
                .shopId(String.valueOf(id))
                .shopName(shop.getShopName())
                .ownerId(shop.getShopIdExternal())
                .expireTime(expiresAt.toString())
                .build();

        log.info("店铺激活成功：id={}, activationCode={}, durationType={}, durationDays={}, expiresAt={}",
                id, activationCode, codeRsp.getDurationType(), codeRsp.getDurationDays(), expiresAt);

        return ExternalRsp.ok(result, "激活成功");
    }

    /**
     * 解析备注JSON
     */
    private JSONObject parseRemark(String remark) {
        if (StrUtil.isBlank(remark)) {
            return new JSONObject();
        }
        try {
            return JSONUtil.parseObj(remark);
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
