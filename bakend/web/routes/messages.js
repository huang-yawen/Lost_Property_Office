const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const { authMiddleware } = require('./auth');

// ============ 模拟数据 ============
let messages = [
    // ===== 物品1: 黑色双肩背包 - 管理员与测试用户的对话 =====
    {
        id: '1',
        itemId: '1',
        itemTitle: '黑色双肩背包',
        senderId: '2',
        senderName: '测试用户',
        receiverId: '1',
        receiverName: '管理员',
        content: '你好，这个背包是我的，请问怎么联系你？',
        isRead: false,
        createdAt: '2024-06-15T11:00:00.000Z'
    },
    {
        id: '2',
        itemId: '1',
        itemTitle: '黑色双肩背包',
        senderId: '1',
        senderName: '管理员',
        receiverId: '2',
        receiverName: '测试用户',
        content: '请到图书馆3楼服务台领取，报我的名字即可。',
        isRead: true,
        createdAt: '2024-06-15T11:30:00.000Z'
    },
    {
        id: '3',
        itemId: '3',
        itemTitle: '蓝色学生证 - 王小明',
        senderId: '2',
        senderName: '测试用户',
        receiverId: '1',
        receiverName: '管理员',
        content: '请问学生证还在吗？我是王小明本人。',
        isRead: false,
        createdAt: '2024-06-14T10:00:00.000Z'
    },
    {
        id: '4',
        itemId: '3',
        itemTitle: '蓝色学生证 - 王小明',
        senderId: '1',
        senderName: '管理员',
        receiverId: '2',
        receiverName: '测试用户',
        content: '还在的，请到行政楼302办公室领取。',
        isRead: true,
        createdAt: '2024-06-14T10:30:00.000Z'
    },
    {
        id: '5',
        itemId: '3',
        itemTitle: '蓝色学生证 - 王小明',
        senderId: '2',
        senderName: '测试用户',
        receiverId: '1',
        receiverName: '管理员',
        content: '好的，我下午过来拿，谢谢！',
        isRead: false,
        createdAt: '2024-06-14T11:00:00.000Z'
    },
    {
        id: '6',
        itemId: '5',
        itemTitle: '寻找黑色钱包',
        senderId: '1',
        senderName: '管理员',
        receiverId: '2',
        receiverName: '测试用户',
        content: '你好，我捡到了一个黑色钱包，请问是你丢的吗？',
        isRead: true,
        createdAt: '2024-06-11T09:00:00.000Z'
    },
    {
        id: '7',
        itemId: '5',
        itemTitle: '寻找黑色钱包',
        senderId: '2',
        senderName: '测试用户',
        receiverId: '1',
        receiverName: '管理员',
        content: '是的！里面有身份证和银行卡，太感谢了！',
        isRead: true,
        createdAt: '2024-06-11T09:15:00.000Z'
    },
    {
        id: '8',
        itemId: '5',
        itemTitle: '寻找黑色钱包',
        senderId: '1',
        senderName: '管理员',
        receiverId: '2',
        receiverName: '测试用户',
        content: '不客气，请到图书馆一楼服务台领取。',
        isRead: true,
        createdAt: '2024-06-11T09:30:00.000Z'
    },
    {
        id: '9',
        itemId: '5',
        itemTitle: '寻找黑色钱包',
        senderId: '2',
        senderName: '测试用户',
        receiverId: '1',
        receiverName: '管理员',
        content: '已经拿到了，非常感谢！',
        isRead: true,
        createdAt: '2024-06-11T15:00:00.000Z'
    },

    // ===== 物品4: 白色iPhone 15手机 - 测试用户与张三的对话 =====
    {
        id: '10',
        itemId: '4',
        itemTitle: '白色iPhone 15手机',
        senderId: '3',
        senderName: '张三',
        receiverId: '2',
        receiverName: '测试用户',
        content: '你好，我昨天在操场丢了一部白色iPhone 15，请问你捡到的是不是我的？',
        isRead: false,
        createdAt: '2024-06-13T09:00:00.000Z'
    },
    {
        id: '11',
        itemId: '4',
        itemTitle: '白色iPhone 15手机',
        senderId: '2',
        senderName: '测试用户',
        receiverId: '3',
        receiverName: '张三',
        content: '你好，请问你的手机有什么特征吗？比如手机壳是什么样的？',
        isRead: true,
        createdAt: '2024-06-13T10:00:00.000Z'
    },
    {
        id: '12',
        itemId: '4',
        itemTitle: '白色iPhone 15手机',
        senderId: '3',
        senderName: '张三',
        receiverId: '2',
        receiverName: '测试用户',
        content: '我的手机壳是透明硅胶的，屏幕右上角有一道小划痕。',
        isRead: false,
        createdAt: '2024-06-13T10:30:00.000Z'
    },

    // ===== 物品9: 寻找蓝牙耳机 - 测试用户与李四的对话 =====
    {
        id: '13',
        itemId: '9',
        itemTitle: '寻找蓝牙耳机',
        senderId: '4',
        senderName: '李四',
        receiverId: '2',
        receiverName: '测试用户',
        content: '我在食堂二楼捡到了一副白色耳机，充电仓有粉色贴纸，是你的吗？',
        isRead: true,
        createdAt: '2024-06-18T08:00:00.000Z'
    },
    {
        id: '14',
        itemId: '9',
        itemTitle: '寻找蓝牙耳机',
        senderId: '2',
        senderName: '测试用户',
        receiverId: '4',
        receiverName: '李四',
        content: '是的！就是我的AirPods Pro，粉色小猪贴纸！太感谢了，怎么联系你？',
        isRead: true,
        createdAt: '2024-06-18T08:30:00.000Z'
    },
    {
        id: '15',
        itemId: '9',
        itemTitle: '寻找蓝牙耳机',
        senderId: '4',
        senderName: '李四',
        receiverId: '2',
        receiverName: '测试用户',
        content: '我下午会在图书馆，你可以到2楼来找我，电话13600136000。',
        isRead: true,
        createdAt: '2024-06-18T09:00:00.000Z'
    },

    // ===== 物品12: 寻找棕色钱包 - 张三与王五的对话 =====
    {
        id: '16',
        itemId: '12',
        itemTitle: '寻找棕色钱包',
        senderId: '5',
        senderName: '王五',
        receiverId: '3',
        receiverName: '张三',
        content: '你好，我昨天在校门口捡到一个棕色钱包，里面有一张身份证名字是张伟，请问是你朋友的吗？',
        isRead: false,
        createdAt: '2024-06-16T10:00:00.000Z'
    },
    {
        id: '17',
        itemId: '12',
        itemTitle: '寻找棕色钱包',
        senderId: '3',
        senderName: '张三',
        receiverId: '5',
        receiverName: '王五',
        content: '是的！是我室友丢的，太感谢了！请问怎么联系你？',
        isRead: true,
        createdAt: '2024-06-16T10:30:00.000Z'
    },

    // ===== 物品14: 华为手表GT3 - 李四与赵六的对话 =====
    {
        id: '18',
        itemId: '14',
        itemTitle: '华为手表GT3',
        senderId: '6',
        senderName: '赵六',
        receiverId: '4',
        receiverName: '李四',
        content: '你好，我昨天在体育馆游泳时可能把手表落在更衣室了，请问你捡到的是深蓝色表盘的吗？',
        isRead: false,
        createdAt: '2024-06-18T09:00:00.000Z'
    },
    {
        id: '19',
        itemId: '14',
        itemTitle: '华为手表GT3',
        senderId: '4',
        senderName: '李四',
        receiverId: '6',
        receiverName: '赵六',
        content: '是的，深蓝色表盘，表带有一点磨损。请描述一下你的手表还有什么特征？',
        isRead: true,
        createdAt: '2024-06-18T09:30:00.000Z'
    },

    // ===== 物品7: 银色笔记本电脑充电器 - 管理员与张三的对话 =====
    {
        id: '20',
        itemId: '7',
        itemTitle: '银色笔记本电脑充电器',
        senderId: '3',
        senderName: '张三',
        receiverId: '1',
        receiverName: '管理员',
        content: '你好，我昨天在图书馆自习区丢了一个联想充电器，请问是银色的65W快充吗？',
        isRead: false,
        createdAt: '2024-06-19T10:00:00.000Z'
    },
    {
        id: '21',
        itemId: '7',
        itemTitle: '银色笔记本电脑充电器',
        senderId: '1',
        senderName: '管理员',
        receiverId: '3',
        receiverName: '张三',
        content: '是的，银色联想65W充电器，请到图书馆服务台认领。',
        isRead: true,
        createdAt: '2024-06-19T10:30:00.000Z'
    },

    // ===== 物品21: 寻找学生证 - 赵六与张三的对话 =====
    {
        id: '22',
        itemId: '21',
        itemTitle: '寻找学生证',
        senderId: '3',
        senderName: '张三',
        receiverId: '6',
        receiverName: '赵六',
        content: '你好，我捡到了一张学生证，名字是刘芳，文学院的，请问是你认识的人吗？',
        isRead: false,
        createdAt: '2024-06-19T14:00:00.000Z'
    },
    {
        id: '23',
        itemId: '21',
        itemTitle: '寻找学生证',
        senderId: '6',
        senderName: '赵六',
        receiverId: '3',
        receiverName: '张三',
        content: '是的！是我同学的学生证，她非常着急。请问在哪里可以拿到？',
        isRead: true,
        createdAt: '2024-06-19T14:30:00.000Z'
    },
    {
        id: '24',
        itemId: '21',
        itemTitle: '寻找学生证',
        senderId: '3',
        senderName: '张三',
        receiverId: '6',
        receiverName: '赵六',
        content: '我在3号宿舍楼，让她下午过来找我拿吧，电话13700137000。',
        isRead: true,
        createdAt: '2024-06-19T15:00:00.000Z'
    }
];

