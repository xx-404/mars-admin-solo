# 大模型编码能力测评题目 -第十一套

## 题目类型：嵌套数据结构与交互

## 题目名称：评论回复模块

---

## 背景说明

系统需要支持对各类内容的评论回复功能，评论支持多层级嵌套回复、点赞、举报。请开发通用评论模块，可应用于文章、公告、文档等多种场景。

---

## 数据库表设计

### 表1：评论表 `sys_comment`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| biz_type | VARCHAR(50) | 业务类型（article/notice/document等） |
| biz_id | BIGINT | 业务ID |
| parent_id | BIGINT | 父评论ID（0表示顶级评论） |
| root_id | BIGINT | 根评论ID（顶级评论ID，用于查询整棵评论树） |
| reply_user_id | BIGINT | 回复用户ID（回复某人时记录） |
| reply_user_name | VARCHAR(50) | 回复用户姓名 |
| user_id | BIGINT | 评论用户ID |
| user_name | VARCHAR(50) | 评论用户姓名 |
| user_avatar | VARCHAR(200) | 评论用户头像 |
| content | TEXT | 评论内容 |
| like_count | INT | 点赞数 |
| reply_count | INT | 回复数 |
| is_top | INT | 是否置顶（0否 1是） |
| is_hot | INT | 是否热门（0否 1是，点赞数超过阈值自动标记） |
| status | INT | 状态（0正常 1隐藏 2已删除） |
| delete_reason | VARCHAR(200) | 删除原因 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

### 表2：评论点赞表 `sys_comment_like`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| comment_id | BIGINT | 评论ID |
| user_id | BIGINT | 点赞用户ID |
| create_time | DATETIME | 创建时间 |

### 表3：评论举报表 `sys_comment_report`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| comment_id | BIGINT | 评论ID |
| report_user_id | BIGINT | 举报用户ID |
| report_type | INT | 举报类型（1垃圾广告 2辱骂攻击 3违法违规 4其他） |
| report_reason | VARCHAR(500) | 举报原因 |
| status | INT | 处理状态（0待处理 1已处理 2已驳回） |
| handle_result | INT | 处理结果（1隐藏评论 2删除评论 3驳回举报） |
| handle_user_id | BIGINT | 处理人ID |
| handle_time | DATETIME | 处理时间 |
| create_time | DATETIME | 创建时间 |

---

## 功能要求

### 1. 评论管理接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 发表评论 | POST | /sys/comment | 无 | 发表顶级评论或回复 |
| 评论列表 | GET | /sys/comment/list | 无 | 获取某业务的评论列表（分页） |
| 评论回复列表 | GET | /sys/comment/{rootId}/replies | 无 | 获取某评论的所有回复（树形） |
| 删除评论 | DELETE | /sys/comment/{id} | sys:comment:delete | 删除评论（自己或管理员） |
| 置顶评论 | POST | /sys/comment/{id}/top | sys:comment:manage | 置顶/取消置顶评论 |
| 隐藏评论 | POST | /sys/comment/{id}/hide | sys:comment:manage | 隐藏评论 |
| 我的评论 | GET | /sys/comment/my | 无 | 当前用户发表的评论 |

### 2. 点赞接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 点赞评论 | POST | /sys/comment/{id}/like | 无 | 点赞评论 |
| 取消点赞 | DELETE | /sys/comment/{id}/like | 无 | 取消点赞 |
| 检查点赞状态 | GET | /sys/comment/{id}/liked | 无 | 检查是否已点赞 |

### 3. 举报接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 举报评论 | POST | /sys/comment/{id}/report | 无 | 举报评论 |
| 举报列表 | GET | /sys/comment/report/page | sys:comment:report | 举报列表（管理员） |
| 处理举报 | POST | /sys/comment/report/{id}/handle | sys:comment:report | 处理举报 |

### 4. 统计接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 评论统计 | GET | /sys/comment/stats/{bizType}/{bizId} | 无 | 统计评论数、点赞数等 |

---

## 评论树形结构设计

### 1. 数据结构

```
顶级评论 (parent_id=0, root_id=自身ID)
├── 二级回复 (parent_id=顶级评论ID, root_id=顶级评论ID)
│   ├── 三级回复 (parent_id=二级回复ID, root_id=顶级评论ID)
│   └── 三级回复
├── 二级回复
│   └── 三级回复
```

### 2. 查询策略

**方案一：两次查询**
1. 第一次查询顶级评论列表（分页）
2. 第二次查询每条顶级评论的回复树

**方案二：递归查询**
1. 查询所有评论（按root_id分组）
2. 内存中组装树形结构

### 3. 返回格式

```json
{
  "id": 1,
  "content": "这是一条评论",
  "userName": "张三",
  "userAvatar": "/avatar/1.png",
  "likeCount": 10,
  "replyCount": 5,
  "createTime": "2024-01-15 10:00:00",
  "isLiked": true,
  "children": [
    {
      "id": 2,
      "parentId": 1,
      "content": "回复评论",
      "replyUserName": "张三",
      "userName": "李四",
      "children": [...]
    }
  ]
}
```

