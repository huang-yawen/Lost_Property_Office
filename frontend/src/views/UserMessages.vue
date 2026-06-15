<template>
    <div class="messages-page">
        <div class="messages-container">
            <div class="page-header">
                <h1 class="page-title">我的消息</h1>
                <p class="page-desc">查看收到的留言和私信</p>
            </div>

            <!-- 标签切换 -->
            <div class="tabs">
                <button
                    class="tab"
                    :class="{ active: activeTab === 'received' }"
                    @click="activeTab = 'received'"
                >
                    收到的消息
                    <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
                </button>
                <button
                    class="tab"
                    :class="{ active: activeTab === 'sent' }"
                    @click="activeTab = 'sent'"
                >
                    发送的消息
                </button>
            </div>

            <!-- 加载状态 -->
            <div v-if="loading" class="loading-section">
                <div class="skeleton-list">
                    <div v-for="i in 3" :key="i" class="skeleton-item">
                        <div class="skeleton-avatar"></div>
                        <div class="skeleton-content">
                            <div class="skeleton-line w-60"></div>
                            <div class="skeleton-line w-80"></div>
                            <div class="skeleton-line w-40"></div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 空状态 -->
            <div v-else-if="messages.length === 0" class="empty-section">
                <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="#c0c4cc" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <p>{{ activeTab === 'received' ? '暂无收到的消息' : '暂无发送的消息' }}</p>
            </div>

            <!-- 消息列表 -->
            <div v-else class="messages-list">
                <div
                    v-for="msg in messages"
                    :key="msg.id"
                    class="message-card"
                    :class="{ unread: !msg.isRead && activeTab === 'received' }"
                    @click="handleMessageClick(msg)"
                >
                    <div class="message-avatar">
                        <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                            <circle cx="12" cy="7" r="4"/>
                        </svg>
                    </div>
                    <div class="message-content">
                        <div class="message-header">
                            <span class="message-sender">{{ activeTab === 'received' ? msg.senderName : msg.receiverName }}</span>
                            <span class="message-time">{{ formatTime(msg.createTime) }}</span>
                        </div>
                        <p class="message-text">{{ msg.content }}</p>
                        <div class="message-footer">
                            <span class="message-item">关于：{{ msg.itemTitle || '物品' }}</span>
                            <span v-if="!msg.isRead && activeTab === 'received'" class="message-status">未读</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { messageApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const messages = ref([])
const activeTab = ref('received')
const unreadCount = ref(0)

function formatTime(time) {
    if (!time) return ''
    const date = new Date(time)
    const now = new Date()
    const diff = now - date
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)

    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
}

async function fetchMessages() {
    loading.value = true
    try {
        let res
        if (activeTab.value === 'received') {
            res = await messageApi.getMyMessages()
        } else {
            res = await messageApi.getSentMessages()
        }
        messages.value = res.data || []
    } catch (error) {
        console.error('获取消息列表失败:', error)
        ElMessage.error('获取消息列表失败')
    } finally {
        loading.value = false
    }
}

async function fetchUnreadCount() {
    try {
        const res = await messageApi.getUnreadCount()
        unreadCount.value = res.data || 0
    } catch (error) {
        console.error('获取未读消息数失败:', error)
    }
}

async function handleMessageClick(msg) {
    if (activeTab.value === 'received' && !msg.isRead) {
        try {
            await messageApi.markRead(msg.id)
            msg.isRead = true
            unreadCount.value = Math.max(0, unreadCount.value - 1)
        } catch (error) {
            console.error('标记已读失败:', error)
        }
    }
    if (msg.itemId) {
        router.push(`/item/${msg.itemId}`)
    }
}

watch(activeTab, () => {
    fetchMessages()
})

onMounted(() => {
    if (!userStore.isLoggedIn) {
        router.push('/login')
        return
    }
    fetchUnreadCount()
    fetchMessages()
})
</script>

<style scoped>
.messages-page {
    min-height: 100vh;
    background: #f0f2f5;
}

.messages-container {
    max-width: 720px;
    margin: 0 auto;
    padding: 84px 24px 60px;
}

.page-header {
    margin-bottom: 24px;
}

.page-title {
    font-size: 22px;
    font-weight: 700;
    color: #2c3e50;
    margin-bottom: 8px;
}

.page-desc {
    font-size: 14px;
    color: #718096;
}

/* 标签切换 */
.tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 24px;
}

.tab {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 24px;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    background: #fff;
    font-size: 14px;
    font-weight: 500;
    color: #4a5568;
    cursor: pointer;
    transition: all 0.25s;
}

.tab:hover {
    border-color: #7aa3d0;
    color: #1a3a5c;
}

.tab.active {
    background: #1a3a5c;
    border-color: #1a3a5c;
    color: #fff;
}

.unread-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    border-radius: 10px;
    background: #e74c3c;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
}

/* 加载状态 */
.loading-section {
    background: #fff;
    border-radius: 16px;
    border: 1px solid #e2e8f0;
    overflow: hidden;
}

.skeleton-list {
    display: flex;
    flex-direction: column;
}

.skeleton-item {
    display: flex;
    gap: 16px;
    padding: 20px 24px;
}

.skeleton-item + .skeleton-item {
    border-top: 1px solid #f0f2f5;
}

.skeleton-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: linear-gradient(90deg, #f0f2f5 25%, #e2e8f0 50%, #f0f2f5 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    flex-shrink: 0;
}

.skeleton-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 10px;
    justify-content: center;
}

.skeleton-line {
    height: 14px;
    background: linear-gradient(90deg, #f0f2f5 25%, #e2e8f0 50%, #f0f2f5 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4px;
}

.skeleton-line.w-60 { width: 60%; }
.skeleton-line.w-80 { width: 80%; }
.skeleton-line.w-40 { width: 40%; }

@keyframes shimmer {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}

/* 空状态 */
.empty-section {
    text-align: center;
    padding: 80px 0;
    background: #fff;
    border-radius: 16px;
    border: 1px solid #e2e8f0;
}

.empty-section svg {
    margin-bottom: 16px;
}

.empty-section p {
    font-size: 16px;
    color: #4a5568;
}

/* 消息列表 */
.messages-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.message-card {
    display: flex;
    gap: 16px;
    padding: 20px 24px;
    background: #fff;
    border-radius: 12px;
    border: 1px solid #e2e8f0;
    cursor: pointer;
    transition: all 0.25s;
}

.message-card:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    border-color: #c0c4cc;
}

.message-card.unread {
    background: #f0f5fa;
    border-color: #d4e4f3;
}

.message-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: #d4e4f3;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #1a3a5c;
    flex-shrink: 0;
}

.message-content {
    flex: 1;
    min-width: 0;
}

.message-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 6px;
}

.message-sender {
    font-size: 14px;
    font-weight: 600;
    color: #2c3e50;
}

.message-time {
    font-size: 12px;
    color: #a0aec0;
}

.message-text {
    font-size: 14px;
    color: #4a5568;
    line-height: 1.6;
    margin-bottom: 8px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.message-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.message-item {
    font-size: 12px;
    color: #7aa3d0;
}

.message-status {
    font-size: 11px;
    color: #e74c3c;
    font-weight: 500;
    padding: 2px 8px;
    background: #fdf2f2;
    border-radius: 4px;
}
</style>