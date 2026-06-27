<template>
    <header class="topbar" :class="{ collapsed: collapsed }">
        <div class="topbar-left">
            <button class="hamburger-btn" @click="$emit('toggle-sidebar')" title="切换侧边栏">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="3" y1="6" x2="21" y2="6"/>
                    <line x1="3" y1="12" x2="21" y2="12"/>
                    <line x1="3" y1="18" x2="21" y2="18"/>
                </svg>
            </button>
        </div>
        <div class="topbar-right">
            <template v-if="isLoggedIn">
                <router-link to="/messages" class="topbar-icon-btn" title="消息中心">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                    </svg>
                    <span v-if="unreadCount > 0" class="icon-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
                </router-link>

                <div class="topbar-user" ref="userMenuRef">
                    <div class="user-trigger" @click="showUserMenu = !showUserMenu">
                        <div class="user-avatar-sm">
                            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                <circle cx="12" cy="7" r="4"/>
                            </svg>
                        </div>
                        <span class="user-name-sm">{{ displayName }}</span>
                        <svg class="arrow-down" :class="{ open: showUserMenu }" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="6 9 12 15 18 9"/>
                        </svg>
                    </div>

                    <transition name="drop">
                        <div v-if="showUserMenu" class="user-dropdown">
                            <router-link v-if="!isAdmin" to="/user" class="drop-item" @click="showUserMenu = false">
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                    <circle cx="12" cy="7" r="4"/>
                                </svg>
                                个人中心
                            </router-link>
                            <router-link v-if="!isAdmin" to="/my-items" class="drop-item" @click="showUserMenu = false">
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
                            <router-link to="/dashboard" class="drop-item" @click="showUserMenu = false">
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <rect x="3" y="3" width="7" height="7"/>
                                    <rect x="14" y="3" width="7" height="7"/>
                                    <rect x="14" y="14" width="7" height="7"/>
                                    <rect x="3" y="14" width="7" height="7"/>
                                </svg>
                                数据看板
                            </router-link>
                            <template v-if="isAdmin">
                                <div class="drop-divider"></div>
                                <div class="drop-item" style="color: #a5b4fc; font-weight: 600;">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                                    </svg>
                                    管理员
                                </div>
                                <router-link to="/admin/certification" class="drop-item" @click="showUserMenu = false">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M9 12l2 2 4-4"/>
                                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                                    </svg>
                                    认证审核
                                </router-link>
                                <router-link to="/admin/users" class="drop-item" @click="showUserMenu = false">
                                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                                        <circle cx="9" cy="7" r="4"/>
                                    </svg>
                                    用户列表
                                </router-link>
                            </template>
                            <div class="drop-divider"></div>
                            <div class="drop-item" @click="toggleDarkMode">
                                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
                                </svg>
                                {{ themeStore.isDark ? '浅色模式' : '深色模式' }}
                            </div>
                            <div class="drop-divider"></div>
                            <div class="drop-item drop-logout" @click="handleLogout">
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
                <router-link to="/login" class="topbar-btn topbar-btn-login">登录</router-link>
                <router-link to="/register" class="topbar-btn topbar-btn-register">注册</router-link>
            </template>
        </div>
    </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useThemeStore } from '../stores/theme'
import { messageApi } from '../api'

defineProps({
    collapsed: Boolean
})

defineEmits(['toggle-sidebar'])

const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

const showUserMenu = ref(false)
const userMenuRef = ref(null)
const unreadCount = ref(0)

// 是否已登录（普通用户或管理员）
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 管理员身份
const isAdmin = computed(() => userStore.isAdmin)

// 当前显示的显示名
const displayName = computed(() => {
    // 优先显示管理员信息
    if (userStore.isAdmin && userStore.adminInfo) {
        return userStore.adminInfo.nickname || userStore.adminInfo.username || '管理员'
    }
    // 普通用户
    if (userStore.userInfo) {
        return userStore.userInfo.nickname || userStore.userInfo.username || '用户'
    }
    return '用户'
})

function toggleDarkMode() {
    showUserMenu.value = false
    themeStore.toggle()
}

function handleLogout() {
    showUserMenu.value = false
    userStore.logout()
    router.push('/')
}

function handleClickOutside(e) {
    if (userMenuRef.value && !userMenuRef.value.contains(e.target)) {
        showUserMenu.value = false
    }
}

