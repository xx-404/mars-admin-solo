<template>
  <n-modal v-model:show="visible" preset="card" title="店铺授权" style="width: 600px">
    <div v-if="shop">
      <!-- 店铺信息 -->
      <n-descriptions :column="2" label-placement="left" bordered size="small">
        <n-descriptions-item label="店铺名称">{{ shop.shopName }}</n-descriptions-item>
        <n-descriptions-item label="平台">
          <n-tag :type="platformColor" size="small">{{ shop.platformName }}</n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="授权状态">
          <n-tag :type="shop.authStatus === 1 ? 'success' : 'error'" size="small">
            {{ shop.authStatusName }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="激活状态">
          <n-tag :type="shop.activationStatus === 1 ? 'info' : 'default'" size="small">
            {{ shop.activationStatusName }}
          </n-tag>
        </n-descriptions-item>
      </n-descriptions>

      <!-- 授权流程 -->
      <n-divider>授权流程</n-divider>

      <n-space vertical size="large">
        <!-- 步骤说明 -->
        <n-alert type="info">
          <template #header>授权说明</template>
          <div v-if="shop.platform === 'DOUYIN'">
            <ol style="margin: 0; padding-left: 16px; line-height: 1.8">
              <li>点击下方"授权链接"按钮，跳转到抖音服务市场</li>
              <li>完成授权后，页面会显示<strong>店铺名称</strong>和<strong>关联码</strong></li>
              <li>复制关联码和店铺名称，填入下方输入框</li>
              <li>点击"绑定店铺"完成授权</li>
            </ol>
          </div>
          <div v-else>
            <ol style="margin: 0; padding-left: 16px; line-height: 1.8">
              <li>点击下方"授权链接"按钮，跳转到平台授权页面</li>
              <li>完成授权后，关闭授权窗口</li>
              <li>点击"完成授权"按钮，系统自动获取店铺信息</li>
            </ol>
          </div>
        </n-alert>

        <!-- 授权链接按钮 -->
        <n-space>
          <n-button type="primary" :loading="loadingAuthUrl" @click="handleGetAuthUrl">
            <template #icon><n-icon><LinkOutline /></n-icon></template>
            获取授权链接
          </n-button>
          <n-button v-if="authUrl" @click="handleOpenAuthUrl">
            <template #icon><n-icon><OpenOutline /></n-icon></template>
            打开授权页面
          </n-button>
          <n-button v-if="orderUrl" quaternary @click="handleOpenOrderUrl">
            <template #icon><n-icon><CartOutline /></n-icon></template>
            订购服务
          </n-button>
        </n-space>

        <!-- 抖音绑定表单 -->
        <div v-if="shop.platform === 'DOUYIN' && authUrl">
          <n-divider style="margin: 16px 0">绑定店铺</n-divider>
          <n-form :model="bindForm" label-placement="left" label-width="100px">
            <n-form-item label="店铺名称" required>
              <n-input v-model:value="bindForm.shopName" placeholder="请输入授权后显示的店铺名称" />
            </n-form-item>
            <n-form-item label="关联码" required>
              <n-input v-model:value="bindForm.bindCode" placeholder="请输入授权后显示的关联码" />
            </n-form-item>
            <n-form-item>
              <n-button type="primary" :loading="submitting" @click="handleBindDouyin">
                绑定店铺
              </n-button>
            </n-form-item>
          </n-form>
        </div>

        <!-- 其他平台完成授权按钮 -->
        <div v-if="shop.platform !== 'DOUYIN' && authUrl && state">
          <n-button type="success" :loading="submitting" @click="handleCompleteAuth">
            <template #icon><n-icon><CheckmarkOutline /></n-icon></template>
            完成授权
          </n-button>
        </div>
      </n-space>
    </div>

    <template #footer>
      <n-button @click="handleClose">关闭</n-button>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { useMessage } from 'naive-ui'
import { LinkOutline, OpenOutline, CartOutline, CheckmarkOutline } from '@vicons/ionicons5'
import { shopApi, type Shop, type AuthUrlResponse } from '@/api/shop'

const props = defineProps<{
  show: boolean
  shop: Shop | null
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'success'): void
}>()

const message = useMessage()

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

// 授权相关状态
const loadingAuthUrl = ref(false)
const authUrl = ref('')
const state = ref('')
const platform = ref('')
const orderUrl = ref('')
const submitting = ref(false)

// 抖音绑定表单
const bindForm = reactive({
  shopName: '',
  bindCode: '',
})

// 获取授权链接
async function handleGetAuthUrl() {
  if (!props.shop) return
  loadingAuthUrl.value = true
  try {
    const res = await shopApi.getAuthUrl(props.shop.id, props.shop.userId)
    authUrl.value = res.authUrl
    state.value = res.state
    platform.value = res.platform
    orderUrl.value = res.orderUrl
    message.success('授权链接已生成，即将打开授权页面')
    // 自动打开授权页面
    setTimeout(() => {
      if (authUrl.value) {
        window.open(authUrl.value, '_blank', 'width=800,height=600')
      }
    }, 500)
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loadingAuthUrl.value = false
  }
}

// 打开授权页面
function handleOpenAuthUrl() {
  if (authUrl.value) {
    window.open(authUrl.value, '_blank', 'width=800,height=600')
  }
}

// 打开订购页面
function handleOpenOrderUrl() {
  if (orderUrl.value) {
    window.open(orderUrl.value, '_blank')
  }
}

// 完成授权（拼多多/小红书）
async function handleCompleteAuth() {
  if (!props.shop || !state.value) return
  submitting.value = true
  try {
    await shopApi.handleAuthCallback(props.shop.id, props.shop.platform, state.value)
    message.success('授权成功')
    handleClose()
    emit('success')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    submitting.value = false
  }
}

// 绑定抖音店铺
async function handleBindDouyin() {
  if (!props.shop) return
  if (!bindForm.shopName || !bindForm.bindCode) {
    message.warning('请填写店铺名称和关联码')
    return
  }
  submitting.value = true
  try {
    // 抖音绑定需要传递 state 和 platform（后端从 remark 中验证 state）
    await shopApi.bindDouyinShop(props.shop.id, bindForm.bindCode, bindForm.shopName, state.value, platform.value)
    message.success('绑定成功')
    handleClose()
    emit('success')
  } catch (error: any) {
    message.error(error?.message || '绑定失败')
  } finally {
    submitting.value = false
  }
}

// 关闭
function handleClose() {
  visible.value = false
  authUrl.value = ''
  state.value = ''
  platform.value = ''
  orderUrl.value = ''
  bindForm.shopName = ''
  bindForm.bindCode = ''
}
</script>