package com.mars.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.mars.biz.dto.req.DouyinBatchUploadImageReq;
import com.mars.biz.dto.req.DouyinBrandListReq;
import com.mars.biz.dto.req.DouyinCatePropertyReq;
import com.mars.biz.dto.req.DouyinFreightTemplateListReq;
import com.mars.biz.dto.req.DouyinMaterialSearchFolderReq;
import com.mars.biz.dto.req.DouyinMaterialSearchMaterialReq;
import com.mars.biz.dto.req.DouyinProductListV2Req;
import com.mars.biz.dto.req.DouyinProductQueryReq;
import com.mars.biz.dto.req.DouyinProductSaveReq;
import com.mars.biz.dto.req.DouyinShopCategoryReq;
import com.mars.biz.dto.rsp.DouyinBatchUploadImageRsp;
import com.mars.biz.dto.rsp.DouyinBrandListRsp;
import com.mars.biz.dto.rsp.DouyinCatePropertyRsp;
import com.mars.biz.dto.rsp.DouyinFreightTemplateListRsp;
import com.mars.biz.dto.rsp.DouyinMaterialSearchFolderRsp;
import com.mars.biz.dto.rsp.DouyinMaterialSearchMaterialRsp;
import com.mars.biz.dto.rsp.DouyinProductListV2Rsp;
import com.mars.biz.dto.rsp.DouyinProductInfoRsp;
import com.mars.biz.dto.rsp.DouyinProductListRsp;
import com.mars.biz.dto.rsp.DouyinShopCategoryRsp;
import com.mars.biz.entity.DouyinProduct;
import com.mars.biz.service.DouyinProductService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 抖音商品管理 Controller
 *
 * @author Mars
 * @date 2026-04-18
 */
@RestController
@RequestMapping("/biz/douyin-product")
@RequiredArgsConstructor
public class DouyinProductController {

    private final DouyinProductService douyinProductService;

