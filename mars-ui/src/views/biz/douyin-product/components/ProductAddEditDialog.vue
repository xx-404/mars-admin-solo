<template>
  <n-modal
    v-model:show="showModal"
    :title="isEdit ? '编辑商品' : '新增商品'"
    preset="dialog"
    :style="{ width: '800px', maxHeight: '85vh' }"
    class="product-modal"
  >
    <n-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-placement="left"
      :label-width="100"
    >
      <!-- 基本信息 - 必填字段 -->
      <n-divider title-placement="left">基本信息</n-divider>

      <n-grid :cols="2" :x-gap="24">
        <n-gi>
          <n-form-item label="商品名称" path="name">
            <n-input v-model:value="formData.name" placeholder="请输入商品名称" />
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="商品类目" path="categoryLeafId">
            <n-input-number v-model:value="formData.categoryLeafId" placeholder="请输入类目ID" style="width: 100%" />
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="外部商品ID">
            <n-input v-model:value="formData.outerProductId" placeholder="商家自定义ID" />
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="商品主图">
            <n-input v-model:value="formData.pic" placeholder="图片URL" />
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="商品类型">
            <n-select v-model:value="formData.productType" :options="productTypeOptions" />
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="支付方式">
            <n-select v-model:value="formData.payType" :options="payTypeOptions" />
          </n-form-item>
        </n-gi>

        <n-gi :span="2">
          <n-form-item label="商品描述">
            <n-input v-model:value="formData.description" type="textarea" placeholder="请输入商品描述" :rows="3" />
          </n-form-item>
        </n-gi>
      </n-grid>

      <!-- 价格库存 - 必填字段 -->
      <n-divider title-placement="left">价格库存</n-divider>

      <n-grid :cols="2" :x-gap="24">
        <n-gi>
          <n-form-item label="商品价格" path="price">
            <n-input-number v-model:value="formData.price" :min="0" :step="0.01" placeholder="0.00" style="width: 100%">
              <template #suffix>元</template>
            </n-input-number>
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="库存数量" path="stockNum">
            <n-input-number v-model:value="formData.stockNum" :min="0" placeholder="0" style="width: 100%" />
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="原价/划线价">
            <n-input-number v-model:value="formData.originalPrice" :min="0" :step="0.01" placeholder="0.00" style="width: 100%">
              <template #suffix>元</template>
            </n-input-number>
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="成本价">
            <n-input-number v-model:value="formData.costPrice" :min="0" :step="0.01" placeholder="0.00" style="width: 100%">
              <template #suffix>元</template>
            </n-input-number>
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="库存扣减">
            <n-select v-model:value="formData.reduceType" :options="reduceTypeOptions" />
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="每单限购">
            <n-input-number v-model:value="formData.maximumPerOrder" :min="0" placeholder="0不限购" style="width: 100%" />
          </n-form-item>
        </n-gi>
      </n-grid>

      <!-- 物流信息 -->
      <n-divider title-placement="left">物流信息</n-divider>

      <n-grid :cols="2" :x-gap="24">
        <n-gi>
          <n-form-item label="配送方式">
            <n-select v-model:value="formData.deliveryMethod" :options="deliveryMethodOptions" />
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="运费模板">
            <n-input-number v-model:value="formData.freightId" placeholder="选填" style="width: 100%" />
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="商品重量">
            <n-input-number v-model:value="formData.weight" :min="0" :step="0.1" placeholder="0" style="width: 100%">
              <template #suffix>{{ weightUnitLabel }}</template>
            </n-input-number>
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="发货延迟">
            <n-input-number v-model:value="formData.deliveryDelayDay" :min="0" placeholder="0天" style="width: 100%" />
          </n-form-item>
        </n-gi>
      </n-grid>

      <!-- 其他设置 -->
      <n-divider title-placement="left">其他设置</n-divider>

      <n-grid :cols="2" :x-gap="24">
        <n-gi>
          <n-form-item label="支持7天退货">
            <n-switch v-model:value="formData.supply7dayReturn" :checked-value="1" :unchecked-value="0" />
          </n-form-item>
        </n-gi>

        <n-gi>
          <n-form-item label="商品状态">
            <n-select v-model:value="formData.status" :options="statusOptions" />
          </n-form-item>
        </n-gi>

        <n-gi :span="2">
          <n-form-item label="商家备注">
            <n-input v-model:value="formData.remark" type="textarea" placeholder="仅内部可见" :rows="2" />
          </n-form-item>
        </n-gi>
      </n-grid>
    </n-form>

    <template #action>
      <n-space justify="end">
        <n-button @click="showModal = false">取消</n-button>
        <n-button type="primary" :loading="submitting" @click="handleSubmit">确定</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue'
import { useMessage, type FormInst, type FormRules } from 'naive-ui'
import { douyinProductApi, type DouyinProduct, type DouyinProductSaveReq } from '@/api/douyin-product'

const props = defineProps<{
  show: boolean
  product: DouyinProduct | null
}>()

const emit = defineEmits<{
  'update:show': [value: boolean]
  'success': []
}>()

const message = useMessage()
const formRef = ref<FormInst | null>(null)
const submitting = ref(false)

const showModal = ref(false)

