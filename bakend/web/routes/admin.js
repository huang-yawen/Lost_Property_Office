const express = require('express');
const router = express.Router();
const jwt = require('jsonwebtoken');

const JWT_SECRET = 'lost-property-office-secret-key-2024';

// 预置管理员账号
const adminAccount = {
    id: 'admin-001',
    username: 'admin',
    password: 'admin123',
    role: 'admin'
};

// Admin login
router.post('/login', (req, res) => {
    const { username, password } = req.body;

    if (!username || !password) {
        return res.json({ code: 400, message: '用户名和密码不能为空', data: null });
    }

    if (username !== adminAccount.username || password !== adminAccount.password) {
        return res.json({ code: 400, message: '管理员账号或密码错误', data: null });
    }

    const token = jwt.sign({ userId: adminAccount.id, role: 'admin' }, JWT_SECRET, { expiresIn: '24h' });

    res.json({
        code: 200,
        message: '登录成功',
        data: {
            token,
            user: {
                id: adminAccount.id,
                username: adminAccount.username,
                role: 'admin'
            }
        }
    });
});

// Middleware to verify admin token
function adminMiddleware(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ code: 401, message: '未登录或登录已过期', data: null });
    }
    const token = authHeader.split(' ')[1];
    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        if (decoded.role !== 'admin') {
            return res.status(403).json({ code: 403, message: '无管理员权限', data: null });
        }
        req.userId = decoded.userId;
        next();
    } catch (err) {
        return res.status(401).json({ code: 401, message: '登录已过期，请重新登录', data: null });
    }
}

// Get certifications list (with status filter)
router.get('/certifications', adminMiddleware, (req, res) => {
    const { users } = require('./auth');
    const { status } = req.query; // pending, approved, rejected

    let filteredUsers = users.filter(u => u.certificationStatus && u.certificationStatus !== 'none');
    if (status) {
        filteredUsers = filteredUsers.filter(u => u.certificationStatus === status);
    }

    const result = filteredUsers.map(u => ({
        id: u.id,
        userId: u.id,
        username: u.nickname || u.username,
        realName: u.nickname || u.username,
        studentId: u.phone || '',
        department: '信息学院',
        phone: u.phone || '',
        reason: '校园认证申请',
        certificationStatus: u.certificationStatus,
        certificationImage: u.certificationImage || '',
        rejectReason: u.rejectReason || '',
        createTime: u.createdAt,
        submitTime: u.createdAt
    }));

    res.json({ code: 200, message: '获取成功', data: result });
});

// Approve or reject certification
router.put('/certifications/:id', adminMiddleware, (req, res) => {
    const { id } = req.params;
    const { action, reason } = req.body; // action: 'approve' or 'reject'
    const { users } = require('./auth');

    const user = users.find(u => u.id === id);
    if (!user) {
        return res.json({ code: 404, message: '用户不存在', data: null });
    }

    if (action === 'approve') {
        user.certificationStatus = 'approved';
        user.certified = true;
        user.rejectReason = '';
        return res.json({ code: 200, message: '认证已通过', data: null });
    } else if (action === 'reject') {
        user.certificationStatus = 'rejected';
        user.certified = false;
        user.rejectReason = reason || '认证信息不符合要求';
        return res.json({ code: 200, message: '认证已拒绝', data: null });
    }

    res.json({ code: 400, message: '无效的操作', data: null });
});

// Get user list
router.get('/users', adminMiddleware, (req, res) => {
    const { users } = require('./auth');
    const userList = users
        .filter(u => u.username !== 'admin') // 排除管理员自己
        .map(u => ({
            id: u.id,
            username: u.nickname || u.username,
            is_certified: u.certified ? 1 : 0,
            certificationStatus: u.certificationStatus || 'none',
            points: u.reputation || 0,
            created_at: u.createdAt
        }));

    res.json({ code: 200, message: '获取成功', data: userList });
});

module.exports = router;