    /**
     * 分页查询商品列表
     */
    @GetMapping("/page")
    @SaCheckPermission("biz:douyin-product:list")
    public Result<PageResult<DouyinProductInfoRsp>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            DouyinProductQueryReq req) {
        var result = douyinProductService.page(page, pageSize, req);
        var list = result.getRecords().stream()
                .map(this::convertToInfoRsp)
                .toList();
        return Result.ok(PageResult.of(list, result.getTotal(), (long) page, (long) pageSize));
    }

    /**
     * 查询商品列表（带响应 DTO）
     */
    @GetMapping("/list")
    @SaCheckPermission("biz:douyin-product:list")
    public Result<DouyinProductListRsp> list(DouyinProductQueryReq req) {
        DouyinProductListRsp result = douyinProductService.list(req);
        return Result.ok(result);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("biz:douyin-product:query")
    public Result<DouyinProductInfoRsp> getInfo(@PathVariable Long id) {
        DouyinProductInfoRsp info = douyinProductService.getInfoById(id);
        if (info == null) {
            return Result.fail("商品不存在");
        }
        return Result.ok(info);
    }

    /**
     * 新增商品
     */
    @PostMapping
    @SaCheckPermission("biz:douyin-product:add")
    @Log(title = "抖音商品管理", businessType = BusinessType.INSERT)
    public Result<DouyinProduct> add(@RequestBody DouyinProductSaveReq req) {
        req.setId(null);
        DouyinProduct created = douyinProductService.create(req);
        return Result.ok(created);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    @SaCheckPermission("biz:douyin-product:edit")
    @Log(title = "抖音商品管理", businessType = BusinessType.UPDATE)
    public Result<DouyinProduct> update(@PathVariable Long id, @RequestBody DouyinProductSaveReq req) {
        req.setId(id);
        DouyinProduct updated = douyinProductService.update(req);
        return Result.ok(updated);
    }

    /**
     * 保存商品（新增或更新）
     */
    @PostMapping("/save")
    @SaCheckPermission("biz:douyin-product:edit")
    @Log(title = "抖音商品管理", businessType = BusinessType.UPDATE)
    public Result<DouyinProduct> save(@RequestBody DouyinProductSaveReq req) {
        DouyinProduct saved = douyinProductService.save(req);
        return Result.ok(saved);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{ids}")
    @SaCheckPermission("biz:douyin-product:remove")
    @Log(title = "抖音商品管理", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] ids) {
        douyinProductService.delete(ids);
        return Result.ok();
    }

    /**
     * 上架商品
     */
    @PutMapping("/{id}/on-sale")
    @SaCheckPermission("biz:douyin-product:edit")
    @Log(title = "抖音商品管理", businessType = BusinessType.UPDATE)
    public Result<Void> onSale(@PathVariable Long id) {
        douyinProductService.onSale(id);
        return Result.ok();
    }

    /**
     * 下架商品
     */
    @PutMapping("/{id}/off-sale")
    @SaCheckPermission("biz:douyin-product:edit")
    @Log(title = "抖音商品管理", businessType = BusinessType.UPDATE)
    public Result<Void> offSale(@PathVariable Long id) {
        douyinProductService.offSale(id);
        return Result.ok();
    }

    /**
     * 提交审核
     */
    @PutMapping("/{id}/submit-audit")
    @SaCheckPermission("biz:douyin-product:edit")
    @Log(title = "抖音商品管理", businessType = BusinessType.UPDATE)
    public Result<Void> submitAudit(@PathVariable Long id) {
        douyinProductService.submitAudit(id);
        return Result.ok();
    }

    /**
     * 根据 ownerId 和 productId 查询商品
     */
    @GetMapping("/external")
    public Result<DouyinProductInfoRsp> getByOwnerIdAndProductId(
            @RequestParam String ownerId,
            @RequestParam String productId) {
        DouyinProduct product = douyinProductService.getByOwnerIdAndProductId(ownerId, productId);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        return Result.ok(convertToInfoRsp(product));
    }

    /**
     * 批量上传图片到抖音素材库
     */
    @PostMapping("/batch-upload-image")
    @SaCheckPermission("biz:douyin-product:batch-upload-image")
    @Log(title = "抖音商品管理", businessType = BusinessType.UPDATE)
    public Result<DouyinBatchUploadImageRsp> batchUploadImage(@RequestBody DouyinBatchUploadImageReq req) {
        DouyinBatchUploadImageRsp result = douyinProductService.batchUploadImage(req);
        return Result.ok(result);
    }

    /**
     * 查询品牌列表
     */
    @GetMapping("/brand/list")
    @SaCheckPermission("biz:douyin-product:list")
    public Result<DouyinBrandListRsp> listBrand(DouyinBrandListReq req) {
        DouyinBrandListRsp result = douyinProductService.listBrand(req);
        return Result.ok(result);
    }

    /**
     * 查询店铺类目列表
     */
    @GetMapping("/shop-category/list")
    @SaCheckPermission("biz:douyin-product:list")
    public Result<DouyinShopCategoryRsp> listShopCategory(DouyinShopCategoryReq req) {
        DouyinShopCategoryRsp result = douyinProductService.listShopCategory(req);
        return Result.ok(result);
    }

    /**
     * 查询运费模板列表
     */
    @GetMapping("/freight-template/list")
    @SaCheckPermission("biz:douyin-product:list")
    public Result<DouyinFreightTemplateListRsp> listFreightTemplate(DouyinFreightTemplateListReq req) {
        DouyinFreightTemplateListRsp result = douyinProductService.listFreightTemplate(req);
        return Result.ok(result);
    }

    /**
     * 查询商品类目属性
     */
    @GetMapping("/cate-property")
    @SaCheckPermission("biz:douyin-product:list")
    public Result<DouyinCatePropertyRsp> getCateProperty(DouyinCatePropertyReq req) {
        DouyinCatePropertyRsp result = douyinProductService.getCateProperty(req);
        return Result.ok(result);
    }

    /**
     * 分页查询素材文件夹
     */
    @GetMapping("/material-folder/search")
    @SaCheckPermission("biz:douyin-product:list")
    public Result<DouyinMaterialSearchFolderRsp> searchMaterialFolder(DouyinMaterialSearchFolderReq req) {
        DouyinMaterialSearchFolderRsp result = douyinProductService.searchMaterialFolder(req);
        return Result.ok(result);
    }

    /**
     * 分页查询素材
     */
    @GetMapping("/material/search")
    @SaCheckPermission("biz:douyin-product:list")
    public Result<DouyinMaterialSearchMaterialRsp> searchMaterial(DouyinMaterialSearchMaterialReq req) {
        DouyinMaterialSearchMaterialRsp result = douyinProductService.searchMaterial(req);
        return Result.ok(result);
    }

    /**
     * 分页查询商品列表（从抖音平台查询）
     */
    @GetMapping("/product/list")
    @SaCheckPermission("biz:douyin-product:list")
    public Result<DouyinProductListV2Rsp> listProductV2(DouyinProductListV2Req req) {
        DouyinProductListV2Rsp result = douyinProductService.listProductV2(req);
        return Result.ok(result);
    }

    // ==================== 辅助方法 ====================

    /**
     * 将实体转换为响应 DTO
     */
    private DouyinProductInfoRsp convertToInfoRsp(DouyinProduct product) {
        if (product == null) {
            return null;
        }
        DouyinProductInfoRsp rsp = new DouyinProductInfoRsp();
        rsp.setId(product.getId());
        rsp.setOwnerId(product.getOwnerId());
        rsp.setProductId(product.getProductId());
        rsp.setOuterProductId(product.getOuterProductId());
        rsp.setProductType(product.getProductType());
        rsp.setProductTypeName(getProductTypeName(product.getProductType()));
        rsp.setCategoryLeafId(product.getCategoryLeafId());
        rsp.setName(product.getName());
        rsp.setPic(product.getPic());
        rsp.setDescription(product.getDescription());
        rsp.setRecommendRemark(product.getRecommendRemark());
        rsp.setPayType(product.getPayType());
        rsp.setPrice(product.getPrice());
        rsp.setOriginalPrice(product.getOriginalPrice());
        rsp.setCostPrice(product.getCostPrice());
        rsp.setStockNum(product.getStockNum());
        rsp.setReduceType(product.getReduceType());
        rsp.setDeliveryMethod(product.getDeliveryMethod());
        rsp.setFreightId(product.getFreightId());
        rsp.setWeight(product.getWeight());
        rsp.setWeightUnit(product.getWeightUnit());
        rsp.setDeliveryDelayDay(product.getDeliveryDelayDay());
        rsp.setPresellType(product.getPresellType());
        rsp.setPresellDelay(product.getPresellDelay());
        rsp.setPresellEndTime(product.getPresellEndTime());
        rsp.setPresellConfigLevel(product.getPresellConfigLevel());
        rsp.setPresellDeliveryType(product.getPresellDeliveryType());
        rsp.setSpecName(product.getSpecName());
        rsp.setSpecs(product.getSpecs());
        rsp.setSpecPrices(product.getSpecPrices());
        rsp.setSpecPic(product.getSpecPic());
        rsp.setMaximumPerOrder(product.getMaximumPerOrder());
        rsp.setMinimumPerOrder(product.getMinimumPerOrder());
        rsp.setLimitPerBuyer(product.getLimitPerBuyer());
        rsp.setProductFormatNew(product.getProductFormatNew());
        rsp.setSpuId(product.getSpuId());
        rsp.setStandardBrandId(product.getStandardBrandId());
        rsp.setMobile(product.getMobile());
        rsp.setSupply7dayReturn(product.getSupply7dayReturn());
        rsp.setRemark(product.getRemark());
        rsp.setStatus(product.getStatus());
        rsp.setStatusName(product.getStatusName());
        rsp.setAuditStatus(product.getAuditStatus());
        rsp.setAuditStatusName(product.getAuditStatusName());
        rsp.setAuditRemark(product.getAuditRemark());
        rsp.setCreateTime(product.getCreateTime());
        rsp.setUpdateTime(product.getUpdateTime());
        rsp.setPublishTime(product.getPublishTime());
        return rsp;
    }

    /**
     * 获取商品类型名称
     */
    private String getProductTypeName(Long productType) {
        if (productType == null) {
            return "普通商品";
        }
        return productType == 1 ? "普通商品" : "电子面单商品";
    }
}
