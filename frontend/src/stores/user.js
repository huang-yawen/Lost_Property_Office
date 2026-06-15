import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

    const isLoggedIn = computed(() => !!token.value)

    async function login(credentials) {
        const res = await authApi.login(credentials)
        token.value = res.data.token
        userInfo.value = res.data.user
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify(res.data.user))
        return res
    }

    async function register(data) {
        const res = await authApi.register(data)
        return res
    }

    function logout() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
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
        isLoggedIn,
        login,
        register,
        logout,
        updateProfile
    }
})