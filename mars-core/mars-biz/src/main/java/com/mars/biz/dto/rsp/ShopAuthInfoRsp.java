package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺授权信息响应
 *
 * @author Mars
 * @date 2026-04-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopAuthInfoRsp {

    /**
     * 店铺ID（平台返回的）
     */
    private String shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺所有者ID
     */
    private String ownerId;

    /**
     * 授权Token
     */
    private String authToken;

    /**
     * 授权过期时间
     */
    private String expireTime;

    /**
     * Token过期时间
     */
    private String tokenExpire;
}