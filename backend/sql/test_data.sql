-- ============================================
-- 失物招领系统 - 测试数据
-- ============================================

-- 1. 插入分类数据
INSERT INTO categories (id, name, icon, sort_order) VALUES
(1, '电子产品', '📱', 1),
(2, '证件卡包', '🪪', 2),
(3, '钥匙', '🔑', 3),
(4, '书包行李', '🎒', 4),
(5, '衣物', '👔', 5),
(6, '水杯餐具', '🥤', 6),
(7, '书籍文具', '📚', 7),
(8, '雨伞', '🌂', 8),
(9, '饰品', '💍', 9),
(10, '其他', '📦', 10);

-- 2. 插入测试用户（密码都是 123456）
INSERT INTO users (id, username, password, nickname, email, phone, campus, reputation, is_certified, is_admin, created_at) VALUES
(1, 'admin', '123456', '系统管理员', 'admin@lpo.com', '13800000000', '主校区', 999, 1, 1, '2025-01-01 00:00:00'),
(2, 'zhangsan', '123456', '张三', 'zhangsan@lpo.com', '13800000001', '主校区', 100, 1, 0, '2025-01-15 10:00:00'),
(3, 'lisi', '123456', '李四', 'lisi@lpo.com', '13800000002', '东校区', 95, 0, 0, '2025-02-01 14:30:00'),
(4, 'wangwu', '123456', '王五', 'wangwu@lpo.com', '13800000003', '西校区', 120, 1, 0, '2025-02-20 09:15:00'),
(5, 'zhaoliu', '123456', '赵六', 'zhaoliu@lpo.com', '13800000004', '南校区', 80, 0, 0, '2025-03-10 16:45:00');

-- 3. 插入测试物品（招领物品 type=found=1，寻物物品 type=lost=2）
-- 招领物品
INSERT INTO items (id, user_id, title, description, type, category, location, date, images, contact, status, created_at) VALUES
(1, 2, '捡到一部黑色手机', '在图书馆三楼自习区捡到一部黑色华为手机，屏幕有轻微划痕，请失主联系我。', 'found', '1', '图书馆三楼', '2025-03-01', '/images/phone1.jpg', '13800000001', 'active', '2025-03-01 10:30:00'),
(2, 2, '捡到学生证', '在食堂一楼捡到学生证一张，姓名：王小明，学号：2024001，请失主认领。', 'found', '2', '食堂一楼', '2025-03-05', '/images/card1.jpg', '13800000001', 'active', '2025-03-05 12:00:00'),
(3, 3, '捡到一串钥匙', '在操场跑道边捡到一串钥匙，共有3把，带有一个蓝色钥匙扣。', 'found', '3', '操场', '2025-03-10', '/images/key1.jpg', '13800000002', 'active', '2025-03-10 17:20:00'),
(4, 4, '捡到灰色双肩包', '在教学楼A栋201教室捡到一个灰色双肩包，内有笔记本电脑和几本书。', 'found', '4', '教学楼A栋201', '2025-03-15', '/images/bag1.jpg', '13800000003', 'active', '2025-03-15 08:45:00'),
(5, 4, '捡到一把蓝色雨伞', '在图书馆门口捡到一把蓝色折叠雨伞，伞柄有"LPO"字样。', 'found', '8', '图书馆门口', '2025-03-20', '/images/umbrella1.jpg', '13800000003', 'active', '2025-03-20 19:10:00'),
(6, 2, '捡到银色保温杯', '在体育馆更衣室捡到一个银色保温杯，杯身贴有"张三"标签。', 'found', '6', '体育馆更衣室', '2025-03-25', '/images/cup1.jpg', '13800000001', 'completed', '2025-03-25 15:30:00');

