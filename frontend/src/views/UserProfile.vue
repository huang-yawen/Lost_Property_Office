<template>
    <div class="profile-page">
        <div class="profile-container">
            <div class="profile-card">
                <div class="profile-header">
                    <div class="profile-avatar">
                        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                            <circle cx="12" cy="7" r="4"/>
                        </svg>
                    </div>
                    <div class="profile-info">
                        <h1 class="profile-name">{{ userInfo?.username || '未登录' }}</h1>
                        <p class="profile-email">{{ userInfo?.email || '暂无邮箱' }}</p>
                    </div>
                </div>

                <div class="profile-stats">
                    <div class="stat-item">
                        <span class="stat-value">{{ stats.total }}</span>
                        <span class="stat-label">全部</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-value">{{ stats.found }}</span>
                        <span class="stat-label">招领</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-value">{{ stats.lost }}</span>
                        <span class="stat-label">寻物</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-value">{{ stats.resolved }}</span>
                        <span class="stat-label">已解决</span>
                    </div>
                </div>
            </div>

            <div class="profile-nav">
                <router-link to="/my-items" class="nav-item">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="3" width="7" height="7"/>
                        <rect x="14" y="3" width="7" height="7"/>
                        <rect x="14" y="14" width="7" height="7"/>
                        <rect x="3" y="14" width="7" height="7"/>
                    </svg>
                    <div class="nav-text">
                        <span class="nav-title">我的发布</span>
                        <span class="nav-desc">管理您发布的招领和寻物信息</span>
                    </div>
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="nav-arrow">
                        <polyline points="9 18 15 12 9 6"/>
                    </svg>
                </router-link>

                <router-link to="/messages" class="nav-item">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                    </svg>
                    <div class="nav-text">
                        <span class="nav-title">我的消息</span>
                        <span class="nav-desc">查看收到的留言和私信</span>
                    </div>
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="nav-arrow">
                        <polyline points="9 18 15 12 9 6"/>
                    </svg>
                </router-link>
            </div>

            <div class="profile-card profile-actions">
                <button class="action-item" @click="handleLogout">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#c0392b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                        <polyline points="16 17 21 12 16 7"/>
                        <line x1="21" y1="12" x2="9" y2="12"/>
                    </svg>
                    <span class="action-text">退出登录</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { itemApi } from '../api'

const router = useRouter()
const userStore = useUserStore()

const userInfo = ref(null)
const stats = ref({
    total: 0,
    found: 0,
    lost: 0,
    resolved: 0
})

async function fetchStats() {
    try {
        const res = await itemApi.getMyItems()
        const items = res.data || []
        stats.value.total = items.length
        stats.value.found = items.filter(i => i.type === 1).length
        stats.value.lost = items.filter(i => i.type === 2).length
        stats.value.resolved = items.filter(i => i.status === 2).length
    } catch (error) {
        console.error('获取统计数据失败:', error)
    }
}

async function handleLogout() {
    try {
        await ElMessageBox.confirm('确定要退出登录吗？', '确认退出', {
            confirmButtonText: '退出',
            cancelButtonText: '取消',
            type: 'warning'
        })
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/')
    } catch (error) {
        if (error !== 'cancel') {
            console.error('退出失败:', error)
        }
    }
}

onMounted(() => {
    if (!userStore.isLoggedIn) {
        router.push('/login')
        return
    }
    userInfo.value = userStore.userInfo
    fetchStats()
})
</script>

<style scoped>
.profile-page {
    min-height: 100vh;
    background: #f0f2f5;
}

.profile-container {
    max-width: 640px;
    margin: 0 auto;
    padding: 84px 24px 60px;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.profile-card {
    background: #fff;
    border-radius: 16px;
    border: 1px solid #e2e8f0;
    overflow: hidden;
}

.profile-header {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 32px;
}

.profile-avatar {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: linear-gradient(135deg, #1a3a5c, #4a7fb5);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
}

.profile-info {
    flex: 1;
    min-width: 0;
}

.profile-name {
    font-size: 20px;
    font-weight: 700;
    color: #2c3e50;
    margin-bottom: 4px;
}

.profile-email {
    font-size: 14px;
    color: #718096;
}

.profile-stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    border-top: 1px solid #f0f2f5;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 12px;
    gap: 4px;
}

.stat-item + .stat-item {
    border-left: 1px solid #f0f2f5;
}

.stat-value {
    font-size: 22px;
    font-weight: 700;
    color: #1a3a5c;
}

.stat-label {
    font-size: 12px;
    color: #a0aec0;
}

/* 导航列表 */
.profile-nav {
    display: flex;
    flex-direction: column;
    gap: 1px;
    background: #e2e8f0;
    border-radius: 16px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px 24px;
    background: #fff;
    text-decoration: none;
    transition: background 0.2s;
}

.nav-item:hover {
    background: #f7fafc;
}

.nav-item svg:first-child {
    color: #1a3a5c;
    flex-shrink: 0;
}

.nav-text {
    flex: 1;
    min-width: 0;
}

.nav-title {
    display: block;
    font-size: 15px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 2px;
}

.nav-desc {
    display: block;
    font-size: 12px;
    color: #a0aec0;
}

.nav-arrow {
    color: #c0c4cc;
    flex-shrink: 0;
}

/* 操作按钮 */
.profile-actions {
    padding: 0;
}

.action-item {
    display: flex;
    align-items: center;
    gap: 12px;
    width: 100%;
    padding: 20px 24px;
    border: none;
    background: none;
    cursor: pointer;
    transition: background 0.2s;
    font-size: 15px;
}

.action-item:hover {
    background: #fdf2f2;
}

.action-text {
    color: #c0392b;
    font-weight: 500;
}
</style>