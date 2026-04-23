package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 推荐类目请求
 * 参考：https://op.jinritemai.com/docs/api-docs/14/2004
 *
 * @author Mars
 * @date 2026-04-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendCategoryReq {

    /**
     * 场景类型
     * category_infer: 基于标题、图片等推断商品类目
     * predict_by_title_and_img: 基于标题、图片等推断商品类目（图片必传）
     * 示例值：product_info
     */
    private String scene;

    /**
     * 商品主图图片 URL 列表
     * scene 为 predict_by_title_and_img 时必传
     */
    private List<String> pic;

    /**
     * 商品类目 id，无需传递（由接口返回）
     * 示例值：20415
     */
    private Long categoryLeafId;

    /**
     * 商品标题
     * 示例值：商品标题
     */
    private String name;

    /**
     * 商品类目属性
     */
    private Map<Long, String> productFormatNew;

    /**
     * 品牌 id
     * 示例值：30241
     */
    private Long standardBrandId;

    /**
     * 深度思考异步任务 ID
     * 命中深度思考时返回，用于轮询深度思考结果
     * 示例值：task-123
     */
    private String asyncTaskId;

}
