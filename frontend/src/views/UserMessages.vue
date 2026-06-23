<template>
    <div class="messages-page">
        <div class="chat-layout">
            <!-- 左侧：会话列表 -->
            <div class="chat-sidebar" :class="{ 'chat-sidebar-expanded': !selectedConversation }">
                <div class="sidebar-header">
                    <h2 class="sidebar-title">消息</h2>
                    <span class="sidebar-count" v-if="totalUnread > 0">{{ totalUnread }} 条未读</span>
                </div>
                <div class="sidebar-search">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#a0aec0" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="8"/>
                        <path d="M21 21l-4.35-4.35"/>
                    </svg>
                    <input type="text" placeholder="搜索消息..." v-model="searchKeyword" />
                </div>
                <div class="sidebar-tabs">
                    <button
                        class="sidebar-tab"
                        :class="{ active: activeTab === 'all' }"
                        @click="activeTab = 'all'"
                    >全部</button>
                    <button
                        class="sidebar-tab"
                        :class="{ active: activeTab === 'unread' }"
                        @click="activeTab = 'unread'"
                    >未读</button>
                </div>
                <div class="conversation-list" ref="conversationListRef">
                    <!-- 加载状态 -->
                    <div v-if="loading" class="loading-state">
                        <div v-for="i in 4" :key="i" class="conv-skeleton">
                            <div class="conv-skeleton-avatar"></div>
                            <div class="conv-skeleton-content">
                                <div class="conv-skeleton-line w-60"></div>
                                <div class="conv-skeleton-line w-40"></div>
                            </div>
                        </div>
                    </div>
                    <!-- 空状态 -->
                    <div v-else-if="filteredConversations.length === 0" class="empty-state">
                        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#c0c4cc" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                        </svg>
                        <p>暂无消息</p>
                    </div>
                    <!-- 会话列表 -->
                    <div
                        v-for="conv in filteredConversations"
                        :key="conv.id"
                        class="conversation-item"
                        :class="{ active: selectedConversation && selectedConversation.id === conv.id }"
                        @click="selectConversation(conv)"
                    >
                        <div class="conv-avatar" :class="{ 'conv-avatar-unread': conv.unread > 0 }">
                            <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                <circle cx="12" cy="7" r="4"/>
                            </svg>
                        </div>
                        <div class="conv-info">
                            <div class="conv-top">
                                <span class="conv-name">{{ conv.otherName }}</span>
                                <span class="conv-time">{{ formatTime(conv.lastTime) }}</span>
                            </div>
                            <div class="conv-bottom">
                                <span class="conv-preview">{{ conv.lastContent }}</span>
                                <span v-if="conv.unread > 0" class="conv-badge">{{ conv.unread > 99 ? '99+' : conv.unread }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 右侧：聊天区域 -->
            <div class="chat-main">
                <!-- 未选择会话 -->
                <div v-if="!selectedConversation" class="chat-welcome">
                    <div class="welcome-icon">
                        <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="#c0c4cc" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                        </svg>
                    </div>
                </div>

                <!-- 聊天界面 -->
                <div v-else class="chat-view" ref="chatViewRef">
                    <div class="chat-header">
                        <button class="chat-back-btn" @click="selectedConversation = null">
                            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="19" y1="12" x2="5" y2="12"/>
                                <polyline points="12 19 5 12 12 5"/>
                            </svg>
                        </button>
                        <div class="chat-header-info">
                            <span class="chat-header-name">{{ selectedConversation.otherName }}</span>
                            <span class="chat-header-item">关于：{{ selectedConversation.itemTitle }}</span>
                        </div>
                        <button class="chat-item-btn" @click="goToItem(selectedConversation.itemId)">
                            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/>
                                <polyline points="15 3 21 3 21 9"/>
                                <line x1="10" y1="14" x2="21" y2="3"/>
                            </svg>
                            查看物品
                        </button>
                    </div>

                    <!-- 消息记录 -->
                    <div class="chat-messages" ref="chatMessagesRef">
                        <div v-if="chatMessages.length === 0" class="chat-empty-msg">
                            <p>暂无消息记录，发送第一条消息吧</p>
                        </div>
                        <div
                            v-for="msg in chatMessages"
                            :key="msg.id"
                            class="chat-msg"
                            :class="{ 'chat-msg-self': msg.senderId === currentUserId, 'chat-msg-other': msg.senderId !== currentUserId }"
                        >
                            <div class="msg-avatar">
                                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                    <circle cx="12" cy="7" r="4"/>
                                </svg>
                            </div>
                            <div class="msg-bubble">
                                <div class="msg-text">{{ msg.content }}</div>
                                <div class="msg-time">{{ formatChatTime(msg.createdAt) }}</div>
                            </div>
                        </div>
                    </div>

                    <!-- 输入区域 -->
                    <div class="chat-input-area">
                        <textarea
                            v-model="inputMessage"
                            class="chat-input"
                            placeholder="输入消息..."
                            rows="2"
                            @keydown.enter.exact.prevent="sendMessage"
                        ></textarea>
                        <button class="chat-send-btn" @click="sendMessage" :disabled="!inputMessage.trim() || sending">
                            <span v-if="sending">发送中...</span>
                            <span v-else>发送</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 物品详情弹窗 -->
        <el-dialog
            v-model="itemDialogVisible"
            :title="itemDetail?.title || '物品详情'"
            width="800px"
            destroy-on-close
            class="item-detail-dialog"
        >
            <template v-if="itemLoading" class="dialog-loading">
                <div style="text-align: center; padding: 40px; color: #999;">加载中...</div>
            </template>
            <template v-else-if="itemDetail">
                <div class="dialog-item-detail">
                    <!-- 图片 -->
                    <div class="dialog-gallery">
                        <div class="dialog-gallery-main">
                            <img :src="itemCurrentImage" :alt="itemDetail.title" @error="handleItemImageError" />
                            <span class="dialog-type-badge" :class="itemDetail.type === 1 ? 'badge-found' : 'badge-lost'">
                                {{ itemDetail.type === 1 ? '招领' : '寻物' }}
                            </span>
                            <span v-if="itemDetail.status === 2" class="dialog-status-badge">已解决</span>
                        </div>
                        <div v-if="itemImages.length > 1" class="dialog-gallery-thumbs">
                            <div
                                v-for="(img, index) in itemImages"
                                :key="index"
                                class="dialog-thumb-item"
                                :class="{ active: itemCurrentIndex === index }"
                                @click="itemCurrentIndex = index"
                            >
                                <img :src="img" :alt="`图片 ${index + 1}`" />
                            </div>
                        </div>
                    </div>
                    <!-- 信息 -->
                    <div class="dialog-info">
                        <div class="dialog-info-header">
                            <h2>{{ itemDetail.title }}</h2>
                            <div class="dialog-info-meta">
                                <span class="dialog-category">{{ itemDetail.categoryName || '其他' }}</span>
                                <span class="dialog-time">{{ formatItemTime(itemDetail.createTime) }}</span>
                            </div>
                        </div>
                        <div class="dialog-info-body">
                            <div class="dialog-desc">
                                <h4>物品描述</h4>
                                <p>{{ itemDetail.description || '暂无描述' }}</p>
                            </div>
                            <div class="dialog-detail-items">
                                <div class="dialog-detail-item" v-if="itemDetail.location">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                                        <circle cx="12" cy="10" r="3"/>
                                    </svg>
                                    <span>{{ itemDetail.location }}</span>
                                </div>
                                <div class="dialog-detail-item">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                        <circle cx="12" cy="7" r="4"/>
                                    </svg>
                                    <span>{{ itemDetail.username || '匿名' }}</span>
                                </div>
                                <div class="dialog-detail-item">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <rect x="5" y="2" width="14" height="20" rx="2" ry="2"/>
                                        <line x1="12" y1="18" x2="12.01" y2="18"/>
                                    </svg>
                                    <span>{{ itemDetail.contact || '暂无' }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
            <template v-else>
                <div style="text-align: center; padding: 40px; color: #999;">获取详情失败</div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { messageApi, itemApi, categoryApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const allMessages = ref([])
const conversations = ref([])
const selectedConversation = ref(null)
const chatMessages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const activeTab = ref('all')
const searchKeyword = ref('')
const totalUnread = ref(0)

const chatMessagesRef = ref(null)
const conversationListRef = ref(null)

const currentUserId = computed(() => userStore.userInfo?.id || '')

// 过滤会话
const filteredConversations = computed(() => {
    let list = conversations.value
    if (activeTab.value === 'unread') {
        list = list.filter(c => c.unread > 0)
    }
    if (searchKeyword.value.trim()) {
        const kw = searchKeyword.value.trim().toLowerCase()
        list = list.filter(c =>
            c.otherName.toLowerCase().includes(kw) ||
            c.itemTitle.toLowerCase().includes(kw) ||
            c.lastContent.toLowerCase().includes(kw)
        )
    }
    return list
})

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
    return `${year}-${month}-${day}`
}

function formatChatTime(time) {
    if (!time) return ''
    const date = new Date(time)
    const now = new Date()
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')

    // 今天只显示时间
    if (date.toDateString() === now.toDateString()) {
        return `${hour}:${minute}`
    }

    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${month}-${day} ${hour}:${minute}`
}

// 构建会话列表
function buildConversations(messages) {
    const convMap = new Map()

    messages.forEach(msg => {
        // 兼容新旧字段名
        const senderId = msg.senderId ?? msg.sender_id
        const receiverId = msg.receiverId ?? msg.receiver_id
        const senderName = msg.senderName ?? msg.sender_nickname ?? msg.sender_username
        const receiverName = msg.receiverName ?? msg.receiver_nickname ?? msg.receiver_username
        const itemId = msg.itemId ?? msg.item_id
        const itemTitle = msg.itemTitle ?? msg.item_title ?? '物品'
        const isReadVal = msg.isRead ?? msg.is_read
        const createdAt = msg.createdAt ?? msg.created_at

        // 确定对方信息
        const isReceived = receiverId === currentUserId.value
        const otherId = isReceived ? senderId : receiverId
        const otherName = isReceived ? senderName : receiverName

        // 用 itemId + otherId 作为会话唯一标识
        const convKey = `${itemId}_${otherId}`

        if (!convMap.has(convKey)) {
            convMap.set(convKey, {
                id: convKey,
                itemId,
                itemTitle,
                otherId,
                otherName,
                lastContent: msg.content,
                lastTime: createdAt,
                unread: (!isReadVal && isReceived) ? 1 : 0,
                messages: []
            })
        }

        const conv = convMap.get(convKey)
        conv.messages.push(msg)

        // 更新最新消息
        if (createdAt && new Date(createdAt) > new Date(conv.lastTime)) {
            conv.lastContent = msg.content
            conv.lastTime = createdAt
        }

        // 统计未读
        if (!isReadVal && isReceived) {
            conv.unread++
        }
    })

    // 按最新消息时间排序
    return Array.from(convMap.values()).sort((a, b) => new Date(b.lastTime) - new Date(a.lastTime))
}

async function fetchAllMessages() {
    loading.value = true
    try {
        const [receivedRes, sentRes] = await Promise.all([
            messageApi.getMyMessages({ pageSize: 100 }),
            messageApi.getSentMessages({ pageSize: 100 })
        ])

        const received = receivedRes.data?.list || receivedRes.data || []
        const sent = sentRes.data?.list || sentRes.data || []

        // 合并去重
        const msgMap = new Map()
        ;[...received, ...sent].forEach(msg => {
            msgMap.set(msg.id, msg)
        })
        allMessages.value = Array.from(msgMap.values())

        // 构建会话
        conversations.value = buildConversations(allMessages.value)

        // 计算总未读
        totalUnread.value = received.filter(m => {
            const isRead = m.isRead ?? m.is_read ?? 0
            return !isRead
        }).length
    } catch (error) {
        console.error('获取消息失败:', error)
        ElMessage.error('获取消息列表失败')
    } finally {
        loading.value = false
    }
}

async function selectConversation(conv) {
    selectedConversation.value = conv

    // 获取该会话的完整消息（按时间排序）
    const itemMessages = allMessages.value.filter(m => {
        const mItemId = m.itemId ?? m.item_id
        const mSenderId = m.senderId ?? m.sender_id
        const mReceiverId = m.receiverId ?? m.receiver_id
        return mItemId === conv.itemId &&
            (mSenderId === conv.otherId || mReceiverId === conv.otherId)
    })
    chatMessages.value = itemMessages.sort((a, b) => {
        const aTime = a.createdAt ?? a.created_at ?? ''
        const bTime = b.createdAt ?? b.created_at ?? ''
        return new Date(aTime) - new Date(bTime)
    })

    // 标记该会话的所有未读消息为已读
    const unreadMessages = chatMessages.value.filter(m => {
        const isRead = m.isRead ?? m.is_read ?? 0
        const receiverId = m.receiverId ?? m.receiver_id
        return !isRead && receiverId === currentUserId.value
    })
    for (const msg of unreadMessages) {
        try {
            await messageApi.markRead(msg.id)
            msg.isRead = true
        } catch (e) {
            console.error('标记已读失败:', e)
        }
    }

    // 更新会话未读计数
    conv.unread = 0
    totalUnread.value = Math.max(0, totalUnread.value - unreadMessages.length)

    // 滚动到底部
    await nextTick()
    scrollToBottom()
}

function scrollToBottom() {
    if (chatMessagesRef.value) {
        chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
}

async function sendMessage() {
    const content = inputMessage.value.trim()
    if (!content || !selectedConversation.value || sending.value) return

    sending.value = true
    try {
        const conv = selectedConversation.value
        const res = await messageApi.send({
            itemId: conv.itemId,
            itemTitle: conv.itemTitle,
            receiverId: conv.otherId,
            receiverName: conv.otherName,
            content
        })

        if (res.data) {
            // 添加到聊天记录
            chatMessages.value.push(res.data)
            // 更新会话预览
            conv.lastContent = content
            conv.lastTime = res.data.createdAt
            // 清空输入
            inputMessage.value = ''
            // 滚动到底部
            await nextTick()
            scrollToBottom()
        }
    } catch (error) {
        console.error('发送消息失败:', error)
        ElMessage.error('发送失败')
    } finally {
        sending.value = false
    }
}

// ============ 物品详情弹窗 ============
const itemDialogVisible = ref(false)
const itemDetail = ref(null)
const itemLoading = ref(false)
const itemCurrentIndex = ref(0)

const itemImages = computed(() => {
    if (!itemDetail.value?.images) return []
    try {
        return JSON.parse(itemDetail.value.images)
    } catch {
        return []
    }
})

const itemCurrentImage = computed(() => {
    return itemImages.value[itemCurrentIndex.value] || itemDefaultImage
})

const itemDefaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAwIiBoZWlnaHQ9IjQwMCIgdmlld0JveD0iMCAwIDYwMCA0MDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjYwMCIgaGVpZ2h0PSI0MDAiIGZpbGw9IiNFNEU3RUQiLz48Y2lyY2xlIGN4PSIzMDAiIGN5PSIxNjAiIHI9IjU2IiBmaWxsPSIjQzBDNENDIi8+PHBhdGggZD0iTTE1MCAzMDBMMzAwIDE4MEw0NTAgMzAwSDE1MEwzMDAgMTgwTDQ1MCAzMDBIMTUwWiIgZmlsbD0iI0MwQzRDQyIvPjwvc3ZnPg=='

function handleItemImageError(e) {
    e.target.src = itemDefaultImage
}

function formatItemTime(time) {
    if (!time) return ''
    const date = new Date(time)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
}

async function goToItem(itemId) {
    if (!itemId) return
    itemLoading.value = true
    itemDialogVisible.value = true
    itemCurrentIndex.value = 0
    try {
        const res = await itemApi.getDetail(itemId)
        itemDetail.value = res.data
    } catch (error) {
        console.error('获取物品详情失败:', error)
        itemDetail.value = null
        ElMessage.error('获取物品详情失败')
    } finally {
        itemLoading.value = false
    }
}

onMounted(() => {
    if (!userStore.isLoggedIn) {
        router.push('/login')
        return
    }
    fetchAllMessages()
})
</script>

<style scoped>
/* ========== 暗黑模式 ========== */
html.dark .messages-page {
    background: transparent;
}

html.dark .page-title {
    color: #e0e0f0;
}

html.dark .page-desc {
    color: #6a6a8a;
}

html.dark .chat-container {
    background: transparent;
    border-color: #2a2a3e;
}

html.dark .chat-sidebar {
    background: #1a1a2e;
    border-right-color: #2a2a3e;
}

html.dark .sidebar-header {
    border-bottom-color: #2a2a3e;
}

html.dark .sidebar-header h3 {
    color: #e0e0f0;
}

html.dark .sidebar-search {
    background: #22223a;
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark .sidebar-search:focus {
    border-color: #6366f1;
}

html.dark .sidebar-search::placeholder {
    color: #6a6a8a;
}

html.dark .conversation-item {
    border-bottom-color: #2a2a3e;
}

html.dark .conversation-item:hover {
    background: rgba(99, 102, 241, 0.08);
}

html.dark .conversation-item.active {
    background: rgba(99, 102, 241, 0.15);
}

html.dark .conv-avatar {
    background: #22223a;
}

html.dark .conv-name {
    color: #e0e0f0;
}

html.dark .conv-preview {
    color: #6a6a8a;
}

html.dark .conv-time {
    color: #6a6a8a;
}

html.dark .unread-badge {
    background: #6366f1;
    color: #fff;
}

html.dark .chat-main {
    background: #0f0f1a;
}

html.dark .chat-header {
    background: #1a1a2e;
    border-bottom-color: #2a2a3e;
}

html.dark .chat-header-name {
    color: #e0e0f0;
}

html.dark .chat-header-status {
    color: #6a6a8a;
}

html.dark .chat-back-btn {
    background: #22223a;
    border-color: #2a2a3e;
    color: #8a8aa0;
}

html.dark .chat-back-btn:hover {
    background: #2a2a4e;
    color: #e0e0f0;
}

html.dark .chat-messages {
    background: #0f0f1a;
}

html.dark .msg-date-divider {
    color: #6a6a8a;
}

html.dark .msg-date-divider::before,
html.dark .msg-date-divider::after {
    background: #2a2a3e;
}

html.dark .msg-bubble {
    background: #1a1a2e;
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark .msg-bubble.self {
    background: linear-gradient(135deg, #4f46e5, #6366f1);
    color: #fff;
}

html.dark .msg-time {
    color: #6a6a8a;
}

html.dark .msg-time.self {
    color: rgba(255, 255, 255, 0.6);
}

html.dark .chat-input-area {
    background: #1a1a2e;
    border-top-color: #2a2a3e;
}

html.dark .chat-input {
    background: #22223a;
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark .chat-input:focus {
    border-color: #6366f1;
    background: #2a2a4e;
}

html.dark .chat-input::placeholder {
    color: #6a6a8a;
}

html.dark .chat-send-btn {
    background: linear-gradient(135deg, #4f46e5, #6366f1);
}

html.dark .chat-send-btn:hover:not(:disabled) {
    background: linear-gradient(135deg, #4338ca, #4f46e5);
}

html.dark .empty-chat-icon {
    background: rgba(99, 102, 241, 0.15);
    color: #a5b4fc;
}

html.dark .empty-chat-title {
    color: #e0e0f0;
}

html.dark .empty-chat-desc {
    color: #6a6a8a;
}

html.dark .empty-conv-icon {
    background: rgba(99, 102, 241, 0.15);
    color: #a5b4fc;
}

html.dark .empty-conv-title {
    color: #e0e0f0;
}

html.dark .empty-conv-desc {
    color: #6a6a8a;
}

html.dark .dialog-info-header h2 {
    color: #e0e0f0;
}

html.dark .dialog-desc h4 {
    color: #c8c8d8;
}

html.dark .dialog-desc p {
    color: #8a8aa0;
}

html.dark .dialog-detail-items {
    background: #0f0f1a;
}

html.dark .dialog-detail-item {
    color: #8a8aa0;
}

html.dark .dialog-detail-item svg {
    color: #a5b4fc;
}

html.dark .dialog-category {
    background: rgba(99, 102, 241, 0.15);
    color: #a5b4fc;
}

html.dark .dialog-time {
    color: #6a6a8a;
}

html.dark .dialog-gallery {
    background: #0f0f1a;
}

html.dark .dialog-gallery-main {
    background: #0f0f1a;
}

html.dark .dialog-gallery-thumbs {
    background: #0f0f1a;
    border-top-color: #2a2a3e;
}

/* ========== 消息页面 ========== */
.messages-page {
    height: 100%;
    display: flex;
    flex-direction: column;
    min-height: 0;
}

.chat-layout {
    display: flex;
    height: 100%;
    flex: 1;
}

/* ========== 左侧会话列表 ========== */
.chat-sidebar {
    width: 320px;
    min-width: 320px;
    border-right: 1px solid #e8e8e8;
    display: flex;
    flex-direction: column;
    background: #ffffff;
    transition: width 0.3s ease;
}

.chat-sidebar-expanded {
    width: 100%;
    min-width: 100%;
}

.sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 20px 12px;
    flex-shrink: 0;
}

.sidebar-title {
    font-size: 18px;
    font-weight: 700;
    color: #1a1a2e;
}

.sidebar-count {
    font-size: 12px;
    color: #e74c3c;
    background: #fef2f2;
    padding: 2px 10px;
    border-radius: 10px;
    font-weight: 500;
}

.sidebar-search {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0 16px 12px;
    padding: 8px 12px;
    background: #eef0f4;
    border-radius: 8px;
    flex-shrink: 0;
}

.sidebar-search input {
    flex: 1;
    border: none;
    background: transparent;
    outline: none;
    font-size: 13px;
    color: #4a4a6a;
}

.sidebar-search input::placeholder {
    color: #a0aec0;
}

.sidebar-tabs {
    display: flex;
    gap: 4px;
    padding: 0 16px 12px;
    flex-shrink: 0;
}

.sidebar-tab {
    padding: 6px 16px;
    border: none;
    border-radius: 6px;
    background: transparent;
    font-size: 13px;
    color: #666;
    cursor: pointer;
    transition: all 0.2s;
    font-weight: 500;
}

.sidebar-tab:hover {
    background: #eef0f4;
    color: #4a4a6a;
}

.sidebar-tab.active {
    background: #4f46e5;
    color: #fff;
}

.conversation-list {
    flex: 1;
    overflow-y: auto;
    padding: 0 8px 8px;
}

/* 加载骨架 */
.loading-state {
    padding: 8px;
}

.conv-skeleton {
    display: flex;
    gap: 12px;
    padding: 12px;
    align-items: center;
}

.conv-skeleton-avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: linear-gradient(90deg, #e8e8e8 25%, #f0f0f0 50%, #e8e8e8 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    flex-shrink: 0;
}

.conv-skeleton-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.conv-skeleton-line {
    height: 12px;
    background: linear-gradient(90deg, #e8e8e8 25%, #f0f0f0 50%, #e8e8e8 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4px;
}

.conv-skeleton-line.w-60 { width: 60%; }
.conv-skeleton-line.w-40 { width: 40%; }

@keyframes shimmer {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}

/* 空状态 */
.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 60px 20px;
    color: #a0aec0;
}

.empty-state p {
    margin-top: 12px;
    font-size: 14px;
}

/* 会话项 */
.conversation-item {
    display: flex;
    gap: 12px;
    padding: 12px;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
    align-items: center;
    border-bottom: 1px solid #f0f0f5;
}

.conversation-item:last-child {
    border-bottom: none;
}

.conversation-item:hover {
    background: #eef0f4;
}

.conversation-item.active {
    background: #e8eaff;
}

.conv-avatar {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: #eef2ff;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #4f46e5;
    flex-shrink: 0;
    position: relative;
}

.conv-avatar-unread::after {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 10px;
    height: 10px;
    background: #e74c3c;
    border-radius: 50%;
    border: 2px solid #ffffff;
}

.conv-info {
    flex: 1;
    min-width: 0;
}

.conv-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 4px;
}

.conv-name {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a2e;
}

.conv-time {
    font-size: 11px;
    color: #a0aec0;
    flex-shrink: 0;
}

.conv-bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
}

.conv-preview {
    font-size: 13px;
    color: #999;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    flex: 1;
}

.conv-badge {
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    border-radius: 9px;
    background: #e74c3c;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

/* ========== 右侧聊天区域 ========== */
.chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #fff;
    min-width: 0;
}

/* 欢迎页 */
.chat-welcome {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #c0c4cc;
}

.welcome-icon {
    margin-bottom: 20px;
    opacity: 0.5;
}

/* 聊天头部 */
.chat-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    border-bottom: 1px solid #e8e8e8;
    flex-shrink: 0;
    background: #fff;
}

.chat-back-btn {
    display: none;
    background: none;
    border: none;
    color: #666;
    cursor: pointer;
    padding: 4px;
    border-radius: 4px;
}

.chat-back-btn:hover {
    background: #f0f0f0;
}

.chat-header-info {
    flex: 1;
    min-width: 0;
}

.chat-header-name {
    font-size: 15px;
    font-weight: 600;
    color: #1a1a2e;
    display: block;
    margin-bottom: 2px;
}

.chat-header-item {
    font-size: 12px;
    color: #818cf8;
}

.chat-item-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #fff;
    font-size: 13px;
    color: #4f46e5;
    cursor: pointer;
    transition: all 0.2s;
    flex-shrink: 0;
}

.chat-item-btn:hover {
    background: #f0f0ff;
    border-color: #c7d2fe;
}

/* 聊天视图容器 */
.chat-view {
    display: flex;
    flex-direction: column;
    flex: 1;
    min-height: 0;
}

/* 消息记录区域 */
.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    background: #ffffff;
    min-height: 0;
}

.chat-empty-msg {
    text-align: center;
    padding: 40px;
    color: #c0c4cc;
    font-size: 14px;
}

.chat-msg {
    display: flex;
    gap: 10px;
    max-width: 80%;
}

.chat-msg-self {
    align-self: flex-end;
    flex-direction: row-reverse;
}

.chat-msg-other {
    align-self: flex-start;
}

.msg-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #eef2ff;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #4f46e5;
    flex-shrink: 0;
}

.chat-msg-self .msg-avatar {
    background: #e8f5e9;
    color: #43a047;
}

.msg-bubble {
    padding: 10px 14px;
    border-radius: 12px;
    position: relative;
    word-break: break-word;
}

.chat-msg-other .msg-bubble {
    background: #fff;
    border: 1px solid #e8e8e8;
    border-top-left-radius: 4px;
}

.chat-msg-self .msg-bubble {
    background: #4f46e5;
    color: #fff;
    border-top-right-radius: 4px;
}

.msg-text {
    font-size: 14px;
    line-height: 1.6;
}

.msg-time {
    font-size: 11px;
    margin-top: 4px;
    opacity: 0.7;
}

.chat-msg-other .msg-time {
    color: #999;
}

.chat-msg-self .msg-time {
    color: rgba(255, 255, 255, 0.7);
}

/* 输入区域 */
.chat-input-area {
    display: flex;
    gap: 12px;
    padding: 40px 20px 20px;
    border-top: 1px solid #e8e8e8;
    background: #fff;
    flex-shrink: 0;
}

.chat-input {
    flex: 1;
    padding: 10px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    font-size: 14px;
    color: #4a4a6a;
    outline: none;
    resize: none;
    font-family: inherit;
    transition: border-color 0.2s;
    background: #fafbfc;
}

.chat-input:focus {
    border-color: #4f46e5;
    background: #fff;
}

.chat-input::placeholder {
    color: #c0c4cc;
}

.chat-send-btn {
    padding: 10px 24px;
    background: #4f46e5;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    align-self: flex-end;
    flex-shrink: 0;
}

.chat-send-btn:hover:not(:disabled) {
    background: #4338ca;
}

.chat-send-btn:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

/* 响应式 */
/* ========== 物品详情弹窗 ========== */
:deep(.item-detail-dialog) {
    display: flex;
    align-items: center;
    justify-content: center;
}

:deep(.item-detail-dialog .el-dialog) {
    margin: 0 !important;
    border-radius: 6px;
    overflow: hidden;
}

:deep(.item-detail-dialog .el-dialog__header) {
    display: none;
}

:deep(.item-detail-dialog .el-dialog__body) {
    padding: 0;
}

.dialog-item-detail {
    display: grid;
    grid-template-columns: 1fr 1fr;
    min-height: 500px;
}

.dialog-gallery {
    position: relative;
    background: #f0f2f5;
}

.dialog-gallery-main {
    position: relative;
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #e8eaed;
    overflow: hidden;
}

.dialog-gallery-main img {
    max-width: 100%;
    max-height: 100%;
    object-fit: contain;
}

.dialog-type-badge {
    position: absolute;
    top: 12px;
    left: 12px;
    padding: 4px 12px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;
}

.dialog-type-badge.badge-found {
    background: linear-gradient(135deg, #1a8a5c, #2ea86e);
}

.dialog-type-badge.badge-lost {
    background: linear-gradient(135deg, #c0392b, #e74c3c);
}

.dialog-status-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
}

.dialog-gallery-thumbs {
    display: flex;
    gap: 6px;
    padding: 8px;
    background: #f7fafc;
    border-top: 1px solid #e2e8f0;
    overflow-x: auto;
}

.dialog-thumb-item {
    width: 48px;
    height: 48px;
    border-radius: 6px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s;
    flex-shrink: 0;
}

.dialog-thumb-item:hover {
    border-color: #a5b4fc;
}

.dialog-thumb-item.active {
    border-color: #4f46e5;
}

.dialog-thumb-item img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.dialog-info {
    padding: 24px;
    display: flex;
    flex-direction: column;
}

.dialog-info-header {
    margin-bottom: 16px;
}

.dialog-info-header h2 {
    font-size: 18px;
    font-weight: 700;
    color: #4a4a6a;
    margin-bottom: 8px;
    line-height: 1.4;
}

.dialog-info-meta {
    display: flex;
    align-items: center;
    gap: 10px;
}

.dialog-category {
    padding: 3px 8px;
    background: #eef2ff;
    color: #4f46e5;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 500;
}

.dialog-time {
    font-size: 12px;
    color: #a0aec0;
}

.dialog-info-body {
    flex: 1;
}

.dialog-desc {
    margin-bottom: 16px;
}

.dialog-desc h4 {
    font-size: 13px;
    font-weight: 600;
    color: #4a4a6a;
    margin-bottom: 6px;
}

.dialog-desc p {
    font-size: 13px;
    color: #4a5568;
    line-height: 1.7;
}

.dialog-detail-items {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 12px;
    background: #f7fafc;
    border-radius: 8px;
}

.dialog-detail-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #4a5568;
}

.dialog-detail-item svg {
    color: #4f46e5;
    flex-shrink: 0;
}

@media (max-width: 768px) {
    .chat-sidebar {
        width: 100%;
        min-width: 100%;
    }

    .chat-sidebar-expanded {
        display: flex;
    }

    .chat-main {
        display: none;
    }

    .chat-sidebar:not(.chat-sidebar-expanded) {
        width: 100%;
        min-width: 100%;
    }

    .chat-sidebar:not(.chat-sidebar-expanded) + .chat-main {
        display: flex;
    }

    .chat-back-btn {
        display: flex;
    }
}
</style>