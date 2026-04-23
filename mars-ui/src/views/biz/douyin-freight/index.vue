<template>
  <div class="page-container">
    <n-card>
      <!-- 搜索表单 -->
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="模板名称">
            <n-input v-model:value="searchForm.templateName" placeholder="请输入模板名称" clearable />
          </n-form-item>
          <n-form-item label="状态">
            <n-select
              v-model:value="searchForm.status"
              placeholder="请选择状态"
              :options="statusOptions"
              clearable
              style="width: 100px"
            />
          </n-form-item>
          <n-form-item label="同步状态">
            <n-select
              v-model:value="searchForm.syncStatus"
              placeholder="请选择同步状态"
              :options="syncStatusOptions"
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
          <n-button type="primary" @click="handleAdd">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新增模板
          </n-button>
          <n-button type="info" @click="handleSync">
            <template #icon><n-icon><CloudDownloadOutline /></n-icon></template>
            从抖音同步
          </n-button>
        </n-space>
      </div>

      <!-- 表格 -->
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: DouyinFreightTemplate) => row.id"
        :scroll-x="1200"
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

    <!-- 新增/编辑弹窗 -->
    <FreightTemplateAddEditDialog
      v-model:show="showEditDialog"
      :template="currentTemplate"
      :owner-id="searchForm.ownerId"
      @success="loadData"
    />

    <!-- 同步弹窗 -->
    <n-modal v-model:show="showSyncDialog" preset="dialog" title="从抖音同步运费模板">
      <n-form :model="syncForm">
        <n-form-item label="店铺所有者ID" required>
          <n-input v-model:value="syncForm.ownerId" placeholder="请输入店铺所有者ID" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space>
          <n-button @click="showSyncDialog = false">取消</n-button>
          <n-button type="primary" @click="handleSyncConfirm" :loading="syncLoading">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted } from 'vue'
import { NButton, NSpace, NTag, NIcon, useMessage, useDialog, type DataTableColumns } from 'naive-ui'
import {
  SearchOutline,
  RefreshOutline,
  AddOutline,
  TrashOutline,
  CreateOutline,
  CloudDownloadOutline,
  CloudUploadOutline,
  CheckmarkCircleOutline,
  CloseCircleOutline
} from '@vicons/ionicons5'
import { douyinFreightApi, type DouyinFreightTemplate } from '@/api/douyin-freight'
import FreightTemplateAddEditDialog from './components/FreightTemplateAddEditDialog.vue'

const message = useMessage()
const dialog = useDialog()

// 搜索表单
const searchForm = reactive({
  ownerId: '',
  templateName: '',
  status: null as number | null,
  syncStatus: null as number | null,
})

// 表格数据
const tableData = ref<DouyinFreightTemplate[]>([])
const loading = ref(false)
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
})

// 弹窗状态
const showEditDialog = ref(false)
const showSyncDialog = ref(false)
const syncLoading = ref(false)
const currentTemplate = ref<DouyinFreightTemplate | null>(null)
const syncForm = reactive({ ownerId: '' })

// 状态选项
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

const syncStatusOptions = [
  { label: '未同步', value: 0 },
  { label: '已同步', value: 1 },
  { label: '同步失败', value: 2 },
]

// 计费类型颜色映射
const calculateTypeColorMap: Record<number, string> = {
  1: 'info',
  2: 'warning',
}

// 运送类型颜色映射
const transferTypeColorMap: Record<number, string> = {
  1: 'success',
  2: 'warning',
  3: 'default',
}

