import { request } from '@/utils/request'

// 激活码类型定义
export interface ActivationCode {
  id: number
  activationCode: string
  durationType: string
  durationTypeName: string
  durationDays: number
  activationStatus: number
  activationStatusName: string
  userId: number | null
  shopId: number | null
  activatedAt: string | null
  createTime: string
  updateTime: string | null
}

// 激活码查询参数
export interface ActivationCodeQueryReq {
  activationCode?: string
  durationType?: string
  activationStatus?: number
  userId?: number
  shopId?: number
}

// 激活响应
export interface ActivationResponse {
  id: number
  activationCode: string
  durationType: string
  durationTypeName: string
  durationDays: number
  activationStatus: number
  activationStatusName: string
  userId: number
  shopId: number
  activatedAt: string
  createTime: string
  updateTime: string
}

// 激活码 API
export const activationCodeApi = {
  // 分页查询
  page(params: { page: number; pageSize: number } & ActivationCodeQueryReq) {
    return request<{ list: ActivationCode[]; total: number }>({
      url: '/biz/activation-code/page',
      method: 'get',
      params
    })
  },

  // 获取列表
  list(params: ActivationCodeQueryReq) {
    return request<ActivationCode[]>({ url: '/biz/activation-code/list', method: 'get', params })
  },

  // 获取详情
  detail(id: number) {
    return request<ActivationCode>({ url: `/biz/activation-code/${id}`, method: 'get' })
  },

  // 新增
  create(data: Partial<ActivationCode>) {
    return request<ActivationCode>({ url: '/biz/activation-code', method: 'post', data })
  },

  // 更新
  update(id: number, data: Partial<ActivationCode>) {
    return request({ url: `/biz/activation-code/${id}`, method: 'put', data })
  },

  // 删除
  delete(ids: number[]) {
    return request({ url: `/biz/activation-code/${ids.join(',')}`, method: 'delete' })
  },

  // 激活
  activate(code: string, shopId: number) {
    return request<ActivationResponse>({
      url: '/biz/activation-code/activate',
      method: 'post',
      params: { code, shopId }
    })
  },

  // 导入激活码
  importCodes(file: File): Promise<{ successCount: number; failCount: number; errors: string[] }> {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/biz/activation-code/import',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
