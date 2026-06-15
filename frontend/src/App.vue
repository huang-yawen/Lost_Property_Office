<template>
    <div id="app">
        <NavBar />
        <router-view />
    </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
</script>

<style>
/* 全局样式重置 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

html {
    scroll-behavior: smooth;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans SC', sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: #2c3e50;
    background: #f0f2f5;
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
    background: #1a3a5c !important;
    border-color: #1a3a5c !important;
}

.el-button--primary:hover {
    background: #2c5f8a !important;
    border-color: #2c5f8a !important;
}

.el-dialog {
    border-radius: 16px !important;
}

.el-pagination button,
.el-pager li {
    border-radius: 8px !important;
}

.el-pager li.is-active {
    background: #1a3a5c !important;
}
</style>