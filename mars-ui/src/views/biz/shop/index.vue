<template>
  <div class="page-container">
    <n-card>
      <!-- 搜索表单 -->
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="平台">
            <n-select
              v-model:value="searchForm.platform"
              placeholder="请选择平台"
              :options="platformOptions"
              clearable
              style="width: 120px"
            />
          </n-form-item>
          <n-form-item label="店铺名称">
            <n-input v-model:value="searchForm.shopName" placeholder="请输入店铺名称" clearable />
          </n-form-item>
          <n-form-item label="授权状态">
            <n-select
              v-model:value="searchForm.authStatus"
              placeholder="请选择状态"
              :options="authStatusOptions"
              clearable
              style="width: 100px"
            />
          </n-form-item>
          <n-form-item label="激活状态">
            <n-select
              v-model:value="searchForm.activationStatus"
              placeholder="请选择状态"
              :options="activationStatusOptions"
              clearable
              style="width: 100px"
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
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新增店铺
          </n-button>
        </n-space>
      </div>

      <!-- 表格 -->
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: Shop) => row.id"
        :scroll-x="1000"
      />

      <div class="pagination-container">
        <n-pagination
          v-model:page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :item-count="pagination.itemCount"
          :page-sizes="[10, 20, 50, 100]"
          show-size-picker
          show-quick-jumper
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        >
          <template #prefix>
            共 {{ pagination.itemCount }} 条
          </template>
        </n-pagination>
      </div>
    </n-card>

    <!-- 新增店铺弹窗 -->
    <ShopAddDialog
      v-model:show="showAddDialog"
      @success="loadData"
    />

    <!-- 授权弹窗 -->
    <ShopAuthorizeDialog
      v-model:show="showAuthorizeDialog"
      :shop="currentShop"
      @success="loadData"
    />

    <!-- 激活弹窗 -->
    <ShopActivateDialog
      v-model:show="showActivateDialog"
      :shop="currentShop"
      @success="loadData"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NButton, NSpace, NTag, NIcon, useMessage, useDialog, type DataTableColumns } from 'naive-ui'
import { SearchOutline, RefreshOutline, AddOutline, TrashOutline, CreateOutline, LinkOutline, FlashOutline, CartOutline } from '@vicons/ionicons5'
import { shopApi, type Shop } from '@/api/shop'
import ShopAddDialog from './components/ShopAddDialog.vue'
import ShopAuthorizeDialog from './components/ShopAuthorizeDialog.vue'
import ShopActivateDialog from './components/ShopActivateDialog.vue'

const message = useMessage()
const dialog = useDialog()

// 搜索表单
const searchForm = reactive({
  platform: null as string | null,
  shopName: '',
  authStatus: null as number | null,
  activationStatus: null as number | null,
})

// 表格数据
const tableData = ref<Shop[]>([])
const loading = ref(false)
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
})

// 弹窗状态
const showAddDialog = ref(false)
const showAuthorizeDialog = ref(false)
const showActivateDialog = ref(false)
const currentShop = ref<Shop | null>(null)

// 平台选项
const platformOptions = [
  { label: '拼多多', value: 'PINDUODUO' },
  { label: '抖音', value: 'DOUYIN' },
  { label: '小红书', value: 'XIAOHONGSHU' },
]

// 授权状态选项
const authStatusOptions = [
  { label: '未授权', value: 0 },
  { label: '已授权', value: 1 },
]

// 激活状态选项
const activationStatusOptions = [
  { label: '未激活', value: 0 },
  { label: '已激活', value: 1 },
]

// 平台颜色映射
const platformColorMap: Record<string, string> = {
  PINDUODUO: 'warning',
  DOUYIN: 'default',
  XIAOHONGSHU: 'error',
}

