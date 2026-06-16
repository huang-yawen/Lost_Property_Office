<template>
    <div class="admin-cert-page">
        <div class="admin-cert-header">
            <h1>认证审核管理</h1>
            <div class="admin-cert-tabs">
                <button
                    :class="['tab-btn', { active: activeTab === 'pending' }]"
                    @click="activeTab = 'pending'"
                >
                    待审核
                    <span v-if="pendingCount > 0" class="tab-count">{{ pendingCount }}</span>
                </button>
                <button
                    :class="['tab-btn', { active: activeTab === 'approved' }]"
                    @click="activeTab = 'approved'"
                >
                    已通过
                </button>
                <button
                    :class="['tab-btn', { active: activeTab === 'rejected' }]"
                    @click="activeTab = 'rejected'"
                >
                    已拒绝
                </button>
            </div>
        </div>

        <div class="admin-cert-body">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
                <div class="loading-spinner"></div>
                <p>加载中...</p>
            </div>

            <!-- 空状态 -->
            <div v-else-if="list.length === 0" class="empty-state">
                <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#c0c4cc" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                    <polyline points="14 2 14 8 20 8"/>
                    <line x1="16" y1="13" x2="8" y2="13"/>
                    <line x1="16" y1="17" x2="8" y2="17"/>
                    <polyline points="10 9 9 9 8 9"/>
                </svg>
                <p>{{ emptyText }}</p>
            </div>

            <!-- 列表 -->
            <div v-else class="cert-list">
                <div
                    v-for="item in list"
                    :key="item.id"
                    class="cert-card"
                    @click="showDetail(item)"
                >
                    <div class="cert-card-left">
                        <div class="cert-avatar">
                            {{ item.username?.charAt(0)?.toUpperCase() || '?' }}
                        </div>
                        <div class="cert-info">
                            <div class="cert-info-row">
                                <span class="cert-username">{{ item.username || '未知用户' }}</span>
                                <span class="cert-status" :class="'status-' + (item.certificationStatus || item.status)">
                                    {{ statusMap[item.certificationStatus || item.status] || '未知' }}
                                </span>
                            </div>
                            <div class="cert-info-row cert-details">
                                <span>{{ item.realName || '未填写' }}</span>
                                <span class="dot">·</span>
                                <span>{{ item.studentId || '未填写' }}</span>
                                <span class="dot">·</span>
                                <span>{{ item.department || '未填写' }}</span>
                            </div>
                            <div class="cert-info-row cert-time">
                                {{ formatTime(item.createTime || item.submitTime) }}
                            </div>
                        </div>
                    </div>

                    <div class="cert-card-right" v-if="(item.certificationStatus || item.status) === 'pending' || (item.certificationStatus || item.status) === 0">
                        <button class="action-btn action-approve" @click.stop="handleApprove(item)" title="通过认证">
                            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <polyline points="20 6 9 17 4 12"/>
                            </svg>
                            通过
                        </button>
                        <button class="action-btn action-reject" @click.stop="handleReject(item)" title="拒绝">
                            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                                <line x1="18" y1="6" x2="6" y2="18"/>
                                <line x1="6" y1="6" x2="18" y2="18"/>
                            </svg>
                            拒绝
                        </button>
                    </div>

                    <div class="cert-card-right" v-else-if="item.rejectReason">
                        <div class="reject-hint">
                            <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#dc2626" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <circle cx="12" cy="12" r="10"/>
                                <line x1="12" y1="8" x2="12" y2="12"/>
                                <line x1="12" y1="16" x2="12.01" y2="16"/>
                            </svg>
                            <span>{{ item.rejectReason }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 申请详情对话框 -->
        <el-dialog v-model="showDetailDialog" title="认证申请详情" width="520px" :close-on-click-modal="false">
            <div class="detail-content" v-if="detailItem">
                <div class="detail-section">
                    <h3 class="detail-section-title">申请人信息</h3>
                    <div class="detail-grid">
                        <div class="detail-field">
                            <label>用户名</label>
                            <span>{{ detailItem.username }}</span>
                        </div>
                        <div class="detail-field">
                            <label>真实姓名</label>
                            <span>{{ detailItem.realName || '未填写' }}</span>
                        </div>
                        <div class="detail-field">
                            <label>学号/工号</label>
                            <span>{{ detailItem.studentId || '未填写' }}</span>
                        </div>
                        <div class="detail-field">
                            <label>学院/部门</label>
                            <span>{{ detailItem.department || '未填写' }}</span>
                        </div>
                        <div class="detail-field" v-if="detailItem.phone">
                            <label>手机号</label>
                            <span>{{ detailItem.phone }}</span>
                        </div>
                        <div class="detail-field">
                            <label>申请状态</label>
                            <span class="cert-status" :class="'status-' + (detailItem.certificationStatus || detailItem.status)">
                                {{ statusMap[detailItem.certificationStatus || detailItem.status] || '未知' }}
                            </span>
                        </div>
                    </div>
                </div>

                <div class="detail-section" v-if="detailItem.reason">
                    <h3 class="detail-section-title">认证说明</h3>
                    <p class="detail-reason">{{ detailItem.reason }}</p>
                </div>

                <div class="detail-section" v-if="detailItem.rejectReason">
                    <h3 class="detail-section-title">拒绝原因</h3>
                    <p class="detail-reason detail-reject">{{ detailItem.rejectReason }}</p>
                </div>

                <div class="detail-section" v-if="detailItem.certificationImage">
                    <h3 class="detail-section-title">学生证图片</h3>
                    <div class="detail-image-wrapper">
                        <img
                            :src="detailItem.certificationImage"
                            alt="学生证"
                            class="detail-image"
                            @click="openPreview(detailItem.certificationImage)"
                        />
                        <p class="detail-image-hint">点击图片可放大查看</p>
                    </div>
                </div>

                <div class="detail-section">
                    <h3 class="detail-section-title">提交时间</h3>
                    <p class="detail-time">{{ formatTime(detailItem.createTime || detailItem.submitTime) }}</p>
                </div>

                <div class="detail-actions" v-if="(detailItem.certificationStatus || detailItem.status) === 'pending' || (detailItem.certificationStatus || detailItem.status) === 0">
                    <button class="detail-action-btn detail-action-approve" @click="handleApprove(detailItem)">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="20 6 9 17 4 12"/>
                        </svg>
                        通过认证
                    </button>
                    <button class="detail-action-btn detail-action-reject" @click="handleRejectFromDetail">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="18" y1="6" x2="6" y2="18"/>
                            <line x1="6" y1="6" x2="18" y2="18"/>
                        </svg>
                        拒绝
                    </button>
                </div>
            </div>
        </el-dialog>

        <!-- 图片预览对话框 -->
        <el-dialog v-model="showPreviewDialog" title="学生证预览" width="600px" :close-on-click-modal="true" @close="previewImage = ''">
            <div class="preview-wrapper" v-if="previewImage">
                <img :src="previewImage" alt="学生证预览" class="preview-image" />
            </div>
        </el-dialog>

        <!-- 拒绝原因对话框 -->
        <el-dialog v-model="showRejectDialog" title="拒绝认证" width="400px" :close-on-click-modal="false">
            <div class="reject-form">
                <label class="reject-label">请输入拒绝原因：</label>
                <textarea
                    v-model="rejectReason"
                    class="reject-textarea"
                    placeholder="请说明拒绝原因..."
                    rows="3"
                    maxlength="200"
                ></textarea>
                <span class="reject-count">{{ rejectReason.length }}/200</span>
            </div>
            <template #footer>
                <el-button @click="showRejectDialog = false">取消</el-button>
                <el-button type="danger" @click="confirmReject" :loading="rejecting">确认拒绝</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../api'

const activeTab = ref('pending')
const list = ref([])
const loading = ref(false)
const pendingCount = ref(0)

// 详情对话框
const showDetailDialog = ref(false)
const detailItem = ref(null)

// 图片预览
const showPreviewDialog = ref(false)
const previewImage = ref('')

// 拒绝对话框
const showRejectDialog = ref(false)
const rejectReason = ref('')
const rejecting = ref(false)
const currentRejectItem = ref(null)

const statusMap = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
}

