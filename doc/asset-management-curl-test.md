# 资产领用管理接口测试用例

## 一、数据库初始化

在运行测试之前，请先执行数据库脚本：

```sql
-- 文件位置: sql/asset-management.sql
-- 请在MySQL数据库中执行此脚本，创建asset表和asset_loan表
```

## 二、接口说明

### 基础信息
- **服务地址**: http://localhost:8080
- **API前缀**: /api (所有@RestController都会自动添加此前缀)
- **认证方式**: Sa-Token (需要先登录获取token)
- **Token传递方式**: HTTP Header - `Authorization: {token值}`
- **Content-Type**: application/json

### 权限说明
以下接口需要对应的权限（admin角色拥有所有权限）：
- `biz:asset:list` - 资产列表查询
- `biz:asset:query` - 资产详情查询
- `biz:asset:add` - 新增资产
- `biz:asset:edit` - 编辑资产
- `biz:asset:remove` - 删除资产
- `biz:asset:borrow` - 资产领用
- `biz:asset:return` - 资产归还

## 三、测试用例

### 1. 用户登录（获取Token）

**重要**: 请先执行此步骤获取真实的token，后续所有接口都需要使用此token。

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**实际响应格式**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321",
    "userId": 1,
    "username": "admin",
    "nickname": "管理员",
    "avatar": null,
    "extra": null
  }
}
```

**关键说明**:
1. 从响应中获取 `data.token` 的值，这是后续接口需要的token
2. 后续所有请求需要在Header中添加: `Authorization: {你的token值}`

**示例**:
```bash
# 假设登录返回的token是 "5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
# 后续请求需要添加Header:
# -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

---

### 2. 资产管理接口测试

**注意**: 以下命令中的token需要替换为你实际登录获取的token值。

#### 2.1 新增资产

```bash
curl -X POST http://localhost:8080/api/biz/asset \
  -H "Content-Type: application/json" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321" \
  -d '{
    "assetName": "笔记本电脑",
    "assetCode": "ASSET-001",
    "assetType": "电子设备",
    "specification": "ThinkPad X1 Carbon",
    "brand": "联想",
    "totalQuantity": 10,
    "availableQuantity": 10,
    "location": "A栋3楼机房",
    "purchaseDate": "2024-01-15",
    "purchasePrice": 8999.00,
    "managerId": 1,
    "remark": "开发用笔记本电脑"
  }'
```

#### 2.2 新增多个资产

```bash
# 新增资产2
curl -X POST http://localhost:8080/api/biz/asset \
  -H "Content-Type: application/json" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321" \
  -d '{
    "assetName": "投影仪",
    "assetCode": "ASSET-002",
    "assetType": "办公设备",
    "specification": "EPSON CB-X50",
    "brand": "爱普生",
    "totalQuantity": 5,
    "availableQuantity": 5,
    "location": "会议室仓库",
    "purchaseDate": "2024-02-20",
    "purchasePrice": 5500.00,
    "managerId": 1,
    "remark": "会议用投影仪"
  }'

# 新增资产3
curl -X POST http://localhost:8080/api/biz/asset \
  -H "Content-Type: application/json" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321" \
  -d '{
    "assetName": "键盘",
    "assetCode": "ASSET-003",
    "assetType": "电子设备",
    "specification": "机械键盘",
    "brand": "樱桃",
    "totalQuantity": 20,
    "availableQuantity": 20,
    "location": "行政仓库",
    "purchaseDate": "2024-03-10",
    "purchasePrice": 899.00,
    "managerId": 1,
    "remark": "办公用机械键盘"
  }'
```

#### 2.3 分页查询资产列表

```bash
# 分页查询第一页，每页10条
curl -X GET "http://localhost:8080/api/biz/asset/page?page=1&pageSize=10" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 2.4 带条件查询资产列表

```bash
# 按资产名称模糊查询
curl -X GET "http://localhost:8080/api/biz/asset/page?page=1&pageSize=10&assetName=笔记本" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"

