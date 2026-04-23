package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 激活码响应
 *
 * @author Mars
 * @date 2026-04-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationCodeRsp {

    /**
     * ID
     */
    private Long id;

    /**
     * 激活码
     */
    private String activationCode;

    /**
     * 类型：DAY-日，MONTH-月，QUARTER-季，YEAR-年
     */
    private String durationType;

    /**
     * 类型名称
     */
    private String durationTypeName;

    /**
     * 天数
     */
    private Integer durationDays;

    /**
     * 激活状态：0-未激活，1-已激活
     */
    private Integer activationStatus;

    /**
     * 激活状态名称
     */
    private String activationStatusName;

    /**
     * 激活用户 ID
     */
    private Long userId;

    /**
     * 激活店铺 ID
     */
    private Long shopId;

    /**
     * 激活时间
     */
    private String activatedAt;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;
}