---

## 业务规则

### 1. 评论规则

- 评论内容必填，最多500字
- 支持表情符号
- 评论后自动填充用户信息（从当前登录用户）
- 回复某人时记录 reply_user_id 和 reply_user_name

### 2. 层级限制

- 评论最多支持3层嵌套（顶级→二级→三级）
- 四级及以上回复统一作为三级回复处理（显示@原用户）

### 3. 点赞规则

- 每个用户对同一评论只能点赞一次
- 点赞后评论的 like_count +1
- 取消点赞后 like_count -1
- like_count超过10自动标记为热门评论（is_hot=1）

### 4. 举报规则

- 同一评论同一用户只能举报一次
- 举报后进入待处理状态
- 处理结果：隐藏评论（status=1）或删除评论（status=2）

### 5. 删除规则

- 用户只能删除自己的评论
- 管理员可以删除任何评论
- 删除评论时，子评论可以选择：
  - 方案A：一并删除
  - 方案B：保留，显示"原评论已删除"
- 删除顶级评论时，更新 root_id下的所有评论状态

---

## 核心实现要点

### 1. 评论发表逻辑

```java
public void createComment(SysComment comment) {
    // 1. 校验内容长度
    // 2. 填充用户信息
    // 3. 处理父评论关系
    if (comment.getParentId() == 0) {
        // 顶级评论：root_id 插入后回填为自身ID
    } else {
        // 回复评论：root_id = 父评论的 root_id
        SysComment parent = getById(comment.getParentId());
        comment.setRootId(parent.getRootId());
        // 更新父评论的 reply_count
        parent.setReplyCount(parent.getReplyCount() + 1);
        updateById(parent);
    }
    // 4. 保存评论
}
```

### 2. 评论树组装

```java
public List<CommentVO> buildCommentTree(List<SysComment> comments) {
    // 1. 过滤顶级评论
    List<SysComment> rootComments = comments.stream()
        .filter(c -> c.getParentId() == 0)
        .collect(Collectors.toList());
    
    // 2. 为每个顶级评论组装子树
    for (SysComment root : rootComments) {
        List<SysComment> children = comments.stream()
            .filter(c -> c.getRootId().equals(root.getId()) && c.getParentId() != 0)
            .collect(Collectors.toList());
        root.setChildren(buildChildrenTree(root.getId(), children));
    }
    return rootComments;
}
```

### 3. 点赞幂等性

```java
public void likeComment(Long commentId, Long userId) {
    // 检查是否已点赞
    SysCommentLike exist = likeMapper.selectOne(
        new LambdaQueryWrapper<SysCommentLike>()
            .eq(SysCommentLike::getCommentId, commentId)
            .eq(SysCommentLike::getUserId, userId)
    );
    if (exist != null) {
        throw new BusinessException("已点赞，请勿重复操作");
    }
    // 保存点赞记录
    SysCommentLike like = new SysCommentLike();
    like.setCommentId(commentId);
    like.setUserId(userId);
    likeMapper.insert(like);
    // 更新点赞数
    baseMapper.updateLikeCount(commentId, 1);
}
```

---

## 项目结构参考

```
mars-core/mars-comment/
  ├── entity/
  │   ├── SysComment.java
  │   ├── SysCommentLike.java
  │   └── SysCommentReport.java
  ├── vo/
  │   └── CommentVO.java            # 评论树形VO
  ├── mapper/
  │   ├── SysCommentMapper.java
  │   ├── SysCommentLikeMapper.java
  │   ├── SysCommentReportMapper.java
  │   └── 对应XML文件
  ├── service/
  │   ├── SysCommentService.java
  │   ├── SysCommentLikeService.java
  │   ├── SysCommentReportService.java
  │   └── impl/
  │       ├── SysCommentServiceImpl.java
  │       ├── SysCommentLikeServiceImpl.java
  │       ├── SysCommentReportServiceImpl.java

mars-api/mars-admin-api/
  ├── controller/
  │   └── comment/
  │       ├── SysCommentController.java
  │       ├── SysCommentReportController.java
```

---

## 输出要求

1. SQL脚本：三张表DDL
2. 实体类：三个Entity
3. VO类：`CommentVO.java`（树形结构）
4. Mapper层：接口和XML
5. Service层：接口和实现（含树形组装、点赞幂等）
6. Controller层：评论控制器、举报控制器

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| 树形结构实现 | 30% | 评论树正确组装和返回 |
| 层级处理 | 15% | 父评论、root_id关系正确 |
| 点赞幂等性 | 20% | 点赞/取消点赞正确处理 |
| 回复计数 | 10% | reply_count正确更新 |
| 举报处理 | 15% | 举报流程完整 |
| 删除处理 | 10% | 删除时子评论处理合理 |

---

## 开始编码

请实现评论回复模块，重点关注多层级嵌套评论树的设计和点赞幂等性的实现。