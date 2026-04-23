# 大模型编码能力测评题目 - 第二套

## 题目类型：复杂业务逻辑开发

## 题目名称：请假审批流程模块

---

## 背景说明

本项目是一个基于 Spring Boot 3.2.2 + Java 17 的多模块管理系统，使用 MyBatis Plus 3.5.5，Sa-Token 进行权限控制。

请开发一个完整的请假审批流程模块，包含请假申请、审批流转、状态管理、多表关联等功能。

---

## 业务场景描述

员工可以提交请假申请，申请经过审批流程流转：

1. 员工提交请假申请（状态：待审批）
2. 部门主管审批（同意/拒绝）
3. 如果请假天数 > 3天，需要HR二次审批
4. 审批完成后，状态变为已通过/已拒绝
5. 员工可以撤销待审批的申请
6. 审批人可以查看待审批列表

---

## 数据库表设计

### 表1：请假申请表 `sys_leave`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| applicant_id | BIGINT |申请人ID |
| applicant_name | VARCHAR(50) |申请人姓名 |
| dept_id | BIGINT |申请人部门ID |
| dept_name | VARCHAR(100) |申请人部门名称 |
| leave_type | INT |请假类型（1年假 2病假 3事假 4调休 5其他） |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| days | DECIMAL(5,1) | 请假天数（计算得出） |
| reason | TEXT |请假原因 |
| attachment | VARCHAR(500) |附件文件ID（多个逗号分隔） |
| status | INT |状态（0待审批 1部门审批中 2HR审批中 3已通过 4已拒绝 5已撤销） |
| current_approver_id | BIGINT | 当前审批人ID |
| current_approver_name | VARCHAR(50) | 当前审批人姓名 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

### 表2：审批记录表 `sys_leave_approval`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| leave_id | BIGINT | 请假申请ID |
| approver_id | BIGINT | 审批人ID |
| approver_name | VARCHAR(50) |审批人姓名 |
| approval_type | INT |审批类型（1部门主管 2HR） |
| result | INT |审批结果（1同意 2拒绝） |
| opinion | TEXT |审批意见 |
| approval_time | DATETIME |审批时间 |

---

## 功能要求

### 1. 请假申请接口

| 接口 | 方法 | 路径 | 权限标识 | 说明 |
|------|------|------|----------|------|
| 我的请假列表 | GET | /sys/leave/my | 无 | 查询当前用户的请假记录 |
| 提交申请 | POST | /sys/leave | sys:leave:add | 提交请假申请 |
| 撤销申请 | POST | /sys/leave/{id}/cancel | sys:leave:add | 撤销待审批的申请 |
| 详情查询 | GET | /sys/leave/{id} | sys:leave:list | 查询请假详情含审批记录 |

### 2. 审批接口

| 接口 | 方法 | 路径 | 权限标识 | 说明 |
|------|------|------|----------|------|
| 待审批列表 | GET | /sys/leave/pending | sys:leave:approve | 查询待当前用户审批的列表 |
| 审批通过 | POST | /sys/leave/{id}/approve | sys:leave:approve | 同意请假申请 |
| 审批拒绝 | POST | /sys/leave/{id}/reject | sys:leave:approve | 拒绝请假申请 |

### 3. 管理接口

| 接口 | 方法 | 路径 | 权限标识 | 说明 |
|------|------|------|----------|------|
| 全部请假列表 | GET | /sys/leave/page | sys:leave:list | 分页查询所有请假记录 |
| 统计查询 | GET | /sys/leave/stats | sys:leave:list | 按状态/类型统计请假数量 |

---

## 业务规则（核心难点）

### 1. 申请规则

- 开始日期必须 <= 结束日期
- 请假天数自动计算（end_date - start_date + 1）
- 只能提交当月及之后的请假申请（不能申请过去的日期）
- 申请提交时自动获取申请人信息（从当前登录用户）
- 申请提交时自动获取申请人部门信息
- 申请提交时自动确定第一审批人（申请人所在部门的主管）

### 2. 审批流转规则（核心逻辑）

```
申请提交 → status=0（待审批），current_approver=部门主管

部门主管审批同意：
  - 如果days <= 3：直接通过，status=3
  - 如果days > 3：流转到HR，status=2，current_approver=HR管理员

部门主管审批拒绝：
  - 直接拒绝，status=4

HR审批同意：
  - 通过，status=3

HR审批拒绝：
  - 拒绝，status=4

申请人撤销：
  - 只能在status=0时撤销，status=5
```

### 3. 状态控制

- 待审批（0）：可撤销、可审批
- 部门审批中（1）：部门主管可审批
- HR审批中（2）：HR可审批
- 已通过（3）：不可操作
- 已拒绝（4）：不可操作
- 已撤销（5）：不可操作

### 4. 审批记录

每次审批操作必须记录到 `sys_leave_approval` 表：
- 记录审批人信息
- 记录审批类型（部门主管=1，HR=2）
- 记录审批结果和意见
- 记录审批时间

---

## 项目结构参考

```
mars-core/mars-leave/
  ├── entity/
  │   ├── SysLeave.java
  │   └── SysLeaveApproval.java
  ├── mapper/
  │   ├── SysLeaveMapper.java
  │   ├── SysLeaveApprovalMapper.java
  │   └── SysLeaveMapper.xml
  │   └── SysLeaveApprovalMapper.xml
  ├── service/
  │   ├── SysLeaveService.java
  │   ├── SysLeaveApprovalService.java
  │   └── impl/
  │       ├── SysLeaveServiceImpl.java
  │       ├── SysLeaveApprovalServiceImpl.java

mars-api/mars-admin-api/
  ├── controller/
  │   └── leave/
  │       └── SysLeaveController.java
```

---

## 参考代码风格

- 参考用户服务的事务处理：`SysUserServiceImpl.java`
- 参考消息通知的状态流转逻辑
- 使用 `@Transactional(rollbackFor = Exception.class)` 确保审批记录和状态更新的一致性

---

## 输出要求

1. **SQL脚本**：创建两张表的DDL，保存到 `mars-core/mars-leave/src/main/resources/db/leave.sql`

2. **实体类**：`SysLeave.java` 和 `SysLeaveApproval.java`

3. **Mapper层**：接口和XML文件

4. **Service层**：接口和实现类

5. **Controller层**：`SysLeaveController.java`

6. **模块配置**：pom.xml更新

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| 多表设计合理 | 15% | 主表和审批记录表关联正确 |
| 状态流转逻辑 | 30% | 审批流程分支判断正确，天数>3流转到HR |
| 事务处理 | 20% | 审批操作与记录写入事务一致 |
| 业务规则校验 | 15% | 时间校验、状态校验、权限校验 |
| 审批人确定 | 10% | 正确获取部门主管和HR |
| 代码风格符合 | 10% | 符合项目规范 |

---

## 开始编码

请开始实现请假审批流程模块，重点关注审批流转的状态管理和事务处理。