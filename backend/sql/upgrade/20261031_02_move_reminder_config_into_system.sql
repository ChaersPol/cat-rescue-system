-- ============================================================
-- 变更：「提醒通知」「系统配置」两个菜单收进「系统管理」目录
-- 影响表：sys_menu
-- 可回滚：是（见文末）
-- 作者/日期：workbuddy / 2026-10-31
-- 说明：
--   1) 原本两者是顶级菜单（parent_id=0，order_num 8/9），侧边栏 12 个一级菜单太长。
--      现挂到「系统管理」(menu_id=1) 下，排在既有子菜单之后（10、11）。
--   2) 「系统配置」原 path='config'，与「系统管理」下已有的「参数设置」
--      (menu_id=106，同样 path='config'）会拼成同一个前端路由 /system/config，
--      故改名为 path='cat-config'。route_name 保持 CatConfig 不变，
--      组件路径仍为 cat/config/index，权限标识 cat:config:* 全部不动。
--   3) 备份子按钮（F 类型）parent_id 指向 2125/2140，不受影响。
--   4) 本脚本幂等：已执行过再跑一次不会命中 WHERE，不会改动任何行。
-- ============================================================

USE cat_rescue;

-- ------------------------------------------------------------
-- 1. 提醒通知 2125：顶级 → 系统管理，顺位 10
-- ------------------------------------------------------------
UPDATE `sys_menu`
   SET `parent_id` = 1,
       `order_num` = 10,
       `update_by` = 'admin',
       `update_time` = NOW()
 WHERE `menu_id` = 2125
   AND `parent_id` = 0
   AND `path` = 'reminder';

-- ------------------------------------------------------------
-- 2. 系统配置 2140：顶级 → 系统管理，顺位 11，path 避开 /system/config
-- ------------------------------------------------------------
UPDATE `sys_menu`
   SET `parent_id` = 1,
       `order_num` = 11,
       `path` = 'cat-config',
       `update_by` = 'admin',
       `update_time` = NOW()
 WHERE `menu_id` = 2140
   AND `parent_id` = 0
   AND `path` = 'config';

-- ------------------------------------------------------------
-- 校验（期望：2125/2140 parent_id=1；系统管理下 path 无重复）
-- ------------------------------------------------------------
-- SELECT menu_id, menu_name, parent_id, order_num, path, route_name
--   FROM sys_menu WHERE parent_id = 1 ORDER BY order_num;
-- SELECT path, COUNT(*) c FROM sys_menu WHERE parent_id = 1
--  GROUP BY path HAVING c > 1;   -- 应为空

-- ============================================================
-- 回滚（恢复为顶级菜单，原顺位与 path）
-- ============================================================
-- UPDATE sys_menu SET parent_id = 0, order_num = 8 WHERE menu_id = 2125;
-- UPDATE sys_menu SET parent_id = 0, order_num = 9, path = 'config' WHERE menu_id = 2140;
