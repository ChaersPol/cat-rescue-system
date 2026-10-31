-- ============================================================
-- 变更：一级菜单序号补齐空洞（8/9 两个位次随菜单下移腾空）
-- 影响表：sys_menu
-- 可回滚：是（见文末）
-- 作者/日期：workbuddy / 2026-10-31
-- 说明：
--   1) 20261031_02 把「提醒通知」「系统配置」从顶级收进「系统管理」后，
--      顶级排序变成 2..7 然后直接跳到 10/11/12，界面上看是断号的。
--   2) 本脚本把「系统管理」「系统工具」「系统监控」依次前移为 8/9/10。
--   3) 只动 order_num，不动 parent_id / path / 权限，任何菜单都不会消失。
--   4) 幂等：带上「原值」作为条件，已执行过再跑不会命中任何行。
-- ============================================================

USE cat_rescue;

-- 系统管理：10 → 8
UPDATE `sys_menu` SET `order_num` = 8, `update_by` = 'admin', `update_time` = NOW()
 WHERE `menu_id` = 1 AND `parent_id` = 0 AND `order_num` = 10;

-- 系统工具：11 → 9
UPDATE `sys_menu` SET `order_num` = 9, `update_by` = 'admin', `update_time` = NOW()
 WHERE `menu_id` = 3 AND `parent_id` = 0 AND `order_num` = 11;

-- 系统监控：12 → 10
UPDATE `sys_menu` SET `order_num` = 10, `update_by` = 'admin', `update_time` = NOW()
 WHERE `menu_id` = 2 AND `parent_id` = 0 AND `order_num` = 12;

-- ------------------------------------------------------------
-- 校验（期望：一级菜单 order_num 为 2..10 连续，无重复）
-- ------------------------------------------------------------
-- SELECT order_num, menu_name FROM sys_menu WHERE parent_id = 0 ORDER BY order_num;

-- ============================================================
-- 回滚
-- ============================================================
-- UPDATE sys_menu SET order_num = 10 WHERE menu_id = 1;
-- UPDATE sys_menu SET order_num = 11 WHERE menu_id = 3;
-- UPDATE sys_menu SET order_num = 12 WHERE menu_id = 2;
