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
    border-radius: 8px !important;
}

.el-message-box {
    border-radius: 8px !important;
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
    border-radius: 8px !important;
}

.el-pagination button,
.el-pager li {
    border-radius: 8px !important;
}

.el-pager li.is-active {
    background: #4f46e5 !important;
}

/* ========== 暗黑模式 ========== */
html.dark body {
    background: #0f0f1a;
    color: #c8c8d8;
}

html.dark .sidebar {
    background: #1a1a2e;
    border-right-color: #2a2a3e;
}

html.dark .sidebar-logo,
html.dark .sidebar-footer {
    border-color: #2a2a3e;
}

html.dark .logo-link,
html.dark .logo-text {
    color: #e0e0f0;
}

html.dark .nav-item {
    color: #8a8aa0;
}

html.dark .nav-item:hover {
    background: #2a2a3e;
    color: #a5b4fc;
}

html.dark .nav-item.active {
    background: #2e2e4a;
    color: #a5b4fc;
}

html.dark .nav-item-create {
    background: #2e2e4a;
    color: #a5b4fc !important;
}

html.dark .nav-item-create:hover {
    background: #3a3a5a;
    color: #c7d2fe !important;
}

html.dark .topbar {
    background: #1a1a2e;
    border-bottom-color: #2a2a3e;
}

html.dark .hamburger-btn {
    color: #8a8aa0;
}

html.dark .hamburger-btn:hover {
    background: #2a2a3e;
    color: #a5b4fc;
}

html.dark .topbar-icon-btn {
    color: #8a8aa0;
}

html.dark .topbar-icon-btn:hover {
    background: #2a2a3e;
    color: #a5b4fc;
}

html.dark .user-trigger:hover {
    background: #2a2a3e;
}

html.dark .user-name-sm {
    color: #c8c8d8;
}

