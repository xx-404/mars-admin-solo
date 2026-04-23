# 大模型编码能力测评题目 - 第六套

## 题目类型：系统监控与实时管理

## 题目名称：在线用户监控模块

---

## 背景说明

项目使用 Sa-Token 进行认证授权，Redis 存储会话信息。请开发在线用户监控模块，实现实时查看在线用户、强制下线、登录历史查询等功能。

---

## 功能要求

### 1. 在线用户管理接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 在线用户列表 | GET | /sys/online/list | sys:online:list | 查询当前在线用户 |
| 强制下线 | POST | /sys/online/kickout/{tokenId} | sys:online:kickout | 强制用户下线 |
| 批量下线 | POST | /sys/online/kickout/batch | sys:online:kickout | 批量强制下线 |
| 当前用户信息 | GET | /sys/online/current | 无 | 获取当前登录用户信息 |

### 2. 登录历史接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 登录日志分页 | GET | /sys/login-log/page | sys:login-log:list | 查询登录日志 |
| 用户登录历史 | GET | /sys/login-log/user/{userId} | sys:login-log:list | 查询指定用户的登录历史 |
| 最近登录统计 | GET | /sys/login-log/stats | sys:login-log:list | 按日期统计登录次数 |

---

## 数据库表设计

### 登录日志表 `sys_login_log`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户ID |
| username | VARCHAR(50) | 用户名 |
| login_type | INT | 登录类型（1密码 2短信 3微信 4第三方） |
| client_type | INT | 客户端类型（1后台 2APP 3Web） |
| login_ip | VARCHAR(50) | 登录IP |
| login_location | VARCHAR(100) | 登录地点（IP解析） |
| login_device | VARCHAR(200) | 登录设备信息 |
| login_browser | VARCHAR(100) | 浏览器信息 |
| login_os | VARCHAR(100) | 操作系统 |
| login_time | DATETIME | 登录时间 |
| logout_time | DATETIME | 登出时间 |
| login_status | INT | 登录状态（1成功 2失败） |
| login_message | VARCHAR(200) | 登录消息（成功/失败原因） |

---

## Sa-Token 集成要点

### 1. 获取在线用户

使用 Sa-Token API 获取在线会话：

```java
// 获取所有在线用户的token值
List<String> tokenList = StpUtil.searchTokenValue("", 0, -1, false);

// 遍历token获取用户信息
for (String token : tokenList) {
    // 通过token获取用户ID
    Object userId = StpUtil.getLoginIdByToken(token);
    
    // 获取token详细信息
    SaSession session = StpUtil.getSessionByToken(token);
}
```

### 2. 强制下线

```java
// 强制指定token下线
StpUtil.kickoutByTokenValue(tokenId);

// 强制指定用户下线（踢掉该用户的所有token）
StpUtil.kickout(userId);
```

### 3. Token信息获取

从 Sa-Token session 中获取：
- 用户ID
- 登录时间
- Token过期时间
- 自定义存储的设备信息

---

## 实现要求

### 1. 在线用户信息返回格式

```json
{
  "tokenId": "xxxx-xxxx-xxxx",
  "userId": 1,
  "username": "admin",
  "nickname": "管理员",
  "deptName": "研发部",
  "loginIp": "192.168.1.100",
  "loginLocation": "北京市",
  "loginDevice": "PC",
  "loginBrowser": "Chrome 120",
  "loginOs": "Windows 11",
  "loginTime": "2024-01-15 10:00:00",
  "expireTime": "2024-01-15 12:00:00"
}
```

### 2. 登录日志记录时机

在登录成功时记录：
- 使用 `LoginHelper` 获取登录信息
- 解析IP获取地理位置（可使用第三方API或离线库）
- 记录 User-Agent 解析出设备和浏览器信息

在登出或被踢下线时更新：
- 更新 logout_time

### 3. IP解析

可使用以下方式获取IP地理位置：
- 纯真IP数据库（离线）
- ip-api.com（在线免费API）
- 百度/腾讯地图API

### 4. User-Agent解析

解析 User-Agent 获取：
- 操作系统：Windows/Mac/Linux/iOS/Android
- 浏览器：Chrome/Edge/Safari/Firefox
- 设备类型：PC/Mobile/Tablet

---

## 项目结构参考

```
mars-core/mars-monitor/
  ├── entity/
  │   └── SysLoginLog.java
  ├── mapper/
  │   ├── SysLoginLogMapper.java
  │   └── SysLoginLogMapper.xml
  ├── service/
  │   ├── OnlineUserService.java      # 在线用户服务
  │   ├── SysLoginLogService.java     # 登录日志服务
  │   └── impl/
  │       ├── OnlineUserServiceImpl.java
  │       ├── SysLoginLogServiceImpl.java
  ├── util/
  │   ├── IpLocationUtil.java         # IP解析工具
  │   └── UserAgentUtil.java          # UA解析工具

mars-api/mars-admin-api/
  ├── controller/
  │   └── monitor/
  │       ├── OnlineUserController.java
  │       └── SysLoginLogController.java
```

---

## 业务规则

### 1. 强制下线规则

- 只有管理员（拥有 sys:online:kickout 权限）才能强制下线
- 不能强制自己下线（需要校验 tokenId 是否属于当前用户）
- 批量下线时同样校验

### 2. 登录日志规则

- 只记录后台管理系统的登录（clientType=1）
- 登录失败也需要记录（loginStatus=2）
- logout_time 在登出时更新，如果被踢下线则记录踢下线时间

### 3. 统计规则

- 按日期统计每天的登录次数（成功和失败分开统计）
- 按用户统计最近登录时间

---

## 输出要求

1. SQL脚本：登录日志表DDL
2. 实体类：`SysLoginLog.java`
3. IP解析工具类：`IpLocationUtil.java`
4. UA解析工具类：`UserAgentUtil.java`
5. Service层：在线用户服务、登录日志服务
6. Controller层：在线用户控制器、登录日志控制器

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| Sa-Token集成 | 25% | 正确使用Sa-Token API获取和管理会话 |
| 强制下线实现 | 20% | 踢人逻辑正确，自我保护校验到位 |
| 登录日志记录 | 20% | 登录/登出时正确记录日志 |
| IP/UA解析 | 15% | 工具类实现完整 |
| 统计功能 | 10% | 统计接口实现正确 |
| 安全校验 | 10% | 不能踢自己等安全规则 |

---

## 开始编码

请实现在线用户监控模块，重点关注Sa-Token会话管理和登录日志记录的集成。