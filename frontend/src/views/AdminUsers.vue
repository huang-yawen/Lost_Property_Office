<template>
    <div class="admin-users-page">
        <div class="admin-users-header">
            <h1>用户列表</h1>
            <div class="header-info">
                <span class="user-count">共 {{ list.length }} 个用户</span>
            </div>
        </div>

        <div class="admin-users-body">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
                <div class="loading-spinner"></div>
                <p>加载中...</p>
            </div>

            <!-- 空状态 -->
            <div v-else-if="list.length === 0" class="empty-state">
                <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#c0c4cc" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                    <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
                <p>暂无用户数据</p>
            </div>

            <!-- 用户列表 -->
            <div v-else class="users-table-wrapper">
                <table class="users-table">
                    <thead>
                        <tr>
                            <th>用户名</th>
                            <th>校园认证</th>
                            <th>当前积分</th>
                            <th>注册时间</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="user in list" :key="user.id">
                            <td>
                                <div class="user-cell">
                                    <div class="user-avatar-sm">
                                        {{ user.username?.charAt(0)?.toUpperCase() || '?' }}
                                    </div>
                                    <span class="user-name">{{ user.username }}</span>
                                </div>
                            </td>
                            <td>
                                <span class="cert-badge" :class="'cert-' + (user.certificationStatus || (user.is_certified ? 'approved' : 'none'))">
                                    {{ certStatusText(user) }}
                                </span>
                            </td>
                            <td>
                                <span class="points-value">{{ user.points }}</span>
                            </td>
                            <td>
                                <span class="time-text">{{ formatTime(user.created_at) }}</span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../api'

const list = ref([])
const loading = ref(false)

function certStatusText(user) {
    const status = user.certificationStatus
    if (status === 'approved' || user.is_certified) return '已认证'
    if (status === 'pending') return '待审核'
    if (status === 'rejected') return '已拒绝'
    return '未认证'
}

function formatTime(time) {
    if (!time) return ''
    const date = new Date(time)
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    const h = String(date.getHours()).padStart(2, '0')
    const min = String(date.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${d} ${h}:${min}`
}

async function fetchUsers() {
    loading.value = true
    try {
        const res = await adminApi.getUsers()
        list.value = res.data || []
    } catch (error) {
        console.error('获取用户列表失败:', error)
        ElMessage.error('获取用户列表失败')
        list.value = []
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    fetchUsers()
})
</script>

<style scoped>
.admin-users-page {
    min-height: 100%;
}

.admin-users-header {
    padding: 0 0 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.admin-users-header h1 {
    font-size: 20px;
    font-weight: 700;
    color: #4a4a6a;
    margin: 0;
}

.header-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.user-count {
    font-size: 13px;
    color: #a0aec0;
    background: #f0f2f5;
    padding: 4px 12px;
    border-radius: 6px;
}

.admin-users-body {
    /* 全宽显示 */
}

/* 加载状态 */
.loading-state {
    text-align: center;
    padding: 60px 0;
    color: #8a8aa0;
}

.loading-spinner {
    width: 32px;
    height: 32px;
    border: 3px solid #e2e8f0;
    border-top-color: #4f46e5;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin: 0 auto 12px;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
    text-align: center;
    padding: 60px 0;
    color: #a0aec0;
}

.empty-state p {
    margin-top: 12px;
    font-size: 14px;
}

/* 表格 - 全宽 */
.users-table-wrapper {
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    overflow: hidden;
}

.users-table {
    width: 100%;
    border-collapse: collapse;
}

.users-table thead {
    background: #f8f9fc;
}

.users-table th {
    padding: 14px 20px;
    text-align: left;
    font-size: 13px;
    font-weight: 600;
    color: #8a8aa0;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    border-bottom: 1px solid #e2e8f0;
}

.users-table td {
    padding: 14px 20px;
    font-size: 14px;
    color: #4a5568;
    border-bottom: 1px solid #f0f2f5;
}

.users-table tbody tr:last-child td {
    border-bottom: none;
}

.users-table tbody tr:hover {
    background: #f8f9fc;
}

/* 用户单元格 */
.user-cell {
    display: flex;
    align-items: center;
    gap: 10px;
}

.user-avatar-sm {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4f46e5, #6366f1);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    font-weight: 600;
    flex-shrink: 0;
}

.user-name {
    font-weight: 500;
    color: #4a4a6a;
}

/* 认证标识 */
.cert-badge {
    display: inline-flex;
    padding: 3px 10px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
}

.cert-approved {
    background: #d1fae5;
    color: #059669;
}

.cert-pending {
    background: #fef3c7;
    color: #d97706;
}

.cert-rejected {
    background: #fee2e2;
    color: #dc2626;
}

.cert-none {
    background: #f0f2f5;
    color: #8a8aa0;
}

/* 积分 */
.points-value {
    font-weight: 600;
    color: #4f46e5;
}

/* 时间 */
.time-text {
    font-size: 13px;
    color: #a0aec0;
}

/* 暗黑模式 */
html.dark .admin-users-header h1 {
    color: #e0e0f0;
}

html.dark .user-count {
    background: #2a2a3e;
    color: #8a8aa0;
}

html.dark .users-table-wrapper {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .users-table thead {
    background: #0f0f1a;
}

html.dark .users-table th {
    color: #8a8aa0;
    border-bottom-color: #2a2a3e;
}

html.dark .users-table td {
    color: #c8c8d8;
    border-bottom-color: #2a2a3e;
}

html.dark .users-table tbody tr:hover {
    background: #22223a;
}

html.dark .user-name {
    color: #e0e0f0;
}

html.dark .cert-none {
    background: #2a2a3e;
    color: #8a8aa0;
}
</style>