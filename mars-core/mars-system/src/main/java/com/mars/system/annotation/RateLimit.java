package com.mars.system.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 支持多种限流策略：按照IP限流、按照用户ID限流、按照IP和用户ID组合限流
 *
 * 使用示例:
 *   @RateLimit(time = 60, count = 10)                                    // 默认按照IP限流，60秒内最多请求10次
 *   @RateLimit(time = 60, count = 10, strategy = RateLimitStrategy.IP)  // 显式指定按照IP限流
 *   @RateLimit(time = 60, count = 5, strategy = RateLimitStrategy.USER_ID)  // 按照用户ID限流，60秒内最多请求5次
 *   @RateLimit(time = 60, count = 10, strategy = RateLimitStrategy.IP_AND_USER_ID)  // 按照IP和用户ID组合限流
 *   @RateLimit(time = 60, count = 10, message = "请求太频繁，请稍后再试")  // 自定义提示消息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 时间窗口（秒），默认60秒
     */
    int time() default 60;

    /**
     * 在时间窗口内允许的最大请求次数，默认10次
     */
    int count() default 10;

    /**
     * 限流策略，默认按照IP限流
     */
    RateLimitStrategy strategy() default RateLimitStrategy.IP;

    /**
     * 限流时的提示消息
     */
    String message() default "请求太频繁，请稍后再试";
}
