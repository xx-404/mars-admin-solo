# 大模型编码能力测评题目 - 第九套

## 题目类型：接口安全与限流

## 题目名称：API接口限流模块

---

## 背景说明

系统需要对敏感接口进行访问频率限制，防止恶意请求和系统过载。请开发API限流模块，支持多种限流策略，包括IP限流、用户限流、接口限流。

---

## 数据库表设计

### 表1：限流规则表 `sys_rate_limit`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| rule_name | VARCHAR(100) | 规则名称 |
| rule_type | INT | 规则类型（1IP限流 2用户限流 3接口限流 4组合限流） |
| target | VARCHAR(200) | 目标（IP/用户ID/接口路径，逗号分隔） |
| limit_count | INT | 限制次数 |
| limit_period | INT | 限制周期（秒） |
| limit_action | INT | 超限处理（1拒绝 2降级 3排队） |
| whitelist | VARCHAR(500) | 白名单（逗号分隔） |
| status | INT | 状态（0禁用 1启用） |
| remark | VARCHAR(500) | 备注 |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

### 表2：限流日志表 `sys_rate_limit_log`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| rule_id | BIGINT | 规则ID |
| request_ip | VARCHAR(50) | 请求IP |
| user_id | BIGINT | 用户ID |
| request_path | VARCHAR(200) | 请求路径 |
| request_time | DATETIME | 请求时间 |
| is_limited | INT | 是否被限流（0否 1是） |
| current_count | INT | 当前计数 |
| limit_count | INT | 限制次数 |

### 表3：IP黑白名单表 `sys_ip_filter`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| ip_address | VARCHAR(50) | IP地址（支持CIDR格式） |
| filter_type | INT | 类型（1白名单 2黑名单） |
| expire_time | DATETIME | 过期时间（永久为NULL） |
| reason | VARCHAR(200) | 原因 |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |

---

## 功能要求

### 1. 限流规则管理

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 规则分页查询 | GET | /sys/rate-limit/page | sys:rate-limit:list | 查询限流规则列表 |
| 规则详情 | GET | /sys/rate-limit/{id} | sys:rate-limit:list | 获取规则详情 |
| 新增规则 | POST | /sys/rate-limit | sys:rate-limit:add | 新增限流规则 |
| 更新规则 | PUT | /sys/rate-limit | sys:rate-limit:edit | 更新限流规则 |
| 删除规则 | DELETE | /sys/rate-limit/{id} | sys:rate-limit:delete | 删除限流规则 |
| 启用/禁用 | POST | /sys/rate-limit/{id}/toggle | sys:rate-limit:edit | 切换规则状态 |

### 2. IP黑白名单管理

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 黑名单列表 | GET | /sys/ip-filter/blacklist | sys:ip-filter:list | 查询IP黑名单 |
| 白名单列表 | GET | /sys/ip-filter/whitelist | sys:ip-filter:list | 查询IP白名单 |
| 添加黑名单 | POST | /sys/ip-filter/blacklist | sys:ip-filter:add | 添加IP到黑名单 |
| 添加白名单 | POST | /sys/ip-filter/whitelist | sys:ip-filter:add | 添加IP到白名单 |
| 移除 | DELETE | /sys/ip-filter/{id} | sys:ip-filter:delete | 移除IP限制 |

### 3. 限流日志查询

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 日志分页查询 | GET | /sys/rate-limit/log/page | sys:rate-limit:list | 查询限流日志 |
| 统计分析 | GET | /sys/rate-limit/log/stats | sys:rate-limit:list | 限流统计（按IP/接口） |
| 清除日志 | DELETE | /sys/rate-limit/log/clear | sys:rate-limit:edit | 清除历史日志 |

---

## 限流实现方式

### 1. Redis + Lua脚本实现

使用Redis原子计数器实现限流：

```lua
-- rate_limit.lua
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local period = tonumber(ARGV[2])
local current = tonumber(redis.call('GET', key) or "0")

if current >= limit then
    return 0  -- 已超限
end

-- 计数+1并设置过期时间
redis.call('INCR', key)
if current == 0 then
    redis.call('EXPIRE', key, period)
end
return 1  -- 未超限
```

### 2. 限流注解

