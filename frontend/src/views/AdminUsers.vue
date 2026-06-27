<template>
    <div class="admin-users-page">
        <div class="page-header-bar">
            <h1 class="page-header-title">用户列表</h1>
        </div>

        <div class="admin-users-body">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
                <div class="loading-spinner"></div>
                <p>加载中...</p>
            </div>

            <!-- 空状态 -->
            <div v-else-if="list.length === 0" class="empty-state">
                <p>暂无用户数据</p>
            </div>

            <!-- 用户表格 -->
            <div v-else class="users-table-wrapper">
                <table class="users-table">
                    <thead>
                        <tr>
                            <th>用户</th>
                            <th>昵称</th>
                            <th>认证状态</th>
                            <th>信誉积分</th>
                            <th>注册时间</th>
                            <th style="width: 100px;">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="user in list" :key="user.id">
                            <td>
                                <div class="user-cell">
                                    <div class="user-avatar-sm">
                                        {{ (user.nickname || user.username || '?').charAt(0).toUpperCase() }}
                                    </div>
                                    <span class="user-name">{{ user.username }}</span>
                                </div>
                            </td>
                            <td>{{ user.nickname || '-' }}</td>
                            <td>
                                <span class="cert-badge" :class="user.is_certified === 1 ? 'cert-approved' : 'cert-none'">
                                    {{ user.is_certified === 1 ? '已认证' : '未认证' }}
                                </span>
                            </td>
                            <td>
                                <span class="points-value">{{ user.reputation ?? 0 }}</span>
                            </td>
                            <td>
                                <span class="time-text">{{ formatTime(user.created_at) }}</span>
                            </td>
                            <td>
                                <button
                                    v-if="user.is_admin !== 1"
                                    class="kick-btn"
                                    @click="handleKick(user)"
                                    title="踢出该用户"
                                >
                                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                                        <polyline points="16 17 21 12 16 7"/>
                                        <line x1="21" y1="12" x2="9" y2="12"/>
                                    </svg>
                                    踢出
                                </button>
                                <span v-else class="admin-badge">管理员</span>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../api'

const list = ref([])
const loading = ref(false)

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

async function handleKick(user) {
    try {
        await ElMessageBox.confirm(
            `确定要将用户 "${user.nickname || user.username}" 踢出系统？\n\n操作将：\n1. 删除该用户账号\n2. 下架该用户所有发布物品\n3. 删除该用户的消息记录\n\n此操作不可撤销！`,
            '踢出用户',
            {
                confirmButtonText: '确认踢出',
                cancelButtonText: '取消',
                type: 'warning',
                confirmButtonClass: 'el-button--danger'
            }
        )
        await adminApi.deleteUser(user.id)
        ElMessage.success(`已踢出用户 "${user.nickname || user.username}"`)
        fetchUsers()
    } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
            console.error('踢出失败:', error)
            const msg = error?.response?.data?.message || error?.message || '踢出失败，请稍后重试'
            ElMessage.error(typeof msg === 'string' ? msg : '踢出失败')
        }
    }
}

onMounted(() => {
    fetchUsers()
})
</script>

<style scoped>
/* ========== 页面标题栏 ========== */
.page-header-bar {
    padding-bottom: 16px;
    border-bottom: 1px solid #e2e8f0;
    margin-bottom: 20px;
}

.page-header-title {
    font-size: 18px;
    font-weight: 700;
    color: #4a4a6a;
    margin: 0;
}

.admin-users-page {
    min-height: 100%;
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

/* 踢出按钮 */
.kick-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 5px 12px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #fee2e2;
    background: #fff;
    color: #dc2626;
    white-space: nowrap;
}

.kick-btn:hover {
    background: #fee2e2;
    border-color: #dc2626;
}

/* 管理员标识 */
.admin-badge {
    display: inline-flex;
    padding: 3px 10px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    background: #eef2ff;
    color: #4f46e5;
}

/* 暗黑模式 */
html.dark .page-header-bar {
    border-bottom-color: #2a2a3e;
}

html.dark .page-header-title {
    color: #e0e0f0;
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

html.dark .kick-btn {
    background: #1a1a2e;
    border-color: #3a2a2a;
    color: #f87171;
}

html.dark .kick-btn:hover {
    background: #2a1a1a;
    border-color: #dc2626;
}

html.dark .admin-badge {
    background: #2e2e4a;
    color: #a5b4fc;
}
</style>