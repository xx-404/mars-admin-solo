# 大模型编码能力测评题目 - 第五套

## 题目类型：数据字典与缓存

## 题目名称：动态数据字典模块

---

## 背景说明

系统需要统一的字典管理功能，支持字典类型和字典数据的动态管理，并使用Redis缓存提高查询效率。请开发完整的数据字典模块。

---

## 数据库表设计

### 表1：字典类型表 `sys_dict_type`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| dict_id | BIGINT | 主键，自增 |
| dict_name | VARCHAR(100) | 字典名称 |
| dict_type | VARCHAR(100) | 字典类型（唯一标识） |
| status | INT | 状态（0禁用 1启用） |
| remark | VARCHAR(500) | 备注 |
| create_by | BIGINT | 创建人ID |
| create_name | VARCHAR(50) | 创建人姓名 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

### 表2：字典数据表 `sys_dict_data`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| dict_code | BIGINT | 主键，自增 |
| dict_type | VARCHAR(100) | 字典类型（关联sys_dict_type） |
| dict_label | VARCHAR(100) | 字典标签（显示值） |
| dict_value | VARCHAR(100) | 字典键值（存储值） |
| css_class | VARCHAR(100) | 样式属性 |
| list_class | VARCHAR(100) | 表格回显样式 |
| is_default | INT | 是否默认（0否 1是） |
| sort | INT | 排序号 |
| status | INT | 状态（0禁用 1启用） |
| remark | VARCHAR(500) | 备注 |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

---

## 功能要求

### 1. 字典类型管理

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 类型分页查询 | GET | /sys/dict/type/page | sys:dict:list | 支持按名称、状态筛选 |
| 类型列表查询 | GET | /sys/dict/type/list | sys:dict:list | 查询所有启用的字典类型 |
| 类型详情 | GET | /sys/dict/type/{dictId} | sys:dict:list | 获取字典类型详情 |
| 新增类型 | POST | /sys/dict/type | sys:dict:add | 新增字典类型 |
| 更新类型 | PUT | /sys/dict/type | sys:dict:edit | 更新字典类型 |
| 删除类型 | DELETE | /sys/dict/type/{dictId} | sys:dict:delete | 删除类型及关联数据 |
| 刷新缓存 | POST | /sys/dict/type/refresh | sys:dict:edit | 刷新所有字典缓存 |

### 2. 字典数据管理

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 数据分页查询 | GET | /sys/dict/data/page | sys:dict:list | 支持按类型、标签筛选 |
| 按类型查询数据 | GET | /sys/dict/data/type/{dictType} | 无 | 查询某类型的所有字典数据（公开接口） |
| 按类型查询启用数据 | GET | /sys/dict/data/type/{dictType}/enabled | 无 | 只查询启用的字典数据 |
| 数据详情 | GET | /sys/dict/data/{dictCode} | sys:dict:list | 获取字典数据详情 |
| 新增数据 | POST | /sys/dict/data | sys:dict:add | 新增字典数据 |
| 更新数据 | PUT | /sys/dict/data | sys:dict:edit | 更新字典数据 |
| 删除数据 | DELETE | /sys/dict/data/{dictCode} | sys:dict:delete | 删除字典数据 |
| 批量删除 | DELETE | /sys/dict/data/batch | sys:dict:delete | 批量删除字典数据 |

---

## 缓存设计要求

### 1. 缓存键设计

```
# 字典类型缓存
dict:type:list           -> 所有字典类型列表
dict:type:{dictType}     -> 单个字典类型详情

# 字典数据缓存
dict:data:{dictType}     -> 某类型的所有字典数据列表（启用状态）
dict:data:{dictType}:all -> 某类型的所有字典数据（包含禁用）
```

### 2. 缓存读写策略

- **读取策略**：先查缓存，缓存不存在则查数据库并写入缓存
- **写入策略**：更新数据库后立即删除相关缓存
- **过期时间**：缓存30分钟过期
- **刷新机制**：提供手动刷新接口，清除所有字典缓存

### 3. 缓存工具类

创建 `DictCacheHelper` 工具类：

```java
public class DictCacheHelper {
    // 根据字典类型获取字典数据列表
    public static List<DictData> getDictDataList(String dictType);
    
    // 根据字典类型和value获取label
    public static String getDictLabel(String dictType, String dictValue);
    
    // 根据字典类型和label获取value
    public static String getDictValue(String dictType, String dictLabel);
    
    // 清除指定类型的缓存
    public static void clearCache(String dictType);
    
    // 清除所有字典缓存
    public static void clearAllCache();
}
```

---

## 业务规则

### 1. 字典类型规则

- dict_type 必须唯一，不能重复
- 删除字典类型时，同时删除该类型下的所有字典数据
- 禁用字典类型时，该类型下的字典数据不可使用

### 2. 字典数据规则

- 同一类型下，dict_value 必须唯一
- 同一类型下，只能有一个 is_default=1 的数据
- 新增默认数据时，如果已有默认数据，将旧的默认数据改为非默认
- 排序号用于控制显示顺序

### 3. 缓存规则

- 查询启用字典数据时使用缓存
- 查询包含禁用数据时不使用缓存（管理界面）
- 新增、更新、删除字典数据后清除该类型的缓存
- 字典类型变更后清除相关缓存

---

## 使用示例

### 前端调用

```javascript
// 获取性别字典
GET /sys/dict/data/type/sys_user_sex/enabled

// 返回结果
[
  { dictLabel: "男", dictValue: "1", isDefault: 1 },
  { dictLabel: "女", dictValue: "2", isDefault: 0 }
]
```

### 后端使用

```java
// 在Service中使用工具类
String genderLabel = DictCacheHelper.getDictLabel("sys_user_sex", "1"); // 返回"男"
String genderValue = DictCacheHelper.getDictValue("sys_user_sex", "男"); // 返回"1"
```

---

## 输出要求

1. SQL脚本：两张表DDL
2. 实体类：`SysDictType.java` 和 `SysDictData.java`
3. Mapper层：接口和XML
4. Service层：接口和实现（含缓存逻辑）
5. 缓存工具类：`DictCacheHelper.java`
6. Controller层：两个Controller

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| 缓存读写策略 | 30% | 缓存键设计合理，读写逻辑正确 |
| 缓存清除时机 | 20% | 数据变更时正确清除缓存 |
| 工具类实现 | 20% | 工具类方法完整，使用方便 |
| 业务规则校验 | 15% | 唯一性校验、默认值处理正确 |
| 接口设计 | 15% | 公开接口与管理接口分离合理 |

---

## 开始编码

请实现数据字典模块，重点关注Redis缓存的设计和工具类的实现。