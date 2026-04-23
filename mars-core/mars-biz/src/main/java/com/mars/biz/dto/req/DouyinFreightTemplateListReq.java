package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抖音运费模板列表查询请求
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinFreightTemplateListReq {

    /**
     * 店铺所有者 ID
     */
    private String ownerId;

    /**
     * 模板名称（模糊搜索）
     */
    private String name;

    /**
     * 页码
     */
    private String page;

    /**
     * 每页数量
     */
    private String size;
}