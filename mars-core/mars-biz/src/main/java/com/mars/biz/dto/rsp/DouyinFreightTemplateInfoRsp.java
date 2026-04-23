package com.mars.biz.dto.rsp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 抖音运费模板详情响应
 *
 * @author Mars
 * @date 2026-04-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinFreightTemplateInfoRsp {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 店铺所有者ID
     */
    private String ownerId;

    /**
     * 抖音运费模板ID（外部ID）
     */
    private Long freightId;

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

    /**
     * 计费类型
     */
    private Long calculateType;

    /**
     * 计费类型名称
     */
    private String calculateTypeName;

    /**
     * 运送类型
     */
    private Long transferType;

    /**
     * 运送类型名称
     */
    private String transferTypeName;

    /**
     * 计费规则
     */
    private Long ruleType;

    /**
     * 计费规则名称
     */
    private String ruleTypeName;

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

    /**
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 同步状态
     */
    private Integer syncStatus;

    /**
     * 同步状态名称
     */
    private String syncStatusName;

    /**
     * 最后同步时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime syncTime;

    /**
     * 同步错误信息
     */
    private String syncError;

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
}