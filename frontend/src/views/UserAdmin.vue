<template>
  <div class="user-admin">
    <div class="page-header">
      <h2>用户管理</h2>
      <button class="add-btn" @click="showAddModal">添加用户</button>
    </div>

    <div class="search-bar">
      <input 
        v-model="searchForm.username" 
        type="text" 
        placeholder="用户名" 
        class="search-input"
      />
      <input 
        v-model="searchForm.nickname" 
        type="text" 
        placeholder="昵称" 
        class="search-input"
      />
      <select v-model="searchForm.roleType" class="search-select">
        <option :value="undefined">全部角色</option>
        <option :value="0">普通用户</option>
        <option :value="1">管理员</option>
      </select>
      <button class="search-btn" @click="handleSearch">搜索</button>
      <button class="reset-btn" @click="handleReset">重置</button>
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
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in userList" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.nickname || '-' }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>{{ user.phone || '-' }}</td>
            <td>
              <span :class="['role-tag', user.roleType === 1 ? 'admin' : 'user']">
                {{ user.roleType === 1 ? '管理员' : '普通用户' }}
              </span>
            </td>
            <td>
              <span :class="['status-tag', user.status === 1 ? 'active' : 'inactive']">
                {{ user.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>{{ formatDate(user.createTime) }}</td>
            <td>
              <button 
                v-if="user.roleType !== 1" 
                class="delete-btn" 
                @click="handleDelete(user)"
              >
                删除
              </button>
              <span v-else class="no-action">-</span>
            </td>
          </tr>
          <tr v-if="userList.length === 0">
            <td colspan="9" class="empty-text">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="total > 0">
      <button 
        class="page-btn" 
        :disabled="currentPage === 1" 
        @click="handlePageChange(currentPage - 1)"
      >
        上一页
      </button>
      <span class="page-info">第 {{ currentPage }} 页 / 共 {{ totalPages }} 页 (共 {{ total }} 条)</span>
      <button 
        class="page-btn" 
        :disabled="currentPage >= totalPages" 
        @click="handlePageChange(currentPage + 1)"
      >
        下一页
      </button>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-group">
            <label>用户名 *</label>
            <input 
              v-model="addForm.username" 
              type="text" 
              placeholder="3-20个字符" 
              required 
              minlength="3"
              maxlength="20"
            />
          </div>
          <div class="form-group">
            <label>密码 *</label>
            <input 
              v-model="addForm.password" 
              type="password" 
              placeholder="6-20个字符" 
              required 
              minlength="6"
              maxlength="20"
            />
          </div>
          <div class="form-group">
            <label>昵称</label>
            <input 
              v-model="addForm.nickname" 
              type="text" 
              placeholder="请输入昵称（可选）" 
            />
          </div>
          <div class="form-group">
            <label>邮箱</label>
            <input 
              v-model="addForm.email" 
              type="email" 
              placeholder="请输入邮箱（可选）" 
            />
          </div>
          <div class="form-group">
            <label>手机号</label>
            <input 
              v-model="addForm.phone" 
              type="tel" 
              placeholder="请输入手机号（可选）" 
            />
          </div>
          <div class="form-group">
            <label>角色</label>
            <select v-model="addForm.roleType">
              <option :value="0">普通用户</option>
              <option :value="1">管理员</option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeModal">取消</button>
            <button type="submit" class="submit-btn" :disabled="submitting">
              {{ submitting ? '提交中...' : '确定' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="message.show" :class="['toast', message.type]">
      {{ message.text }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { userApi } from '@/api/user'
import type { UserListItem, UserQuery, UserAddRequest } from '@/types/user'

const userList = ref<UserListItem[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const showModal = ref(false)
const submitting = ref(false)

const searchForm = reactive<UserQuery>({
  username: '',
  nickname: '',
  roleType: undefined
})

const addForm = reactive<UserAddRequest>({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  roleType: 0
})

const message = reactive({
  show: false,
  type: 'success',
  text: ''
})

const modalTitle = computed(() => '添加用户')

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const showMessage = (text: string, type: 'success' | 'error' = 'success') => {
  message.text = text
  message.type = type
  message.show = true
  setTimeout(() => {
    message.show = false
  }, 3000)
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchUserList = async () => {
  try {
    const result = await userApi.getUserList({
      ...searchForm,
      page: currentPage.value,
      pageSize: pageSize.value
    })
    if (result.success && result.data) {
      userList.value = result.data.records
      total.value = result.data.total
    } else {
      showMessage(result.message || '获取用户列表失败', 'error')
    }
  } catch (e) {
    showMessage('获取用户列表失败', 'error')
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.nickname = ''
  searchForm.roleType = undefined
  currentPage.value = 1
  fetchUserList()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchUserList()
}

const showAddModal = () => {
  addForm.username = ''
  addForm.password = ''
  addForm.nickname = ''
  addForm.email = ''
  addForm.phone = ''
  addForm.roleType = 0
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const handleSubmit = async () => {
  if (!addForm.username || !addForm.password) {
    showMessage('请填写必填项', 'error')
    return
  }

  submitting.value = true
  try {
    const result = await userApi.addUser(addForm)
    if (result.success) {
      showMessage('添加成功')
      closeModal()
      fetchUserList()
    } else {
      showMessage(result.message || '添加失败', 'error')
    }
  } catch (e) {
    showMessage('添加失败', 'error')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (user: UserListItem) => {
  if (!confirm(`确定要删除用户 "${user.username}" 吗？`)) {
    return
  }

  try {
    const result = await userApi.deleteUser(user.id)
    if (result.success) {
      showMessage('删除成功')
      fetchUserList()
    } else {
      showMessage(result.message || '删除失败', 'error')
    }
  } catch (e) {
    showMessage('删除失败', 'error')
  }
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-admin {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.add-btn {
  padding: 8px 20px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.add-btn:hover {
  background: #40a9ff;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 150px;
}

.search-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: #fff;
}

.search-btn {
  padding: 8px 20px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.reset-btn {
  padding: 8px 20px;
  background: #fff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.user-table {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  background: #fafafa;
  font-weight: 600;
  color: #333;
}

td {
  color: #666;
}

.empty-text {
  text-align: center;
  color: #999;
  padding: 40px;
}

.role-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.role-tag.user {
  background: #f0f0f0;
  color: #666;
}

.role-tag.admin {
  background: #e6f7ff;
  color: #1890ff;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.status-tag.active {
  background: #f6ffed;
  color: #52c41a;
}

.status-tag.inactive {
  background: #fff2f0;
  color: #ff4d4f;
}

.delete-btn {
  padding: 4px 12px;
  background: #ff4d4f;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.delete-btn:hover {
  background: #ff7875;
}

.no-action {
  color: #ccc;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 20px;
}

.page-btn {
  padding: 6px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
}

.page-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 8px;
  width: 400px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
}

.modal-form {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #333;
  font-size: 14px;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cancel-btn {
  padding: 8px 20px;
  background: #fff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn {
  padding: 8px 20px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 24px;
  border-radius: 4px;
  color: #fff;
  font-size: 14px;
  z-index: 2000;
}

.toast.success {
  background: #52c41a;
}

.toast.error {
  background: #ff4d4f;
}
</style>
