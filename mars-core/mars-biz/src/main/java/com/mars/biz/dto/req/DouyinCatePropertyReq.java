package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抖音商品类目属性查询请求
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinCatePropertyReq {

    /**
     * 店铺所有者 ID
     */
    private String ownerId;

    /**
     * 类目叶子节点 ID
     */
    private Long categoryLeafId;
}