package com.mars.system.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.mars.common.exception.BusinessException;
import com.mars.system.annotation.RateLimit;
import com.mars.system.annotation.RateLimitStrategy;
import com.mars.system.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 接口限流切面
 * 基于 Redis 实现，支持多种限流策略：按照IP限流、按照用户ID限流、按照IP和用户ID组合限流
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    private static final String CACHE_KEY_PREFIX = "rate_limit:";

    @Before("@annotation(rateLimit)")
    public void before(JoinPoint joinPoint, RateLimit rateLimit) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return;
        }

        String ip = IpUtils.getIpAddr(request);
        String uri = request.getRequestURI();
        String userId = getUserId();
        RateLimitStrategy strategy = rateLimit.strategy();

        String cacheKey = buildCacheKey(ip, uri, userId, strategy);

        Long currentCount = redisTemplate.opsForValue().increment(cacheKey);

        if (currentCount == null || currentCount == 1) {
            redisTemplate.expire(cacheKey, rateLimit.time(), TimeUnit.SECONDS);
        }

        if (currentCount != null && currentCount > rateLimit.count()) {
            log.warn("接口限流拦截: 策略={}, IP={}, 用户ID={}, URI={}, 限制={}秒{}次, 当前={}次", 
                    strategy.getDescription(), ip, userId, uri, rateLimit.time(), rateLimit.count(), currentCount);
            throw new BusinessException(rateLimit.message());
        }

        log.debug("接口限流统计: 策略={}, IP={}, 用户ID={}, URI={}, 当前次数={}/{}", 
                strategy.getDescription(), ip, userId, uri, currentCount, rateLimit.count());
    }

    /**
     * 构建缓存 key
     * 根据不同的限流策略构建不同的 key
     * - IP策略: rate_limit:ip:{ip}:{uri}
     * - 用户ID策略: rate_limit:user:{userId}:{uri}
     * - 组合策略: rate_limit:combo:{ip}:{userId}:{uri}
     */
    private String buildCacheKey(String ip, String uri, String userId, RateLimitStrategy strategy) {
        StringBuilder keyBuilder = new StringBuilder(CACHE_KEY_PREFIX);

        switch (strategy) {
            case IP:
                keyBuilder.append("ip:").append(ip).append(":").append(uri);
                break;
            case USER_ID:
                if (userId == null) {
                    keyBuilder.append("ip:").append(ip).append(":").append(uri);
                    log.warn("用户未登录，用户ID限流策略降级为IP限流策略");
                } else {
                    keyBuilder.append("user:").append(userId).append(":").append(uri);
                }
                break;
            case IP_AND_USER_ID:
                if (userId == null) {
                    keyBuilder.append("ip:").append(ip).append(":").append(uri);
                    log.warn("用户未登录，IP和用户ID组合限流策略降级为IP限流策略");
                } else {
                    keyBuilder.append("combo:").append(ip).append(":").append(userId).append(":").append(uri);
                }
                break;
            default:
                keyBuilder.append("ip:").append(ip).append(":").append(uri);
        }

        return keyBuilder.toString();
    }

    /**
     * 获取当前登录用户ID
     * 如果用户未登录，返回 null
     */
    private String getUserId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsString();
            }
        } catch (Exception e) {
            log.debug("获取用户ID失败，用户可能未登录: {}", e.getMessage());
        }
        return null;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
