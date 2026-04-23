package com.mars.biz.service;

import com.doudian.open.api.brand_list.data.BrandListData;
import com.doudian.open.api.brand_list.param.BrandListParam;
import com.doudian.open.api.product_GetRecommendCategory.data.ProductGetRecommendCategoryData;
import com.doudian.open.api.product_GetRecommendCategory.param.ProductGetRecommendCategoryParam;
import com.doudian.open.api.product_addV2.data.ProductAddV2Data;
import com.doudian.open.api.product_addV2.param.ProductAddV2Param;
import com.doudian.open.api.product_detail.data.ProductDetailData;
import com.doudian.open.api.product_detail.param.ProductDetailParam;
import com.doudian.open.api.product_listV2.data.ProductListV2Data;
import com.doudian.open.api.product_listV2.param.ProductListV2Param;
import com.doudian.open.api.product_setOffline.data.ProductSetOfflineData;
import com.doudian.open.api.product_setOffline.param.ProductSetOfflineParam;
import com.doudian.open.api.product_setOnline.data.ProductSetOnlineData;
import com.doudian.open.api.product_setOnline.param.ProductSetOnlineParam;
import com.doudian.open.api.freightTemplate_create.data.FreightTemplateCreateData;
import com.doudian.open.api.freightTemplate_create.param.FreightTemplateCreateParam;
import com.doudian.open.api.freightTemplate_detail.data.FreightTemplateDetailData;
import com.doudian.open.api.freightTemplate_detail.param.FreightTemplateDetailParam;
import com.doudian.open.api.freightTemplate_list.data.FreightTemplateListData;
import com.doudian.open.api.freightTemplate_list.param.FreightTemplateListParam;
import com.doudian.open.api.freightTemplate_update.data.FreightTemplateUpdateData;
import com.doudian.open.api.freightTemplate_update.param.FreightTemplateUpdateParam;
import com.doudian.open.api.material_batchUploadImageSync.data.MaterialBatchUploadImageSyncData;
import com.doudian.open.api.material_batchUploadImageSync.param.MaterialBatchUploadImageSyncParam;
import com.doudian.open.api.material_searchFolder.data.MaterialSearchFolderData;
import com.doudian.open.api.material_searchFolder.param.MaterialSearchFolderParam;
import com.doudian.open.api.material_searchMaterial.data.MaterialSearchMaterialData;
import com.doudian.open.api.material_searchMaterial.param.MaterialSearchMaterialParam;
import com.doudian.open.api.shop_getShopCategory.data.ShopGetShopCategoryData;
import com.doudian.open.api.shop_getShopCategory.param.ShopGetShopCategoryParam;
import com.doudian.open.api.product_getCatePropertyV2.data.ProductGetCatePropertyV2Data;
import com.doudian.open.api.product_getCatePropertyV2.param.ProductGetCatePropertyV2Param;
import com.mars.biz.dto.rsp.AuthUrlRsp;
import com.mars.biz.dto.rsp.ExternalRsp;
import com.mars.biz.dto.rsp.ShopAuthInfoRsp;

/**
 * 抖音外部接口服务
 * 调用抖音电商平台外部接口
 *
 * @author Mars
 * @date 2026-04-18
 */
public interface DouyinExternalService {

    /**
     * 获取授权链接
     *
     * @param shopId 店铺 ID
     * @return 授权链接响应
     */
    ExternalRsp<AuthUrlRsp> getAuthUrl(String shopId);

    /**
     * 授权回调处理 - 获取店铺信息
     *
     * @param state 授权状态
     * @return 店铺授权信息
     */
    ExternalRsp<ShopAuthInfoRsp> handleAuthCallback(String state);

    /**
     * 使用关联码绑定店铺
     *
     * @param bindCode 关联码
     * @param shopName 店铺名称
     * @return 店铺授权信息
     */
    ExternalRsp<ShopAuthInfoRsp> bindDouyinShop(String bindCode, String shopName);

    /**
     * 获取推荐类目
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 推荐类目响应
     */
    ExternalRsp<ProductGetRecommendCategoryData> getRecommendCategory(String ownerId, ProductGetRecommendCategoryParam req);

    /**
     * 发布商品
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 商品列表响应
     */
    ExternalRsp<ProductAddV2Data> addProduct(String ownerId, ProductAddV2Param req);

    /**
     * 商品分页查询
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 商品列表响应
     */
    ExternalRsp<ProductListV2Data> listProduct(String ownerId, ProductListV2Param req);

    /**
     * 商品上架
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 上架响应
     */
    ExternalRsp<ProductSetOnlineData> setProductOnline(String ownerId, ProductSetOnlineParam req);

    /**
     * 商品下架
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 下架响应
     */
    ExternalRsp<ProductSetOfflineData> setProductOffline(String ownerId, ProductSetOfflineParam req);

    /**
     * 查询商品详情
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 商品详情响应
     */
    ExternalRsp<ProductDetailData> getProductDetail(String ownerId, ProductDetailParam req);

    /**
     * 运费模板分页查询
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 运费模板列表响应
     */
    ExternalRsp<FreightTemplateListData> listFreightTemplate(String ownerId, FreightTemplateListParam req);

    /**
     * 运费模板新增
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 新增响应
     */
    ExternalRsp<FreightTemplateCreateData> createFreightTemplate(String ownerId, FreightTemplateCreateParam req);

    /**
     * 运费模板更新
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 更新响应
     */
    ExternalRsp<FreightTemplateUpdateData> updateFreightTemplate(String ownerId, FreightTemplateUpdateParam req);

    /**
     * 运费模板详情
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 运费模板详情响应
     */
    ExternalRsp<FreightTemplateDetailData> getFreightTemplateDetail(String ownerId, FreightTemplateDetailParam req);

    /**
     * 批量上传图片
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 批量上传图片响应
     */
    ExternalRsp<MaterialBatchUploadImageSyncData> batchUploadImage(String ownerId, MaterialBatchUploadImageSyncParam req);

    /**
     * 分页查询素材文件夹
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 素材文件夹响应
     */
    ExternalRsp<MaterialSearchFolderData> searchMaterialFolder(String ownerId, MaterialSearchFolderParam req);

    /**
     * 分页查询素材
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 素材响应
     */
    ExternalRsp<MaterialSearchMaterialData> searchMaterial(String ownerId, MaterialSearchMaterialParam req);

    /**
     * 查询品牌列表
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 品牌列表响应
     */
    ExternalRsp<BrandListData> listBrand(String ownerId, BrandListParam req);

    /**
     * 查询店铺类目列表
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 店铺类目响应
     */
    ExternalRsp<ShopGetShopCategoryData> listShopCategory(String ownerId, ShopGetShopCategoryParam req);

    /**
     * 查询商品类目属性
     *
     * @param ownerId 店铺所有者 ID
     * @param req     请求报文
     * @return 商品类目属性响应
     */
    ExternalRsp<ProductGetCatePropertyV2Data> getCateProperty(String ownerId, ProductGetCatePropertyV2Param req);

    /**
     * 获取平台订购地址
     *
     * @return 订购地址
     */
    String getOrderUrl();

    /**
     * 获取平台名称
     *
     * @return 平台名称
     */
    String getPlatformName();
}
