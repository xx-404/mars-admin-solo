package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 抖音商品 SKU 表
 *
 * @author Mars
 * @date 2026-04-18
 */
@Data
@TableName("douyin_product_sku")
public class DouyinProductSku implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品 ID（关联 douyin_product.id）
     */
    private Long productId;

    /**
     * 抖音 SKU ID
     */
    private String skuId;

    /**
     * SKU 名称
     */
    private String skuName;

    /**
     * 规格详情（JSON）
     */
    private String specDetail;

    /**
     * SKU 价格（元）
     */
    private BigDecimal price;

    /**
     * 原价（元）
     */
    private BigDecimal originalPrice;

    /**
     * 成本价（元）
     */
    private BigDecimal costPrice;

    /**
     * 库存数量
     */
    private Integer stockNum;

    /**
     * SKU 图片 URL
     */
    private String skuPic;

    /**
     * 规格编码
     */
    private String specCodes;

    /**
     * 商品条码
     */
    private String barcode;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
