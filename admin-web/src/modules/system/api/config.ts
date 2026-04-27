import http from '@/shared/http/client'
import type {
  AdminLlmTestResponse,
  LlmCallRecord,
  PageResponse,
  SystemConfigChangeLog,
  SystemConfigItem
} from '@/shared/types/admin'

export interface SystemConfigQuery {
  group?: string
  keyword?: string
}

export interface SystemConfigUpdateRequest {
  value?: string
  keepSensitiveValue?: boolean
  enabled?: number
}

export interface SystemConfigChangeLogQuery {
  configKey?: string
  pageNo?: number
  pageSize?: number
}

export interface AdminLlmTestRequest {
  mode: 'text' | 'json'
  scene: string
  systemPrompt: string
  userPrompt: string
  schemaName?: string
  jsonSchema?: string
}

export interface LlmCallRecordQuery {
  status?: string
  scene?: string
  pageNo?: number
  pageSize?: number
}

export function fetchSystemConfigs(params: SystemConfigQuery = {}): Promise<SystemConfigItem[]> {
  return http.get('/admin/configs', { params })
}

export function updateSystemConfig(configKey: string, data: SystemConfigUpdateRequest): Promise<SystemConfigItem> {
  return http.put(`/admin/configs/${encodeURIComponent(configKey)}`, data)
}

export function fetchSystemConfigChangeLogs(
  params: SystemConfigChangeLogQuery = {}
): Promise<PageResponse<SystemConfigChangeLog>> {
  return http.get('/admin/configs/change-logs', { params })
}

export function testLlm(data: AdminLlmTestRequest): Promise<AdminLlmTestResponse> {
  return http.post('/admin/llm/test', data)
}

export function fetchLlmCallRecords(params: LlmCallRecordQuery = {}): Promise<PageResponse<LlmCallRecord>> {
  return http.get('/admin/llm/records', { params })
}

export function fetchLlmCallRecordDetail(id: number): Promise<LlmCallRecord> {
  return http.get(`/admin/llm/records/${id}`)
}
