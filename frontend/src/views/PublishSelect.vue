<template>
    <div class="publish-select-page">
        <div class="ps-container">
            <div class="ps-header">
                <h1 class="ps-title">发布信息</h1>
                <p class="ps-desc">请选择您要发布的信息类型</p>
            </div>
            <div class="ps-cards">
                <div class="ps-card ps-card-found" @click="goPublish(1)">
                    <div class="ps-card-icon">
                        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                            <polyline points="22 4 12 14.01 9 11.01"/>
                        </svg>
                    </div>
                    <h2 class="ps-card-title">招领</h2>
                    <p class="ps-card-desc">我捡到了物品，希望找到失主</p>
                    <span class="ps-card-action">发布招领 →</span>
                </div>
                <div class="ps-card ps-card-lost" @click="goPublish(2)">
                    <div class="ps-card-icon">
                        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="11" cy="11" r="8"/>
                            <path d="M21 21l-4.35-4.35"/>
                            <line x1="8" y1="11" x2="14" y2="11"/>
                        </svg>
                    </div>
                    <h2 class="ps-card-title">寻物</h2>
                    <p class="ps-card-desc">我丢失了物品，希望找回</p>
                    <span class="ps-card-action">发布寻物 →</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goPublish(type) {
    router.push({ name: 'PublishForm', query: { type } })
}
</script>

<style scoped>
.publish-select-page {
    min-height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
}

.ps-container {
    text-align: center;
    max-width: 640px;
    width: 100%;
}

.ps-header {
    margin-bottom: 48px;
}

.ps-title {
    font-size: 28px;
    font-weight: 700;
    color: #4a4a6a;
    margin-bottom: 8px;
}

.ps-desc {
    font-size: 15px;
    color: #718096;
}

.ps-cards {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
}

.ps-card {
    background: #fff;
    border-radius: 8px;
    padding: 48px 32px;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid #e2e8f0;
    text-align: center;
}

.ps-card:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
}

.ps-card-found:hover {
    border-color: #059669;
    background: linear-gradient(180deg, #fff 0%, #f0fdf4 100%);
}

.ps-card-lost:hover {
    border-color: #dc2626;
    background: linear-gradient(180deg, #fff 0%, #fef2f2 100%);
}

.ps-card-icon {
    margin-bottom: 20px;
    color: #a0aec0;
    transition: color 0.3s;
}

.ps-card-found:hover .ps-card-icon {
    color: #059669;
}

.ps-card-lost:hover .ps-card-icon {
    color: #dc2626;
}

.ps-card-title {
    font-size: 22px;
    font-weight: 700;
    color: #4a4a6a;
    margin-bottom: 8px;
}

.ps-card-desc {
    font-size: 14px;
    color: #718096;
    margin-bottom: 24px;
    line-height: 1.5;
}

.ps-card-action {
    display: inline-block;
    font-size: 14px;
    font-weight: 600;
    padding: 8px 24px;
    border-radius: 6px;
    transition: all 0.3s;
}

.ps-card-found .ps-card-action {
    color: #059669;
    background: #f0fdf4;
}

.ps-card-lost .ps-card-action {
    color: #dc2626;
    background: #fef2f2;
}

.ps-card-found:hover .ps-card-action {
    background: #059669;
    color: #fff;
}

.ps-card-lost:hover .ps-card-action {
    background: #dc2626;
    color: #fff;
}
</style>