package com.mars.system.controller;

import com.mars.common.result.Result;
import com.mars.system.annotation.RateLimit;
import com.mars.system.annotation.RateLimitStrategy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/rate-limit")
public class RateLimitTestController {

    @GetMapping("/ip")
    @RateLimit(time = 60, count = 3, strategy = RateLimitStrategy.IP, message = "IP限流：请求太频繁")
    public Result<String> testIpRateLimit() {
        return Result.ok("IP限流测试成功");
    }

    @GetMapping("/user")
    @RateLimit(time = 60, count = 5, strategy = RateLimitStrategy.USER_ID, message = "用户ID限流：请求太频繁")
    public Result<String> testUserIdRateLimit() {
        return Result.ok("用户ID限流测试成功");
    }

    @GetMapping("/combo")
    @RateLimit(time = 60, count = 2, strategy = RateLimitStrategy.IP_AND_USER_ID, message = "组合限流：请求太频繁")
    public Result<String> testComboRateLimit() {
        return Result.ok("组合限流测试成功");
    }

    @GetMapping("/default")
    @RateLimit(time = 60, count = 4)
    public Result<String> testDefaultRateLimit() {
        return Result.ok("默认限流测试成功");
    }
}
