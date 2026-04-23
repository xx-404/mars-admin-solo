package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 抖音商品保存请求
 *
 * @author Mars
 * @date 2026-04-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinProductSaveReq {

    /**
     * 商品 ID（编辑时必填）
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
     * 外部商品 ID（商家自定义）
     */
    private String outerProductId;

    // ==================== 基本信息 ====================

    /**
     * 商品类型：1-普通商品，2-电子面单商品
     */
    private Long productType;

    /**
     * 叶子类目 ID
     */
    private Long categoryLeafId;

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
     * 支付方式：0-全款，1-定金 + 尾款
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
     * 库存扣减方式：1-付款减库存，2-发货减库存
     */
    private Long reduceType;

    // ==================== 物流信息 ====================

    /**
     * 配送方式：1-快递，2-上门取件，3-商家自配
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
     * 重量单位：1-kg，2-g，3-jin
     */
    private Long weightUnit;

    /**
     * 发货延迟天数
     */
    private Long deliveryDelayDay;

    // ==================== 预售信息 ====================

    /**
     * 预售类型：0-非预售，1-全款预售，2-定金预售
     */
    private Long presellType;

    /**
     * 预售延迟天数
     */
    private Long presellDelay;

    /**
     * 预售结束时间（时间戳）
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

    /**
     * 资质列表（JSON）
     */
    private String qualityList;

    // ==================== 其他设置 ====================

    /**
     * 跨境购类目
     */
    private String cdfCategory;

    /**
     * 关联商品 ID 列表
     */
    private String assocIds;

    /**
     * 联系电话
     */
    private String mobile;

    /**
     * 是否支持 7 天无理由：0-不支持，1-支持
     */
    private Long supply7dayReturn;

    /**
     * 是否提交审核
     */
    private Boolean commit;

    /**
     * 商家备注
     */
    private String remark;

    // ==================== 状态 ====================

    /**
     * 状态：0-草稿，1-审核中，2-已上架，3-已下架，4-审核驳回，5-已删除
     */
    private Integer status;
}
