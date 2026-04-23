package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抖音上传失败项
 *
 * @author Mars
 * @date 2026-04-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinUploadFailedItem {

    /**
     * 错误码
     */
    private Integer errCode;

    /**
     * 错误信息
     */
    private String errMsg;
}