// 表格列
const columns: DataTableColumns<DouyinFreightTemplate> = [
  {
    title: 'ID',
    key: 'id',
    width: 80,
  },
  {
    title: '模板名称',
    key: 'templateName',
    width: 150,
    render(row) {
      return h('span', { style: { fontWeight: 'bold' } }, row.templateName)
    }
  },
  {
    title: '发货地',
    key: 'location',
    width: 120,
    render(row) {
      const location = row.productProvinceName || row.productCityName
      return location || '-'
    }
  },
  {
    title: '计费类型',
    key: 'calculateType',
    width: 100,
    render(row) {
      return h(NTag, { type: calculateTypeColorMap[row.calculateType] || 'default', size: 'small' }, {
        default: () => row.calculateTypeName || '未知'
      })
    }
  },
  {
    title: '运送类型',
    key: 'transferType',
    width: 100,
    render(row) {
      return h(NTag, { type: transferTypeColorMap[row.transferType] || 'default', size: 'small' }, {
        default: () => row.transferTypeName || '未知'
      })
    }
  },
  {
    title: '计费规则',
    key: 'ruleType',
    width: 100,
    render(row) {
      const type = row.ruleType === 2 ? 'success' : 'default'
      return h(NTag, { type, size: 'small' }, {
        default: () => row.ruleTypeName || '未知'
      })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render(row) {
      const type = row.status === 1 ? 'success' : 'error'
      return h(NTag, { type, size: 'small' }, {
        default: () => row.statusName
      })
    }
  },
  {
    title: '同步状态',
    key: 'syncStatus',
    width: 100,
    render(row) {
      const type = row.syncStatus === 1 ? 'success' : (row.syncStatus === 2 ? 'error' : 'default')
      return h(NTag, { type, size: 'small' }, {
        default: () => row.syncStatusName
      })
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

      // 编辑按钮
      buttons.push(
        h(NButton, { size: 'small', quaternary: true, onClick: () => handleEdit(row) }, {
          default: () => [h(NIcon, null, { default: () => h(CreateOutline) }), ' 编辑']
        })
      )

      // 启用/禁用按钮
      if (row.status === 1) {
        buttons.push(
          h(NButton, { size: 'small', quaternary: true, type: 'warning', onClick: () => handleDisable(row) }, {
            default: () => [h(NIcon, null, { default: () => h(CloseCircleOutline) }), ' 禁用']
          })
        )
      } else {
        buttons.push(
          h(NButton, { size: 'small', quaternary: true, type: 'success', onClick: () => handleEnable(row) }, {
            default: () => [h(NIcon, null, { default: () => h(CheckmarkCircleOutline) }), ' 启用']
          })
        )
      }

      // 推送按钮
      if (row.syncStatus !== 1) {
        buttons.push(
          h(NButton, { size: 'small', quaternary: true, type: 'info', onClick: () => handlePush(row) }, {
            default: () => [h(NIcon, null, { default: () => h(CloudUploadOutline) }), ' 推送']
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
    const res = await douyinFreightApi.page({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ownerId: searchForm.ownerId || undefined,
      templateName: searchForm.templateName || undefined,
      status: searchForm.status ?? undefined,
      syncStatus: searchForm.syncStatus ?? undefined,
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
  searchForm.ownerId = ''
  searchForm.templateName = ''
  searchForm.status = null
  searchForm.syncStatus = null
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
  currentTemplate.value = null
  showEditDialog.value = true
}

// 编辑
function handleEdit(row: DouyinFreightTemplate) {
  currentTemplate.value = row
  showEditDialog.value = true
}

// 启用
async function handleEnable(row: DouyinFreightTemplate) {
  try {
    await douyinFreightApi.enable(row.id)
    message.success('启用成功')
    loadData()
  } catch (error) {
    // 错误已在拦截器处理
  }
}

// 禁用
async function handleDisable(row: DouyinFreightTemplate) {
  try {
    await douyinFreightApi.disable(row.id)
    message.success('禁用成功')
    loadData()
  } catch (error) {
    // 错误已在拦截器处理
  }
}

// 推送到抖音
function handlePush(row: DouyinFreightTemplate) {
  dialog.warning({
    title: '推送确认',
    content: '确定要将该运费模板推送到抖音平台吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await douyinFreightApi.push(row.id)
        message.success('推送成功')
        loadData()
      } catch (error) {
        // 错误已在拦截器处理
      }
    }
  })
}

// 删除
function handleDelete(row: DouyinFreightTemplate) {
  dialog.warning({
    title: '删除确认',
    content: '确定要删除该运费模板吗？删除后数据无法恢复。',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await douyinFreightApi.delete([row.id])
        message.success('删除成功')
        loadData()
      } catch (error) {
        // 错误已在拦截器处理
      }
    }
  })
}

// 同步
function handleSync() {
  syncForm.ownerId = searchForm.ownerId || ''
  showSyncDialog.value = true
}

// 同步确认
async function handleSyncConfirm() {
  if (!syncForm.ownerId) {
    message.warning('请输入店铺所有者ID')
    return
  }
  syncLoading.value = true
  try {
    await douyinFreightApi.sync({ ownerId: syncForm.ownerId })
    message.success('同步成功')
    showSyncDialog.value = false
    searchForm.ownerId = syncForm.ownerId
    loadData()
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    syncLoading.value = false
  }
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