import axios from 'axios'
import type { Result, User } from '@/types/user'

const api = axios.create({
  baseURL: '/api/admin',
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

export interface AddUserRequest {
  username: string
  password: string
  nickname?: string
  email?: string
  phone?: string
  roleType?: number
}

export const adminApi = {
  getUserList: async (): Promise<Result<User[]>> => {
    const response = await api.get('/users/list')
    return response.data
  },

  addUser: async (data: AddUserRequest): Promise<Result<User>> => {
    const response = await api.post('/users/add', data)
    return response.data
  },

  deleteUser: async (userId: number): Promise<Result<boolean>> => {
    const response = await api.delete(`/users/delete/${userId}`)
    return response.data
  },

  getUserById: async (userId: number): Promise<Result<User>> => {
    const response = await api.get(`/users/${userId}`)
    return response.data
  }
}

export default api
