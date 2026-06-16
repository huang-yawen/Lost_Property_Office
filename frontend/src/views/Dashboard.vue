<template>
    <div class="dashboard-page">
        <!-- 页面标题 -->
        <div class="dashboard-header">
            <h1 class="dashboard-title">数据看板</h1>
            <p class="dashboard-subtitle">校园失物招领数据概览</p>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon stat-icon-today">
                    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                        <line x1="16" y1="2" x2="16" y2="6"/>
                        <line x1="8" y1="2" x2="8" y2="6"/>
                        <line x1="3" y1="10" x2="21" y2="10"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-value">{{ stats.todayNew }}</span>
                    <span class="stat-label">今日新增</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon-total">
                    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                        <polyline points="22 4 12 14.01 9 11.01"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-value">{{ stats.totalItems }}</span>
                    <span class="stat-label">总发布量</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon-resolved">
                    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                        <polyline points="22 4 12 14.01 9 11.01"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-value">{{ stats.resolvedCount }}</span>
                    <span class="stat-label">已解决</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-icon-rate">
                    <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="12" y1="20" x2="12" y2="10"/>
                        <line x1="18" y1="20" x2="18" y2="4"/>
                        <line x1="6" y1="20" x2="6" y2="16"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-value">{{ stats.resolveRate }}%</span>
                    <span class="stat-label">找回成功率</span>
                </div>
            </div>
        </div>

        <!-- 图表区域 -->
        <div class="charts-grid">
            <!-- 热心值排行榜（柱状图）- 放在最前面，突出宣传效果 -->
            <div class="chart-card chart-card-full">
                <h3 class="chart-title">热心值排行榜 Top10</h3>
                <div class="chart-container" ref="rankChartRef"></div>
            </div>

            <!-- 物品类型分布（饼图） -->
            <div class="chart-card">
                <h3 class="chart-title">物品类型分布</h3>
                <div class="chart-container" ref="typeChartRef"></div>
            </div>

            <!-- 近7天趋势（折线图） -->
            <div class="chart-card">
                <h3 class="chart-title">近7天发布趋势</h3>
                <div class="chart-container" ref="trendChartRef"></div>
            </div>

            <!-- 分类统计（柱状图） -->
            <div class="chart-card">
                <h3 class="chart-title">分类统计 Top10</h3>
                <div class="chart-container" ref="categoryChartRef"></div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { itemApi } from '../api'

const typeChartRef = ref(null)
const trendChartRef = ref(null)
const categoryChartRef = ref(null)
const rankChartRef = ref(null)

const stats = ref({
    todayNew: 0,
    totalItems: 0,
    resolvedCount: 0,
    resolveRate: 0
})

let typeChart = null
let trendChart = null
let categoryChart = null
let rankChart = null

// 获取统计数据
async function fetchStats() {
    try {
        const res = await itemApi.getUserStats()
        if (res.data) {
            stats.value = {
                todayNew: res.data.todayNew || 0,
                totalItems: res.data.totalItems || 0,
                resolvedCount: res.data.resolvedCount || 0,
                resolveRate: res.data.resolveRate || 0
            }
        }
    } catch (error) {
        console.error('获取统计数据失败:', error)
        // 使用模拟数据展示效果
        stats.value = {
            todayNew: 12,
            totalItems: 256,
            resolvedCount: 168,
            resolveRate: 66
        }
    }
}

// 获取图表数据
async function fetchChartData() {
    try {
        const res = await itemApi.getList({ page: 1, pageSize: 1000 })
        const items = res.data?.list || []
        renderCharts(items)
    } catch (error) {
        console.error('获取图表数据失败:', error)
        renderCharts([])
    }
}

function renderCharts(items) {
    // 1. 类型分布饼图
    renderTypeChart(items)
    // 2. 近7天趋势
    renderTrendChart(items)
    // 3. 分类统计
    renderCategoryChart(items)
    // 4. 热心值排行榜
    renderRankChart()
}

