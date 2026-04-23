package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 抖音运费模板表
 *
 * @author Mars
 * @date 2026-04-19
 */
@Data
@TableName("douyin_freight_template")
public class DouyinFreightTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 店铺所有者ID（关联店铺表）
     */
    private String ownerId;

    /**
     * 抖音运费模板ID（外部ID）
     */
    private Long freightId;

    // ==================== 模板基本信息 ====================

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 发货省份ID
     */
    private Long productProvince;

    /**
     * 发货省份名称
     */
    private String productProvinceName;

    /**
     * 发货城市ID
     */
    private Long productCity;

    /**
     * 发货城市名称
     */
    private String productCityName;

    // ==================== 计费设置 ====================

    /**
     * 计费类型: 1-按重量, 2-按件数
     */
    private Long calculateType;

    /**
     * 运送类型: 1-快递, 2-EMS, 3-平邮
     */
    private Long transferType;

    /**
     * 计费规则: 1-自定义, 2-卖家承担运费
     */
    private Long ruleType;

    /**
     * 固定运费（单位: 分）
     */
    private Long fixedAmount;

    /**
     * 计费规则列表（JSON格式）
     */
    private String columns;

    /**
     * 是否更新转运规则
     */
    private Boolean upsertTransferRule;

    // ==================== 状态字段 ====================

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 同步状态: 0-未同步, 1-已同步, 2-同步失败
     */
    private Integer syncStatus;

    /**
     * 最后同步时间
     */
    private LocalDateTime syncTime;

    /**
     * 同步错误信息
     */
    private String syncError;

    // ==================== 时间字段 ====================

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

    // ==================== 计算字段（不存数据库） ====================

    /**
     * 计费类型名称
     */
    @TableField(exist = false)
    private String calculateTypeName;

    /**
     * 运送类型名称
     */
    @TableField(exist = false)
    private String transferTypeName;

    /**
     * 计费规则名称
     */
    @TableField(exist = false)
    private String ruleTypeName;

    /**
     * 状态名称
     */
    @TableField(exist = false)
    private String statusName;

    /**
     * 同步状态名称
     */
    @TableField(exist = false)
    private String syncStatusName;

    // ==================== 状态常量 ====================

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    public static final int SYNC_STATUS_NONE = 0;
    public static final int SYNC_STATUS_DONE = 1;
    public static final int SYNC_STATUS_FAILED = 2;

    // ==================== 计费类型常量 ====================

    public static final long CALCULATE_TYPE_WEIGHT = 1;
    public static final long CALCULATE_TYPE_COUNT = 2;

    // ==================== 运送类型常量 ====================

    public static final long TRANSFER_TYPE_EXPRESS = 1;
    public static final long TRANSFER_TYPE_EMS = 2;
    public static final long TRANSFER_TYPE_POST = 3;

    // ==================== 计费规则常量 ====================

    public static final long RULE_TYPE_CUSTOM = 1;
    public static final long RULE_TYPE_FREE = 2;

    // ==================== 计算方法 ====================

    public String getCalculateTypeName() {
        if (calculateType == null) return "未知";
        return switch (calculateType.intValue()) {
            case 1 -> "按重量";
            case 2 -> "按件数";
            default -> "未知";
        };
    }

    public String getTransferTypeName() {
        if (transferType == null) return "未知";
        return switch (transferType.intValue()) {
            case 1 -> "快递";
            case 2 -> "EMS";
            case 3 -> "平邮";
            default -> "未知";
        };
    }

    public String getRuleTypeName() {
        if (ruleType == null) return "未知";
        return switch (ruleType.intValue()) {
            case 1 -> "自定义";
            case 2 -> "卖家承担";
            default -> "未知";
        };
    }

    public String getStatusName() {
        if (status == null) return "未知";
        return status == STATUS_ENABLED ? "启用" : "禁用";
    }

    public String getSyncStatusName() {
        if (syncStatus == null) return "未知";
        return switch (syncStatus) {
            case SYNC_STATUS_NONE -> "未同步";
            case SYNC_STATUS_DONE -> "已同步";
            case SYNC_STATUS_FAILED -> "同步失败";
            default -> "未知";
        };
    }

    public boolean isEnabled() {
        return status != null && status == STATUS_ENABLED;
    }

    public boolean isSynced() {
        return syncStatus != null && syncStatus == SYNC_STATUS_DONE;
    }
}