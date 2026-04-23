# 大模型编码能力测评题目 -第十二套

## 题目类型：数据版本追踪

## 题目名称：数据变更历史模块

---

## 背景说明

系统需要对关键业务数据的变更进行追踪，记录每次修改的历史版本，支持版本对比、数据回滚。请开发通用数据版本管理模块。

---

## 数据库表设计

### 表1：数据版本表 `sys_data_version`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| biz_type | VARCHAR(50) | 业务类型（user/role/config等） |
| biz_id | BIGINT | 业务数据ID |
| biz_name | VARCHAR(100) | 业务数据名称（便于识别） |
| version_no | INT | 版本号 |
| version_data | TEXT | 版本数据（JSON格式，完整数据快照） |
| change_type | INT | 变更类型（1创建 2更新 3删除） |
| change_fields | VARCHAR(500) | 变更字段列表（逗号分隔） |
| change_detail | TEXT | 变更详情（JSON格式，记录字段新旧值） |
| operator_id | BIGINT | 操作人ID |
| operator_name | VARCHAR(50) | 操作人姓名 |
| operate_time | DATETIME | 操作时间 |
| operate_ip | VARCHAR(50) | 操作IP |
| remark | VARCHAR(500) | 备注 |

---

## 功能要求

### 1. 版本查询接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 版本历史列表 | GET | /sys/version/history | sys:version:list | 查询某数据的版本历史 |
| 版本详情 | GET | /sys/version/{versionId} | sys:version:list | 获取某版本详情 |
| 最新版本 | GET | /sys/version/latest/{bizType}/{bizId} | sys:version:list | 获取最新版本 |
| 版本对比 | GET | /sys/version/compare | sys:version:list | 对比两个版本差异 |
| 版本统计 | GET | /sys/version/stats | sys:version:list | 变更统计（按业务类型） |

### 2. 版本操作接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 数据回滚 | POST | /sys/version/rollback/{versionId} | sys:version:rollback | 回滚到指定版本 |
| 标记重要版本 | POST | /sys/version/{versionId}/mark | sys:version:edit | 标记为重要版本 |

---

## 版本记录触发机制

### 1. 注解方式

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VersionLog {
    /**
     * 业务类型
     */
    String bizType();

    /**
     * 业务ID字段（SpEL表达式）
     */
    String bizIdField() default "id";

    /**
     * 业务名称字段（SpEL表达式）
     */
    String bizNameField() default "name";

    /**
     * 是否记录详细变更
     */
    boolean recordDetail() default true;
}
```

### 2. 使用示例

```java
@PostMapping
@VersionLog(bizType = "user", bizIdField = "#user.id", bizNameField = "#user.username")
public Result<Void> create(@RequestBody SysUser user) {
    userService.create(user);
    return Result.ok();
}

@PutMapping
@VersionLog(bizType = "user", bizIdField = "#user.id", bizNameField = "#user.username", recordDetail = true)
public Result<Void> update(@RequestBody SysUser user) {
    userService.update(user);
    return Result.ok();
}
```

### 3. AOP拦截实现

```java
@Aspect
@Component
public class VersionLogAspect {

