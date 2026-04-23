# 大模型编码能力测评题目 - 第一套

## 题目类型：基础CRUD模块开发

## 题目名称：会议管理模块

---

## 背景说明

本项目是一个基于 Spring Boot 3.2.2 + Java 17 的多模块管理系统，使用 MyBatis Plus 3.5.5 作为 ORM 框架，Sa-Token 进行权限控制。

请基于现有项目架构，开发一个完整的会议管理模块（Meeting Management），包含会议的创建、查询、更新、删除功能。

---

## 项目结构参考

```
mars-core/mars-meeting/           # 新建核心模块
  ├── entity/
  │   └── SysMeeting.java         # 会议实体
  ├── mapper/
  │   └── SysMeetingMapper.java   # Mapper接口
  │   └── SysMeetingMapper.xml    # Mapper XML
  ├── service/
  │   ├── SysMeetingService.java  # 服务接口
  │   └── impl/
  │       └── SysMeetingServiceImpl.java  # 服务实现

mars-api/mars-admin-api/
  ├── controller/
  │   └── meeting/
  │       └── SysMeetingController.java   # 控制器
```

---

## 数据库表设计

创建会议表 `sys_meeting`，字段如下：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| title | VARCHAR(100) | 会议标题，必填 |
| content | TEXT | 会议内容/议程 |
| meeting_type | INT | 会议类型（1普通会议 2重要会议 3紧急会议） |
| location | VARCHAR(200) | 会议地点 |
| start_time | DATETIME | 开始时间，必填 |
| end_time | DATETIME | 结束时间，必填 |
| organizer_id | BIGINT | 组织人ID |
| organizer_name | VARCHAR(50) | 组织人姓名 |
| participants | TEXT | 参会人员JSON（用户ID数组） |
| status | INT | 状态（0待开始 1进行中 2已结束 3已取消） |
| remark | VARCHAR(500) | 备注 |
| create_by | BIGINT | 创建人ID |
| create_name | VARCHAR(50) | 创建人姓名 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记（0未删除 1已删除） |

---

## 功能要求

### 1. 基础CRUD接口

| 接口 | 方法 | 路径 | 权限标识 | 说明 |
|------|------|------|----------|------|
| 分页查询 | GET | /sys/meeting/page | sys:meeting:list | 支持按标题、状态、时间范围筛选 |
| 详情查询 | GET | /sys/meeting/{id} | sys:meeting:list | 返回会议详情 |
| 新增会议 | POST | /sys/meeting | sys:meeting:add | 创建会议，自动填充创建人信息 |
| 更新会议 | PUT | /sys/meeting | sys:meeting:edit | 更新会议信息 |
| 删除会议 | DELETE | /sys/meeting/{id} | sys:meeting:delete | 逻辑删除 |
| 批量删除 | DELETE | /sys/meeting/batch | sys:meeting:delete | 批量逻辑删除 |

### 2. 业务规则

- 开始时间必须小于结束时间
- 同一组织人在同一时间段内不能创建重复会议（时间冲突检测）
- 创建会议时，status 默认为 0（待开始）
- 只有待开始状态（status=0）的会议才能修改和删除
- 更新时自动填充 update_time

### 3. 分页查询筛选条件

支持以下筛选条件：
- title：标题模糊查询
- status：状态精确查询
- meetingType：会议类型精确查询
- organizerId：组织人ID精确查询
- startTimeBegin/startTimeEnd：开始时间范围查询

---

## 参考现有代码风格

请参考以下现有代码的编码风格：

1. **实体类风格**：参考 `mars-core/mars-message/src/main/java/com/mars/message/entity/SysNotice.java`
2. **服务接口风格**：参考 `mars-core/mars-system/src/main/java/com/mars/system/service/SysUserService.java`
3. **服务实现风格**：参考 `mars-core/mars-system/src/main/java/com/mars/system/service/impl/SysUserServiceImpl.java`
4. **控制器风格**：参考 `mars-api/mars-admin-api/src/main/java/com/mars/admin/controller/system/SysUserController.java`
5. **统一返回结果**：使用 `com.mars.common.result.Result` 和 `com.mars.common.result.PageResult`

---

## 输出要求

请输出以下内容：

1. **SQL脚本**：创建 `sys_meeting` 表的DDL语句，保存到 `mars-core/mars-meeting/src/main/resources/db/meeting.sql`

2. **实体类**：`SysMeeting.java`

3. **Mapper接口**：`SysMeetingMapper.java`

4. **Mapper XML**：`SysMeetingMapper.xml`（包含分页查询方法）

5. **Service接口**：`SysMeetingService.java`

6. **Service实现**：`SysMeetingServiceImpl.java`

7. **Controller**：`SysMeetingController.java`

8. **模块配置**：
   - 创建 `mars-core/mars-meeting/pom.xml`
   - 更新 `mars-core/pom.xml` 添加 meeting 模块
   - 更新根 `pom.xml` 添加 meeting 模块依赖管理

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| 代码结构规范 | 20% | 模块划分清晰，命名规范，符合项目风格 |
| 实体设计完整 | 15% | 字段定义正确，注解使用规范 |
| 业务逻辑正确 | 25% | 时间冲突检测、状态控制、自动填充等 |
| CRUD功能完整 | 20% | 所有接口功能完整，参数校验到位 |
| 权限控制 | 10% | @SaCheckPermission 注解正确使用 |
| 日志记录 | 10% | @Log 注解正确使用 |

---

## 开始编码

请开始实现上述功能，按照项目现有风格编写代码。