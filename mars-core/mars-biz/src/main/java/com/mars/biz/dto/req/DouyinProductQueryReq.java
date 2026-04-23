package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抖音商品查询请求
 *
 * @author Mars
 * @date 2026-04-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinProductQueryReq {

    /**
     * 店铺所有者 ID
     */
    private String ownerId;

    /**
     * 商品 ID（抖音商品 ID）
     */
    private String productId;

    /**
     * 外部商品 ID
     */
    private String outerProductId;

    /**
     * 商品名称（模糊查询）
     */
    private String name;

    /**
     * 叶子类目 ID
     */
    private Long categoryLeafId;

    /**
     * 状态：0-草稿，1-审核中，2-已上架，3-已下架，4-审核驳回，5-已删除
     */
    private Integer status;

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核驳回
     */
    private Integer auditStatus;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
