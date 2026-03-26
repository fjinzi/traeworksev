export interface User {
  id: number
  username: string
  nickname: string
  email: string | null
  phone: string | null
  roleType: number
  token?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
}

export interface AuthResult {
  success: boolean
  message: string
  data: User | null
}

export interface UserListItem {
  id: number
  username: string
  nickname: string
  email: string | null
  phone: string | null
  roleType: number
  status: number
  createTime: string
  updateTime: string
}

export interface UserQuery {
  username?: string
  nickname?: string
  roleType?: number
  status?: number
  page?: number
  pageSize?: number
}

export interface UserAddRequest {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
  roleType?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface Result<T> {
  success: boolean
  message: string
  data: T
}

export const RoleType = {
  USER: 0,
  ADMIN: 1
} as const

export const RoleTypeText: Record<number, string> = {
  0: '普通用户',
  1: '管理员'
}