# 按资产类型查询
curl -X GET "http://localhost:8080/api/biz/asset/page?page=1&pageSize=10&assetType=电子设备" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"

# 按状态查询（1-正常）
curl -X GET "http://localhost:8080/api/biz/asset/page?page=1&pageSize=10&status=1" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 2.5 查询资产详情

```bash
# 查询ID为1的资产详情
curl -X GET "http://localhost:8080/api/biz/asset/1" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 2.6 更新资产信息

```bash
curl -X PUT http://localhost:8080/api/biz/asset/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321" \
  -d '{
    "assetName": "笔记本电脑（更新）",
    "location": "A栋3楼机房2号柜",
    "remark": "开发用笔记本电脑，性能强劲"
  }'
```

---

### 3. 资产领用接口测试

#### 3.1 领用资产

```bash
# 领用ID为1的资产，数量2台，预计7天后归还
curl -X POST "http://localhost:8080/api/biz/asset/1/borrow?quantity=2&borrowReason=项目开发需要&expectedReturnTime=2026-04-30%2018:00:00" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

**参数说明**:
- `quantity`: 领用数量（必填）
- `borrowReason`: 领用原因（可选）
- `expectedReturnTime`: 预计归还时间（可选，格式：yyyy-MM-dd HH:mm:ss）

#### 3.2 领用另一个资产

```bash
# 领用ID为2的投影仪，数量1台
curl -X POST "http://localhost:8080/api/biz/asset/2/borrow?quantity=1&borrowReason=会议室演示&expectedReturnTime=2026-04-25%2017:00:00" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

---

### 4. 领用记录查询接口测试

#### 4.1 分页查询领用记录

```bash
curl -X GET "http://localhost:8080/api/biz/asset/loan/page?page=1&pageSize=10" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 4.2 按条件查询领用记录

```bash
# 按资产ID查询
curl -X GET "http://localhost:8080/api/biz/asset/loan/page?page=1&pageSize=10&assetId=1" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"

# 按领用人ID查询
curl -X GET "http://localhost:8080/api/biz/asset/loan/page?page=1&pageSize=10&borrowerId=1" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"

# 按状态查询（0-领用中，1-已归还，2-部分归还）
curl -X GET "http://localhost:8080/api/biz/asset/loan/page?page=1&pageSize=10&status=0" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 4.3 查询领用记录详情

```bash
# 查询ID为1的领用记录详情
curl -X GET "http://localhost:8080/api/biz/asset/loan/1" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 4.4 查询某资产的所有领用记录

```bash
# 查询资产ID为1的所有领用记录
curl -X GET "http://localhost:8080/api/biz/asset/loan/asset/1" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 4.5 查询某用户的所有领用记录

```bash
# 查询用户ID为1的所有领用记录
curl -X GET "http://localhost:8080/api/biz/asset/loan/borrower/1" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

---

### 5. 资产归还接口测试

#### 5.1 全部归还

**重要**: 请先执行"分页查询领用记录"接口，获取实际的`loanId`，然后替换下面的`loanId`参数。

```bash
# 归还资产ID为1，领用记录ID为1，数量2台（全部归还）
curl -X POST "http://localhost:8080/api/biz/asset/1/return?loanId=1&quantity=2&returnRemark=设备完好" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 5.2 部分归还

```bash
# 先领用3台键盘
curl -X POST "http://localhost:8080/api/biz/asset/3/borrow?quantity=3&borrowReason=新员工入职" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"

# 先归还1台（需要先查询获取实际的loanId）
curl -X POST "http://localhost:8080/api/biz/asset/3/return?loanId=3&quantity=1&returnRemark=员工离职归还" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"

# 再归还剩余2台
curl -X POST "http://localhost:8080/api/biz/asset/3/return?loanId=3&quantity=2&returnRemark=全部归还" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

---

### 6. 删除接口测试

