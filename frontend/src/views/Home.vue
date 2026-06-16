<template>
    <div class="home-page">
        <div class="main-content">
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
                    @click="router.push(`/item/${item.id}`)"
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
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { itemApi, categoryApi } from '../api'

const router = useRouter()

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

const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDMwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjMwMCIgaGVpZ2h0PSIyMDAiIGZpbGw9IiNFNEU3RUQiLz48Y2lyY2xlIGN4PSIxNTAiIGN5PSI4MCIgcj0iMjgiIGZpbGw9IiNDMEM0Q0MiLz48cGF0aCBkPSJNNzUgMTUwTDE1MCA5MEwyMjUgMTUwSDc1TDE1MCA5MEwyMjUgMTUwSDc1WiIgZmlsbD0iI0MwQzRDQyIvPjwvc3ZnPg=='

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

/* ========== 首页 ========== */
.home-page {
    min-height: 100%;
}

/* 主内容 */
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
    background: none;
    border: none;
    padding: 4px;
    cursor: pointer;
    color: #a0aec0;
    display: flex;
    align-items: center;
    transition: color 0.2s;
}

.search-clear:hover {
    color: #4a5568;
}

.search-btn {
    padding: 5px 14px;
    background: #4f46e5;
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s;
}

.search-btn:hover {
    background: #4338ca;
}

/* ===== 筛选区域：三行简约布局 ===== */
.filter-area {
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    padding: 10px 14px;
    background: #fafaff;
}

.filter-row {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 3px 0;
}

.filter-row + .filter-row {
    border-top: 1px solid #eef1f5;
    margin-top: 3px;
    padding-top: 7px;
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

/* 分类标签（chip 样式） */
.category-chip {
    padding: 2px 9px;
    border-radius: 3px;
    font-size: 12px;
    color: #4a4a6a;
    background: #f0f0ff;
    cursor: pointer;
    transition: all 0.2s;
    line-height: 1.4;
    user-select: none;
}

.category-chip:hover {
    background: #e0e7ff;
    color: #4f46e5;
}

.category-chip.active {
    background: #4f46e5;
    color: #fff;
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

/* 分隔线 */
.content-divider {
    height: 1px;
    background: #e2e8f0;
    margin: 20px 0 24px;
}

/* 骨架屏 */
.loading-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.skeleton-card {
    background: #f7fafc;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
}

.skeleton-image {
    height: 180px;
    background: linear-gradient(90deg, #eef2f7 25%, #e2e8f0 50%, #eef2f7 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
}

.skeleton-body {
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.skeleton-line {
    height: 14px;
    background: linear-gradient(90deg, #eef2f7 25%, #e2e8f0 50%, #eef2f7 75%);
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
    padding: 60px 0 40px;
}

.empty-section svg {
    margin-bottom: 16px;
}

.empty-section p {
    font-size: 16px;
    color: #4a5568;
}

/* 物品网格 */
.items-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.item-card {
    background: #f7fafc;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
    cursor: pointer;
    transition: all 0.3s;
}

.item-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    border-color: transparent;
}

.item-image {
    position: relative;
    height: 180px;
    overflow: hidden;
    background: #eef2f7;
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
    top: 10px;
    left: 10px;
    padding: 4px 12px;
    border-radius: 6px;
    font-size: 12px;
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
    top: 10px;
    right: 10px;
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 11px;
    font-weight: 500;
    background: rgba(0, 0, 0, 0.6);
    color: #fff;
}

.item-info {
    padding: 16px;
}

.item-title {
    font-size: 15px;
    font-weight: 600;
    color: #4a4a6a;
    margin-bottom: 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.item-desc {
    font-size: 13px;
    color: #718096;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin-bottom: 12px;
}

.item-meta {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
    flex-wrap: wrap;
}

.meta-category {
    padding: 2px 8px;
    background: #eef2ff;
    color: #4f46e5;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 500;
}

.meta-location {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #718096;
}

.item-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 10px;
    border-top: 1px solid #e2e8f0;
}

.item-time {
    font-size: 12px;
    color: #a0aec0;
}

.item-author {
    font-size: 12px;
    color: #718096;
}

/* 分页 */
.pagination-section {
    display: flex;
    justify-content: center;
    margin-top: 28px;
    padding-top: 20px;
    border-top: 1px solid #e2e8f0;
}

.pagination-section :deep(.el-pager li) {
    border-radius: 4px;
    margin: 0 2px;
}

.pagination-section :deep(.el-pager li.is-active) {
    background: #4f46e5;
}

.pagination-section :deep(.btn-prev),
.pagination-section :deep(.btn-next) {
    border-radius: 4px;
}
</style>