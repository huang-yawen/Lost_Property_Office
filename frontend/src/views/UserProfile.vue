<template>
    <div class="profile-page" style="margin-top: 0px;padding-top: 10px;">
        <!-- 顶部：头像 + 信息 + 统计数据 - 与背景融为一体 -->
        <div class="profile-header">
            <div class="profile-avatar" @click="showAvatarUpload = true">
                <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="avatar" />
                <svg v-else viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                </svg>
                <div class="avatar-overlay">
                    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                        <circle cx="12" cy="13" r="4"/>
                    </svg>
                </div>
            </div>
            <div class="profile-info">
                <div class="profile-name-row">
                    <h1 class="profile-name">{{ userInfo?.nickname || userInfo?.username || '未登录' }}</h1>
                    <!-- 校园认证标识 -->
                    <span v-if="userInfo?.certified" class="cert-badge" title="已通过校园认证">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                            <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/>
                        </svg>
                        已认证
                    </span>
                    <span v-else-if="userInfo?.certificationStatus === 'pending'" class="cert-badge cert-pending" title="认证审核中">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                        </svg>
                        审核中
                    </span>
                </div>
                <p class="profile-email">{{ userInfo?.email || '暂无邮箱' }}</p>
                <p class="profile-join-date" v-if="userInfo?.createdAt">注册时间：{{ formatDate(userInfo.createdAt) }}</p>
            </div>
            <div class="profile-stats">
                <div class="stat-item">
                    <span class="stat-value">{{ stats.total }}</span>
                    <span class="stat-label">全部</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">{{ stats.found }}</span>
                    <span class="stat-label">招领</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">{{ stats.lost }}</span>
                    <span class="stat-label">寻物</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">{{ stats.resolved }}</span>
                    <span class="stat-label">已解决</span>
                </div>
            </div>
        </div>

        <!-- 信誉积分展示 -->
        <div class="reputation-section">
            <div class="reputation-bar">
                <div class="reputation-label">
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                    </svg>
                    信誉积分
                </div>
                <div class="reputation-score">
                    <span class="score-value" :class="reputationLevel">{{ userInfo?.reputation ?? 100 }}</span>
                    <span class="score-max">/ 100</span>
                </div>
                <div class="reputation-track">
                    <div class="reputation-fill" :style="{ width: (userInfo?.reputation ?? 100) + '%' }" :class="reputationLevel"></div>
                </div>
                <span class="reputation-desc">{{ reputationDesc }}</span>
            </div>
        </div>

        <!-- 设置相关导航 - 与背景融为一体 -->
        <div class="settings-section">
            <div class="settings-group-label">账号管理</div>

            <router-link to="/my-items" class="settings-item">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="3" width="7" height="7"/>
                    <rect x="14" y="3" width="7" height="7"/>
                    <rect x="14" y="14" width="7" height="7"/>
                    <rect x="3" y="14" width="7" height="7"/>
                </svg>
                <div class="settings-text">
                    <span class="settings-title">我的发布</span>
                    <span class="settings-desc">管理您发布的招领和寻物信息</span>
                </div>
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="settings-arrow">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
            </router-link>

            <router-link to="/messages" class="settings-item">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
                </svg>
                <div class="settings-text">
                    <span class="settings-title">我的消息</span>
                    <span class="settings-desc">查看收到的留言和私信</span>
                </div>
                <span class="unread-badge" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="settings-arrow">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
            </router-link>

            <button class="settings-item" @click="showEditProfile = true">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
                <div class="settings-text">
                    <span class="settings-title">编辑资料</span>
                    <span class="settings-desc">修改昵称、邮箱等个人信息</span>
                </div>
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="settings-arrow">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
            </button>

            <button class="settings-item" @click="showCertification = true">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
                </svg>
                <div class="settings-text">
                    <span class="settings-title">校园认证</span>
                    <span class="settings-desc">{{ certDesc }}</span>
                </div>
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="settings-arrow">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
            </button>

            <button class="settings-item" @click="showChangePassword = true">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                <div class="settings-text">
                    <span class="settings-title">修改密码</span>
                    <span class="settings-desc">定期更换密码保障账号安全</span>
                </div>
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="settings-arrow">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
            </button>

            <div class="settings-group-label">其他</div>

            <button class="settings-item" @click="showAbout = true">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="16" x2="12" y2="12"/>
                    <line x1="12" y1="8" x2="12.01" y2="8"/>
                </svg>
                <div class="settings-text">
                    <span class="settings-title">关于</span>
                    <span class="settings-desc">查看应用版本和相关信息</span>
                </div>
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="settings-arrow">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
            </button>

            <button class="settings-item settings-item--danger" @click="handleLogout">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="#c0392b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                </svg>
                <div class="settings-text">
                    <span class="settings-title" style="color: #c0392b;">退出登录</span>
                    <span class="settings-desc">退出当前账号</span>
                </div>
                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#c0392b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="settings-arrow">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
            </button>
        </div>

        <!-- 编辑资料弹窗 -->
        <el-dialog v-model="showEditProfile" title="编辑资料" width="520px" :close-on-click-modal="false">
            <div class="dialog-form">
                <div class="form-field">
                    <label class="form-label">昵称</label>
                    <el-input v-model="editForm.nickname" placeholder="请输入昵称" class="form-input" />
                </div>
                <div class="form-field">
                    <label class="form-label">邮箱</label>
                    <el-input v-model="editForm.email" placeholder="请输入邮箱" class="form-input" />
                </div>
                <div class="form-field">
                    <label class="form-label">手机号</label>
                    <el-input v-model="editForm.phone" placeholder="请输入手机号" class="form-input" />
                </div>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button class="btn-cancel" @click="showEditProfile = false">取消</el-button>
                    <el-button type="primary" class="btn-primary" :loading="saving" @click="saveProfile">保存</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 校园认证弹窗 -->
        <el-dialog v-model="showCertification" title="校园认证" width="520px" :close-on-click-modal="false">
            <div class="dialog-form">
                <div class="cert-status-info">
                    <div v-if="userInfo?.certified" class="cert-status cert-approved">
                        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#10b981" stroke-width="1.5">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                            <polyline points="22 4 12 14.01 9 11.01"/>
                        </svg>
                        <h3>已通过校园认证</h3>
                        <p>您已完成校园身份认证，发布信息将显示认证标识</p>
                    </div>
                    <div v-else-if="userInfo?.certificationStatus === 'pending'" class="cert-status cert-pending-status">
                        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#f59e0b" stroke-width="1.5">
                            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/>
                            <line x1="12" y1="16" x2="12" y2="12"/>
                            <line x1="12" y1="8" x2="12.01" y2="8"/>
                        </svg>
                        <h3>认证审核中</h3>
                        <p>您的认证申请已提交，请耐心等待管理员审核</p>
                    </div>
                    <div v-else class="cert-status cert-none">
                        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#6b7280" stroke-width="1.5">
                            <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z"/>
                        </svg>
                        <h3>未认证</h3>
                        <p>上传学生证或校园卡照片进行校园身份认证</p>
                    </div>
                </div>
                <div v-if="!userInfo?.certified && userInfo?.certificationStatus !== 'pending'" class="cert-form-fields">
                    <div class="form-field">
                        <label class="form-label">真实姓名 <span style="color: #ef4444;">*</span></label>
                        <el-input v-model="certForm.real_name" placeholder="请输入真实姓名" class="form-input" />
                    </div>
                    <div class="form-field">
                        <label class="form-label">学号/工号 <span style="color: #ef4444;">*</span></label>
                        <el-input v-model="certForm.student_id" placeholder="请输入学号或工号" class="form-input" />
                    </div>
                    <div class="form-field">
                        <label class="form-label">学院/校区</label>
                        <el-input v-model="certForm.campus" placeholder="请输入所在学院或校区（可选）" class="form-input" />
                    </div>
                    <div class="form-field">
                        <label class="form-label">上传学生证/校园卡照片 <span style="color: #ef4444;">*</span></label>
                        <div class="upload-area" @click="triggerCertUpload">
                            <img v-if="certImagePreview" :src="certImagePreview" class="upload-preview" />
                            <div v-else class="upload-placeholder">
                                <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="#9ca3af" stroke-width="1.5">
                                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                                    <polyline points="17 8 12 3 7 8"/>
                                    <line x1="12" y1="3" x2="12" y2="15"/>
                                </svg>
                                <span>点击上传照片</span>
                            </div>
                        </div>
                        <input ref="certFileInput" type="file" accept="image/*" style="display:none" @change="onCertFileChange" />
                    </div>
                </div>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button class="btn-cancel" @click="showCertification = false">关闭</el-button>
                    <el-button v-if="!userInfo?.certified && userInfo?.certificationStatus !== 'pending'" type="primary" class="btn-primary" :loading="certSubmitting" @click="submitCertification">提交认证</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 修改密码弹窗 -->
        <el-dialog v-model="showChangePassword" title="修改密码" width="520px" :close-on-click-modal="false">
            <div class="dialog-form">
                <div class="form-field">
                    <label class="form-label">当前密码</label>
                    <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" class="form-input" />
                </div>
                <div class="form-field">
                    <label class="form-label">新密码</label>
                    <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" class="form-input" />
               </div>
                <div class="form-field">
                    <label class="form-label">确认新密码</label>
                    <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" class="form-input" />
                </div>
            </div>
            <template #footer>
                <div class="dialog-footer">
                    <el-button class="btn-cancel" @click="showChangePassword = false">取消</el-button>
                    <el-button type="primary" class="btn-primary" :loading="saving" @click="changePassword">确认修改</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 关于弹窗 -->
        <el-dialog v-model="showAbout" title="关于" width="460px">
            <div class="about-content">
                <div class="about-logo">
                    <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#4f46e5" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                        <circle cx="12" cy="7" r="4"/>
                    </svg>
                </div>
                <h3>校园失物招领系统</h3>
                <p class="about-version">版本 1.0.0</p>
                <p class="about-desc">帮助师生快速找回遗失物品，共建温暖校园。</p>
            </div>
            <template #footer>
                <el-button @click="showAbout = false">关闭</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { itemApi, messageApi, authApi } from '../api'