#### 6.1 删除领用记录

```bash
# 删除ID为1的领用记录
curl -X DELETE "http://localhost:8080/api/biz/asset/loan/1" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 6.2 批量删除领用记录

```bash
# 删除ID为1,2,3的领用记录
curl -X DELETE "http://localhost:8080/api/biz/asset/loan/1,2,3" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 6.3 删除资产

```bash
# 删除ID为1的资产
curl -X DELETE "http://localhost:8080/api/biz/asset/1" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

#### 6.4 批量删除资产

```bash
# 删除ID为1,2,3的资产
curl -X DELETE "http://localhost:8080/api/biz/asset/1,2,3" \
  -H "Authorization: 5a99b0a6-5e3a-4c8d-9f1e-8d7c6b5a4321"
```

---

## 四、接口汇总

### 资产管理接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 分页查询 | GET | /api/biz/asset/page | biz:asset:list | 分页查询资产列表 |
| 列表查询 | GET | /api/biz/asset/list | biz:asset:list | 查询资产列表 |
| 详情查询 | GET | /api/biz/asset/{id} | biz:asset:query | 查询资产详情 |
| 新增 | POST | /api/biz/asset | biz:asset:add | 新增资产 |
| 更新 | PUT | /api/biz/asset/{id} | biz:asset:edit | 更新资产 |
| 删除 | DELETE | /api/biz/asset/{ids} | biz:asset:remove | 批量删除资产 |

### 领用归还接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 领用 | POST | /api/biz/asset/{id}/borrow | biz:asset:borrow | 领用资产 |
| 归还 | POST | /api/biz/asset/{id}/return | biz:asset:return | 归还资产 |

### 领用记录接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 分页查询 | GET | /api/biz/asset/loan/page | biz:asset:list | 分页查询领用记录 |
| 列表查询 | GET | /api/biz/asset/loan/list | biz:asset:list | 查询领用记录列表 |
| 详情查询 | GET | /api/biz/asset/loan/{id} | biz:asset:query | 查询领用记录详情 |
| 按资产查询 | GET | /api/biz/asset/loan/asset/{assetId} | biz:asset:query | 查询某资产的领用记录 |
| 按用户查询 | GET | /api/biz/asset/loan/borrower/{borrowerId} | biz:asset:query | 查询某用户的领用记录 |
| 删除 | DELETE | /api/biz/asset/loan/{ids} | biz:asset:remove | 批量删除领用记录 |

---

## 五、状态说明

### 资产状态

| 状态值 | 状态名称 | 说明 |
|--------|----------|------|
| 0 | 维修中 | 资产正在维修，不可领用 |
| 1 | 正常 | 资产正常，可领用 |
| 2 | 报废 | 资产已报废，不可领用 |

### 领用状态

| 状态值 | 状态名称 | 说明 |
|--------|----------|------|
| 0 | 领用中 | 资产已领用，尚未归还 |
| 1 | 已归还 | 资产已全部归还 |
| 2 | 部分归还 | 资产部分归还，还有部分在使用中 |

---

## 六、常见问题

### Q1: 未登录异常: 未能读取到有效 token

**原因**: 
1. 没有先调用登录接口获取token
2. Token头名称错误（应该使用 `Authorization`，不是 `satoken`）
3. Token值已过期

**解决方法**:
1. 先执行登录接口: `POST /api/auth/login`
2. 从响应中获取 `data.token` 的值
3. 在后续请求中添加Header: `Authorization: {你的token值}`

### Q2: HttpRequestMethodNotSupportedException

**原因**: API路径缺少 `/api` 前缀

**解决方法**: 
- 错误: `http://localhost:8080/biz/asset`
- 正确: `http://localhost:8080/api/biz/asset`

### Q3: 权限不足

**原因**: 当前用户没有相应的权限

**解决方法**: 
1. 使用admin账号登录（admin角色拥有所有权限）
2. 或在系统中给用户分配相应的权限
