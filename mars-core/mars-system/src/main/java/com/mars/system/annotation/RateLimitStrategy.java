package com.mars.system.annotation;

/**
 * 限流策略枚举
 */
public enum RateLimitStrategy {

    /**
     * 按照请求IP限流
     */
    IP("IP"),

    /**
     * 按照用户ID限流
     */
    USER_ID("用户ID"),

    /**
     * 按照请求IP和用户ID组合限流
     */
    IP_AND_USER_ID("IP和用户ID组合");

    private final String description;

    RateLimitStrategy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
