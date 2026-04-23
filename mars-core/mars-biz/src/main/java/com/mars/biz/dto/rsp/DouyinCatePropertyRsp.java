package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音商品类目属性响应
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinCatePropertyRsp {

    /**
     * 属性列表
     */
    private List<PropertyItem> list;

    /**
     * 模板类型
     */
    private Long tplType;

    /**
     * 属性项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyItem {

        /**
         * 属性 ID
         */
        private Long propertyId;

        /**
         * 属性名称
         */
        private String propertyName;

        /**
         * 类目 ID
         */
        private Long categoryId;

        /**
         * 属性类型：1-关键属性，2-销售属性，3-非关键属性
         */
        private Long propertyType;

        /**
         * 是否必填
         */
        private Long required;

        /**
         * 状态
         */
        private Long status;

        /**
         * 排序
         */
        private Long sequence;

        /**
         * 关联 ID
         */
        private Long relationId;

        /**
         * 输入类型
         */
        private String type;

        /**
         * 是否有子属性
         */
        private Boolean hasSubProperty;

        /**
         * 最大多选数量
         */
        private Long multiSelectMax;

        /**
         * 自定义类型
         */
        private Long diyType;

        /**
         * 重要类型
         */
        private Long importantType;

        /**
         * 功能类型列表
         */
        private List<Long> featureTypeList;

        /**
         * 属性选项列表
         */
        private List<OptionItem> options;

        /**
         * 图片规则
         */
        private PicRule propertyPicRule;

        /**
         * 计量模板列表
         */
        private List<MeasureTemplateItem> measureTemplates;
    }

    /**
     * 属性选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionItem {

        /**
         * 选项名称
         */
        private String name;

        /**
         * 选项值
         */
        private String value;

        /**
         * 选项 ID
         */
        private Long valueId;

        /**
         * 排序
         */
        private Long sequence;
    }

    /**
     * 图片规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PicRule {

        /**
         * 是否可用
         */
        private Boolean available;

        /**
         * 是否必填
         */
        private Boolean required;
    }

    /**
     * 计量模板
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MeasureTemplateItem {

        /**
         * 模板 ID
         */
        private Long templateId;

        /**
         * 显示名称
         */
        private String displayName;

        /**
         * 输入规则
         */
        private InputRule inputRule;

        /**
         * 值模块列表
         */
        private List<ValueModuleItem> valueModules;
    }

    /**
     * 输入规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InputRule {

        /**
         * 依赖检查规则
         */
        private List<DependencyCheckRuleItem> dependencyCheckRules;

        /**
         * 全局检查规则
         */
        private List<GlobalCheckRuleItem> globalCheckRules;
    }

    /**
     * 值模块
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueModuleItem {

        /**
         * 模块 ID
         */
        private Long moduleId;

        /**
         * 输入类型
         */
        private String inputType;

        /**
         * 前缀
         */
        private String prefix;

        /**
         * 后缀
         */
        private String suffix;

        /**
         * 是否必填
         */
        private Boolean valueRequired;

        /**
         * 验证规则
         */
        private ValidateRule validateRule;

        /**
         * 单位列表
         */
        private List<UnitItem> units;

        /**
         * 值列表
         */
        private List<ValueItem> values;
    }

    /**
     * 依赖检查规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DependencyCheckRuleItem {

        /**
         * 模板 ID
         */
        private Long templateId;

        /**
         * 规则约束
         */
        private RuleConstraint ruleConstraint;

        /**
         * 规则条件
         */
        private RuleCondition ruleCondition;
    }

    /**
     * 全局检查规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalCheckRuleItem {

        /**
         * 模板 ID
         */
        private Long templateId;

        /**
         * 模块 ID
         */
        private Long moduleId;

        /**
         * 操作符
         */
        private String operator;

        /**
         * 目标值
         */
        private String targetValue;

        /**
         * 错误消息
         */
        private String errMsg;
    }

    /**
     * 验证规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidateRule {

        /**
         * 数据类型
         */
        private String dataType;

        /**
         * 最小值
         */
        private Double min;

        /**
         * 最大值
         */
        private Double max;

        /**
         * 精度
         */
        private Long precision;

        /**
         * 时间格式
         */
        private String timeFormat;

        /**
         * 单位规则
         */
        private List<UnitRuleItem> unitRules;
    }

    /**
     * 单位项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitItem {

        /**
         * 单位 ID
         */
        private Long unitId;

        /**
         * 单位名称
         */
        private String unitName;
    }

    /**
     * 值项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueItem {

        /**
         * 值名称
         */
        private String valueName;
    }

    /**
     * 规则约束
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleConstraint {

        /**
         * 模块 ID
         */
        private Long moduleId;

        /**
         * 组函数
         */
        private String groupFunc;

        /**
         * 操作符
         */
        private String operator;

        /**
         * 值
         */
        private String value;

        /**
         * 错误消息
         */
        private String errMsg;
    }

    /**
     * 规则条件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleCondition {

        /**
         * 模块 ID
         */
        private Long moduleId;

        /**
         * 操作符
         */
        private String operator;

        /**
         * 值
         */
        private String value;
    }

    /**
     * 单位规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitRuleItem {

        /**
         * 单位 ID
         */
        private Long unitId;

        /**
         * 最小值
         */
        private Double min;

        /**
         * 最大值
         */
        private Double max;
    }
}