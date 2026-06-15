<template>
    <div class="my-items-page">
        <div class="my-items-container">
            <div class="page-header">
                <h1 class="page-title">我的发布</h1>
                <p class="page-desc">管理您发布的招领和寻物信息</p>
            </div>

            <!-- 筛选标签 -->
            <div class="filter-tabs">
                <button
                    class="filter-tab"
                    :class="{ active: currentFilter === 'all' }"
                    @click="currentFilter = 'all'"
                >
                    全部
                </button>
                <button
                    class="filter-tab"
                    :class="{ active: currentFilter === 'found' }"
                    @click="currentFilter = 'found'"
                >
                    招领
                </button>
                <button
                    class="filter-tab"
                    :class="{ active: currentFilter === 'lost' }"
                    @click="currentFilter = 'lost'"
                >
                    寻物
                </button>
                <button
                    class="filter-tab"
                    :class="{ active: currentFilter === 'resolved' }"
                    @click="currentFilter = 'resolved'"
                >
                    已解决
                </button>
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
                <svg viewBox="0 0 24 24" width="64" height="64" fill="none" stroke="#c0c4cc" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="3" width="7" height="7"/>
                    <rect x="14" y="3" width="7" height="7"/>
                    <rect x="14" y="14" width="7" height="7"/>
                    <rect x="3" y="14" width="7" height="7"/>
                </svg>
                <p>暂无发布信息</p>
                <router-link to="/create" class="btn-create">去发布</router-link>
            </div>

            <!-- 物品列表 -->
            <div v-else class="items-list">
                <div
                    v-for="item in filteredItems"
                    :key="item.id"
                    class="item-card"
                >
                    <div class="item-image" @click="router.push(`/item/${item.id}`)">
                        <img :src="getFirstImage(item.images)" :alt="item.title" @error="handleImageError" />
                        <span class="item-type" :class="item.type === 1 ? 'type-found' : 'type-lost'">
                            {{ item.type === 1 ? '招领' : '寻物' }}
                        </span>
                    </div>
                    <div class="item-info">
                        <div class="item-header">
                            <h3 class="item-title" @click="router.push(`/item/${item.id}`)">{{ item.title }}</h3>
                            <span class="item-status" :class="item.status === 2 ? 'status-resolved' : 'status-active'">
                                {{ item.status === 2 ? '已解决' : '进行中' }}
                            </span>
                        </div>
                        <p class="item-desc">{{ item.description || '暂无描述' }}</p>
                        <div class="item-meta">
                            <span class="meta-time">{{ formatTime(item.createTime) }}</span>
                            <span class="meta-category">{{ getCategoryName(item.categoryId) }}</span>
                        </div>
                        <div class="item-actions">
                            <button class="action-btn action-edit" @click="router.push(`/edit/${item.id}`)">
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
                            <button class="action-btn action-delete" @click="handleDelete(item)">
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

const filteredItems = computed(() => {
    let list = items.value
    if (currentFilter.value === 'found') {
        list = list.filter(i => i.type === 1)
    } else if (currentFilter.value === 'lost') {
        list = list.filter(i => i.type === 2)
    } else if (currentFilter.value === 'resolved') {
        list = list.filter(i => i.status === 2)
    }
    return list
})

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
        const res = await itemApi.getMyItems()
        items.value = res.data || []
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
.my-items-page {
    min-height: 100vh;
    background: #f0f2f5;
}

.my-items-container {
    max-width: 800px;
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

/* 筛选标签 */
.filter-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 24px;
    flex-wrap: wrap;
}

.filter-tab {
    padding: 8px 20px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #fff;
    font-size: 13px;
    font-weight: 500;
    color: #4a5568;
    cursor: pointer;
    transition: all 0.25s;
}

.filter-tab:hover {
    border-color: #7aa3d0;
    color: #1a3a5c;
}

.filter-tab.active {
    background: #1a3a5c;
    border-color: #1a3a5c;
    color: #fff;
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

.skeleton-img {
    width: 100px;
    height: 100px;
    border-radius: 10px;
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

.skeleton-line.w-70 { width: 70%; }
.skeleton-line.w-50 { width: 50%; }
.skeleton-line.w-30 { width: 30%; }

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
    margin-bottom: 20px;
}

.btn-create {
    display: inline-block;
    padding: 10px 24px;
    background: linear-gradient(135deg, #1a3a5c, #2c5f8a);
    color: #fff;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    text-decoration: none;
    transition: all 0.25s;
}

.btn-create:hover {
    background: linear-gradient(135deg, #2c5f8a, #4a7fb5);
    box-shadow: 0 4px 12px rgba(26, 58, 92, 0.3);
}

/* 物品列表 */
.items-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.item-card {
    display: flex;
    background: #fff;
    border-radius: 16px;
    border: 1px solid #e2e8f0;
    overflow: hidden;
    transition: box-shadow 0.25s;
}

.item-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.item-image {
    position: relative;
    width: 180px;
    min-height: 160px;
    flex-shrink: 0;
    cursor: pointer;
    background: #f0f2f5;
}

.item-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.item-type {
    position: absolute;
    top: 8px;
    left: 8px;
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;
}

.type-found {
    background: linear-gradient(135deg, #1a8a5c, #2ea86e);
}

.type-lost {
    background: linear-gradient(135deg, #c0392b, #e74c3c);
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
    margin-bottom: 8px;
}

.item-title {
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
    cursor: pointer;
    transition: color 0.2s;
    margin: 0;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.item-title:hover {
    color: #1a3a5c;
}

.item-status {
    padding: 3px 10px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
    white-space: nowrap;
    flex-shrink: 0;
}

.status-active {
    background: #fff3cd;
    color: #856404;
}

.status-resolved {
    background: #d4edda;
    color: #155724;
}

.item-desc {
    font-size: 13px;
    color: #718096;
    margin-bottom: 12px;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.item-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
}

.meta-time {
    font-size: 12px;
    color: #a0aec0;
}

.meta-category {
    padding: 2px 8px;
    background: #d4e4f3;
    color: #1a3a5c;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 500;
}

.item-actions {
    display: flex;
    gap: 8px;
    margin-top: auto;
}

.action-btn {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 14px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 500;
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

/* 响应式 */
@media (max-width: 640px) {
    .item-card {
        flex-direction: column;
    }

    .item-image {
        width: 100%;
        height: 180px;
    }

    .item-info {
        padding: 16px;
    }

    .item-actions {
        flex-wrap: wrap;
    }
}
</style>