<template>
  <div class="page-container">
    <n-card>
      <!-- 搜索表单 -->
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="店铺">
            <n-select
              v-model:value="searchForm.ownerId"
              placeholder="请选择店铺"
              :options="shopOptions"
              :loading="shopLoading"
              clearable
              filterable
              style="width: 220px"
            />
          </n-form-item>
          <n-form-item label="商品名称">
            <n-input v-model:value="searchForm.name" placeholder="请输入商品名称" clearable />
          </n-form-item>
          <n-form-item label="商品状态">
            <n-select
              v-model:value="searchForm.status"
              placeholder="请选择状态"
              :options="statusOptions"
              clearable
              style="width: 120px"
            />
          </n-form-item>
          <n-form-item label="审核状态">
            <n-select
              v-model:value="searchForm.checkStatus"
              placeholder="请选择审核状态"
              :options="checkStatusOptions"
              clearable
              style="width: 120px"
            />
          </n-form-item>
          <n-form-item label="商品类型">
            <n-select
              v-model:value="searchForm.productType"
              placeholder="请选择商品类型"
              :options="productTypeOptions"
              clearable
              style="width: 120px"
            />
          </n-form-item>
          <n-form-item>
            <n-space>
              <n-button type="primary" @click="handleSearch">
                <template #icon><n-icon><SearchOutline /></n-icon></template>
                搜索
              </n-button>
              <n-button @click="handleReset">
                <template #icon><n-icon><RefreshOutline /></n-icon></template>
                重置
              </n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </div>

      <!-- 工具栏 -->
      <div class="table-toolbar">
        <n-space>
          <n-button type="primary" @click="handleRefresh">
            <template #icon><n-icon><RefreshOutline /></n-icon></template>
            刷新数据
          </n-button>
        </n-space>
      </div>

      <!-- 表格 -->
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: DouyinProductV2) => row.productId"
        :scroll-x="1400"
      />

      <div class="pagination-container">
        <n-pagination
          v-model:page="pagination.page"
          v-model:page-size="pagination.size"
          :item-count="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          show-size-picker
          show-quick-jumper
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        >
          <template #prefix>
            共 {{ pagination.total }} 条
          </template>
        </n-pagination>
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h, computed } from 'vue'
import { NButton, NSpace, NTag, NIcon, NImage, useMessage, type DataTableColumns } from 'naive-ui'
import { SearchOutline, RefreshOutline, EyeOutline } from '@vicons/ionicons5'
import { douyinProductApi, type DouyinProductV2 } from '@/api/douyin-product'
import { shopApi, type Shop } from '@/api/shop'

const message = useMessage()

// 搜索表单
const searchForm = reactive({
  ownerId: '',
  name: '',
  status: null as number | null,
  checkStatus: null as number | null,
  productType: null as number | null,
})

// 店铺列表
const shopList = ref<Shop[]>([])
const shopLoading = ref(false)

// 店铺下拉选项
const shopOptions = computed(() => {
  return shopList.value.map(shop => ({
    label: shop.shopName,
    value: shop.shopIdExternal || '',
  }))
})

// 表格数据
const tableData = ref<DouyinProductV2[]>([])
const loading = ref(false)
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

// 商品状态选项（抖音平台）
const statusOptions = [
  { label: '创建中', value: 0 },
  { label: '审核中', value: 1 },
  { label: '审核通过', value: 2 },
  { label: '审核驳回', value: 3 },
  { label: '已上架', value: 4 },
  { label: '已下架', value: 5 },
  { label: '已删除', value: 6 },
]

// 审核状态选项（抖音平台）
const checkStatusOptions = [
  { label: '未审核', value: 0 },
  { label: '审核中', value: 1 },
  { label: '审核通过', value: 2 },
  { label: '审核驳回', value: 3 },
]

// 商品类型选项
const productTypeOptions = [
  { label: '普通商品', value: 0 },
  { label: '电子面单商品', value: 1 },
]

// 商品状态映射
const statusMap: Record<number, { type: string; label: string }> = {
  0: { type: 'default', label: '创建中' },
  1: { type: 'warning', label: '审核中' },
  2: { type: 'info', label: '审核通过' },
  3: { type: 'error', label: '审核驳回' },
  4: { type: 'success', label: '已上架' },
  5: { type: 'default', label: '已下架' },
  6: { type: 'error', label: '已删除' },
}

// 审核状态映射
const checkStatusMap: Record<number, { type: string; label: string }> = {
  0: { type: 'default', label: '未审核' },
  1: { type: 'warning', label: '审核中' },
  2: { type: 'success', label: '审核通过' },
  3: { type: 'error', label: '审核驳回' },
}

// 商品类型映射
const productTypeMap: Record<number, string> = {
  0: '普通商品',
  1: '电子面单商品',
}

