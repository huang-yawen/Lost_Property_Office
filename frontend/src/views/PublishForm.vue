<template>
    <div class="publish-form-page">
        <div class="pf-header">
            <button class="pf-back" @click="goBack">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="19" y1="12" x2="5" y2="12"/>
                    <polyline points="12 19 5 12 12 5"/>
                </svg>
                返回
            </button>
            <h1 class="pf-title">{{ isEdit ? '编辑信息' : typeLabel }}</h1>
        </div>

        <div class="pf-body">
            <form class="pf-form" @submit.prevent="handleSubmit">
                <!-- 物品分类 -->
                <div class="pf-field">
                    <label class="pf-label">物品分类</label>
                    <el-select v-model="form.categoryId" placeholder="请选择物品分类" class="pf-select">
                        <el-option
                            v-for="cat in categories"
                            :key="cat.id"
                            :label="cat.name"
                            :value="cat.id"
                        />
                    </el-select>
                </div>

                <!-- 物品名称 -->
                <div class="pf-field">
                    <label class="pf-label">物品名称</label>
                    <input
                        v-model="form.title"
                        type="text"
                        class="pf-input"
                        placeholder="请输入物品名称"
                        maxlength="50"
                        required
                    />
                </div>

                <!-- 物品描述 -->
                <div class="pf-field">
                    <label class="pf-label">物品描述</label>
                    <textarea
                        v-model="form.description"
                        class="pf-textarea"
                        placeholder="请详细描述物品的特征、颜色、品牌等信息..."
                        rows="4"
                        maxlength="500"
                    ></textarea>
                    <span class="pf-count">{{ form.description.length }}/500</span>
                </div>

                <!-- 地点 -->
                <div class="pf-field">
                    <label class="pf-label">地点</label>
                    <input
                        v-model="form.location"
                        type="text"
                        class="pf-input"
                        placeholder="例如：图书馆三楼、食堂二楼"
                    />
                </div>

                <!-- 联系方式 -->
                <div class="pf-field">
                    <label class="pf-label">联系方式</label>
                    <input
                        v-model="form.contact"
                        type="text"
                        class="pf-input"
                        placeholder="手机号、QQ、微信等"
                        required
                    />
                </div>

                <!-- 图片上传 -->
                <div class="pf-field">
                    <label class="pf-label">上传图片</label>
                    <div class="pf-upload">
                        <div class="pf-upload-grid">
                            <div
                                v-for="(img, index) in imageList"
                                :key="index"
                                class="pf-upload-preview"
                            >
                                <img :src="img" alt="preview" />
                                <button type="button" class="pf-upload-remove" @click="removeImage(index)">
                                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                        <line x1="18" y1="6" x2="6" y2="18"/>
                                        <line x1="6" y1="6" x2="18" y2="18"/>
                                    </svg>
                                </button>
                            </div>
                            <div
                                v-if="imageList.length < 6"
                                class="pf-upload-btn"
                                @click="triggerUpload"
                            >
                                <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                                    <line x1="12" y1="8" x2="12" y2="16"/>
                                    <line x1="8" y1="12" x2="16" y2="12"/>
                                </svg>
                                <span>上传图片</span>
                            </div>
                        </div>
                        <input
                            ref="fileInput"
                            type="file"
                            accept="image/*"
                            multiple
                            style="display: none"
                            @change="handleFileChange"
                        />
                        <p class="pf-upload-tip">支持 JPG、PNG 格式，最多上传 6 张</p>
                    </div>
                </div>

                <!-- 提交按钮 -->
                <div class="pf-actions">
                    <button type="submit" class="pf-submit" :disabled="submitting">
                        <span v-if="submitting" class="pf-loading">
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
                            发布中...
                        </span>
                        <span v-else>{{ isEdit ? '保存修改' : '发布信息' }}</span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { itemApi, categoryApi, uploadApi } from '../api'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const submitting = ref(false)
const categories = ref([])
const fileInput = ref(null)
const imageList = ref([])
const uploadedUrls = ref([])

const typeLabel = computed(() => {
    return form.type === 1 ? '发布招领信息' : '发布寻物信息'
})

const form = reactive({
    type: 1,
    categoryId: null,
    title: '',
    description: '',
    location: '',
    contact: '',
    images: ''
})

async function fetchCategories() {
    try {
        const res = await categoryApi.getList()
        categories.value = res.data || []
    } catch (error) {
        console.error('获取分类失败:', error)
    }
}

async function fetchItemDetail() {
    if (!route.params.id) return
    try {
        const res = await itemApi.getDetail(route.params.id)
        const item = res.data
        form.type = item.type
        form.categoryId = item.categoryId
        form.title = item.title
        form.description = item.description || ''
        form.location = item.location || ''
        form.contact = item.contact || ''
        form.images = item.images || ''

        if (item.images) {
            try {
                const urls = JSON.parse(item.images)
                imageList.value = urls
                uploadedUrls.value = urls
            } catch {
                imageList.value = []
            }
        }
    } catch (error) {
        console.error('获取物品详情失败:', error)
        ElMessage.error('获取物品信息失败')
        router.push('/')
    }
}

function goBack() {
    if (isEdit.value) {
        router.push(`/item/${route.params.id}`)
    } else {
        router.push('/create')
    }
}

function triggerUpload() {
    fileInput.value?.click()
}

