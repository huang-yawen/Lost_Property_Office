<template>
    <div class="my-items-page">
        <div class="my-items-container" style="padding-top: 0px;margin-top: 0px;">
            <!-- 页面头部 + 统计卡片（同一行）- 参考个人中心样式 -->
            <div class="page-header-row">
                <div class="page-header">
                    <h1 class="page-title">我的发布</h1>
                    <p class="page-desc">管理您发布的招领和寻物信息</p>
                </div>
                <div class="header-right">
                    <div class="profile-stats">
                        <div class="stat-item">
                            <span class="stat-value">{{ items.length }}</span>
                            <span class="stat-label">全部</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-value">{{ items.filter(i => i.type === 1).length }}</span>
                            <span class="stat-label">招领</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-value">{{ items.filter(i => i.type === 2).length }}</span>
                            <span class="stat-label">寻物</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-value">{{ items.filter(i => i.status === 2).length }}</span>
                            <span class="stat-label">已解决</span>
                        </div>
                    </div>
                    <router-link to="/create" class="header-create-btn">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="12" y1="5" x2="12" y2="19"/>
                            <line x1="5" y1="12" x2="19" y2="12"/>
                        </svg>
                        去发布
                    </router-link>
                </div>
            </div>

            <!-- 筛选区域 - 参考首页样式 -->
            <div class="filter-area">
                <div class="filter-row">
                    <div class="filter-label">状态</div>
                    <div class="filter-options">
                        <button
                            class="filter-btn"
                            :class="{ active: currentFilter === 'all' }"
                            @click="currentFilter = 'all'"
                        >全部</button>
                        <button
                            class="filter-btn type-found"
                            :class="{ active: currentFilter === 'found' }"
                            @click="currentFilter = 'found'"
                        >招领</button>
                        <button
                            class="filter-btn type-lost"
                            :class="{ active: currentFilter === 'lost' }"
                            @click="currentFilter = 'lost'"
                        >寻物</button>
                        <button
                            class="filter-btn"
                            :class="{ active: currentFilter === 'resolved' }"
                            @click="currentFilter = 'resolved'"
                        >已解决</button>
                    </div>
                    <div class="filter-right">
                        <button class="sort-btn" @click="toggleSort">
                            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="21" y1="18" x2="11" y2="18"/>
                                <polyline :points="sortOrder === 'desc' ? '3 10 7 6 11 10' : '3 14 7 18 11 14'"/>
                                <line x1="21" y1="6" x2="15" y2="6"/>
                            </svg>
                            {{ sortOrder === 'desc' ? '最新优先' : '最早优先' }}
                        </button>
                    </div>
                </div>
            </div>

            <!-- 加载状态 -->
            <div v-if="loading" class="loading-section">
                <div class="skeleton-list">
                    <div v-for="i in 3" :key="i" class="skeleton-item">
                        <div class="skeleton-img"></div>
                        <div class="skeleton-content">
                            <div class="skeleton-line w-70"></div>
                            <div class="skeleton-line w-50"></div>
                            <div class="skeleton-line w-30"></div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 空状态 -->
            <div v-else-if="filteredItems.length === 0" class="empty-section">
                <div class="empty-icon">
                    <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                        <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
                        <line x1="12" y1="22.08" x2="12" y2="12"/>
                    </svg>
                </div>
                <h3 class="empty-title">暂无发布信息</h3>
                <p class="empty-desc">您还没有发布任何招领或寻物信息</p>
            </div>

            <!-- 物品列表 -->
            <div v-else class="items-list">
                <div
                    v-for="item in filteredItems"
                    :key="item.id"
                    class="item-card"
                    @click="openItemDialog(item)"
                >
                    <div class="item-image">
                        <img :src="getFirstImage(item.images)" :alt="item.title" @error="handleImageError" />
                        <div class="item-type-badge" :class="item.type === 1 ? 'badge-found' : 'badge-lost'">
                            {{ item.type === 1 ? '招领' : '寻物' }}
                        </div>
                        <div v-if="item.status === 2" class="item-resolved-overlay">
                            <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                                <polyline points="22 4 12 14.01 9 11.01"/>
                            </svg>
                            <span>已解决</span>
                        </div>
                    </div>
                    <div class="item-info">
                        <div class="item-header">
                            <h3 class="item-title" @click="router.push(`/item/${item.id}`)">{{ item.title }}</h3>
                            <span class="item-status" :class="item.status === 2 ? 'status-resolved' : 'status-active'">
                                <span class="status-dot"></span>
                                {{ item.status === 2 ? '已解决' : '进行中' }}
                            </span>
                        </div>
                        <p class="item-desc">{{ item.description || '暂无描述' }}</p>
                        <div class="item-meta">
                            <span class="meta-category">{{ getCategoryName(item.categoryId) }}</span>
                            <span class="meta-divider"></span>
                            <span class="meta-time">{{ formatTime(item.createTime) }}</span>
                        </div>
                        <div class="item-actions" @click.stop>
                            <button class="action-btn action-edit" @click.stop="router.push(`/edit/${item.id}`)">
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                                </svg>
                                编辑
                            </button>
                            <button
                                v-if="item.status === 1"
                                class="action-btn action-resolve"
                                @click="handleResolve(item)"
                            >
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                                    <polyline points="22 4 12 14.01 9 11.01"/>
                                </svg>
                                标记已解决
                            </button>
                            <button class="action-btn action-delete" @click.stop="handleDelete(item)">
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <polyline points="3 6 5 6 21 6"/>
                                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                                </svg>
                                下架
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 物品详情弹窗 -->
            <el-dialog
                v-model="itemDialogVisible"
                :title="dialogItem?.title || '物品详情'"
                width="700px"
                destroy-on-close
                class="item-detail-dialog"
            >
                <template v-if="dialogItem">
                    <div class="dialog-item-detail">
                        <!-- 图片 -->
                        <div class="dialog-gallery">
                            <div class="dialog-gallery-main">
                                <img :src="getFirstImage(dialogItem.images)" :alt="dialogItem.title" @error="handleImageError" />
                                <span class="dialog-type-badge" :class="dialogItem.type === 1 ? 'badge-found' : 'badge-lost'">
                                    {{ dialogItem.type === 1 ? '招领' : '寻物' }}
                                </span>
                                <span v-if="dialogItem.status === 2" class="dialog-status-badge">已解决</span>
                            </div>
                        </div>
                        <!-- 信息 -->
                        <div class="dialog-info">
                            <div class="dialog-info-header">
                                <h2>{{ dialogItem.title }}</h2>
                                <div class="dialog-info-meta">
                                    <span class="dialog-category">{{ getCategoryName(dialogItem.categoryId) }}</span>
                                    <span class="dialog-time">{{ formatTime(dialogItem.createTime) }}</span>
                                </div>
                            </div>
                            <div class="dialog-info-body">
                                <div class="dialog-desc">
                                    <h4>物品描述</h4>
                                    <p>{{ dialogItem.description || '暂无描述' }}</p>
                                </div>
                                <div class="dialog-detail-items">
                                    <div class="dialog-detail-item">
                                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                            <circle cx="12" cy="7" r="4"/>
                                        </svg>
                                        <span>{{ dialogItem.username || '匿名' }}</span>
                                    </div>
                                    <div class="dialog-detail-item" v-if="dialogItem.location">
                                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                                            <circle cx="12" cy="10" r="3"/>
                                        </svg>
                                        <span>{{ dialogItem.location }}</span>
                                    </div>
                                    <div class="dialog-detail-item">
                                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <rect x="5" y="2" width="14" height="20" rx="2" ry="2"/>
                                            <line x1="12" y1="18" x2="12.01" y2="18"/>
                                        </svg>
                                        <span>{{ dialogItem.contact || '暂无' }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </template>
            </el-dialog>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { itemApi, categoryApi } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const items = ref([])
const categories = ref([])
const currentFilter = ref('all')
const sortOrder = ref('desc')
const itemDialogVisible = ref(false)
const dialogItem = ref(null)

function openItemDialog(item) {
    dialogItem.value = item
    itemDialogVisible.value = true
}

const filteredItems = computed(() => {
    let list = items.value
    if (currentFilter.value === 'found') {
        list = list.filter(i => i.type === 1)
    } else if (currentFilter.value === 'lost') {
        list = list.filter(i => i.type === 2)
    } else if (currentFilter.value === 'resolved') {
        list = list.filter(i => i.status === 2)
    }
    // 按时间排序
    list = [...list].sort((a, b) => {
        const timeA = new Date(a.createTime).getTime()
        const timeB = new Date(b.createTime).getTime()
        return sortOrder.value === 'desc' ? timeB - timeA : timeA - timeB
    })
    return list
})

function toggleSort() {
    sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
}

const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjIwMCIgaGVpZ2h0PSIyMDAiIGZpbGw9IiNFNEU3RUQiLz48Y2lyY2xlIGN4PSIxMDAiIGN5PSI4MCIgcj0iMjgiIGZpbGw9IiNDMEM0Q0MiLz48cGF0aCBkPSJNNTAgMTUwTDEwMCA5MEwxNTAgMTUwSDUwWiIgZmlsbD0iI0MwQzRDQyIvPjwvc3ZnPg=='

function getFirstImage(images) {
    if (!images) return defaultImage
    try {
        const urls = JSON.parse(images)
        return urls[0] || defaultImage
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

async function fetchItems() {
    loading.value = true
    try {
        const res = await itemApi.getUserItems()
        items.value = res.data?.list || []
    } catch (error) {
        console.error('获取物品列表失败:', error)
        ElMessage.error('获取列表失败')
    } finally {
        loading.value = false
    }
}

async function handleResolve(item) {
    try {
        await ElMessageBox.confirm('确认该物品已找到主人？', '确认操作', {
            confirmButtonText: '确认',
            cancelButtonText: '取消',
            type: 'success'
        })
        await itemApi.update(item.id, { status: 2 })
        ElMessage.success('已标记为已解决')
        item.status = 2
    } catch (error) {
        if (error !== 'cancel') {
            console.error('操作失败:', error)
        }
    }
}

async function handleDelete(item) {
    try {
        await ElMessageBox.confirm('下架后该信息将不再展示，确定要下架吗？', '确认下架', {
            confirmButtonText: '确认下架',
            cancelButtonText: '取消',
            type: 'warning'
        })
        await itemApi.delete(item.id)
        ElMessage.success('已下架')
        items.value = items.value.filter(i => i.id !== item.id)
    } catch (error) {
        if (error !== 'cancel') {
            console.error('操作失败:', error)
        }
    }
}

onMounted(() => {
    if (!userStore.isLoggedIn) {
        router.push('/login')
        return
    }
    fetchCategories()
    fetchItems()
})
</script>

<style scoped>
/* ========== 暗黑模式 ========== */
html.dark .my-items-page {
    background: transparent;
}

html.dark .page-title {
    color: #e0e0f0;
}

html.dark .page-desc {
    color: #6a6a8a;
}

html.dark .page-header-row {
    border-bottom-color: #2a2a3e;
}

html.dark .stat-value {
    color: #a5b4fc;
}

html.dark .stat-label {
    color: #6a6a8a;
}

html.dark .filter-area {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .filter-label {
    color: #8a8aa0;
}

html.dark .filter-btn {
    background: #22223a;
    border-color: #2a2a3e;
    color: #8a8aa0;
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

html.dark .sort-btn {
    background: #22223a;
    border-color: #2a2a3e;
    color: #8a8aa0;
}

html.dark .sort-btn:hover {
    border-color: #6366f1;
    color: #a5b4fc;
    background: #1a1a2e;
}

html.dark .loading-section {
    background: rgba(26, 26, 46, 0.5);
    border-color: #2a2a3e;
}

html.dark .skeleton-item + .skeleton-item {
    border-top-color: #2a2a3e;
}

html.dark .skeleton-img {
    background: linear-gradient(90deg, #1a1a2e 25%, #2a2a3e 50%, #1a1a2e 75%);
    background-size: 200% 100%;
}

html.dark .skeleton-line {
    background: linear-gradient(90deg, #1a1a2e 25%, #2a2a3e 50%, #1a1a2e 75%);
    background-size: 200% 100%;
}

html.dark .empty-section {
    background: rgba(26, 26, 46, 0.4);
    border-color: #2a2a3e;
}

html.dark .empty-icon {
    background: rgba(99, 102, 241, 0.15);
    color: #a5b4fc;
}

html.dark .empty-title {
    color: #e0e0f0;
}

html.dark .empty-desc {
    color: #6a6a8a;
}

html.dark .item-card {
    background: rgba(26, 26, 46, 0.5);
    border-color: #2a2a3e;
}

html.dark .item-card:hover {
    border-color: #3a3a5e;
    background: rgba(26, 26, 46, 0.7);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

html.dark .item-image {
    background: #0f0f1a;
}

html.dark .item-title {
    color: #e0e0f0;
}

html.dark .item-title:hover {
    color: #a5b4fc;
}

html.dark .status-active {
    background: rgba(245, 158, 11, 0.15);
    color: #fbbf24;
}

html.dark .status-resolved {
    background: rgba(16, 185, 129, 0.15);
    color: #34d399;
}

html.dark .item-desc {
    color: #6a6a8a;
}

html.dark .meta-category {
    background: rgba(99, 102, 241, 0.15);
    color: #a5b4fc;
}

html.dark .meta-divider {
    background: #3a3a5e;
}

html.dark .meta-time {
    color: #6a6a8a;
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
    border-color: #059669;
    color: #34d399;
    background: rgba(5, 150, 105, 0.1);
}

html.dark .action-delete:hover {
    border-color: #dc2626;
    color: #f87171;
    background: rgba(220, 38, 38, 0.1);
}

/* ========== 我的发布页 ========== */
.my-items-page {
    min-height: 100%;
}

.my-items-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 32px 0 60px;
}

/* ===== 页面头部行（标题 + 统计 + 按钮同一行） ===== */
.page-header-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
    margin-bottom: 28px;
    padding-bottom: 28px;
    border-bottom: 1px solid #e2e8f0;
}

.page-header {
    flex-shrink: 0;
}

.page-title {
    font-size: 26px;
    font-weight: 700;
    color: #1a1a2e;
    margin: 0 0 4px;
    letter-spacing: -0.3px;
    white-space: nowrap;
}

.page-desc {
    font-size: 14px;
    color: #8b8fa3;
    margin: 0;
    white-space: nowrap;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-left: auto;
}

/* 统计数据 - 参考个人中心样式 */
.profile-stats {
    display: grid;
    grid-template-columns: repeat(4, auto);
    gap: 28px;
    flex-shrink: 0;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}

.stat-value {
    font-size: 24px;
    font-weight: 700;
    color: #4f46e5;
}

.stat-label {
    font-size: 13px;
    color: #a0aec0;
}

.header-create-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 20px;
    background: linear-gradient(135deg, #4f46e5, #6366f1);
    color: #fff;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 600;
    text-decoration: none;
    transition: all 0.25s;
    box-shadow: 0 2px 8px rgba(79, 70, 229, 0.25);
    flex-shrink: 0;
}

.header-create-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(79, 70, 229, 0.35);
}

/* ===== 筛选区域 - 参考首页样式 ===== */
.filter-area {
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    padding: 10px 14px;
    background: #fafaff;
    margin-bottom: 24px;
}

.filter-row {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 3px 0;
}

.filter-label {
    font-size: 12px;
    font-weight: 500;
    color: #4a4a6a;
    min-width: 30px;
    flex-shrink: 0;
}

.filter-options {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-wrap: wrap;
    flex: 1;
}

.filter-btn {
    padding: 3px 10px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 500;
    color: #4a4a6a;
    background: #fff;
    border: 1px solid #e0e0f0;
    cursor: pointer;
    transition: all 0.2s;
    line-height: 1.4;
}

.filter-btn:hover {
    border-color: #a5b4fc;
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

/* 右侧排序 */
.filter-right {
    flex-shrink: 0;
    margin-left: auto;
}

.sort-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 3px 10px;
    border-radius: 4px;
    font-size: 11px;
    color: #4a4a6a;
    background: #fff;
    border: 1px solid #e0e0f0;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
}

.sort-btn:hover {
    border-color: #a5b4fc;
    color: #4f46e5;
    background: #f5f5ff;
}

/* ===== 加载状态 ===== */
.loading-section {
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    overflow: hidden;
    background: rgba(255, 255, 255, 0.5);
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
    border-top: 1px solid #eef1f5;
}

.skeleton-img {
    width: 100px;
    height: 100px;
    border-radius: 10px;
    background: linear-gradient(90deg, #eef2f7 25%, #e2e8f0 50%, #eef2f7 75%);
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
    background: linear-gradient(90deg, #eef2f7 25%, #e2e8f0 50%, #eef2f7 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4px;
}

.skeleton-line.w-70 { width: 70%; }
.skeleton-line.w-50 { width: 50%; }
.skeleton-line.w-30 { width: 30%; }

@keyframes shimmer {
    0% { background-position: 200% 0; }
    100% { background-position: -200% 0; }
}

/* ===== 空状态 ===== */
.empty-section {
    text-align: center;
    padding: 80px 20px;
    background: rgba(255, 255, 255, 0.4);
    border: 1px solid #e2e8f0;
    border-radius: 8px;
}

.empty-icon {
    width: 80px;
    height: 80px;
    margin: 0 auto 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f0f0ff;
    border-radius: 20px;
    color: #4f46e5;
}

.empty-title {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a2e;
    margin: 0 0 8px;
}

.empty-desc {
    font-size: 14px;
    color: #8b8fa3;
    margin: 0;
}

/* ===== 物品列表 ===== */
.items-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
}

.item-card {
    display: flex;
    height: 170px;
    border-radius: 6px;
    border: 1px solid #e2e8f0;
    overflow: hidden;
    transition: all 0.3s;
    background: rgba(255, 255, 255, 0.5);
}

.item-card:hover {
    border-color: #cbd5e1;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
    background: rgba(255, 255, 255, 0.7);
}

.item-image {
    position: relative;
    width: 200px;
    min-height: 170px;
    flex-shrink: 0;
    cursor: pointer;
    background: #eef2f7;
    overflow: hidden;
}

.item-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s;
}

.item-card:hover .item-image img {
    transform: scale(1.05);
}

.item-type-badge {
    position: absolute;
    top: 10px;
    left: 10px;
    padding: 4px 12px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;
    letter-spacing: 0.3px;
}

.badge-found {
    background: linear-gradient(135deg, #059669, #10b981);
    box-shadow: 0 2px 6px rgba(5, 150, 105, 0.3);
}

.badge-lost {
    background: linear-gradient(135deg, #dc2626, #ef4444);
    box-shadow: 0 2px 6px rgba(220, 38, 38, 0.3);
}

.item-resolved-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.55);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    color: #fff;
    font-size: 14px;
    font-weight: 600;
    letter-spacing: 0.5px;
}

.item-info {
    flex: 1;
    padding: 20px 24px;
    display: flex;
    flex-direction: column;
    min-width: 0;
}

.item-header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 10px;
}

.item-title {
    font-size: 16px;
    font-weight: 600;
    color: #1a1a2e;
    cursor: pointer;
    transition: color 0.2s;
    margin: 0;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.item-title:hover {
    color: #4f46e5;
}

.item-status {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 4px 12px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 600;
    white-space: nowrap;
    flex-shrink: 0;
}

.status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    display: inline-block;
}

.status-active {
    background: #fef3c7;
    color: #92400e;
}

.status-active .status-dot {
    background: #f59e0b;
    animation: pulse-dot 1.5s infinite;
}

@keyframes pulse-dot {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.4; }
}

.status-resolved {
    background: #d1fae5;
    color: #065f46;
}

.status-resolved .status-dot {
    background: #10b981;
}

.item-desc {
    font-size: 13px;
    color: #6b7280;
    margin-bottom: 14px;
    line-height: 1.6;
    display: none;
}

.item-meta {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 14px;
}

.meta-category {
    padding: 3px 10px;
    background: #eef2ff;
    color: #4f46e5;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
}

.meta-divider {
    width: 3px;
    height: 3px;
    border-radius: 50%;
    background: #d1d5db;
}

.meta-time {
    font-size: 12px;
    color: #9ca3af;
}

.item-actions {
    display: flex;
    gap: 8px;
}

.action-btn {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 7px 16px;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
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
    box-shadow: 0 2px 8px rgba(79, 70, 229, 0.12);
}

.action-resolve:hover {
    border-color: #059669;
    color: #059669;
    background: #f0faf5;
    box-shadow: 0 2px 8px rgba(5, 150, 105, 0.12);
}

.action-delete:hover {
    border-color: #dc2626;
    color: #dc2626;
    background: #fef2f2;
    box-shadow: 0 2px 8px rgba(220, 38, 38, 0.12);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
    .my-items-container {
        padding: 24px 16px 40px;
    }

    .page-header-row {
        flex-wrap: wrap;
        gap: 16px;
    }

    .page-header {
        width: 100%;
    }

    .page-title {
        font-size: 22px;
    }

    .header-right {
        width: 100%;
        justify-content: flex-end;
    }

    .profile-stats {
        gap: 16px;
        justify-content: flex-start;
    }

    .header-create-btn {
        width: 100%;
        justify-content: center;
    }

    .item-card {
        flex-direction: column;
    }

    .item-image {
        width: 100%;
        height: 200px;
    }

    .item-info {
        padding: 16px;
    }

    .item-actions {
        flex-wrap: wrap;
    }
}

/* ===== 物品详情弹窗 ===== */
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
    min-height: 400px;
}

.dialog-gallery {
    position: relative;
    background: #f0f2f5;
}

.dialog-gallery-main {
    position: relative;
    height: 400px;
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

html.dark .dialog-gallery,
html.dark .dialog-gallery-main {
    background: #0f0f1a;
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

@media (max-width: 480px) {
    .profile-stats {
        gap: 12px;
    }

    .stat-value {
        font-size: 20px;
    }

    .stat-label {
        font-size: 11px;
    }
}
</style>