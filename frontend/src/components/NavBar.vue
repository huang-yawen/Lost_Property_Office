<template>
    <aside class="sidebar" :class="{ collapsed: collapsed }">
        <!-- Logo 区域 -->
        <div class="sidebar-logo">
            <router-link to="/" class="logo-link">
                <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8"/>
                    <path d="M12 18V6"/>
                </svg>
                <span class="logo-text">校园失物招领</span>
            </router-link>
        </div>

        <!-- 导航菜单 -->
        <nav class="sidebar-nav">
            <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                    <polyline points="9 22 9 12 15 12 15 22"/>
                </svg>
                <span class="nav-label">首页</span>
            </router-link>

            <router-link v-if="userStore.isLoggedIn" to="/create" class="nav-item nav-item-create" :class="{ active: $route.path === '/create' || $route.path === '/create/form' }">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="16"/>
                    <line x1="8" y1="12" x2="16" y2="12"/>
                </svg>
                <span class="nav-label">发布信息</span>
            </router-link>

            <router-link v-if="userStore.isLoggedIn" to="/messages" class="nav-item" :class="{ active: $route.path === '/messages' }">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <span class="nav-label">消息中心</span>
                <span v-if="unreadCount > 0" class="nav-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </router-link>

            <router-link v-if="userStore.isLoggedIn" to="/user" class="nav-item" :class="{ active: $route.path === '/user' }">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                </svg>
                <span class="nav-label">个人中心</span>
            </router-link>

            <router-link v-if="userStore.isLoggedIn" to="/my-items" class="nav-item" :class="{ active: $route.path === '/my-items' }">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="8" y1="6" x2="21" y2="6"/>
                    <line x1="8" y1="12" x2="21" y2="12"/>
                    <line x1="8" y1="18" x2="21" y2="18"/>
                    <line x1="3" y1="6" x2="3.01" y2="6"/>
                    <line x1="3" y1="12" x2="3.01" y2="12"/>
                    <line x1="3" y1="18" x2="3.01" y2="18"/>
                </svg>
                <span class="nav-label">我的发布</span>
            </router-link>
        </nav>

        <!-- 底部版本信息 -->
        <div class="sidebar-footer">
            <span class="version-text">v1.0.0</span>
        </div>
    </aside>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '../stores/user'
import { messageApi } from '../api'

defineProps({
    collapsed: Boolean
})

const userStore = useUserStore()
const unreadCount = ref(0)

async function fetchUnreadCount() {
    if (!userStore.isLoggedIn) return
    try {
        const res = await messageApi.getUnreadCount()
        unreadCount.value = res.data || 0
    } catch (error) {
        console.error('获取未读消息数失败:', error)
    }
}

let unreadTimer = null

onMounted(() => {
    fetchUnreadCount()
    unreadTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
    if (unreadTimer) {
        clearInterval(unreadTimer)
    }
})
</script>

<style scoped>
/* ========== 侧边栏 ========== */
.sidebar {
    position: fixed;
    top: 0;
    left: 0;
    width: 240px;
    height: 100vh;
    background: #ffffff;
    z-index: 1000;
    display: flex;
    flex-direction: column;
    box-shadow: 2px 0 12px rgba(0, 0, 0, 0.06);
    overflow: hidden;
    transition: width 0.3s ease;
    border-right: 1px solid #f0f0f0;
}

.sidebar.collapsed {
    width: 64px;
}

.sidebar.collapsed .sidebar-nav {
    padding: 16px 0;
}

.sidebar.collapsed .nav-item {
    padding: 12px 0;
    justify-content: center;
    gap: 0;
    width: 100%;
}

.sidebar.collapsed .nav-item svg {
    margin: 0;
}

/* ========== Logo ========== */
.sidebar-logo {
    padding: 24px 20px 20px;
    border-bottom: 1px solid #f0f0f0;
    white-space: nowrap;
    overflow: hidden;
}

.logo-link {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #1a1a2e;
    font-size: 18px;
    font-weight: 700;
    text-decoration: none;
}

.logo-link svg {
    flex-shrink: 0;
}

.logo-text {
    color: #1a1a2e;
    letter-spacing: 1px;
    transition: opacity 0.2s ease;
}

.sidebar.collapsed .logo-text {
    opacity: 0;
    width: 0;
    overflow: hidden;
}

/* ========== 导航菜单 ========== */
.sidebar-nav {
    flex: 1;
    padding: 16px 12px;
    display: flex;
    flex-direction: column;
    gap: 2px;
    overflow-y: auto;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #4a4a6a;
    transition: all 0.2s;
    text-decoration: none;
    position: relative;
    white-space: nowrap;
}

.nav-item svg {
    flex-shrink: 0;
}

.nav-item:hover {
    background: #f5f5ff;
    color: #4f46e5;
}

.nav-item.active {
    background: #eef2ff;
    color: #4f46e5;
    font-weight: 600;
}

.nav-item-create {
    background: #eef2ff;
    color: #4f46e5 !important;
}

.nav-item-create:hover {
    background: #e0e7ff;
    color: #4338ca !important;
}

.nav-item-create.active {
    background: #e0e7ff;
    color: #4338ca !important;
}

.nav-label {
    flex: 1;
    transition: opacity 0.2s ease;
}

.sidebar.collapsed .nav-label {
    opacity: 0;
    width: 0;
    overflow: hidden;
    flex: 0;
}

.nav-badge {
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    border-radius: 10px;
    background: #e74c3c;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    transition: opacity 0.2s ease;
}

.sidebar.collapsed .nav-badge {
    position: absolute;
    top: 4px;
    right: 4px;
    min-width: 14px;
    height: 14px;
    padding: 0 3px;
    font-size: 9px;
    opacity: 1;
}

/* ========== 底部 ========== */
.sidebar-footer {
    padding: 16px 20px;
    border-top: 1px solid #f0f0f0;
    text-align: center;
    white-space: nowrap;
    overflow: hidden;
}

.version-text {
    font-size: 12px;
    color: #b0b0c0;
    transition: opacity 0.2s ease;
}

.sidebar.collapsed .version-text {
    opacity: 0;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
    .sidebar {
        width: 64px;
    }

    .logo-text,
    .nav-label,
    .version-text {
        display: none;
    }

    .sidebar-logo {
        padding: 16px 12px;
        display: flex;
        justify-content: center;
    }

    .sidebar-nav {
        padding: 12px 8px;
    }

    .nav-item {
        padding: 12px 16px;
        width: auto;
        height: auto;
        justify-content: flex-start;
    }

    .sidebar-footer {
        padding: 12px 8px;
    }
}
</style>