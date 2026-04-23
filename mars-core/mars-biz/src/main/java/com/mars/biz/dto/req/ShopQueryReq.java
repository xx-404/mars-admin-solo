package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 店铺查询请求
 *
 * @author Mars
 * @date 2026-04-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopQueryReq {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 平台类型：PINDUODUO-拼多多, DOUYIN-抖音, XIAOHONGSHU-小红书
     */
    private String platform;

    /**
     * 店铺名称（模糊查询）
     */
    private String shopName;

    /**
     * 授权状态：0-未授权, 1-已授权
     */
    private Integer authStatus;

    /**
     * 激活状态：0-未激活, 1-已激活
     */
    private Integer activationStatus;

    /**
     * 外部店铺ID
     */
    private String shopIdExternal;
}