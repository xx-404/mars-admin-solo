package com.mars.biz.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抖音运费模板保存请求
 *
 * @author Mars
 * @date 2026-04-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinFreightTemplateSaveReq {

    /**
     * 主键ID（编辑时必填）
     */
    private Long id;

    /**
     * 店铺所有者ID
     */
    @NotBlank(message = "店铺所有者ID不能为空")
    private String ownerId;

    /**
     * 抖音运费模板ID（外部ID）
     */
    private Long freightId;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称不能超过100字符")
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

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
}