const router = useRouter()
const userStore = useUserStore()

const userInfo = ref(null)
const unreadCount = ref(0)
const showEditProfile = ref(false)
const showCertification = ref(false)
const showChangePassword = ref(false)
const showAbout = ref(false)
const showAvatarUpload = ref(false)
const saving = ref(false)
const certSubmitting = ref(false)
const certFileInput = ref(null)
const certImagePreview = ref('')
const certImageData = ref('')

const editForm = ref({
    nickname: '',
    email: '',
    phone: ''
})

const passwordForm = ref({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
})

const certForm = ref({
    real_name: '',
    student_id: '',
    campus: ''
})

const stats = ref({
    total: 0,
    found: 0,
    lost: 0,
    resolved: 0
})

// 信誉等级
const reputationLevel = computed(() => {
    const score = userInfo.value?.reputation ?? 100
    if (score >= 90) return 'level-high'
    if (score >= 70) return 'level-medium'
    return 'level-low'
})

const reputationDesc = computed(() => {
    const score = userInfo.value?.reputation ?? 100
    if (score >= 90) return '信誉极佳 · 优秀用户'
    if (score >= 70) return '信誉良好 · 继续加油'
    if (score >= 50) return '信誉一般 · 请注意行为'
    return '信誉较低 · 需要改善'
})