创建注解标记需要限流的接口：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 限流类型：IP/USER/API
     */
    LimitType type() default LimitType.IP;

    /**
     * 限制次数
     */
    int count() default 100;

    /**
     * 限制周期（秒）
     */
    int period() default 60;

    /**
     * 超限提示消息
     */
    String message() default "访问频率过高，请稍后再试";
}
```

### 3. 限流拦截器

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                             HttpServletResponse response, 
                             Object handler) throws Exception {
        // 1. 检查IP黑白名单
        // 2. 检查方法是否有@RateLimit注解
        // 3. 检查数据库配置的限流规则
        // 4. 执行限流检查（Redis Lua）
        // 5. 记录限流日志
        // 6. 超限则返回429错误
    }
}
```

---

## 限流类型说明

| 类型 | 说明 | Redis Key格式 |
|------|------|---------------|
| IP限流 | 同一IP在周期内请求次数限制 | `rate:ip:{ip}:{path}` |
| 用户限流 | 同一用户在周期内请求次数限制 | `rate:user:{userId}:{path}` |
| 接口限流 | 某接口在周期内总请求次数限制 | `rate:api:{path}` |
| 组合限流 | IP+用户组合限流 | `rate:combo:{ip}:{userId}:{path}` |

---

## 超限处理方式

| 方式 | 说明 |
|------|------|
| 拒绝 | 直接返回HTTP 429错误 |
| 降级 | 返回降级响应（如空数据或缓存数据） |
| 排队 | 请求进入队列，延迟处理 |

---

## IP黑白名单实现

### 1. 白名单检查

- 白名单IP不受任何限流规则限制
- 支持CIDR格式（如 192.168.1.0/24）

### 2. 黑名单检查

- 黑名单IP直接拒绝访问
- 支持永久黑名单和临时黑名单（有过期时间）

### 3. CIDR匹配算法

```java
public class IpUtils {
    /**
     * 检查IP是否在CIDR范围内
     */
    public static boolean isInCidr(String ip, String cidr) {
        // 解析CIDR，计算IP范围
        // 判断ip是否在范围内
    }
}
```

---

## 项目结构参考

```
mars-core/mars-security/
  ├── entity/
  │   ├── SysRateLimit.java
  │   ├── SysRateLimitLog.java
  │   └── SysIpFilter.java
  ├── mapper/
  │   ├── SysRateLimitMapper.java
  │   ├── SysRateLimitLogMapper.java
  │   ├── SysIpFilterMapper.java
  │   └── 对应XML文件
  ├── annotation/
  │   └── RateLimit.java
  ├── interceptor/
  │   ├── RateLimitInterceptor.java
  │   └── IpFilterInterceptor.java
  ├── service/
  │   ├── RateLimitService.java
  │   ├── IpFilterService.java
  │   └── impl/
  │       ├── RateLimitServiceImpl.java
  │       ├── IpFilterServiceImpl.java
  ├── util/
  │   ├── IpUtils.java
  │   └── RateLimitLua.java

mars-api/mars-admin-api/
  ├── controller/
  │   └── security/
  │       ├── RateLimitController.java
  │       ├── IpFilterController.java
```

---

## 业务规则

### 1. 限流规则

- 规则启用后才生效
- 白名单IP不受限流
- 管理员（admin）用户不受用户限流
- 同一接口可以有多条规则叠加

### 2. 规则优先级

- 黑名单 > 白名单 > 限流规则
- 具体IP > CIDR范围
- 用户限流 > IP限流 > 接口限流

### 3. 日志记录

- 每次请求都记录日志（可选）
- 只记录超限请求（必选）
- 日志保留30天

---

## 输出要求

1. SQL脚本：三张表DDL
2. 实体类：三个Entity
3. 注解类：`RateLimit.java`
4. Redis Lua脚本：`rate_limit.lua`
5. 拦截器：`RateLimitInterceptor.java`、`IpFilterInterceptor.java`
6. IP工具类：`IpUtils.java`
7. Service层：限流服务、IP过滤服务
8. Controller层：两个控制器

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| Redis限流实现 | 25% | Lua脚本原子操作正确 |
| 拦截器实现 | 20% | 正确拦截并处理请求 |
| 注解+配置结合 | 15% | 注解和数据库规则都生效 |
| IP黑白名单 | 15% | CIDR匹配正确 |
| 超限处理 | 10% | 多种处理方式实现 |
| 规则叠加 | 10% | 多规则优先级处理正确 |
| 日志记录 | 5% | 日志完整记录 |

---

## 开始编码

请实现API接口限流模块，重点关注Redis原子计数器的实现和拦截器的设计。