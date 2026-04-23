package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.biz.entity.AssetLoan;

import java.util.List;

/**
 * 资产领用记录 Service
 *
 * @author Mars
 * @date 2026-04-23
 */
public interface AssetLoanService {

    Page<AssetLoan> page(Integer page, Integer pageSize, Long assetId, Long borrowerId, Integer status);

    List<AssetLoan> list(Long assetId, Long borrowerId, Integer status);

    AssetLoan getById(Long id);

    void delete(Long[] ids);

    List<AssetLoan> getByAssetId(Long assetId);

    List<AssetLoan> getByBorrowerId(Long borrowerId);
}
