import http from '@/shared/http/client'
import type { KyzzCategoryAdminDashboard, KyzzCategoryAdminItem } from '@/shared/types/admin'

export interface KyzzCategoryDashboardQuery {
  keyword?: string
  isEnabled?: number
  categoryLevel?: number
}

export interface KyzzCategoryUpsertRequest {
  categoryCode: string
  categoryName: string
  categoryLevel: number
  sortNo: number
  isEnabled: number
}

export function fetchKyzzCategoryDashboard(params: KyzzCategoryDashboardQuery): Promise<KyzzCategoryAdminDashboard> {
  return http.get('/admin/kyzz/question-bank-categories', { params })
}

export function createKyzzCategory(data: KyzzCategoryUpsertRequest): Promise<KyzzCategoryAdminItem> {
  return http.post('/admin/kyzz/question-bank-categories', data)
}

export function updateKyzzCategory(categoryId: number, data: KyzzCategoryUpsertRequest): Promise<KyzzCategoryAdminItem> {
  return http.put(`/admin/kyzz/question-bank-categories/${categoryId}`, data)
}

export function updateKyzzCategoryStatus(categoryId: number, isEnabled: number): Promise<KyzzCategoryAdminItem> {
  return http.put(`/admin/kyzz/question-bank-categories/${categoryId}/status`, { isEnabled })
}

export function deleteKyzzCategory(categoryId: number): Promise<void> {
  return http.delete(`/admin/kyzz/question-bank-categories/${categoryId}`)
}
