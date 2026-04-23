package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推荐类目响应
 *
 * @author Mars
 * @date 2026-04-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendCategoryRsp {

    /**
     * 异步任务 ID
     */
    private String asyncTaskId;

    /**
     * 异步任务状态（2-完成）
     */
    private String asyncTaskStatus;

    /**
     * 类目详情列表
     */
    private List<CategoryDetailInfo> categoryDetails;

    /**
     * 轮询间隔（毫秒）
     */
    private String interval;

    /**
     * 推荐 ID
     */
    private String recommendId;

    /**
     * 超时时间（毫秒）
     */
    private String timeout;

    /**
     * 类目详情信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDetailInfo {

        /**
         * 类目明细
         */
        private CategoryInfo categoryDetail;

        /**
         * 资质状态（0-不需要，1-需要）
         */
        private String qualificationStatus;
    }

    /**
     * 类目信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        /**
         * 一级类目 ID
         */
        private String firstCid;

        /**
         * 一级类目名称
         */
        private String firstCname;

        /**
         * 二级类目 ID
         */
        private String secondCid;

        /**
         * 二级类目名称
         */
        private String secondCname;

        /**
         * 三级类目 ID
         */
        private String thirdCid;

        /**
         * 三级类目名称
         */
        private String thirdCname;

        /**
         * 四级类目 ID
         */
        private String fourthCid;

        /**
         * 四级类目名称
         */
        private String fourthCname;
    }
}
