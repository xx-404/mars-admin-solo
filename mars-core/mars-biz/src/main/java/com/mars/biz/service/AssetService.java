package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.biz.entity.Asset;
import com.mars.biz.entity.AssetLoan;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产 Service
 *
 * @author Mars
 * @date 2026-04-23
 */
public interface AssetService {

    Page<Asset> page(Integer page, Integer pageSize, String assetName, String assetType, Integer status);

    List<Asset> list(String assetName, String assetType, Integer status);

    Asset getById(Long id);

    Asset create(Asset asset);

    void update(Asset asset);

    void delete(Long[] ids);

    boolean borrowAsset(Long assetId, Long borrowerId, Integer quantity, String borrowReason, LocalDateTime expectedReturnTime);

    boolean returnAsset(Long assetId, Long loanId, Integer quantity, String returnRemark);
}
