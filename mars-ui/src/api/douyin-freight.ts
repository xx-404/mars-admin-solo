import { request } from '@/utils/request'

// 分页结果类型
interface PageResult<T> {
  list: T[]
  total: number
}

// 抖音运费模板类型
export interface DouyinFreightTemplate {
  id: number
  ownerId: string
  freightId: number | null
  templateName: string
  productProvince: number | null
  productProvinceName: string | null
  productCity: number | null
  productCityName: string | null
  calculateType: number
  calculateTypeName: string
  transferType: number
  transferTypeName: string
  ruleType: number
  ruleTypeName: string
  fixedAmount: number | null
  columns: string | null
  upsertTransferRule: boolean | null
  status: number
  statusName: string
  syncStatus: number
  syncStatusName: string
  syncTime: string | null
  syncError: string | null
  createTime: string
  updateTime: string | null
}

// 查询请求
export interface DouyinFreightTemplateQueryReq {
  ownerId?: string
  templateName?: string
  status?: number | null
  syncStatus?: number | null
  page?: number
  pageSize?: number
}

// 保存请求
export interface DouyinFreightTemplateSaveReq {
  id?: number
  ownerId?: string
  freightId?: number | null
  templateName?: string
  productProvince?: number | null
  productProvinceName?: string | null
  productCity?: number | null
  productCityName?: string | null
  calculateType?: number | null
  transferType?: number | null
  ruleType?: number | null
  fixedAmount?: number | null
  columns?: string | null
  upsertTransferRule?: boolean | null
  status?: number | null
}

// 同步请求
export interface DouyinFreightTemplateSyncReq {
  ownerId: string
}

// 运费模板 API
export const douyinFreightApi = {
  // 分页查询
  page(params: { page: number; pageSize: number } & DouyinFreightTemplateQueryReq) {
    return request<PageResult<DouyinFreightTemplate>>({
      url: '/biz/douyin-freight/page',
      method: 'get',
      params
    })
  },

  // 列表查询
  list(params: DouyinFreightTemplateQueryReq) {
    return request<DouyinFreightTemplate[]>({
      url: '/biz/douyin-freight/list',
      method: 'get',
      params
    })
  },

  // 详情查询
  getInfo(id: number) {
    return request<DouyinFreightTemplate>({
      url: `/biz/douyin-freight/${id}`,
      method: 'get'
    })
  },

  // 根据店铺查询
  getByOwnerId(ownerId: string) {
    return request<DouyinFreightTemplate[]>({
      url: '/biz/douyin-freight/by-owner',
      method: 'get',
      params: { ownerId }
    })
  },

  // 新增
  add(data: DouyinFreightTemplateSaveReq) {
    return request<DouyinFreightTemplate>({
      url: '/biz/douyin-freight',
      method: 'post',
      data
    })
  },

  // 更新
  update(id: number, data: DouyinFreightTemplateSaveReq) {
    return request<DouyinFreightTemplate>({
      url: `/biz/douyin-freight/${id}`,
      method: 'put',
      data
    })
  },

  // 保存（新增或更新）
  save(data: DouyinFreightTemplateSaveReq) {
    return request<DouyinFreightTemplate>({
      url: '/biz/douyin-freight/save',
      method: 'post',
      data
    })
  },

  // 批量删除
  delete(ids: number[]) {
    return request<void>({
      url: `/biz/douyin-freight/${ids.join(',')}`,
      method: 'delete'
    })
  },

  // 启用
  enable(id: number) {
    return request<void>({
      url: `/biz/douyin-freight/${id}/enable`,
      method: 'put'
    })
  },

  // 禁用
  disable(id: number) {
    return request<void>({
      url: `/biz/douyin-freight/${id}/disable`,
      method: 'put'
    })
  },

  // 从抖音同步
  sync(data: DouyinFreightTemplateSyncReq) {
    return request<void>({
      url: '/biz/douyin-freight/sync',
      method: 'post',
      data
    })
  },

  // 推送到抖音
  push(id: number) {
    return request<void>({
      url: `/biz/douyin-freight/${id}/push`,
      method: 'post'
    })
  }
}