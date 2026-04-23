package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抖音运费模板查询请求
 *
 * @author Mars
 * @date 2026-04-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinFreightTemplateQueryReq {

    /**
     * 店铺所有者ID
     */
    private String ownerId;

    /**
     * 模板名称（模糊查询）
     */
    private String templateName;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 同步状态: 0-未同步, 1-已同步, 2-同步失败
     */
    private Integer syncStatus;

    /**
     * 页码（默认1）
     */
    private Integer page = 1;

    /**
     * 每页数量（默认10）
     */
    private Integer pageSize = 10;
}