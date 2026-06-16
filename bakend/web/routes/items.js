const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const { authMiddleware } = require('./auth');

// ============ 模拟数据 ============
let items = [
    {
        id: '1',
        title: '黑色双肩背包',
        description: '在图书馆3楼捡到一个黑色双肩背包，内有笔记本电脑一台、充电器、笔记本等物品。',
        category: '包类',
        type: 'lost',
        status: 'pending',
        location: '图书馆3楼',
        date: '2024-06-15',
        contactName: '张三',
        contactPhone: '13800138001',
        images: [],
        userId: '1',
        username: 'admin',
        createdAt: '2024-06-15T10:30:00.000Z',
        updatedAt: '2024-06-15T10:30:00.000Z'
    },
    {
        id: '2',
        title: '银色金属U盘',
        description: '在教学楼A座201教室捡到一个银色金属U盘，32GB容量。',
        category: '电子产品',
        type: 'lost',
        status: 'pending',
        location: '教学楼A座201教室',
        date: '2024-06-14',
        contactName: '李四',
        contactPhone: '13800138002',
        images: [],
        userId: '2',
        username: 'test',
        createdAt: '2024-06-14T15:00:00.000Z',
        updatedAt: '2024-06-14T15:00:00.000Z'
    },
    {
        id: '3',
        title: '蓝色学生证 - 王小明',
        description: '在食堂一楼捡到蓝色学生证，姓名：王小明，学号：2024001。',
        category: '证件',
        type: 'lost',
        status: 'claimed',
        location: '食堂一楼',
        date: '2024-06-13',
        contactName: '王五',
        contactPhone: '13800138003',
        images: [],
        userId: '1',
        username: 'admin',
        createdAt: '2024-06-13T12:00:00.000Z',
        updatedAt: '2024-06-14T09:00:00.000Z'
    },
    {
        id: '4',
        title: '白色iPhone 15手机',
        description: '在操场跑道旁捡到一部白色iPhone 15手机，屏幕有轻微划痕。',
        category: '电子产品',
        type: 'lost',
        status: 'pending',
        location: '操场跑道旁',
        date: '2024-06-12',
        contactName: '赵六',
        contactPhone: '13800138004',
        images: [],
        userId: '2',
        username: 'test',
        createdAt: '2024-06-12T18:00:00.000Z',
        updatedAt: '2024-06-12T18:00:00.000Z'
    },
    {
        id: '5',
        title: '寻找黑色钱包',
        description: '6月10日在行政楼丢失黑色钱包一个，内有身份证、银行卡和现金若干。如有捡到请联系我，必有重谢！',
        category: '包类',
        type: 'found',
        status: 'pending',
        location: '行政楼',
        date: '2024-06-10',
        contactName: '钱七',
        contactPhone: '13800138005',
        images: [],
        userId: '1',
        username: 'admin',
        createdAt: '2024-06-10T09:00:00.000Z',
        updatedAt: '2024-06-10T09:00:00.000Z'
    },
    {
        id: '6',
        title: '红色钥匙串',
        description: '在体育馆更衣室捡到一串红色钥匙串，上面有3把钥匙和一个门禁卡。',
        category: '其他',
        type: 'lost',
        status: 'pending',
        location: '体育馆更衣室',
        date: '2024-06-11',
        contactName: '孙八',
        contactPhone: '13800138006',
        images: [],
        userId: '2',
        username: 'test',
        createdAt: '2024-06-11T14:00:00.000Z',
        updatedAt: '2024-06-11T14:00:00.000Z'
    }
];

// ============ GET /api/items - 获取物品列表（公开） ============
router.get('/', (req, res) => {
    const { type, category, status, page = 1, pageSize = 10 } = req.query;
    let filtered = [...items];

    if (type) {
        filtered = filtered.filter(item => item.type === type);
    }
    if (category) {
        filtered = filtered.filter(item => item.category === category);
    }
    if (status) {
        filtered = filtered.filter(item => item.status === status);
    }

    // 按创建时间倒序
    filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const total = filtered.length;
    const p = parseInt(page);
    const ps = parseInt(pageSize);
    const start = (p - 1) * ps;
    const list = filtered.slice(start, start + ps);

    res.json({
        code: 200,
        message: '获取成功',
        data: {
            list,
            total,
            page: p,
            pageSize: ps
        }
    });
});

// ============ GET /api/items/search - 搜索物品 ============
router.get('/search', (req, res) => {
    const { keyword, page = 1, pageSize = 10 } = req.query;
    let filtered = [...items];

    if (keyword) {
        const kw = keyword.toLowerCase();
        filtered = filtered.filter(item =>
            item.title.toLowerCase().includes(kw) ||
            item.description.toLowerCase().includes(kw) ||
            item.location.toLowerCase().includes(kw)
        );
    }

    filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const total = filtered.length;
    const p = parseInt(page);
    const ps = parseInt(pageSize);
    const start = (p - 1) * ps;
    const list = filtered.slice(start, start + ps);

    res.json({
        code: 200,
        message: '搜索成功',
        data: {
            list,
            total,
            page: p,
            pageSize: ps
        }
    });
});

