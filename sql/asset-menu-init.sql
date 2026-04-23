-- =============================================
-- 资产管理菜单和权限初始化 SQL
-- 执行时间: 2026-04-23
-- =============================================

-- 注意：请先查询现有最大菜单ID，避免冲突
-- SELECT MAX(id) FROM sys_menu;
-- 此脚本假设从 369 开始，如果有冲突请调整ID值

-- =============================================
-- 列顺序参考 (根据 menu-init.sql 的 INSERT 语句)
-- =============================================
-- 值顺序: 
-- id, parent_id, name, type, path, component, permission, icon, sort, 
-- visible, status, is_frame, create_time, update_time, create_by, update_by, deleted

-- =============================================
-- 1. 资产管理菜单 (type=2 表示菜单)
-- 父级：业务管理 (id=333)
-- =============================================

-- 资产管理主菜单
INSERT INTO `sys_menu` VALUES (369, 333, '资产管理', 2, '/biz/asset', '/biz/asset/index', 'biz:asset:list', 'CubeOutline', 5, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- =============================================
-- 2. 资产管理功能按钮 (type=3 表示按钮)
-- 父级：资产管理 (id=369)
-- =============================================

-- 查看资产列表
INSERT INTO `sys_menu` VALUES (370, 369, '查看资产', 3, NULL, NULL, 'biz:asset:list', NULL, 1, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 新增资产
INSERT INTO `sys_menu` VALUES (371, 369, '新增资产', 3, NULL, NULL, 'biz:asset:add', NULL, 2, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 编辑资产
INSERT INTO `sys_menu` VALUES (372, 369, '编辑资产', 3, NULL, NULL, 'biz:asset:edit', NULL, 3, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 删除资产
INSERT INTO `sys_menu` VALUES (373, 369, '删除资产', 3, NULL, NULL, 'biz:asset:remove', NULL, 4, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 查询资产详情
INSERT INTO `sys_menu` VALUES (374, 369, '查询详情', 3, NULL, NULL, 'biz:asset:query', NULL, 5, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 资产领用
INSERT INTO `sys_menu` VALUES (375, 369, '资产领用', 3, NULL, NULL, 'biz:asset:borrow', NULL, 6, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- 资产归还
INSERT INTO `sys_menu` VALUES (376, 369, '资产归还', 3, NULL, NULL, 'biz:asset:return', NULL, 7, 1, 1, 0, NOW(), NOW(), NULL, NULL, 0);

-- =============================================
-- 3. 给 admin 角色分配资产管理权限
-- =============================================

-- 请先查询 admin 角色的实际 ID
-- SELECT id, role_code, role_name FROM sys_role WHERE deleted = 0;

-- 假设 admin 角色的 ID 是 1（默认值）
-- 插入角色菜单关联

-- 先删除已存在的关联（避免重复）
-- DELETE FROM sys_role_menu WHERE role_id = 1 AND menu_id IN (369, 370, 371, 372, 373, 374, 375, 376);

-- 插入新的关联
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES 
(1, 369),
(1, 370),
(1, 371),
(1, 372),
(1, 373),
(1, 374),
(1, 375),
(1, 376);

-- =============================================
-- 4. 验证 SQL
-- =============================================

-- 验证菜单是否插入成功
-- SELECT * FROM sys_menu WHERE id >= 369 AND deleted = 0;

-- 验证角色菜单关联是否成功
-- SELECT * FROM sys_role_menu WHERE role_id = 1 AND menu_id >= 369;

-- 验证 admin 用户是否拥有这些权限
-- SELECT DISTINCT m.permission 
-- FROM sys_menu m 
-- INNER JOIN sys_role_menu rm ON m.id = rm.menu_id 
-- INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id 
-- WHERE ur.user_id = 1 AND m.deleted = 0 AND m.permission IS NOT NULL;

-- =============================================
-- 5. 注意事项
-- =============================================
-- 1. 执行完此 SQL 后，用户需要重新登录才能获取新的权限
-- 2. 权限缓存在 SaSession 中，重新登录会刷新缓存
-- 3. 如果 admin 角色的 ID 不是 1，请修改上面的 role_id 值
-- 4. 如果菜单 ID 有冲突，请先查询最大 ID 后调整
-- 5. 列顺序参考: id, parent_id, name, type, path, component, permission, icon, sort, visible, status, is_frame, create_time, update_time, create_by, update_by, deleted
