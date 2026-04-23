package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品上下架请求
 *
 * @author Mars
 * @date 2026-04-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusReq {

    /**
     * 店铺所有者ID
     */
    private String ownerId;

    /**
     * 商品ID列表
     */
    private List<String> productIds;

    /**
     * 操作类型：list（上架），unlist（下架）
     */
    private String action;

    /**
     * 授权Token（可选）
     */
    private String authToken;
}