// ============ GET /api/items/user - 获取用户自己的物品列表 ============
router.get('/user', authMiddleware, (req, res) => {
    const { type, status, page = 1, pageSize = 10 } = req.query;
    let filtered = items.filter(item => item.userId === req.userId);

    if (type) {
        filtered = filtered.filter(item => item.type === type);
    }
    if (status) {
        filtered = filtered.filter(item => item.status === status);
    }

    filtered.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const total = filtered.length;
    const p = parseInt(page);
    const ps = parseInt(pageSize);
    const start = (p - 1) * ps;
    const list = filtered.slice(start, start + ps);

    res.json({
        code: 200,
        message: '获取成功',
        data: {
            list,
            total,
            page: p,
            pageSize: ps
        }
    });
});

// ============ GET /api/items/user/stats - 获取用户统计 ============
router.get('/user/stats', authMiddleware, (req, res) => {
    const userItems = items.filter(item => item.userId === req.userId);
    const stats = {
        total: userItems.length,
        lost: userItems.filter(item => item.type === 'lost').length,
        found: userItems.filter(item => item.type === 'found').length,
        pending: userItems.filter(item => item.status === 'pending').length,
        claimed: userItems.filter(item => item.status === 'claimed').length
    };

    res.json({ code: 200, message: '获取成功', data: stats });
});

// ============ GET /api/items/:id - 获取物品详情 ============
router.get('/:id', (req, res) => {
    const item = items.find(i => i.id === req.params.id);
    if (!item) {
        return res.json({ code: 404, message: '物品不存在', data: null });
    }
    res.json({ code: 200, message: '获取成功', data: item });
});

// ============ POST /api/items - 创建物品 ============
router.post('/', authMiddleware, (req, res) => {
    const { title, description, category, type, location, date, contactName, contactPhone, images } = req.body;

    if (!title || !category || !type || !location || !date) {
        return res.json({ code: 400, message: '请填写必要信息（标题、分类、类型、地点、日期）', data: null });
    }

    const newItem = {
        id: uuidv4(),
        title,
        description: description || '',
        category,
        type,
        status: 'pending',
        location,
        date,
        contactName: contactName || '',
        contactPhone: contactPhone || '',
        images: images || [],
        userId: req.userId,
        username: req.username || '用户',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
    };

    items.unshift(newItem);
    res.json({ code: 200, message: '发布成功', data: newItem });
});

// ============ PUT /api/items/:id - 更新物品 ============
router.put('/:id', authMiddleware, (req, res) => {
    const item = items.find(i => i.id === req.params.id);
    if (!item) {
        return res.json({ code: 404, message: '物品不存在', data: null });
    }
    if (item.userId !== req.userId) {
        return res.json({ code: 403, message: '无权修改此物品', data: null });
    }

    const { title, description, category, type, location, date, contactName, contactPhone, images } = req.body;
    if (title) item.title = title;
    if (description !== undefined) item.description = description;
    if (category) item.category = category;
    if (type) item.type = type;
    if (location) item.location = location;
    if (date) item.date = date;
    if (contactName !== undefined) item.contactName = contactName;
    if (contactPhone !== undefined) item.contactPhone = contactPhone;
    if (images !== undefined) item.images = images;
    item.updatedAt = new Date().toISOString();

    res.json({ code: 200, message: '更新成功', data: item });
});

// ============ PUT /api/items/:id/status - 更新物品状态 ============
router.put('/:id/status', authMiddleware, (req, res) => {
    const item = items.find(i => i.id === req.params.id);
    if (!item) {
        return res.json({ code: 404, message: '物品不存在', data: null });
    }
    if (item.userId !== req.userId) {
        return res.json({ code: 403, message: '无权修改此物品', data: null });
    }

    const { status } = req.body;
    if (!['pending', 'claimed'].includes(status)) {
        return res.json({ code: 400, message: '无效的状态值', data: null });
    }

    item.status = status;
    item.updatedAt = new Date().toISOString();
    res.json({ code: 200, message: '状态更新成功', data: item });
});

// ============ DELETE /api/items/:id - 删除物品 ============
router.delete('/:id', authMiddleware, (req, res) => {
    const index = items.findIndex(i => i.id === req.params.id);
    if (index === -1) {
        return res.json({ code: 404, message: '物品不存在', data: null });
    }
    if (items[index].userId !== req.userId) {
        return res.json({ code: 403, message: '无权删除此物品', data: null });
    }

    items.splice(index, 1);
    res.json({ code: 200, message: '删除成功', data: null });
});

module.exports = router;