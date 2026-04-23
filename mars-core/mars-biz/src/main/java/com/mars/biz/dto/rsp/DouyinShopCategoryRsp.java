package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音店铺类目响应
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinShopCategoryRsp {

    /**
     * 类目列表
     */
    private List<CategoryItem> list;

    /**
     * 类目项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryItem {

        /**
         * 类目 ID
         */
        private Long id;

        /**
         * 类目名称
         */
        private String name;

        /**
         * 类目层级
         */
        private Long level;

        /**
         * 父类目 ID
         */
        private Long parentId;

        /**
         * 是否叶子节点
         */
        private Boolean isLeaf;

        /**
         * 是否启用
         */
        private Boolean enable;

        /**
         * 渠道类型
         */
        private Integer channel;
    }
}