import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
    const isDark = ref(localStorage.getItem('darkMode') === 'true')

    // 初始化时应用
    function init() {
        document.documentElement.classList.toggle('dark', isDark.value)
    }

    function toggle() {
        isDark.value = !isDark.value
        localStorage.setItem('darkMode', isDark.value)
        document.documentElement.classList.toggle('dark', isDark.value)
    }

    function setDark(val) {
        isDark.value = val
        localStorage.setItem('darkMode', val)
        document.documentElement.classList.toggle('dark', val)
    }

    return {
        isDark,
        init,
        toggle,
        setDark
    }
})