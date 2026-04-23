package com.mars.biz.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 抖音批量上传图片请求
 *
 * @author Mars
 * @date 2026-04-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinBatchUploadImageReq {

    /**
     * 店铺所有者 ID
     */
    @NotBlank(message = "店铺所有者ID不能为空")
    private String ownerId;

    /**
     * 图片材料列表
     */
    @NotEmpty(message = "图片材料列表不能为空")
    private List<DouyinMaterialItem> materials;

    /**
     * 是否需要去重：true-去重，false-不去重
     */
    private Boolean needDistinct=true;
}
