package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品上下架响应
 *
 * @author Mars
 * @date 2026-04-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatusRsp {

    /**
     * 操作类型
     */
    private String action;

    /**
     * 影响的商品数量
     */
    private Integer affected;

    /**
     * 失败数量
     */
    private Integer failedCount;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 操作消息
     */
    private String message;
}