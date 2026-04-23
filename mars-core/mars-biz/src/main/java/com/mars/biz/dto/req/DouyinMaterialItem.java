package com.mars.biz.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抖音材料项（图片）
 *
 * @author Mars
 * @date 2026-04-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinMaterialItem {

    /**
     * 请求 ID（用于标识每个上传项）
     */
    private String requestId;

    /**
     * 文件夹 ID
     */
    private String folderId;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 图片 URL（外部链接）
     */
    private String url;

    /**
     * 文件 URI（本地文件）
     */
    private String fileUri;

    /**
     * 材料类型
     */
    private String materialType;
}