package com.mars.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.mars.biz.dto.req.ShopQueryReq;
import com.mars.biz.dto.rsp.AuthUrlRsp;
import com.mars.biz.dto.rsp.ExternalRsp;
import com.mars.biz.dto.rsp.ShopAuthInfoRsp;
import com.mars.biz.entity.Shop;
import com.mars.biz.enums.PlatformType;
import com.mars.biz.service.ShopExternalService;
import com.mars.biz.service.ShopService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 店铺管理 Controller
 *
 * @author Mars
 * @date 2026-04-16
 */
@RestController
@RequestMapping("/biz/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final ShopExternalService shopExternalService;

    // ==================== 店铺 CRUD ====================

    /**
     * 分页查询店铺列表
     */
    @GetMapping("/page")
    @SaCheckPermission("biz:shop:list")
    public Result<PageResult<Shop>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            ShopQueryReq req) {
        var result = shopService.page(page, pageSize, req);
        return Result.ok(PageResult.of(result));
    }

    /**
     * 查询用户的店铺列表
     */
    @GetMapping("/list")
    public Result<List<Shop>> list(@RequestParam Long userId) {
        List<Shop> shops = shopService.listByUserId(userId);
        return Result.ok(shops);
    }

    /**
     * 根据平台+授权状态+激活状态查询当前用户的店铺列表
     */
    @GetMapping("/list-by-conditions")
    public Result<List<Shop>> listByConditions(
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) Integer authStatus,
            @RequestParam(required = false) Integer activationStatus) {
        // 自动从当前登录用户获取 userId
        Long userId = StpUtil.getLoginIdAsLong();
        List<Shop> shops = shopService.listByConditions(userId, platform, authStatus, activationStatus);
        return Result.ok(shops);
    }

    /**
     * 获取店铺详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("biz:shop:query")
    public Result<Shop> getInfo(@PathVariable Long id) {
        Shop shop = shopService.getById(id);
        return Result.ok(shop);
    }

    /**
     * 新增店铺
     */
    @PostMapping
    @SaCheckPermission("biz:shop:add")
    @Log(title = "店铺管理", businessType = BusinessType.INSERT)
    public Result<Shop> add(@RequestBody Shop shop) {
        Shop created = shopService.create(shop);
        return Result.ok(created);
    }

    /**
     * 更新店铺基本信息
     */
    @PutMapping("/{id}")
    @SaCheckPermission("biz:shop:edit")
    @Log(title = "店铺管理", businessType = BusinessType.UPDATE)
    public Result<Void> update(@PathVariable Long id, @RequestBody Shop shop) {
        shop.setId(id);
        shopService.update(shop);
        return Result.ok();
    }

    /**
     * 删除店铺
     */
    @DeleteMapping("/{ids}")
    @SaCheckPermission("biz:shop:remove")
    @Log(title = "店铺管理", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] ids) {
        shopService.delete(ids);
        return Result.ok();
    }

    // ==================== 授权管理 ====================

    /**
     * 获取授权链接
     */
    @PostMapping("/{id}/authorize")
    @SaCheckPermission("biz:shop:authorize")
    @Log(title = "店铺授权", businessType = BusinessType.UPDATE)
    public Result<AuthUrlRsp> getAuthUrl(
            @PathVariable Long id,
            @RequestParam Long userId) {
        ExternalRsp<AuthUrlRsp> response = shopService.getAuthUrl(id, userId);
        if (!response.isSuccess()) {
            return Result.fail(response.getError());
        }
        return Result.ok(response.getData());
    }

    /**
     * 授权回调处理
     */
    @GetMapping("/{id}/authorize/callback")
    @SaCheckPermission("biz:shop:authorize")
    public Result<ShopAuthInfoRsp> handleAuthCallback(
            @PathVariable Long id,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String platform) {
        // 抖音平台使用关联码绑定
//        if (PlatformType.DOUYIN.getCode().equalsIgnoreCase(platform) && StrUtil.isNotBlank(bindCode) && StrUtil.isNotBlank(shopName)) {
//            // 关联码绑定需要用户ID，这里暂时从店铺获取
//            Shop shop = shopService.getById(id);
//            if (shop == null) {
//                return Result.fail("店铺不存在");
//            }
//            // 验证当前登录用户是否为店铺所有者
//            Long currentUserId = StpUtil.getLoginIdAsLong();
//            if (!shop.getUserId().equals(currentUserId)) {
//                return Result.fail("无权访问此店铺");
//            }
//            ExternalRsp<ShopAuthInfoRsp> response = shopService.bindDouyinShop(id, shop.getUserId(), bindCode, shopName, state);
//            if (!response.isSuccess()) {
//                return Result.fail(response.getError());
//            }
//            return Result.ok(response.getData());
//        }

        // 其他平台使用state回调
        ExternalRsp<ShopAuthInfoRsp> response = shopService.handleAuthCallback(id, platform, state);
        if (!response.isSuccess()) {
            return Result.fail(response.getError());
        }
        return Result.ok(response.getData());
    }

    /**
     * 获取授权状态
     */
    @GetMapping("/{id}/auth-status")
    @SaCheckPermission("biz:shop:query")
    public Result<ShopAuthInfoRsp> getAuthStatus(
            @PathVariable Long id,
            @RequestParam Long userId) {
        ShopAuthInfoRsp authInfo = shopService.getAuthStatus(id, userId);
        if (authInfo == null) {
            return Result.fail("店铺不存在或无权访问");
        }
        return Result.ok(authInfo);
    }

    /**
     * 刷新授权状态
     */
    @PostMapping("/{id}/auth-status")
    @SaCheckPermission("biz:shop:authorize")
    @Log(title = "刷新授权", businessType = BusinessType.UPDATE)
    public Result<AuthUrlRsp> refreshAuth(
            @PathVariable Long id,
            @RequestParam Long userId) {
        ExternalRsp<AuthUrlRsp> response = shopService.refreshAuth(id, userId);
        if (!response.isSuccess()) {
            return Result.fail(response.getError());
        }
        return Result.ok(response.getData());
    }

    // ==================== 激活管理 ====================

    /**
     * 激活店铺
     */
    @PostMapping("/{id}/activate")
    @SaCheckPermission("biz:shop:activate")
    @Log(title = "店铺激活", businessType = BusinessType.UPDATE)
    public Result<ShopAuthInfoRsp> activate(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam String activationCode) {
        ExternalRsp<ShopAuthInfoRsp> response = shopService.activate(id, userId, activationCode);
        if (!response.isSuccess()) {
            return Result.fail(response.getError());
        }
        return Result.ok(response.getData());
    }

    /**
     * 获取平台订购地址
     */
    @GetMapping("/platform/{platform}/order-url")
    public Result<String> getOrderUrl(@PathVariable String platform) {
        PlatformType platformType = PlatformType.fromCode(platform);
        if (platformType == null) {
            return Result.fail("不支持的平台类型");
        }
        String orderUrl = shopExternalService.getOrderUrl(platformType);
        return Result.ok(orderUrl);
    }

    /**
     * 获取平台名称列表
     */
    @GetMapping("/platforms")
    public Result<List<PlatformType>> getPlatforms() {
        return Result.ok(List.of(PlatformType.values()));
    }
}
