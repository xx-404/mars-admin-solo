package com.mars.biz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.biz.entity.Asset;
import com.mars.biz.entity.AssetLoan;
import com.mars.biz.mapper.AssetLoanMapper;
import com.mars.biz.mapper.AssetMapper;
import com.mars.biz.service.AssetLoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 资产领用记录 Service 实现
 *
 * @author Mars
 * @date 2026-04-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetLoanServiceImpl implements AssetLoanService {

    private final AssetLoanMapper assetLoanMapper;
    private final AssetMapper assetMapper;

    @Override
    public Page<AssetLoan> page(Integer page, Integer pageSize, Long assetId, Long borrowerId, Integer status) {
        Page<AssetLoan> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<AssetLoan> wrapper = new LambdaQueryWrapper<>();

        if (assetId != null) {
            wrapper.eq(AssetLoan::getAssetId, assetId);
        }
        if (borrowerId != null) {
            wrapper.eq(AssetLoan::getBorrowerId, borrowerId);
        }
        if (status != null) {
            wrapper.eq(AssetLoan::getStatus, status);
        }

        wrapper.orderByDesc(AssetLoan::getCreateTime);
        Page<AssetLoan> result = assetLoanMapper.selectPage(pageParam, wrapper);

        fillAssetInfo(result.getRecords());
        return result;
    }

    @Override
    public List<AssetLoan> list(Long assetId, Long borrowerId, Integer status) {
        LambdaQueryWrapper<AssetLoan> wrapper = new LambdaQueryWrapper<>();

        if (assetId != null) {
            wrapper.eq(AssetLoan::getAssetId, assetId);
        }
        if (borrowerId != null) {
            wrapper.eq(AssetLoan::getBorrowerId, borrowerId);
        }
        if (status != null) {
            wrapper.eq(AssetLoan::getStatus, status);
        }

        wrapper.orderByDesc(AssetLoan::getCreateTime);
        List<AssetLoan> result = assetLoanMapper.selectList(wrapper);
        fillAssetInfo(result);
        return result;
    }

    @Override
    public AssetLoan getById(Long id) {
        AssetLoan loan = assetLoanMapper.selectById(id);
        if (loan != null) {
            fillAssetInfo(List.of(loan));
        }
        return loan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        assetLoanMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public List<AssetLoan> getByAssetId(Long assetId) {
        LambdaQueryWrapper<AssetLoan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetLoan::getAssetId, assetId);
        wrapper.orderByDesc(AssetLoan::getCreateTime);
        List<AssetLoan> result = assetLoanMapper.selectList(wrapper);
        fillAssetInfo(result);
        return result;
    }

    @Override
    public List<AssetLoan> getByBorrowerId(Long borrowerId) {
        LambdaQueryWrapper<AssetLoan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetLoan::getBorrowerId, borrowerId);
        wrapper.orderByDesc(AssetLoan::getCreateTime);
        List<AssetLoan> result = assetLoanMapper.selectList(wrapper);
        fillAssetInfo(result);
        return result;
    }

    private void fillAssetInfo(List<AssetLoan> loans) {
        for (AssetLoan loan : loans) {
            if (loan.getAssetId() != null) {
                Asset asset = assetMapper.selectById(loan.getAssetId());
                if (asset != null) {
                    loan.setAssetName(asset.getAssetName());
                    loan.setAssetCode(asset.getAssetCode());
                }
            }
        }
    }
}
