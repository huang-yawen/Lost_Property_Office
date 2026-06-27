<template>
    <div class="home-page">
        <div class="main-content">
            <div class="page-header-bar">
                <h1 class="page-header-title">首页</h1>
            </div>
            <!-- 搜索栏 -->
            <div class="search-section">
                <div class="search-box">
                    <svg class="search-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="8"/>
                        <path d="M21 21l-4.35-4.35"/>
                    </svg>
                    <input
                        v-model="searchKeyword"
                        type="text"
                        class="search-input"
                        placeholder="搜索物品名称、描述..."
                        @keyup.enter="handleSearch"
                    />
                    <button v-if="searchKeyword" class="search-clear" @click="clearSearch">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="18" y1="6" x2="6" y2="18"/>
                            <line x1="6" y1="6" x2="18" y2="18"/>
                        </svg>
                    </button>
                    <button class="search-btn" @click="handleSearch">搜索</button>
                </div>
            </div>

            <!-- 筛选区域：三行布局 -->
            <div class="filter-area">
                <!-- 第一行：类型筛选（招领 / 失物） -->
                <div class="filter-row">
                    <div class="filter-label">类型</div>
                    <div class="filter-options">
                        <button
                            class="filter-btn"
                            :class="{ active: activeType === 0 }"
                            @click="switchType(0)"
                        >全部</button>
                        <button
                            class="filter-btn type-found"
                            :class="{ active: activeType === 1 }"
                            @click="switchType(1)"
                        >招领</button>
                        <button
                            class="filter-btn type-lost"
                            :class="{ active: activeType === 2 }"
                            @click="switchType(2)"
                        >寻物</button>
                    </div>
                </div>

                <!-- 第二行：分类筛选（证件、生活用品等） -->
                <div class="filter-row">
                    <div class="filter-label">分类</div>
                    <div class="filter-options">
                        <span
                            class="category-chip"
                            :class="{ active: activeCategory === 0 }"
                            @click="switchCategory(0)"
                        >全部</span>
                        <span
                            v-for="cat in categories"
                            :key="cat.id"
                            class="category-chip"
                            :class="{ active: activeCategory === cat.id }"
                            @click="switchCategory(cat.id)"
                        >{{ cat.name }}</span>
                    </div>
                </div>

                <!-- 第三行：时间筛选 + 排序 -->
                <div class="filter-row">
                    <div class="filter-label">时间</div>
                    <div class="filter-options">
                        <button
                            class="filter-btn"
                            :class="{ active: timeRange === 0 }"
                            @click="setTimeRange(0)"
                        >全部</button>
                        <button
                            class="filter-btn"
                            :class="{ active: timeRange === 1 }"
                            @click="setTimeRange(1)"
                        >今天</button>
                        <button
                            class="filter-btn"
                            :class="{ active: timeRange === 3 }"
                            @click="setTimeRange(3)"
                        >近三天</button>
                        <button
                            class="filter-btn"
                            :class="{ active: timeRange === 7 }"
                            @click="setTimeRange(7)"
                        >近一周</button>
                        <button
                            class="filter-btn"
                            :class="{ active: timeRange === 30 }"
                            @click="setTimeRange(30)"
                        >近一月</button>
                    </div>
                    <div class="filter-right">
                        <button class="sort-btn" @click="toggleSort">
                            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="21" y1="6" x2="9" y2="6"/>
                                <line x1="21" y1="12" x2="5" y2="12"/>
                                <line x1="21" y1="18" x2="11" y2="18"/>
                                <polyline :points="sortOrder === 'desc' ? '3 10 7 6 11 10' : '3 14 7 18 11 14'"/>
                            </svg>
                            {{ sortOrder === 'desc' ? '最新优先' : '最早优先' }}
                        </button>
                    </div>
                </div>
            </div>

            <!-- 分隔线 -->
            <div class="content-divider"></div>

            <!-- 加载状态 -->
            <div v-if="loading" class="loading-grid">
                <div v-for="n in 6" :key="n" class="skeleton-card">
                    <div class="skeleton-image"></div>
                    <div class="skeleton-body">
                        <div class="skeleton-line w-60"></div>
                        <div class="skeleton-line w-80"></div>
                        <div class="skeleton-line w-40"></div>
                    </div>
                </div>
            </div>

            <!-- 空状态 -->
            <div v-else-if="items.length === 0" class="empty-section">
                <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="#c0c4cc" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"/>
                    <path d="M21 21l-4.35-4.35"/>
                    <line x1="8" y1="11" x2="14" y2="11"/>
                </svg>
                <p>暂无相关信息</p>
            </div>

            <!-- 物品列表 -->
            <div v-else class="items-grid">
                <div
                    v-for="item in items"
                    :key="item.id"
                    class="item-card"
                    @click="openDetail(item)"
                >
                    <div class="item-image">
                        <img
                            :src="getFirstImage(item.images)"
                            :alt="item.title"
                            @error="handleImageError"
                        />
                        <span class="item-type-badge" :class="item.type === 1 ? 'badge-found' : 'badge-lost'">
                            {{ item.type === 1 ? '招领' : '寻物' }}
                        </span>
                        <span v-if="item.status === 2" class="item-status-badge">已解决</span>
                    </div>
                    <div class="item-info">
                        <h3 class="item-title">{{ item.title }}</h3>
                        <p class="item-desc">{{ item.description }}</p>
                        <div class="item-meta">
                            <span class="meta-category">{{ getCategoryName(item.categoryId) }}</span>
                            <span class="meta-location" v-if="item.location">
                                <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                                    <circle cx="12" cy="10" r="3"/>
                                </svg>
                                {{ item.location }}
                            </span>
                        </div>
                        <div class="item-footer">
                            <span class="item-time">{{ formatTime(item.createTime) }}</span>
                            <span class="item-author">{{ item.username || '匿名' }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 分页 -->
            <div v-if="total > pageSize" class="pagination-section">
                <el-pagination
                    v-model:current-page="currentPage"
                    :page-size="pageSize"
                    :total="total"
                    layout="prev, pager, next"
                    @current-change="handlePageChange"
                />
            </div>
        </div>

        <!-- 物品详情弹窗 -->
        <el-dialog
            v-model="detailVisible"
            :title="detailItem?.title || '物品详情'"
            width="820px"
            :close-on-click-modal="false"
            top="5vh"
            class="detail-dialog"
            @closed="handleDetailClosed"
        >
            <div v-if="detailLoading" class="detail-loading">
                <div class="detail-skeleton">
                    <div class="detail-skeleton-gallery"></div>
                    <div class="detail-skeleton-info">
                        <div class="skeleton-line w-60"></div>
                        <div class="skeleton-line w-80"></div>
                        <div class="skeleton-line w-40"></div>
                        <div class="skeleton-line w-70"></div>
                    </div>
                </div>
            </div>
            <template v-else-if="detailItem">
                <div class="detail-body">
                    <!-- 左侧：图片 -->
                    <div class="detail-gallery">
                        <div class="detail-gallery-main">
                            <img :src="detailCurrentImage" :alt="detailItem.title" @error="e => e.target.src = defaultImage" />
                            <span class="detail-type-badge" :class="detailItem.type === 1 ? 'badge-found' : 'badge-lost'">
                                {{ detailItem.type === 1 ? '招领' : '寻物' }}
                            </span>
                            <span v-if="detailItem.status === 2" class="detail-status-badge">已解决</span>
                        </div>
                        <div v-if="detailImages.length > 1" class="detail-gallery-thumbs">
                            <div
                                v-for="(img, index) in detailImages"
                                :key="index"
                                class="detail-thumb-item"
                                :class="{ active: detailCurrentIndex === index }"
                                @click="detailCurrentIndex = index"
                            >
                                <img :src="img" :alt="`图片 ${index + 1}`" />
                            </div>
                        </div>
                    </div>

                    <!-- 右侧：信息 -->
                    <div class="detail-info">
                        <div class="detail-info-header">
                            <h2 class="detail-info-title">{{ detailItem.title }}</h2>
                            <div class="detail-info-meta">
                                <span class="detail-meta-category">{{ getCategoryName(detailItem.categoryId) }}</span>
                                <span class="detail-meta-time">{{ formatTime(detailItem.createTime) }}</span>
                            </div>
                        </div>

                        <div class="detail-info-body">
                            <div class="detail-desc-section">
                                <h4 class="detail-section-label">物品描述</h4>
                                <p>{{ detailItem.description || '暂无描述' }}</p>
                            </div>

                            <div class="detail-info-list">
                                <div class="detail-info-item" v-if="detailItem.location">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                                        <circle cx="12" cy="10" r="3"/>
                                    </svg>
                                    <span>{{ detailItem.location }}</span>
                                </div>
                                <div class="detail-info-item">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                        <circle cx="12" cy="7" r="4"/>
                                    </svg>
                                    <span>{{ detailItem.username || '匿名' }}</span>
                                    <span v-if="detailItem.certified" class="detail-cert-badge" title="已通过校园认证">
                                        <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                                            <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/>
                                        </svg>
                                        已认证
                                    </span>
                                </div>
                                <div class="detail-info-item">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <rect x="5" y="2" width="14" height="20" rx="2" ry="2"/>
                                        <line x1="12" y1="18" x2="12.01" y2="18"/>
                                    </svg>
                                    <span>{{ detailItem.contact || '暂无' }}</span>
                                </div>
                            </div>
                        </div>

                        <!-- 操作按钮 -->
                        <div class="detail-actions" v-if="userStore.isLoggedIn">
                            <button
                                v-if="detailIsOwner"
                                class="action-btn action-edit"
                                @click="handleEdit"
                            >
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                                </svg>
                                编辑
                            </button>
                            <button
                                v-if="detailIsOwner && detailItem.status === 1"
                                class="action-btn action-resolve"
                                @click="handleResolve"
                            >
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                                    <polyline points="22 4 12 14.01 9 11.01"/>
                                </svg>
                                标记已解决
                            </button>
                            <button
                                v-if="detailIsOwner"
                                class="action-btn action-delete"
                                @click="handleDelete"
                            >
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <polyline points="3 6 5 6 21 6"/>
                                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                                </svg>
                                下架
                            </button>
                            <button
                                v-if="!detailIsOwner"
                                class="action-btn action-message"
                                @click="showMessageDialog = true"
                            >
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                                </svg>
                                留言私信
                            </button>
                            <button
                                class="action-btn action-share"
                                @click="handleShare"
                            >
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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

                <!-- 智能匹配推荐 -->
                <div v-if="matchItems.length > 0" class="detail-match-section">
                    <div class="detail-match-header">
                        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                        </svg>
                        <span>智能匹配推荐</span>
                    </div>
                    <div class="detail-match-list">
                        <div
                            v-for="m in matchItems"
                            :key="m.id"
                            class="detail-match-card"
                            @click="switchToMatch(m)"
                        >
                            <div class="detail-match-card-img">
                                <img v-if="getMatchImage(m.images)" :src="getMatchImage(m.images)" alt="" />
                                <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#c0c4cc" stroke-width="1.5">
                                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                                    <circle cx="8.5" cy="8.5" r="1.5"/>
                                    <polyline points="21 15 16 10 5 21"/>
                                </svg>
                            </div>
                            <div class="detail-match-card-info">
                                <h4>{{ m.title }}</h4>
                                <p>{{ m.description || '暂无描述' }}</p>
                                <span class="match-type-badge" :class="m.type === 1 ? 'type-found' : 'type-lost'">
                                    {{ m.type === 1 ? '招领' : '寻物' }}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </template>
            <template #footer>
                <span></span>
            </template>
        </el-dialog>

        <!-- 留言对话框 -->
        <el-dialog v-model="showMessageDialog" title="发送私信" width="400px" :close-on-click-modal="false" :append-to-body="true">
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
        <SharePoster :visible="showPoster" :item="detailItem" @close="showPoster = false" />
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { itemApi, categoryApi, messageApi } from '../api'
import { useUserStore } from '../stores/user'
import SharePoster from '../components/SharePoster.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const items = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 12
const activeCategory = ref(0)
const activeType = ref(0)
const searchKeyword = ref('')
const timeRange = ref(0)
const sortOrder = ref('desc')

// 详情弹窗
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailItem = ref(null)
const detailCurrentIndex = ref(0)
const matchItems = ref([])
const showMessageDialog = ref(false)
const messageContent = ref('')
const sendingMessage = ref(false)
const showPoster = ref(false)

const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDMwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjMwMCIgaGVpZ2h0PSIyMDAiIGZpbGw9IiNFNEU3RUQiLz48Y2lyY2xlIGN4PSIxNTAiIGN5PSI4MCIgcj0iMjgiIGZpbGw9IiNDMEM0Q0MiLz48cGF0aCBkPSJNNzUgMTUwTDE1MCA5MEwyMjUgMTUwSDc1TDE1MCA5MEwyMjUgMTUwSDc1WiIgZmlsbD0iI0MwQzRDQyIvPjwvc3ZnPg=='

const detailImages = computed(() => {
    if (!detailItem.value?.images) return []
    try {
        return JSON.parse(detailItem.value.images)
    } catch {
        return []
    }
})

const detailCurrentImage = computed(() => {
    return detailImages.value[detailCurrentIndex.value] || defaultImage
})

const detailIsOwner = computed(() => {
    if (!userStore.isLoggedIn || !detailItem.value) return false
    return userStore.userInfo?.id === detailItem.value.userId
})

function getFirstImage(images) {
    if (!images) return defaultImage
    try {
        const list = JSON.parse(images)
        return list[0] || defaultImage
    } catch {
        return defaultImage
    }
}

function handleImageError(e) {
    e.target.src = defaultImage
}

function formatTime(time) {
    if (!time) return ''
    const date = new Date(time)
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${month}-${day} ${hour}:${minute}`
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

async function fetchItems() {
    loading.value = true
    try {
        const params = {
            page: currentPage.value,
            pageSize: pageSize,
            sort: sortOrder.value
        }
        if (activeCategory.value > 0) params.categoryId = activeCategory.value
        if (activeType.value > 0) params.type = activeType.value
        if (searchKeyword.value.trim()) params.keyword = searchKeyword.value.trim()
        if (timeRange.value > 0) {
            const now = new Date()
            const past = new Date(now.getTime() - timeRange.value * 24 * 60 * 60 * 1000)
            params.startTime = past.toISOString()
            params.endTime = now.toISOString()
        }

        const res = await itemApi.getList(params)
        items.value = res.data?.list || []
        total.value = res.data?.total || 0
    } catch (error) {
        console.error('获取列表失败:', error)
    } finally {
        loading.value = false
    }
}

function switchCategory(id) {
    activeCategory.value = id
    currentPage.value = 1
    fetchItems()
}

function switchType(type) {
    activeType.value = type
    currentPage.value = 1
    fetchItems()
}

function setTimeRange(days) {
    timeRange.value = days
    currentPage.value = 1
    fetchItems()
}

function toggleSort() {
    sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
    currentPage.value = 1
    fetchItems()
}

function handleSearch() {
    currentPage.value = 1
    fetchItems()
}

function clearSearch() {
    searchKeyword.value = ''
    currentPage.value = 1
    fetchItems()
}

function handlePageChange(page) {
    currentPage.value = page
    fetchItems()
    window.scrollTo({ top: 0, behavior: 'smooth' })
}

// ===== 详情弹窗逻辑 =====
async function openDetail(item) {
    detailVisible.value = true
    detailLoading.value = true
    detailCurrentIndex.value = 0
    matchItems.value = []
    try {
        const res = await itemApi.getDetail(item.id)
        detailItem.value = res.data
        // 获取智能匹配推荐
        try {
            const matchRes = await itemApi.smartMatch(item.id)
            if (matchRes.data && matchRes.data.length > 0) {
                matchItems.value = matchRes.data
            }
        } catch (e) {
            console.error('获取匹配推荐失败:', e)
        }
    } catch (error) {
        console.error('获取详情失败:', error)
        ElMessage.error('获取物品详情失败')
        detailVisible.value = false
    } finally {
        detailLoading.value = false
    }
}

function handleDetailClosed() {
    detailItem.value = null
    matchItems.value = []
    detailCurrentIndex.value = 0
}

function switchToMatch(m) {
    openDetail(m)
}

async function handleResolve() {
    try {
        await ElMessageBox.confirm('确认该物品已找到主人？', '确认操作', {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'success'
        })
        await itemApi.update(detailItem.value.id, { status: 2 })
        ElMessage.success('已标记为已解决')
        detailItem.value.status = 2
        // 刷新列表
        fetchItems()
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
        await itemApi.delete(detailItem.value.id)
        ElMessage.success('已下架')
        detailVisible.value = false
        fetchItems()
    } catch (error) {
        if (error !== 'cancel') {
            console.error('操作失败:', error)
        }
    }
}

function handleEdit() {
    router.push(`/edit/${detailItem.value.id}`)
    detailVisible.value = false
}

async function handleSendMessage() {
    if (!messageContent.value.trim()) {
        ElMessage.warning('请输入消息内容')
        return
    }
    sendingMessage.value = true
    try {
        await messageApi.send({
            itemId: detailItem.value.id,
            receiverId: detailItem.value.userId,
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

onMounted(() => {
    fetchCategories()
    fetchItems()
})
</script>

<style scoped>
/* ========== 暗黑模式 ========== */
html.dark .search-box {
    background: #22223a;
    border-color: #2a2a3e;
}

html.dark .search-box:focus-within {
    border-color: #6366f1;
    background: #1a1a2e;
    box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.15);
}

html.dark .search-input {
    color: #c8c8d8;
}

html.dark .search-input::placeholder {
    color: #6a6a8a;
}

html.dark .filter-area {
    background: #22223a;
    border-color: #2a2a3e;
}

html.dark .filter-row + .filter-row {
    border-top-color: #2a2a3e;
}

html.dark .filter-label {
    color: #8a8aa0;
}

html.dark .filter-btn {
    color: #8a8aa0;
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .filter-btn:hover {
    border-color: #6366f1;
    color: #a5b4fc;
}

html.dark .filter-btn.active {
    background: #4f46e5;
    color: #fff;
    border-color: #4f46e5;
}

html.dark .filter-btn.type-found.active {
    background: #059669;
    border-color: #059669;
}

html.dark .filter-btn.type-lost.active {
    background: #dc2626;
    border-color: #dc2626;
}

html.dark .category-chip {
    color: #8a8aa0;
    background: #1a1a2e;
}

html.dark .category-chip:hover {
    background: #2a2a3e;
    color: #a5b4fc;
}

html.dark .category-chip.active {
    background: #4f46e5;
    color: #fff;
}

html.dark .sort-btn {
    color: #8a8aa0;
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .sort-btn:hover {
    border-color: #6366f1;
    color: #a5b4fc;
    background: #2a2a3e;
}

html.dark .content-divider {
    background: #2a2a3e;
}

html.dark .item-card {
    background: #22223a;
    border-color: #2a2a3e;
}

html.dark .item-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
    border-color: transparent;
}

html.dark .item-title {
    color: #e0e0f0;
}

html.dark .item-desc {
    color: #8a8aa0;
}

html.dark .meta-category {
    background: #2a2a3e;
    color: #a5b4fc;
}

html.dark .meta-location {
    color: #8a8aa0;
}

html.dark .item-footer {
    border-top-color: #2a2a3e;
}

html.dark .item-time {
    color: #6a6a8a;
}

html.dark .item-author {
    color: #8a8aa0;
}

html.dark .pagination-section {
    border-top-color: #2a2a3e;
}

html.dark .skeleton-card {
    background: #22223a;
    border-color: #2a2a3e;
}

html.dark .skeleton-image,
html.dark .skeleton-line {
    background: linear-gradient(90deg, #2a2a3e 25%, #3a3a5a 50%, #2a2a3e 75%);
    background-size: 200% 100%;
}

html.dark .empty-section p {
    color: #8a8aa0;
}

html.dark .item-image {
    background: #2a2a3e;
}

html.dark .page-header-bar {
    border-bottom-color: #2a2a3e;
}

html.dark .page-header-title {
    color: #e0e0f0;
}

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

/* ========== 首页 ========== */
.home-page {
    min-height: 100%;
}

.main-content {
    max-width: 1200px;
    margin: 0 auto;
}

/* 搜索栏 */
.search-section {
    margin-bottom: 16px;
}

.search-box {
    display: flex;
    align-items: center;
    background: #fafaff;
    border-radius: 6px;
    padding: 3px 3px 3px 12px;
    border: 1px solid #f0f0f0;
    transition: all 0.25s;
}

.search-box:focus-within {
    border-color: #4f46e5;
    background: #fff;
    box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.08);
}

.search-icon {
    color: #a0aec0;
    flex-shrink: 0;
    width: 16px;
    height: 16px;
}

.search-input {
    flex: 1;
    border: none;
    outline: none;
    padding: 6px 8px;
    font-size: 13px;
    color: #4a4a6a;
    background: transparent;
}

.search-input::placeholder {
    color: #a0aec0;
}

.search-clear {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border: none;
    background: transparent;
    color: #a0aec0;
    cursor: pointer;
    border-radius: 50%;
    transition: background 0.2s;
}

.search-clear:hover {
    background: #f0f0f5;
}

.search-btn {
    padding: 6px 16px;
    background: #4f46e5;
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 13px;
    cursor: pointer;
    transition: background 0.2s;
    white-space: nowrap;
}

.search-btn:hover {
    background: #4338ca;
}

/* 筛选区域 */
.filter-area {
    background: #fafaff;
    border-radius: 8px;
    border: 1px solid #f0f0f0;
    overflow: hidden;
}

.filter-row {
    display: flex;
    align-items: center;
    padding: 10px 16px;
    gap: 12px;
}

.filter-row + .filter-row {
    border-top: 1px solid #f0f0f0;
}

.filter-label {
    font-size: 13px;
    color: #6a6a8a;
    white-space: nowrap;
    min-width: 36px;
}

.filter-options {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    flex: 1;
}

.filter-btn {
    padding: 4px 12px;
    border: 1px solid #e8e8f0;
    background: #fff;
    border-radius: 4px;
    font-size: 12px;
    color: #6a6a8a;
    cursor: pointer;
    transition: all 0.2s;
}

.filter-btn:hover {
    border-color: #4f46e5;
    color: #4f46e5;
}

.filter-btn.active {
    background: #4f46e5;
    color: #fff;
    border-color: #4f46e5;
}

.filter-btn.type-found.active {
    background: #059669;
    border-color: #059669;
}

.filter-btn.type-lost.active {
    background: #dc2626;
    border-color: #dc2626;
}

.category-chip {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 4px;
    font-size: 12px;
    color: #6a6a8a;
    background: #fff;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #e8e8f0;
}

.category-chip:hover {
    background: #f0f0ff;
    color: #4f46e5;
    border-color: #c7d2fe;
}

.category-chip.active {
    background: #4f46e5;
    color: #fff;
    border-color: #4f46e5;
}

.filter-right {
    flex-shrink: 0;
}

.sort-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    border: 1px solid #e8e8f0;
    background: #fff;
    border-radius: 4px;
    font-size: 12px;
    color: #6a6a8a;
    cursor: pointer;
    transition: all 0.2s;
}

.sort-btn:hover {
    border-color: #4f46e5;
    color: #4f46e5;
    background: #f0f0ff;
}

/* 分隔线 */
.content-divider {
    height: 1px;
    background: #f0f0f0;
    margin: 16px 0;
}

/* 物品网格 */
.items-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
}

@media (max-width: 1024px) {
    .items-grid {
        grid-template-columns: repeat(3, 1fr);
    }
}

@media (max-width: 768px) {
    .items-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 480px) {
    .items-grid {
        grid-template-columns: 1fr;
    }
}

.item-card {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #f0f0f0;
    cursor: pointer;
    transition: all 0.25s;
}

.item-card:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    border-color: transparent;
    transform: translateY(-2px);
}

.item-image {
    position: relative;
    width: 100%;
    height: 160px;
    overflow: hidden;
    background: #f5f5f5;
}

.item-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s;
}

.item-card:hover .item-image img {
    transform: scale(1.05);
}

.item-type-badge {
    position: absolute;
    top: 8px;
    left: 8px;
    padding: 2px 8px;
    border-radius: 3px;
    font-size: 11px;
    font-weight: 500;
    color: #fff;
}

.badge-found {
    background: #059669;
}

.badge-lost {
    background: #dc2626;
}

.item-status-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    padding: 2px 8px;
    border-radius: 3px;
    font-size: 11px;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
}

.item-info {
    padding: 10px 12px;
}

.item-title {
    font-size: 14px;
    font-weight: 600;
    color: #4a4a6a;
    margin: 0 0 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.item-desc {
    font-size: 12px;
    color: #8a8aa0;
    margin: 0 0 8px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.item-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;
}

.meta-category {
    font-size: 11px;
    padding: 1px 6px;
    background: #f0f0ff;
    color: #4f46e5;
    border-radius: 3px;
}

.meta-location {
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 11px;
    color: #8a8aa0;
}

.item-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 8px;
    border-top: 1px solid #f0f0f0;
}

.item-time {
    font-size: 11px;
    color: #a0aec0;
}

.item-author {
    font-size: 11px;
    color: #8a8aa0;
}

/* 分页 */
.pagination-section {
    display: flex;
    justify-content: center;
    padding: 24px 0;
    margin-top: 16px;
    border-top: 1px solid #f0f0f0;
}

/* 骨架屏 */
.loading-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
}

@media (max-width: 1024px) {
    .loading-grid {
        grid-template-columns: repeat(3, 1fr);
    }
}

@media (max-width: 768px) {
    .loading-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 480px) {
    .loading-grid {
        grid-template-columns: 1fr;
    }
}

.skeleton-card {
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #f0f0f0;
}

.skeleton-image {
    width: 100%;
    height: 160px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
}

.skeleton-body {
    padding: 12px;
}

.skeleton-line {
    height: 12px;
    margin-bottom: 8px;
    border-radius: 4px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
}

.skeleton-line:last-child {
    margin-bottom: 0;
}

.w-60 { width: 60%; }
.w-80 { width: 80%; }
.w-40 { width: 40%; }
.w-70 { width: 70%; }

@keyframes shimmer {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}

/* 空状态 */
.empty-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 80px 20px;
}

.empty-section p {
    margin-top: 16px;
    font-size: 14px;
    color: #a0aec0;
}

/* ========== 详情弹窗 ========== */
.detail-dialog {
    border-radius: 8px;
}

.detail-dialog :deep(.el-dialog__header) {
    padding: 16px 20px;
    margin: 0;
    border-bottom: 1px solid #f0f0f0;
}

.detail-dialog :deep(.el-dialog__title) {
    font-size: 16px;
    font-weight: 600;
    color: #4a4a6a;
}

.detail-dialog :deep(.el-dialog__body) {
    padding: 20px;
}

.detail-dialog :deep(.el-dialog__footer) {
    display: none;
}

/* 详情加载 */
.detail-loading {
    padding: 20px 0;
}

.detail-skeleton {
    display: flex;
    gap: 20px;
}

.detail-skeleton-gallery {
    width: 320px;
    height: 240px;
    border-radius: 6px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e8e8e8 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    flex-shrink: 0;
}

.detail-skeleton-info {
    flex: 1;
}

/* 详情主体 */
.detail-body {
    display: flex;
    gap: 24px;
}

/* 左侧图片 */
.detail-gallery {
    width: 320px;
    flex-shrink: 0;
}

.detail-gallery-main {
    position: relative;
    width: 100%;
    height: 240px;
    border-radius: 6px;
    overflow: hidden;
    background: #f5f5f5;
}

.detail-gallery-main img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.detail-type-badge {
    position: absolute;
    top: 10px;
    left: 10px;
    padding: 3px 10px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    color: #fff;
}

.detail-status-badge {
    position: absolute;
    top: 10px;
    right: 10px;
    padding: 3px 10px;
    border-radius: 4px;
    font-size: 12px;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
}

.detail-gallery-thumbs {
    display: flex;
    gap: 8px;
    margin-top: 8px;
}

.detail-thumb-item {
    width: 56px;
    height: 42px;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
    border: 2px solid transparent;
    transition: border-color 0.2s;
}

.detail-thumb-item.active {
    border-color: #4f46e5;
}

.detail-thumb-item img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

/* 右侧信息 */
.detail-info {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
}

.detail-info-header {
    margin-bottom: 16px;
}

.detail-info-title {
    font-size: 18px;
    font-weight: 600;
    color: #4a4a6a;
    margin: 0 0 8px;
}

.detail-info-meta {
    display: flex;
    align-items: center;
    gap: 10px;
}

.detail-meta-category {
    font-size: 12px;
    padding: 2px 8px;
    background: #f0f0ff;
    color: #4f46e5;
    border-radius: 4px;
}

.detail-meta-time {
    font-size: 12px;
    color: #a0aec0;
}

.detail-info-body {
    flex: 1;
}

.detail-desc-section {
    margin-bottom: 16px;
}

.detail-section-label {
    font-size: 13px;
    font-weight: 600;
    color: #4a4a6a;
    margin: 0 0 6px;
}

.detail-desc-section p {
    font-size: 13px;
    color: #6a6a8a;
    line-height: 1.6;
    margin: 0;
}

.detail-info-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.detail-info-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #6a6a8a;
}

.detail-info-item svg {
    flex-shrink: 0;
    color: #a0aec0;
}

.detail-cert-badge {
    display: inline-flex;
    align-items: center;
    gap: 2px;
    font-size: 11px;
    color: #059669;
    background: #ecfdf5;
    padding: 1px 6px;
    border-radius: 3px;
}

/* 操作按钮 */
.detail-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
}

.action-btn {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 6px 14px;
    border: 1px solid #e8e8f0;
    background: #fff;
    border-radius: 6px;
    font-size: 12px;
    color: #6a6a8a;
    cursor: pointer;
    transition: all 0.2s;
}

.action-btn:hover {
    border-color: #c7d2fe;
    background: #f0f0ff;
    color: #4f46e5;
}

.action-edit:hover {
    border-color: #c7d2fe;
    background: #f0f0ff;
    color: #4f46e5;
}

.action-resolve:hover {
    border-color: #a7f3d0;
    background: #ecfdf5;
    color: #059669;
}

.action-delete:hover {
    border-color: #fecaca;
    background: #fef2f2;
    color: #dc2626;
}

.action-message:hover {
    border-color: #c7d2fe;
    background: #f0f0ff;
    color: #4f46e5;
}

.action-share:hover {
    border-color: #fde68a;
    background: #fffbeb;
    color: #d97706;
}

/* 智能匹配推荐 */
.detail-match-section {
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
}

.detail-match-header {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    font-weight: 600;
    color: #4a4a6a;
    margin-bottom: 12px;
}

.detail-match-header svg {
    color: #f43f5e;
}

.detail-match-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.detail-match-card {
    display: flex;
    gap: 12px;
    padding: 10px;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
}

.detail-match-card:hover {
    background: #fafaff;
    border-color: #c7d2fe;
}

.detail-match-card-img {
    width: 64px;
    height: 48px;
    border-radius: 4px;
    overflow: hidden;
    flex-shrink: 0;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
}

.detail-match-card-img img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.detail-match-card-info {
    flex: 1;
    min-width: 0;
}

.detail-match-card-info h4 {
    font-size: 13px;
    font-weight: 600;
    color: #4a4a6a;
    margin: 0 0 2px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.detail-match-card-info p {
    font-size: 12px;
    color: #8a8aa0;
    margin: 0 0 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.match-type-badge {
    display: inline-block;
    padding: 1px 6px;
    border-radius: 3px;
    font-size: 10px;
    color: #fff;
}

.match-type-badge.type-found {
    background: #059669;
}

.match-type-badge.type-lost {
    background: #dc2626;
}

/* 留言表单 */
.message-form {
    position: relative;
}

.message-textarea {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid #e8e8f0;
    border-radius: 6px;
    font-size: 13px;
    color: #4a4a6a;
    resize: none;
    outline: none;
    transition: border-color 0.2s;
    box-sizing: border-box;
    font-family: inherit;
}

.message-textarea:focus {
    border-color: #4f46e5;
}

.message-count {
    position: absolute;
    bottom: 8px;
    right: 8px;
    font-size: 11px;
    color: #a0aec0;
}

/* 暗黑模式 - 详情弹窗 */
html.dark .detail-dialog :deep(.el-dialog__header) {
    border-bottom-color: #2a2a3e;
}

html.dark .detail-dialog :deep(.el-dialog__title) {
    color: #e0e0f0;
}

html.dark .detail-info-title {
    color: #e0e0f0;
}

html.dark .detail-section-label {
    color: #c8c8d8;
}

html.dark .detail-desc-section p {
    color: #8a8aa0;
}

html.dark .detail-info-item {
    color: #8a8aa0;
}

html.dark .detail-actions {
    border-top-color: #2a2a3e;
}

html.dark .action-btn {
    background: #1a1a2e;
    border-color: #2a2a3e;
    color: #8a8aa0;
}

html.dark .action-btn:hover {
    background: #2a2a3e;
    border-color: #6366f1;
    color: #a5b4fc;
}

html.dark .action-resolve:hover {
    border-color: #059669;
    color: #34d399;
    background: #064e3b;
}

html.dark .action-delete:hover {
    border-color: #dc2626;
    color: #f87171;
    background: #450a0a;
}

html.dark .action-share:hover {
    border-color: #d97706;
    color: #fbbf24;
    background: #451a03;
}

html.dark .detail-match-section {
    border-top-color: #2a2a3e;
}

html.dark .detail-match-header {
    color: #e0e0f0;
}

html.dark .detail-match-card {
    border-color: #2a2a3e;
}

html.dark .detail-match-card:hover {
    background: #1a1a2e;
    border-color: #6366f1;
}

html.dark .detail-match-card-info h4 {
    color: #e0e0f0;
}

html.dark .detail-match-card-info p {
    color: #8a8aa0;
}

html.dark .detail-cert-badge {
    background: #064e3b;
    color: #34d399;
}

html.dark .detail-meta-category {
    background: #2a2a3e;
    color: #a5b4fc;
}

html.dark .detail-meta-time {
    color: #6a6a8a;
}

html.dark .message-textarea {
    background: #1a1a2e;
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark .message-textarea:focus {
    border-color: #6366f1;
}

html.dark .detail-gallery-main {
    background: #2a2a3e;
}

html.dark .detail-match-card-img {
    background: #2a2a3e;
}
</style>
