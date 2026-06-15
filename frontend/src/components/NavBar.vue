<template>
    <header class="site-header">
        <!-- 顶部导航栏 -->
        <div class="header-top">
            <div class="header-container">
                <router-link to="/" class="header-logo">
                    <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"/>
                        <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8"/>
                        <path d="M12 18V6"/>
                    </svg>
                    <span class="logo-text">校园失物招领</span>
                </router-link>

                <nav class="header-nav">
                    <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                            <polyline points="9 22 9 12 15 12 15 22"/>
                        </svg>
                        <span>首页</span>
                    </router-link>

                    <router-link v-if="userStore.isLoggedIn" to="/create" class="nav-item nav-item-create">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="12" cy="12" r="10"/>
                            <line x1="12" y1="8" x2="12" y2="16"/>
                            <line x1="8" y1="12" x2="16" y2="12"/>
                        </svg>
                        <span>发布</span>
                    </router-link>

                    <router-link v-if="userStore.isLoggedIn" to="/messages" class="nav-item" :class="{ active: $route.path === '/messages' }">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                        </svg>
                        <span>消息中心</span>
                        <span v-if="unreadCount > 0" class="nav-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
                    </router-link>

                    <router-link v-if="userStore.isLoggedIn" to="/user" class="nav-item" :class="{ active: $route.path === '/user' }">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                            <circle cx="12" cy="7" r="4"/>
                        </svg>
                        <span>个人中心</span>
                    </router-link>
                </nav>

                <div class="header-actions">
                    <template v-if="userStore.isLoggedIn">
                        <div class="user-menu" ref="menuRef">
                            <div class="user-trigger" @click="showMenu = !showMenu">
                                <div class="user-avatar">
                                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                        <circle cx="12" cy="7" r="4"/>
                                    </svg>
                                </div>
                                <span class="user-name">{{ userStore.userInfo?.username }}</span>
                                <svg class="menu-arrow" :class="{ open: showMenu }" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <polyline points="6 9 12 15 18 9"/>
                                </svg>
                            </div>

                            <transition name="menu-fade">
                                <div v-if="showMenu" class="dropdown-menu">
                                    <router-link to="/my-items" class="dropdown-item" @click="showMenu = false">
                                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <line x1="8" y1="6" x2="21" y2="6"/>
                                            <line x1="8" y1="12" x2="21" y2="12"/>
                                            <line x1="8" y1="18" x2="21" y2="18"/>
                                            <line x1="3" y1="6" x2="3.01" y2="6"/>
                                            <line x1="3" y1="12" x2="3.01" y2="12"/>
                                            <line x1="3" y1="18" x2="3.01" y2="18"/>
                                        </svg>
                                        我的发布
                                    </router-link>
                                    <div class="dropdown-divider"></div>
                                    <div class="dropdown-item dropdown-logout" @click="handleLogout">
                                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                                            <polyline points="16 17 21 12 16 7"/>
                                            <line x1="21" y1="12" x2="9" y2="12"/>
                                        </svg>
                                        退出登录
                                    </div>
                                </div>
                            </transition>
                        </div>
                    </template>

                    <template v-else>
                        <router-link to="/login" class="btn-login">登录</router-link>
                        <router-link to="/register" class="btn-register">注册</router-link>
                    </template>
                </div>
            </div>
        </div>

        <!-- Banner 区域 - 仅在首页显示 -->
        <div v-if="$route.path === '/'" class="header-banner">
            <div class="banner-overlay"></div>
            <div class="banner-content">
                <h1 class="banner-title">校园失物招领平台</h1>
                <p class="banner-desc">让每一件遗失的物品都能找到回家的路</p>
                <div class="banner-stats">
                    <div class="stat-item">
                        <span class="stat-number">1,200+</span>
                        <span class="stat-label">已发布信息</span>
                    </div>
                    <div class="stat-divider"></div>
                    <div class="stat-item">
                        <span class="stat-number">800+</span>
                        <span class="stat-label">成功找回</span>
                    </div>
                    <div class="stat-divider"></div>
                    <div class="stat-item">
                        <span class="stat-number">98%</span>
                        <span class="stat-label">找回率</span>
                    </div>
                </div>
            </div>
        </div>
    </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { messageApi } from '../api'

const router = useRouter()
const userStore = useUserStore()

const showMenu = ref(false)
const menuRef = ref(null)
const unreadCount = ref(0)

function handleLogout() {
    showMenu.value = false
    userStore.logout()
    router.push('/')
}

function handleClickOutside(e) {
    if (menuRef.value && !menuRef.value.contains(e.target)) {
        showMenu.value = false
    }
}

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
    document.addEventListener('click', handleClickOutside)
    fetchUnreadCount()
    // 每30秒刷新一次未读消息数
    unreadTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
    if (unreadTimer) {
        clearInterval(unreadTimer)
    }
})
</script>

<style scoped>
/* ========== 顶部导航栏 ========== */
.header-top {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 64px;
    background: linear-gradient(135deg, #1a3a5c 0%, #2c5f8a 50%, #4a7fb5 100%);
    z-index: 1000;
    transition: box-shadow 0.3s;
}

.header-top:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.header-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

/* Logo */
.header-logo {
    display: flex;
    align-items: center;
    gap: 10px;
    color: #fff;
    font-size: 20px;
    font-weight: 700;
    text-decoration: none;
    flex-shrink: 0;
}

.logo-text {
    color: #fff;
}

/* 导航链接 */
.header-nav {
    display: flex;
    align-items: center;
    gap: 2px;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    color: rgba(255, 255, 255, 0.85);
    transition: all 0.2s;
    text-decoration: none;
    position: relative;
}

.nav-item:hover {
    background: rgba(255, 255, 255, 0.15);
    color: #fff;
}

.nav-item.active {
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
    font-weight: 600;
}

.nav-item-create {
    background: rgba(255, 255, 255, 0.2);
    color: #fff !important;
}

.nav-item-create:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.nav-badge {
    position: absolute;
    top: 2px;
    right: 2px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    border-radius: 8px;
    background: #e74c3c;
    color: #fff;
    font-size: 10px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.95);
}

