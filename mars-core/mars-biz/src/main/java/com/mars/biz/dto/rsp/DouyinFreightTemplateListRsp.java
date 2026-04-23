package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音运费模板列表响应
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinFreightTemplateListRsp {

    /**
     * 模板列表
     */
    private List<TemplateItem> list;

    /**
     * 总数量
     */
    private Long count;

    /**
     * 运费模板项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateItem {

        /**
         * 模板 ID
         */
        private Long id;

        /**
         * 模板名称
         */
        private String templateName;

        /**
         * 发货省份
         */
        private String productProvince;

        /**
         * 发货城市
         */
        private String productCity;

        /**
         * 计费类型：1-按件计费，2-按重量计费
         */
        private Long calculateType;

        /**
         * 运送类型：1-快递，2-EMS，3-平邮
         */
        private Long transferType;

        /**
         * 规则类型
         */
        private Long ruleType;

        /**
         * 固定运费（包邮时使用）
         */
        private Long fixedAmount;
    }
}