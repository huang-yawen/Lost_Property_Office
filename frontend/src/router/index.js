import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '校园失物招领' }
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { title: '登录', hideLayout: true }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: { title: '注册', hideLayout: true }
    },
    {
        path: '/create',
        name: 'PublishForm',
        component: () => import('../views/PublishForm.vue'),
        meta: { title: '发布信息', requiresAuth: true }
    },
    {
        path: '/edit/:id',
        name: 'ItemEdit',
        component: () => import('../views/PublishForm.vue'),
        meta: { title: '编辑信息', requiresAuth: true }
    },
    {
        path: '/user',
        name: 'UserProfile',
        component: () => import('../views/UserProfile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
    },
    {
        path: '/my-items',
        name: 'UserItems',
        component: () => import('../views/UserItems.vue'),
        meta: { title: '我的发布', requiresAuth: true }
    },
    {
        path: '/messages',
        name: 'Messages',
        component: () => import('../views/UserMessages.vue'),
        meta: { title: '我的消息', requiresAuth: true }
    },
    {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '数据看板' }
    },
    {
        path: '/admin/login',
        name: 'AdminLogin',
        redirect: '/login',
        meta: { title: '登录', hideLayout: true }
    },
    {
        path: '/admin/certification',
        name: 'AdminCertification',
        component: () => import('../views/AdminCertification.vue'),
        meta: { title: '认证审核', requiresAdmin: true }
    },
    {
        path: '/admin/users',
        name: 'AdminUsers',
        component: () => import('../views/AdminUsers.vue'),
        meta: { title: '用户列表', requiresAdmin: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = to.meta.title || '校园失物招领'

    // 检查是否需要管理员登录（必须有 admin_token 且不能有普通用户 token）
    if (to.meta.requiresAdmin) {
        const adminToken = localStorage.getItem('admin_token')
        const userToken = localStorage.getItem('token')
        if (!adminToken || userToken) {
            next({ name: 'Login', query: { redirect: to.fullPath } })
            return
        }
    }

    // 检查是否需要用户登录
    if (to.meta.requiresAuth) {
        const userStore = useUserStore()
        if (!userStore.isLoggedIn) {
            next({ name: 'Login', query: { redirect: to.fullPath } })
            return
        }
    }

    next()
})

export default router