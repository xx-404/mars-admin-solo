package com.mars.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 电商平台类型枚举
 *
 * @author Mars
 * @date 2026-04-16
 */
@Getter
@AllArgsConstructor
public enum PlatformType {

    /**
     * 拼多多
     */
    PINDUODUO("PINDUODUO", "拼多多"),

    /**
     * 抖音
     */
    DOUYIN("DOUYIN", "抖音"),

    /**
     * 小红书
     */
    XIAOHONGSHU("XIAOHONGSHU", "小红书");

    /**
     * 平台代码
     */
    private final String code;

    /**
     * 平台名称
     */
    private final String name;

    /**
     * 根据代码获取平台类型
     */
    public static PlatformType fromCode(String code) {
        for (PlatformType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
