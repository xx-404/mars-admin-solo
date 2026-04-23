package com.mars.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.biz.dto.req.DouyinFreightTemplateQueryReq;
import com.mars.biz.dto.req.DouyinFreightTemplateSaveReq;
import com.mars.biz.dto.req.DouyinFreightTemplateSyncReq;
import com.mars.biz.dto.rsp.DouyinFreightTemplateInfoRsp;
import com.mars.biz.entity.DouyinFreightTemplate;
import com.mars.biz.service.DouyinFreightTemplateService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 抖音运费模板 Controller
 *
 * @author Mars
 * @date 2026-04-19
 */
@RestController
@RequestMapping("/biz/douyin-freight")
@RequiredArgsConstructor
@Validated
public class DouyinFreightTemplateController {

    private final DouyinFreightTemplateService service;

    // ==================== 分页查询 ====================

    /**
     * 分页查询运费模板列表
     */
    @GetMapping("/page")
    @SaCheckPermission("biz:douyin-freight:list")
    public Result<PageResult<DouyinFreightTemplateInfoRsp>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            DouyinFreightTemplateQueryReq req) {
        Page<DouyinFreightTemplate> result = service.page(page, pageSize, req);
        List<DouyinFreightTemplateInfoRsp> list = result.getRecords().stream()
                .map(this::convertToInfoRsp)
                .collect(Collectors.toList());
        return Result.ok(PageResult.of(list, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 列表查询运费模板
     */
    @GetMapping("/list")
    @SaCheckPermission("biz:douyin-freight:list")
    public Result<List<DouyinFreightTemplateInfoRsp>> list(DouyinFreightTemplateQueryReq req) {
        List<DouyinFreightTemplate> entities = service.list(req);
        List<DouyinFreightTemplateInfoRsp> list = entities.stream()
                .map(this::convertToInfoRsp)
                .collect(Collectors.toList());
        return Result.ok(list);
    }

    // ==================== 详情查询 ====================

    /**
     * 获取运费模板详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("biz:douyin-freight:query")
    public Result<DouyinFreightTemplateInfoRsp> getInfo(@PathVariable Long id) {
        DouyinFreightTemplateInfoRsp info = service.getInfoById(id);
        return Result.ok(info);
    }

    /**
     * 根据店铺所有者ID查询运费模板列表
     */
    @GetMapping("/by-owner")
    @SaCheckPermission("biz:douyin-freight:list")
    public Result<List<DouyinFreightTemplateInfoRsp>> getByOwnerId(@RequestParam String ownerId) {
        List<DouyinFreightTemplate> entities = service.getByOwnerId(ownerId);
        List<DouyinFreightTemplateInfoRsp> list = entities.stream()
                .map(this::convertToInfoRsp)
                .collect(Collectors.toList());
        return Result.ok(list);
    }

    // ==================== 新增 ====================

    /**
     * 新增运费模板
     */
    @PostMapping
    @SaCheckPermission("biz:douyin-freight:add")
    @Log(title = "抖音运费模板", businessType = BusinessType.INSERT)
    public Result<DouyinFreightTemplate> add(@Valid @RequestBody DouyinFreightTemplateSaveReq req) {
        DouyinFreightTemplate entity = service.create(req);
        return Result.ok(entity);
    }

    // ==================== 更新 ====================

    /**
     * 更新运费模板
     */
    @PutMapping("/{id}")
    @SaCheckPermission("biz:douyin-freight:edit")
    @Log(title = "抖音运费模板", businessType = BusinessType.UPDATE)
    public Result<DouyinFreightTemplate> update(@PathVariable Long id, @RequestBody DouyinFreightTemplateSaveReq req) {
        req.setId(id);
        DouyinFreightTemplate entity = service.update(req);
        return Result.ok(entity);
    }

    /**
     * 保存运费模板（新增或更新）
     */
    @PostMapping("/save")
    @SaCheckPermission("biz:douyin-freight:edit")
    @Log(title = "抖音运费模板", businessType = BusinessType.UPDATE)
    public Result<DouyinFreightTemplate> save(@RequestBody DouyinFreightTemplateSaveReq req) {
        DouyinFreightTemplate entity = service.save(req);
        return Result.ok(entity);
    }

    // ==================== 删除 ====================

    /**
     * 批量删除运费模板
     */
    @DeleteMapping("/{ids}")
    @SaCheckPermission("biz:douyin-freight:remove")
    @Log(title = "抖音运费模板", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] ids) {
        service.delete(ids);
        return Result.ok();
    }

    // ==================== 启用/禁用 ====================

    /**
     * 启用运费模板
     */
    @PutMapping("/{id}/enable")
    @SaCheckPermission("biz:douyin-freight:edit")
    @Log(title = "抖音运费模板", businessType = BusinessType.UPDATE)
    public Result<Void> enable(@PathVariable Long id) {
        service.enable(id);
        return Result.ok();
    }

    /**
     * 禁用运费模板
     */
    @PutMapping("/{id}/disable")
    @SaCheckPermission("biz:douyin-freight:edit")
    @Log(title = "抖音运费模板", businessType = BusinessType.UPDATE)
    public Result<Void> disable(@PathVariable Long id) {
        service.disable(id);
        return Result.ok();
    }

    // ==================== 同步操作 ====================

    /**
     * 从抖音平台同步运费模板
     */
    @PostMapping("/sync")
    @SaCheckPermission("biz:douyin-freight:sync")
    @Log(title = "抖音运费模板同步", businessType = BusinessType.OTHER)
    public Result<Void> syncFromDouyin(@Valid @RequestBody DouyinFreightTemplateSyncReq req) {
        service.syncFromDouyin(req);
        return Result.ok();
    }

    /**
     * 将运费模板推送到抖音平台
     */
    @PostMapping("/{id}/push")
    @SaCheckPermission("biz:douyin-freight:sync")
    @Log(title = "抖音运费模板推送", businessType = BusinessType.OTHER)
    public Result<Void> pushToDouyin(@PathVariable Long id) {
        service.pushToDouyin(id);
        return Result.ok();
    }

    // ==================== 辅助方法 ====================

    private DouyinFreightTemplateInfoRsp convertToInfoRsp(DouyinFreightTemplate entity) {
        return DouyinFreightTemplateInfoRsp.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .freightId(entity.getFreightId())
                .templateName(entity.getTemplateName())
                .productProvince(entity.getProductProvince())
                .productProvinceName(entity.getProductProvinceName())
                .productCity(entity.getProductCity())
                .productCityName(entity.getProductCityName())
                .calculateType(entity.getCalculateType())
                .calculateTypeName(entity.getCalculateTypeName())
                .transferType(entity.getTransferType())
                .transferTypeName(entity.getTransferTypeName())
                .ruleType(entity.getRuleType())
                .ruleTypeName(entity.getRuleTypeName())
                .fixedAmount(entity.getFixedAmount())
                .columns(entity.getColumns())
                .upsertTransferRule(entity.getUpsertTransferRule())
                .status(entity.getStatus())
                .statusName(entity.getStatusName())
                .syncStatus(entity.getSyncStatus())
                .syncStatusName(entity.getSyncStatusName())
                .syncTime(entity.getSyncTime())
                .syncError(entity.getSyncError())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}