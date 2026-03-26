import axios from 'axios'
import type { 
  User, 
  LoginRequest, 
  RegisterRequest, 
  Result, 
  UserListItem, 
  UserQuery, 
  UserAddRequest, 
  PageResult 
} from '@/types/user'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export const authApi = {
  login: async (data: LoginRequest): Promise<Result<User>> => {
    const response = await api.post('/auth/login', data)
    return response.data
  },

  register: async (data: RegisterRequest): Promise<Result<User>> => {
    const response = await api.post('/auth/register', data)
    return response.data
  },

  getUserInfo: async (): Promise<Result<User>> => {
    const response = await api.get('/auth/info')
    return response.data
  },

  checkUsername: async (username: string): Promise<Result<boolean>> => {
    const response = await api.get('/auth/check-username', { params: { username } })
    return response.data
  }
}

export const userApi = {
  getUserList: async (params: UserQuery): Promise<Result<PageResult<UserListItem>>> => {
    const response = await api.get('/admin/user/list', { params })
    return response.data
  },

  getUserById: async (id: number): Promise<Result<UserListItem>> => {
    const response = await api.get(`/admin/user/${id}`)
    return response.data
  },

  addUser: async (data: UserAddRequest): Promise<Result<UserListItem>> => {
    const response = await api.post('/admin/user/add', data)
    return response.data
  },

  deleteUser: async (id: number): Promise<Result<boolean>> => {
    const response = await api.delete(`/admin/user/${id}`)
    return response.data
  }
}

export default api
