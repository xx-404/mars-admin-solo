package com.mars.biz.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.biz.dto.req.ActivationCodeQueryReq;
import com.mars.biz.dto.rsp.ActivationCodeRsp;
import com.mars.biz.entity.ActivationCode;
import com.mars.biz.mapper.ActivationCodeMapper;
import com.mars.biz.service.ActivationCodeService;
import com.mars.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 激活码 Service 实现
 *
 * @author Mars
 * @date 2026-04-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivationCodeServiceImpl implements ActivationCodeService {

    private final ActivationCodeMapper activationCodeMapper;

    @Override
    public Page<ActivationCode> page(Integer page, Integer pageSize, ActivationCodeQueryReq req) {
        Page<ActivationCode> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<ActivationCode> wrapper = new LambdaQueryWrapper<>();

        if (req != null) {
            if (StrUtil.isNotBlank(req.getActivationCode())) {
                wrapper.like(ActivationCode::getActivationCode, req.getActivationCode());
            }
            if (StrUtil.isNotBlank(req.getDurationType())) {
                wrapper.eq(ActivationCode::getDurationType, req.getDurationType().toUpperCase());
            }
            if (req.getActivationStatus() != null) {
                wrapper.eq(ActivationCode::getActivationStatus, req.getActivationStatus());
            }
            if (req.getUserId() != null) {
                wrapper.eq(ActivationCode::getUserId, req.getUserId());
            }
            if (req.getShopId() != null) {
                wrapper.eq(ActivationCode::getShopId, req.getShopId());
            }
        }

        wrapper.orderByDesc(ActivationCode::getCreateTime);
        return activationCodeMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public List<ActivationCode> list(ActivationCodeQueryReq req) {
        LambdaQueryWrapper<ActivationCode> wrapper = new LambdaQueryWrapper<>();

        if (req != null) {
            if (StrUtil.isNotBlank(req.getActivationCode())) {
                wrapper.like(ActivationCode::getActivationCode, req.getActivationCode());
            }
            if (StrUtil.isNotBlank(req.getDurationType())) {
                wrapper.eq(ActivationCode::getDurationType, req.getDurationType().toUpperCase());
            }
            if (req.getActivationStatus() != null) {
                wrapper.eq(ActivationCode::getActivationStatus, req.getActivationStatus());
            }
            if (req.getUserId() != null) {
                wrapper.eq(ActivationCode::getUserId, req.getUserId());
            }
            if (req.getShopId() != null) {
                wrapper.eq(ActivationCode::getShopId, req.getShopId());
            }
        }

        wrapper.orderByDesc(ActivationCode::getCreateTime);
        return activationCodeMapper.selectList(wrapper);
    }

    @Override
    public ActivationCode getById(Long id) {
        return activationCodeMapper.selectById(id);
    }

    @Override
    public ActivationCode getByCode(String code) {
        LambdaQueryWrapper<ActivationCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivationCode::getActivationCode, code);
        return activationCodeMapper.selectOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivationCode create(ActivationCode activationCode) {
        // 生成激活码（如果未提供）
        if (StrUtil.isBlank(activationCode.getActivationCode())) {
            activationCode.setActivationCode(generateActivationCode());
        }

        // 初始状态：未激活
        activationCode.setActivationStatus(ActivationCode.STATUS_UNACTIVATED);
        activationCodeMapper.insert(activationCode);
        return activationCode;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ActivationCode activationCode) {
        activationCodeMapper.updateById(activationCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        activationCodeMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivationCodeRsp activate(String code, Long userId, Long shopId) {
        // 查询激活码
        ActivationCode activationCode = getByCode(code);
        if (activationCode == null) {
            throw new BusinessException("激活码不存在");
        }

        // 检查是否已激活
        if (activationCode.isActivated()) {
            throw new BusinessException("激活码已被使用");
        }

        // 更新激活码状态
        activationCode.setActivationStatus(ActivationCode.STATUS_ACTIVATED);
        activationCode.setUserId(userId);
        activationCode.setShopId(shopId);
        activationCode.setActivatedAt(LocalDateTime.now());
        activationCodeMapper.updateById(activationCode);

        log.info("激活码使用成功：code={}, userId={}, shopId={}", code, userId, shopId);

        return ActivationCodeRsp.builder()
                .id(activationCode.getId())
                .activationCode(activationCode.getActivationCode())
                .durationType(activationCode.getDurationType())
                .durationTypeName(activationCode.getDurationTypeName())
                .durationDays(activationCode.getDurationDays())
                .activationStatus(activationCode.getActivationStatus())
                .activationStatusName(activationCode.getActivationStatusName())
                .userId(activationCode.getUserId())
                .shopId(activationCode.getShopId())
                .activatedAt(activationCode.getActivatedAt() != null
                        ? activationCode.getActivatedAt().toString() : null)
                .createTime(activationCode.getCreateTime() != null
                        ? activationCode.getCreateTime().toString() : null)
                .updateTime(activationCode.getUpdateTime() != null
                        ? activationCode.getUpdateTime().toString() : null)
                .build();
    }

    /**
     * 生成激活码（16 位随机数字和大小写字母组合）
     */
    private String generateActivationCode() {
        return RandomUtil.randomStringUpper(16);
    }
}