async function fetchUnreadCount() {
    if (!userStore.isLoggedIn || userStore.isAdmin) return
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
/* ========== 暗黑模式 ========== */
html.dark .topbar {
    background: #12121e;
    border-bottom-color: #1e1e32;
}

html.dark .hamburger-btn {
    color: #8a8aa0;
}

html.dark .hamburger-btn:hover {
    background: #1a1a2e;
    color: #818cf8;
}

html.dark .topbar-icon-btn {
    color: #8a8aa0;
}

html.dark .topbar-icon-btn:hover {
    background: #1a1a2e;
    color: #818cf8;
}

html.dark .user-trigger:hover {
    background: #1a1a2e;
}

html.dark .user-name-sm {
    color: #c8c8d8;
}

html.dark .arrow-down {
    color: #6a6a8a;
}

html.dark .user-dropdown {
    background: #1a1a2e;
    border-color: #2a2a3e;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

html.dark .drop-item {
    color: #c8c8d8;
}

html.dark .drop-item:hover {
    background: #22223a;
    color: #a5b4fc;
}

html.dark .drop-divider {
    background: #2a2a3e;
}

html.dark .drop-logout {
    color: #f87171;
}

html.dark .drop-logout:hover {
    background: rgba(220, 38, 38, 0.1);
    color: #f87171;
}

html.dark .topbar-btn-login {
    color: #8a8aa0;
    border-color: #2a2a3e;
}

html.dark .topbar-btn-login:hover {
    border-color: #6366f1;
    color: #a5b4fc;
    background: rgba(99, 102, 241, 0.1);
}

html.dark .topbar-btn-register {
    background: #6366f1;
    border-color: #6366f1;
}

html.dark .topbar-btn-register:hover {
    background: #4f46e5;
    border-color: #4f46e5;
}

/* ========== 顶部栏 ========== */
.topbar {
    position: fixed;
    top: 0;
    left: 240px;
    right: 0;
    height: 56px;
    background: #ffffff;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 28px;
    z-index: 999;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
    transition: left 0.3s ease;
}

.topbar.collapsed {
    left: 64px;
}

.topbar-left {
    display: flex;
    align-items: center;
}

/* 汉堡按钮 */
.hamburger-btn {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    border: none;
    background: transparent;
    color: #4a5568;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
}

.hamburger-btn:hover {
    background: #f5f5ff;
    color: #4f46e5;
}

.topbar-right {
    display: flex;
    align-items: center;
    gap: 12px;
}

/* 消息按钮 */
.topbar-icon-btn {
    position: relative;
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #4a5568;
    transition: all 0.2s;
    text-decoration: none;
}

.topbar-icon-btn:hover {
    background: #f5f5ff;
    color: #4f46e5;
}

.icon-badge {
    position: absolute;
    top: 4px;
    right: 4px;
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
}

/* 用户区域 */
.topbar-user {
    position: relative;
}

.user-trigger {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px 6px 6px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
}

.user-trigger:hover {
    background: #f5f5ff;
}

.user-avatar-sm {
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: #4f46e5;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.user-name-sm {
    font-size: 14px;
    font-weight: 500;
    color: #4a4a6a;
    max-width: 100px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.arrow-down {
    color: #a0aec0;
    transition: transform 0.2s;
}

.arrow-down.open {
    transform: rotate(180deg);
}

/* 下拉菜单 */
.user-dropdown {
    position: absolute;
    top: calc(100% + 6px);
    right: 0;
    min-width: 160px;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    overflow: hidden;
}

.drop-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 16px;
    font-size: 14px;
    color: #4a4a6a;
    cursor: pointer;
    transition: all 0.15s;
    text-decoration: none;
}

.drop-item:hover {
    background: #f5f5ff;
    color: #4f46e5;
}

.drop-divider {
    height: 1px;
    background: #e2e8f0;
    margin: 4px 0;
}

.drop-logout {
    color: #c0392b;
}

.drop-logout:hover {
    background: #fdf2f2;
    color: #c0392b;
}

/* 下拉动画 */
.drop-enter-active,
.drop-leave-active {
    transition: all 0.2s ease;
}

.drop-enter-from,
.drop-leave-to {
    opacity: 0;
    transform: translateY(-6px);
}

/* 登录注册按钮 */
.topbar-btn {
    padding: 7px 18px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.25s;
    text-decoration: none;
}

.topbar-btn-login {
    color: #4a4a6a;
    border: 1px solid #e0e0f0;
}

.topbar-btn-login:hover {
    border-color: #4f46e5;
    color: #4f46e5;
    background: #f5f5ff;
}

.topbar-btn-register {
    color: #fff;
    background: #4f46e5;
    border: 1px solid #4f46e5;
}

.topbar-btn-register:hover {
    background: #4338ca;
    border-color: #4338ca;
}
</style>