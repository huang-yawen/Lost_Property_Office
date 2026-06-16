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
        createdAt: '2024-01-01T00:00:00.000Z',
        // 校园认证
        certified: true,
        certificationImage: '',
        certificationStatus: 'approved', // pending, approved, rejected
        // 信誉积分
        reputation: 100,
        complaints: 0
    },
    {
        id: '2',
        username: 'test',
        password: '123456',
        nickname: '测试用户',
        email: 'test@test.com',
        phone: '13900139000',
        avatar: '',
        createdAt: '2024-01-02T00:00:00.000Z',
        certified: false,
        certificationImage: '',
        certificationStatus: 'none',
        reputation: 80,
        complaints: 2
    },
    {
        id: '3',
        username: 'zhangsan',
        password: '123456',
        nickname: '张三',
        email: 'zhangsan@test.com',
        phone: '13700137000',
        avatar: '',
        createdAt: '2024-02-15T00:00:00.000Z',
        certified: true,
        certificationImage: '',
        certificationStatus: 'approved',
        reputation: 95,
        complaints: 0
    },
    {
        id: '4',
        username: 'lisi',
        password: '123456',
        nickname: '李四',
        email: 'lisi@test.com',
        phone: '13600136000',
        avatar: '',
        createdAt: '2024-03-10T00:00:00.000Z',
        certified: false,
        certificationImage: '',
        certificationStatus: 'none',
        reputation: 70,
        complaints: 3
    },
    {
        id: '5',
        username: 'wangwu',
        password: '123456',
        nickname: '王五',
        email: 'wangwu@test.com',
        phone: '13500135000',
        avatar: '',
        createdAt: '2024-04-20T00:00:00.000Z',
        certified: false,
        certificationImage: '',
        certificationStatus: 'none',
        reputation: 85,
        complaints: 1
    },
    {
        id: '6',
        username: 'zhaoliu',
        password: '123456',
        nickname: '赵六',
        email: 'zhaoliu@test.com',
        phone: '13400134000',
        avatar: '',
        createdAt: '2024-05-05T00:00:00.000Z',
        certified: false,
        certificationImage: '',
        certificationStatus: 'none',
        reputation: 90,
        complaints: 0
    },
    // ===== 测试数据：待审核的认证申请 =====
    {
        id: '7',
        username: 'chenqi',
        password: '123456',
        nickname: '陈七',
        email: 'chenqi@test.com',
        phone: '13300133000',
        avatar: '',
        createdAt: '2024-06-01T00:00:00.000Z',
        certified: false,
        certificationImage: '/images/test-student-id-1.jpg',
        certificationStatus: 'pending',
        reputation: 75,
        complaints: 0
    },
    {
        id: '8',
        username: 'liuba',
        password: '123456',
        nickname: '刘八',
        email: 'liuba@test.com',
        phone: '13200132000',
        avatar: '',
        createdAt: '2024-06-10T00:00:00.000Z',
        certified: false,
        certificationImage: '/images/test-student-id-2.jpg',
        certificationStatus: 'pending',
        reputation: 60,
        complaints: 1
    },
    {
        id: '9',
        username: 'sunjiu',
        password: '123456',
        nickname: '孙九',
        email: 'sunjiu@test.com',
        phone: '13100131000',
        avatar: '',
        createdAt: '2024-06-15T00:00:00.000Z',
        certified: false,
        certificationImage: '/images/test-student-id-3.jpg',
        certificationStatus: 'pending',
        reputation: 88,
        complaints: 0
    },
    {
        id: '10',
        username: 'wushi',
        password: '123456',
        nickname: '吴十',
        email: 'wushi@test.com',
        phone: '13000130000',
        avatar: '',
        createdAt: '2024-06-20T00:00:00.000Z',
        certified: false,
        certificationImage: '/images/test-student-id-4.jpg',
        certificationStatus: 'pending',
        reputation: 92,
        complaints: 0
    },
    // ===== 测试数据：已拒绝的认证 =====
    {
        id: '11',
        username: 'zhouyi',
        password: '123456',
        nickname: '周一',
        email: 'zhouyi@test.com',
        phone: '12900129000',
        avatar: '',
        createdAt: '2024-05-20T00:00:00.000Z',
        certified: false,
        certificationImage: '/images/test-student-id-rejected.jpg',
        certificationStatus: 'rejected',
        rejectReason: '学生证照片不清晰，请重新上传',
        reputation: 50,
        complaints: 2
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
        // 获取用户名
        const user = users.find(u => u.id === req.userId);
        if (user) {
            req.username = user.nickname || user.username;
        }
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
        createdAt: new Date().toISOString(),
        // 校园认证
        certified: false,
        certificationImage: '',
        certificationStatus: 'none', // none, pending, approved, rejected
        // 信誉积分
        reputation: 100,
        complaints: 0
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

// ============ POST /api/auth/certification - 提交校园认证 ============
router.post('/certification', authMiddleware, (req, res) => {
    const user = users.find(u => u.id === req.userId);
    if (!user) {
        return res.json({ code: 404, message: '用户不存在', data: null });
    }

    const { image } = req.body;
    if (!image) {
        return res.json({ code: 400, message: '请上传学生证或校园卡图片', data: null });
    }

    user.certificationImage = image;
    user.certificationStatus = 'pending';
    user.certified = false;

    const { password: _, ...userInfo } = user;
    res.json({ code: 200, message: '认证申请已提交，请等待管理员审核', data: userInfo });
});

// ============ GET /api/auth/certification/list - 管理员获取认证申请列表 ============
router.get('/certification/list', authMiddleware, (req, res) => {
    const user = users.find(u => u.id === req.userId);
    if (!user || user.username !== 'admin') {
        return res.json({ code: 403, message: '仅管理员可查看', data: null });
    }

    const { status } = req.query; // pending, approved, rejected
    let filteredUsers = users.filter(u => u.certificationStatus !== 'none');
    if (status) {
        filteredUsers = filteredUsers.filter(u => u.certificationStatus === status);
    }

    const result = filteredUsers.map(u => {
        const { password: _, ...info } = u;
        return {
            ...info,
            userId: u.id,
            realName: u.nickname || u.username,
            studentId: u.phone || '',
            department: '信息学院',
            reason: '校园认证申请',
            submitTime: u.createdAt
        };
    });

    res.json({ code: 200, message: '获取成功', data: result });
});

// ============ POST /api/auth/certification/review - 管理员审核认证 ============
router.post('/certification/review', authMiddleware, (req, res) => {
    const admin = users.find(u => u.id === req.userId);
    if (!admin || admin.username !== 'admin') {
        return res.json({ code: 403, message: '仅管理员可审核', data: null });
    }

    const { userId, action, reason } = req.body; // action: 'approve' or 'reject'
    const targetUser = users.find(u => u.id === userId);
    if (!targetUser) {
        return res.json({ code: 404, message: '用户不存在', data: null });
    }

    if (action === 'approve') {
        targetUser.certificationStatus = 'approved';
        targetUser.certified = true;
        targetUser.rejectReason = '';
        const { password: _, ...userInfo } = targetUser;
        return res.json({ code: 200, message: '认证已通过', data: userInfo });
    } else if (action === 'reject') {
        targetUser.certificationStatus = 'rejected';
        targetUser.certified = false;
        targetUser.rejectReason = reason || '认证信息不符合要求';
        const { password: _, ...userInfo } = targetUser;
        return res.json({ code: 200, message: '认证已拒绝', data: userInfo });
    }

    res.json({ code: 400, message: '无效的操作', data: null });
});

// ============ POST /api/auth/reputation - 更新信誉积分 ============
router.post('/reputation', authMiddleware, (req, res) => {
    const { targetUserId, type } = req.body; // type: 'resolve' or 'complaint'
    const targetUser = users.find(u => u.id === targetUserId);
    if (!targetUser) {
        return res.json({ code: 404, message: '用户不存在', data: null });
    }

    if (type === 'resolve') {
        targetUser.reputation = Math.min(100, targetUser.reputation + 10);
    } else if (type === 'complaint') {
        targetUser.reputation = Math.max(0, targetUser.reputation - 10);
        targetUser.complaints = (targetUser.complaints || 0) + 1;
    }

    const { password: _, ...userInfo } = targetUser;
    res.json({ code: 200, message: '信誉积分已更新', data: userInfo });
});

// ============ GET /api/auth/users - 获取所有用户信息（公开，用于显示认证标识） ============
router.get('/users', (req, res) => {
    const { ids } = req.query;
    let result = users;
    if (ids) {
        const idArr = ids.split(',');
        result = users.filter(u => idArr.includes(u.id));
    }
    const safeUsers = result.map(u => ({
        id: u.id,
        username: u.username,
        nickname: u.nickname,
        avatar: u.avatar,
        certified: u.certified,
        certificationStatus: u.certificationStatus,
        reputation: u.reputation
    }));
    res.json({ code: 200, message: '获取成功', data: safeUsers });
});

module.exports = router;
module.exports.authMiddleware = authMiddleware;
module.exports.users = users;
