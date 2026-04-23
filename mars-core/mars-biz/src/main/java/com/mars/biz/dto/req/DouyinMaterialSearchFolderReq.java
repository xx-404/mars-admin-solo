package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音素材文件夹分页查询请求
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinMaterialSearchFolderReq {

    /**
     * 店铺所有者 ID
     */
    private String ownerId;

    /**
     * 文件夹 ID
     */
    private String folderId;

    /**
     * 父文件夹 ID
     */
    private String parentFolderId;

    /**
     * 文件夹名称（模糊搜索）
     */
    private String name;

    /**
     * 创建时间开始（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String createTimeStart;

    /**
     * 创建时间结束（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String createTimeEnd;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 排序方式：0-创建时间升序，1-创建时间降序
     */
    private Integer orderBy;

    /**
     * 操作状态列表：0-正常，1-删除
     */
    private List<Integer> operateStatus;
}