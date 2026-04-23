# 大模型编码能力测评题目 - 第八套

## 题目类型：WebSocket实时通信

## 题目名称：实时消息推送模块

---

## 背景说明

项目已有WebSocket基础设施（mars-websocket模块），请在此基础上开发实时消息推送功能，支持单点推送、群组推送、全员广播，并实现消息已读未读状态管理。

---

## 数据库表设计

### 表1：实时消息表 `sys_realtime_message`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| message_type | INT | 消息类型（1系统通知 2待办提醒 3审批提醒 4聊天消息） |
| title | VARCHAR(200) | 消息标题 |
| content | TEXT | 消息内容 |
| sender_id | BIGINT | 发送人ID |
| sender_name | VARCHAR(50) | 发送人姓名 |
| receiver_type | INT | 接收类型（1指定用户 2指定部门 3指定角色 4全员） |
| receiver_ids | VARCHAR(500) | 接收人ID列表（逗号分隔，指定用户时使用） |
| dept_ids | VARCHAR(500) | 部门ID列表（逗号分隔，指定部门时使用） |
| role_ids | VARCHAR(500) | 角色ID列表（逗号分隔，指定角色时使用） |
| biz_type | VARCHAR(50) | 业务类型（用于分类筛选） |
| biz_id | BIGINT | 业务ID（关联业务数据） |
| priority | INT | 优先级（1普通 2重要 3紧急） |
| push_time | DATETIME | 推送时间 |
| expire_time | DATETIME | 过期时间 |
| create_time | DATETIME | 创建时间 |

### 表2：消息接收记录表 `sys_message_receiver`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| message_id | BIGINT | 消息ID |
| user_id | BIGINT | 接收用户ID |
| user_name | VARCHAR(50) | 接收用户姓名 |
| is_read | INT | 是否已读（0未读 1已读） |
| read_time | DATETIME | 阅读时间 |
| is_deleted | INT | 是否删除（0未删 1已删） |
| delete_time | DATETIME | 删除时间 |
| create_time | DATETIME | 创建时间 |

---

## 功能要求

### 1. 消息发送接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 发送给指定用户 | POST | /sys/message/send/user | sys:message:send | 发送消息给指定用户 |
| 发送给部门 | POST | /sys/message/send/dept | sys:message:send | 发送消息给部门所有用户 |
| 发送给角色 | POST | /sys/message/send/role | sys:message:send | 发送消息给拥有该角色的用户 |
| 全员广播 | POST | /sys/message/send/all | sys:message:send:broadcast | 发送全员消息 |

### 2. 消息查询接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 我的消息列表 | GET | /sys/message/my | 无 | 当前用户的消息列表 |
| 未读消息数量 | GET | /sys/message/unread/count | 无 | 获取未读消息数量 |
| 未读消息列表 | GET | /sys/message/unread | 无 | 获取未读消息列表 |
| 消息详情 | GET | /sys/message/{id} | 无 | 查看消息详情（自动标记已读） |
| 消息统计 | GET | /sys/message/stats | sys:message:list | 消息发送统计 |

### 3. 消息操作接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 标记已读 | POST | /sys/message/{id}/read | 无 | 标记消息已读 |
| 批量标记已读 | POST | /sys/message/read/batch | 无 | 批量标记已读 |
| 全部标记已读 | POST | /sys/message/read/all | 无 | 所有未读消息标记已读 |
| 删除消息 | DELETE | /sys/message/{id} | 无 | 删除消息（软删除） |
| 批量删除 | DELETE | /sys/message/batch | 无 | 批量删除消息 |

---

## WebSocket推送实现

### 1. 消息推送格式

```json
{
  "type": "message",
  "data": {
    "messageId": 1,
    "messageType": 1,
    "title": "系统通知",
    "content": "您有新的审批待处理",
    "senderName": "系统",
    "priority": 2,
    "bizType": "approval",
    "bizId": 100,
    "pushTime": "2024-01-15 10:00:00"
  }
}
```

### 2. WebSocket连接管理

