import http from '@/shared/http/client'
import type { KyzzQuestionBankAdminDashboard, KyzzQuestionBankAdminItem, UploadFileResponse } from '@/shared/types/admin'

export interface KyzzQuestionBankDashboardQuery {
  keyword?: string
  status?: number
  categoryId?: number
  difficultyLevel?: number
}

export interface KyzzQuestionBankUpsertRequest {
  bankCode: string
  bankName: string
  subtitle: string
  description: string
  categoryId: number | null
  difficultyLevel: number
  sortNo: number
  status: number
}

export function fetchKyzzQuestionBankDashboard(params: KyzzQuestionBankDashboardQuery): Promise<KyzzQuestionBankAdminDashboard> {
  return http.get('/admin/kyzz/question-banks', { params })
}

export function createKyzzQuestionBank(data: KyzzQuestionBankUpsertRequest): Promise<KyzzQuestionBankAdminItem> {
  return http.post('/admin/kyzz/question-banks', data)
}

export function updateKyzzQuestionBank(bankId: number, data: KyzzQuestionBankUpsertRequest): Promise<KyzzQuestionBankAdminItem> {
  return http.put(`/admin/kyzz/question-banks/${bankId}`, data)
}

export function updateKyzzQuestionBankStatus(bankId: number, status: number): Promise<KyzzQuestionBankAdminItem> {
  return http.put(`/admin/kyzz/question-banks/${bankId}/status`, { status })
}

export function updateKyzzQuestionBankCover(bankId: number, coverStorageKey: string | null): Promise<KyzzQuestionBankAdminItem> {
  return http.put(`/admin/kyzz/question-banks/${bankId}/cover`, { coverStorageKey })
}

export function syncKyzzQuestionBankQuestionCount(bankId: number): Promise<KyzzQuestionBankAdminItem> {
  return http.put(`/admin/kyzz/question-banks/${bankId}/sync-question-count`)
}

export function uploadKyzzQuestionBankCover(file: File): Promise<UploadFileResponse> {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/admin/files/kyzz-question-bank-cover', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function deleteKyzzQuestionBank(bankId: number): Promise<void> {
  return http.delete(`/admin/kyzz/question-banks/${bankId}`)
}
