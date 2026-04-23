<template>
  <div class="page-container">
    <n-card>
      <!-- 搜索表单 -->
      <div class="search-form">
        <n-form inline :model="searchForm" label-placement="left">
          <n-form-item label="激活码">
            <n-input v-model:value="searchForm.activationCode" placeholder="请输入激活码" clearable />
          </n-form-item>
          <n-form-item label="类型">
            <n-select
              v-model:value="searchForm.durationType"
              placeholder="请选择类型"
              :options="durationTypeOptions"
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
            新增激活码
          </n-button>
          <n-button @click="importModalVisible = true">
            <template #icon><n-icon><CloudUploadOutline /></n-icon></template>
            导入
          </n-button>
          <n-button type="error" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
            <template #icon><n-icon><TrashOutline /></n-icon></template>
            批量删除
          </n-button>
        </n-space>
      </div>

      <!-- 表格 -->
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :row-key="(row: ActivationCode) => row.id"
        :scroll-x="1000"
        @update:checked-row-keys="handleSelectionChange"
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
    <ActivationCodeDialog
      v-model:show="showDialog"
      :edit-data="editData"
      @success="loadData"
    />

    <!-- 激活弹窗 -->
    <ActivationDialog
      v-model:show="showActivationDialog"
      @success="loadData"
    />

    <!-- 导入弹窗 -->
    <n-modal
      v-model:show="importModalVisible"
      title="导入激活码"
      preset="card"
      style="width: 500px"
      :mask-closable="false"
    >
      <n-space vertical>
        <n-alert type="info" :show-icon="true">
          <template #header>导入说明</template>
          <ul style="margin: 0; padding-left: 16px; line-height: 1.8">
            <li>Excel 格式：第一行为表头，包含"激活码"、"类型"、"天数"三列</li>
            <li>激活码：可选，为空则自动生成16位随机码</li>
            <li>类型：可选，支持"日/月/季/年"或"DAY/MONTH/QUARTER/YEAR"</li>
            <li>天数：必填，如 1、30、90、365 等</li>
            <li>激活状态：导入后默认为"未激活"</li>
          </ul>
        </n-alert>
        <n-upload
          :max="1"
          accept=".xlsx,.xls"
          :show-file-list="true"
          :custom-request="handleImportUpload"
        >
          <n-upload-dragger>
            <div style="margin-bottom: 12px">
              <n-icon size="48" :depth="3">
                <CloudUploadOutline />
              </n-icon>
            </div>
            <n-text style="font-size: 16px">点击或拖拽文件到此处上传</n-text>
            <n-p depth="3" style="margin: 8px 0 0 0">支持 .xlsx 或 .xls 格式</n-p>
          </n-upload-dragger>
        </n-upload>
      </n-space>
      <template #footer>
        <n-space justify="end">
          <n-button @click="importModalVisible = false">关闭</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, computed } from 'vue'
import { useMessage, useDialog, NTag, NButton, NInput, type UploadCustomRequestOptions } from 'naive-ui'
import {
  SearchOutline,
  RefreshOutline,
  AddOutline,
  TrashOutline,
  CreateOutline,
  CheckmarkOutline,
  CloudUploadOutline
} from '@vicons/ionicons5'
import { activationCodeApi, type ActivationCode, type ActivationCodeQueryReq } from '@/api/activation-code'
import ActivationCodeDialog from './components/ActivationCodeDialog.vue'
import ActivationDialog from './components/ActivationDialog.vue'

const message = useMessage()
const dialog = useDialog()

// 搜索表单
const searchForm = reactive<ActivationCodeQueryReq>({
  activationCode: '',
  durationType: '',
  activationStatus: null
})

// 类型选项
const durationTypeOptions = [
  { label: '日', value: 'DAY' },
  { label: '月', value: 'MONTH' },
  { label: '季', value: 'QUARTER' },
  { label: '年', value: 'YEAR' }
]

// 激活状态选项
const activationStatusOptions = [
  { label: '未激活', value: 0 },
  { label: '已激活', value: 1 }
]