function isDarkMode() {
    return document.documentElement.classList.contains('dark')
}

function getChartTextColor() {
    return isDarkMode() ? '#c8c8d8' : '#4a4a6a'
}

function getChartAxisColor() {
    return isDarkMode() ? '#2a2a3e' : '#e2e8f0'
}

function renderTypeChart(items) {
    if (!typeChartRef.value) return
    if (!typeChart) {
        typeChart = echarts.init(typeChartRef.value, null, { renderer: 'canvas' })
    }

    const foundCount = items.filter(i => i.type === 1).length
    const lostCount = items.filter(i => i.type === 2).length

    const textColor = getChartTextColor()

    typeChart.setOption({
        tooltip: {
            trigger: 'item',
            backgroundColor: isDarkMode() ? '#1a1a2e' : '#fff',
            borderColor: isDarkMode() ? '#2a2a3e' : '#e2e8f0',
            textStyle: { color: textColor }
        },
        legend: {
            bottom: '0%',
            textStyle: { fontSize: 12, color: textColor }
        },
        series: [{
            type: 'pie',
            radius: ['40%', '65%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: true,
            itemStyle: {
                borderRadius: 6,
                borderColor: isDarkMode() ? '#1a1a2e' : '#fff',
                borderWidth: 2
            },
            label: {
                show: true,
                formatter: '{b}: {c} ({d}%)',
                fontSize: 12,
                color: textColor
            },
            emphasis: {
                label: { show: true, fontSize: 14, fontWeight: 'bold' }
            },
            data: [
                { value: foundCount, name: '招领', itemStyle: { color: '#059669' } },
                { value: lostCount, name: '寻物', itemStyle: { color: '#dc2626' } }
            ]
        }]
    })
}

function renderTrendChart(items) {
    if (!trendChartRef.value) return
    if (!trendChart) {
        trendChart = echarts.init(trendChartRef.value)
    }

    // 生成近7天日期
    const days = []
    const foundData = []
    const lostData = []
    for (let i = 6; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        const dateStr = `${date.getMonth() + 1}/${date.getDate()}`
        days.push(dateStr)

        const dayStart = new Date(date)
        dayStart.setHours(0, 0, 0, 0)
        const dayEnd = new Date(date)
        dayEnd.setHours(23, 59, 59, 999)

        foundData.push(items.filter(i =>
            i.type === 1 &&
            new Date(i.createTime) >= dayStart &&
            new Date(i.createTime) <= dayEnd
        ).length)

        lostData.push(items.filter(i =>
            i.type === 2 &&
            new Date(i.createTime) >= dayStart &&
            new Date(i.createTime) <= dayEnd
        ).length)
    }

    const textColor = getChartTextColor()
    const axisColor = getChartAxisColor()

    trendChart.setOption({
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'cross' },
            backgroundColor: isDarkMode() ? '#1a1a2e' : '#fff',
            borderColor: isDarkMode() ? '#2a2a3e' : '#e2e8f0',
            textStyle: { color: textColor }
        },
        legend: {
            data: ['招领', '寻物'],
            bottom: '0%',
            textStyle: { fontSize: 12, color: textColor }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '18%',
            top: '8%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: days,
            axisLabel: { fontSize: 11, color: textColor },
            axisLine: { lineStyle: { color: axisColor } },
            splitLine: { lineStyle: { color: axisColor } }
        },
        yAxis: {
            type: 'value',
            minInterval: 1,
            axisLabel: { fontSize: 11, color: textColor },
            axisLine: { lineStyle: { color: axisColor } },
            splitLine: { lineStyle: { color: axisColor } }
        },
        series: [
            {
                name: '招领',
                type: 'line',
                smooth: true,
                lineStyle: { color: '#059669', width: 2 },
                itemStyle: { color: '#059669' },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(5, 150, 105, 0.3)' },
                        { offset: 1, color: 'rgba(5, 150, 105, 0.05)' }
                    ])
                },
                data: foundData
            },
            {
                name: '寻物',
                type: 'line',
                smooth: true,
                lineStyle: { color: '#dc2626', width: 2 },
                itemStyle: { color: '#dc2626' },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(220, 38, 38, 0.3)' },
                        { offset: 1, color: 'rgba(220, 38, 38, 0.05)' }
                    ])
                },
                data: lostData
            }
        ]
    })
}

