<template>
  <n-modal
    v-model:show="visible"
    preset="card"
    :title="isEdit ? '编辑运费模板' : '新增运费模板'"
    style="width: 600px"
    :mask-closable="false"
  >
    <n-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-placement="left"
      label-width="100"
    >
      <n-form-item label="模板名称" path="templateName">
        <n-input v-model:value="formData.templateName" placeholder="请输入模板名称" />
      </n-form-item>

      <n-form-item label="发货省份" path="productProvince">
        <n-input-number v-model:value="formData.productProvince" placeholder="请输入发货省份ID" clearable />
      </n-form-item>

      <n-form-item label="发货省份名称" path="productProvinceName">
        <n-input v-model:value="formData.productProvinceName" placeholder="请输入发货省份名称" />
      </n-form-item>

      <n-form-item label="发货城市" path="productCity">
        <n-input-number v-model:value="formData.productCity" placeholder="请输入发货城市ID" clearable />
      </n-form-item>

      <n-form-item label="发货城市名称" path="productCityName">
        <n-input v-model:value="formData.productCityName" placeholder="请输入发货城市名称" />
      </n-form-item>

      <n-form-item label="计费类型" path="calculateType">
        <n-select
          v-model:value="formData.calculateType"
          :options="calculateTypeOptions"
          placeholder="请选择计费类型"
        />
      </n-form-item>

      <n-form-item label="运送类型" path="transferType">
        <n-select
          v-model:value="formData.transferType"
          :options="transferTypeOptions"
          placeholder="请选择运送类型"
        />
      </n-form-item>

      <n-form-item label="计费规则" path="ruleType">
        <n-select
          v-model:value="formData.ruleType"
          :options="ruleTypeOptions"
          placeholder="请选择计费规则"
        />
      </n-form-item>

      <n-form-item label="固定运费(分)" path="fixedAmount">
        <n-input-number
          v-model:value="formData.fixedAmount"
          placeholder="请输入固定运费（单位：分）"
          clearable
          :min="0"
        />
      </n-form-item>

      <n-form-item label="计费规则JSON" path="columns">
        <n-input
          v-model:value="formData.columns"
          type="textarea"
          placeholder="请输入计费规则JSON"
          :rows="5"
        />
      </n-form-item>

      <n-form-item label="状态" path="status">
        <n-select
          v-model:value="formData.status"
          :options="statusOptions"
          placeholder="请选择状态"
        />
      </n-form-item>
    </n-form>

    <template #action>
      <n-space>
        <n-button @click="handleCancel">取消</n-button>
        <n-button type="primary" @click="handleSave" :loading="saving">保存</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useMessage, type FormInst, type FormRules } from 'naive-ui'
import { douyinFreightApi, type DouyinFreightTemplate, type DouyinFreightTemplateSaveReq } from '@/api/douyin-freight'

const props = defineProps<{
  show: boolean
  template: DouyinFreightTemplate | null
  ownerId: string
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'success'): void
}>()

const message = useMessage()
const formRef = ref<FormInst | null>(null)
const saving = ref(false)

const visible = computed({
  get: () => props.show,
  set: (value) => emit('update:show', value)
})

const isEdit = computed(() => props.template !== null)

// 表单数据
const formData = reactive<DouyinFreightTemplateSaveReq>({
  id: undefined,
  ownerId: '',
  freightId: null,
  templateName: '',
  productProvince: null,
  productProvinceName: '',
  productCity: null,
  productCityName: '',
  calculateType: 1,
  transferType: 1,
  ruleType: 1,
  fixedAmount: null,
  columns: '',
  status: 1,
})

// 表单验证规则
const rules: FormRules = {
  templateName: {
    required: true,
    message: '请输入模板名称',
    trigger: ['blur', 'input']
  },
  calculateType: {
    required: true,
    type: 'number',
    message: '请选择计费类型',
    trigger: ['blur', 'change']
  },
  transferType: {
    required: true,
    type: 'number',
    message: '请选择运送类型',
    trigger: ['blur', 'change']
  },
  ruleType: {
    required: true,
    type: 'number',
    message: '请选择计费规则',
    trigger: ['blur', 'change']
  },
}

// 选项配置
const calculateTypeOptions = [
  { label: '按重量', value: 1 },
  { label: '按件数', value: 2 },
]

const transferTypeOptions = [
  { label: '快递', value: 1 },
  { label: 'EMS', value: 2 },
  { label: '平邮', value: 3 },
]

const ruleTypeOptions = [
  { label: '自定义', value: 1 },
  { label: '卖家承担运费', value: 2 },
]

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

// 监听模板变化，初始化表单数据
watch(
  () => props.template,
  (template) => {
    if (template) {
      formData.id = template.id
      formData.ownerId = template.ownerId
      formData.freightId = template.freightId
      formData.templateName = template.templateName
      formData.productProvince = template.productProvince
      formData.productProvinceName = template.productProvinceName || ''
      formData.productCity = template.productCity
      formData.productCityName = template.productCityName || ''
      formData.calculateType = template.calculateType
      formData.transferType = template.transferType
      formData.ruleType = template.ruleType
      formData.fixedAmount = template.fixedAmount
      formData.columns = template.columns || ''
      formData.status = template.status
    } else {
      resetForm()
    }
  },
  { immediate: true }
)

// 监听 ownerId
watch(
  () => props.ownerId,
  (ownerId) => {
    if (ownerId && !formData.ownerId) {
      formData.ownerId = ownerId
    }
  }
)

// 重置表单
function resetForm() {
  formData.id = undefined
  formData.ownerId = props.ownerId || ''
  formData.freightId = null
  formData.templateName = ''
  formData.productProvince = null
  formData.productProvinceName = ''
  formData.productCity = null
  formData.productCityName = ''
  formData.calculateType = 1
  formData.transferType = 1
  formData.ruleType = 1
  formData.fixedAmount = null
  formData.columns = ''
  formData.status = 1
}

// 取消
function handleCancel() {
  visible.value = false
  resetForm()
}

// 保存
async function handleSave() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  saving.value = true
  try {
    await douyinFreightApi.save(formData)
    message.success(isEdit.value ? '更新成功' : '新增成功')
    visible.value = false
    emit('success')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    saving.value = false
  }
}
</script>