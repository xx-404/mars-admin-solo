# 大模型编码能力测评题目 -第十套

## 题目类型：动态配置管理

## 题目名称：系统参数配置模块

---

## 背景说明

系统需要支持动态参数配置，参数可以在运行时修改并立即生效，无需重启服务。请开发参数配置模块，支持参数分组、参数校验、热加载。

---

## 数据库表设计

### 表1：参数分组表 `sys_config_group`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| group_id | BIGINT | 主键，自增 |
| group_name | VARCHAR(50) | 分组名称 |
| group_code | VARCHAR(50) | 分组编码，唯一 |
| group_icon | VARCHAR(100) | 分组图标 |
| sort | INT | 排序号 |
| status | INT | 状态（0禁用 1启用） |
| remark | VARCHAR(500) | 备注 |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

### 表2：系统参数表 `sys_config`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| config_id | BIGINT | 主键，自增 |
| group_id | BIGINT | 分组ID |
| config_name | VARCHAR(100) | 参数名称 |
| config_key | VARCHAR(100) | 参数键名，全局唯一 |
| config_value | TEXT | 参数值 |
| config_type | INT | 参数类型（1字符串 2数字 3布尔 4JSON 5富文本） |
| input_type | INT | 输入类型（1输入框 2文本域 3下拉选择 4开关 5颜色选择 6日期 7数字范围） |
| options | TEXT | 选项配置（JSON格式，下拉选择时使用） |
| is_required | INT | 是否必填（0否 1是） |
| validate_rule | VARCHAR(200) | 校验规则（正则表达式） |
| default_value | VARCHAR(500) | 默认值 |
| sort | INT | 排序号 |
| status | INT | 状态（0禁用 1启用） |
| remark | VARCHAR(500) | 备注 |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

---

## 功能要求

### 1. 参数分组管理

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 分组列表 | GET | /sys/config/group/list | sys:config:list | 查询所有参数分组 |
| 新增分组 | POST | /sys/config/group | sys:config:add | 新增参数分组 |
| 更新分组 | PUT | /sys/config/group | sys:config:edit | 更新参数分组 |
| 删除分组 | DELETE | /sys/config/group/{groupId} | sys:config:delete | 删除分组（需检查是否有参数） |

### 2. 系统参数管理

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 按分组查询参数 | GET | /sys/config/group/{groupId} | sys:config:list | 查询某分组的所有参数 |
| 参数详情 | GET | /sys/config/{configId} | sys:config:list | 获取参数详情 |
| 新增参数 | POST | /sys/config | sys:config:add | 新增系统参数 |
| 更新参数 | PUT | /sys/config | sys:config:edit | 更新参数值 |
| 删除参数 | DELETE | /sys/config/{configId} | sys:config:delete | 删除参数 |
| 刷新缓存 | POST | /sys/config/refresh | sys:config:edit | 刷新参数缓存 |

### 3. 参数获取接口（公开）

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 获取参数值 | GET | /sys/config/key/{configKey} | 无 | 根据key获取参数值 |
| 获取分组参数 | GET | /sys/config/group/{groupCode}/all | 无 | 获取某分组所有参数 |

---

## 参数类型说明

| 类型 | 说明 | 值格式 |
|------|------|--------|
| 字符串 | 普通文本 | 直接存储 |
| 数字 | 整数或小数 | 数字字符串 |
| 布尔 | true/false | "true"或"false" |
| JSON | JSON对象/数组 | JSON字符串 |
| 富文本 | HTML内容 | HTML字符串 |

---

## 输入类型说明

| 类型 | 说明 | options格式 |
|------|------|-------------|
| 输入框 | 单行文本输入 | 无 |
| 文本域 | 多行文本输入 | {"rows": 5} |
| 下拉选择 | 下拉框选择 | [{"label":"选项1","value":"1"},...] |
| 开关 | 布尔开关 | 无 |
| 颜色选择 | 颜色选择器 | 无 |
| 日期 | 日期选择 | {"format":"YYYY-MM-DD"} |
| 数字范围 | 数字输入带范围 | {"min":0,"max":100,"step":1} |

---

## 缓存与热加载

### 1. 缓存策略

```
# 缓存键设计
sys:config:{configKey}       -> 单个参数值
sys:config:group:{groupCode} -> 分组所有参数（Map）

# 过期时间：30分钟
# 更新参数时立即清除缓存
```

### 2. 配置工具类

