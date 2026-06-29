-- ============================================
-- 失物招领系统 - 数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS lost_property DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lost_property;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS messages;
DROP TABLE IF EXISTS certifications;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    campus VARCHAR(50) DEFAULT NULL COMMENT '校区',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    reputation INT DEFAULT 100 COMMENT '信誉积分',
    is_certified TINYINT(1) DEFAULT 0 COMMENT '是否通过校园认证',
    is_admin TINYINT(1) DEFAULT 0 COMMENT '是否为管理员',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 分类表
-- ============================================
CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(20) DEFAULT NULL COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品分类表';

-- ============================================
-- 3. 物品表
-- ============================================
CREATE TABLE items (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '物品ID',
    user_id INT NOT NULL COMMENT '发布者ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    type VARCHAR(20) NOT NULL COMMENT '类型：found=招领, lost=寻物',
    category VARCHAR(20) DEFAULT NULL COMMENT '分类ID（字符串）',
    location VARCHAR(200) DEFAULT NULL COMMENT '地点',
    date VARCHAR(50) DEFAULT NULL COMMENT '日期',
    images TEXT COMMENT '图片JSON数组',
    contact VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
    status VARCHAR(20) DEFAULT 'active' COMMENT '状态：active=进行中, completed=已解决, deleted=已删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品表';

-- ============================================
-- 4. 认证记录表
-- ============================================
CREATE TABLE certifications (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id INT NOT NULL COMMENT '用户ID',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    student_id VARCHAR(50) NOT NULL COMMENT '学号',
    campus VARCHAR(50) DEFAULT NULL COMMENT '校区',
    id_card LONGTEXT DEFAULT NULL COMMENT '证件照片/base64图片',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending=待审核, approved=通过, rejected=拒绝',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认证记录表';

-- ============================================
-- 5. 消息表
-- ============================================
CREATE TABLE messages (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    sender_id INT NOT NULL COMMENT '发送者ID',
    receiver_id INT NOT NULL COMMENT '接收者ID',
    item_id INT DEFAULT NULL COMMENT '关联物品ID',
    content TEXT NOT NULL COMMENT '消息内容',
    is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id),
    FOREIGN KEY (item_id) REFERENCES items(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 打印提示
SELECT '数据库初始化完成！' as message;