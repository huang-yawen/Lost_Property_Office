<template>
    <div class="share-poster-overlay" v-if="visible" @click.self="close">
        <div class="share-poster-container">
            <div class="share-poster-header">
                <h3>分享海报</h3>
                <button class="share-poster-close" @click="close">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="18" y1="6" x2="6" y2="18"/>
                        <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                </button>
            </div>
            <div class="share-poster-body">
                <div ref="posterRef" class="poster-card" :class="{ 'poster-found': item?.type === 1, 'poster-lost': item?.type === 2 }">
                    <!-- 顶部装饰 -->
                    <div class="poster-top-deco">
                        <div class="poster-deco-circle"></div>
                        <div class="poster-deco-circle"></div>
                        <div class="poster-deco-circle"></div>
                    </div>

                    <!-- 标题区域 -->
                    <div class="poster-title-area">
                        <div class="poster-badge">
                            {{ item?.type === 1 ? '招领' : '寻物' }}
                        </div>
                        <h2 class="poster-title">{{ item?.title || '物品信息' }}</h2>
                    </div>

                    <!-- 图片区域 -->
                    <div class="poster-image-area" v-if="firstImage">
                        <img :src="firstImage" :alt="item?.title" />
                    </div>
                    <div class="poster-image-area poster-image-placeholder" v-else>
                        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                            <circle cx="8.5" cy="8.5" r="1.5"/>
                            <polyline points="21 15 16 10 5 21"/>
                        </svg>
                    </div>

                    <!-- 信息区域 -->
                    <div class="poster-info-area">
                        <div class="poster-info-row" v-if="item?.description">
                            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                                <polyline points="14 2 14 8 20 8"/>
                                <line x1="16" y1="13" x2="8" y2="13"/>
                                <line x1="16" y1="17" x2="8" y2="17"/>
                            </svg>
                            <span>{{ item.description }}</span>
                        </div>
                        <div class="poster-info-row" v-if="item?.location">
                            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                                <circle cx="12" cy="10" r="3"/>
                            </svg>
                            <span>{{ item.location }}</span>
                        </div>
                        <div class="poster-info-row">
                            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                <circle cx="12" cy="7" r="4"/>
                            </svg>
                            <span>{{ item?.username || '匿名' }}</span>
                            <span v-if="item?.certified" class="poster-cert-badge">已认证</span>
                        </div>
                        <div class="poster-info-row" v-if="item?.contact">
                            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="5" y="2" width="14" height="20" rx="2" ry="2"/>
                                <line x1="12" y1="18" x2="12.01" y2="18"/>
                            </svg>
                            <span>{{ item.contact }}</span>
                        </div>
                    </div>

                    <!-- 二维码区域 -->
                    <div class="poster-qr-area" v-if="qrCodeUrl">
                        <img :src="qrCodeUrl" alt="扫码查看详情" class="poster-qr-img" />
                        <span class="poster-qr-text">扫码查看详情</span>
                    </div>

                    <!-- 底部 -->
                    <div class="poster-footer">
                        <div class="poster-footer-left">
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                                <path d="M2 17l10 5 10-5"/>
                                <path d="M2 12l10 5 10-5"/>
                            </svg>
                            <span>校园失物招领系统</span>
                        </div>
                        <div class="poster-footer-right">
                            <span>{{ timeStr }}</span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="share-poster-actions">
                <button class="poster-action-btn poster-action-save" @click="handleSave" :disabled="saving">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                        <polyline points="7 10 12 15 17 10"/>
                        <line x1="12" y1="15" x2="12" y2="3"/>
                    </svg>
                    {{ saving ? '保存中...' : '保存图片' }}
                </button>
                <button class="poster-action-btn poster-action-share" @click="handleSharePoster">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="18" cy="5" r="3"/>
                        <circle cx="6" cy="12" r="3"/>
                        <circle cx="18" cy="19" r="3"/>
                        <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                        <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
                    </svg>
                    分享
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
    visible: {
        type: Boolean,
        default: false
    },
    item: {
        type: Object,
        default: null
    }
})

const emit = defineEmits(['close'])

const posterRef = ref(null)
const saving = ref(false)
const qrCodeUrl = ref('')

