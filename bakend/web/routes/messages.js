const express = require('express');
const router = express.Router();
const { v4: uuidv4 } = require('uuid');
const { authMiddleware } = require('./auth');

// ============ 模拟数据 ============
let messages = [
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