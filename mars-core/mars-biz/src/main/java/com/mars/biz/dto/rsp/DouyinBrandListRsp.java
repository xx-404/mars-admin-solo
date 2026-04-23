package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 抖音品牌列表响应
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinBrandListRsp {

    /**
     * 品牌 ID 列表
     */
    private List<Long> brandIds;

    /**
     * 品牌详细信息（key为品牌ID）
     */
    private Map<Long, BrandInfoItem> brandInfos;

    /**
     * 总数量
     */
    private Long total;

    /**
     * 是否有更多数据
     */
    private Boolean hasMore;

    /**
     * 是否需要授权
     */
    private Boolean authRequired;

    /**
     * 已授权品牌列表
     */
    private List<BrandSimpleItem> authBrandList;

    /**
     * 品牌列表（简略信息）
     */
    private List<BrandSimpleItem> brandList;

    /**
     * 品牌详细信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandInfoItem {

        /**
         * 品牌 ID
         */
        private Long brandId;

        /**
         * 品牌中文名
         */
        private String brandNameCN;

        /**
         * 品牌英文名
         */
        private String brandNameEN;

        /**
         * 品牌等级
         */
        private Integer level;

        /**
         * 状态
         */
        private Integer status;

        /**
         * 品牌别名列表
         */
        private List<String> brandAlias;

        /**
         * 创建时间戳
         */
        private Long createTimestamp;

        /**
         * 更新时间戳
         */
        private Long updateTimestamp;

        /**
         * 审核状态
         */
        private Integer auditStatus;

        /**
         * 业务类型
         */
        private Integer bizType;

        /**
         * 品牌Logo
         */
        private String logo;
    }

    /**
     * 品牌简略信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandSimpleItem {

        /**
         * 品牌 ID
         */
        private Long brandId;

        /**
         * 品牌中文名
         */
        private String nameCn;

        /**
         * 品牌英文名
         */
        private String nameEn;
    }
}