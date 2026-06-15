-- ============================================
-- 校园失物招领系统 - 数据库建表脚本
-- 数据库名称: lost_property
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS lost_property DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lost_property;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 物品分类表
-- ============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品分类表';

-- 插入默认分类数据
INSERT INTO `category` (`name`, `sort`) VALUES
('证件卡包', 1),
('电子产品', 2),
('书籍资料', 3),
('生活用品', 4),
('衣物鞋帽', 5),
('钥匙', 6),
('水杯餐具', 7),
('体育用品', 8),
('其他', 99);

-- ============================================
-- 3. 物品信息表
-- ============================================
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    `user_id` INT NOT NULL COMMENT '发布用户ID',
    `type` TINYINT NOT NULL COMMENT '类型：1-招领，2-寻物',
    `category_id` INT NOT NULL COMMENT '物品分类ID',
    `title` VARCHAR(100) NOT NULL COMMENT '标题',
    `description` TEXT COMMENT '物品描述',
    `images` TEXT COMMENT '图片URL（JSON数组格式，如["url1","url2"]）',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `contact_qq` VARCHAR(20) DEFAULT NULL COMMENT '联系QQ',
    `location` VARCHAR(255) DEFAULT NULL COMMENT '丢失/拾取地点',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-已下架，1-发布中，2-已完成',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    FULLTEXT KEY `ft_title_description` (`title`, `description`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物品信息表';

-- ============================================
-- 4. 留言/私信表
-- ============================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '留言ID',
    `item_id` INT NOT NULL COMMENT '关联物品ID',
    `from_user_id` INT NOT NULL COMMENT '发送者ID',
    `to_user_id` INT NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '留言内容',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_from_user_id` (`from_user_id`),
    KEY `idx_to_user_id` (`to_user_id`),
    KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='留言/私信表';