# 大模型编码能力测评题目 - 第三套

## 题目类型：系统扩展功能开发

## 题目名称：资产领用管理模块（含Excel导入导出与定时任务）

---

## 背景说明

本项目使用 EasyExcel 3.3.3 进行Excel处理，支持定时任务调度。

请开发一个资产领用管理模块，包含Excel批量导入导出、定时归还提醒、缓存优化等功能。

---

## 数据库表设计

### 表1：资产表 `sys_asset`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| asset_code | VARCHAR(50) | 资产编码，唯一 |
| asset_name | VARCHAR(100) | 资产名称 |
| asset_type | INT | 资产类型（1电脑 2打印机 3投影仪 4办公桌 5其他） |
| brand | VARCHAR(50) | 品牌 |
| model | VARCHAR(100) | 型号 |
| purchase_date | DATE | 购置日期 |
| purchase_price | DECIMAL(10,2) | 购置价格 |
| status | INT | 状态（0闲置 1已领用 2维修中 3报废） |
| location | VARCHAR(200) | 存放位置 |
| remark | VARCHAR(500) | 备注 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

### 表2：领用记录表 `sys_asset_usage`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| asset_id | BIGINT | 资产ID |
| asset_code | VARCHAR(50) | 资产编码 |
| asset_name | VARCHAR(100) | 资产名称 |
| user_id | BIGINT | 领用人ID |
| user_name | VARCHAR(50) | 领用人姓名 |
| dept_id | BIGINT | 领用人部门ID |
| dept_name | VARCHAR(100) | 领用人部门名称 |
| usage_type | INT | 领用类型（1领用 2归还） |
| plan_return_date | DATE | 计划归还日期 |
| actual_return_date | DATE | 实际归还日期 |
| usage_status | INT | 领用状态（0领用中 1已归还 2逾期未还） |
| purpose | TEXT | 领用用途说明 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

---

## 功能要求

### 1. 资产管理接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 分页查询 | GET | /sys/asset/page | sys:asset:list | 支持按编码、名称、类型、状态筛选 |
| 详情查询 | GET | /sys/asset/{id} | sys:asset:list | 返回资产详情 |
| 新增资产 | POST | /sys/asset | sys:asset:add | 单个新增 |
| 更新资产 | PUT | /sys/asset | sys:asset:edit | 更新资产信息 |
| 删除资产 | DELETE | /sys/asset/{id} | sys:asset:delete | 逻辑删除 |

### 2. 领用管理接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 领用申请 | POST | /sys/asset/{id}/borrow | sys:asset:borrow | 领用资产，更新资产状态 |
| 归还资产 | POST | /sys/asset/{id}/return | sys:asset:borrow | 归还资产，更新领用状态 |
| 我的领用记录 | GET | /sys/asset/my-usage | 无 | 当前用户的领用记录 |
| 领用记录列表 | GET | /sys/asset/usage/page | sys:asset:list | 全部领用记录分页 |

### 3. Excel导入导出（核心功能）

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 导出资产 | GET | /sys/asset/export | sys:asset:export | 导出资产列表Excel |
| 导入资产 | POST | /sys/asset/import | sys:asset:import | 批量导入资产Excel |
| 下载导入模板 | GET | /sys/asset/template | 无 | 下载导入模板Excel |

### 4. 统计接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 资产统计 | GET | /sys/asset/stats | sys:asset:list | 按类型、状态统计资产数量 |
| 领用统计 | GET | /sys/asset/usage-stats | sys:asset:list | 按部门统计领用情况 |

---

## Excel导入导出详细要求

### 导出格式

导出Excel包含以下列（参考用户导出格式）：

| 列名 | 字段 | 说明 |
|------|------|------|
| 资产编码 | assetCode | |
| 资产名称 | assetName | |
| 资产类型 | assetTypeStr | 转换为中文（电脑、打印机等） |
| 品牌 | brand | |
| 型号 | model | |
| 购置日期 | purchaseDate | 格式：yyyy-MM-dd |
| 购置价格 | purchasePrice | |
| 状态 | statusStr | 转换为中文（闲置、已领用等） |
| 存放位置 | location | |

### 导入规则

