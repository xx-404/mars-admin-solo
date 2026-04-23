package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抖音店铺类目查询请求
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinShopCategoryReq {

    /**
     * 店铺所有者 ID
     */
    private String ownerId;

    /**
     * 类目 ID（父类目 ID，获取子类目列表）
     */
    private Long cid;

    /**
     * 渠道类型：0-所有渠道，1-自营，2-直播带货
     */
    private Integer channel;
}