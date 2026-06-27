import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code === 200) {
            return res
        }
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
    },
    error => {
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    localStorage.removeItem('token')
                    localStorage.removeItem('userInfo')
                    ElMessage.error('登录已过期，请重新登录')
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('没有权限访问')
                    break
                case 404:
                    ElMessage.error('请求的资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error(error.response.data?.message || '请求失败')
            }
        } else if (error.request) {
            ElMessage.error('网络错误，请检查网络连接')
        } else {
            ElMessage.error('请求配置错误')
        }
        return Promise.reject(error)
    }
)

// ============ 认证相关 API ============
export const authApi = {
    login(data) {
        return request.post('/auth/login', data)
    },
    register(data) {
        return request.post('/auth/register', data)
    },
    getProfile() {
        return request.get('/auth/profile')
    },
    updateProfile(data) {
        return request.put('/auth/profile', data)
    },
    // 提交校园认证
    submitCertification(data) {
        return request.post('/auth/certification', data)
    },
    // 获取认证申请列表（管理员）
    getCertificationList(params) {
        return request.get('/auth/certification/list', { params })
    },
    // 审核认证（管理员）
    reviewCertification(data) {
        return request.post('/auth/certification/review', data)
    },
    // 更新信誉积分
    updateReputation(data) {
        return request.post('/auth/reputation', data)
    },
    // 获取用户公开信息
    getUsers(params) {
        return request.get('/auth/users', { params })
    },
    // 获取热心值排行榜（公开）
    getReputationRank(params) {
        return request.get('/auth/reputation/rank', { params })
    }
}

// ============ 物品相关 API ============
export const itemApi = {
    // 获取物品列表（公开）
    getList(params) {
        return request.get('/items', { params })
    },
    // 获取物品详情
    getDetail(id) {
        return request.get(`/items/${id}`)
    },
    // 创建物品
    create(data) {
        return request.post('/items', data)
    },
    // 更新物品
    update(id, data) {
        return request.put(`/items/${id}`, data)
    },
    // 删除/下架物品
    delete(id) {
        return request.delete(`/items/${id}`)
    },
    // 更新物品状态
    updateStatus(id, status) {
        return request.put(`/items/${id}/status`, { status })
    },
    // 获取用户自己的物品列表
    getUserItems(params) {
        return request.get('/items/user', { params })
    },
    // 获取用户统计
    getUserStats() {
        return request.get('/items/user/stats')
    },
    // 获取全局看板统计（公开）
    getDashboardStats() {
        return request.get('/items/dashboard/stats')
    },
    // 搜索物品
    search(params) {
        return request.get('/items/search', { params })
    },
    // 智能匹配推荐
    smartMatch(id) {
        return request.get(`/items/match/${id}`)
    }
}

// ============ 分类相关 API ============
export const categoryApi = {
    getList() {
        return request.get('/categories')
    }
}

// ============ 留言相关 API ============
export const messageApi = {
    // 获取物品的留言列表
    getList(itemId) {
        return request.get(`/messages/${itemId}`)
    },
    // 发送留言
    send(data) {
        return request.post('/messages', data)
    },
    // 获取用户收到的消息列表
    getMyMessages(params) {
        return request.get('/messages/user/received', { params })
    },
    // 获取用户发送的消息列表
    getSentMessages(params) {
        return request.get('/messages/user/sent', { params })
    },
    // 获取用户的未读消息数
    getUnreadCount() {
        return request.get('/messages/unread/count')
    },
    // 标记消息已读
    markRead(id) {
        return request.put(`/messages/${id}/read`)
    }
}

// ============ 文件上传 API ============
export const uploadApi = {
    upload(file) {
        const formData = new FormData()
        formData.append('file', file)
        return request.post('/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
    }
}

// ============ 管理端 API ============
// 创建 admin 专用的 axios 实例（使用 admin_token）
const adminRequest = axios.create({
    baseURL: '/api',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

adminRequest.interceptors.request.use(
    config => {
        const token = localStorage.getItem('admin_token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

adminRequest.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code === 200) {
            return res
        }
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
    },
    error => {
        if (error.response) {
            switch (error.response.status) {
                case 401:
                    localStorage.removeItem('admin_token')
                    localStorage.removeItem('admin_info')
                    ElMessage.error('管理员登录已过期，请重新登录')
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('没有权限访问')
                    break
                case 404:
                    ElMessage.error('请求的资源不存在')
                    break
                case 500:
                    ElMessage.error('服务器错误')
                    break
                default:
                    ElMessage.error(error.response.data?.message || '请求失败')
            }
        } else if (error.request) {
            ElMessage.error('网络错误，请检查网络连接')
        } else {
            ElMessage.error('请求配置错误')
        }
        return Promise.reject(error)
    }
)

export const matchApi = {
    smartMatch(id) {
        return request.get(`/items/match/${id}`)
    }
}

export const adminApi = {
    login(data) {
        return adminRequest.post('/admin/login', data)
    },
    getCertificationList(params) {
        return adminRequest.get('/admin/certifications', { params })
    },
    reviewCertification(data) {
        return adminRequest.put(`/admin/certifications/${data.userId}`, data)
    },
    getUsers() {
        return adminRequest.get('/admin/users')
    },
    deleteUser(userId) {
        return adminRequest.delete(`/admin/users/${userId}`)
    }
}

export default request
