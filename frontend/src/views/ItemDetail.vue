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
                <button class="detail-close-btn" @click="goBack" title="关闭">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="18" y1="6" x2="6" y2="18"/>
                        <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                </button>
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
                                    <span v-if="item.certified" class="detail-cert-badge" title="已通过校园认证">
                                        <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                                            <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/>
                                        </svg>
                                        已认证
                                    </span>
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
                            <button
                                class="action-btn action-share"
                                @click="handleShare"
                            >
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <circle cx="18" cy="5" r="3"/>
                                    <circle cx="6" cy="12" r="3"/>
                                    <circle cx="18" cy="19" r="3"/>
                                    <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                                    <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
                                </svg>
                                分享
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

                <!-- 分享海报 -->
                <SharePoster :visible="showPoster" :item="item" @close="showPoster = false" />
            </template>

            <!-- 智能匹配推荐 -->
            <div v-if="item && matchItems.length > 0" class="match-section">
                <div class="match-header">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                    </svg>
                    <span>智能匹配推荐</span>
                </div>
                <div class="match-list">
                    <div
                        v-for="m in matchItems"
                        :key="m.id"
                        class="match-card"
                        @click="router.push(`/item/${m.id}`)"
                    >
                        <div class="match-card-img">
                            <img v-if="getMatchImage(m.images)" :src="getMatchImage(m.images)" alt="" />
                            <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#c0c4cc" stroke-width="1.5">
                                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                                <circle cx="8.5" cy="8.5" r="1.5"/>
                                <polyline points="21 15 16 10 5 21"/>
                            </svg>
                        </div>
                        <div class="match-card-info">
                            <h4>{{ m.title }}</h4>
                            <p>{{ m.description || '暂无描述' }}</p>
                            <span class="match-type-badge" :class="m.type === 1 ? 'type-found' : 'type-lost'">
                                {{ m.type === 1 ? '招领' : '寻物' }}
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { itemApi, categoryApi, messageApi } from '../api'
import { useUserStore } from '../stores/user'
import SharePoster from '../components/SharePoster.vue'

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
const showPoster = ref(false)
const matchItems = ref([])
const matchLoading = ref(false)

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

