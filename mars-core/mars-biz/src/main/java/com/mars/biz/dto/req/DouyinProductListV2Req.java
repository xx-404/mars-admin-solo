package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音商品列表查询请求（从抖音平台查询）
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinProductListV2Req {

    /**
     * 店铺所有者 ID
     */
    private String ownerId;

    /**
     * 商品名称（模糊搜索）
     */
    private String name;

    /**
     * 商品 ID 列表
     */
    private List<String> productId;

    /**
     * SKU 编码列表
     */
    private List<String> skuCodes;

    /**
     * 商品状态：0-创建中，1-审核中，2-审核通过，3-审核驳回，4-已上架，5-已下架，6-已删除
     */
    private Long status;

    /**
     * 审核状态：0-未审核，1-审核中，2-审核通过，3-审核驳回
     */
    private Long checkStatus;

    /**
     * 商品类型：0-普通商品，1-电子面单商品
     */
    private Long productType;

    /**
     * 门店 ID
     */
    private Long storeId;

    /**
     * 创建时间起始（时间戳，秒）
     */
    private Long startTime;

    /**
     * 创建时间结束（时间戳，秒）
     */
    private Long endTime;

    /**
     * 更新时间起始（时间戳，秒）
     */
    private Long updateStartTime;

    /**
     * 更新时间结束（时间戳，秒）
     */
    private Long updateEndTime;

    /**
     * 页码
     */
    private Long page;

    /**
     * 每页数量
     */
    private Long size;

    /**
     * 是否使用游标分页
     */
    private Boolean useCursor;

    /**
     * 游标 ID
     */
    private String cursorId;

    /**
     * 是否可组合商品
     */
    private Boolean canCombineProduct;

    /**
     * 是否需要整改信息
     */
    private Boolean needRectificationInfo;

    /**
     * 是否需要审核拒绝建议
     */
    private Boolean needCheckOut;

    /**
     * 查询选项
     */
    private QueryOptions queryOptions;

    /**
     * 查询选项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryOptions {

        /**
         * 是否存在类目错放
         */
        private Boolean existCategoryMisplaced;

        /**
         * 是否存在审核拒绝建议
         */
        private Boolean existAuditRejectSuggest;

        /**
         * 是否需要审核拒绝建议
         */
        private Boolean needAuditRejectSuggest;
    }
}