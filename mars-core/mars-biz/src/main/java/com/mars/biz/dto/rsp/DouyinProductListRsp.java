package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音商品列表响应
 *
 * @author Mars
 * @date 2026-04-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinProductListRsp {

    /**
     * 商品列表
     */
    private List<DouyinProductInfoRsp> list;

    /**
     * 总数量
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer pageSize;
}
