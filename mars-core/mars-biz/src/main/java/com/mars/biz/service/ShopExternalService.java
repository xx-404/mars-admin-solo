package com.mars.biz.service;

import com.mars.biz.dto.req.ProductStatusReq;
import com.mars.biz.dto.req.RecommendCategoryReq;
import com.mars.biz.dto.rsp.*;
import com.mars.biz.enums.PlatformType;

import java.util.List;

/**
 * 店铺外部接口服务
 * 调用电商平台（拼多多、抖音、小红书）的外部接口
 *
 * @author Mars
 * @date 2026-04-16
 */
public interface ShopExternalService {

    /**
     * 获取授权链接
     *
     * @param platform 平台类型
     * @return 授权链接响应
     */
    ExternalRsp<AuthUrlRsp> getAuthUrl(PlatformType platform);

    /**
     * 获取授权链接（带店铺ID，用于生成state）
     *
     * @param platform 平台类型
     * @param shopId   店铺ID
     * @return 授权链接响应
     */
    ExternalRsp<AuthUrlRsp> getAuthUrl(PlatformType platform, String shopId);

    /**
     * 授权回调处理 - 获取店铺信息
     *
     * @param platform 平台类型
     * @param state    授权状态
     * @return 店铺授权信息
     */
    ExternalRsp<ShopAuthInfoRsp> handleAuthCallback(PlatformType platform, String state);

    /**
     * 抖音平台 - 使用关联码绑定店铺
     *
     * @param bindCode 关联码
     * @param shopName 店铺名称
     * @return 店铺授权信息
     */
    ExternalRsp<ShopAuthInfoRsp> bindDouyinShop(String bindCode, String shopName);


    /**
     * 获取平台订购地址
     *
     * @param platform 平台类型
     * @return 订购地址
     */
    String getOrderUrl(PlatformType platform);

}
