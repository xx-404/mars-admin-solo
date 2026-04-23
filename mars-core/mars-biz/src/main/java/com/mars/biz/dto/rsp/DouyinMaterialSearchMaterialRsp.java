package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音素材分页查询响应
 *
 * @author Mars
 * @date 2026-04-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinMaterialSearchMaterialRsp {

    /**
     * 素材信息列表
     */
    private List<MaterialInfoItem> materialInfoList;

    /**
     * 总数量
     */
    private Long total;

    /**
     * 素材信息项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialInfoItem {

        /**
         * 素材 ID
         */
        private String materialId;

        /**
         * 文件夹 ID
         */
        private String folderId;

        /**
         * 原始 URL
         */
        private String originUrl;

        /**
         * 字节 URL（抖音内部链接）
         */
        private String byteUrl;

        /**
         * 素材名称
         */
        private String materialName;

        /**
         * 素材类型：photo-图片，video-视频
         */
        private String materialType;

        /**
         * 操作状态：0-正常，1-删除
         */
        private Integer operateStatus;

        /**
         * 审核状态：0-审核中，1-审核通过，2-审核失败
         */
        private Integer auditStatus;

        /**
         * 审核拒绝描述
         */
        private String auditRejectDesc;

        /**
         * 文件大小（字节）
         */
        private Long size;

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
         * 图片信息（仅图片素材）
         */
        private PhotoInfo photoInfo;

        /**
         * 视频信息（仅视频素材）
         */
        private VideoInfo videoInfo;
    }

    /**
     * 图片信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoInfo {

        /**
         * 图片高度
         */
        private Integer height;

        /**
         * 图片宽度
         */
        private Integer width;

        /**
         * 图片格式
         */
        private String format;
    }

    /**
     * 视频信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoInfo {

        /**
         * 视频格式
         */
        private String format;

        /**
         * 视频时长（秒）
         */
        private Double duration;

        /**
         * 视频 ID
         */
        private String vid;

        /**
         * 视频封面 URL
         */
        private String videoCoverUrl;

        /**
         * 视频高度
         */
        private Integer height;

        /**
         * 视频宽度
         */
        private Integer width;

        /**
         * 视频大小（字节）
         */
        private Long size;
    }
}