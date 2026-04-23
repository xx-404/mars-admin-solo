import { request } from '@/utils/request'

// 店铺类型定义
export interface Shop {
  id: number
  userId: number
  platform: string
  shopName: string
  shopIdExternal: string | null
  authToken: string | null
  authStatus: number
  activationStatus: number
  activatedAt: string | null
  expiresAt: string | null
  remark: string | null
  lastSyncAt: string | null
  createTime: string
  updateTime: string | null
  remainingDays: number
  platformName: string
  authStatusName: string
  activationStatusName: string
}

// 授权链接响应
export interface AuthUrlResponse {
  authUrl: string
  state: string
  platform: string
  platformName: string
  orderUrl: string
  orderName: string
  expiresIn: number
}

// 店铺授权信息
export interface ShopAuthInfo {
  shopId: string
  shopName: string
  ownerId: string
  authToken: string
  expireTime: string
}

// 平台类型
export interface PlatformType {
  code: string
  name: string
}

// 店铺查询参数
export interface ShopQueryReq {
  userId?: number
  platform?: string
  shopName?: string
  authStatus?: number
  activationStatus?: number
  shopIdExternal?: string
}

// 店铺 API
export const shopApi = {
  // 分页查询
  page(params: { page: number; pageSize: number } & ShopQueryReq) {
    return request<{ list: Shop[]; total: number }>({ url: '/biz/shop/page', method: 'get', params })
  },

  // 获取用户店铺列表
  listByUserId(userId: number) {
    return request<Shop[]>({ url: '/biz/shop/list', method: 'get', params: { userId } })
  },

  // 根据条件查询店铺列表（自动使用当前登录用户）
  listByConditions(params: {
    platform?: string
    authStatus?: number
    activationStatus?: number
  }) {
    return request<Shop[]>({ url: '/biz/shop/list-by-conditions', method: 'get', params })
  },

  // 获取详情
  detail(id: number) {
    return request<Shop>({ url: `/biz/shop/${id}`, method: 'get' })
  },

  // 新增
  create(data: { userId: number; platform: string; shopName: string; shopIdExternal?: string; remark?: string }) {
    return request<Shop>({ url: '/biz/shop', method: 'post', data })
  },

  // 更新
  update(id: number, data: Partial<Shop>) {
    return request({ url: `/biz/shop/${id}`, method: 'put', data })
  },

  // 删除
  delete(ids: number[]) {
    return request({ url: `/biz/shop/${ids.join(',')}`, method: 'delete' })
  },

  // 获取授权链接
  getAuthUrl(id: number, userId: number) {
    return request<AuthUrlResponse>({ url: `/biz/shop/${id}/authorize`, method: 'post', params: { userId } })
  },

  // 授权回调
  handleAuthCallback(id: number, platform: string, state: string, bindCode?: string, shopName?: string) {
    const params: Record<string, string> = { platform, state }
    if (bindCode) params.bindCode = bindCode
    if (shopName) params.shopName = shopName
    return request<ShopAuthInfo>({ url: `/biz/shop/${id}/authorize/callback`, method: 'get', params })
  },

  // 抖音绑定店铺
  bindDouyinShop(id: number, bindCode: string, shopName: string, state?: string, platform?: string) {
    const params: Record<string, string> = { platform: platform || 'douyin', bindCode, shopName }
    if (state) params.state = state
    return request<ShopAuthInfo>({
      url: `/biz/shop/${id}/authorize/callback`,
      method: 'get',
      params
    })
  },

  // 获取授权状态
  getAuthStatus(id: number, userId: number) {
    return request<ShopAuthInfo>({ url: `/biz/shop/${id}/auth-status`, method: 'get', params: { userId } })
  },

  // 刷新授权
  refreshAuth(id: number, userId: number) {
    return request<AuthUrlResponse>({ url: `/biz/shop/${id}/auth-status`, method: 'post', params: { userId } })
  },

  // 激活店铺
  activate(id: number, userId: number, activationCode: string) {
    return request<ShopAuthInfo>({ url: `/biz/shop/${id}/activate`, method: 'post', params: { userId, activationCode } })
  },

  // 同步商品
  syncProducts(id: number, userId: number, page: number = 1, pageSize: number = 20) {
    return request({ url: `/biz/shop/${id}/products/sync`, method: 'post', params: { userId, page, pageSize } })
  },

  // 商品上架
  listProducts(id: number, userId: number, productIds: string[]) {
    return request({ url: `/biz/shop/${id}/products/list`, method: 'put', params: { userId }, data: productIds })
  },

  // 商品下架
  unlistProducts(id: number, userId: number, productIds: string[]) {
    return request({ url: `/biz/shop/${id}/products/unlist`, method: 'put', params: { userId }, data: productIds })
  },

  // 获取商品详情
  getProductDetail(id: number, userId: number, productId: string) {
    return request({ url: `/biz/shop/${id}/products/${productId}`, method: 'get', params: { userId } })
  },

  // 获取平台订购地址
  getOrderUrl(platform: string) {
    return request<string>({ url: `/biz/shop/platform/${platform}/order-url`, method: 'get' })
  },

  // 获取平台列表
  getPlatforms() {
    return request<PlatformType[]>({ url: '/biz/shop/platforms', method: 'get' })
  }
}