    @AfterReturning("@annotation(versionLog)")
    public void recordVersion(JoinPoint joinPoint, VersionLog versionLog) {
        // 1. 解析SpEL获取业务ID和名称
        // 2. 获取变更类型（根据方法名判断）
        // 3. 获取变更前后数据（查询数据库）
        // 4. 计算变更字段和详情
        // 5. 保存版本记录
    }
}
```

---

## 版本对比实现

### 1. 对比返回格式

```json
{
  "versionBefore": {
    "versionNo": 1,
    "operateTime": "2024-01-15 10:00:00",
    "operatorName": "张三"
  },
  "versionAfter": {
    "versionNo": 2,
    "operateTime": "2024-01-15 11:00:00",
    "operatorName": "李四"
  },
  "changes": [
    {
      "field": "username",
      "fieldName": "用户名",
      "oldValue": "zhangsan",
      "newValue": "lisi"
    },
    {
      "field": "status",
      "fieldName": "状态",
      "oldValue": "1",
      "newValue": "0",
      "oldValueDisplay": "启用",
      "newValueDisplay": "禁用"
    }
  ]
}
```

### 2. 字段对比算法

```java
public List<FieldChange> compareData(String oldData, String newData) {
    JSONObject oldJson = JSON.parseObject(oldData);
    JSONObject newJson = JSON.parseObject(newData);
    
    List<FieldChange> changes = new ArrayList<>();
    
    // 遍历所有字段
    for (String field : oldJson.keySet()) {
        Object oldValue = oldJson.get(field);
        Object newValue = newJson.get(field);
        
        // 比较是否变化
        if (!Objects.equals(oldValue, newValue)) {
            FieldChange change = new FieldChange();
            change.setField(field);
            change.setOldValue(oldValue);
            change.setNewValue(newValue);
            changes.add(change);
        }
    }
    return changes;
}
```

---

## 数据回滚实现

### 1. 回滚流程

```java
public void rollback(Long versionId) {
    // 1. 获取目标版本数据
    SysDataVersion version = getById(versionId);
    
    // 2. 获取当前数据（用于记录回滚前的版本）
    Object currentData = getBizData(version.getBizType(), version.getBizId());
    
    // 3. 解析目标版本数据
    Object targetData = JSON.parseObject(version.getVersionData(), getBizClass(version.getBizType()));
    
    // 4. 执行回滚（更新数据库）
    updateBizData(version.getBizType(), version.getBizId(), targetData);
    
    // 5. 记录回滚操作为新版本
    SysDataVersion rollbackVersion = new SysDataVersion();
    rollbackVersion.setBizType(version.getBizType());
    rollbackVersion.setBizId(version.getBizId());
    rollbackVersion.setChangeType(4); // 回滚类型
    rollbackVersion.setRemark("回滚到版本" + version.getVersionNo());
    save(rollbackVersion);
}
```

### 2. 回滚校验

- 回滚前检查数据是否还存在
- 回滚前检查目标版本是否有效
- 回滚操作本身也记录为新版本

---

## 业务规则

### 1. 版本号生成

- 版本号从1开始递增
- 每次变更操作版本号+1
- 同一业务数据的版本号唯一

### 2. 数据快照

- 记录完整数据快照（JSON格式）
- 包含所有字段，便于回滚
- 大数据量字段可以选择性排除（如大文本、文件内容）

### 3. 变更详情

- 记录具体变更字段列表
- 记录每个字段的新旧值
- 支持字段名称映射（便于显示）

### 4. 版本保留策略

- 默认保留最近100个版本
- 重要版本永久保留
- 定期清理过期版本（可配置保留天数）

---

## 字段映射配置

支持配置字段名称和值映射，便于版本对比时显示：

```java
public class FieldMappingConfig {
    // 字段名称映射
    private Map<String, String> fieldNameMap;
    
    // 字段值映射（如状态码→状态名称）
    private Map<String, Map<Object, String>> fieldValueMap;
}
```

示例配置：
```json
{
  "fieldNameMap": {
    "status": "状态",
    "username": "用户名",
    "createTime": "创建时间"
  },
  "fieldValueMap": {
    "status": {
      "0": "禁用",
      "1": "启用"
    }
  }
}
```

---

## 项目结构参考

```
mars-core/mars-version/
  ├── entity/
  │   ├── SysDataVersion.java
  ├── vo/
  │   ├── VersionCompareVO.java      # 版本对比结果
  │   ├── FieldChange.java           # 字段变更
  ├── annotation/
  │   └── VersionLog.java            # 版本记录注解
  ├── aspect/
  │   └── VersionLogAspect.java      # AOP拦截器
  ├── mapper/
  │   ├── SysDataVersionMapper.java
  │   ├── SysDataVersionMapper.xml
  ├── service/
  │   ├── DataVersionService.java
  │   ├── VersionCompareService.java
  │   └── impl/
  │       ├── DataVersionServiceImpl.java
  │       ├── VersionCompareServiceImpl.java
  ├── config/
  │   ├── FieldMappingConfig.java    # 字段映射配置

mars-api/mars-admin-api/
  ├── controller/
  │   └── version/
  │       └── DataVersionController.java
```

---

## 输出要求

1. SQL脚本：版本表DDL
2. 实体类：`SysDataVersion.java`
3. VO类：`VersionCompareVO.java`、`FieldChange.java`
4. 注解类：`VersionLog.java`
5. AOP拦截器：`VersionLogAspect.java`
6. Service层：版本服务、对比服务
7. Controller层：版本控制器
8. 配置类：字段映射配置

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| AOP拦截实现 | 25% | 正确拦截并记录版本 |
| SpEL解析 | 15% | 正确解析业务ID和名称 |
| 数据快照记录 | 15% | JSON快照完整准确 |
| 版本对比算法 | 20% | 字段变更正确识别 |
| 数据回滚实现 | 15% | 回滚流程正确 |
| 字段映射配置 | 10% | 字段名称和值映射合理 |

---

## 开始编码

请实现数据变更历史模块，重点关注AOP版本记录和版本对比算法的实现。