// 表格列
const columns: DataTableColumns<Shop> = [
  {
    title: '平台',
    key: 'platform',
    width: 100,
    render(row) {
      return h(NTag, { type: platformColorMap[row.platform] || 'default', size: 'small' }, {
        default: () => row.platformName || row.platform
      })
    }
  },
  {
    title: '店铺名称',
    key: 'shopName',
    width: 150,
    render(row) {
      return h('span', { style: { fontWeight: 'bold' } }, row.shopName || '-')
    }
  },
  {
    title: '店铺ID',
    key: 'shopIdExternal',
    width: 120,
    render(row) {
      const displayId = row.shopIdExternal || (row.id ? String(row.id).slice(0, 8) : '-')
      return h('code', { style: { fontSize: '12px', color: '#666' } }, displayId)
    }
  },
  {
    title: '授权状态',
    key: 'authStatus',
    width: 100,
    render(row) {
      const type = row.authStatus === 1 ? 'success' : 'error'
      return h(NTag, { type, size: 'small' }, {
        default: () => row.authStatusName || (row.authStatus === 1 ? '已授权' : '未授权')
      })
    }
  },
  {
    title: '激活状态',
    key: 'activationStatus',
    width: 100,
    render(row) {
      const type = row.activationStatus === 1 ? 'info' : 'default'
      return h(NTag, { type, size: 'small' }, {
        default: () => row.activationStatusName || (row.activationStatus === 1 ? '已激活' : '未激活')
      })
    }
  },
  {
    title: '剩余时长',
    key: 'remainingDays',
    width: 100,
    render(row) {
      if (row.activationStatus !== 1) return '-'
      return h('span', {}, `${row.remainingDays || 0}天`)
    }
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 160,
    render(row) {
      return row.createTime ? row.createTime.slice(0, 16) : '-'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render(row) {
      const buttons: any[] = []

      // 已授权且已激活 - 显示商品按钮
      if (row.authStatus === 1 && row.activationStatus === 1) {
        buttons.push(
          h(NButton, { size: 'small', quaternary: true, onClick: () => handleProducts(row) }, {
            default: () => [h(NIcon, null, { default: () => h(CartOutline) }), ' 商品']
          })
        )
      }

      // 未授权 - 显示授权按钮
      if (row.authStatus !== 1) {
        buttons.push(
          h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => handleAuthorize(row) }, {
            default: () => [h(NIcon, null, { default: () => h(LinkOutline) }), ' 授权']
          })
        )
      }

      // 未激活且已授权 - 显示激活按钮
      if (row.authStatus === 1 && row.activationStatus !== 1) {
        buttons.push(
          h(NButton, { size: 'small', quaternary: true, type: 'warning', onClick: () => handleActivate(row) }, {
            default: () => [h(NIcon, null, { default: () => h(FlashOutline) }), ' 激活']
          })
        )
      }

      // 删除按钮
      buttons.push(
        h(NButton, { size: 'small', quaternary: true, type: 'error', onClick: () => handleDelete(row) }, {
          default: () => [h(NIcon, null, { default: () => h(TrashOutline) }), ' 删除']
        })
      )

      return h('div', { style: { display: 'flex', alignItems: 'center', gap: '4px', flexWrap: 'wrap' } }, buttons)
    }
  }
]

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await shopApi.page({
      page: pagination.page,
      pageSize: pagination.pageSize,
      platform: searchForm.platform || undefined,
      shopName: searchForm.shopName || undefined,
      authStatus: searchForm.authStatus ?? undefined,
      activationStatus: searchForm.activationStatus ?? undefined,
    })
    tableData.value = res.list
    pagination.itemCount = res.total
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  loadData()
}

// 重置
function handleReset() {
  searchForm.platform = null
  searchForm.shopName = ''
  searchForm.authStatus = null
  searchForm.activationStatus = null
  handleSearch()
}

// 分页
function handlePageChange(page: number) {
  pagination.page = page
  loadData()
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

// 新增
function handleAdd() {
  showAddDialog.value = true
}

// 授权
function handleAuthorize(row: Shop) {
  currentShop.value = row
  showAuthorizeDialog.value = true
}

// 激活
function handleActivate(row: Shop) {
  currentShop.value = row
  showActivateDialog.value = true
}

// 商品管理
function handleProducts(row: Shop) {
  message.info('商品管理功能开发中...')
}

// 删除
function handleDelete(row: Shop) {
  dialog.warning({
    title: '提示',
    content: '确定要删除该店铺吗？删除后数据无法恢复。',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await shopApi.delete([row.id])
        message.success('删除成功')
        loadData()
      } catch (error) {
        // 错误已在拦截器处理
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}

.table-toolbar {
  margin-bottom: 16px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>