const emptyText = computed(() => {
    const map = {
        pending: '暂无待审核的认证申请',
        approved: '暂无已通过的认证',
        rejected: '暂无已拒绝的认证'
    }
    return map[activeTab.value] || '暂无数据'
})

function formatTime(time) {
    if (!time) return ''
    const date = new Date(time)
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    const h = String(date.getHours()).padStart(2, '0')
    const min = String(date.getMinutes()).padStart(2, '0')
    return `${y}-${m}-${d} ${h}:${min}`
}

async function fetchList() {
    loading.value = true
    try {
        const res = await adminApi.getCertificationList({ status: activeTab.value })
        list.value = res.data || []
        if (activeTab.value === 'pending') {
            pendingCount.value = list.value.length
        }
    } catch (error) {
        console.error('获取认证列表失败:', error)
        ElMessage.error('获取列表失败')
        list.value = []
    } finally {
        loading.value = false
    }
}

async function handleApprove(item) {
    try {
        await ElMessageBox.confirm(
            `确认通过用户 "${item.username}" 的认证申请？`,
            '确认通过',
            {
                confirmButtonText: '确认通过',
                cancelButtonText: '取消',
                type: 'success'
            }
        )
        await adminApi.reviewCertification({ userId: item.userId || item.id, action: 'approve' })
        ElMessage.success('已通过认证')
        showDetailDialog.value = false
        fetchList()
    } catch (error) {
        if (error !== 'cancel') {
            console.error('操作失败:', error)
        }
    }
}

