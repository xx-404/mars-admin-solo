# 大模型编码能力测评题目 - 第四套

## 题目类型：文件附件管理

## 题目名称：文档资料库模块

---

## 背景说明

本项目已有文件管理基础模块（mars-file），请在此基础上开发一个文档资料库模块，实现文档分类管理、版本管理、在线预览、权限控制等功能。

---

## 数据库表设计

### 表1：文档分类表 `sys_doc_category`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| parent_id | BIGINT | 父分类ID（支持多级分类） |
| category_name | VARCHAR(50) | 分类名称 |
| category_code | VARCHAR(50) | 分类编码，唯一 |
| sort | INT | 排序号 |
| icon | VARCHAR(100) | 分类图标 |
| status | INT | 状态（0禁用 1启用） |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

### 表2：文档表 `sys_document`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| category_id | BIGINT | 分类ID |
| doc_title | VARCHAR(200) | 文档标题 |
| doc_code | VARCHAR(50) | 文档编码 |
| file_id | BIGINT | 关联文件ID（sys_file表） |
| file_name | VARCHAR(200) | 文件名称 |
| file_size | BIGINT | 文件大小（字节） |
| file_type | VARCHAR(50) | 文件类型（pdf/doc/docx/xls/xlsx/ppt等） |
| version | INT | 版本号 |
| description | TEXT | 文档描述 |
| keywords | VARCHAR(500) | 关键词（逗号分隔） |
| download_count | INT | 下载次数 |
| view_count | INT | 查看次数 |
| is_public | INT | 是否公开（0私有 1公开） |
| owner_id | BIGINT | 所属人ID |
| owner_name | VARCHAR(50) | 所属人姓名 |
| status | INT | 状态（0草稿 1已发布 2已归档） |
| publish_time | DATETIME | 发布时间 |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

### 表3：文档权限表 `sys_doc_permission`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| doc_id | BIGINT | 文档ID |
| target_type | INT | 对象类型（1用户 2部门 3角色） |
| target_id | BIGINT | 对象ID |
| permission_type | INT | 权限类型（1查看 2下载 3编辑） |
| create_time | DATETIME | 创建时间 |

---

## 功能要求

### 1. 分类管理接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 分类树查询 | GET | /sys/doc/category/tree | sys:doc:list | 返回分类树形结构 |
| 分类列表 | GET | /sys/doc/category/list | sys:doc:list | 按父ID查询子分类 |
| 新增分类 | POST | /sys/doc/category | sys:doc:category:add | 新增分类 |
| 更新分类 | PUT | /sys/doc/category | sys:doc:category:edit | 更新分类 |
| 删除分类 | DELETE | /sys/doc/category/{id} | sys:doc:category:delete | 删除分类（需检查是否有子分类或文档） |

### 2. 文档管理接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 文档分页查询 | GET | /sys/doc/page | sys:doc:list | 支持按分类、标题、关键词筛选 |
| 文档详情 | GET | /sys/doc/{id} | sys:doc:list | 返回文档详情及权限列表 |
| 新增文档 | POST | /sys/doc | sys:doc:add | 上传文件并创建文档 |
| 更新文档 | PUT | /sys/doc | sys:doc:edit | 更新文档信息（不更新文件） |
| 更新文件版本 | POST | /sys/doc/{id}/version | sys:doc:edit | 上传新版本文件 |
| 删除文档 | DELETE | /sys/doc/{id} | sys:doc:delete | 删除文档 |
| 发布文档 | POST | /sys/doc/{id}/publish | sys:doc:edit | 草稿→已发布 |
| 归档文档 | POST | /sys/doc/{id}/archive | sys:doc:edit | 已发布→已归档 |

### 3. 文档操作接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 下载文档 | GET | /sys/doc/{id}/download | sys:doc:download | 下载文档文件，记录下载次数 |
| 在线预览 | GET | /sys/doc/{id}/preview | sys:doc:view | 返回预览地址，记录查看次数 |
| 搜索文档 | GET | /sys/doc/search | 无 | 按关键词搜索公开文档 |

### 4. 权限管理接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 设置权限 | POST | /sys/doc/{id}/permission | sys:doc:permission | 设置文档权限（批量） |
| 获取权限列表 | GET | /sys/doc/{id}/permission | sys:doc:list | 获取文档权限配置 |
| 检查权限 | GET | /sys/doc/{id}/check | 无 | 检查当前用户对文档的权限 |

---

## 业务规则

### 1. 分类规则

- 分类支持多级嵌套（树形结构）
- 删除分类前必须检查：
  - 是否有子分类
  - 分类下是否有文档
- 分类编码唯一，不能重复

### 2. 文档规则

- 文档标题必填
- 上传文档时自动获取文件信息（名称、大小、类型）
- 版本号从1开始，每次更新文件版本号+1
- 只有草稿状态（status=0）才能修改和删除
- 发布后不能删除，只能归档
- 私有文档（is_public=0）需要权限才能访问

### 3. 权限规则

- 公开文档（is_public=1）所有人可查看
- 私有文档需要检查权限表：
  - 用户权限：target_type=1, target_id=用户ID
  - 部门权限：target_type=2, target_id=部门ID（部门下所有用户）
  - 角色权限：target_type=3, target_id=角色ID（拥有该角色的所有用户）
- 权限优先级：用户 > 部门 > 角色
- 文档所有者（owner_id）拥有所有权限

### 4. 统计规则

- 每次下载 download_count +1
- 每次预览/查看 view_count +1

---

## 核心实现要点

### 1. 树形分类查询

使用递归或一次查询后内存组装树形结构：
```java
// 返回 List<TreeNode>，每个节点包含 children 列表
```

### 2. 文件上传集成

使用现有 `SysFileService` 进行文件上传：
- 上传文件后获取返回的 file_id
- 将 file_id 关联到文档表

### 3. 权限检查逻辑

```java
// 权限检查伪代码
if (doc.isPublic == 1) return true;
if (doc.ownerId == currentUserId) return true;
// 检查用户权限
if (hasUserPermission(docId, userId)) return true;
// 检查部门权限
if (hasDeptPermission(docId, userDeptId)) return true;
// 检查角色权限
for (roleId in userRoles) {
    if (hasRolePermission(docId, roleId)) return true;
}
return false;
```

---

## 输出要求

1. SQL脚本：三张表DDL
2. 实体类：三个Entity
3. Mapper层：接口和XML
4. Service层：接口和实现
5. Controller层：三个Controller（分类、文档、权限）
6. 权限检查工具类：`DocPermissionHelper.java`

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| 树形结构实现 | 20% | 分类树正确递归/组装 |
| 文件关联处理 | 15% | 与文件服务正确集成 |
| 版本管理 | 15% | 版本号递增正确 |
| 权限检查逻辑 | 30% | 多维度权限检查正确 |
| 状态流转 | 10% | 发布/归档流程正确 |
| 统计功能 | 10% | 下载/查看次数记录 |

---

## 开始编码

请实现文档资料库模块，重点关注树形分类结构和权限检查逻辑的实现。