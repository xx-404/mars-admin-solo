package com.mars.biz.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺表
 *
 * @author Mars
 * @date 2026-04-16
 */
@Data
@TableName("shop")
public class Shop implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @ExcelProperty(value = "ID", index = 0)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    @ExcelProperty(value = "用户 ID", index = 1)
    private Long userId;

    /**
     * 平台类型：PINDUODUO-拼多多，DOUYIN-抖音，XIAOHONGSHU-小红书
     */
    @ExcelProperty(value = "平台", index = 2)
    private String platform;

    /**
     * 店铺名称
     */
    @ExcelProperty(value = "店铺名称", index = 3)
    private String shopName;

    /**
     * 店铺编码
     */
    @ExcelProperty(value = "店铺编码", index = 4)
    private String shopCode;

    /**
     * 外部店铺 ID（平台返回的店铺 ID）
     */
    @ExcelProperty(value = "外部店铺 ID", index = 5)
    private String shopIdExternal;

    /**
     * 授权 Token
     */
    private String authToken;

    /**
     * 授权状态：0-未授权，1-已授权
     */
    @ExcelProperty(value = "授权状态", index = 6)
    private Integer authStatus;

    /**
     * 激活状态：0-未激活，1-已激活
     */
    @ExcelProperty(value = "激活状态", index = 7)
    private Integer activationStatus;

    /**
     * 激活时间
     */
    @ExcelProperty(value = "激活时间", index = 8)
    private LocalDateTime activatedAt;

    /**
     * 过期时间
     */
    @ExcelProperty(value = "过期时间", index = 9)
    private LocalDateTime expiresAt;

    /**
     * 备注（JSON 格式，存储授权 state 等信息）
     */
    private String remark;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty(value = "创建时间", index = 10)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ==================== 计算字段（不存数据库） ====================

    /**
     * 剩余天数（计算得出）
     */
    @TableField(exist = false)
    private Integer remainingDays;

    /**
     * 平台名称
     */
    @TableField(exist = false)
    private String platformName;

    /**
     * 授权状态名称
     */
    @TableField(exist = false)
    private String authStatusName;

    /**
     * 激活状态名称
     */
    @TableField(exist = false)
    private String activationStatusName;

    // ==================== 授权状态常量 ====================

    public static final int AUTH_STATUS_UNAUTHORIZED = 0;
    public static final int AUTH_STATUS_AUTHORIZED = 1;

    // ==================== 激活状态常量 ====================

    public static final int ACTIVATION_STATUS_INACTIVE = 0;
    public static final int ACTIVATION_STATUS_ACTIVE = 1;

    /**
     * 计算剩余天数
     */
    public Integer getRemainingDays() {
        if (expiresAt == null) {
            return 0;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expiresAt);
        return days > 0 ? (int) days : 0;
    }

    /**
     * 获取平台名称
     */
    public String getPlatformName() {
        if (platform == null) {
            return null;
        }
        return switch (platform.toUpperCase()) {
            case "PINDUODUO" -> "拼多多";
            case "DOUYIN" -> "抖音";
            case "XIAOHONGSHU" -> "小红书";
            default -> platform;
        };
    }

    /**
     * 获取授权状态名称
     */
    public String getAuthStatusName() {
        if (authStatus == null) {
            return "未知";
        }
        return authStatus == AUTH_STATUS_AUTHORIZED ? "已授权" : "未授权";
    }

    /**
     * 获取激活状态名称
     */
    public String getActivationStatusName() {
        if (activationStatus == null) {
            return "未知";
        }
        return activationStatus == ACTIVATION_STATUS_ACTIVE ? "已激活" : "未激活";
    }

    /**
     * 是否已授权
     */
    public boolean isAuthorized() {
        return authStatus != null && authStatus == AUTH_STATUS_AUTHORIZED;
    }

    /**
     * 是否已激活
     */
    public boolean isActivated() {
        return activationStatus != null && activationStatus == ACTIVATION_STATUS_ACTIVE;
    }

    /**
     * 是否有效（已授权且已激活且未过期）
     */
    public boolean isValid() {
        return isAuthorized() && isActivated() && (expiresAt == null || expiresAt.isAfter(LocalDateTime.now()));
    }
}
