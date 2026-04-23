package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 激活码查询请求
 *
 * @author Mars
 * @date 2026-04-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationCodeQueryReq {

    /**
     * 激活码（模糊查询）
     */
    private String activationCode;

    /**
     * 类型：DAY-日，MONTH-月，QUARTER-季，YEAR-年
     */
    private String durationType;

    /**
     * 激活状态：0-未激活，1-已激活
     */
    private Integer activationStatus;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 店铺 ID
     */
    private Long shopId;
}
