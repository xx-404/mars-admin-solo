package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品列表响应
 *
 * @author Mars
 * @date 2026-04-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListRsp {

    /**
     * 商品列表
     */
    private List<ProductInfoRsp> products;

    /**
     * 总数量
     */
    private Integer total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 同步成功数量
     */
    private Integer synced;

    /**
     * 同步失败数量
     */
    private Integer failed;
}