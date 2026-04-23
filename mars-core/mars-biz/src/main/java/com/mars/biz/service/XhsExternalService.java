package com.mars.biz.service;

import com.mars.biz.dto.req.ProductStatusReq;
import com.mars.biz.dto.rsp.*;

import java.util.List;

/**
 * 小红书外部接口服务
 * 调用小红书电商平台外部接口
 *
 * @author Mars
 * @date 2026-04-18
 */
public interface XhsExternalService {

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
     * 同步商品列表
     *
     * @param ownerId  店铺所有者 ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 商品列表响应
     */
    ExternalRsp<ProductListRsp> syncProducts(String ownerId, Integer page, Integer pageSize);

    /**
     * 商品上架
     *
     * @param ownerId    店铺所有者 ID
     * @param productIds 商品 ID 列表
     * @return 商品状态响应
     */
    ExternalRsp<ProductStatusRsp> listProducts(String ownerId, List<String> productIds);

    /**
     * 商品下架
     *
     * @param ownerId    店铺所有者 ID
     * @param productIds 商品 ID 列表
     * @return 商品状态响应
     */
    ExternalRsp<ProductStatusRsp> unlistProducts(String ownerId, List<String> productIds);

    /**
     * 批量更新商品上下架状态
     *
     * @param req 商品状态请求
     * @return 商品状态响应
     */
    ExternalRsp<ProductStatusRsp> updateProductStatus(ProductStatusReq req);

    /**
     * 查询商品详情
     *
     * @param ownerId   店铺所有者 ID
     * @param productId 商品 ID
     * @return 商品信息
     */
    ExternalRsp<ProductInfoRsp> getProductDetail(String ownerId, String productId);

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