```java
public class ConfigHelper {
    
    // 获取字符串参数
    public static String getString(String configKey);
    
    // 获取数字参数
    public static Integer getInt(String configKey);
    public static Long getLong(String configKey);
    public static Double getDouble(String configKey);
    
    // 获取布尔参数
    public static Boolean getBoolean(String configKey);
    
    // 获取JSON参数
    public static <T> T getJson(String configKey, Class<T> clazz);
    
    // 获取分组所有参数
    public static Map<String, String> getGroupConfig(String groupCode);
    
    // 获取参数（带默认值）
    public static String getString(String configKey, String defaultValue);
}
```

### 3. 参数变更监听

参数更新后通过事件机制通知相关组件：

```java
@Configuration
public class ConfigChangeListener {
    
    @EventListener
    public void onConfigChange(ConfigChangeEvent event) {
        String configKey = event.getConfigKey();
        // 重新加载相关配置
        // 如数据库连接池参数、线程池参数等
    }
}
```

---

## 业务规则

### 1. 参数规则

- config_key 必须全局唯一
- 必填参数（is_required=1）值不能为空
- 如果配置了校验规则（validate_rule），值必须符合正则表达式
- 参数类型与值格式必须匹配

### 2. 校验规则示例

```json
// 手机号校验
"validate_rule": "^1[3-9]\\d{9}$"

// 邮箱校验
"validate_rule": "^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$"

// 端口号校验
"validate_rule": "^\\d{1,5}$" // 配合数字范围

// IP地址校验
"validate_rule": "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
```

### 3. 参数值校验流程

```java
public void validateConfig(SysConfig config) {
    // 1. 检查必填
    if (config.getIsRequired() == 1 && StringUtils.isBlank(config.getConfigValue())) {
        throw new BusinessException("参数值不能为空");
    }
    
    // 2. 检查类型匹配
    validateByType(config.getConfigType(), config.getConfigValue());
    
    // 3. 检查正则规则
    if (StringUtils.hasText(config.getValidateRule())) {
        if (!config.getConfigValue().matches(config.getValidateRule())) {
            throw new BusinessException("参数值格式不正确");
        }
    }
}
```

---

## 内置参数示例

### 系统基础配置分组

| 参数名 |参数键 |类型 | 默认值 |
|--------|--------|------|--------|
| 系统名称 | sys.system.name | 字符串 | Mars Admin |
| 系统Logo | sys.system.logo | 字符串 | /static/logo.png |
| 版权信息 | sys.system.copyright | 字符串 | ©2024 Mars |
| 主题色 | sys.system.themeColor | 字符串 | #1890ff |

### 安全配置分组

| 参数名 | 参数键 | 类型 | 默认值 |
|--------|--------|------|--------|
| 密码最小长度 | sys.security.passwordMinLength | 数字 | 6 |
| 密码强度要求 | sys.security.passwordStrength | 字符串 | medium |
| 登录验证码开关 | sys.security.captchaEnabled | 布尔 | true |
| 登录失败锁定次数 | sys.security.loginFailLockCount | 数字 | 5 |

---

## 项目结构参考

```
mars-core/mars-system/
  ├── entity/
  │   ├── SysConfigGroup.java
  │   └── SysConfig.java
  ├── mapper/
  │   ├── SysConfigGroupMapper.java
  │   ├── SysConfigMapper.java
  │   └── 对应XML文件
  ├── service/
  │   ├── SysConfigGroupService.java
  │   ├── SysConfigService.java
  │   └── impl/
  │       ├── SysConfigGroupServiceImpl.java
  │       ├── SysConfigServiceImpl.java
  ├── helper/
  │   └── ConfigHelper.java          # 配置获取工具类
  ├── event/
  │   ├── ConfigChangeEvent.java     # 配置变更事件
  │   ├── ConfigChangeListener.java  # 配置变更监听

mars-api/mars-admin-api/
  ├── controller/
  │   └── system/
  │       ├── SysConfigGroupController.java
  │       ├── SysConfigController.java
```

---

## 输出要求

1. SQL脚本：两张表DDL，包含内置参数数据
2. 实体类：两个Entity
3. 工具类：`ConfigHelper.java`
4. 事件类：配置变更事件和监听器
5. Service层：接口和实现（含缓存逻辑、校验逻辑）
6. Controller层：两个控制器

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| 缓存设计 | 20% | 缓存键合理，更新时清除 |
| 工具类实现 | 25% | 各类型参数获取方法完整 |
| 参数校验 | 20% | 必填、类型、正则校验正确 |
| 分组管理 | 10% | 分组与参数关联正确 |
| 热加载机制 | 15% | 配置变更事件处理 |
| 内置参数 | 10% | 提供合理的默认参数 |

---

## 开始编码

请实现系统参数配置模块，重点关注配置工具类的实现和参数校验逻辑。