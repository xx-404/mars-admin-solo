package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音品牌列表查询请求
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinBrandListReq {

    /**
     * 店铺所有者 ID
     */
    private String ownerId;

    /**
     * 类目 ID 列表
     */
    private List<Long> categories;

    /**
     * 品牌类目 ID
     */
    private Long categoryId;

    /**
     * 品牌 ID 列表
     */
    private List<Long> brandIds;

    /**
     * 搜索关键词
     */
    private String query;

    /**
     * 偏移量（分页）
     */
    private Long offset;

    /**
     * 每页数量
     */
    private Long size;

    /**
     * 排序方式
     */
    private Integer sort;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 是否返回完整品牌信息
     */
    private Boolean fullBrandInfo;
}