package com.mars.biz.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mars.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 资产领用记录表
 *
 * @author Mars
 * @date 2026-04-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("asset_loan")
public class AssetLoan extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 领用人ID
     */
    private Long borrowerId;

    /**
     * 领用数量
     */
    private Integer borrowQuantity;

    /**
     * 领用时间
     */
    private LocalDateTime borrowTime;

    /**
     * 预计归还时间
     */
    private LocalDateTime expectedReturnTime;

    /**
     * 领用原因
     */
    private String borrowReason;

    /**
     * 归还数量
     */
    private Integer returnQuantity;

    /**
     * 归还时间
     */
    private LocalDateTime returnTime;

    /**
     * 归还备注
     */
    private String returnRemark;

    /**
     * 领用状态：0-领用中，1-已归还，2-部分归还
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private String assetName;

    @TableField(exist = false)
    private String assetCode;

    @TableField(exist = false)
    private String borrowerName;

    public static final int STATUS_BORROWED = 0;
    public static final int STATUS_RETURNED = 1;
    public static final int STATUS_PARTIAL_RETURNED = 2;

    public String getStatusName() {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case STATUS_BORROWED -> "领用中";
            case STATUS_RETURNED -> "已归还";
            case STATUS_PARTIAL_RETURNED -> "部分归还";
            default -> "未知";
        };
    }

    public boolean isBorrowed() {
        return status != null && status == STATUS_BORROWED;
    }

    public boolean isPartialReturned() {
        return status != null && status == STATUS_PARTIAL_RETURNED;
    }

    public boolean isReturned() {
        return status != null && status == STATUS_RETURNED;
    }
}