const firstImage = computed(() => {
    if (!props.item?.images) return ''
    try {
        const arr = JSON.parse(props.item.images)
        return arr[0] || ''
    } catch {
        return ''
    }
})

const timeStr = computed(() => {
    if (!props.item?.createTime) return ''
    const date = new Date(props.item.createTime)
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}.${m}.${d}`
})

// 生成二维码
watch(() => props.visible, async (val) => {
    if (val && props.item?.id) {
        await generateQRCode()
    }
})

async function generateQRCode() {
    try {
        const QRCode = (await import('qrcode')).default
        const url = `${window.location.origin}/#/item/${props.item.id}`
        qrCodeUrl.value = await QRCode.toDataURL(url, {
            width: 120,
            height: 120,
            margin: 1,
            color: {
                dark: '#4a4a6a',
                light: '#ffffff'
            }
        })
    } catch (error) {
        console.error('生成二维码失败:', error)
    }
}

function close() {
    emit('close')
}

async function handleSave() {
    saving.value = true
    try {
        const html2canvas = (await import('html2canvas')).default
        const canvas = await html2canvas(posterRef.value, {
            scale: 2,
            useCORS: true,
            backgroundColor: '#ffffff',
            logging: false
        })
        const link = document.createElement('a')
        link.download = `校园失物招领-${props.item?.title || '分享'}.png`
        link.href = canvas.toDataURL('image/png')
        link.click()
        ElMessage.success('海报已保存')
    } catch (error) {
        console.error('保存海报失败:', error)
        ElMessage.error('保存失败，请重试')
    } finally {
        saving.value = false
    }
}

async function handleSharePoster() {
    try {
        const html2canvas = (await import('html2canvas')).default
        const canvas = await html2canvas(posterRef.value, {
            scale: 2,
            useCORS: true,
            backgroundColor: '#ffffff',
            logging: false
        })
        canvas.toBlob(async (blob) => {
            if (!blob) {
                ElMessage.error('生成图片失败')
                return
            }
            const file = new File([blob], 'poster.png', { type: 'image/png' })
            if (navigator.share && navigator.canShare({ files: [file] })) {
                try {
                    await navigator.share({
                        title: `校园失物招领 - ${props.item?.title || ''}`,
                        files: [file]
                    })
                } catch (e) {
                    if (e.name !== 'AbortError') {
                        ElMessage.error('分享失败')
                    }
                }
            } else {
                // 不支持文件分享，保存到本地
                const link = document.createElement('a')
                link.download = `校园失物招领-${props.item?.title || '分享'}.png`
                link.href = canvas.toDataURL('image/png')
                link.click()
                ElMessage.success('海报已保存到本地')
            }
        }, 'image/png')
    } catch (error) {
        console.error('分享海报失败:', error)
        ElMessage.error('分享失败，请重试')
    }
}
</script>

<style scoped>
.share-poster-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 3100;
    backdrop-filter: blur(4px);
}

.share-poster-container {
    background: #fff;
    border-radius: 16px;
    width: 380px;
    max-height: 90vh;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.share-poster-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    border-bottom: 1px solid #e2e8f0;
}

.share-poster-header h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #4a4a6a;
}

.share-poster-close {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    border: none;
    background: #f0f2f5;
    color: #666;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
}

.share-poster-close:hover {
    background: #e2e8f0;
    color: #333;
}

.share-poster-body {
    padding: 20px;
    display: flex;
    justify-content: center;
}

/* 海报卡片 */
.poster-card {
    width: 320px;
    border-radius: 16px;
    overflow: hidden;
    background: #fff;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    position: relative;
}

.poster-found {
    border: 2px solid #2ea86e;
}

.poster-lost {
    border: 2px solid #e74c3c;
}

/* 顶部装饰 */
.poster-top-deco {
    display: flex;
    gap: 6px;
    padding: 12px 16px 8px;
}

.poster-deco-circle {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #e2e8f0;
}

.poster-found .poster-deco-circle:first-child {
    background: #2ea86e;
}

.poster-lost .poster-deco-circle:first-child {
    background: #e74c3c;
}

/* 标题区域 */
.poster-title-area {
    padding: 0 16px 12px;
    text-align: center;
}

