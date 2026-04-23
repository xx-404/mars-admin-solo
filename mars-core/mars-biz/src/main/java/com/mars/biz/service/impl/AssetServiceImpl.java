package com.mars.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.biz.entity.Asset;
import com.mars.biz.entity.AssetLoan;
import com.mars.biz.mapper.AssetLoanMapper;
import com.mars.biz.mapper.AssetMapper;
import com.mars.biz.service.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 资产 Service 实现
 *
 * @author Mars
 * @date 2026-04-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetMapper assetMapper;
    private final AssetLoanMapper assetLoanMapper;

    @Override
    public Page<Asset> page(Integer page, Integer pageSize, String assetName, String assetType, Integer status) {
        Page<Asset> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(assetName)) {
            wrapper.like(Asset::getAssetName, assetName);
        }
        if (StrUtil.isNotBlank(assetType)) {
            wrapper.eq(Asset::getAssetType, assetType);
        }
        if (status != null) {
            wrapper.eq(Asset::getStatus, status);
        }

        wrapper.orderByDesc(Asset::getCreateTime);
        return assetMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public List<Asset> list(String assetName, String assetType, Integer status) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(assetName)) {
            wrapper.like(Asset::getAssetName, assetName);
        }
        if (StrUtil.isNotBlank(assetType)) {
            wrapper.eq(Asset::getAssetType, assetType);
        }
        if (status != null) {
            wrapper.eq(Asset::getStatus, status);
        }

        wrapper.orderByDesc(Asset::getCreateTime);
        return assetMapper.selectList(wrapper);
    }

    @Override
    public Asset getById(Long id) {
        return assetMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Asset create(Asset asset) {
        if (asset.getAvailableQuantity() == null) {
            asset.setAvailableQuantity(asset.getTotalQuantity() != null ? asset.getTotalQuantity() : 0);
        }
        if (asset.getBorrowedQuantity() == null) {
            asset.setBorrowedQuantity(0);
        }
        if (asset.getStatus() == null) {
            asset.setStatus(Asset.STATUS_NORMAL);
        }
        assetMapper.insert(asset);
        return asset;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Asset asset) {
        assetMapper.updateById(asset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        assetMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean borrowAsset(Long assetId, Long borrowerId, Integer quantity, String borrowReason, LocalDateTime expectedReturnTime) {
        Asset asset = assetMapper.selectById(assetId);
        if (asset == null) {
            log.warn("资产不存在: assetId={}", assetId);
            return false;
        }

        if (!asset.isAvailable()) {
            log.warn("资产不可用: assetId={}, status={}, availableQuantity={}", assetId, asset.getStatus(), asset.getAvailableQuantity());
            return false;
        }

        if (quantity == null || quantity <= 0) {
            log.warn("领用数量无效: quantity={}", quantity);
            return false;
        }

        if (asset.getAvailableQuantity() < quantity) {
            log.warn("可用数量不足: availableQuantity={}, borrowQuantity={}", asset.getAvailableQuantity(), quantity);
            return false;
        }

        AssetLoan loan = new AssetLoan();
        loan.setAssetId(assetId);
        loan.setBorrowerId(borrowerId);
        loan.setBorrowQuantity(quantity);
        loan.setBorrowTime(LocalDateTime.now());
        loan.setExpectedReturnTime(expectedReturnTime);
        loan.setBorrowReason(borrowReason);
        loan.setReturnQuantity(0);
        loan.setStatus(AssetLoan.STATUS_BORROWED);
        assetLoanMapper.insert(loan);

        asset.setAvailableQuantity(asset.getAvailableQuantity() - quantity);
        asset.setBorrowedQuantity(asset.getBorrowedQuantity() + quantity);
        assetMapper.updateById(asset);

        log.info("资产领用成功: assetId={}, borrowerId={}, quantity={}, loanId={}", assetId, borrowerId, quantity, loan.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnAsset(Long assetId, Long loanId, Integer quantity, String returnRemark) {
        AssetLoan loan = assetLoanMapper.selectById(loanId);
        if (loan == null) {
            log.warn("领用记录不存在: loanId={}", loanId);
            return false;
        }

        if (!loan.getAssetId().equals(assetId)) {
            log.warn("资产ID不匹配: assetId={}, loanAssetId={}", assetId, loan.getAssetId());
            return false;
        }

        if (loan.isReturned()) {
            log.warn("该领用记录已全部归还: loanId={}", loanId);
            return false;
        }

        if (quantity == null || quantity <= 0) {
            log.warn("归还数量无效: quantity={}", quantity);
            return false;
        }

        int pendingReturn = loan.getBorrowQuantity() - (loan.getReturnQuantity() != null ? loan.getReturnQuantity() : 0);
        if (quantity > pendingReturn) {
            log.warn("归还数量超过待归还数量: quantity={}, pendingReturn={}", quantity, pendingReturn);
            return false;
        }

        Asset asset = assetMapper.selectById(assetId);
        if (asset == null) {
            log.warn("资产不存在: assetId={}", assetId);
            return false;
        }

        loan.setReturnQuantity((loan.getReturnQuantity() != null ? loan.getReturnQuantity() : 0) + quantity);
        loan.setReturnTime(LocalDateTime.now());
        loan.setReturnRemark(returnRemark);

        if (loan.getReturnQuantity() >= loan.getBorrowQuantity()) {
            loan.setStatus(AssetLoan.STATUS_RETURNED);
        } else {
            loan.setStatus(AssetLoan.STATUS_PARTIAL_RETURNED);
        }
        assetLoanMapper.updateById(loan);

        asset.setAvailableQuantity(asset.getAvailableQuantity() + quantity);
        asset.setBorrowedQuantity(asset.getBorrowedQuantity() - quantity);
        assetMapper.updateById(asset);

        log.info("资产归还成功: assetId={}, loanId={}, quantity={}", assetId, loanId, quantity);
        return true;
    }
}
