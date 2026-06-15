<template>
    <div class="detail-page">
        <div class="detail-container">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-section">
                <div class="skeleton-detail">
                    <div class="skeleton-gallery"></div>
                    <div class="skeleton-info">
                        <div class="skeleton-line w-60"></div>
                        <div class="skeleton-line w-80"></div>
                        <div class="skeleton-line w-40"></div>
                        <div class="skeleton-line w-70"></div>
                    </div>
                </div>
            </div>

            <!-- 错误状态 -->
            <div v-else-if="error" class="error-section">
                <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="#c0c4cc" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                <p>{{ error }}</p>
                <button class="btn-back" @click="router.push('/')">返回首页</button>
            </div>

            <!-- 详情内容 -->
            <template v-else-if="item">
                <div class="detail-card">
                    <!-- 图片画廊 -->
                    <div class="gallery-section">
                        <div class="gallery-main">
                            <img :src="currentImage" :alt="item.title" @error="handleImageError" />
                            <span class="item-type-badge" :class="item.type === 1 ? 'badge-found' : 'badge-lost'">
                                {{ item.type === 1 ? '招领' : '寻物' }}
                            </span>
                            <span v-if="item.status === 2" class="item-status-badge">已解决</span>
                        </div>
                        <div v-if="images.length > 1" class="gallery-thumbs">
                            <div
                                v-for="(img, index) in images"
                                :key="index"
                                class="thumb-item"
                                :class="{ active: currentIndex === index }"
                                @click="currentIndex = index"
                            >
                                <img :src="img" :alt="`图片 ${index + 1}`" />
                            </div>
                        </div>
                    </div>

                    <!-- 信息区域 -->
                    <div class="info-section">
                        <div class="info-header">
                            <h1 class="info-title">{{ item.title }}</h1>
                            <div class="info-meta">
                                <span class="meta-category">{{ getCategoryName(item.categoryId) }}</span>
                                <span class="meta-time">{{ formatTime(item.createTime) }}</span>
                            </div>
                        </div>

                        <div class="info-body">
                            <div class="info-desc">
                                <h3 class="info-label">物品描述</h3>
                                <p>{{ item.description || '暂无描述' }}</p>
                            </div>

                            <div class="info-detail">
                                <div class="detail-item" v-if="item.location">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                                        <circle cx="12" cy="10" r="3"/>
                                    </svg>
                                    <span>{{ item.location }}</span>
                                </div>
                                <div class="detail-item">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                        <circle cx="12" cy="7" r="4"/>
                                    </svg>
                                    <span>{{ item.username || '匿名' }}</span>
                                </div>
                                <div class="detail-item">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <rect x="5" y="2" width="14" height="20" rx="2" ry="2"/>
                                        <line x1="12" y1="18" x2="12.01" y2="18"/>
                                    </svg>
                                    <span>{{ item.contact || '暂无' }}</span>
                                </div>
                            </div>
                        </div>

                        <!-- 操作按钮 -->
                        <div class="info-actions" v-if="userStore.isLoggedIn">
                            <button
                                v-if="isOwner"
                                class="action-btn action-edit"
                                @click="router.push(`/edit/${item.id}`)"
                            >
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                                </svg>
                                编辑
                            </button>
                            <button
                                v-if="isOwner && item.status === 1"
                                class="action-btn action-resolve"
                                @click="handleResolve"
                            >
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                                    <polyline points="22 4 12 14.01 9 11.01"/>
                                </svg>
                                标记已解决
                            </button>
                            <button
                                v-if="isOwner"
                                class="action-btn action-delete"
                                @click="handleDelete"
                            >
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <polyline points="3 6 5 6 21 6"/>
                                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                                </svg>
                                下架
                            </button>
                            <button
                                v-if="!isOwner"
                                class="action-btn action-message"
                                @click="showMessageDialog = true"
                            >
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                                </svg>
                                留言私信
                            </button>
                        </div>
                    </div>
                </div>

                <!-- 留言对话框 -->
                <el-dialog v-model="showMessageDialog" title="发送私信" width="400px" :close-on-click-modal="false">
                    <div class="message-form">
                        <textarea
                            v-model="messageContent"
                            class="message-textarea"
                            placeholder="请输入您要发送的消息..."
                            rows="4"
                            maxlength="200"
                        ></textarea>
                        <span class="message-count">{{ messageContent.length }}/200</span>
                    </div>
                    <template #footer>
                        <el-button @click="showMessageDialog = false">取消</el-button>
                        <el-button type="primary" @click="handleSendMessage" :loading="sendingMessage">发送</el-button>
                    </template>
                </el-dialog>
            </template>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { itemApi, categoryApi, messageApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const error = ref('')
const item = ref(null)
const categories = ref([])
const currentIndex = ref(0)
const showMessageDialog = ref(false)
const messageContent = ref('')
const sendingMessage = ref(false)

const images = computed(() => {
    if (!item.value?.images) return []
    try {
        return JSON.parse(item.value.images)
    } catch {
        return []
    }
})

const currentImage = computed(() => {
    return images.value[currentIndex.value] || defaultImage
})

const isOwner = computed(() => {
    if (!userStore.isLoggedIn || !item.value) return false
    return userStore.userInfo?.id === item.value.userId
})

const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAwIiBoZWlnaHQ9IjQwMCIgdmlld0JveD0iMCAwIDYwMCA0MDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjYwMCIgaGVpZ2h0PSI0MDAiIGZpbGw9IiNFNEU3RUQiLz48Y2lyY2xlIGN4PSIzMDAiIGN5PSIxNjAiIHI9IjU2IiBmaWxsPSIjQzBDNENDIi8+PHBhdGggZD0iTTE1MCAzMDBMMzAwIDE4MEw0NTAgMzAwSDE1MEwzMDAgMTgwTDQ1MCAzMDBIMTUwWiIgZmlsbD0iI0MwQzRDQyIvPjwvc3ZnPg=='

function handleImageError(e) {
    e.target.src = defaultImage
}

function formatTime(time) {
    if (!time) return ''
    const date = new Date(time)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
}

function getCategoryName(id) {
    const cat = categories.value.find(c => c.id === id)
    return cat ? cat.name : '其他'
}

async function fetchCategories() {
    try {
        const res = await categoryApi.getList()
        categories.value = res.data || []
    } catch (error) {
        console.error('获取分类失败:', error)
    }
}

async function fetchDetail() {
    loading.value = true
    error.value = ''
    try {
        const res = await itemApi.getDetail(route.params.id)
        item.value = res.data
    } catch (error) {
        console.error('获取详情失败:', error)
        error.value = '获取物品信息失败，请稍后重试'
    } finally {
        loading.value = false
    }
}

async function handleResolve() {
    try {
        await ElMessageBox.confirm('确认该物品已找到主人？', '确认操作', {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'success'
        })
        await itemApi.update(item.value.id, { status: 2 })
        ElMessage.success('已标记为已解决')
        item.value.status = 2
    } catch (error) {
        if (error !== 'cancel') {
            console.error('操作失败:', error)
        }
    }
}

async function handleDelete() {
    try {
        await ElMessageBox.confirm('下架后该信息将不再展示，确定要下架吗？', '确认下架', {
            confirmButtonText: '确认下架',
            cancelButtonText: '取消',
            type: 'warning'
        })
        await itemApi.delete(item.value.id)
        ElMessage.success('已下架')
        router.push('/')
    } catch (error) {
        if (error !== 'cancel') {
            console.error('操作失败:', error)
        }
    }
}

async function handleSendMessage() {
    if (!messageContent.value.trim()) {
        ElMessage.warning('请输入消息内容')
        return
    }

    sendingMessage.value = true
    try {
        await messageApi.send({
            itemId: item.value.id,
            receiverId: item.value.userId,
            content: messageContent.value.trim()
        })
        ElMessage.success('消息已发送')
        showMessageDialog.value = false
        messageContent.value = ''
    } catch (error) {
        console.error('发送消息失败:', error)
    } finally {
        sendingMessage.value = false
    }
}

onMounted(() => {
    fetchCategories()
    fetchDetail()
})
</script>

<style scoped>
.detail-page {
    min-height: 100vh;
    background: #f0f2f5;
}

.detail-container {
    max-width: 1000px;
    margin: 0 auto;
    padding: 84px 24px 60px;
}

/* 加载状态 */
.loading-section {
    background: #fff;
    border-radius: 16px;
    border: 1px solid #e2e8f0;
    overflow: hidden;
}

.skeleton-detail {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0;
}

.skeleton-gallery {
    height: 400px;
    background: linear-gradient(90deg, #f0f2f5 25%, #e2e8f0 50%, #f0f2f5 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
}

.skeleton-info {
    padding: 32px;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.skeleton-line {
    height: 16px;
    background: linear-gradient(90deg, #f0f2f5 25%, #e2e8f0 50%, #f0f2f5 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4px;
}

.skeleton-line.w-60 { width: 60%; }
.skeleton-line.w-80 { width: 80%; }
.skeleton-line.w-40 { width: 40%; }
.skeleton-line.w-70 { width: 70%; }

@keyframes shimmer {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}

/* 错误状态 */
.error-section {
    text-align: center;
    padding: 80px 0;
    background: #fff;
    border-radius: 16px;
    border: 1px solid #e2e8f0;
}

.error-section svg {
    margin-bottom: 16px;
}

.error-section p {
    font-size: 16px;
    color: #4a5568;
    margin-bottom: 20px;
}

.btn-back {
    padding: 10px 24px;
    background: #1a3a5c;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.2s;
}

.btn-back:hover {
    background: #2c5f8a;
}

/* 详情卡片 */
.detail-card {
    background: #fff;
    border-radius: 16px;
    border: 1px solid #e2e8f0;
    overflow: hidden;
    display: grid;
    grid-template-columns: 1fr 1fr;
}

/* 图片画廊 */
.gallery-section {
    position: relative;
}

.gallery-main {
    position: relative;
    height: 400px;
    background: #f0f2f5;
}

.gallery-main img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.item-type-badge {
    position: absolute;
    top: 16px;
    left: 16px;
    padding: 6px 16px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    color: #fff;
}

.badge-found {
    background: linear-gradient(135deg, #1a8a5c, #2ea86e);
}

.badge-lost {
    background: linear-gradient(135deg, #c0392b, #e74c3c);
}

.item-status-badge {
    position: absolute;
    top: 16px;
    right: 16px;
    padding: 6px 14px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
}

.gallery-thumbs {
    display: flex;
    gap: 8px;
    padding: 12px;
    background: #f7fafc;
    border-top: 1px solid #e2e8f0;
    overflow-x: auto;
}

.thumb-item {
    width: 64px;
    height: 64px;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s;
    flex-shrink: 0;
}

.thumb-item:hover {
    border-color: #7aa3d0;
}

.thumb-item.active {
    border-color: #1a3a5c;
}

.thumb-item img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

/* 信息区域 */
.info-section {
    padding: 32px;
    display: flex;
    flex-direction: column;
}

.info-header {
    margin-bottom: 24px;
}

.info-title {
    font-size: 22px;
    font-weight: 700;
    color: #2c3e50;
    margin-bottom: 12px;
    line-height: 1.4;
}

.info-meta {
    display: flex;
    align-items: center;
    gap: 12px;
}

.meta-category {
    padding: 4px 10px;
    background: #d4e4f3;
    color: #1a3a5c;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
}

.meta-time {
    font-size: 13px;
    color: #a0aec0;
}

.info-body {
    flex: 1;
}

.info-desc {
    margin-bottom: 24px;
}

.info-label {
    font-size: 14px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 8px;
}

.info-desc p {
    font-size: 14px;
    color: #4a5568;
    line-height: 1.8;
}

.info-detail {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
    background: #f7fafc;
    border-radius: 12px;
}

.detail-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: #4a5568;
}

.detail-item svg {
    color: #1a3a5c;
    flex-shrink: 0;
}

/* 操作按钮 */
.info-actions {
    display: flex;
    gap: 10px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid #f0f2f5;
    flex-wrap: wrap;
}

.action-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 20px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s;
    border: 1px solid #e2e8f0;
    background: #fff;
    color: #4a5568;
}

.action-btn:hover {
    transform: translateY(-1px);
}

.action-edit:hover {
    border-color: #1a3a5c;
    color: #1a3a5c;
    background: #f0f5fa;
}

.action-resolve:hover {
    border-color: #1a8a5c;
    color: #1a8a5c;
    background: #f0faf5;
}

.action-delete:hover {
    border-color: #c0392b;
    color: #c0392b;
    background: #fdf2f2;
}

.action-message {
    background: linear-gradient(135deg, #1a3a5c, #2c5f8a);
    color: #fff;
    border: none;
}

.action-message:hover {
    background: linear-gradient(135deg, #2c5f8a, #4a7fb5);
    box-shadow: 0 4px 12px rgba(26, 58, 92, 0.3);
}

/* 留言对话框 */
.message-form {
    position: relative;
}

.message-textarea {
    width: 100%;
    padding: 12px 16px;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    font-size: 14px;
    color: #2c3e50;
    outline: none;
    resize: none;
    font-family: inherit;
    box-sizing: border-box;
    transition: border-color 0.2s;
}

.message-textarea:focus {
    border-color: #1a3a5c;
}

.message-count {
    position: absolute;
    bottom: 12px;
    right: 12px;
    font-size: 12px;
    color: #a0aec0;
}

/* 响应式 */
@media (max-width: 768px) {
    .detail-card {
        grid-template-columns: 1fr;
    }

    .gallery-main {
        height: 280px;
    }

    .info-section {
        padding: 24px;
    }
}
</style>