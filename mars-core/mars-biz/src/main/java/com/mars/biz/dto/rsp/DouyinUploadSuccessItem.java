package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抖音上传成功项
 *
 * @author Mars
 * @date 2026-04-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinUploadSuccessItem {

    /**
     * 材料ID
     */
    private String materialId;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 文件夹 ID
     */
    private String folderId;

    /**
     * 原始 URL
     */
    private String originUrl;

    /**
     * 字节 URL
     */
    private String byteUrl;

    /**
     * 审核状态
     */
    private Integer auditStatus;

    /**
     * 是否新建
     */
    private Boolean isNew;
}