package com.mars.biz.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抖音运费模板同步请求
 *
 * @author Mars
 * @date 2026-04-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DouyinFreightTemplateSyncReq {

    /**
     * 店铺所有者ID
     */
    @NotBlank(message = "店铺所有者ID不能为空")
    private String ownerId;
}