function renderCategoryChart(items) {
    if (!categoryChartRef.value) return
    if (!categoryChart) {
        categoryChart = echarts.init(categoryChartRef.value)
    }

    // 按分类统计
    const categoryMap = {}
    items.forEach(item => {
        const key = item.categoryId || 0
        if (!categoryMap[key]) {
            categoryMap[key] = { count: 0, name: `分类${key}` }
        }
        categoryMap[key].count++
    })

    // 取 Top10
    const sorted = Object.entries(categoryMap)
        .sort((a, b) => b[1].count - a[1].count)
        .slice(0, 10)

    const textColor = getChartTextColor()
    const axisColor = getChartAxisColor()

    categoryChart.setOption({
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            backgroundColor: isDarkMode() ? '#1a1a2e' : '#fff',
            borderColor: isDarkMode() ? '#2a2a3e' : '#e2e8f0',
            textStyle: { color: textColor }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '8%',
            containLabel: true
        },
        xAxis: {
            type: 'value',
            minInterval: 1,
            axisLabel: { fontSize: 11, color: textColor },
            axisLine: { lineStyle: { color: axisColor } },
            splitLine: { lineStyle: { color: axisColor } }
        },
        yAxis: {
            type: 'category',
            data: sorted.map(s => s[1].name).reverse(),
            axisLabel: { fontSize: 11, color: textColor },
            axisLine: { lineStyle: { color: axisColor } },
            splitLine: { lineStyle: { color: axisColor } }
        },
        series: [{
            type: 'bar',
            data: sorted.map(s => s[1].count).reverse(),
            itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                    { offset: 0, color: '#6366f1' },
                    { offset: 1, color: '#4f46e5' }
                ]),
                borderRadius: [0, 4, 4, 0]
            },
            barMaxWidth: 24
        }]
    })
}

function renderRankChart() {
    if (!rankChartRef.value) return
    if (!rankChart) {
        rankChart = echarts.init(rankChartRef.value)
    }

    // 模拟热心值数据（后续可从后端获取）
    const rankData = [
        { name: '张三', score: 120 },
        { name: '李四', score: 98 },
        { name: '王五', score: 85 },
        { name: '赵六', score: 72 },
        { name: '钱七', score: 65 },
        { name: '孙八', score: 54 },
        { name: '周九', score: 48 },
        { name: '吴十', score: 42 },
        { name: '郑十一', score: 36 },
        { name: '陈十二', score: 28 }
    ]

    const textColor = getChartTextColor()
    const axisColor = getChartAxisColor()

    rankChart.setOption({
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            backgroundColor: isDarkMode() ? '#1a1a2e' : '#fff',
            borderColor: isDarkMode() ? '#2a2a3e' : '#e2e8f0',
            textStyle: { color: textColor }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            top: '8%',
            containLabel: true
        },
        xAxis: {
            type: 'value',
            minInterval: 1,
            axisLabel: { fontSize: 11, color: textColor },
            axisLine: { lineStyle: { color: axisColor } },
            splitLine: { lineStyle: { color: axisColor } }
        },
        yAxis: {
            type: 'category',
            data: rankData.map(d => d.name).reverse(),
            axisLabel: { fontSize: 11, color: textColor },
            axisLine: { lineStyle: { color: axisColor } },
            splitLine: { lineStyle: { color: axisColor } }
        },
        series: [{
            type: 'bar',
            data: rankData.map(d => ({
                value: d.score,
                itemStyle: {
                    color: d.score >= 100
                        ? new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                            { offset: 0, color: '#f59e0b' },
                            { offset: 1, color: '#d97706' }
                          ])
                        : new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                            { offset: 0, color: '#60a5fa' },
                            { offset: 1, color: '#3b82f6' }
                          ])
                }
            })).reverse(),
            barMaxWidth: 24,
            label: {
                show: true,
                position: 'right',
                fontSize: 11,
                color: textColor,
                formatter: '{c} 分'
            }
        }]
    })
}

