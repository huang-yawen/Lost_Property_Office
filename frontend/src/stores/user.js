import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
    // 管理员状态（使用 reactive ref 确保 Vue 能检测变化）
    const adminToken = ref(localStorage.getItem('admin_token') || '')
    const adminInfo = ref(JSON.parse(localStorage.getItem('admin_info') || 'null'))

    const isLoggedIn = computed(() => !!token.value || !!adminToken.value)

    // 是否处于管理员模式
    const isAdmin = computed(() => !!adminToken.value && !token.value)

    async function login(credentials) {
        const res = await authApi.login(credentials)
        token.value = res.data.token
        userInfo.value = res.data.user
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify(res.data.user))
        // 清除管理员 token，确保互斥
        adminToken.value = ''
        adminInfo.value = null
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_info')
        return res
    }

    // 直接从登录结果设置用户状态（无需重复请求）
    function setLoginState(data) {
        token.value = data.token
        userInfo.value = data.user
        localStorage.setItem('token', data.token)
        localStorage.setItem('userInfo', JSON.stringify(data.user))
        adminToken.value = ''
        adminInfo.value = null
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_info')
    }

    // 管理员登录
    function setAdminState(tokenVal, userVal) {
        // 清除普通用户状态
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        // 设置管理员状态
        adminToken.value = tokenVal
        adminInfo.value = userVal
        localStorage.setItem('admin_token', tokenVal)
        localStorage.setItem('admin_info', JSON.stringify(userVal))
    }

    async function register(data) {
        const res = await authApi.register(data)
        return res
    }

    function logout() {
        token.value = ''
        userInfo.value = null
        adminToken.value = ''
        adminInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_info')
    }

    async function updateProfile(data) {
        const res = await authApi.updateProfile(data)
        userInfo.value = res.data
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        return res
    }

    return {
        token,
        userInfo,
        adminToken,
        adminInfo,
        isLoggedIn,
        isAdmin,
        login,
        setLoginState,
        setAdminState,
        register,
        logout,
        updateProfile
    }
})
