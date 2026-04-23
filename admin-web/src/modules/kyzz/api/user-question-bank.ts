import http from '@/shared/http/client'
import type {
  KyzzUserQuestionBankAdminDashboard,
  KyzzUserQuestionBankAdminUserDetail,
  KyzzUserQuestionBankSelectionUpdateResponse
} from '@/shared/types/admin'

export interface KyzzUserQuestionBankDashboardQuery {
  keyword?: string
  selectionStatus?: 'all' | 'selected' | 'unselected'
  pageNo?: number
  pageSize?: number
}

export interface KyzzUserQuestionBankSelectionUpdateRequest {
  selected: boolean
}

export function fetchKyzzUserQuestionBankDashboard(
  params: KyzzUserQuestionBankDashboardQuery
): Promise<KyzzUserQuestionBankAdminDashboard> {
  return http.get('/admin/kyzz/user-question-banks', { params })
}

export function fetchKyzzUserQuestionBankDetail(userId: number): Promise<KyzzUserQuestionBankAdminUserDetail> {
  return http.get(`/admin/kyzz/user-question-banks/${userId}`)
}

export function updateKyzzUserQuestionBankSelection(
  userId: number,
  bankId: number,
  data: KyzzUserQuestionBankSelectionUpdateRequest
): Promise<KyzzUserQuestionBankSelectionUpdateResponse> {
  return http.put(`/admin/kyzz/user-question-banks/${userId}/banks/${bankId}/selection`, data)
}