function showDetail(item) {
    detailItem.value = item
    showDetailDialog.value = true
}

function openPreview(img) {
    previewImage.value = img
    showPreviewDialog.value = true
}

function handleReject(item) {
    showDetailDialog.value = false
    currentRejectItem.value = item
    rejectReason.value = ''
    showRejectDialog.value = true
}

function handleRejectFromDetail() {
    handleReject(detailItem.value)
}

async function confirmReject() {
    if (!rejectReason.value.trim()) {
        ElMessage.warning('请输入拒绝原因')
        return
    }
    rejecting.value = true
    try {
        await adminApi.reviewCertification({
            userId: currentRejectItem.value.userId || currentRejectItem.value.id,
            action: 'reject',
            reason: rejectReason.value.trim()
        })
        ElMessage.success('已拒绝认证')
        showRejectDialog.value = false
        fetchList()
    } catch (error) {
        console.error('拒绝失败:', error)
    } finally {
        rejecting.value = false
    }
}

onMounted(() => {
    fetchList()
})

watch(activeTab, () => {
    fetchList()
})
</script>

<style scoped>
.admin-cert-page {
    min-height: 100%;
}

.admin-cert-header {
    padding: 0 0 20px;
}

.admin-cert-header h1 {
    font-size: 20px;
    font-weight: 700;
    color: #4a4a6a;
    margin: 0 0 16px;
}

.admin-cert-tabs {
    display: flex;
    gap: 8px;
}

.tab-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 20px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: #fff;
    color: #4a5568;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
}

.tab-btn:hover {
    border-color: #a5b4fc;
    color: #4f46e5;
}

.tab-btn.active {
    background: #4f46e5;
    color: #fff;
    border-color: #4f46e5;
}

.tab-count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    border-radius: 8px;
    font-size: 11px;
    font-weight: 600;
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
}

/* 加载状态 */
.loading-state {
    text-align: center;
    padding: 60px 0;
    color: #8a8aa0;
}

.loading-spinner {
    width: 32px;
    height: 32px;
    border: 3px solid #e2e8f0;
    border-top-color: #4f46e5;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
    margin: 0 auto 12px;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
    text-align: center;
    padding: 60px 0;
    color: #a0aec0;
}

.empty-state p {
    margin-top: 12px;
    font-size: 14px;
}

/* 认证卡片 - 水平布局 */
.cert-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.cert-card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #fff;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    padding: 14px 20px;
    transition: box-shadow 0.2s;
    gap: 16px;
    cursor: pointer;
}

.cert-card:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

/* 左侧：头像 + 信息 */
.cert-card-left {
    display: flex;
    align-items: center;
    gap: 14px;
    flex: 1;
    min-width: 0;
}

.cert-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4f46e5, #6366f1);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 600;
    flex-shrink: 0;
}

.cert-info {
    min-width: 0;
}

.cert-info-row {
    display: flex;
    align-items: center;
    gap: 6px;
}

.cert-username {
    font-size: 14px;
    font-weight: 600;
    color: #4a4a6a;
}

.cert-status {
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 11px;
    font-weight: 500;
}

.status-pending {
    background: #fef3c7;
    color: #d97706;
}

.status-approved {
    background: #d1fae5;
    color: #059669;
}

.status-rejected {
    background: #fee2e2;
    color: #dc2626;
}

.cert-details {
    margin-top: 3px;
    font-size: 13px;
    color: #8a8aa0;
}

.dot {
    color: #d0d0d8;
}

.cert-time {
    margin-top: 2px;
    font-size: 12px;
    color: #b0b0c0;
}

/* 右侧：操作按钮 */
.cert-card-right {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
}

.action-btn {
    display: inline-flex;
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
    white-space: nowrap;
}

.action-approve {
    color: #059669;
    border-color: #d1fae5;
}

.action-approve:hover {
    background: #d1fae5;
    border-color: #059669;
}

