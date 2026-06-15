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

            <!-- 分类标签 -->
            <div class="categories-section">
                <div class="categories-scroll">
                    <div
                        class="category-tab"
                        :class="{ active: activeCategory === 0 }"
                        @click="switchCategory(0)"
                    >
                        <div class="category-icon category-all">
                            <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="3" width="7" height="7"/>
                                <rect x="14" y="3" width="7" height="7"/>
                                <rect x="14" y="14" width="7" height="7"/>
                                <rect x="3" y="14" width="7" height="7"/>
                            </svg>
                        </div>
                        <span>全部</span>
                    </div>
                    <div
                        v-for="cat in categories"
                        :key="cat.id"
                        class="category-tab"
                        :class="{ active: activeCategory === cat.id }"
                        @click="switchCategory(cat.id)"
                    >
                        <div class="category-icon" :style="{ background: cat.color || '#d4e4f3' }">
                            <span class="category-emoji">{{ cat.icon || '📦' }}</span>
                        </div>
                        <span>{{ cat.name }}</span>
                    </div>
                </div>
            </div>

            <!-- 类型筛选 -->
            <div class="type-filter">
                <div
                    class="type-btn"
                    :class="{ active: activeType === 0 }"
                    @click="switchType(0)"
                >
                    全部
                </div>
                <div
                    class="type-btn type-found"
                    :class="{ active: activeType === 1 }"
                    @click="switchType(1)"
                >
                    招领
                </div>
                <div
                    class="type-btn type-lost"
                    :class="{ active: activeType === 2 }"
                    @click="switchType(2)"
                >
                    寻物
                </div>
            </div>

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
            size: pageSize
        }
        if (activeCategory.value > 0) params.categoryId = activeCategory.value
        if (activeType.value > 0) params.type = activeType.value
        if (searchKeyword.value.trim()) params.keyword = searchKeyword.value.trim()

        const res = await itemApi.getList(params)
        items.value = res.data?.records || []
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
.home-page {
    min-height: 100vh;
    background: #f0f2f5;
}

/* 主内容 */
.main-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px 24px 60px;
}

/* Banner 区域 - 由 NavBar 组件提供 */

/* 搜索栏 */
.search-section {
    margin-bottom: 20px;
}

.search-box {
    display: flex;
    align-items: center;
    background: #fff;
    border-radius: 14px;
    padding: 4px 4px 4px 16px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    border: 1px solid #e2e8f0;
}

.search-icon {
    color: #a0aec0;
    flex-shrink: 0;
}

.search-input {
    flex: 1;
    border: none;
    outline: none;
    padding: 12px 12px;
    font-size: 15px;
    color: #2c3e50;
    background: transparent;
}

.search-input::placeholder {
    color: #a0aec0;
}

.search-clear {
    background: none;
    border: none;
    padding: 8px;
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
    padding: 10px 24px;
    background: #1a3a5c;
    color: #fff;
    border: none;
    border-radius: 10px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s;
}

.search-btn:hover {
    background: #2c5f8a;
}

/* 分类标签 */
.categories-section {
    margin-bottom: 20px;
}

.categories-scroll {
    display: flex;
    gap: 12px;
    overflow-x: auto;
    padding-bottom: 8px;
    -webkit-overflow-scrolling: touch;
}

.categories-scroll::-webkit-scrollbar {
    height: 4px;
}

.categories-scroll::-webkit-scrollbar-thumb {
    background: #c0c4cc;
    border-radius: 2px;
}

.category-tab {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 14px;
    border: 1px solid #e2e8f0;
    cursor: pointer;
    transition: all 0.25s;
    min-width: 80px;
    flex-shrink: 0;
}

.category-tab:hover {
    border-color: #7aa3d0;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.category-tab.active {
    border-color: #1a3a5c;
    background: #f0f5fa;
}

.category-tab span {
    font-size: 12px;
    font-weight: 500;
    color: #4a5568;
    white-space: nowrap;
}

.category-tab.active span {
    color: #1a3a5c;
    font-weight: 600;
}

.category-icon {
    width: 44px;
    height: 44px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
}

.category-all {
    background: linear-gradient(135deg, #d4e4f3, #a8c4e3);
    color: #1a3a5c;
}

.category-emoji {
    font-size: 20px;
}

/* 类型筛选 */
.type-filter {
    display: flex;
    gap: 8px;
    margin-bottom: 20px;
}

.type-btn {
    padding: 8px 20px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    color: #4a5568;
    background: #fff;
    border: 1px solid #e2e8f0;
    cursor: pointer;
    transition: all 0.25s;
}

.type-btn:hover {
    border-color: #7aa3d0;
    color: #1a3a5c;
}

.type-btn.active {
    background: #1a3a5c;
    color: #fff;
    border-color: #1a3a5c;
}

.type-found.active {
    background: #1a8a5c;
    border-color: #1a8a5c;
}

.type-lost.active {
    background: #c0392b;
    border-color: #c0392b;
}

/* 骨架屏 */
.loading-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.skeleton-card {
    background: #fff;
    border-radius: 14px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
}

.skeleton-image {
    height: 180px;
    background: linear-gradient(90deg, #f0f2f5 25%, #e2e8f0 50%, #f0f2f5 75%);
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

/* 物品网格 */
.items-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
}

.item-card {
    background: #fff;
    border-radius: 14px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
    cursor: pointer;
    transition: all 0.3s;
}

.item-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    border-color: transparent;
}

.item-image {
    position: relative;
    height: 180px;
    overflow: hidden;
    background: #f0f2f5;
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
    color: #2c3e50;
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
    background: #d4e4f3;
    color: #1a3a5c;
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
    border-top: 1px solid #f0f2f5;
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
    margin-top: 32px;
}

.pagination-section :deep(.el-pager li) {
    border-radius: 8px;
    margin: 0 2px;
}

.pagination-section :deep(.el-pager li.is-active) {
    background: #1a3a5c;
}

.pagination-section :deep(.btn-prev),
.pagination-section :deep(.btn-next) {
    border-radius: 8px;
}
</style>