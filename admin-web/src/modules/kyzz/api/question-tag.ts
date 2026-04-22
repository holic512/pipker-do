// AI 索引: KYZZ 题目标签管理请求。
import http from '@/shared/http/client'
import type { KyzzQuestionTagAdminDashboard, KyzzQuestionTagAdminRow } from '@/shared/types/admin'

export interface KyzzQuestionTagDashboardQuery {
  keyword?: string
  usedStatus?: number
}

export interface KyzzQuestionTagUpsertRequest {
  tagName: string
  color: string
}

export function fetchKyzzQuestionTagDashboard(params: KyzzQuestionTagDashboardQuery): Promise<KyzzQuestionTagAdminDashboard> {
  return http.get('/admin/kyzz/question-tags', { params })
}

export function createKyzzQuestionTag(data: KyzzQuestionTagUpsertRequest): Promise<KyzzQuestionTagAdminRow> {
  return http.post('/admin/kyzz/question-tags', data)
}

export function updateKyzzQuestionTag(tagId: number, data: KyzzQuestionTagUpsertRequest): Promise<KyzzQuestionTagAdminRow> {
  return http.put(`/admin/kyzz/question-tags/${tagId}`, data)
}

export function syncKyzzQuestionTagUseCount(tagId: number): Promise<KyzzQuestionTagAdminRow> {
  return http.put(`/admin/kyzz/question-tags/${tagId}/sync-use-count`)
}

export function deleteKyzzQuestionTag(tagId: number): Promise<void> {
  return http.delete(`/admin/kyzz/question-tags/${tagId}`)
}
