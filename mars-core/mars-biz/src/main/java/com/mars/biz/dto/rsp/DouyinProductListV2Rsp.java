package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音商品列表查询响应（从抖音平台查询）
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinProductListV2Rsp {

    /**
     * 商品列表
     */
    private List<ProductItem> data;

    /**
     * 总数量
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long page;

    /**
     * 每页数量
     */
    private Long size;

    /**
     * 游标 ID
     */
    private String cursorId;

    /**
     * 商品项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductItem {

        /**
         * 商品 ID
         */
        private Long productId;

        /**
         * 商品名称
         */
        private String name;

        /**
         * 商品主图
         */
        private String img;

        /**
         * 商品描述
         */
        private String description;

        /**
         * 推荐备注
         */
        private String recommendRemark;

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
         * 支付类型
         */
        private Long payType;

        /**
         * 市场价（分）
         */
        private Long marketPrice;

        /**
         * 折扣价（分）
         */
        private Long discountPrice;

        /**
         * 外部商品 ID
         */
        private String outerProductId;

        /**
         * 运费模板 ID
         */
        private Long freightId;

        /**
         * 联系电话
         */
        private String mobile;

        /**
         * 规格价格列表
         */
        private List<SpecPriceItem> specPrices;

        /**
         * 类目详情
         */
        private CategoryDetail categoryDetail;

        /**
         * 创建时间（时间戳，秒）
         */
        private Long createTime;

        /**
         * 更新时间（时间戳，秒）
         */
        private Long updateTime;

        /**
         * 销量
         */
        private Long sellNum;

        /**
         * 是否组合商品
         */
        private Boolean canCombine;

        /**
         * 是否二手数码商品
         */
        private Boolean isSecondHandDigital;

        /**
         * 是否套餐商品
         */
        private Boolean isPackageProduct;

        /**
         * 店铺分类
         */
        private ShopCategory shopCategory;
    }

    /**
     * 规格价格项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecPriceItem {

        /**
         * 规格 ID
         */
        private Long id;

        /**
         * 规格编码
         */
        private String code;

        /**
         * 条码列表
         */
        private List<String> barcodes;
    }

    /**
     * 类目详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDetail {

        /**
         * 一级类目 ID
         */
        private Long firstCid;

        /**
         * 二级类目 ID
         */
        private Long secondCid;

        /**
         * 三级类目 ID
         */
        private Long thirdCid;

        /**
         * 四级类目 ID
         */
        private Long fourthCid;

        /**
         * 一级类目名称
         */
        private String firstCname;

        /**
         * 二级类目名称
         */
        private String secondCname;

        /**
         * 三级类目名称
         */
        private String thirdCname;

        /**
         * 四级类目名称
         */
        private String fourthCname;
    }

    /**
     * 店铺分类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShopCategory {

        /**
         * 叶子类目 ID 列表
         */
        private List<Long> leafCategoryIds;
    }
}