.action-reject {
    color: #dc2626;
    border-color: #fee2e2;
}

.action-reject:hover {
    background: #fee2e2;
    border-color: #dc2626;
}

/* 拒绝原因提示 */
.reject-hint {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #dc2626;
    max-width: 200px;
}

.reject-hint span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

/* 详情对话框 */
.detail-content {
    max-height: 70vh;
    overflow-y: auto;
}

.detail-section {
    margin-bottom: 20px;
}

.detail-section:last-child {
    margin-bottom: 0;
}

.detail-section-title {
    font-size: 13px;
    font-weight: 600;
    color: #8a8aa0;
    margin: 0 0 10px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.detail-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
}

.detail-field {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.detail-field label {
    font-size: 12px;
    color: #a0aec0;
}

.detail-field span {
    font-size: 14px;
    color: #4a4a6a;
}

.detail-reason {
    margin: 0;
    font-size: 14px;
    color: #4a5568;
    line-height: 1.6;
    padding: 12px;
    background: #f8f9fc;
    border-radius: 8px;
}

.detail-reject {
    color: #dc2626;
    background: #fef2f2;
}

.detail-image-wrapper {
    text-align: center;
}

.detail-image {
    max-width: 100%;
    max-height: 300px;
    border-radius: 8px;
    border: 1px solid #e2e8f0;
    cursor: zoom-in;
    transition: opacity 0.2s;
}

.detail-image:hover {
    opacity: 0.85;
}

.detail-image-hint {
    margin: 8px 0 0;
    font-size: 12px;
    color: #a0aec0;
}

.detail-time {
    margin: 0;
    font-size: 14px;
    color: #4a5568;
}

.detail-actions {
    display: flex;
    gap: 12px;
    margin-top: 20px;
    padding-top: 16px;
    border-top: 1px solid #e2e8f0;
}

.detail-action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    padding: 10px 20px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.25s;
    border: 1px solid #e2e8f0;
    background: #fff;
}

.detail-action-approve {
    color: #059669;
    border-color: #d1fae5;
}

.detail-action-approve:hover {
    background: #d1fae5;
    border-color: #059669;
}

.detail-action-reject {
    color: #dc2626;
    border-color: #fee2e2;
}

.detail-action-reject:hover {
    background: #fee2e2;
    border-color: #dc2626;
}

/* 图片预览 */
.preview-wrapper {
    text-align: center;
}

.preview-image {
    max-width: 100%;
    max-height: 70vh;
    border-radius: 8px;
}

/* 拒绝对话框 */
.reject-form {
    position: relative;
}

.reject-label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #4a5568;
    margin-bottom: 8px;
}

.reject-textarea {
    width: 100%;
    padding: 12px 16px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    font-size: 14px;
    color: #4a4a6a;
    outline: none;
    resize: none;
    font-family: inherit;
    box-sizing: border-box;
    transition: border-color 0.2s;
}

.reject-textarea:focus {
    border-color: #dc2626;
}

.reject-count {
    position: absolute;
    bottom: 12px;
    right: 12px;
    font-size: 12px;
    color: #a0aec0;
}

/* 暗黑模式 */
html.dark .admin-cert-header h1 {
    color: #e0e0f0;
}

html.dark .tab-btn {
    background: #1a1a2e;
    border-color: #2a2a3e;
    color: #8a8aa0;
}

html.dark .tab-btn:hover {
    border-color: #6366f1;
    color: #a5b4fc;
}

html.dark .tab-btn.active {
    background: #4f46e5;
    color: #fff;
    border-color: #4f46e5;
}

html.dark .cert-card {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .cert-username {
    color: #e0e0f0;
}

html.dark .cert-details {
    color: #8a8aa0;
}

html.dark .dot {
    color: #3a3a5a;
}

html.dark .cert-time {
    color: #6a6a8a;
}

html.dark .action-btn {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .detail-field span {
    color: #c8c8d8;
}

html.dark .detail-reason {
    background: #0f0f1a;
    color: #c8c8d8;
}

html.dark .detail-reject {
    background: #2a1a1a;
    color: #f87171;
}

html.dark .detail-image {
    border-color: #2a2a3e;
}

html.dark .detail-time {
    color: #c8c8d8;
}

html.dark .detail-actions {
    border-top-color: #2a2a3e;
}

html.dark .detail-action-btn {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .reject-label {
    color: #c8c8d8;
}

html.dark .reject-textarea {
    background: #22223a;
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark .reject-textarea:focus {
    border-color: #dc2626;
}
</style>