const certDesc = computed(() => {
    if (userInfo.value?.certified) return '已通过校园认证，身份可信'
    if (userInfo.value?.certificationStatus === 'pending') return '认证申请审核中，请耐心等待'
    return '未认证，点击进行校园身份认证'
})

function formatDate(dateStr) {
    if (!dateStr) return ''
    const d = new Date(dateStr)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function fetchStats() {
    try {
        const res = await itemApi.getUserStats()
        if (res.data) {
            stats.value.total = res.data.total || 0
            stats.value.found = res.data.found_count || 0
            stats.value.lost = res.data.lost_count || 0
            stats.value.resolved = res.data.completed_count || 0
        } else {
            const itemsRes = await itemApi.getUserItems()
            const items = itemsRes.data || []
            stats.value.total = items.length
            stats.value.found = items.filter(i => i.type === 1).length
            stats.value.lost = items.filter(i => i.type === 2).length
            stats.value.resolved = items.filter(i => i.status === 2).length
        }
    } catch (error) {
        console.error('获取统计数据失败:', error)
    }
}

async function fetchUnreadCount() {
    try {
        const res = await messageApi.getUnreadCount()
        unreadCount.value = res.data || 0
    } catch (error) {
        console.error('获取未读消息数失败:', error)
    }
}

async function saveProfile() {
    if (!editForm.value.nickname.trim()) {
        ElMessage.warning('昵称不能为空')
        return
    }
    saving.value = true
    try {
        await userStore.updateProfile({
            nickname: editForm.value.nickname,
            email: editForm.value.email,
            phone: editForm.value.phone
        })
        userInfo.value = userStore.userInfo
        ElMessage.success('资料更新成功')
        showEditProfile.value = false
    } catch (error) {
        console.error('更新资料失败:', error)
    } finally {
        saving.value = false
    }
}

async function changePassword() {
    const { oldPassword, newPassword, confirmPassword } = passwordForm.value
    if (!oldPassword || !newPassword || !confirmPassword) {
        ElMessage.warning('请填写完整信息')
        return
    }
    if (newPassword.length < 6) {
        ElMessage.warning('新密码长度不能少于6位')
        return
    }
    if (newPassword !== confirmPassword) {
        ElMessage.warning('两次输入的新密码不一致')
        return
    }
    saving.value = true
    try {
        await authApi.updateProfile({
            oldPassword,
            newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        showChangePassword.value = false
        passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
        userStore.logout()
        router.push('/login')
    } catch (error) {
        console.error('修改密码失败:', error)
    } finally {
        saving.value = false
    }
}

function triggerCertUpload() {
    certFileInput.value?.click()
}

function onCertFileChange(e) {
    const file = e.target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = (ev) => {
        certImagePreview.value = ev.target.result
        certImageData.value = ev.target.result
    }
    reader.readAsDataURL(file)
}

async function submitCertification() {
    if (!certForm.value.real_name.trim()) {
        ElMessage.warning('请输入真实姓名')
        return
    }
    if (!certForm.value.student_id.trim()) {
        ElMessage.warning('请输入学号/工号')
        return
    }
    if (!certImageData.value) {
        ElMessage.warning('请上传学生证或校园卡照片')
        return
    }
    certSubmitting.value = true
    try {
        const res = await authApi.submitCertification({
            real_name: certForm.value.real_name,
            student_id: certForm.value.student_id,
            campus: certForm.value.campus || '',
            id_card: certImageData.value
        })
        if (res.code === 200) {
            ElMessage.success('认证申请已提交，请等待管理员审核')
            showCertification.value = false
            if (res.data) {
                // 更新本地和 store 中的用户信息
                userInfo.value = { ...userInfo.value, ...res.data }
                userStore.userInfo = { ...userStore.userInfo, ...res.data }
                localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
            }
        }
    } catch (error) {
        console.error('提交认证失败:', error)
    } finally {
        certSubmitting.value = false
    }
}

async function handleLogout() {
    try {
        await ElMessageBox.confirm('确定要退出登录吗？', '确认退出', {
            confirmButtonText: '退出',
            cancelButtonText: '取消',
            type: 'warning'
        })
        userStore.logout()
        ElMessage.success('已退出登录')
        router.push('/')
    } catch (error) {
        if (error !== 'cancel') {
            console.error('退出失败:', error)
        }
    }
}

onMounted(() => {
    if (!userStore.isLoggedIn) {
        router.push('/login')
        return
    }
    userInfo.value = userStore.userInfo
    editForm.value = {
        nickname: userInfo.value?.nickname || userInfo.value?.username || '',
        email: userInfo.value?.email || '',
        phone: userInfo.value?.phone || ''
    }
    fetchStats()
    fetchUnreadCount()
})
</script>

<style scoped>
/* ========== 暗黑模式 ========== */
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

html.dark .about-content h3 {
    color: #e0e0f0;
}

html.dark .about-version {
    color: #6a6a8a;
}

html.dark .about-desc {
    color: #8a8aa0;
}

html.dark .form-label {
    color: #c8c8d8 !important;
}

html.dark .reputation-section {
    border-bottom-color: #2a2a3e;
}

html.dark .reputation-label {
    color: #8a8aa0;
}

html.dark .score-max {
    color: #6a6a8a;
}

html.dark .reputation-desc {
    color: #6a6a8a;
}

html.dark .cert-badge {
    background: rgba(16, 185, 129, 0.15);
    color: #34d399;
}

html.dark .cert-pending {
    background: rgba(245, 158, 11, 0.15);
    color: #fbbf24;
}

html.dark :deep(.el-dialog) {
    background: #1a1a2e !important;
}

html.dark :deep(.el-dialog__title) {
    color: #e0e0f0 !important;
}

html.dark :deep(.el-dialog__body) {
    color: #c8c8d8;
}

html.dark :deep(.el-dialog .el-input__inner) {
    background: #22223a;
    border-color: #2a2a3e;
    color: #c8c8d8;
}

html.dark :deep(.el-dialog .el-input__inner:focus) {
    border-color: #6366f1;
}

/* ========== 个人中心 ========== */
.profile-page {
    min-height: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 40px 0 60px;
    display: flex;
    flex-direction: column;
}

/* 顶部：头像 + 信息 + 统计数据 - 与背景融为一体，无卡片 */
.profile-header {
    display: flex;
    align-items: center;
    gap: 32px;
    width: 100%;
    padding: 0 0 24px;
    border-bottom: 1px solid #e2e8f0;
}

.profile-avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, #4f46e5, #6366f1);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
    position: relative;
    cursor: pointer;
    overflow: hidden;
}

.profile-avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s;
}

.profile-avatar:hover .avatar-overlay {
    opacity: 1;
}

.profile-info {
    flex: 1;
    min-width: 0;
}

.profile-name-row {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 4px;
}

.profile-name {
    font-size: 22px;
    font-weight: 700;
    color: #4a4a6a;
    margin: 0;
}

.cert-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    font-weight: 600;
    color: #059669;
    background: rgba(16, 185, 129, 0.1);
    padding: 2px 8px;
    border-radius: 10px;
    white-space: nowrap;
}

