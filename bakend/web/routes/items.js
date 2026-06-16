const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const { authMiddleware } = require('./auth');

// ============ 模拟数据 ============
// type: 1=招领, 2=寻物
// status: 1=进行中, 2=已解决
// categoryId: 1=电子产品, 2=包类, 3=证件, 4=钥匙, 5=衣物, 6=书籍, 7=水杯, 8=其他
let items = [
    // ===== 管理员(admin) 的发布 =====
    {
        id: '1',
        title: '黑色双肩背包',
        description: '在图书馆3楼捡到一个黑色双肩背包，内有笔记本电脑一台、充电器、笔记本等物品。请失主携带相关证明到图书馆服务台领取。',
        categoryId: 2,
        type: 1,
        status: 1,
        location: '图书馆3楼',
        contact: '图书馆服务台 - 13800138001',
        images: '[]',
        userId: '1',
        username: '管理员',
        createTime: '2024-06-15T10:30:00.000Z',
        updateTime: '2024-06-15T10:30:00.000Z'
    },
    {
        id: '3',
        title: '蓝色学生证 - 王小明',
        description: '在食堂一楼捡到蓝色学生证，姓名：王小明，学号：2024001，计算机科学与技术专业。请失主到行政楼302办公室领取。',
        categoryId: 3,
        type: 1,
        status: 2,
        location: '食堂一楼',
        contact: '行政楼302 - 13800138003',
        images: '[]',
        userId: '1',
        username: '管理员',
        createTime: '2024-06-13T12:00:00.000Z',
        updateTime: '2024-06-14T09:00:00.000Z'
    },
    {
        id: '5',
        title: '寻找黑色钱包',
        description: '6月10日在行政楼丢失黑色钱包一个，内有身份证（姓名：陈小明）、银行卡（工商银行、建设银行各一张）和现金约500元。如有捡到请联系我，必有重谢！',
        categoryId: 2,
        type: 2,
        status: 2,
        location: '行政楼大厅',
        contact: '陈小明 - 13800138005',
        images: '[]',
        userId: '1',
        username: '管理员',
        createTime: '2024-06-10T09:00:00.000Z',
        updateTime: '2024-06-11T15:00:00.000Z'
    },
    {
        id: '7',
        title: '银色笔记本电脑充电器',
        description: '在图书馆自习区捡到一个银色笔记本电脑充电器，品牌为联想，65W快充。请失主到图书馆服务台认领。',
        categoryId: 1,
        type: 1,
        status: 1,
        location: '图书馆自习区',
        contact: '图书馆服务台 - 13800138001',
        images: '[]',
        userId: '1',
        username: '管理员',
        createTime: '2024-06-18T14:00:00.000Z',
        updateTime: '2024-06-18T14:00:00.000Z'
    },
    {
        id: '8',
        title: '寻找灰色围巾',
        description: '昨天下午在操场看台丢失一条灰色羊毛围巾，是朋友送的生日礼物，很有纪念意义。如有看到请联系我，谢谢！',
        categoryId: 5,
        type: 2,
        status: 1,
        location: '操场看台',
        contact: '管理员 - 13800138000',
        images: '[]',
        userId: '1',
        username: '管理员',
        createTime: '2024-06-19T08:00:00.000Z',
        updateTime: '2024-06-19T08:00:00.000Z'
    },

    // ===== 测试用户(test) 的发布 =====
    {
        id: '2',
        title: '银色金属U盘',
        description: '在教学楼A座201教室捡到一个银色金属U盘，32GB容量，外壳刻有"LXY"字样。请失主联系我认领。',
        categoryId: 1,
        type: 1,
        status: 1,
        location: '教学楼A座201教室',
        contact: '李四 - 13800138002',
        images: '[]',
        userId: '2',
        username: '测试用户',
        createTime: '2024-06-14T15:00:00.000Z',
        updateTime: '2024-06-14T15:00:00.000Z'
    },
    {
        id: '4',
        title: '白色iPhone 15手机',
        description: '在操场跑道旁捡到一部白色iPhone 15手机，屏幕有轻微划痕，手机壳为透明硅胶壳。请失主描述手机特征后认领。',
        categoryId: 1,
        type: 1,
        status: 1,
        location: '操场跑道旁',
        contact: '赵六 - 13800138004',
        images: '[]',
        userId: '2',
        username: '测试用户',
        createTime: '2024-06-12T18:00:00.000Z',
        updateTime: '2024-06-12T18:00:00.000Z'
    },
    {
        id: '6',
        title: '红色钥匙串',
        description: '在体育馆更衣室捡到一串红色钥匙串，上面有3把钥匙（一把大门钥匙、一把自行车钥匙、一把小锁钥匙）和一个蓝色门禁卡。',
        categoryId: 4,
        type: 1,
        status: 1,
        location: '体育馆更衣室',
        contact: '孙八 - 13800138006',
        images: '[]',
        userId: '2',
        username: '测试用户',
        createTime: '2024-06-11T14:00:00.000Z',
        updateTime: '2024-06-11T14:00:00.000Z'
    },
    {
        id: '9',
        title: '寻找蓝牙耳机',
        description: '在食堂二楼丢失一副白色AirPods Pro蓝牙耳机，充电仓有粉色小猪贴纸。耳机对我很重要，捡到者请联系我，酬谢！',
        categoryId: 1,
        type: 2,
        status: 1,
        location: '食堂二楼',
        contact: '测试用户 - 13900139000',
        images: '[]',
        userId: '2',
        username: '测试用户',
        createTime: '2024-06-17T12:30:00.000Z',
        updateTime: '2024-06-17T12:30:00.000Z'
    },
    {
        id: '10',
        title: '高等数学教材',
        description: '在教学楼B座305教室捡到一本《高等数学（第七版）》上册，书内夹有若干笔记和书签，封面写有"李华"名字。',
        categoryId: 6,
        type: 1,
        status: 1,
        location: '教学楼B座305教室',
        contact: '测试用户 - 13900139000',
        images: '[]',
        userId: '2',
        username: '测试用户',
        createTime: '2024-06-20T09:00:00.000Z',
        updateTime: '2024-06-20T09:00:00.000Z'
    },

    // ===== 张三(zhangsan) 的发布 =====
    {
        id: '11',
        title: '不锈钢保温杯',
        description: '在图书馆2楼捡到一个白色不锈钢保温杯，品牌为"膳魔师"，杯身贴有卡通贴纸。请失主到2楼服务台认领。',
        categoryId: 7,
        type: 1,
        status: 1,
        location: '图书馆2楼',
        contact: '张三 - 13700137000',
        images: '[]',
        userId: '3',
        username: '张三',
        createTime: '2024-06-16T16:00:00.000Z',
        updateTime: '2024-06-16T16:00:00.000Z'
    },
    {
        id: '12',
        title: '寻找棕色钱包',
        description: '6月15日在校门口公交站丢失棕色真皮钱包一个，内有身份证（姓名：张伟）、学生卡和公交卡。希望好心人捡到联系我。',
        categoryId: 2,
        type: 2,
        status: 1,
        location: '校门口公交站',
        contact: '张伟 - 13700137001',
        images: '[]',
        userId: '3',
        username: '张三',
        createTime: '2024-06-15T20:00:00.000Z',
        updateTime: '2024-06-15T20:00:00.000Z'
    },
    {
        id: '13',
        title: '黑色皮质手套',
        description: '在行政楼门口捡到一副黑色皮质手套，内衬为羊毛材质，尺寸M号。天气寒冷，失主一定很需要，请尽快认领。',
        categoryId: 5,
        type: 1,
        status: 1,
        location: '行政楼门口',
        contact: '张三 - 13700137000',
        images: '[]',
        userId: '3',
        username: '张三',
        createTime: '2024-06-21T10:00:00.000Z',
        updateTime: '2024-06-21T10:00:00.000Z'
    },

    // ===== 李四(lisi) 的发布 =====
    {
        id: '14',
        title: '华为手表GT3',
        description: '在体育馆游泳池更衣室捡到一块华为Watch GT3智能手表，表盘为深蓝色，表带有轻微磨损。请失主联系认领。',
        categoryId: 1,
        type: 1,
        status: 1,
        location: '体育馆游泳池更衣室',
        contact: '李四 - 13600136000',
        images: '[]',
        userId: '4',
        username: '李四',
        createTime: '2024-06-17T17:00:00.000Z',
        updateTime: '2024-06-17T17:00:00.000Z'
    },
    {
        id: '15',
        title: '寻找单反相机镜头',
        description: '6月12日在校园樱花大道拍照时丢失一个佳能EF 50mm f/1.8镜头，镜头盖为黑色。这是借朋友的镜头，非常重要！捡到者必有重谢！',
        categoryId: 1,
        type: 2,
        status: 1,
        location: '校园樱花大道',
        contact: '摄影师小王 - 13600136001',
        images: '[]',
        userId: '4',
        username: '李四',
        createTime: '2024-06-12T16:00:00.000Z',
        updateTime: '2024-06-12T16:00:00.000Z'
    },
    {
        id: '16',
        title: '大学英语四级词汇书',
        description: '在图书馆5楼自习室捡到一本《大学英语四级词汇闪过》，书内有很多荧光笔标记，扉页写有"加油四级！"字样。',
        categoryId: 6,
        type: 1,
        status: 1,
        location: '图书馆5楼自习室',
        contact: '李四 - 13600136000',
        images: '[]',
        userId: '4',
        username: '李四',
        createTime: '2024-06-22T11:00:00.000Z',
        updateTime: '2024-06-22T11:00:00.000Z'
    },

    // ===== 王五(wangwu) 的发布 =====
    {
        id: '17',
        title: '小米充电宝20000mAh',
        description: '在阶梯教室101捡到一个白色小米充电宝，20000mAh容量，有两条数据线（Type-C和Lightning）。请失主到教室管理处认领。',
        categoryId: 1,
        type: 1,
        status: 1,
        location: '阶梯教室101',
        contact: '王五 - 13500135000',
        images: '[]',
        userId: '5',
        username: '王五',
        createTime: '2024-06-19T15:00:00.000Z',
        updateTime: '2024-06-19T15:00:00.000Z'
    },
    {
        id: '18',
        title: '寻找自行车钥匙',
        description: '昨天在宿舍楼下停车棚丢失一把自行车钥匙，钥匙上有一个蓝色小熊挂件。没有钥匙没法骑车了，捡到请联系我，谢谢！',
        categoryId: 4,
        type: 2,
        status: 1,
        location: '宿舍楼下停车棚',
        contact: '王五 - 13500135000',
        images: '[]',
        userId: '5',
        username: '王五',
        createTime: '2024-06-20T07:00:00.000Z',
        updateTime: '2024-06-20T07:00:00.000Z'
    },
    {
        id: '19',
        title: '灰色运动外套',
        description: '在体育馆篮球场捡到一件灰色运动外套，品牌为Nike，L号，左侧口袋有一包纸巾。请失主到体育馆管理处认领。',
        categoryId: 5,
        type: 1,
        status: 1,
        location: '体育馆篮球场',
        contact: '王五 - 13500135000',
        images: '[]',
        userId: '5',
        username: '王五',
        createTime: '2024-06-23T14:00:00.000Z',
        updateTime: '2024-06-23T14:00:00.000Z'
    },

    // ===== 赵六(zhaoliu) 的发布 =====
    {
        id: '20',
        title: '透明玻璃水杯',
        description: '在教学楼C座401教室捡到一个透明玻璃水杯，杯身有星巴克logo，杯盖为黑色塑料材质。请失主联系认领。',
        categoryId: 7,
        type: 1,
        status: 1,
        location: '教学楼C座401教室',
        contact: '赵六 - 13400134000',
        images: '[]',
        userId: '6',
        username: '赵六',
        createTime: '2024-06-21T09:30:00.000Z',
        updateTime: '2024-06-21T09:30:00.000Z'
    },
    {
        id: '21',
        title: '寻找学生证',
        description: '6月18日不慎丢失学生证，姓名：刘芳，学号：2024015，文学院。学生证对我很重要，补办很麻烦，捡到者请联系我，感谢！',
        categoryId: 3,
        type: 2,
        status: 1,
        location: '校园内',
        contact: '刘芳 - 13400134001',
        images: '[]',
        userId: '6',
        username: '赵六',
        createTime: '2024-06-18T18:00:00.000Z',
        updateTime: '2024-06-18T18:00:00.000Z'
    },
    {
        id: '22',
        title: '一卡通（教师卡）',
        description: '在教工食堂捡到一张教师一卡通，卡号：T2023018，姓名：王教授。请失主到教工食堂窗口认领。',
        categoryId: 3,
        type: 1,
        status: 1,
        location: '教工食堂',
        contact: '赵六 - 13400134000',
        images: '[]',
        userId: '6',
        username: '赵六',
        createTime: '2024-06-24T12:00:00.000Z',
        updateTime: '2024-06-24T12:00:00.000Z'
    },
    {
        id: '23',
        title: '寻找雨伞',
        description: '6月22日下雨天在图书馆门口伞架上丢失一把蓝色格子雨伞，伞柄为弯钩木质手柄。这把伞用了很久有感情了，希望捡到者归还，谢谢！',
        categoryId: 8,
        type: 2,
        status: 1,
        location: '图书馆门口伞架',
        contact: '赵六 - 13400134000',
        images: '[]',
        userId: '6',
        username: '赵六',
        createTime: '2024-06-22T17:00:00.000Z',
        updateTime: '2024-06-22T17:00:00.000Z'
    },
    {
        id: '24',
        title: '帆布手提袋',
        description: '在行政楼会议室捡到一个米色帆布手提袋，内有一个笔记本、两支笔和一瓶矿泉水。请失主到行政楼一楼服务台认领。',
        categoryId: 2,
        type: 1,
        status: 1,
        location: '行政楼会议室',
        contact: '赵六 - 13400134000',
        images: '[]',
        userId: '6',
        username: '赵六',
        createTime: '2024-06-25T10:00:00.000Z',
        updateTime: '2024-06-25T10:00:00.000Z'
    }
];

