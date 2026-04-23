-- =============================================
-- 店铺管理菜单和权限初始化 SQL
-- 执行时间: 2026-04-16
-- =============================================

-- 注意：请根据现有菜单 ID 调整以下 ID 值，避免冲突
-- 可先查询最大 ID: SELECT MAX(id) FROM sys_menu;
-- 主键 ID 从 333 开始

-- =============================================
-- 1. 业务管理目录 (type=1 表示目录)
-- =============================================
INSERT INTO `sys_menu` VALUES (333, 0, '业务管理', 1, '/biz', NULL, NULL, 'BriefcaseOutline', 5, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- =============================================
-- 2. 店铺管理菜单 (type=2 表示菜单)
-- =============================================
INSERT INTO `sys_menu` VALUES (334, 333, '店铺管理', 2, '/biz/shop', '/biz/shop/index', 'biz:shop:list', 'StorefrontOutline', 1, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- =============================================
-- 3. 店铺管理功能按钮 (type=3 表示按钮)
-- =============================================

-- 查看店铺列表
INSERT INTO `sys_menu` VALUES (335, 334, '查看店铺', 3, NULL, NULL, 'biz:shop:list', NULL, 1, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 新增店铺
INSERT INTO `sys_menu` VALUES (336, 334, '新增店铺', 3, NULL, NULL, 'biz:shop:add', NULL, 2, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 编辑店铺
INSERT INTO `sys_menu` VALUES (337, 334, '编辑店铺', 3, NULL, NULL, 'biz:shop:edit', NULL, 3, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 删除店铺
INSERT INTO `sys_menu` VALUES (338, 334, '删除店铺', 3, NULL, NULL, 'biz:shop:remove', NULL, 4, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 查询店铺详情
INSERT INTO `sys_menu` VALUES (339, 334, '查询详情', 3, NULL, NULL, 'biz:shop:query', NULL, 5, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 授权店铺
INSERT INTO `sys_menu` VALUES (340, 334, '授权店铺', 3, NULL, NULL, 'biz:shop:authorize', NULL, 6, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 激活店铺
INSERT INTO `sys_menu` VALUES (341, 334, '激活店铺', 3, NULL, NULL, 'biz:shop:activate', NULL, 7, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 同步商品
INSERT INTO `sys_menu` VALUES (342, 334, '同步商品', 3, NULL, NULL, 'biz:shop:sync', NULL, 8, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 商品管理（上架/下架）
INSERT INTO `sys_menu` VALUES (343, 334, '商品管理', 3, NULL, NULL, 'biz:shop:product', NULL, 9, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 商品上架
INSERT INTO `sys_menu` VALUES (344, 334, '商品上架', 3, NULL, NULL, 'biz:shop:product:list', NULL, 10, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 商品下架
INSERT INTO `sys_menu` VALUES (345, 334, '商品下架', 3, NULL, NULL, 'biz:shop:product:unlist', NULL, 11, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);


-- 激活码管理菜单 (type=2 表示菜单)
INSERT INTO `sys_menu` VALUES (346, 333, '激活码管理', 2, '/biz/activation-code', '/biz/activation-code/index', 'biz:activation-code:list', 'TicketOutline', 2, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- =============================================
-- 6. 激活码管理功能按钮 (type=3 表示按钮)
-- =============================================

-- 查看激活码列表
INSERT INTO `sys_menu` VALUES (347, 346, '查看激活码', 3, NULL, NULL, 'biz:activation-code:list', NULL, 1, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 新增激活码
INSERT INTO `sys_menu` VALUES (348, 346, '新增激活码', 3, NULL, NULL, 'biz:activation-code:add', NULL, 2, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 编辑激活码
INSERT INTO `sys_menu` VALUES (349, 346, '编辑激活码', 3, NULL, NULL, 'biz:activation-code:edit', NULL, 3, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 删除激活码
INSERT INTO `sys_menu` VALUES (350, 346, '删除激活码', 3, NULL, NULL, 'biz:activation-code:remove', NULL, 4, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 激活激活码
INSERT INTO `sys_menu` VALUES (351, 346, '激活激活码', 3, NULL, NULL, 'biz:activation-code:activate', NULL, 5, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- =============================================
-- 7. 抖音商品管理菜单 (type=2 表示菜单)
-- =============================================
INSERT INTO `sys_menu` VALUES (352, 333, '抖音商品', 2, '/biz/douyin-product', '/biz/douyin-product/index', 'biz:douyin-product:list', 'CartOutline', 3, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- =============================================
-- 8. 抖音商品管理功能按钮 (type=3 表示按钮)
-- =============================================

-- 查看商品列表
INSERT INTO `sys_menu` VALUES (353, 352, '查看商品', 3, NULL, NULL, 'biz:douyin-product:list', NULL, 1, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 新增商品
INSERT INTO `sys_menu` VALUES (354, 352, '新增商品', 3, NULL, NULL, 'biz:douyin-product:add', NULL, 2, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 编辑商品
INSERT INTO `sys_menu` VALUES (355, 352, '编辑商品', 3, NULL, NULL, 'biz:douyin-product:edit', NULL, 3, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 删除商品
INSERT INTO `sys_menu` VALUES (356, 352, '删除商品', 3, NULL, NULL, 'biz:douyin-product:remove', NULL, 4, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 查询商品详情
INSERT INTO `sys_menu` VALUES (357, 352, '查询详情', 3, NULL, NULL, 'biz:douyin-product:query', NULL, 5, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 商品上架
INSERT INTO `sys_menu` VALUES (358, 352, '商品上架', 3, NULL, NULL, 'biz:douyin-product:edit', NULL, 6, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 商品下架
INSERT INTO `sys_menu` VALUES (359, 352, '商品下架', 3, NULL, NULL, 'biz:douyin-product:edit', NULL, 7, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 提交审核
INSERT INTO `sys_menu` VALUES (360, 352, '提交审核', 3, NULL, NULL, 'biz:douyin-product:edit', NULL, 8, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 同步商品
INSERT INTO `sys_menu` VALUES (361, 352, '同步商品', 3, NULL, NULL, 'biz:douyin-product:sync', NULL, 9, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);


-- =============================================
-- 9. 抖音运费模板管理菜单 (type=2 表示菜单)
-- =============================================
INSERT INTO `sys_menu` VALUES (362, 333, '运费模板', 2, '/biz/douyin-freight', '/biz/douyin-freight/index', 'biz:douyin-freight:list', 'VehicleOutline', 4, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- =============================================
-- 10. 抖音运费模板管理功能按钮 (type=3 表示按钮)
-- =============================================

-- 查看模板列表
INSERT INTO `sys_menu` VALUES (363, 362, '查看模板', 3, NULL, NULL, 'biz:douyin-freight:list', NULL, 1, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 新增模板
INSERT INTO `sys_menu` VALUES (364, 362, '新增模板', 3, NULL, NULL, 'biz:douyin-freight:add', NULL, 2, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 编辑模板
INSERT INTO `sys_menu` VALUES (365, 362, '编辑模板', 3, NULL, NULL, 'biz:douyin-freight:edit', NULL, 3, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 删除模板
INSERT INTO `sys_menu` VALUES (366, 362, '删除模板', 3, NULL, NULL, 'biz:douyin-freight:remove', NULL, 4, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 查询模板详情
INSERT INTO `sys_menu` VALUES (367, 362, '查询详情', 3, NULL, NULL, 'biz:douyin-freight:query', NULL, 5, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 同步模板
INSERT INTO `sys_menu` VALUES (368, 362, '同步模板', 3, NULL, NULL, 'biz:douyin-freight:sync', NULL, 6, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);


