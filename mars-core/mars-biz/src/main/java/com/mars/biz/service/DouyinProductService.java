package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

/**
 * 抖音商品 Service
 *
 * @author Mars
 * @date 2026-04-18
 */
public interface DouyinProductService {

    /**
     * 分页查询商品列表
     *
     * @param page 页码
     * @param pageSize 每页数量
     * @param req 查询条件
     * @return 分页结果
     */
    Page<DouyinProduct> page(Integer page, Integer pageSize, DouyinProductQueryReq req);

    /**
     * 查询商品列表（带响应 DTO）
     *
     * @param req 查询条件
     * @return 商品列表响应
     */
    DouyinProductListRsp list(DouyinProductQueryReq req);

    /**
     * 根据 ID 查询商品
     *
     * @param id 商品 ID
     * @return 商品信息
     */
    DouyinProduct getById(Long id);

    /**
     * 根据 ID 查询商品（带响应 DTO）
     *
     * @param id 商品 ID
     * @return 商品信息
     */
    DouyinProductInfoRsp getInfoById(Long id);

    /**
     * 新增商品
     *
     * @param req 商品保存请求
     * @return 新增后的商品
     */
    DouyinProduct create(DouyinProductSaveReq req);

    /**
     * 更新商品
     *
     * @param req 商品保存请求
     * @return 更新后的商品
     */
    DouyinProduct update(DouyinProductSaveReq req);

    /**
     * 保存商品（新增或更新）
     *
     * @param req 商品保存请求
     * @return 保存后的商品
     */
    DouyinProduct save(DouyinProductSaveReq req);

    /**
     * 删除商品
     *
     * @param ids 商品 ID 数组
     */
    void delete(Long[] ids);

    /**
     * 删除商品（单个）
     *
     * @param id 商品 ID
     */
    void deleteById(Long id);

    /**
     * 上架商品
     *
     * @param id 商品 ID
     */
    void onSale(Long id);

    /**
     * 下架商品
     *
     * @param id 商品 ID
     */
    void offSale(Long id);

    /**
     * 提交审核
     *
     * @param id 商品 ID
     */
    void submitAudit(Long id);

    /**
     * 根据 ownerId 和 productId 查询商品
     *
     * @param ownerId   店铺所有者 ID
     * @param productId 抖音商品 ID
     * @return 商品信息
     */
    DouyinProduct getByOwnerIdAndProductId(String ownerId, String productId);

    /**
     * 批量上传图片到抖音素材库
     *
     * @param req 批量上传图片请求
     * @return 批量上传图片响应
     */
    DouyinBatchUploadImageRsp batchUploadImage(DouyinBatchUploadImageReq req);

    /**
     * 查询品牌列表
     *
     * @param req 品牌列表查询请求
     * @return 品牌列表响应
     */
    DouyinBrandListRsp listBrand(DouyinBrandListReq req);

    /**
     * 查询店铺类目列表
     *
     * @param req 店铺类目查询请求
     * @return 店铺类目响应
     */
    DouyinShopCategoryRsp listShopCategory(DouyinShopCategoryReq req);

    /**
     * 查询运费模板列表
     *
     * @param req 运费模板列表查询请求
     * @return 运费模板列表响应
     */
    DouyinFreightTemplateListRsp listFreightTemplate(DouyinFreightTemplateListReq req);

    /**
     * 查询商品类目属性
     *
     * @param req 商品类目属性查询请求
     * @return 商品类目属性响应
     */
    DouyinCatePropertyRsp getCateProperty(DouyinCatePropertyReq req);

    /**
     * 分页查询素材文件夹
     *
     * @param req 素材文件夹查询请求
     * @return 素材文件夹响应
     */
    DouyinMaterialSearchFolderRsp searchMaterialFolder(DouyinMaterialSearchFolderReq req);

    /**
     * 分页查询商品列表（从抖音平台查询）
     *
     * @param req 商品列表查询请求
     * @return 商品列表响应
     */
    DouyinProductListV2Rsp listProductV2(DouyinProductListV2Req req);

    /**
     * 分页查询素材
     *
     * @param req 素材查询请求
     * @return 素材响应
     */
    DouyinMaterialSearchMaterialRsp searchMaterial(DouyinMaterialSearchMaterialReq req);
}