html.dark .user-dropdown {
    background: #1a1a2e;
    border-color: #2a2a3e;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

html.dark .drop-item {
    color: #c8c8d8;
}

html.dark .drop-item:hover {
    background: #2a2a3e;
    color: #a5b4fc;
}

html.dark .drop-divider {
    background: #2a2a3e;
}

html.dark .drop-logout {
    color: #f87171;
}

html.dark .drop-logout:hover {
    background: #2a1a1a;
    color: #f87171;
}

html.dark .topbar-btn-login {
    color: #c8c8d8;
    border-color: #3a3a5a;
}

html.dark .topbar-btn-login:hover {
    border-color: #6366f1;
    color: #a5b4fc;
    background: #2a2a3e;
}

html.dark .app-main {
    background: #1a1a2e;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

html.dark .stat-card,
html.dark .chart-card {
    background: #22223a;
    border-color: #2a2a3e;
}

html.dark .dashboard-title,
html.dark .chart-title {
    color: #e0e0f0;
}

html.dark .dashboard-subtitle,
html.dark .stat-label {
    color: #8a8aa0;
}

html.dark .stat-value {
    color: #e0e0f0;
}

html.dark ::-webkit-scrollbar-thumb {
    background: #3a3a5a;
}

html.dark ::-webkit-scrollbar-thumb:hover {
    background: #4a4a6a;
}

/* ===== Home 页面暗黑样式 ===== */
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

/* ===== UserProfile 页面暗黑样式 ===== */
html.dark .profile-header {
    border-bottom-color: #2a2a3e;
}

html.dark .profile-name {
    color: #e0e0f0;
}

html.dark .profile-email {
    color: #8a8aa0;
}

html.dark .profile-join-date {
    color: #6a6a8a;
}

html.dark .stat-value {
    color: #a5b4fc;
}

html.dark .stat-label {
    color: #6a6a8a;
}

html.dark .settings-group-label {
    color: #6a6a8a;
}

html.dark .settings-item {
    border-bottom-color: #2a2a3e;
}

html.dark .settings-title {
    color: #e0e0f0;
}

html.dark .settings-desc {
    color: #8a8aa0;
}

html.dark .settings-arrow {
    color: #6a6a8a;
}

html.dark .settings-item svg:first-child {
    color: #a5b4fc;
}

html.dark .settings-item--danger .settings-title {
    color: #f87171 !important;
}

html.dark .settings-item--danger svg:first-child {
    color: #f87171 !important;
}

html.dark .settings-item--danger .settings-arrow {
    color: #f87171 !important;
}

/* ===== NavBar 暗黑样式补充 ===== */
html.dark .sidebar-collapsed .nav-item span,
html.dark .sidebar-collapsed .nav-item-create span {
    color: #c8c8d8;
}

/* ===== Login / Register 页面暗黑样式 ===== */
html.dark .login-page,
html.dark .register-page {
    background: #0f0f1a;
}

html.dark .login-card,
html.dark .register-card {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .login-title,
html.dark .register-title {
    color: #e0e0f0;
}

html.dark .login-subtitle,
html.dark .register-subtitle {
    color: #8a8aa0;
}

html.dark .login-form .el-input__inner,
html.dark .register-form .el-input__inner {
    background: #22223a;
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark .login-form .el-input__inner:focus,
html.dark .register-form .el-input__inner:focus {
    border-color: #6366f1;
}

html.dark .login-form .el-input__prefix,
html.dark .register-form .el-input__prefix {
    color: #6a6a8a;
}

html.dark .login-footer,
html.dark .register-footer {
    color: #8a8aa0;
}

html.dark .login-footer a,
html.dark .register-footer a {
    color: #a5b4fc;
}

/* ===== ItemDetail 页面暗黑样式 ===== */
html.dark .detail-page {
    background: #1a1a2e;
}

html.dark .detail-header {
    border-bottom-color: #2a2a3e;
}

html.dark .detail-title {
    color: #e0e0f0;
}

html.dark .detail-meta {
    color: #8a8aa0;
}

html.dark .detail-section-title {
    color: #e0e0f0;
}

html.dark .detail-description {
    color: #c8c8d8;
}

html.dark .detail-info-item {
    color: #8a8aa0;
}

html.dark .detail-info-item strong {
    color: #e0e0f0;
}

/* ===== UserItems 页面暗黑样式 ===== */
html.dark .user-items-page {
    background: #1a1a2e;
}

html.dark .user-items-title {
    color: #e0e0f0;
}

html.dark .user-items-tabs .el-tabs__item {
    color: #8a8aa0;
}

html.dark .user-items-tabs .el-tabs__item.is-active {
    color: #a5b4fc;
}

html.dark .user-items-tabs .el-tabs__active-bar {
    background: #a5b4fc;
}

/* ===== UserMessages 页面暗黑样式 ===== */
html.dark .messages-page {
    background: #1a1a2e;
}

html.dark .messages-title {
    color: #e0e0f0;
}

html.dark .message-item {
    border-bottom-color: #2a2a3e;
}

html.dark .message-content {
    color: #c8c8d8;
}

html.dark .message-time {
    color: #6a6a8a;
}

/* ===== PublishForm / ItemCreate 页面暗黑样式 ===== */
html.dark .publish-page,
html.dark .create-page {
    background: #1a1a2e;
}

html.dark .publish-title,
html.dark .create-title {
    color: #e0e0f0;
}

html.dark .publish-form .el-input__inner,
html.dark .create-form .el-input__inner,
html.dark .publish-form .el-textarea__inner,
html.dark .create-form .el-textarea__inner {
    background: #22223a;
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark .publish-form .el-input__inner:focus,
html.dark .create-form .el-input__inner:focus,
html.dark .publish-form .el-textarea__inner:focus,
html.dark .create-form .el-textarea__inner:focus {
    border-color: #6366f1;
}

html.dark .publish-form .el-form-item__label,
html.dark .create-form .el-form-item__label {
    color: #c8c8d8;
}

/* ===== PublishSelect 页面暗黑样式 ===== */
html.dark .publish-select-page {
    background: #1a1a2e;
}

html.dark .publish-select-title {
    color: #e0e0f0;
}

html.dark .publish-card {
    background: #22223a;
    border-color: #2a2a3e;
}

html.dark .publish-card:hover {
    border-color: #6366f1;
}

html.dark .publish-card-title {
    color: #e0e0f0;
}

html.dark .publish-card-desc {
    color: #8a8aa0;
}

/* ===== Element Plus 弹窗暗黑样式 ===== */
html.dark .el-dialog {
    background: #1a1a2e !important;
}

html.dark .el-dialog__title {
    color: #e0e0f0 !important;
}

html.dark .el-dialog__body {
    color: #c8c8d8;
}

html.dark .el-dialog .el-input__inner {
    background: #22223a;
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark .el-dialog .el-input__inner:focus {
    border-color: #6366f1;
}

html.dark .form-label {
    color: #c8c8d8 !important;
}

html.dark .about-content h3 {
    color: #e0e0f0;
}

html.dark .about-version {
    color: #6a6a8a;
}

html.dark .about-desc {
    color: #8a8aa0;
}

html.dark .el-message-box {
    background: #1a1a2e !important;
}

html.dark .el-message-box__title {
    color: #e0e0f0 !important;
}

html.dark .el-message-box__content {
    color: #c8c8d8 !important;
}

html.dark .el-message-box__btns button {
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark .el-message-box__btns button:hover {
    border-color: #6366f1;
    color: #a5b4fc;
}

html.dark .el-pagination button,
html.dark .el-pager li {
    background: #22223a;
    color: #8a8aa0;
}

html.dark .el-pager li.is-active {
    background: #4f46e5 !important;
    color: #fff !important;
}

html.dark .el-pagination .btn-prev,
html.dark .el-pagination .btn-next {
    color: #8a8aa0;
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