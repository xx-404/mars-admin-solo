package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品信息响应
 *
 * @author Mars
 * @date 2026-04-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInfoRsp {

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 主图URL
     */
    private String imageUrl;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 状态：on_sale, off_sale, deleted, draft, auditing, rejected 等
     */
    private String status;

    /**
     * 类目名称
     */
    private String category;

    /**
     * SKU规格列表
     */
    private List<SkuInfo> specs;

    /**
     * SKU信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuInfo {
        /**
         * SKU ID
         */
        private String skuId;

        /**
         * 规格名称
         */
        private String specName;

        /**
         * 价格
         */
        private BigDecimal price;

        /**
         * 库存
         */
        private Integer stock;
    }
}