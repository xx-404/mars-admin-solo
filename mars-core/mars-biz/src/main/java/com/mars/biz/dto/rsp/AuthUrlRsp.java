package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 授权链接响应
 *
 * @author Mars
 * @date 2026-04-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUrlRsp {

    /**
     * 授权链接
     */
    private String authUrl;

    /**
     * 授权状态（用于回调验证）
     */
    private String state;

    /**
     * 平台类型
     */
    private String platform;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 订购地址
     */
    private String orderUrl;

    /**
     * 订购服务名称
     */
    private String orderName;

    /**
     * 授权有效期（秒）
     */
    private Long expiresIn;
}