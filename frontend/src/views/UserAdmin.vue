<template>
  <div class="user-admin">
    <div class="header">
      <h2>用户管理</h2>
      <button class="btn-add" @click="openAddModal">添加用户</button>
    </div>

    <div class="user-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>昵称</th>
            <th>邮箱</th>
            <th>手机号</th>
            <th>角色</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in userList" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.nickname }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>
              <span :class="['role-tag', user.roleType === 1 ? 'admin' : 'user']">
                {{ user.roleType === 1 ? '管理员' : '普通用户' }}
              </span>
            </td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td>
              <button 
                class="btn-delete" 
                @click="deleteUser(user.id)"
                :disabled="user.username === 'admin'"
              >
                删除
              </button>
            </td>
          </tr>
          <tr v-if="userList.length === 0">
            <td colspan="8" class="no-data">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 添加用户弹窗 -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="closeAddModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>添加用户</h3>
          <button class="btn-close" @click="closeAddModal">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitAddUser">
            <div class="form-group">
              <label>用户名 <span class="required">*</span></label>
              <input 
                v-model="addForm.username" 
                type="text" 
                placeholder="请输入用户名"
                required
              />
            </div>
            <div class="form-group">
              <label>密码 <span class="required">*</span></label>
              <input 
                v-model="addForm.password" 
                type="password" 
                placeholder="请输入密码"
                required
              />
            </div>
            <div class="form-group">
              <label>昵称</label>
              <input 
                v-model="addForm.nickname" 
                type="text" 
                placeholder="请输入昵称"
              />
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input 
                v-model="addForm.email" 
                type="email" 
                placeholder="请输入邮箱"
              />
            </div>
            <div class="form-group">
              <label>手机号</label>
              <input 
                v-model="addForm.phone" 
                type="text" 
                placeholder="请输入手机号"
              />
            </div>
            <div class="form-group">
              <label>角色</label>
              <select v-model="addForm.roleType">
                <option :value="0">普通用户</option>
                <option :value="1">管理员</option>
              </select>
            </div>
            <div class="form-actions">
              <button type="button" class="btn-cancel" @click="closeAddModal">取消</button>
              <button type="submit" class="btn-submit">确定</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-if="error" class="error-message">{{ error }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi, type AddUserRequest } from '@/api/admin'
import type { User } from '@/types/user'

const userList = ref<User[]>([])
const loading = ref(false)
const error = ref('')
const showAddModal = ref(false)

const addForm = ref<AddUserRequest>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  roleType: 0
})

const formatDate = (date: string | Date | undefined) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadUserList = async () => {
  loading.value = true
  error.value = ''
  try {
    const result = await adminApi.getUserList()
    if (result.success) {
      userList.value = result.data || []
    } else {
      error.value = result.message || '加载用户列表失败'
    }
  } catch (err) {
    error.value = '网络错误，请稍后重试'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const openAddModal = () => {
  showAddModal.value = true
  resetAddForm()
}

const closeAddModal = () => {
  showAddModal.value = false
  resetAddForm()
}

const resetAddForm = () => {
  addForm.value = {
    username: '',
    password: '',
    nickname: '',
    email: '',
    phone: '',
    roleType: 0
  }
}

const submitAddUser = async () => {
  if (!addForm.value.username || !addForm.value.password) {
    alert('请填写必填项')
    return
  }

  try {
    const result = await adminApi.addUser(addForm.value)
    if (result.success) {
      alert('添加成功')
      closeAddModal()
      await loadUserList()
    } else {
      alert(result.message || '添加失败')
    }
  } catch (err) {
    alert('网络错误，请稍后重试')
    console.error(err)
  }
}

const deleteUser = async (userId: number) => {
  if (!confirm('确定要删除该用户吗？')) {
    return
  }

  try {
    const result = await adminApi.deleteUser(userId)
    if (result.success) {
      alert('删除成功')
      await loadUserList()
    } else {
      alert(result.message || '删除失败')
    }
  } catch (err) {
    alert('网络错误，请稍后重试')
    console.error(err)
  }
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-admin {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #333;
}

.btn-add {
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-add:hover {
  background-color: #40a9ff;
}

.user-table {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

th {
  background-color: #fafafa;
  font-weight: 600;
  color: #333;
}

tbody tr:hover {
  background-color: #f5f5f5;
}

.role-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.role-tag.admin {
  background-color: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.role-tag.user {
  background-color: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.btn-delete {
  padding: 4px 12px;
  background-color: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.btn-delete:hover:not(:disabled) {
  background-color: #ff7875;
}

.btn-delete:disabled {
  background-color: #d9d9d9;
  cursor: not-allowed;
}

.no-data {
  text-align: center;
  color: #999;
  padding: 40px !important;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  color: #333;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
}

.form-group .required {
  color: #ff4d4f;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.btn-cancel {
  padding: 8px 16px;
  background-color: #fff;
  color: #333;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.btn-submit {
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-submit:hover {
  background-color: #40a9ff;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

.error-message {
  text-align: center;
  padding: 20px;
  color: #ff4d4f;
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 4px;
  margin-bottom: 20px;
}
</style>
