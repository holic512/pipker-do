export interface AdminProject {
  code: string
  name: string
  enabled: boolean
}

export interface AdminCurrentUser {
  id: number
  username: string
  displayName: string
  roles: string[]
  projects: AdminProject[]
  defaultProjectCode: string | null
  lastLoginAt: string | null
}

export interface AdminRoleSummary {
  id: number
  roleCode: string
  roleName: string
  status: number
  adminCount: number
  description: string
  capabilities: string[]
  protectedRole: boolean
  createdAt: string
  updatedAt: string
}

export interface AdminAssignedRole {
  id: number
  roleCode: string
  roleName: string
  status: number
  protectedRole: boolean
}

export interface AdminManagedUser {
  id: number
  username: string
  displayName: string
  status: number
  defaultProjectCode: string | null
  defaultProjectName: string | null
  roles: AdminAssignedRole[]
  projects: AdminProject[]
  protectedAccount: boolean
  lastLoginAt: string | null
  createdAt: string
  updatedAt: string
}

export interface AdminLoginData {
  token: string
  admin: AdminCurrentUser
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  requestId: string
  timestamp: string
}

export interface UploadFileResponse {
  storageKey: string
  url: string
  size: number
  contentType: string
  originalFilename: string
}

export interface AdminSessionState {
  token: string
  adminUser: AdminCurrentUser | null
  currentProjectCode: string | null
  availableProjects: AdminProject[]
}

export interface KyzzCategoryAdminStats {
  totalCategories: number
  enabledCategories: number
  levelOneCategories: number
  linkedQuestionBanks: number
  linkedQuestions: number
}

export interface KyzzCategoryAdminItem {
  id: number
  categoryCode: string
  categoryName: string
  categoryLevel: number
  sortNo: number
  isEnabled: number
  questionBankCount: number
  questionCount: number
  canDelete: boolean
  createdAt: string
  updatedAt: string
}

export interface KyzzCategoryAdminDashboard {
  stats: KyzzCategoryAdminStats
  categories: KyzzCategoryAdminItem[]
}

export interface KyzzCategoryOption {
  id: number
  categoryCode: string
  categoryName: string
  categoryLevel: number
  isEnabled: number
}

export interface KyzzQuestionBankAdminStats {
  totalBanks: number
  activeBanks: number
  inactiveBanks: number
  uncategorizedBanks: number
  totalQuestions: number
  totalStudyUsers: number
}

export interface KyzzQuestionBankAdminItem {
  id: number
  bankCode: string
  bankName: string
  subtitle: string | null
  coverUrl: string | null
  coverStorageKey: string | null
  description: string | null
  categoryId: number | null
  categoryName: string | null
  categoryLevel: number | null
  difficultyLevel: number
  questionCount: number
  actualQuestionCount: number
  totalScore: number
  ratingCount: number
  collectCount: number
  studyUserCount: number
  status: number
  sortNo: number
  createdBy: number | null
  createdByDisplayName: string | null
  canDelete: boolean
  createdAt: string
  updatedAt: string
}

export interface KyzzQuestionBankAdminDashboard {
  stats: KyzzQuestionBankAdminStats
  banks: KyzzQuestionBankAdminItem[]
  categories: KyzzCategoryOption[]
}

export interface KyzzQuestionBankOption {
  id: number
  bankCode: string
  bankName: string
  categoryId: number | null
  categoryName: string | null
  status: number
}

export interface KyzzQuestionAdminOption {
  id: number | null
  optionKey: string
  optionContent: string
  isCorrect: number
  sortNo: number
}

export interface KyzzQuestionAdminStats {
  totalQuestions: number
  activeQuestions: number
  inactiveQuestions: number
  singleChoiceQuestions: number
  multipleChoiceQuestions: number
  shortAnswerQuestions: number
}

export interface KyzzQuestionAdminPagination {
  pageNo: number
  pageSize: number
  total: number
  totalPages: number
}

export interface KyzzQuestionAdminItem {
  id: number
  questionBankId: number
  questionBankName: string | null
  categoryId: number | null
  categoryName: string | null
  questionType: string
  difficultyLevel: number
  score: number
  sourceName: string | null
  yearNo: number | null
  sortNo: number
  status: number
  stem: string
  stemPreview: string
  analysis: string | null
  answerText: string | null
  correctOptionKeys: string[]
  optionCount: number
  canDelete: boolean
  deleteBlockReason: string | null
  createdAt: string
  updatedAt: string
}

export interface KyzzQuestionAdminDetail extends KyzzQuestionAdminItem {
  options: KyzzQuestionAdminOption[]
}

export interface KyzzQuestionAdminDashboard {
  stats: KyzzQuestionAdminStats
  records: KyzzQuestionAdminItem[]
  pagination: KyzzQuestionAdminPagination
  questionBanks: KyzzQuestionBankOption[]
  categories: KyzzCategoryOption[]
}
