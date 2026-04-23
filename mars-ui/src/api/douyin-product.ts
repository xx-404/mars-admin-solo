import { request } from '@/utils/request'

// 抖音商品类型定义（本地数据库）
export interface DouyinProduct {
  id: number
  ownerId: string
  productId: string | null
  outerProductId: string | null
  name: string
  pic: string | null
  description: string | null
  price: number | null
  originalPrice: number | null
  costPrice: number | null
  stockNum: number | null
  status: number
  statusName: string
  auditStatus: number
  auditStatusName: string
  auditRemark: string | null
  categoryLeafId: number | null
  productType: number | null
  productTypeName: string
  deliveryMethod: number | null
  presellType: number | null
  presellConfigLevel: number | null
  presellDeliveryType: number | null
  supply7dayReturn: number | null
  createTime: string
  updateTime: string | null
  publishTime: string | null
  // 物流信息
  weight: number | null
  weightUnit: number | null
  deliveryDelayDay: number | null
  freightId: number | null
  // 限购设置
  maximumPerOrder: number | null
  minimumPerOrder: number | null
  limitPerBuyer: number | null
  // 库存扣减方式
  reduceType: number | null
  // 支付方式
  payType: number | null
  // 预售延迟
  presellDelay: number | null
  // 推荐备注
  recommendRemark: string | null
  // 规格
  specName: string | null
  spuId: number | null
  // 其他
  commit: boolean | null
  mobile: string | null
  remark: string | null
}

// 抖音平台商品类型定义（从抖音API查询）
export interface DouyinProductV2 {
  productId: number
  name: string
  img: string | null
  description: string | null
  recommendRemark: string | null
  status: number
  checkStatus: number
  productType: number
  payType: number | null
  marketPrice: number | null // 单位：分
  discountPrice: number | null // 单位：分
  outerProductId: string | null
  freightId: number | null
  mobile: string | null
  createTime: number | null // 时间戳（秒）
  updateTime: number | null // 时间戳（秒）
  sellNum: number | null
  canCombine: boolean | null
  isSecondHandDigital: boolean | null
  isPackageProduct: boolean | null
  specPrices: SpecPriceItem[] | null
  categoryDetail: CategoryDetail | null
  shopCategory: ShopCategory | null
}

export interface SpecPriceItem {
  id: number
  code: string | null
  barcodes: string[] | null
}

export interface CategoryDetail {
  firstCid: number | null
  secondCid: number | null
  thirdCid: number | null
  fourthCid: number | null
  firstCname: string | null
  secondCname: string | null
  thirdCname: string | null
  fourthCname: string | null
}

export interface ShopCategory {
  leafCategoryIds: number[] | null
}

// 商品保存请求
export interface DouyinProductSaveReq {
  id?: number
  ownerId?: string
  productId?: string
  outerProductId?: string
  name?: string
  pic?: string
  description?: string
  categoryLeafId?: number | null
  price?: number | null
  originalPrice?: number | null
  costPrice?: number | null
  stockNum?: number | null
  deliveryMethod?: number | null
  weight?: number | null
  weightUnit?: number | null
  deliveryDelayDay?: number | null
  presellType?: number | null
  presellConfigLevel?: number | null
  presellDeliveryType?: number | null
  supply7dayReturn?: number | null
  remark?: string
  status?: number
  // 限购设置
  maximumPerOrder?: number | null
  minimumPerOrder?: number | null
  limitPerBuyer?: number | null
  // 规格
  specName?: string
  spuId?: number | null
  // 其他
  commit?: boolean
  mobile?: string
  // 支付
  payType?: number | null
  // 库存
  reduceType?: number | null
  // 运费
  freightId?: number | null
  // 预售
  presellDelay?: number | null
  // 推荐备注
  recommendRemark?: string
  // 商品类型
  productType?: number | null
}

// 商品查询参数（本地数据库）
export interface DouyinProductQueryReq {
  ownerId?: string
  productId?: string
  outerProductId?: string
  name?: string
  categoryLeafId?: number | null
  status?: number | null
  auditStatus?: number | null
  page?: number
  pageSize?: number
}

// 商品查询参数（抖音平台）
export interface DouyinProductListV2Req {
  ownerId?: string
  name?: string
  productId?: string[]
  skuCodes?: string[]
  status?: number | null
  checkStatus?: number | null
  productType?: number | null
  storeId?: number | null
  startTime?: number | null // 时间戳（秒）
  endTime?: number | null // 时间戳（秒）
  updateStartTime?: number | null
  updateEndTime?: number | null
  page?: number
  size?: number
  useCursor?: boolean
  cursorId?: string
  canCombineProduct?: boolean
  needRectificationInfo?: boolean
  needCheckOut?: boolean
}

// 分页响应
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// 抖音平台商品列表响应
export interface DouyinProductListV2Rsp {
  data: DouyinProductV2[]
  total: number
  page: number
  size: number
  cursorId: string | null
}

// 抖音商品 API
export const douyinProductApi = {
  // 分页查询（本地数据库）
  page(params: { page: number; pageSize: number } & DouyinProductQueryReq) {
    return request<PageResult<DouyinProduct>>({ url: '/biz/douyin-product/page', method: 'get', params })
  },

  // 列表查询（本地数据库）
  list(params: DouyinProductQueryReq) {
    return request<{ list: DouyinProduct[]; total: number }>({ url: '/biz/douyin-product/list', method: 'get', params })
  },

  // 从抖音平台查询商品列表
  listProductV2(params: DouyinProductListV2Req) {
    return request<DouyinProductListV2Rsp>({ url: '/biz/douyin-product/product/list', method: 'get', params })
  },

  // 获取详情
  getInfo(id: number) {
    return request<DouyinProduct>({ url: `/biz/douyin-product/${id}`, method: 'get' })
  },

  // 新增
  add(data: DouyinProductSaveReq) {
    return request<DouyinProduct>({ url: '/biz/douyin-product', method: 'post', data })
  },

  // 更新
  update(id: number, data: DouyinProductSaveReq) {
    return request<DouyinProduct>({ url: `/biz/douyin-product/${id}`, method: 'put', data })
  },

  // 保存（新增或更新）
  save(data: DouyinProductSaveReq) {
    return request<DouyinProduct>({ url: '/biz/douyin-product/save', method: 'post', data })
  },

  // 删除
  delete(ids: number[]) {
    return request({ url: `/biz/douyin-product/${ids.join(',')}`, method: 'delete' })
  },

  // 上架
  onSale(id: number) {
    return request({ url: `/biz/douyin-product/${id}/on-sale`, method: 'put' })
  },

  // 下架
  offSale(id: number) {
    return request({ url: `/biz/douyin-product/${id}/off-sale`, method: 'put' })
  },

  // 提交审核
  submitAudit(id: number) {
    return request({ url: `/biz/douyin-product/${id}/submit-audit`, method: 'put' })
  },

  // 根据外部 ID 查询
  getByOwnerIdAndProductId(ownerId: string, productId: string) {
    return request<DouyinProduct>({
      url: '/biz/douyin-product/external',
      method: 'get',
      params: { ownerId, productId }
    })
  },
}
