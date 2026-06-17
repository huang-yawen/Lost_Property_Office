<template>
    <div class="redirect-page">
        <div class="redirect-content">
            <p>管理端登录已合并到统一登录页面...</p>
            <router-link to="/login" class="redirect-link">前往登录</router-link>
        </div>
    </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
    router.replace('/login')
})
</script>

<style scoped>
.redirect-page {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #1a1a2e 0%, #2d2d4a 50%, #4a4a6a 100%);
}

.redirect-content {
    text-align: center;
    color: #fff;
}

.redirect-link {
    color: #a5b4fc;
    font-size: 16px;
}
</style>