-- 寻物物品
INSERT INTO items (id, user_id, title, description, type, category, location, date, images, contact, status, created_at) VALUES
(7, 3, '寻找白色耳机', '昨天在图书馆二楼丢失一副白色苹果耳机，充电仓有贴纸标记，如有捡到请联系我，谢谢！', 'lost', '1', '图书馆二楼', '2025-03-02', '/images/earphone1.jpg', '13800000002', 'active', '2025-03-02 20:00:00'),
(8, 5, '寻找校园卡', '3月8日在食堂到宿舍路上丢失校园卡一张，卡号：2024005，有捡到者请联系我。', 'lost', '2', '食堂到宿舍路段', '2025-03-08', '/images/card2.jpg', '13800000004', 'active', '2025-03-08 18:30:00'),
(9, 5, '寻找黑色钱包', '在商业街附近丢失一个黑色短款钱包，内有身份证和银行卡若干，非常重要！', 'lost', '2', '商业街', '2025-03-12', '/images/wallet1.jpg', '13800000004', 'active', '2025-03-12 14:00:00'),
(10, 3, '寻找粉色笔记本', '在自习室丢失一本粉色封面的笔记本，封面写有"学习笔记"字样，里面有重要笔记。', 'lost', '7', '自习室', '2025-03-18', '/images/notebook1.jpg', '13800000002', 'active', '2025-03-18 22:15:00'),
(11, 4, '寻找充电宝', '在图书馆丢失一个白色20000mAh充电宝，有数据线缠绕在上面。', 'lost', '1', '图书馆', '2025-03-22', '/images/powerbank1.jpg', '13800000003', 'active', '2025-03-22 11:00:00'),
(12, 2, '寻找灰色围巾', '在操场看台丢失一条灰色针织围巾，是朋友送的生日礼物，很有纪念意义。', 'lost', '5', '操场看台', '2025-03-28', '/images/scarf1.jpg', '13800000001', 'active', '2025-03-28 16:40:00');

-- 4. 插入认证记录
INSERT INTO certifications (id, user_id, real_name, student_id, campus, id_card, status, created_at) VALUES
(1, 2, '张三', '2024001', '主校区', '110101200001011234', 'approved', '2025-01-20 10:00:00'),
(2, 3, '李四', '2024002', '东校区', '110101200002021235', 'pending', '2025-02-10 14:00:00'),
(3, 4, '王五', '2024003', '西校区', '110101200003031236', 'approved', '2025-03-01 09:00:00'),
(4, 5, '赵六', '2024004', '南校区', '110101200004041237', 'rejected', '2025-03-15 11:00:00');

-- 5. 插入留言消息
INSERT INTO messages (id, sender_id, receiver_id, item_id, content, is_read, created_at) VALUES
(1, 3, 2, 1, '你好，这个手机好像是我丢的，能约个时间看看吗？', 0, '2025-03-01 14:00:00'),
(2, 4, 2, 2, '学生证是我的，请问在哪里领取？', 1, '2025-03-05 15:30:00'),
(3, 2, 4, 2, '在食堂一楼服务台，我交给工作人员了。', 1, '2025-03-05 16:00:00'),
(4, 5, 3, 7, '我好像看到过你的耳机，在图书馆二楼靠窗位置。', 0, '2025-03-03 09:20:00'),
(5, 2, 5, 8, '我捡到了一张校园卡，请联系我确认。', 0, '2025-03-09 10:00:00');

-- 6. 重置自增主键
ALTER TABLE categories AUTO_INCREMENT = 11;
ALTER TABLE users AUTO_INCREMENT = 6;
ALTER TABLE items AUTO_INCREMENT = 13;
ALTER TABLE certifications AUTO_INCREMENT = 5;
ALTER TABLE messages AUTO_INCREMENT = 6;

-- 7. 打印提示
SELECT '测试数据插入完成！' as message;
SELECT CONCAT('用户数: ', COUNT(*)) as info FROM users;
SELECT CONCAT('物品数: ', COUNT(*)) as info FROM items;
SELECT CONCAT('分类数: ', COUNT(*)) as info FROM categories;
SELECT CONCAT('认证记录: ', COUNT(*)) as info FROM certifications;
SELECT CONCAT('消息数: ', COUNT(*)) as info FROM messages;