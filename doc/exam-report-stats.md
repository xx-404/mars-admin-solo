# 大模型编码能力测评题目 -第十三套

## 题目类型：报表统计与数据聚合

## 题目名称：运营数据统计报表模块

---

## 背景说明

系统需要对各项运营数据进行统计分析，生成多维度的报表数据，支持按时间、部门、类型等维度聚合。请开发运营统计报表模块。

---

## 数据库表设计

### 表1：统计报表配置表 `sys_report_config`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| report_name | VARCHAR(100) | 报表名称 |
| report_code | VARCHAR(50) | 报表编码，唯一 |
| report_type | INT | 报表类型（1日报 2周报 3月报 4季报 5年报 6自定义） |
| data_source | VARCHAR(200) | 数据源（表名或SQL） |
| query_sql | TEXT | 查询SQL模板 |
| dimensions | TEXT | 维度配置（JSON格式） |
| metrics | TEXT | 指标配置（JSON格式） |
| filters | TEXT | 筛选条件配置（JSON格式） |
| chart_type | INT | 图表类型（1表格 2折线图 3柱状图 4饼图 5面积图） |
| is_cache | INT | 是否缓存（0否 1是） |
| cache_period | INT | 缓存周期（分钟） |
| schedule | VARCHAR(100) | 定时生成周期（cron表达式） |
| status | INT | 状态（0禁用 1启用） |
| create_by | BIGINT | 创建人ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | INT | 删除标记 |

### 表2：报表数据表 `sys_report_data`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| report_id | BIGINT | 报表配置ID |
| report_date | DATE | 报表日期 |
| period_type | INT | 周期类型（同report_type） |
| dimension_values | TEXT | 维度值（JSON格式） |
| metric_values | TEXT | 指标值（JSON格式） |
| generate_time | DATETIME | 生成时间 |
| data_count | INT | 数据行数 |

### 表3：报表收藏表 `sys_report_favorite`

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| report_id | BIGINT | 报表ID |
| user_id | BIGINT | 用户ID |
| sort | INT | 排序号 |
| create_time | DATETIME | 创建时间 |

---

## 功能要求

### 1. 报表配置管理

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 报表配置列表 | GET | /sys/report/config/page | sys:report:list | 分页查询报表配置 |
| 报表配置详情 | GET | /sys/report/config/{id} | sys:report:list | 获取配置详情 |
| 新增报表配置 | POST | /sys/report/config | sys:report:add | 新增报表配置 |
| 更新报表配置 | PUT | /sys/report/config | sys:report:edit | 更新报表配置 |
| 删除报表配置 | DELETE | /sys/report/config/{id} | sys:report:delete | 删除报表配置 |
| 复制报表配置 | POST | /sys/report/config/{id}/copy | sys:report:add | 复制报表 |

### 2. 报表数据查询

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 报表列表 | GET | /sys/report/list | sys:report:view | 查询可用报表列表 |
| 报表数据查询 | GET | /sys/report/data/{reportCode} | sys:report:view | 查询报表数据 |
| 报表数据筛选 | POST | /sys/report/data/{reportCode}/filter | sys:report:view | 带筛选条件查询 |
| 报表趋势分析 | GET | /sys/report/trend/{reportCode} | sys:report:view | 查询历史趋势数据 |
| 报表对比分析 | POST | /sys/report/compare | sys:report:view | 对比不同时期数据 |

### 3. 报表导出

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 导出Excel | GET | /sys/report/export/{reportCode} | sys:report:export | 导出报表Excel |
| 导出PDF | GET | /sys/report/export/{reportCode}/pdf | sys:report:export | 导出报表PDF |

### 4. 收藏管理

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 我的收藏 | GET | /sys/report/favorite | 无 | 获取收藏的报表 |
| 添加收藏 | POST | /sys/report/favorite/{reportId} | 无 | 收藏报表 |
| 取消收藏 | DELETE | /sys/report/favorite/{reportId} | 无 | 取消收藏 |

---

## 维度与指标配置

### 1. 维度配置格式

```json
[
  {
    "field": "dept_id",
    "fieldName": "部门",
    "fieldType": "select",
    "dataSource": "sys_dept",
    "dataField": "id",
    "labelField": "dept_name",
    "isRequired": false,
    "defaultValue": null
  },
  {
    "field": "date_range",
    "fieldName": "日期范围",
    "fieldType": "dateRange",
    "isRequired": true,
    "defaultValue": "last_7_days"
  },
  {
    "field": "user_type",
    "fieldName": "用户类型",
    "fieldType": "select",
    "options": [
      {"label": "管理员", "value": "admin"},
      {"label": "普通用户", "value": "user"}
    ]
  }
]
```

### 2. 指标配置格式

```json
[
  {
    "field": "total_users",
    "fieldName": "总用户数",
    "aggregation": "count",
    "sourceField": "id",
    "format": "number",
    "decimals": 0
  },
  {
    "field": "active_users",
    "fieldName": "活跃用户数",
    "aggregation": "count",
    "sourceField": "id",
    "condition": "status = 1",
    "format": "number"
  },
  {
    "field": "growth_rate",
    "fieldName": "增长率",
    "type": "computed",
    "formula": "(current - previous) / previous * 100",
    "format": "percent",
    "decimals": 2
  }
]
```

---

## SQL模板设计

### 1. SQL模板示例

