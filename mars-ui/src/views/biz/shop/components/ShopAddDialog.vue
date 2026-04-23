<template>
  <n-modal v-model:show="visible" preset="card" title="新增店铺" style="width: 500px">
    <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="100px">
      <n-form-item label="平台" path="platform">
        <n-select
          v-model:value="formData.platform"
          placeholder="请选择平台"
          :options="platformOptions"
        />
      </n-form-item>
      <n-form-item label="店铺名称" path="shopName">
        <n-input v-model:value="formData.shopName" placeholder="请输入店铺名称" maxlength="100" show-count />
      </n-form-item>
      <n-form-item label="外部店铺ID" path="shopIdExternal">
        <n-input v-model:value="formData.shopIdExternal" placeholder="可选，授权后会自动获取" />
      </n-form-item>
      <n-form-item label="备注" path="remark">
        <n-input v-model:value="formData.remark" placeholder="请输入备注" type="textarea" :rows="3" maxlength="500" show-count />
      </n-form-item>
    </n-form>

    <n-alert type="info" style="margin-top: 16px">
      <template #header>说明</template>
      <ul style="margin: 0; padding-left: 16px; line-height: 1.8">
        <li>新增店铺后需要完成授权才能正常使用</li>
        <li>授权流程：点击"授权"按钮 → 跳转平台授权 → 完成授权</li>
        <li>授权成功后需要激活才能使用商品管理功能</li>
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
import { ref, reactive, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { shopApi } from '@/api/shop'

const props = defineProps<{
  show: boolean
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

// 平台选项
const platformOptions = [
  { label: '拼多多', value: 'PINDUODUO' },
  { label: '抖音', value: 'DOUYIN' },
  { label: '小红书', value: 'XIAOHONGSHU' },
]

// 表单数据
const defaultFormData = {
  userId: 1, // 默认用户ID，实际应从登录信息获取
  platform: null as string | null,
  shopName: '',
  shopIdExternal: '',
  remark: '',
}
const formData = reactive({ ...defaultFormData })

// 表单校验规则
const formRules = {
  platform: { required: true, message: '请选择平台', trigger: 'blur' },
  shopName: { required: true, message: '请输入店铺名称', trigger: 'blur' },
}

// 关闭
function handleClose() {
  visible.value = false
  Object.assign(formData, defaultFormData)
  formRef.value?.restoreValidation()
}

// 提交
async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await shopApi.create({
      userId: formData.userId,
      platform: formData.platform!,
      shopName: formData.shopName,
      shopIdExternal: formData.shopIdExternal || undefined,
      remark: formData.remark || undefined,
    })
    message.success('新增成功，请完成授权')
    handleClose()
    emit('success')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>