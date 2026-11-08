-- =====================================================================
-- 20261108_01 检查项目及用药：补齐「操作」列按钮的角色授权
--
-- 现象：非超级管理员登录后，检查项目及用药的「操作」列是空的，
--       新增 / 新增下级 / 修改 / 删除 / 导出 一个按钮都不出现。
-- 原因：页面上按钮都写了 v-hasPermi="['cat:checkitem:xxx']"，
--       但菜单 2190 的 4 个按钮权限（2192 新增 / 2193 修改 / 2194 删除 / 2195 导出）
--       自建库以来只存在 sys_menu 里，从没写进 sys_role_menu 授权给角色。
--       超管（admin）因为走后端 *:*:* 全量权限，所以一直看不出问题。
--
-- 授权口径：本页是「开单目录」这类基础资料，与同类模块保持一致 ——
--     cat:material:*（物资台账）、cat:base:*（基础信息）、
--     cat:feecategory:*（费用类别）、cat:tag:*（标签）均是「超管 + 管理员(1,4)」。
--     医生/助理只负责开单使用目录，不维护目录，故不授予。
--   若以后希望医生也能加项目，把下面 2192/2193 的 (2, ...) 补上即可。
--
-- 幂等：sys_role_menu 主键 (role_id, menu_id)，用 INSERT IGNORE，可重复执行。
-- 生效：改的是权限数据，已登录的用户需重新登录（或清 Redis 里的登录态）才会刷新按钮。
-- =====================================================================

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
  (1, 2192), (4, 2192),   -- 项目新增
  (1, 2193), (4, 2193),   -- 项目修改
  (1, 2194), (4, 2194),   -- 项目删除
  (1, 2195), (4, 2195);   -- 项目导出

-- 校验：2192~2195 每个按钮都应有 2 行（role_id 1、4）
SELECT m.menu_id,
       m.menu_name,
       m.perms,
       GROUP_CONCAT(rm.role_id ORDER BY rm.role_id) AS roles
  FROM sys_menu m
  LEFT JOIN sys_role_menu rm ON rm.menu_id = m.menu_id
 WHERE m.menu_id BETWEEN 2192 AND 2195
 GROUP BY m.menu_id, m.menu_name, m.perms
 ORDER BY m.menu_id;
