package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音素材文件夹分页查询响应
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinMaterialSearchFolderRsp {

    /**
     * 文件夹信息列表
     */
    private List<FolderInfoItem> folderInfoList;

    /**
     * 总数量
     */
    private Long total;

    /**
     * 文件夹信息项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FolderInfoItem {

        /**
         * 文件夹 ID
         */
        private String folderId;

        /**
         * 文件夹类型
         */
        private Integer folderType;

        /**
         * 文件夹名称
         */
        private String name;

        /**
         * 操作状态：0-正常，1-删除
         */
        private Integer operateStatus;

        /**
         * 父文件夹 ID
         */
        private String parentFolderId;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 更新时间
         */
        private String updateTime;

        /**
         * 删除时间
         */
        private String deleteTime;

        /**
         * 文件夹属性
         */
        private String folderAttr;
    }
}