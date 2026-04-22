// AI 索引: KYZZ 题目管理请求。
import http from '@/shared/http/client'
import type {
  KyzzQuestionAdminDashboard,
  KyzzQuestionAdminDetail,
  KyzzQuestionAdminItem
} from '@/shared/types/admin'

export interface KyzzQuestionDashboardQuery {
  pageNo: number
  pageSize: number
  keyword?: string
  questionBankId?: number
  categoryId?: number
  questionType?: string
  status?: number
  difficultyLevel?: number
  yearNo?: number
}

export interface KyzzQuestionOptionPayload {
  optionKey: string
  optionContent: string
  isCorrect: number
}

export interface KyzzQuestionUpsertRequest {
  questionBankId: number
  categoryId: number | null
  questionType: string
  stem: string
  analysis: string
  answerText: string
  difficultyLevel: number
  score: number
  sourceName: string
  yearNo: number | null
  sortNo: number
  status: number
  options: KyzzQuestionOptionPayload[]
}

export function fetchKyzzQuestionDashboard(params: KyzzQuestionDashboardQuery): Promise<KyzzQuestionAdminDashboard> {
  return http.get('/admin/kyzz/questions', { params })
}

export function fetchKyzzQuestionDetail(questionId: number): Promise<KyzzQuestionAdminDetail> {
  return http.get(`/admin/kyzz/questions/${questionId}`)
}

export function createKyzzQuestion(data: KyzzQuestionUpsertRequest): Promise<KyzzQuestionAdminDetail> {
  return http.post('/admin/kyzz/questions', data)
}

export function updateKyzzQuestion(questionId: number, data: KyzzQuestionUpsertRequest): Promise<KyzzQuestionAdminDetail> {
  return http.put(`/admin/kyzz/questions/${questionId}`, data)
}

export function updateKyzzQuestionStatus(questionId: number, status: number): Promise<KyzzQuestionAdminItem> {
  return http.put(`/admin/kyzz/questions/${questionId}/status`, { status })
}

export function deleteKyzzQuestion(questionId: number): Promise<void> {
  return http.delete(`/admin/kyzz/questions/${questionId}`)
}
