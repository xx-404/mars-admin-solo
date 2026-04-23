package com.mars.biz.dto.rsp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 抖音商品信息响应
 *
 * @author Mars
 * @date 2026-04-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinProductInfoRsp {

    /**
     * 商品 ID
     */
    private Long id;

    /**
     * 店铺所有者 ID
     */
    private String ownerId;

    /**
     * 抖音商品 ID
     */
    private String productId;

    /**
     * 外部商品 ID
     */
    private String outerProductId;

    // ==================== 基本信息 ====================

    /**
     * 商品类型
     */
    private Long productType;

    /**
     * 商品类型名称
     */
    private String productTypeName;

    /**
     * 叶子类目 ID
     */
    private Long categoryLeafId;

    /**
     * 类目名称
     */
    private String categoryName;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品主图 URL
     */
    private String pic;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 推荐备注
     */
    private String recommendRemark;

    // ==================== 价格库存 ====================

    /**
     * 支付方式
     */
    private Long payType;

    /**
     * 商品价格（元）
     */
    private BigDecimal price;

    /**
     * 原价/划线价（元）
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
     * 库存扣减方式
     */
    private Long reduceType;

    // ==================== 物流信息 ====================

    /**
     * 配送方式
     */
    private Integer deliveryMethod;

    /**
     * 运费模板 ID
     */
    private Long freightId;

    /**
     * 商品重量
     */
    private BigDecimal weight;

    /**
     * 重量单位
     */
    private Long weightUnit;

    /**
     * 发货延迟天数
     */
    private Long deliveryDelayDay;

    // ==================== 预售信息 ====================

    /**
     * 预售类型
     */
    private Long presellType;

    /**
     * 预售延迟天数
     */
    private Long presellDelay;

    /**
     * 预售结束时间
     */
    private String presellEndTime;

    /**
     * 预售配置等级
     */
    private Long presellConfigLevel;

    /**
     * 预售发货类型
     */
    private Long presellDeliveryType;

    // ==================== 规格 SKU ====================

    /**
     * 规格名称
     */
    private String specName;

    /**
     * 规格信息（JSON）
     */
    private String specs;

    /**
     * 规格价格（JSON）
     */
    private String specPrices;

    /**
     * 规格图片（JSON）
     */
    private String specPic;

    // ==================== 限购设置 ====================

    /**
     * 每单最大购买数量
     */
    private Long maximumPerOrder;

    /**
     * 每单最小购买数量
     */
    private Long minimumPerOrder;

    /**
     * 每人限购数量
     */
    private Long limitPerBuyer;

    // ==================== 商品属性 ====================

    /**
     * 商品属性（JSON）
     */
    private String productFormatNew;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 标准品牌 ID
     */
    private Long standardBrandId;

    // ==================== 其他设置 ====================

    /**
     * 联系电话
     */
    private String mobile;

    /**
     * 是否支持 7 天无理由
     */
    private Long supply7dayReturn;

    /**
     * 商家备注
     */
    private String remark;

    // ==================== 状态字段 ====================

    /**
     * 状态：0-草稿，1-审核中，2-已上架，3-已下架，4-审核驳回，5-已删除
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核驳回
     */
    private Integer auditStatus;

    /**
     * 审核状态名称
     */
    private String auditStatusName;

    /**
     * 审核驳回原因
     */
    private String auditRemark;

    // ==================== 时间字段 ====================

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 上架时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
