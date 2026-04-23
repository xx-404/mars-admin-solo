<template>
  <n-modal v-model:show="visible" preset="card" title="激活店铺" style="width: 400px">
    <div v-if="shop">
      <!-- 店铺信息 -->
      <n-descriptions :column="1" label-placement="left" bordered size="small">
        <n-descriptions-item label="店铺名称">{{ shop.shopName }}</n-descriptions-item>
        <n-descriptions-item label="平台">
          <n-tag :type="platformColor" size="small">{{ shop.platformName }}</n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="授权状态">
          <n-tag type="success" size="small">已授权</n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="当前状态">
          <n-tag :type="shop.activationStatus === 1 ? 'info' : 'default'" size="small">
            {{ shop.activationStatusName }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item v-if="shop.expiresAt" label="到期时间">
          {{ shop.expiresAt.slice(0, 10) }}
        </n-descriptions-item>
      </n-descriptions>

      <!-- 激活表单 -->
      <n-divider>输入激活码</n-divider>

      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="100px">
        <n-form-item label="激活码" path="activationCode">
          <n-input v-model:value="formData.activationCode" placeholder="请输入激活码" />
        </n-form-item>
      </n-form>

      <n-alert type="info" style="margin-top: 16px">
        <template #header>说明</template>
        <ul style="margin: 0; padding-left: 16px; line-height: 1.8">
          <li>激活码请联系管理员获取</li>
          <li>激活成功后，店铺可使用商品管理功能</li>
          <li>如店铺已激活，新激活码将延长使用时长</li>
        </ul>
      </n-alert>
    </div>

    <template #footer>
      <n-space justify="end">
        <n-button @click="handleClose">取消</n-button>
        <n-button type="primary" :loading="submitting" @click="handleSubmit">激活</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { useMessage } from 'naive-ui'
import { shopApi, type Shop } from '@/api/shop'

const props = defineProps<{
  show: boolean
  shop: Shop | null
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

// 平台颜色
const platformColor = computed(() => {
  if (!props.shop) return 'default'
  const colorMap: Record<string, string> = {
    PINDUODUO: 'warning',
    DOUYIN: 'default',
    XIAOHONGSHU: 'error',
  }
  return colorMap[props.shop.platform] || 'default'
})

// 表单数据
const formData = reactive({
  activationCode: '',
})

// 表单校验规则
const formRules = {
  activationCode: { required: true, message: '请输入激活码', trigger: 'blur' },
}

// 关闭
function handleClose() {
  visible.value = false
  formData.activationCode = ''
  formRef.value?.restoreValidation()
}

// 提交
async function handleSubmit() {
  if (!props.shop) return
  await formRef.value?.validate()
  submitting.value = true
  try {
    const res = await shopApi.activate(props.shop.id, props.shop.userId, formData.activationCode)
    message.success(`激活成功，有效期至 ${res.expireTime?.slice(0, 10) || '未知'}`)
    handleClose()
    emit('success')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>