- 用户上线时注册 userId 与 WebSocket Session 的映射
- 用户下线时移除映射
- 支持同一用户多终端连接

### 3. 推送策略

```java
// 推送给指定用户
public void pushToUser(Long userId, RealtimeMessage message) {
    // 1. 获取用户的所有WebSocket Session
    // 2. 遍历发送消息
    // 3. 如果用户不在线，记录到数据库，上线后再推送
}

// 推送给部门所有用户
public void pushToDept(Long deptId, RealtimeMessage message) {
    // 1. 查询部门下所有用户ID
    // 2. 遍历推送给每个用户
}

// 推送给角色所有用户
public void pushToRole(Long roleId, RealtimeMessage message) {
    // 1. 查询拥有该角色的所有用户ID
    // 2. 遍历推送给每个用户
}

// 全员广播
public void pushToAll(RealtimeMessage message) {
    // 1. 遍历所有在线用户发送
    // 2. 记录到消息接收表（所有用户）
}
```

---

## 离线消息处理

### 1. 离线消息存储

用户不在线时：
- 消息保存到 `sys_realtime_message` 表
- 接收记录保存到 `sys_message_receiver` 表（is_read=0）

### 2.上线推送离线消息

用户上线时：
- 查询该用户未读消息（is_read=0）
- 通过WebSocket推送离线消息
- 推送数量限制（最多推送最近50条）

### 3.未读消息提醒

定时检查未读消息：
- 每分钟检查是否有新未读消息
- 如果有，通过WebSocket推送未读数量

---

## 项目结构参考

```
mars-core/mars-message/
  ├── entity/
  │   ├── SysRealtimeMessage.java
  │   └── SysMessageReceiver.java
  ├── mapper/
  │   ├── SysRealtimeMessageMapper.java
  │   ├── SysMessageReceiverMapper.java
  │   ├── SysRealtimeMessageMapper.xml
  │   └── SysMessageReceiverMapper.xml
  ├── service/
  │   ├── MessagePushService.java      # 推送服务
  │   ├── RealtimeMessageService.java  # 消息管理服务
  │   └── impl/
  │       ├── MessagePushServiceImpl.java
  │       ├── RealtimeMessageServiceImpl.java

mars-websocket/
  ├── handler/
  │   └── MessageWebSocketHandler.java  # 消息WebSocket处理器
  ├── manager/
  │   ├── WebSocketSessionManager.java  # Session管理器

mars-api/mars-admin-api/
  ├── controller/
  │   └── message/
  │       └── RealtimeMessageController.java
```

---

## 业务规则

### 1. 发送规则

- 消息标题和内容必填
- 接收类型和对应ID列表必须匹配
- 发送时间默认为当前时间
- 发送时同时创建接收记录

### 2. 已读规则

- 查看消息详情时自动标记已读
- 已读时间记录当前时间
- 批量标记已读时更新所有选中消息

### 3. 删除规则

- 删除是软删除（is_deleted=1）
- 已删除的消息不在列表中显示
- 用户只能删除自己的消息

---

## 输出要求

1. SQL脚本：两张表DDL
2. 实体类：两个Entity
3. WebSocket处理器：`MessageWebSocketHandler.java`
4. Session管理器：`WebSocketSessionManager.java`
5. 推送服务：`MessagePushService.java` 及实现
6. 消息管理服务：`RealtimeMessageService.java` 及实现
7. Controller层：消息控制器
8. Mapper层：接口和XML

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| WebSocket推送实现 | 25% | 正确实现单点/群组/广播推送 |
| Session管理 | 15% | 用户上下线正确管理Session |
| 离线消息处理 | 20% | 离线存储和上线推送正确 |
| 已读未读状态 | 15% | 状态更新正确 |
| 消息分发逻辑 | 15% | 部门/角色用户正确查询 |
| 接口设计 | 10% | 接口完整，权限控制合理 |

---

## 开始编码

请实现实时消息推送模块，重点关注WebSocket推送和离线消息的处理逻辑。