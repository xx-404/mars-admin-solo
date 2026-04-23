package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音素材分页查询请求
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinMaterialSearchMaterialReq {

    /**
     * 店铺所有者 ID
     */
    private String ownerId;

    /**
     * 素材 ID（精确查询）
     */
    private String materialId;

    /**
     * 素材名称（模糊搜索）
     */
    private String materialName;

    /**
     * 素材类型列表：photo-图片，video-视频
     */
    private List<String> materialType;

    /**
     * 操作状态列表：0-正常，1-删除
     */
    private List<Integer> operateStatus;

    /**
     * 审核状态列表：0-审核中，1-审核通过，2-审核失败
     */
    private List<Integer> auditStatus;

    /**
     * 创建时间起始（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String createTimeStart;

    /**
     * 创建时间结束（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String createTimeEnd;

    /**
     * 文件夹 ID
     */
    private String folderId;

    /**
     * 素材 ID 列表（批量查询）
     */
    private List<String> materialIdList;

    /**
     * 页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 排序类型：0-创建时间升序，1-创建时间降序，2-更新时间升序，3-更新时间降序
     */
    private Integer orderType;
}