// 格式化时间戳
const formatTimestamp = (timestamp: number | null): string => {
  if (!timestamp) return '-'
  const date = new Date(timestamp * 1000)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 格式化价格（分转元）
const formatPrice = (price: number | null): string => {
  if (!price) return '-'
  return `¥${(price / 100).toFixed(2)}`
}

// 获取类目名称
const getCategoryName = (row: DouyinProductV2): string => {
  if (!row.categoryDetail) return '-'
  const parts = [
    row.categoryDetail.firstCname,
    row.categoryDetail.secondCname,
    row.categoryDetail.thirdCname,
    row.categoryDetail.fourthCname,
  ].filter(Boolean)
  return parts.length > 0 ? parts.join('/') : '-'
}

// 表格列定义
const columns: DataTableColumns<DouyinProductV2> = [
  {
    title: '商品ID',
    key: 'productId',
    width: 100,
    ellipsis: { tooltip: true },
  },
  {
    title: '商品图片',
    key: 'img',
    width: 80,
    render: (row) => {
      return h(NImage, {
        src: row.img || '',
        style: { width: '50px', height: '50px', objectFit: 'cover' },
        fallbackSrc: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI1MCIgaGVpZ2h0PSI1MCI+PHJlY3QgZmlsbD0iI2VlZSIgd2lkdGg9IjUwIiBoZWlnaHQ9IjUwIi8+PHRleHQgeD0iMjUiIHk9IjMwIiBmb250LXNpemU9IjEwIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjOTk5Ij7lhp7mloY8L3RleHQ+PC9zdmc+',
      })
    },
  },
  {
    title: '商品名称',
    key: 'name',
    minWidth: 180,
    ellipsis: { tooltip: true },
  },
  {
    title: '类目',
    key: 'categoryDetail',
    width: 150,
    ellipsis: { tooltip: true },
    render: (row) => getCategoryName(row),
  },
  {
    title: '市场价',
    key: 'marketPrice',
    width: 90,
    render: (row) => formatPrice(row.marketPrice),
  },
  {
    title: '折扣价',
    key: 'discountPrice',
    width: 90,
    render: (row) => formatPrice(row.discountPrice),
  },
  {
    title: '销量',
    key: 'sellNum',
    width: 70,
    render: (row) => row.sellNum?.toString() || '-',
  },
  {
    title: '商品状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const status = statusMap[row.status] || { type: 'default', label: '未知' }
      return h(NTag, { type: status.type, size: 'small' }, { default: () => status.label })
    },
  },
  {
    title: '审核状态',
    key: 'checkStatus',
    width: 100,
    render: (row) => {
      const checkStatus = checkStatusMap[row.checkStatus] || { type: 'default', label: '未知' }
      return h(NTag, { type: checkStatus.type, size: 'small' }, { default: () => checkStatus.label })
    },
  },
  {
    title: '商品类型',
    key: 'productType',
    width: 100,
    render: (row) => productTypeMap[row.productType] || '-',
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 150,
    render: (row) => formatTimestamp(row.createTime),
  },
  {
    title: '更新时间',
    key: 'updateTime',
    width: 150,
    render: (row) => formatTimestamp(row.updateTime),
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    fixed: 'right',
    render: (row) => {
      return h(NSpace, {}, {
        default: () => [
          h(NButton, {
            size: 'small',
            quaternary: true,
            onClick: () => handleViewDetail(row),
          }, {
            default: () => '详情',
            icon: () => h(NIcon, {}, { default: () => h(EyeOutline) })
          }),
        ],
      })
    },
  },
]

// 加载店铺列表（抖音已授权已激活的店铺）
const loadShopList = async () => {
  shopLoading.value = true
  try {
    const res = await shopApi.listByConditions({
      platform: 'DOUYIN',
      authStatus: 1,
      activationStatus: 1,
    })
    shopList.value = res || []
    // 如果只有一个店铺，自动选中
    if (shopList.value.length === 1) {
      searchForm.ownerId = shopList.value[0].shopIdExternal || ''
    }
  } catch (error) {
    message.error('加载店铺列表失败')
  } finally {
    shopLoading.value = false
  }
}

// 加载数据（从抖音平台查询）
const loadData = async () => {
  if (!searchForm.ownerId) {
    message.warning('请选择店铺')
    return
  }

  loading.value = true
  try {
    const res = await douyinProductApi.listProductV2({
      ownerId: searchForm.ownerId,
      name: searchForm.name || undefined,
      status: searchForm.status ?? undefined,
      checkStatus: searchForm.checkStatus ?? undefined,
      productType: searchForm.productType ?? undefined,
      page: pagination.page,
      size: pagination.size,
    })
    tableData.value = res.data || []
    pagination.total = res.total || 0
  } catch (error) {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.ownerId = ''
  searchForm.name = ''
  searchForm.status = null
  searchForm.checkStatus = null
  searchForm.productType = null
  pagination.page = 1
  tableData.value = []
  pagination.total = 0
}

// 刷新数据
const handleRefresh = () => {
  if (searchForm.ownerId) {
    loadData()
  } else {
    message.warning('请选择店铺')
  }
}

// 查看详情
const handleViewDetail = (row: DouyinProductV2) => {
  message.info(`商品ID: ${row.productId}, 名称: ${row.name}`)
}

// 分页
const handlePageChange = (page: number) => {
  pagination.page = page
  loadData()
}

const handlePageSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  loadData()
}

onMounted(() => {
  // 页面加载时先获取店铺列表
  loadShopList()
})
</script>

<style scoped>
.page-container {
  padding: 16px;
}

.search-form {
  margin-bottom: 16px;
}

.table-toolbar {
  margin-bottom: 16px;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>