# 大模型编码能力测评题目 - 第七套

## 题目类型：数据权限控制

## 题目名称：部门数据权限模块

---

## 背景说明

系统需要实现细粒度的数据权限控制，不同角色可以看到不同范围的数据。请开发数据权限模块，支持按部门、自定义范围等方式控制数据可见性。

---

## 数据库表设计

### 表1：角色数据权限表 `sys_role_data_scope`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| role_id | BIGINT | 角色ID |
| data_scope | INT | 数据范围（1全部 2自定义 3本部门 4本部门及以下 5仅本人） |
| scope_dept_ids | VARCHAR(500) | 自定义部门权限（逗号分隔的部门ID） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

---

## 数据范围类型说明

| 值 | 类型 | 说明 |
|----|------|------|
| 1 | 全部数据权限 | 可以查看所有数据，不受限制 |
| 2 | 自定义数据权限 | 只能查看指定部门的数据 |
| 3 | 本部门数据权限 | 只能查看本部门的数据 |
| 4 | 本部门及以下数据权限 | 可以查看本部门及子部门的数据 |
| 5 | 仅本人数据权限 | 只能查看自己创建的数据 |

---

## 功能要求

### 1. 数据权限配置接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 角色数据权限详情 | GET | /sys/role/data-scope/{roleId} | sys:role:edit | 获取角色的数据权限配置 |
| 设置角色数据权限 | POST | /sys/role/data-scope | sys:role:edit | 设置角色的数据范围 |
| 角色部门授权 | POST | /sys/role/data-scope/dept | sys:role:edit | 设置自定义部门权限 |

### 2. 数据权限查询接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 获取用户数据范围 | GET | /sys/data-scope/user | 无 | 获取当前用户的数据权限范围 |
| 获取用户可访问部门 | GET | /sys/data-scope/depts | 无 | 获取当前用户可访问的部门列表 |

---

## MyBatis Plus拦截器实现

### 1. DataScope注解

创建注解标记需要数据权限控制的查询：

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    /**
     * 部门表的别名
     */
    String deptAlias() default "d";

    /**
     * 用户表的别名
     */
    String userAlias() default "u";

    /**
     * 部门ID字段名
     */
    String deptIdField() default "dept_id";

    /**
     * 用户ID字段名
     */
    String userIdField() default "create_by";
}
```

### 2. DataScopeInterceptor拦截器

实现 MyBatis Plus 内部拦截器，在查询SQL执行前自动添加数据权限条件：

```java
public class DataScopeInterceptor implements InnerInterceptor {
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, 
                            Object parameter, RowBounds rowBounds, 
                            ResultHandler resultHandler, BoundSql boundSql) {
        // 1. 检查方法是否有@DataScope注解
        // 2. 获取当前用户的数据权限范围
        // 3. 根据数据范围类型生成SQL条件
        // 4. 将条件添加到原SQL中
    }
}
```

### 3. SQL生成逻辑

根据不同的数据范围类型生成SQL片段：

```
数据范围=1（全部）：不添加条件

数据范围=2（自定义）：
  AND dept_id IN (自定义部门列表)

数据范围=3（本部门）：
  AND dept_id = 当前用户部门ID

数据范围=4（本部门及以下）：
  AND dept_id IN (当前部门及所有子部门ID)

数据范围=5（仅本人）：
  AND create_by = 当前用户ID
```

---

## 使用示例

### 1. Controller中使用

```java
@GetMapping("/page")
@DataScope(deptAlias = "d", userAlias = "u")
public Result<PageResult<SysUser>> page(...) {
    // 查询会自动添加数据权限条件
    return Result.ok(userService.page(...));
}
```

### 2. Service中使用

```java
@Override
@DataScope(deptAlias = "d", deptIdField = "dept_id")
public PageResult<SysUser> page(...) {
    // mapper查询会自动过滤数据
    return PageResult.of(baseMapper.selectUserPage(pageParam, wrapper));
}
```

### 3. Mapper XML中

```xml
<select id="selectUserPage" resultType="SysUser">
    SELECT u.*, d.dept_name 
    FROM sys_user u
    LEFT JOIN sys_dept d ON u.dept_id = d.id
    WHERE u.deleted = 0
    ${dataScope} <!-- 数据权限SQL会注入到这里 -->
    ORDER BY u.create_time DESC
</select>
```

---

## 部门层级查询实现

获取本部门及所有子部门ID：

```java
public List<Long> getDeptAndChildrenIds(Long deptId) {
    // 1. 查询部门表，使用 parent_id 字段
    // 2. 递归查询或使用 like 方式查询所有子部门
    // 例如：WHERE id = deptId OR parent_id = deptId 
    //       OR parent_id LIKE 'deptId,%' 或使用递归
}
```

---

## 业务规则

### 1. 权限叠加规则

用户可能有多个角色，数据权限取最大范围：
- 如果任一角色有全部权限（dataScope=1），则用户拥有全部权限
- 否则取各角色数据范围的并集

### 2. 管理员特殊处理

超级管理员（admin角色）默认拥有全部数据权限，无需配置

### 3. 权限缓存

用户的数据权限范围缓存到Redis：
- 缓存键：`data_scope:user:{userId}`
- 角色权限变更时清除缓存

---

## 项目结构参考

```
mars-core/mars-system/
  ├── entity/
  │   └── SysRoleDataScope.java
  ├── mapper/
  │   ├── SysRoleDataScopeMapper.java
  │   └── SysRoleDataScopeMapper.xml
  ├── annotation/
  │   └── DataScope.java
  ├── interceptor/
  │   └── DataScopeInterceptor.java
  ├── service/
  │   ├── DataScopeService.java
  │   └── impl/
  │       └── DataScopeServiceImpl.java
  ├── helper/
  │   └── DataScopeHelper.java    # 获取用户数据范围的工具类
```

---

## 输出要求

1. SQL脚本：角色数据权限表DDL
2. 实体类：`SysRoleDataScope.java`
3. 注解类：`DataScope.java`
4. 拦截器：`DataScopeInterceptor.java`
5. 工具类：`DataScopeHelper.java`
6. Service层：接口和实现
7. Controller层：数据权限配置控制器

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| MyBatis拦截器实现 | 30% | 正确拦截并修改SQL |
| SQL条件生成 | 25% | 各数据范围SQL生成正确 |
| 部门层级查询 | 15% | 正确获取子部门列表 |
| 权限叠加逻辑 | 15% | 多角色权限计算正确 |
| 注解使用便捷 | 10% | 注解设计合理，使用简单 |
| 缓存处理 | 5% | 权限缓存正确管理 |

---

## 开始编码

请实现部门数据权限模块，重点关注MyBatis拦截器的实现和SQL动态拼接逻辑。