/* 右侧操作区 */
.header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-shrink: 0;
}

/* 用户菜单 */
.user-menu {
    position: relative;
}

.user-trigger {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
}

.user-trigger:hover {
    background: rgba(255, 255, 255, 0.15);
}

.user-avatar {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.25);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
}

.user-name {
    font-size: 14px;
    font-weight: 500;
    color: #fff;
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.menu-arrow {
    color: rgba(255, 255, 255, 0.7);
    transition: transform 0.2s;
}

.menu-arrow.open {
    transform: rotate(180deg);
}

.dropdown-menu {
    position: absolute;
    top: calc(100% + 8px);
    right: 0;
    min-width: 160px;
    background: #fff;
    border-radius: 12px;
    border: 1px solid #e2e8f0;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.dropdown-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    font-size: 14px;
    color: #2c3e50;
    cursor: pointer;
    transition: all 0.2s;
    text-decoration: none;
}

.dropdown-item:hover {
    background: #f7fafc;
    color: #1a3a5c;
}

.dropdown-divider {
    height: 1px;
    background: #e2e8f0;
    margin: 4px 0;
}

.dropdown-logout {
    color: #c0392b;
}

.dropdown-logout:hover {
    background: #fdf2f2;
    color: #c0392b;
}

/* 菜单动画 */
.menu-fade-enter-active,
.menu-fade-leave-active {
    transition: all 0.2s ease;
}

.menu-fade-enter-from,
.menu-fade-leave-to {
    opacity: 0;
    transform: translateY(-8px);
}

/* 登录注册按钮 */
.btn-login {
    padding: 8px 20px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #fff;
    border: 1px solid rgba(255, 255, 255, 0.6);
    transition: all 0.25s;
    text-decoration: none;
}

.btn-login:hover {
    background: rgba(255, 255, 255, 0.15);
    border-color: #fff;
}

.btn-register {
    padding: 8px 20px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    color: #1a3a5c;
    background: #fff;
    transition: all 0.25s;
    text-decoration: none;
}

.btn-register:hover {
    background: rgba(255, 255, 255, 0.9);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* ========== Banner 区域 ========== */
.header-banner {
    position: relative;
    margin-top: 64px;
    height: 320px;
    background: linear-gradient(135deg, #1a3a5c 0%, #2c5f8a 40%, #4a7fb5 70%, #6a9fd5 100%);
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
}

/* 装饰性背景元素 */
.header-banner::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 600px;
    height: 600px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 70%);
    pointer-events: none;
}

.header-banner::after {
    content: '';
    position: absolute;
    bottom: -30%;
    left: -10%;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, transparent 70%);
    pointer-events: none;
}

.banner-overlay {
    position: absolute;
    inset: 0;
    background: 
        radial-gradient(ellipse at 20% 50%, rgba(255,255,255,0.1) 0%, transparent 50%),
        radial-gradient(ellipse at 80% 20%, rgba(255,255,255,0.06) 0%, transparent 50%),
        radial-gradient(ellipse at 50% 80%, rgba(255,255,255,0.04) 0%, transparent 50%);
    pointer-events: none;
}

.banner-content {
    position: relative;
    z-index: 1;
    text-align: center;
    color: #fff;
    padding: 0 24px;
}

.banner-title {
    font-size: 36px;
    font-weight: 800;
    margin-bottom: 12px;
    letter-spacing: 2px;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.banner-desc {
    font-size: 16px;
    opacity: 0.9;
    margin-bottom: 32px;
    letter-spacing: 1px;
}

.banner-stats {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 32px;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}

.stat-number {
    font-size: 28px;
    font-weight: 800;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.stat-label {
    font-size: 13px;
    opacity: 0.8;
    font-weight: 500;
}

.stat-divider {
    width: 1px;
    height: 40px;
    background: rgba(255, 255, 255, 0.3);
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
    .header-nav {
        gap: 0;
    }

    .nav-item span {
        display: none;
    }

    .nav-item {
        padding: 8px 10px;
    }

    .nav-item-create {
        padding: 8px 12px;
    }

    .nav-item-create span {
        display: inline;
        font-size: 12px;
    }

    .user-name {
        display: none;
    }

    .header-banner {
        height: 240px;
    }

    .banner-title {
        font-size: 26px;
    }

    .banner-desc {
        font-size: 14px;
    }

    .banner-stats {
        gap: 20px;
    }

    .stat-number {
        font-size: 22px;
    }

    .stat-label {
        font-size: 12px;
    }

    .stat-divider {
        height: 30px;
    }
}

@media (max-width: 480px) {
    .header-container {
        padding: 0 12px;
    }

    .logo-text {
        display: none;
    }

    .header-banner {
        height: 200px;
    }

    .banner-title {
        font-size: 22px;
    }

    .banner-stats {
        gap: 16px;
    }

    .stat-number {
        font-size: 18px;
    }
}
</style>