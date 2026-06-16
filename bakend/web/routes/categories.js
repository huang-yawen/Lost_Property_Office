const express = require('express');
const router = express.Router();

// ============ 模拟分类数据 ============
const categories = [
    { id: 1, name: '电子产品', icon: 'Monitor', count: 8 },
    { id: 2, name: '包类', icon: 'Backpack', count: 5 },
    { id: 3, name: '证件', icon: 'CreditCard', count: 4 },
    { id: 4, name: '钥匙', icon: 'Key', count: 2 },
    { id: 5, name: '衣物', icon: 'Tickets', count: 3 },
    { id: 6, name: '书籍', icon: 'Reading', count: 2 },
    { id: 7, name: '水杯', icon: 'Cup', count: 2 },
    { id: 8, name: '其他', icon: 'MoreFilled', count: 1 }
];

// ============ GET /api/categories ============
router.get('/', (req, res) => {
    res.json({ code: 200, message: '获取成功', data: categories });
});

module.exports = router;