.cert-pending {
    color: #d97706;
    background: rgba(245, 158, 11, 0.1);
}

.profile-email {
    font-size: 14px;
    color: #718096;
    margin-bottom: 2px;
}

.profile-join-date {
    font-size: 12px;
    color: #a0aec0;
}

/* 统计数据 */
.profile-stats {
    display: grid;
    grid-template-columns: repeat(4, auto);
    gap: 28px;
    flex-shrink: 0;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}

.stat-value {
    font-size: 24px;
    font-weight: 700;
    color: #4f46e5;
}

.stat-label {
    font-size: 13px;
    color: #a0aec0;
}

/* 信誉积分区域 - 与背景融为一体 */
.reputation-section {
    width: 100%;
    padding: 20px 0 24px;
    border-bottom: 1px solid #e2e8f0;
}

.reputation-bar {
    display: flex;
    align-items: center;
    gap: 16px;
}

.reputation-label {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
    color: #718096;
    white-space: nowrap;
}

.reputation-score {
    display: flex;
    align-items: baseline;
    gap: 2px;
}

.score-value {
    font-size: 20px;
    font-weight: 700;
}

.score-value.level-high {
    color: #10b981;
}

.score-value.level-medium {
    color: #f59e0b;
}

.score-value.level-low {
    color: #ef4444;
}

