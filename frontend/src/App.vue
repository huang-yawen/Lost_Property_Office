<template>
    <!-- 全屏页面（登录/注册等），不包含侧边栏和顶栏 -->
    <template v-if="route.meta.hideLayout">
        <router-view />
    </template>

    <!-- 标准布局页面 -->
    <template v-else>
        <div class="app-layout">
            <NavBar :collapsed="sidebarCollapsed" />
            <div class="app-right" :class="{ collapsed: sidebarCollapsed }">
                <TopBar :collapsed="sidebarCollapsed" @toggle-sidebar="sidebarCollapsed = !sidebarCollapsed" />
                <main class="app-main" :class="{ 'app-main-chat': route.name === 'Messages' }">
                    <router-view />
                </main>
            </div>
        </div>
    </template>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'
import TopBar from './components/TopBar.vue'

const route = useRoute()
const sidebarCollapsed = ref(false)
</script>

<style>
/* 全局样式重置 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

html {
    overflow: hidden;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans SC', sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: #4a4a6a;
    background: #f5f5ff;
    overflow: hidden;
}

a {
    text-decoration: none;
    color: inherit;
}

/* 滚动条样式 */
::-webkit-scrollbar {
    width: 6px;
}

::-webkit-scrollbar-track {
    background: transparent;
}

::-webkit-scrollbar-thumb {
    background: #c0c4cc;
    border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
    background: #a0aec0;
}

/* Element Plus 全局覆盖 */
.el-message {
    border-radius: 10px !important;
}

.el-message-box {
    border-radius: 14px !important;
}

.el-message-box__header {
    padding: 20px 24px 0 !important;
}

.el-message-box__title {
    font-size: 16px !important;
    font-weight: 600 !important;
    color: #1a3a5c !important;
}

.el-message-box__content {
    padding: 20px 24px !important;
    color: #4a5568 !important;
    font-size: 14px !important;
}

.el-message-box__btns {
    padding: 0 24px 20px !important;
}

.el-message-box__btns button {
    border-radius: 8px !important;
}

.el-button--primary {
    background: #4f46e5 !important;
    border-color: #4f46e5 !important;
}

.el-button--primary:hover {
    background: #4338ca !important;
    border-color: #4338ca !important;
}

.el-dialog {
    border-radius: 16px !important;
}

.el-pagination button,
.el-pager li {
    border-radius: 8px !important;
}

.el-pager li.is-active {
    background: #4f46e5 !important;
}
</style>

<style scoped>
.app-layout {
    display: flex;
    height: 100vh;
    overflow: hidden;
}

.app-right {
    flex: 1;
    margin-left: 240px;
    display: flex;
    flex-direction: column;
    height: 100vh;
    overflow: hidden;
    transition: margin-left 0.3s ease;
}

.app-right.collapsed {
    margin-left: 64px;
}

.app-main {
    flex: 1;
    margin: 16px;
    margin-top: 72px;
    background: #ffffff;
    border-radius: 6px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.02);
    padding: 24px;
    overflow-y: auto;
    min-height: 0;
}

.app-main-chat {
    padding: 0;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    min-height: 0;
}
</style>