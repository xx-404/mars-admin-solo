package com.mars.biz.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 激活码表
 *
 * @author Mars
 * @date 2026-04-17
 */
@Data
@TableName("activation_code")
public class ActivationCode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @ExcelProperty(value = "ID", index = 0)
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 激活码
     */
    @ExcelProperty(value = "激活码", index = 1)
    private String activationCode;

    /**
     * 类型：DAY-日，MONTH-月，QUARTER-季，YEAR-年
     */
    @ExcelProperty(value = "类型", index = 2)
    private String durationType;

    /**
     * 天数
     */
    @ExcelProperty(value = "天数", index = 3)
    private Integer durationDays;

    /**
     * 激活状态：0-未激活，1-已激活
     */
    @ExcelProperty(value = "激活状态", index = 4)
    private Integer activationStatus;

    /**
     * 激活用户 ID
     */
    @ExcelProperty(value = "激活用户 ID", index = 5)
    private Long userId;

    /**
     * 激活店铺 ID
     */
    @ExcelProperty(value = "激活店铺 ID", index = 6)
    private Long shopId;

    /**
     * 激活时间
     */
    @ExcelProperty(value = "激活时间", index = 7)
    private LocalDateTime activatedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty(value = "创建时间", index = 8)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ==================== 计算字段（不存数据库） ====================

    /**
     * 类型名称
     */
    @TableField(exist = false)
    private String durationTypeName;

    /**
     * 激活状态名称
     */
    @TableField(exist = false)
    private String activationStatusName;

    /**
     * 激活状态常量
     */
    public static final int STATUS_UNACTIVATED = 0;
    public static final int STATUS_ACTIVATED = 1;

    /**
     * 获取类型名称
     */
    public String getDurationTypeName() {
        if (durationType == null) {
            return null;
        }
        return switch (durationType.toUpperCase()) {
            case "DAY" -> "日";
            case "MONTH" -> "月";
            case "QUARTER" -> "季";
            case "YEAR" -> "年";
            default -> durationType;
        };
    }

    /**
     * 获取激活状态名称
     */
    public String getActivationStatusName() {
        if (activationStatus == null) {
            return "未知";
        }
        return activationStatus == STATUS_ACTIVATED ? "已激活" : "未激活";
    }

    /**
     * 是否已激活
     */
    public boolean isActivated() {
        return activationStatus != null && activationStatus == STATUS_ACTIVATED;
    }
}
