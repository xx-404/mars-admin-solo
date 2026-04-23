package com.mars.biz.dto.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 外部接口通用响应
 *
 * @author Mars
 * @date 2026-04-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalRsp<T> {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 成功响应
     */
    public static <T> ExternalRsp<T> ok(T data) {
        return ExternalRsp.<T>builder()
                .success(true)
                .data(data)
                .code(0)
                .build();
    }

    /**
     * 成功响应（带消息）
     */
    public static <T> ExternalRsp<T> ok(T data, String message) {
        return ExternalRsp.<T>builder()
                .success(true)
                .data(data)
                .code(0)
                .message(message)
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> ExternalRsp<T> fail(String error) {
        return ExternalRsp.<T>builder()
                .success(false)
                .error(error)
                .code(-1)
                .build();
    }

    /**
     * 失败响应（带响应码）
     */
    public static <T> ExternalRsp<T> fail(Integer code, String error) {
        return ExternalRsp.<T>builder()
                .success(false)
                .error(error)
                .code(code)
                .build();
    }

    /**
     * 失败响应（带响应码和消息）
     */
    public static <T> ExternalRsp<T> fail(Integer code, String error, String message) {
        return ExternalRsp.<T>builder()
                .success(false)
                .error(error)
                .code(code)
                .message(message)
                .build();
    }
}