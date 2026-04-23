<template>
  <n-modal v-model:show="visible" preset="card" title="激活激活码" style="width: 500px">
    <div v-if="activationCode">
      <!-- 激活码信息 -->
      <n-descriptions :column="2" label-placement="left" bordered size="small">
        <n-descriptions-item label="激活码">{{ activationCode.activationCode }}</n-descriptions-item>
        <n-descriptions-item label="类型">
          <n-tag :type="durationTypeColor" size="small">{{ activationCode.durationTypeName }}</n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="天数">{{ activationCode.durationDays }}天</n-descriptions-item>
        <n-descriptions-item label="激活状态">
          <n-tag :type="activationCode.activationStatus === 1 ? 'success' : 'default'" size="small">
            {{ activationCode.activationStatusName }}
          </n-tag>
        </n-descriptions-item>
      </n-descriptions>

      <!-- 选择店铺 -->
      <n-divider>选择店铺</n-divider>

      <n-form ref="formRef" :model="formData" :rules="formRules" label-placement="left" label-width="100px">
        <n-form-item label="店铺" path="shopId">
          <n-select
            v-model:value="formData.shopId"
            placeholder="请选择店铺"
            :options="shopOptions"
            :disabled="shopOptions.length === 0"
          />
        </n-form-item>
      </n-form>

      <n-alert type="info" style="margin-top: 16px">
        <template #header>说明</template>
        <ul style="margin: 0; padding-left: 16px; line-height: 1.8">
          <li>请选择要激活的店铺</li>
          <li>激活成功后，店铺可使用商品管理功能</li>
          <li>如店铺已激活，将延长使用时长</li>
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
import { ref, reactive, watch, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { activationCodeApi, type ActivationCode } from '@/api/activation-code'
import { shopApi, type Shop } from '@/api/shop'

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

// 激活码数据
const activationCode = ref<ActivationCode | null>(null)

// 店铺列表
const shopOptions = ref<{ label: string; value: number }[]>([])

// 类型颜色
const durationTypeColor = computed(() => {
  if (!activationCode.value) return 'default'
  const colorMap: Record<string, string> = {
    DAY: 'warning',
    MONTH: 'info',
    QUARTER: 'success',
    YEAR: 'error'
  }
  return colorMap[activationCode.value.durationType] || 'default'
})

// 表单数据
const formData = reactive({
  shopId: null as number | null
})

// 表单校验规则
const formRules = {
  shopId: { required: true, message: '请选择店铺', trigger: 'blur' }
}

// 加载店铺列表
async function loadShops() {
  try {
    // 假设用户 ID 为 1，实际应从登录信息获取
    const shops = await shopApi.listByUserId(1)
    shopOptions.value = shops
      .filter(s => s.authStatus === 1) // 只显示已授权店铺
      .map(s => ({ label: s.shopName, value: s.id }))
  } catch (error) {
    // 错误已在拦截器处理
  }
}

// 关闭
function handleClose() {
  visible.value = false
  activationCode.value = null
  formData.shopId = null
  formRef.value?.restoreValidation()
}

// 提交
async function handleSubmit() {
  await formRef.value?.validate()
  if (!formData.shopId) return

  submitting.value = true
  try {
    await activationCodeApi.activate(activationCode.value!.activationCode, formData.shopId)
    message.success('激活成功')
    handleClose()
    emit('success')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    submitting.value = false
  }
}

// 监听弹窗打开
watch(visible, (val) => {
  if (val) {
    // 获取未激活的激活码
    activationCodeApi.list({ activationStatus: 0 }).then(res => {
      if (res.length > 0) {
        activationCode.value = res[0]
      }
    })
    loadShops()
  }
})
</script>
