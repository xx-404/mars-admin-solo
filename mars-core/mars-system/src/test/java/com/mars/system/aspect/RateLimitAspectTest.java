package com.mars.system.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mars.common.exception.BusinessException;
import com.mars.common.result.Result;
import com.mars.system.annotation.RateLimit;
import com.mars.system.annotation.RateLimitStrategy;
import com.mars.system.controller.RateLimitTestController;
import com.mars.system.RateLimitTestApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RateLimitTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RateLimit 限流注解单元测试")
class RateLimitAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_IP = "192.168.1.100";
    private static final String TEST_USER_ID = "1001";
    private static final String TEST_USER_ID_2 = "1002";

    @BeforeEach
    void setUp() {
        redisTemplate.keys("rate_limit:*").forEach(redisTemplate::delete);
        try {
            StpUtil.logout();
        } catch (Exception e) {
        }
    }

    @Test
    @DisplayName("测试RateLimit注解定义 - 默认值")
    void testRateLimitAnnotation_DefaultValues() throws NoSuchMethodException {
        Method method = RateLimitTestController.class.getMethod("testDefaultRateLimit");
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        assertNotNull(rateLimit, "RateLimit注解应该存在");
        assertEquals(60, rateLimit.time(), "默认时间窗口应该是60秒");
        assertEquals(4, rateLimit.count(), "默认最大请求次数应该是4次");
        assertEquals(RateLimitStrategy.IP, rateLimit.strategy(), "默认策略应该是IP限流");
        assertEquals("请求太频繁，请稍后再试", rateLimit.message(), "默认提示消息应该正确");
    }

    @Test
    @DisplayName("测试RateLimit注解定义 - IP策略")
    void testRateLimitAnnotation_IpStrategy() throws NoSuchMethodException {
        Method method = RateLimitTestController.class.getMethod("testIpRateLimit");
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        assertNotNull(rateLimit, "RateLimit注解应该存在");
        assertEquals(60, rateLimit.time(), "时间窗口应该是60秒");
        assertEquals(3, rateLimit.count(), "最大请求次数应该是3次");
        assertEquals(RateLimitStrategy.IP, rateLimit.strategy(), "策略应该是IP限流");
        assertEquals("IP限流：请求太频繁", rateLimit.message(), "提示消息应该正确");
    }

    @Test
    @DisplayName("测试RateLimit注解定义 - 用户ID策略")
    void testRateLimitAnnotation_UserIdStrategy() throws NoSuchMethodException {
        Method method = RateLimitTestController.class.getMethod("testUserIdRateLimit");
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        assertNotNull(rateLimit, "RateLimit注解应该存在");
        assertEquals(60, rateLimit.time(), "时间窗口应该是60秒");
        assertEquals(5, rateLimit.count(), "最大请求次数应该是5次");
        assertEquals(RateLimitStrategy.USER_ID, rateLimit.strategy(), "策略应该是用户ID限流");
        assertEquals("用户ID限流：请求太频繁", rateLimit.message(), "提示消息应该正确");
    }

    @Test
    @DisplayName("测试RateLimit注解定义 - 组合策略")
    void testRateLimitAnnotation_ComboStrategy() throws NoSuchMethodException {
        Method method = RateLimitTestController.class.getMethod("testComboRateLimit");
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        assertNotNull(rateLimit, "RateLimit注解应该存在");
        assertEquals(60, rateLimit.time(), "时间窗口应该是60秒");
        assertEquals(2, rateLimit.count(), "最大请求次数应该是2次");
        assertEquals(RateLimitStrategy.IP_AND_USER_ID, rateLimit.strategy(), "策略应该是组合限流");
        assertEquals("组合限流：请求太频繁", rateLimit.message(), "提示消息应该正确");
    }

    @Test
    @DisplayName("测试RateLimitStrategy枚举")
    void testRateLimitStrategyEnum() {
        assertEquals("IP", RateLimitStrategy.IP.getDescription(), "IP策略描述应该正确");
        assertEquals("用户ID", RateLimitStrategy.USER_ID.getDescription(), "用户ID策略描述应该正确");
        assertEquals("IP和用户ID组合", RateLimitStrategy.IP_AND_USER_ID.getDescription(), "组合策略描述应该正确");
        
        assertEquals(3, RateLimitStrategy.values().length, "应该有3种限流策略");
    }

    @Test
    @DisplayName("测试IP限流策略 - 正常请求")
    void testIpRateLimit_NormalRequest() throws Exception {
        for (int i = 0; i < 3; i++) {
            MvcResult result = mockMvc.perform(get("/test/rate-limit/ip")
                    .header("X-Forwarded-For", TEST_IP)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
            
            String content = result.getResponse().getContentAsString();
            Result<?> response = objectMapper.readValue(content, Result.class);
            assertEquals(200, response.getCode(), "第" + (i + 1) + "次请求应该成功");
        }
    }

    @Test
    @DisplayName("测试IP限流策略 - 触发限流")
    void testIpRateLimit_TriggerLimit() throws Exception {
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/test/rate-limit/ip")
                    .header("X-Forwarded-For", TEST_IP)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        
        MvcResult result = mockMvc.perform(get("/test/rate-limit/ip")
                .header("X-Forwarded-For", TEST_IP)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("IP限流：请求太频繁"), "应该返回限流提示消息");
    }

    @Test
    @DisplayName("测试IP限流策略 - 不同IP独立计数")
    void testIpRateLimit_DifferentIpIndependent() throws Exception {
        String ip1 = "192.168.1.101";
        String ip2 = "192.168.1.102";
        
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/test/rate-limit/ip")
                    .header("X-Forwarded-For", ip1)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        
        mockMvc.perform(get("/test/rate-limit/ip")
                .header("X-Forwarded-For", ip2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/test/rate-limit/ip")
                .header("X-Forwarded-For", ip1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("测试用户ID限流策略 - 已登录用户")
    void testUserIdRateLimit_LoggedInUser() throws Exception {
        StpUtil.login(TEST_USER_ID);
        
        for (int i = 0; i < 5; i++) {
            MvcResult result = mockMvc.perform(get("/test/rate-limit/user")
                    .header("X-Forwarded-For", TEST_IP)
                    .cookie(new jakarta.servlet.http.Cookie("satoken", StpUtil.getTokenValue()))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
            
            String content = result.getResponse().getContentAsString();
            Result<?> response = objectMapper.readValue(content, Result.class);
            assertEquals(200, response.getCode(), "第" + (i + 1) + "次请求应该成功");
        }
        
        MvcResult result = mockMvc.perform(get("/test/rate-limit/user")
                .header("X-Forwarded-For", TEST_IP)
                .cookie(new jakarta.servlet.http.Cookie("satoken", StpUtil.getTokenValue()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("用户ID限流：请求太频繁"), "应该返回限流提示消息");
    }

    @Test
    @DisplayName("测试用户ID限流策略 - 未登录用户降级为IP限流")
    void testUserIdRateLimit_NotLoggedInFallback() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/test/rate-limit/user")
                    .header("X-Forwarded-For", TEST_IP)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        
        MvcResult result = mockMvc.perform(get("/test/rate-limit/user")
                .header("X-Forwarded-For", TEST_IP)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("用户ID限流：请求太频繁"), "应该返回限流提示消息");
    }

    @Test
    @DisplayName("测试用户ID限流策略 - 不同用户独立计数")
    void testUserIdRateLimit_DifferentUserIndependent() throws Exception {
        StpUtil.login(TEST_USER_ID);
        String token1 = StpUtil.getTokenValue();
        
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/test/rate-limit/user")
                    .header("X-Forwarded-For", TEST_IP)
                    .cookie(new jakarta.servlet.http.Cookie("satoken", token1))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        
        StpUtil.logout();
        StpUtil.login(TEST_USER_ID_2);
        String token2 = StpUtil.getTokenValue();
        
        mockMvc.perform(get("/test/rate-limit/user")
                .header("X-Forwarded-For", TEST_IP)
                .cookie(new jakarta.servlet.http.Cookie("satoken", token2))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/test/rate-limit/user")
                .header("X-Forwarded-For", TEST_IP)
                .cookie(new jakarta.servlet.http.Cookie("satoken", token1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("测试组合限流策略 - 已登录用户")
    void testComboRateLimit_LoggedInUser() throws Exception {
        StpUtil.login(TEST_USER_ID);
        
        for (int i = 0; i < 2; i++) {
            MvcResult result = mockMvc.perform(get("/test/rate-limit/combo")
                    .header("X-Forwarded-For", TEST_IP)
                    .cookie(new jakarta.servlet.http.Cookie("satoken", StpUtil.getTokenValue()))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn();
            
            String content = result.getResponse().getContentAsString();
            Result<?> response = objectMapper.readValue(content, Result.class);
            assertEquals(200, response.getCode(), "第" + (i + 1) + "次请求应该成功");
        }
        
        MvcResult result = mockMvc.perform(get("/test/rate-limit/combo")
                .header("X-Forwarded-For", TEST_IP)
                .cookie(new jakarta.servlet.http.Cookie("satoken", StpUtil.getTokenValue()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError())
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("组合限流：请求太频繁"), "应该返回限流提示消息");
    }

    @Test
    @DisplayName("测试组合限流策略 - 同一IP不同用户独立计数")
    void testComboRateLimit_SameIpDifferentUser() throws Exception {
        StpUtil.login(TEST_USER_ID);
        String token1 = StpUtil.getTokenValue();
        
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(get("/test/rate-limit/combo")
                    .header("X-Forwarded-For", TEST_IP)
                    .cookie(new jakarta.servlet.http.Cookie("satoken", token1))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        
        StpUtil.logout();
        StpUtil.login(TEST_USER_ID_2);
        String token2 = StpUtil.getTokenValue();
        
        mockMvc.perform(get("/test/rate-limit/combo")
                .header("X-Forwarded-For", TEST_IP)
                .cookie(new jakarta.servlet.http.Cookie("satoken", token2))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试组合限流策略 - 未登录用户降级为IP限流")
    void testComboRateLimit_NotLoggedInFallback() throws Exception {
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(get("/test/rate-limit/combo")
                    .header("X-Forwarded-For", TEST_IP)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        
        mockMvc.perform(get("/test/rate-limit/combo")
                .header("X-Forwarded-For", TEST_IP)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("测试Redis键过期机制")
    void testRedisKeyExpiration() throws Exception {
        mockMvc.perform(get("/test/rate-limit/ip")
                .header("X-Forwarded-For", TEST_IP)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        String cacheKey = "rate_limit:ip:" + TEST_IP + ":/test/rate-limit/ip";
        String count = redisTemplate.opsForValue().get(cacheKey);
        assertEquals("1", count, "Redis计数应该是1");
        
        Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
        assertNotNull(ttl, "TTL应该存在");
        assertTrue(ttl > 0 && ttl <= 60, "TTL应该在0-60秒之间");
    }

    @Test
    @DisplayName("测试默认限流策略")
    void testDefaultRateLimit() throws Exception {
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(get("/test/rate-limit/default")
                    .header("X-Forwarded-For", TEST_IP)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
        
        mockMvc.perform(get("/test/rate-limit/default")
                .header("X-Forwarded-For", TEST_IP)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("测试BusinessException异常类型")
    void testBusinessExceptionType() {
        BusinessException exception = new BusinessException("测试限流异常");
        assertNotNull(exception, "BusinessException应该被创建");
        assertEquals("测试限流异常", exception.getMessage(), "异常消息应该正确");
    }
}
