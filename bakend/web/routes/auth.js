const express = require('express');
const router = express.Router();
const jwt = require('jsonwebtoken');
const { v4: uuidv4 } = require('uuid');

const JWT_SECRET = 'lost-property-office-secret-key-2024';

// ============ 模拟数据 ============
// 预置用户（用于测试）
let users = [
    {
        id: '1',
        username: 'admin',
        password: '123456',
        nickname: '管理员',
        email: 'admin@test.com',
        phone: '13800138000',
        avatar: '',
        createdAt: '2024-01-01T00:00:00.000Z'
    },
    {
        id: '2',
        username: 'test',
        password: '123456',
        nickname: '测试用户',
        email: 'test@test.com',
        phone: '13900139000',
        avatar: '',
        createdAt: '2024-01-02T00:00:00.000Z'
    }
];

// ============ 中间件：JWT 验证 ============
function authMiddleware(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ code: 401, message: '未登录或登录已过期', data: null });
    }
    const token = authHeader.split(' ')[1];
    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        req.userId = decoded.userId;
        next();
    } catch (err) {
        return res.status(401).json({ code: 401, message: '登录已过期，请重新登录', data: null });
    }
}

// ============ POST /api/auth/login ============
router.post('/login', (req, res) => {
    const { username, password } = req.body;

    if (!username || !password) {
        return res.json({ code: 400, message: '用户名和密码不能为空', data: null });
    }

    const user = users.find(u => u.username === username);
    if (!user) {
        return res.json({ code: 400, message: '用户名或密码错误', data: null });
    }

    if (user.password !== password) {
        return res.json({ code: 400, message: '用户名或密码错误', data: null });
    }

    const token = jwt.sign({ userId: user.id }, JWT_SECRET, { expiresIn: '7d' });

    const { password: _, ...userInfo } = user;
    res.json({
        code: 200,
        message: '登录成功',
        data: {
            token,
            user: userInfo
        }
    });
});

// ============ POST /api/auth/register ============
router.post('/register', (req, res) => {
    const { username, password, nickname, email, phone } = req.body;

    if (!username || !password) {
        return res.json({ code: 400, message: '用户名和密码不能为空', data: null });
    }

    if (username.length < 3 || username.length > 20) {
        return res.json({ code: 400, message: '用户名长度应在3-20个字符之间', data: null });
    }

    if (password.length < 6) {
        return res.json({ code: 400, message: '密码长度不能少于6位', data: null });
    }

    const existingUser = users.find(u => u.username === username);
    if (existingUser) {
        return res.json({ code: 400, message: '用户名已存在', data: null });
    }

    const newUser = {
        id: uuidv4(),
        username,
        password,
        nickname: nickname || username,
        email: email || '',
        phone: phone || '',
        avatar: '',
        createdAt: new Date().toISOString()
    };

    users.push(newUser);

    const token = jwt.sign({ userId: newUser.id }, JWT_SECRET, { expiresIn: '7d' });

    const { password: _, ...userInfo } = newUser;
    res.json({
        code: 200,
        message: '注册成功',
        data: {
            token,
            user: userInfo
        }
    });
});

// ============ GET /api/auth/profile ============
router.get('/profile', authMiddleware, (req, res) => {
    const user = users.find(u => u.id === req.userId);
    if (!user) {
        return res.json({ code: 404, message: '用户不存在', data: null });
    }

    const { password: _, ...userInfo } = user;
    res.json({ code: 200, message: '获取成功', data: userInfo });
});

// ============ PUT /api/auth/profile ============
router.put('/profile', authMiddleware, (req, res) => {
    const user = users.find(u => u.id === req.userId);
    if (!user) {
        return res.json({ code: 404, message: '用户不存在', data: null });
    }

    const { nickname, email, phone, avatar } = req.body;
    if (nickname) user.nickname = nickname;
    if (email !== undefined) user.email = email;
    if (phone !== undefined) user.phone = phone;
    if (avatar !== undefined) user.avatar = avatar;

    const { password: _, ...userInfo } = user;
    res.json({ code: 200, message: '更新成功', data: userInfo });
});

module.exports = router;
module.exports.authMiddleware = authMiddleware;