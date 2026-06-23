<template>
    <div class="auth-page">
        <router-link to="/" class="auth-logo">
            <svg viewBox="0 0 24 24" width="32" height="32" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8"/>
                <path d="M12 18V6"/>
            </svg>
            <span>校园失物招领</span>
        </router-link>
        <div class="auth-container">
            <div class="auth-card">
                <div class="auth-header">
                    <h1 class="auth-title">登录</h1>
                    <p class="auth-desc">使用您的账号登录系统</p>
                </div>

                <form class="auth-form" @submit.prevent="handleLogin">
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
                                placeholder="请输入密码"
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
                            登录中...
                        </span>
                        <span v-else>登录</span>
                    </button>
                </form>

                <div class="auth-footer">
                    <span>还没有账号？</span>
                    <router-link to="/register" class="auth-link">立即注册</router-link>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { adminApi } from '../api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const showPassword = ref(false)

const form = reactive({
    username: '',
    password: ''
})

async function handleLogin() {
    if (!form.username.trim() || !form.password.trim()) {
        ElMessage.warning('请填写用户名和密码')
        return
    }

    loading.value = true
    try {
        // 先尝试管理员登录
        const adminRes = await adminApi.login({
            username: form.username.trim(),
            password: form.password
        })
        // 管理员登录成功
        // 清除普通用户 token，确保互斥
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        // 保存管理员 token
        localStorage.setItem('admin_token', adminRes.data.token)
        localStorage.setItem('admin_info', JSON.stringify(adminRes.data.user))
        ElMessage.success('管理员登录成功')
        const redirect = route.query.redirect || '/admin/certification'
        router.push(redirect)
    } catch (adminError) {
        // 管理员登录失败，尝试普通用户登录
        try {
            // 清除管理员 token，确保互斥
            localStorage.removeItem('admin_token')
            localStorage.removeItem('admin_info')
            await userStore.login({
                username: form.username.trim(),
                password: form.password
            })
            ElMessage.success('登录成功')
            const redirect = route.query.redirect || '/'
            router.push(redirect)
        } catch (userError) {
            // 两种登录都失败
            ElMessage.error('用户名或密码错误')
        }
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
/* ========== 暗黑模式 ========== */
html.dark .auth-card {
    background: #1a1a2e;
}

html.dark .auth-title {
    color: #e0e0f0;
}

html.dark .auth-desc {
    color: #8a8aa0;
}

html.dark .form-label {
    color: #c8c8d8;
}

html.dark .input-wrapper {
    background: #22223a;
    border-color: #2a2a3e;
}

html.dark .input-wrapper:focus-within {
    border-color: #6366f1;
    background: #1a1a2e;
    box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

html.dark .input-icon {
    color: #6a6a8a;
}

html.dark .form-input {
    color: #c8c8d8;
}

html.dark .form-input::placeholder {
    color: #6a6a8a;
}

html.dark .toggle-password {
    color: #6a6a8a;
}

html.dark .toggle-password:hover {
    color: #a5b4fc;
}

html.dark .auth-footer {
    color: #8a8aa0;
}

html.dark .auth-link {
    color: #a5b4fc;
}

html.dark .auth-link:hover {
    color: #818cf8;
}

/* ========== 登录页 ========== */
.auth-page {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #1a1a2e 0%, #2d2d4a 50%, #4a4a6a 100%);
    padding: 24px;
    overflow: hidden;
}

.auth-container {
    width: 100%;
    max-width: 420px;
}

.auth-card {
    background: #fff;
    border-radius: 8px;
    padding: 40px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.auth-header {
    text-align: center;
    margin-bottom: 32px;
}

.auth-logo {
    position: fixed;
    top: 32px;
    left: 32px;
    display: inline-flex;
    align-items: center;
    gap: 12px;
    color: #fff;
    font-size: 22px;
    font-weight: 800;
    text-decoration: none;
    letter-spacing: 2px;
    z-index: 10;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    font-style: italic;
    transition: opacity 0.3s;
}

.auth-logo:hover {
    opacity: 0.85;
}

.auth-logo svg {
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.auth-title {
    font-size: 24px;
    font-weight: 700;
    color: #4a4a6a;
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
    color: #4a4a6a;
}

.input-wrapper {
    display: flex;
    align-items: center;
    background: #fafaff;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    padding: 0 14px;
    transition: all 0.25s;
}

.input-wrapper:focus-within {
    border-color: #4f46e5;
    background: #fff;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
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
    color: #4a4a6a;
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
    background: linear-gradient(135deg, #4f46e5, #6366f1);
    color: #fff;
    border: none;
    border-radius: 6px;
    font-size: 15px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.25s;
    margin-top: 8px;
}

.btn-submit:hover:not(:disabled) {
    background: linear-gradient(135deg, #4338ca, #4f46e5);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
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
    color: #4f46e5;
    font-weight: 600;
    margin-left: 4px;
    transition: color 0.2s;
}

.auth-link:hover {
    color: #4338ca;
}
</style>