```sql
SELECT 
    d.dept_name as dept_name,
    COUNT(u.id) as total_users,
    COUNT(CASE WHEN u.status = 1 THEN 1 END) as active_users,
    COUNT(CASE WHEN u.create_time >= :startDate THEN 1 END) as new_users
FROM sys_user u
LEFT JOIN sys_dept d ON u.dept_id = d.id
WHERE u.deleted = 0
AND u.create_time >= :startDate
AND u.create_time <= :endDate
${deptFilter} --动态条件
GROUP BY d.id, d.dept_name
ORDER BY total_users DESC
```

### 2. 参数替换

```java
public String buildQuerySql(SysReportConfig config, Map<String, Object> params) {
    String sql = config.getQuerySql();
    
    // 1. 替换参数占位符
    for (Map.Entry<String, Object> entry : params.entrySet()) {
        sql = sql.replace(":" + entry.getKey(), String.valueOf(entry.getValue()));
    }
    
    // 2. 添加动态筛选条件
    List<String> conditions = buildFilterConditions(config.getFilters(), params);
    if (!conditions.isEmpty()) {
        sql = sql.replace("${dynamicFilter}", "AND " + String.join(" AND ", conditions));
    }
    
    return sql;
}
```

---

## 报表类型与周期

| 类型 | 说明 | 日期范围计算 |
|------|------|-------------|
| 日报 | 每日数据 | 当日00:00-23:59 |
| 周报 | 每周数据 | 本周一到周日 |
| 月报 | 每月数据 | 本月1日到月末 |
| 季报 | 每季数据 | 季初到季末 |
| 年报 | 每年数据 | 本年1月1日到12月31日 |
| 自定义 | 用户指定范围 | startDate到endDate |

---

## 定时生成任务

### 1. 定时任务实现

```java
@Component
public class ReportGenerateJob {

    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点
    public void generateDailyReports() {
        // 1. 查询所有日报类型报表配置
        List<SysReportConfig> dailyReports = reportConfigService.listByType(1);
        
        // 2. 遍历生成报表数据
        for (SysReportConfig config : dailyReports) {
            generateReport(config, LocalDate.now().minusDays(1));
        }
    }

    private void generateReport(SysReportConfig config, LocalDate reportDate) {
        // 1. 构建查询参数（日期范围）
        // 2. 执行SQL查询
        // 3. 保存报表数据到sys_report_data
    }
}
```

---

## 数据缓存策略

### 1. 缓存键设计

```
report:data:{reportCode}:{date}          -> 单日报表数据
report:data:{reportCode}:trend:{months}  -> 趋势数据
report:config:{reportCode}               -> 报表配置
```

### 2. 缓存更新策略

- 报表配置变更时清除配置缓存
- 报表数据生成时清除旧数据缓存
- 手动查询时按配置决定是否缓存

---

## 项目结构参考

```
mars-core/mars-report/
  ├── entity/
  │   ├── SysReportConfig.java
  │   ├── SysReportData.java
  │   ├── SysReportFavorite.java
  ├── vo/
  │   ├── ReportDataVO.java          # 报表数据返回格式
  │   ├── ReportFilterVO.java        # 筛选条件
  │   ├── ChartDataVO.java           # 图表数据格式
  ├── mapper/
  │   ├── SysReportConfigMapper.java
  │   ├── SysReportDataMapper.java
  │   ├── SysReportFavoriteMapper.java
  │   ├── 对应XML文件
  ├── service/
  │   ├── ReportConfigService.java
  │   ├── ReportDataService.java
  │   ├── ReportGenerateService.java  # 报表生成服务
  │   ├── ReportExportService.java    # 报表导出服务
  │   └── impl/
  │       ├── ReportConfigServiceImpl.java
  │       ├── ReportDataServiceImpl.java
  │       ├── ReportGenerateServiceImpl.java
  │       ├── ReportExportServiceImpl.java
  ├── job/
  │   └── ReportGenerateJob.java      # 定时生成任务
  ├── util/
  │   ├── SqlBuilderUtil.java         # SQL构建工具
  │   ├── DateRangeUtil.java          # 日期范围计算

mars-api/mars-admin-api/
  ├── controller/
  │   └── report/
  │       ├── ReportConfigController.java
  │       ├── ReportDataController.java
```

---

## 输出要求

1. SQL脚本：三张表DDL
2. 实体类：三个Entity
3. VO类：报表数据VO、图表数据VO
4. SQL构建工具类：`SqlBuilderUtil.java`
5. 日期范围工具类：`DateRangeUtil.java`
6. Service层：配置服务、数据服务、生成服务、导出服务
7. 定时任务：`ReportGenerateJob.java`
8. Controller层：配置控制器、数据控制器

---

## 评分标准

| 评分项 | 权重 | 说明 |
|--------|------|------|
| SQL模板处理 | 25% | 参数替换、动态条件正确 |
| 维度指标配置 | 20% | 配置格式合理，解析正确 |
| 数据聚合查询 | 20% | 多维度聚合SQL正确 |
| 定时生成任务 | 15% | 各类型报表定时生成正确 |
| 数据缓存 | 10% | 缓存策略合理 |
| 报表导出 | 10% | Excel/PDF导出实现 |

---

## 开始编码

请实现运营数据统计报表模块，重点关注SQL模板的动态构建和多维度数据聚合的实现。