.poster-badge {
    display: inline-block;
    padding: 3px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;
    margin-bottom: 8px;
}

.poster-found .poster-badge {
    background: linear-gradient(135deg, #1a8a5c, #2ea86e);
}

.poster-lost .poster-badge {
    background: linear-gradient(135deg, #c0392b, #e74c3c);
}

.poster-title {
    font-size: 18px;
    font-weight: 700;
    color: #4a4a6a;
    margin: 0;
    line-height: 1.4;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
}

/* 图片区域 */
.poster-image-area {
    margin: 0 16px 12px;
    border-radius: 12px;
    overflow: hidden;
    height: 180px;
    background: #f0f2f5;
}

.poster-image-area img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.poster-image-placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #c0c4cc;
}

/* 信息区域 */
.poster-info-area {
    padding: 0 16px 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.poster-info-row {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    font-size: 12px;
    color: #4a5568;
    line-height: 1.5;
}

.poster-info-row svg {
    flex-shrink: 0;
    margin-top: 2px;
    color: #4f46e5;
}

.poster-info-row span {
    flex: 1;
    word-break: break-all;
}

.poster-cert-badge {
    display: inline-block;
    font-size: 10px;
    font-weight: 600;
    color: #059669;
    background: rgba(16, 185, 129, 0.1);
    padding: 1px 6px;
    border-radius: 6px;
    flex-shrink: 0;
    margin-left: 4px;
}

/* 二维码区域 */
.poster-qr-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 8px 16px 12px;
    gap: 4px;
}

.poster-qr-img {
    width: 72px;
    height: 72px;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
}

.poster-qr-text {
    font-size: 10px;
    color: #a0aec0;
}

html.dark .poster-qr-img {
    border-color: #2a2a3e;
}

html.dark .poster-qr-text {
    color: #6a6a8a;
}

/* 底部 */
.poster-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px;
    background: #f7fafc;
    border-top: 1px solid #e2e8f0;
}

.poster-footer-left {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 11px;
    color: #4f46e5;
    font-weight: 500;
}

.poster-footer-right {
    font-size: 11px;
    color: #a0aec0;
}

/* 操作按钮 */
.share-poster-actions {
    display: flex;
    gap: 10px;
    padding: 16px 20px;
    border-top: 1px solid #e2e8f0;
}

.poster-action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px 16px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s;
    border: 1px solid #e2e8f0;
    background: #fff;
    color: #4a5568;
}

.poster-action-btn:hover:not(:disabled) {
    transform: translateY(-1px);
}

.poster-action-save:hover:not(:disabled) {
    border-color: #4f46e5;
    color: #4f46e5;
    background: #f0f0ff;
}

.poster-action-share:hover:not(:disabled) {
    border-color: #10b981;
    color: #10b981;
    background: #f0faf5;
}

.poster-action-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

/* 暗黑模式 */
html.dark .share-poster-container {
    background: #1a1a2e;
}

html.dark .share-poster-header {
    border-bottom-color: #2a2a3e;
}

html.dark .share-poster-header h3 {
    color: #e0e0f0;
}

html.dark .share-poster-close {
    background: #2a2a4e;
    color: #8a8aa0;
}

html.dark .share-poster-close:hover {
    background: #3a3a5e;
    color: #e0e0f0;
}

html.dark .poster-card {
    background: #1a1a2e;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

html.dark .poster-title {
    color: #e0e0f0;
}

html.dark .poster-info-row {
    color: #8a8aa0;
}

html.dark .poster-footer {
    background: #0f0f1a;
    border-top-color: #2a2a3e;
}

html.dark .poster-footer-right {
    color: #6a6a8a;
}

html.dark .share-poster-actions {
    border-top-color: #2a2a3e;
}

html.dark .poster-action-btn {
    background: #1a1a2e;
    border-color: #2a2a3e;
    color: #8a8aa0;
}

html.dark .poster-action-save:hover:not(:disabled) {
    border-color: #6366f1;
    color: #a5b4fc;
    background: rgba(99, 102, 241, 0.1);
}

html.dark .poster-action-share:hover:not(:disabled) {
    border-color: #2ea86e;
    color: #2ea86e;
    background: rgba(46, 168, 110, 0.1);
}
</style>