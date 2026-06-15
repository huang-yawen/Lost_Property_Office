<template>
    <div class="auth-page">
        <div class="auth-container">
            <div class="auth-card">
                <div class="auth-header">
                    <router-link to="/" class="auth-logo">
                        <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="12" cy="12" r="10"/>
                            <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8"/>
                            <path d="M12 18V6"/>
                        </svg>
                        <span>校园失物招领</span>
                    </router-link>
                    <h1 class="auth-title">创建账号</h1>
                    <p class="auth-desc">注册后即可发布招领和寻物信息</p>
                </div>

                <form class="auth-form" @submit.prevent="handleRegister">
                    <div class="form-group">
                        <label class="form-label">用户名</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                                <circle cx="12" cy="7" r="4"/>
                            </svg>
                            <input
                                v-model="form.username"
                                type="text"
                                class="form-input"
                                placeholder="请输入用户名"
                                required
                            />
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label">邮箱</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                                <polyline points="22,6 12,13 2,6"/>
                            </svg>
                            <input
                                v-model="form.email"
                                type="email"
                                class="form-input"
                                placeholder="请输入邮箱（选填）"
                            />
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label">手机号</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="5" y="2" width="14" height="20" rx="2" ry="2"/>
                                <line x1="12" y1="18" x2="12.01" y2="18"/>
                            </svg>
                            <input
                                v-model="form.phone"
                                type="tel"
                                class="form-input"
                                placeholder="请输入手机号（选填）"
                            />
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label">密码</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                            </svg>
                            <input
                                v-model="form.password"
                                :type="showPassword ? 'text' : 'password'"
                                class="form-input"
                                placeholder="请输入密码（至少6位）"
                                required
                            />
                            <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                                <svg v-if="!showPassword" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                                    <circle cx="12" cy="12" r="3"/>
                                </svg>
                                <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                                    <line x1="1" y1="1" x2="23" y2="23"/>
                                </svg>
                            </button>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="form-label">确认密码</label>
                        <div class="input-wrapper">
                            <svg class="input-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                            </svg>
                            <input
                                v-model="form.confirmPassword"
                                :type="showConfirmPassword ? 'text' : 'password'"
                                class="form-input"
                                placeholder="请再次输入密码"
                                required
                            />
                            <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword">
                                <svg v-if="!showConfirmPassword" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                                    <circle cx="12" cy="12" r="3"/>
                                </svg>
                                <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                                    <line x1="1" y1="1" x2="23" y2="23"/>
                                </svg>
                            </button>
                        </div>
                    </div>

                    <button type="submit" class="btn-submit" :disabled="loading">
                        <span v-if="loading" class="btn-loading">
                            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="spin">
                                <line x1="12" y1="2" x2="12" y2="6"/>
                                <line x1="12" y1="18" x2="12" y2="22"/>
                                <line x1="4.93" y1="4.93" x2="7.76" y2="7.76"/>
                                <line x1="16.24" y1="16.24" x2="19.07" y2="19.07"/>
                                <line x1="2" y1="12" x2="6" y2="12"/>
                                <line x1="18" y1="12" x2="22" y2="12"/>
                                <line x1="4.93" y1="19.07" x2="7.76" y2="16.24"/>
                                <line x1="16.24" y1="7.76" x2="19.07" y2="4.93"/>
                            </svg>
                            注册中...
                        </span>
                        <span v-else>注册</span>
                    </button>
                </form>

                <div class="auth-footer">
                    <span>已有账号？</span>
                    <router-link to="/login" class="auth-link">立即登录</router-link>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const form = reactive({
    username: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: ''
})

async function handleRegister() {
    if (!form.username.trim()) {
        ElMessage.warning('请输入用户名')
        return
    }
    if (form.password.length < 6) {
        ElMessage.warning('密码至少需要6位')
        return
    }
    if (form.password !== form.confirmPassword) {
        ElMessage.warning('两次输入的密码不一致')
        return
    }

    loading.value = true
    try {
        await userStore.register({
            username: form.username.trim(),
            email: form.email.trim() || undefined,
            phone: form.phone.trim() || undefined,
            password: form.password
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
    } catch (error) {
        console.error('注册失败:', error)
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.auth-page {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #1a3a5c 0%, #2c5f8a 50%, #4a7fb5 100%);
    padding: 24px;
}

.auth-container {
    width: 100%;
    max-width: 420px;
}

.auth-card {
    background: #fff;
    border-radius: 20px;
    padding: 40px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.auth-header {
    text-align: center;
    margin-bottom: 32px;
}

.auth-logo {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    color: #1a3a5c;
    font-size: 16px;
    font-weight: 700;
    margin-bottom: 24px;
    text-decoration: none;
}

.auth-title {
    font-size: 24px;
    font-weight: 700;
    color: #2c3e50;
    margin-bottom: 8px;
}

.auth-desc {
    font-size: 14px;
    color: #718096;
}

.auth-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.form-label {
    font-size: 13px;
    font-weight: 600;
    color: #2c3e50;
}

.input-wrapper {
    display: flex;
    align-items: center;
    background: #f7fafc;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    padding: 0 14px;
    transition: all 0.25s;
}

.input-wrapper:focus-within {
    border-color: #1a3a5c;
    background: #fff;
    box-shadow: 0 0 0 3px rgba(26, 58, 92, 0.1);
}

.input-icon {
    color: #a0aec0;
    flex-shrink: 0;
}

.form-input {
    flex: 1;
    border: none;
    outline: none;
    padding: 12px 10px;
    font-size: 14px;
    color: #2c3e50;
    background: transparent;
}

.form-input::placeholder {
    color: #a0aec0;
}

.toggle-password {
    background: none;
    border: none;
    padding: 8px;
    cursor: pointer;
    color: #a0aec0;
    display: flex;
    align-items: center;
    transition: color 0.2s;
}

.toggle-password:hover {
    color: #4a5568;
}

.btn-submit {
    width: 100%;
    padding: 14px;
    background: linear-gradient(135deg, #1a3a5c, #2c5f8a);
    color: #fff;
    border: none;
    border-radius: 10px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s;
    margin-top: 8px;
}

.btn-submit:hover:not(:disabled) {
    background: linear-gradient(135deg, #2c5f8a, #4a7fb5);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(26, 58, 92, 0.3);
}

.btn-submit:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.btn-loading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.spin {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

.auth-footer {
    text-align: center;
    margin-top: 24px;
    font-size: 14px;
    color: #718096;
}

.auth-link {
    color: #1a3a5c;
    font-weight: 600;
    margin-left: 4px;
    transition: color 0.2s;
}

.auth-link:hover {
    color: #2c5f8a;
}
</style>