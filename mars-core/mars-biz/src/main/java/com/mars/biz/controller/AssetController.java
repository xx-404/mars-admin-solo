package com.mars.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.mars.biz.entity.Asset;
import com.mars.biz.entity.AssetLoan;
import com.mars.biz.service.AssetLoanService;
import com.mars.biz.service.AssetService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产管理 Controller
 *
 * @author Mars
 * @date 2026-04-23
 */
@RestController
@RequestMapping("/biz/asset")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final AssetLoanService assetLoanService;

    // ==================== 资产 CRUD ====================

    @GetMapping("/page")
    @SaCheckPermission("biz:asset:list")
    public Result<PageResult<Asset>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) Integer status) {
        var result = assetService.page(page, pageSize, assetName, assetType, status);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/list")
    @SaCheckPermission("biz:asset:list")
    public Result<List<Asset>> list(
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) Integer status) {
        List<Asset> assets = assetService.list(assetName, assetType, status);
        return Result.ok(assets);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("biz:asset:query")
    public Result<Asset> getInfo(@PathVariable Long id) {
        Asset asset = assetService.getById(id);
        return Result.ok(asset);
    }

    @PostMapping
    @SaCheckPermission("biz:asset:add")
    @Log(title = "资产管理", businessType = BusinessType.INSERT)
    public Result<Asset> add(@RequestBody Asset asset) {
        Asset created = assetService.create(asset);
        return Result.ok(created);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("biz:asset:edit")
    @Log(title = "资产管理", businessType = BusinessType.UPDATE)
    public Result<Void> update(@PathVariable Long id, @RequestBody Asset asset) {
        asset.setId(id);
        assetService.update(asset);
        return Result.ok();
    }

    @DeleteMapping("/{ids}")
    @SaCheckPermission("biz:asset:remove")
    @Log(title = "资产管理", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] ids) {
        assetService.delete(ids);
        return Result.ok();
    }

    // ==================== 领用管理 ====================

    @PostMapping("/{id}/borrow")
    @SaCheckPermission("biz:asset:borrow")
    @Log(title = "资产领用", businessType = BusinessType.UPDATE)
    public Result<Void> borrow(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String borrowReason,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime expectedReturnTime) {
        Long borrowerId = StpUtil.getLoginIdAsLong();
        boolean success = assetService.borrowAsset(id, borrowerId, quantity, borrowReason, expectedReturnTime);
        if (!success) {
            return Result.fail("领用失败，请检查资产状态和可用数量");
        }
        return Result.ok();
    }

    @PostMapping("/{id}/return")
    @SaCheckPermission("biz:asset:return")
    @Log(title = "资产归还", businessType = BusinessType.UPDATE)
    public Result<Void> returnAsset(
            @PathVariable Long id,
            @RequestParam Long loanId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String returnRemark) {
        boolean success = assetService.returnAsset(id, loanId, quantity, returnRemark);
        if (!success) {
            return Result.fail("归还失败，请检查领用记录状态和归还数量");
        }
        return Result.ok();
    }

    // ==================== 领用记录查询 ====================

    @GetMapping("/loan/page")
    @SaCheckPermission("biz:asset:list")
    public Result<PageResult<AssetLoan>> loanPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) Long borrowerId,
            @RequestParam(required = false) Integer status) {
        var result = assetLoanService.page(page, pageSize, assetId, borrowerId, status);
        return Result.ok(PageResult.of(result));
    }

    @GetMapping("/loan/list")
    @SaCheckPermission("biz:asset:list")
    public Result<List<AssetLoan>> loanList(
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) Long borrowerId,
            @RequestParam(required = false) Integer status) {
        List<AssetLoan> loans = assetLoanService.list(assetId, borrowerId, status);
        return Result.ok(loans);
    }

    @GetMapping("/loan/{id}")
    @SaCheckPermission("biz:asset:query")
    public Result<AssetLoan> getLoanInfo(@PathVariable Long id) {
        AssetLoan loan = assetLoanService.getById(id);
        return Result.ok(loan);
    }

    @GetMapping("/loan/asset/{assetId}")
    @SaCheckPermission("biz:asset:query")
    public Result<List<AssetLoan>> getLoansByAssetId(@PathVariable Long assetId) {
        List<AssetLoan> loans = assetLoanService.getByAssetId(assetId);
        return Result.ok(loans);
    }

    @GetMapping("/loan/borrower/{borrowerId}")
    @SaCheckPermission("biz:asset:query")
    public Result<List<AssetLoan>> getLoansByBorrowerId(@PathVariable Long borrowerId) {
        List<AssetLoan> loans = assetLoanService.getByBorrowerId(borrowerId);
        return Result.ok(loans);
    }

    @DeleteMapping("/loan/{ids}")
    @SaCheckPermission("biz:asset:remove")
    @Log(title = "领用记录", businessType = BusinessType.DELETE)
    public Result<Void> removeLoan(@PathVariable Long[] ids) {
        assetLoanService.delete(ids);
        return Result.ok();
    }
}