// ============ GET /api/messages/:itemId - 获取物品的留言列表 ============
router.get('/:itemId', (req, res) => {
    const itemMessages = messages.filter(m => m.itemId === req.params.itemId);
    itemMessages.sort((a, b) => new Date(a.createdAt) - new Date(b.createdAt));
    res.json({ code: 200, message: '获取成功', data: itemMessages });
});

// ============ POST /api/messages - 发送留言 ============
router.post('/', authMiddleware, (req, res) => {
    const { itemId, itemTitle, receiverId, receiverName, content } = req.body;

    if (!itemId || !receiverId || !content) {
        return res.json({ code: 400, message: '请填写必要信息', data: null });
    }

    const newMessage = {
        id: uuidv4(),
        itemId,
        itemTitle: itemTitle || '',
        senderId: req.userId,
        senderName: req.username || '用户',
        receiverId,
        receiverName: receiverName || '',
        content,
        isRead: false,
        createdAt: new Date().toISOString()
    };

    messages.push(newMessage);
    res.json({ code: 200, message: '发送成功', data: newMessage });
});

// ============ GET /api/messages/user/received - 获取用户收到的消息 ============
router.get('/user/received', authMiddleware, (req, res) => {
    const { page = 1, pageSize = 10 } = req.query;
    let userMessages = messages.filter(m => m.receiverId === req.userId);
    userMessages.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const total = userMessages.length;
    const p = parseInt(page);
    const ps = parseInt(pageSize);
    const start = (p - 1) * ps;
    const list = userMessages.slice(start, start + ps);

    res.json({
        code: 200,
        message: '获取成功',
        data: { list, total, page: p, pageSize: ps }
    });
});

// ============ GET /api/messages/user/sent - 获取用户发送的消息 ============
router.get('/user/sent', authMiddleware, (req, res) => {
    const { page = 1, pageSize = 10 } = req.query;
    let userMessages = messages.filter(m => m.senderId === req.userId);
    userMessages.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    const total = userMessages.length;
    const p = parseInt(page);
    const ps = parseInt(pageSize);
    const start = (p - 1) * ps;
    const list = userMessages.slice(start, start + ps);

    res.json({
        code: 200,
        message: '获取成功',
        data: { list, total, page: p, pageSize: ps }
    });
});

// ============ GET /api/messages/unread/count - 获取未读消息数 ============
router.get('/unread/count', authMiddleware, (req, res) => {
    const count = messages.filter(m => m.receiverId === req.userId && !m.isRead).length;
    res.json({ code: 200, message: '获取成功', data: { count } });
});

// ============ PUT /api/messages/:id/read - 标记消息已读 ============
router.put('/:id/read', authMiddleware, (req, res) => {
    const message = messages.find(m => m.id === req.params.id);
    if (!message) {
        return res.json({ code: 404, message: '消息不存在', data: null });
    }
    message.isRead = true;
    res.json({ code: 200, message: '标记成功', data: message });
});

module.exports = router;