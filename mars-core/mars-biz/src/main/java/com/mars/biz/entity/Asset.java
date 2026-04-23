package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mars.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资产表
 *
 * @author Mars
 * @date 2026-04-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset")
public class Asset extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 资产名称
     */
    private String assetName;

    /**
     * 资产编码
     */
    private String assetCode;

    /**
     * 资产类型
     */
    private String assetType;

    /**
     * 规格型号
     */
    private String specification;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 总数量
     */
    private Integer totalQuantity;

    /**
     * 可用数量
     */
    private Integer availableQuantity;

    /**
     * 已领用数量
     */
    private Integer borrowedQuantity;

    /**
     * 资产状态：0-维修中，1-正常，2-报废
     */
    private Integer status;

    /**
     * 存放位置
     */
    private String location;

    /**
     * 购买日期
     */
    private LocalDate purchaseDate;

    /**
     * 购买价格
     */
    private BigDecimal purchasePrice;

    /**
     * 负责人ID
     */
    private Long managerId;

    /**
     * 备注
     */
    private String remark;

    public static final int STATUS_MAINTENANCE = 0;
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_SCRAP = 2;

    public String getStatusName() {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case STATUS_MAINTENANCE -> "维修中";
            case STATUS_NORMAL -> "正常";
            case STATUS_SCRAP -> "报废";
            default -> "未知";
        };
    }

    public boolean isAvailable() {
        return status != null && status == STATUS_NORMAL && availableQuantity != null && availableQuantity > 0;
    }
}