// 表格列定义
const columns = [
  { type: 'selection' },
  { title: 'ID', key: 'id', width: 80 },
  {
    title: '激活码',
    key: 'activationCode',
    width: 150,
    ellipsis: { tooltip: true }
  },
  {
    title: '类型',
    key: 'durationType',
    width: 80,
    render: (row: ActivationCode) => {
      const typeMap: Record<string, string> = {
        DAY: 'warning',
        MONTH: 'info',
        QUARTER: 'success',
        YEAR: 'error'
      }
      return h(NTag, { type: typeMap[row.durationType] || 'default' }, { default: () => row.durationTypeName })
    }
  },
  { title: '天数', key: 'durationDays', width: 80 },
  {
    title: '激活状态',
    key: 'activationStatus',
    width: 100,
    render: (row: ActivationCode) => {
      const type = row.activationStatus === 1 ? 'success' : 'default'
      return h(NTag, { type }, { default: () => row.activationStatusName })
    }
  },
  { title: '激活用户 ID', key: 'userId', width: 100, ellipsis: { tooltip: true } },
  { title: '激活店铺 ID', key: 'shopId', width: 100, ellipsis: { tooltip: true } },
  { title: '激活时间', key: 'activatedAt', width: 180, ellipsis: { tooltip: true } },
  { title: '创建时间', key: 'createTime', width: 180, ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render: (row: ActivationCode) => {
      // 已激活的数据不显示编辑和删除按钮
      if (row.activationStatus === 1) {
        return h('div', { style: { color: '#999', fontSize: '12px' } }, '已激活，无法编辑或删除')
      }
      return h('div', { style: { display: 'flex', gap: '8px' } }, [
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            ghost: true,
            onClick: () => handleEdit(row)
          },
          { default: () => '编辑', icon: () => h(CreateOutline) }
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'success',
            ghost: true,
            onClick: () => handleActivate(row)
          },
          { default: () => '激活', icon: () => h(CheckmarkOutline) }
        ),
        h(
          NButton,
          {
            size: 'small',
            type: 'error',
            ghost: true,
            onClick: () => handleDelete(row)
          },
          { default: () => '删除' }
        )
      ])
    }
  }
]

// 表格数据
const tableData = ref<ActivationCode[]>([])
const loading = ref(false)
const selectedIds = ref<number[]>([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

// 弹窗控制
const showDialog = ref(false)
const showActivationDialog = ref(false)
const importModalVisible = ref(false)
const editData = ref<ActivationCode | null>(null)

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await activationCodeApi.page({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = res.list
    pagination.itemCount = res.total
  } catch (error) {
    // 错误已在拦截器处理
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
  searchForm.activationCode = ''
  searchForm.durationType = ''
  searchForm.activationStatus = null
  handleSearch()
}

// 新增
function handleAdd() {
  editData.value = null
  showDialog.value = true
}

// 编辑
function handleEdit(row: ActivationCode) {
  editData.value = { ...row }
  showDialog.value = true
}

// 删除
function handleDelete(row: ActivationCode) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除激活码 "${row.activationCode}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await activationCodeApi.delete([row.id])
      message.success('删除成功')
      loadData()
    }
  })
}

// 批量删除
function handleBatchDelete() {
  // 过滤掉已激活的数据
  const activableIds = selectedIds.value.filter(id => {
    const row = tableData.value.find(item => item.id === id)
    return row && row.activationStatus === 0
  })

  if (activableIds.length === 0) {
    message.warning('选中的数据中包含已激活的激活码，无法删除')
    return
  }

  dialog.warning({
    title: '批量删除',
    content: `确定要删除选中的 ${activableIds.length} 条记录吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      await activationCodeApi.delete(activableIds)
      message.success('删除成功')
      selectedIds.value = []
      loadData()
    }
  })
}

// 激活
function handleActivate(row: ActivationCode) {
  editData.value = row
  showActivationDialog.value = true
}

// 选择变更
function handleSelectionChange(rowKeys: (string | number)[]) {
  selectedIds.value = rowKeys as number[]
}

// 分页变更
function handlePageChange(page: number) {
  pagination.page = page
  loadData()
}

function handlePageSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadData()
}

// 导入上传
async function handleImportUpload({ file }: UploadCustomRequestOptions) {
  if (!file.file) return
  try {
    const result = await activationCodeApi.importCodes(file.file)
    if (result.failCount > 0) {
      dialog.warning({
        title: '导入结果',
        content: `成功: ${result.successCount} 条，失败: ${result.failCount} 条\n错误信息: ${result.errors?.join('\n') || '无'}`,
        positiveText: '确定'
      })
    } else {
      message.success(`导入成功，共 ${result.successCount} 条数据`)
      importModalVisible.value = false
    }
    loadData()
  } catch (error) {
    // 错误已在拦截器处理
  }
}

// 初始化加载
loadData()
</script>

<style scoped>
/* 继承全局样式 */
</style>