1. **数据校验**：
   - 资产编码必填且不能重复（检查现有数据）
   - 资产名称必填
   - 资产类型必须有效（1-5）
   - 购置价格必须为正数

2. **错误处理**：
   - 记录每行导入结果（成功/失败）
   - 返回成功数量、失败数量、错误详情列表
   - 错误详情包含：行号、错误原因

3. **导入监听器**：
   - 参考 `SysUserImportListener` 实现自定义监听器
   - 批量处理，每100条提交一次

### 导入模板

生成包含表头的空Excel文件作为导入模板。

---

## 定时任务要求

### 归还提醒任务

创建定时任务 `AssetReturnReminderJob`：

1. **执行频率**：每天上午9点执行

2. **任务逻辑**：
   - 查询所有 usage_status=0（领用中）且 plan_return_date <= 当前日期的记录
   - 这些是逾期未归还的资产
   - 更新 usage_status=2（逾期未还）
   - 发送通知给领用人（使用现有的消息通知服务）
   - 通知内容：您领用的资产 {assetName} 已逾期，请及时归还

3. **通知方式**：
   - 使用 `SysNoticeService` 创建系统通知
   - targetType=1（指定用户），targetIds=[领用人ID]

4. **任务注册**：
   - 使用项目现有的任务调度框架（参考 `mars-job` 模块）

---

## 缓存优化要求

### 资产类型缓存

1. **场景**：资产类型、状态等字典数据频繁查询，使用缓存减少数据库访问

2. **实现**：
   - 使用 Redis 缓存资产统计数据
   - 缓存键：`asset:stats:type:{typeId}` 和 `asset:stats:status:{statusId}`
   - 缓存过期时间：30分钟
   - 资产变更时清除相关缓存

3. **参考**：项目使用 `mars-redis` 模块，参考 Redis 操作方式

---

## 项目结构参考

```
mars-core/mars-asset/
  ├── entity/
  │   ├── SysAsset.java
  │   └── SysAssetUsage.java
  ├── excel/
  │   ├── SysAssetExcel.java          # Excel导出DTO
  │   └── SysAssetImportListener.java  # 导入监听器
  ├── mapper/
  │   ├── SysAssetMapper.java
  │   ├── SysAssetUsageMapper.java
  │   ├── SysAssetMapper.xml
  │   └── SysAssetUsageMapper.xml
  ├── service/
  │   ├── SysAssetService.java
  │   ├── SysAssetUsageService.java
  │   └── impl/
  │       ├── SysAssetServiceImpl.java
  │       ├── SysAssetUsageServiceImpl.java

mars-job/
  ├── jobs/
  │   └── AssetReturnReminderJob.java  # 定时任务

mars-api/mars-admin-api/
  ├── controller/
  │   └── asset/
  │       └── SysAssetController.java
```

---

## 参考代码

- Excel导入导出：参考 `SysUserServiceImpl.exportUsers()` 和 `importUsers()`
- Excel监听器：参考 `SysUserImportListener`
- 定时任务：参考 `mars-job` 模块结构
- 缓存操作：参考 `mars-redis` 模块

---

## 输出要求

1. **SQL脚本**：创建两张表的DDL

2. **实体类**：`SysAsset.java` 和 `SysAssetUsage.java`

3. **Excel DTO和监听器**：
   - `SysAssetExcel.java`（导出格式）
   - `SysAssetImportListener.java`（导入监听器）

4. **Mapper层**：接口和XML

5. **Service层**：接口和实现类（含缓存逻辑）

6. **Controller层**：`SysAssetController.java`

7. **定时任务**：`AssetReturnReminderJob.java`

8. **模块配置**：pom.xml更新

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| Excel导出格式 | 20% | 列定义正确，类型转换正确 |
| Excel导入校验 | 25% | 数据校验完整，错误处理规范 |
| 导入监听器 | 15% | 批量处理实现正确 |
| 定时任务逻辑 | 20% | 逾期检测、状态更新、通知发送 |
| 缓存实现 | 10% | 缓存读写正确，变更时清除 |
| 代码风格 | 10% | 符合项目规范 |

---

## 开始编码

请开始实现资产领用管理模块，重点关注Excel导入导出的数据处理和定时任务的实现。