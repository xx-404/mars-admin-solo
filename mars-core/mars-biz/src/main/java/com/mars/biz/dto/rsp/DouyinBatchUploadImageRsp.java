package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 抖音批量上传图片响应
 *
 * @author Mars
 * @date 2026-04-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinBatchUploadImageRsp {

    /**
     * 成功上传的图片映射（requestId -> 上传结果）
     */
    private Map<String, DouyinUploadSuccessItem> successMap;

    /**
     * 失败上传的图片映射（requestId -> 失败原因）
     */
    private Map<String, DouyinUploadFailedItem> failedMap;
}