function handleResize() {
    typeChart?.resize()
    trendChart?.resize()
    categoryChart?.resize()
    rankChart?.resize()
}

onMounted(async () => {
    await fetchStats()
    await fetchChartData()
    window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    typeChart?.dispose()
    trendChart?.dispose()
    categoryChart?.dispose()
    rankChart?.dispose()
})
</script>

<style scoped>
/* ========== 暗黑模式 ========== */
html.dark .dashboard-page {
    background: transparent;
}

html.dark .dashboard-title {
    color: #e0e0f0;
}

html.dark .dashboard-subtitle {
    color: #6a6a8a;
}

html.dark .stat-card {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .stat-card:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

html.dark .stat-icon-today {
    background: rgba(79, 70, 229, 0.15);
    color: #818cf8;
}

html.dark .stat-icon-total {
    background: rgba(5, 150, 105, 0.15);
    color: #34d399;
}

html.dark .stat-icon-resolved {
    background: rgba(217, 119, 6, 0.15);
    color: #fbbf24;
}

html.dark .stat-icon-rate {
    background: rgba(219, 39, 119, 0.15);
    color: #f472b6;
}

html.dark .stat-value {
    color: #e0e0f0;
}

html.dark .stat-label {
    color: #6a6a8a;
}

html.dark .chart-card {
    background: #1a1a2e;
    border-color: #2a2a3e;
}

html.dark .chart-title {
    color: #e0e0f0;
}

/* ========== 数据看板 ========== */
.dashboard-page {
    padding: 8px;
}

.dashboard-header {
    margin-bottom: 24px;
}

.dashboard-title {
    font-size: 22px;
    font-weight: 700;
    color: var(--text-primary, #4a4a6a);
    margin-bottom: 4px;
}

.dashboard-subtitle {
    font-size: 13px;
    color: var(--text-secondary, #a0aec0);
}

/* 统计卡片 */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 24px;
}

.stat-card {
    background: var(--card-bg, #ffffff);
    border-radius: 8px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    border: 1px solid var(--border-color, #f0f0f0);
    transition: all 0.3s;
}

.stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.stat-icon-today {
    background: #eef2ff;
    color: #4f46e5;
}

.stat-icon-total {
    background: #f0fdf4;
    color: #059669;
}

.stat-icon-resolved {
    background: #fef3c7;
    color: #d97706;
}

.stat-icon-rate {
    background: #fce7f3;
    color: #db2777;
}

.stat-info {
    display: flex;
    flex-direction: column;
}

.stat-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary, #4a4a6a);
    line-height: 1.2;
}

.stat-label {
    font-size: 12px;
    color: var(--text-secondary, #a0aec0);
    margin-top: 2px;
}

/* 图表区域 */
.charts-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
}

.chart-card {
    background: var(--card-bg, #ffffff);
    border-radius: 8px;
    padding: 20px;
    border: 1px solid var(--border-color, #f0f0f0);
}

.chart-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary, #4a4a6a);
    margin-bottom: 16px;
}

.chart-container {
    width: 100%;
    height: 300px;
}

/* 响应式 */
@media (max-width: 1024px) {
    .stats-grid {
        grid-template-columns: repeat(2, 1fr);
    }
    .charts-grid {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 640px) {
    .stats-grid {
        grid-template-columns: 1fr;
    }
}
</style>