function getMatchImage(images) {
    try {
        const arr = JSON.parse(images)
        return arr[0] || ''
    } catch {
        return ''
    }
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

async function fetchMatchItems() {
    if (!item.value) return
    matchLoading.value = true
    try {
        const res = await itemApi.smartMatch(item.value.id)
        if (res.data && res.data.length > 0) {
            matchItems.value = res.data
        }
    } catch (e) {
        console.error('获取匹配推荐失败:', e)
    } finally {
        matchLoading.value = false
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

function handleShare() {
    showPoster.value = true
}

function goBack() {
    router.back()
}

onMounted(() => {
    fetchCategories()
    fetchDetail()
})

// 在获取详情后加载匹配推荐
watch(item, (val) => {
    if (val) {
        fetchMatchItems()
    }
})
</script>

<style scoped>
/* ========== 暗黑模式 ========== */
html.dark .detail-page {
    background: #0f0f1a;
}

html.dark .detail-container {
    background: transparent;
}

html.dark .loading-section {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .skeleton-gallery {
    background: linear-gradient(90deg, #1a1a2e 25%, #2a2a3e 50%, #1a1a2e 75%);
    background-size: 200% 100%;
}

html.dark .skeleton-line {
    background: linear-gradient(90deg, #1a1a2e 25%, #2a2a3e 50%, #1a1a2e 75%);
    background-size: 200% 100%;
}

html.dark .error-section {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .error-section p {
    color: #c8c8d8;
}

html.dark .detail-close-btn {
    background: #1a1a2e;
    border-color: #2a2a3e;
    color: #8a8aa0;
}

html.dark .detail-close-btn:hover {
    background: #22223a;
    color: #e0e0f0;
}

html.dark .detail-card {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .gallery-main {
    background: #0f0f1a;
}

html.dark .gallery-thumbs {
    background: #0f0f1a;
    border-top-color: #2a2a3e;
}

html.dark .info-title {
    color: #e0e0f0;
}

html.dark .meta-category {
    background: rgba(99, 102, 241, 0.15);
    color: #a5b4fc;
}

html.dark .meta-time {
    color: #6a6a8a;
}

html.dark .info-label {
    color: #c8c8d8;
}

html.dark .info-desc p {
    color: #8a8aa0;
}

html.dark .info-detail {
    background: #0f0f1a;
}

html.dark .detail-item {
    color: #8a8aa0;
}

html.dark .info-actions {
    border-top-color: #2a2a3e;
}

html.dark .action-btn {
    background: #1a1a2e;
    border-color: #2a2a3e;
    color: #8a8aa0;
}

html.dark .action-edit:hover {
    border-color: #6366f1;
    color: #a5b4fc;
    background: rgba(99, 102, 241, 0.1);
}

html.dark .action-resolve:hover {
    border-color: #2ea86e;
    color: #2ea86e;
    background: rgba(46, 168, 110, 0.1);
}

html.dark .action-delete:hover {
    border-color: #e74c3c;
    color: #e74c3c;
    background: rgba(231, 76, 60, 0.1);
}

html.dark .message-textarea {
    background: #22223a;
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark .message-textarea:focus {
    border-color: #6366f1;
}

html.dark .message-textarea::placeholder {
    color: #6a6a8a;
}

html.dark .message-count {
    color: #6a6a8a;
}

html.dark .match-section {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .match-header {
    color: #e0e0f0;
}

html.dark .match-card {
    border-color: #2a2a3e;
}

html.dark .match-card:hover {
    border-color: #6366f1;
    background: rgba(99, 102, 241, 0.08);
}

html.dark .match-card-info h4 {
    color: #c8c8d8;
}

html.dark .match-card-info p {
    color: #6a6a8a;
}

html.dark .match-card-img {
    background: #0f0f1a;
}

/* ========== 详情页 ========== */
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
    border-radius: 8px;
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
    border-radius: 8px;
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
    background: #4f46e5;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.2s;
}

.btn-back:hover {
    background: #4338ca;
}

/* 关闭按钮 */
.detail-close-btn {
    position: fixed;
    top: 88px;
    right: 24px;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #fff;
    border: 1px solid #e2e8f0;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #666;
    transition: all 0.2s;
    z-index: 100;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.detail-close-btn:hover {
    background: #f5f5f5;
    color: #333;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

/* 详情卡片 */
.detail-card {
    background: #fff;
    border-radius: 8px;
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
    border-color: #a5b4fc;
}

.thumb-item.active {
    border-color: #4f46e5;
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
    color: #4a4a6a;
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
    background: #eef2ff;
    color: #4f46e5;
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
    color: #4a4a6a;
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
    border-radius: 8px;
}

.detail-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: #4a5568;
}

.detail-item svg {
    color: #4f46e5;
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
    border-color: #4f46e5;
    color: #4f46e5;
    background: #f0f0ff;
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
    background: linear-gradient(135deg, #4f46e5, #6366f1);
    color: #fff;
    border: none;
}

.action-message:hover {
    background: linear-gradient(135deg, #4338ca, #4f46e5);
    box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.action-share {
    background: #fff;
    border-color: #e2e8f0;
    color: #4a5568;
}

.action-share:hover {
    border-color: #10b981;
    color: #10b981;
    background: #f0faf5;
}

/* 认证标识 */
.detail-cert-badge {
    display: inline-flex;
    align-items: center;
    gap: 3px;
    font-size: 11px;
    font-weight: 600;
    color: #059669;
    background: rgba(16, 185, 129, 0.1);
    padding: 1px 6px;
    border-radius: 8px;
    white-space: nowrap;
}

/* 留言对话框 */
.message-form {
    position: relative;
}

.message-textarea {
    width: 100%;
    padding: 12px 16px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    font-size: 14px;
    color: #4a4a6a;
    outline: none;
    resize: none;
    font-family: inherit;
    box-sizing: border-box;
    transition: border-color 0.2s;
}

.message-textarea:focus {
    border-color: #4f46e5;
}

.message-count {
    position: absolute;
    bottom: 12px;
    right: 12px;
    font-size: 12px;
    color: #a0aec0;
}

/* 智能匹配推荐 */
.match-section {
    margin-top: 24px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    padding: 20px 24px;
}

.match-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #4a4a6a;
    margin-bottom: 16px;
}

.match-header svg {
    color: #e74c3c;
}

.match-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 12px;
}

.match-card {
    display: flex;
    gap: 12px;
    padding: 12px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
}

.match-card:hover {
    border-color: #4f46e5;
    background: rgba(79, 70, 229, 0.04);
}

.match-card-img {
    width: 72px;
    height: 72px;
    border-radius: 6px;
    overflow: hidden;
    flex-shrink: 0;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
}

.match-card-img img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.match-card-info {
    flex: 1;
    min-width: 0;
}

.match-card-info h4 {
    font-size: 14px;
    font-weight: 600;
    color: #4a4a6a;
    margin: 0 0 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.match-card-info p {
    font-size: 12px;
    color: #8a8aa0;
    margin: 0 0 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.match-type-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 500;
}

.type-found {
    background: rgba(46, 168, 110, 0.1);
    color: #1a8a5c;
}

.type-lost {
    background: rgba(231, 76, 60, 0.1);
    color: #c0392b;
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

    .match-list {
        grid-template-columns: 1fr;
    }
}
</style>