.score-max {
    font-size: 13px;
    color: #a0aec0;
}

.reputation-track {
    flex: 1;
    height: 6px;
    background: #e2e8f0;
    border-radius: 3px;
    overflow: hidden;
    max-width: 200px;
}

.reputation-fill {
    height: 100%;
    border-radius: 3px;
    transition: width 0.5s ease;
}

.reputation-fill.level-high {
    background: linear-gradient(90deg, #10b981, #34d399);
}

.reputation-fill.level-medium {
    background: linear-gradient(90deg, #f59e0b, #fbbf24);
}

.reputation-fill.level-low {
    background: linear-gradient(90deg, #ef4444, #f87171);
}

.reputation-desc {
    font-size: 12px;
    color: #a0aec0;
    white-space: nowrap;
}

/* 设置区域 - 与背景融为一体 */
.settings-section {
    width: 100%;
    padding: 24px 0;
}

.settings-group-label {
    font-size: 12px;
    font-weight: 600;
    color: #a0aec0;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    padding: 12px 0 8px;
}

.settings-item {
    display: flex;
    align-items: center;
    gap: 14px;
    width: 100%;
    padding: 14px 0;
    border: none;
    border-bottom: 1px solid #e2e8f0;
    background: none;
    cursor: pointer;
    text-decoration: none;
    transition: opacity 0.2s;
}

.settings-item:hover {
    opacity: 0.8;
}

.settings-item svg:first-child {
    color: #4f46e5;
    flex-shrink: 0;
}

.settings-text {
    flex: 1;
    min-width: 0;
    text-align: left;
}

.settings-title {
    display: block;
    font-size: 15px;
    font-weight: 600;
    color: #4a4a6a;
    margin-bottom: 2px;
}

.settings-desc {
    display: block;
    font-size: 12px;
    color: #a0aec0;
}

.settings-arrow {
    color: #cbd5e0;
    flex-shrink: 0;
}

.unread-badge {
    background: #ef4444;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    padding: 1px 6px;
    border-radius: 8px;
    min-width: 18px;
    text-align: center;
    flex-shrink: 0;
}

.settings-item--danger .settings-title {
    color: #c0392b !important;
}

.settings-item--danger svg:first-child {
    color: #c0392b !important;
}

.settings-item--danger .settings-arrow {
    color: #c0392b !important;
}

/* 弹窗样式 */
.dialog-form {
    padding: 8px 0;
}

.form-field {
    margin-bottom: 18px;
}

.form-label {
    display: block;
    font-size: 13px;
    font-weight: 600;
    color: #4a4a6a;
    margin-bottom: 6px;
}

.form-input {
    width: 100%;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.btn-cancel {
    background: #f3f4f6;
    border: 1px solid #e5e7eb;
    color: #6b7280;
}

.btn-primary {
    background: #4f46e5;
    border-color: #4f46e5;
}

/* 认证状态 */
.cert-status-info {
    margin-bottom: 20px;
}

.cert-status {
    text-align: center;
    padding: 24px;
    border-radius: 12px;
}

.cert-status h3 {
    font-size: 16px;
    font-weight: 600;
    margin: 12px 0 6px;
}

.cert-status p {
    font-size: 13px;
    color: #718096;
    margin: 0;
}

.cert-approved {
    background: rgba(16, 185, 129, 0.05);
}

.cert-approved h3 {
    color: #059669;
}

.cert-pending-status {
    background: rgba(245, 158, 11, 0.05);
}

.cert-pending-status h3 {
    color: #d97706;
}

.cert-none {
    background: rgba(107, 114, 128, 0.05);
}

.cert-none h3 {
    color: #6b7280;
}

.upload-area {
    border: 2px dashed #d1d5db;
    border-radius: 12px;
    padding: 24px;
    text-align: center;
    cursor: pointer;
    transition: border-color 0.2s;
}

.upload-area:hover {
    border-color: #4f46e5;
}

.upload-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #9ca3af;
    font-size: 14px;
}

.upload-preview {
    max-height: 200px;
    border-radius: 8px;
}

/* 关于弹窗 */
.about-content {
    text-align: center;
    padding: 16px 0;
}

.about-logo {
    margin-bottom: 12px;
}

.about-content h3 {
    font-size: 18px;
    font-weight: 700;
    color: #4a4a6a;
    margin: 0 0 4px;
}

.about-version {
    font-size: 13px;
    color: #a0aec0;
    margin: 0 0 8px;
}

.about-desc {
    font-size: 14px;
    color: #718096;
    margin: 0;
}
</style>