async function handleFileChange(e) {
    const files = e.target.files
    if (!files.length) return

    const remaining = 6 - imageList.value.length
    const toUpload = Array.from(files).slice(0, remaining)

    for (const file of toUpload) {
        if (file.size > 5 * 1024 * 1024) {
            ElMessage.warning(`图片 ${file.name} 超过5MB限制`)
            continue
        }
        if (!['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)) {
            ElMessage.warning(`图片 ${file.name} 格式不支持`)
            continue
        }

        try {
            const res = await uploadApi.upload(file)
            const url = res.data
            uploadedUrls.value.push(url)
            imageList.value.push(url)
        } catch (error) {
            console.error('上传图片失败:', error)
            ElMessage.error(`图片 ${file.name} 上传失败`)
        }
    }

    if (fileInput.value) {
        fileInput.value.value = ''
    }
}

function removeImage(index) {
    imageList.value.splice(index, 1)
    uploadedUrls.value.splice(index, 1)
}

async function handleSubmit() {
    if (!form.type) {
        ElMessage.warning('请选择信息类型')
        return
    }
    if (!form.categoryId) {
        ElMessage.warning('请选择物品分类')
        return
    }
    if (!form.title.trim()) {
        ElMessage.warning('请输入物品名称')
        return
    }
    if (!form.contact.trim()) {
        ElMessage.warning('请输入联系方式')
        return
    }

    submitting.value = true
    try {
        const data = {
            type: form.type,
            categoryId: form.categoryId,
            title: form.title.trim(),
            description: form.description.trim(),
            location: form.location.trim(),
            contact: form.contact.trim(),
            images: JSON.stringify(uploadedUrls.value)
        }

        if (isEdit.value) {
            await itemApi.update(route.params.id, data)
            ElMessage.success('修改成功')
        } else {
            await itemApi.create(data)
            ElMessage.success('发布成功')
        }
        router.push('/')
    } catch (error) {
        console.error('提交失败:', error)
    } finally {
        submitting.value = false
    }
}

onMounted(() => {
    // 从 query 获取 type 参数
    const type = parseInt(route.query.type)
    if (type === 1 || type === 2) {
        form.type = type
    }
    fetchCategories()
    if (isEdit.value) {
        fetchItemDetail()
    }
})
</script>

<style scoped>
.publish-form-page {
    min-height: 100%;
    display: flex;
    flex-direction: column;
}

.pf-header {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 10px;
    background: #fff;
    border-bottom: 1px solid #e2e8f0;
    flex-shrink: 0;
}

.pf-back {
    display: flex;
    align-items: center;
    gap: 4px;
    background: none;
    border: none;
    font-size: 14px;
    color: #4a5568;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: 4px;
    transition: all 0.2s;
}

.pf-back:hover {
    background: #f0f2f5;
    color: #4f46e5;
}

.pf-title {
    font-size: 18px;
    font-weight: 700;
    color: #4a4a6a;
    margin: 0;
}

.pf-body {
    flex: 1;
    max-width: 640px;
    width: 100%;
    margin: 0 auto;
    padding: 32px 24px 60px;
}

.pf-form {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.pf-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.pf-label {
    font-size: 14px;
    font-weight: 600;
    color: #4a4a6a;
}

.pf-input {
    width: 100%;
    padding: 12px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    font-size: 14px;
    color: #4a4a6a;
    background: #fafaff;
    transition: all 0.25s;
    outline: none;
    box-sizing: border-box;
}

.pf-input:focus {
    border-color: #4f46e5;
    background: #fff;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.pf-input::placeholder {
    color: #a0aec0;
}

.pf-textarea {
    width: 100%;
    padding: 12px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    font-size: 14px;
    color: #4a4a6a;
    background: #fafaff;
    transition: all 0.25s;
    outline: none;
    resize: vertical;
    min-height: 100px;
    font-family: inherit;
    box-sizing: border-box;
}

.pf-textarea:focus {
    border-color: #4f46e5;
    background: #fff;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.pf-textarea::placeholder {
    color: #a0aec0;
}

.pf-count {
    align-self: flex-end;
    font-size: 12px;
    color: #a0aec0;
}

.pf-select {
    width: 100%;
}

.pf-select :deep(.el-input__wrapper) {
    background: #fafaff;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    box-shadow: none;
    padding: 4px 12px;
}

.pf-select :deep(.el-input__wrapper.is-focus) {
    border-color: #4f46e5;
    background: #fff;
    box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.pf-upload {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.pf-upload-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
    gap: 10px;
}

.pf-upload-preview {
    position: relative;
    aspect-ratio: 1;
    border-radius: 6px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
}

.pf-upload-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.pf-upload-remove {
    position: absolute;
    top: 4px;
    right: 4px;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: rgba(0, 0, 0, 0.5);
    border: none;
    color: #fff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;
}

.pf-upload-remove:hover {
    background: rgba(192, 57, 43, 0.8);
}

.pf-upload-btn {
    aspect-ratio: 1;
    border: 2px dashed #c0c4cc;
    border-radius: 6px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6px;
    color: #a0aec0;
    cursor: pointer;
    transition: all 0.25s;
}

.pf-upload-btn:hover {
    border-color: #4f46e5;
    color: #4f46e5;
    background: #f0f0ff;
}

.pf-upload-btn span {
    font-size: 12px;
}

.pf-upload-tip {
    font-size: 12px;
    color: #a0aec0;
    margin: 0;
}

.pf-actions {
    padding-top: 12px;
}

.pf-submit {
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
}

.pf-submit:hover:not(:disabled) {
    background: linear-gradient(135deg, #4338ca, #4f46e5);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.pf-submit:disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.pf-loading {
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
</style>