// ============ GET /api/items - 获取物品列表（公开） ============
router.get('/', (req, res) => {
    const { type, categoryId, status, page = 1, pageSize = 10 } = req.query;
    let filtered = [...items];

    if (type) {
        const typeNum = parseInt(type);
        filtered = filtered.filter(item => item.type === typeNum);
    }
    if (categoryId) {
        const catId = parseInt(categoryId);
        filtered = filtered.filter(item => item.categoryId === catId);
    }
    if (status) {
        const statusNum = parseInt(status);
        filtered = filtered.filter(item => item.status === statusNum);
    }

    // 按创建时间倒序
    filtered.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));

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

    filtered.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));

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
        const typeNum = parseInt(type);
        filtered = filtered.filter(item => item.type === typeNum);
    }
    if (status) {
        const statusNum = parseInt(status);
        filtered = filtered.filter(item => item.status === statusNum);
    }

    filtered.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));

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
        found: userItems.filter(item => item.type === 1).length,
        lost: userItems.filter(item => item.type === 2).length,
        resolved: userItems.filter(item => item.status === 2).length
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
    const { title, description, categoryId, type, location, contact, images } = req.body;

    if (!title || !categoryId || !type) {
        return res.json({ code: 400, message: '请填写必要信息（标题、分类、类型）', data: null });
    }

    const newItem = {
        id: uuidv4(),
        title,
        description: description || '',
        categoryId: parseInt(categoryId),
        type: parseInt(type),
        status: 1,
        location: location || '',
        contact: contact || '',
        images: images || '[]',
        userId: req.userId,
        username: req.username || '用户',
        createTime: new Date().toISOString(),
        updateTime: new Date().toISOString()
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

    const { title, description, categoryId, type, location, contact, images, status } = req.body;
    if (title) item.title = title;
    if (description !== undefined) item.description = description;
    if (categoryId) item.categoryId = parseInt(categoryId);
    if (type) item.type = parseInt(type);
    if (location) item.location = location;
    if (contact !== undefined) item.contact = contact;
    if (images !== undefined) item.images = images;
    if (status !== undefined) item.status = parseInt(status);
    item.updateTime = new Date().toISOString();

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
    const statusNum = parseInt(status);
    if (statusNum !== 1 && statusNum !== 2) {
        return res.json({ code: 400, message: '无效的状态值', data: null });
    }

    item.status = statusNum;
    item.updateTime = new Date().toISOString();
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

// ============ GET /api/items/match/:id - 智能匹配推荐 ============
router.get('/match/:id', (req, res) => {
    const item = items.find(i => i.id === req.params.id);
    if (!item) {
        return res.json({ code: 404, message: '物品不存在', data: null });
    }

    // 如果是招领(type=1)，匹配寻物(type=2)；如果是寻物(type=2)，匹配招领(type=1)
    const targetType = item.type === 1 ? 2 : 1;
    const keyword = (item.title + ' ' + item.description).toLowerCase();
    const keywords = keyword.split(/[\s,，、.。]+/).filter(k => k.length > 1);

    // 计算匹配分数
    let candidates = items
        .filter(i => i.type === targetType && i.status === 1 && i.id !== item.id)
        .map(i => {
            const targetText = (i.title + ' ' + i.description).toLowerCase();
            let score = 0;

            // 标题匹配权重高
            const titleWords = i.title.toLowerCase().split(/[\s,，、.。]+/);
            for (const kw of keywords) {
                if (i.title.toLowerCase().includes(kw)) {
                    score += 5;
                } else if (targetText.includes(kw)) {
                    score += 2;
                }
            }

            // 分类匹配加分
            if (i.categoryId === item.categoryId) {
                score += 3;
            }

            // 地点匹配加分
            if (item.location && i.location) {
                const locWords = item.location.split(/[\s,，、.。]+/);
                for (const lw of locWords) {
                    if (lw.length > 1 && i.location.includes(lw)) {
                        score += 2;
                        break;
                    }
                }
            }

            return { item: i, score };
        })
        .filter(c => c.score > 0)
        .sort((a, b) => b.score - a.score)
        .slice(0, 5);

    res.json({
        code: 200,
        message: '获取成功',
        data: candidates.map(c => c.item)
    });
});

module.exports = router;
