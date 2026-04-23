<template>
  <n-modal v-model:show="visible" preset="card" title="新增激活码" style="width: 500px">
    <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="100px">
      <n-form-item label="激活码" path="activationCode">
        <n-input
          v-model:value="formData.activationCode"
          placeholder="不填写则自动生成"
          maxlength="100"
        />
      </n-form-item>
      <n-form-item label="类型" path="durationType">
        <n-select
          v-model:value="formData.durationType"
          placeholder="请选择类型"
          :options="durationTypeOptions"
          @update:value="handleTypeChange"
        />
      </n-form-item>
      <n-form-item label="天数" path="durationDays">
        <n-input-number
          v-model:value="formData.durationDays"
          placeholder="根据类型自动填充"
          :min="1"
          style="width: 100%"
          @update:value="() => formRef?.validate()"
        />
      </n-form-item>
    </n-form>

    <n-alert type="info" style="margin-top: 16px">
      <template #header>说明</template>
      <ul style="margin: 0; padding-left: 16px; line-height: 1.8">
        <li>激活码类型：日、月、季、年</li>
        <li>天数：按实际的天数计算到期时间</li>
        <li>激活码不填写则自动生成 8 位随机码</li>
      </ul>
    </n-alert>

    <template #footer>
      <n-space justify="end">
        <n-button @click="handleClose">取消</n-button>
        <n-button type="primary" :loading="submitting" @click="handleSubmit">确定</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { activationCodeApi, type ActivationCode } from '@/api/activation-code'

const props = defineProps<{
  show: boolean
  editData: ActivationCode | null
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'success'): void
}>()

const message = useMessage()
const formRef = ref()
const submitting = ref(false)

// 控制显示
const visible = ref(props.show)
watch(() => props.show, (val) => visible.value = val)
watch(visible, (val) => emit('update:show', val))
watch(() => props.editData, (val) => {
  if (val) {
    formData.value.activationCode = val.activationCode || ''
    formData.value.durationType = val.durationType || 'DAY'
    formData.value.durationDays = val.durationDays || 1
  } else {
    // 重置表单时重新初始化
    formData.value.activationCode = ''
    formData.value.durationType = 'DAY'
    formData.value.durationDays = 1
  }
})

// 类型选项
const durationTypeOptions = [
  { label: '日', value: 'DAY' },
  { label: '月', value: 'MONTH' },
  { label: '季', value: 'QUARTER' },
  { label: '年', value: 'YEAR' }
]

// 类型对应的默认天数
const defaultDaysMap: Record<string, number> = {
  DAY: 1,
  MONTH: 30,
  QUARTER: 90,
  YEAR: 365
}

// 表单数据
const formData = ref({
  activationCode: '',
  durationType: 'DAY',
  durationDays: 1,
})

// 表单校验规则
const formRules = {
  durationType: { required: true, message: '请选择类型', trigger: 'change' },
  durationDays: [
    {
      required: true,
      type: 'number',
      message: '请输入天数',
      trigger: 'input'
    },
    {
      validator: (_rule: any, value: number) => {
        if (value === null || value === undefined || value < 1) {
          return new Error('天数必须大于 0')
        }
        return Promise.resolve()
      },
      trigger: 'input'
    }
  ],
}

// 类型变更时自动填充天数
function handleTypeChange(value: string) {
  formData.value.durationDays = defaultDaysMap[value] || 1
}

// 关闭
function handleClose() {
  visible.value = false
  formData.value.activationCode = ''
  formData.value.durationType = 'DAY'
  formData.value.durationDays = 1
  formRef.value?.restoreValidation()
}

// 提交
async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (props.editData) {
      // 编辑
      await activationCodeApi.update(props.editData.id, {
        durationType: formData.value.durationType,
        durationDays: formData.value.durationDays,
      })
      message.success('更新成功')
    } else {
      // 新增
      await activationCodeApi.create({
        activationCode: formData.value.activationCode || undefined,
        durationType: formData.value.durationType,
        durationDays: formData.value.durationDays,
      })
      message.success('新增成功')
    }
    handleClose()
    emit('success')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>