watch(() => props.show, (val) => {
  showModal.value = val
})

watch(showModal, (val) => {
  emit('update:show', val)
})

// 选项数据
const productTypeOptions = [
  { label: '普通商品', value: 1 },
  { label: '电子面单商品', value: 2 },
]

const payTypeOptions = [
  { label: '全款支付', value: 0 },
  { label: '定金+尾款', value: 1 },
]

const reduceTypeOptions = [
  { label: '付款减库存', value: 1 },
  { label: '发货减库存', value: 2 },
]

const deliveryMethodOptions = [
  { label: '快递', value: 1 },
  { label: '上门取件', value: 2 },
  { label: '商家自配', value: 3 },
]

const statusOptions = [
  { label: '草稿', value: 0 },
  { label: '审核中', value: 1 },
  { label: '已上架', value: 2 },
  { label: '已下架', value: 3 },
  { label: '审核驳回', value: 4 },
]

// 重量单位标签
const weightUnitLabel = computed(() => {
  const unitMap: Record<number, string> = { 1: 'kg', 2: 'g', 3: '斤' }
  return unitMap[formData.weightUnit || 1]
})

// 表单数据
const formData = reactive<DouyinProductSaveReq>({
  ownerId: '',
  outerProductId: '',
  name: '',
  pic: '',
  description: '',
  recommendRemark: '',
  productType: 1,
  categoryLeafId: null,
  payType: 0,
  price: null,
  originalPrice: null,
  costPrice: null,
  stockNum: null,
  reduceType: 1,
  deliveryMethod: 1,
  freightId: null,
  weight: null,
  weightUnit: 1,
  deliveryDelayDay: 0,
  presellType: null,
  presellDelay: null,
  maximumPerOrder: null,
  minimumPerOrder: 1,
  limitPerBuyer: null,
  supply7dayReturn: 1,
  commit: false,
  status: 0,
  specName: '',
  spuId: null,
  mobile: '',
  remark: '',
})

// 表单验证规则 - 必填字段
const formRules: FormRules = {
  name: { required: true, message: '请输入商品名称', trigger: ['blur', 'input'] },
  categoryLeafId: { required: true, type: 'number', message: '请输入商品类目ID', trigger: ['blur', 'change'] },
  price: { required: true, type: 'number', message: '请输入商品价格', trigger: ['blur', 'change'] },
  stockNum: { required: true, type: 'number', message: '请输入库存数量', trigger: ['blur', 'change'] },
}

// 是否为编辑模式
const isEdit = ref(false)

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    ownerId: '',
    outerProductId: '',
    name: '',
    pic: '',
    description: '',
    recommendRemark: '',
    productType: 1,
    categoryLeafId: null,
    payType: 0,
    price: null,
    originalPrice: null,
    costPrice: null,
    stockNum: null,
    reduceType: 1,
    deliveryMethod: 1,
    freightId: null,
    weight: null,
    weightUnit: 1,
    deliveryDelayDay: 0,
    presellType: null,
    presellDelay: null,
    maximumPerOrder: null,
    minimumPerOrder: 1,
    limitPerBuyer: null,
    supply7dayReturn: 1,
    commit: false,
    status: 0,
    specName: '',
    spuId: null,
    mobile: '',
    remark: '',
  })
}

// 加载商品数据（编辑模式）
watch(() => props.product, (product) => {
  if (product) {
    isEdit.value = true
    Object.assign(formData, {
      id: product.id,
      ownerId: product.ownerId,
      outerProductId: product.outerProductId,
      name: product.name,
      pic: product.pic,
      description: product.description,
      recommendRemark: product.recommendRemark,
      productType: product.productType,
      categoryLeafId: product.categoryLeafId,
      payType: product.payType,
      price: product.price,
      originalPrice: product.originalPrice,
      costPrice: product.costPrice,
      stockNum: product.stockNum,
      reduceType: product.reduceType,
      deliveryMethod: product.deliveryMethod,
      freightId: product.freightId,
      weight: product.weight,
      weightUnit: product.weightUnit,
      deliveryDelayDay: product.deliveryDelayDay,
      presellType: product.presellType,
      presellDelay: product.presellDelay,
      maximumPerOrder: product.maximumPerOrder,
      minimumPerOrder: product.minimumPerOrder,
      limitPerBuyer: product.limitPerBuyer,
      supply7dayReturn: product.supply7dayReturn,
      commit: product.commit,
      status: product.status,
      specName: product.specName,
      spuId: product.spuId,
      mobile: product.mobile,
      remark: product.remark,
    })
  } else {
    isEdit.value = false
    resetForm()
  }
}, { immediate: true })

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    submitting.value = true

    const saveData = { ...formData }
    if (isEdit.value && formData.id) {
      await douyinProductApi.update(formData.id, saveData)
      message.success('更新成功')
    } else {
      await douyinProductApi.add(saveData)
      message.success('新增成功')
    }

    emit('success')
    showModal.value = false
  } catch (error) {
    if (error !== false) {
      message.error('提交失败，请检查必填字段')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.product-modal :deep(.n-divider) {
  margin: 16px 0 12px;
}

.product-modal :deep(.n-form-item) {
  margin-bottom: 16px;
}
</style>