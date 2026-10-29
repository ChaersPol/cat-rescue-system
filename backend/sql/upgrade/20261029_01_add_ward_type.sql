-- ============================================================
-- 变更：新增「病房类型」字典表 cat_ward_type，并给 cat_ward 增加 ward_type_id
--       同时新增菜单「基础信息 → 病房类型」及其按钮权限
-- 影响表：cat_ward_type（新建）、cat_ward（加字段）、sys_menu、sys_role_menu
-- 可回滚：是
--   DELETE FROM sys_role_menu WHERE menu_id IN (2180,2181,2182,2183,2184,2185);
--   DELETE FROM sys_menu      WHERE menu_id IN (2180,2181,2182,2183,2184,2185);
--   UPDATE sys_menu SET order_num = 3 WHERE menu_id = 2030;
--   ALTER TABLE `cat_ward` DROP COLUMN `ward_type_id`;
--   DROP TABLE `cat_ward_type`;
-- 作者/日期：workbuddy / 2026-10-29
-- 说明：
--   1) cat_ward.ward_type（名称）**保留不动**，导出、历史数据、老页面继续可用；
--      ward_type_id 是新增的规范化外键，两者由后端保持一致。
--   2) 本脚本幂等，可重复执行。
-- ============================================================

USE cat_rescue;

-- ------------------------------------------------------------
-- 1. 病房类型表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cat_ward_type` (
  `type_id` bigint NOT NULL AUTO_INCREMENT COMMENT '病房类型id',
  `type_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '类型名称',
  `tag_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'primary' COMMENT '标签颜色（primary/success/info/warning/danger）',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  PRIMARY KEY (`type_id`),
  KEY `idx_ward_type_name` (`type_name`)
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='病房类型表';

-- ------------------------------------------------------------
-- 2. cat_ward 增加 ward_type_id（MySQL 8 没有 ADD COLUMN IF NOT EXISTS）
-- ------------------------------------------------------------
SET @exist := (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'cat_ward'
                 AND COLUMN_NAME = 'ward_type_id');
SET @sql := IF(@exist = 0,
  'ALTER TABLE `cat_ward` ADD COLUMN `ward_type_id` bigint DEFAULT NULL COMMENT ''病房类型id'' AFTER `ward_name`',
  'SELECT ''ward_type_id 已存在，跳过'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = DATABASE()
                 AND TABLE_NAME = 'cat_ward'
                 AND INDEX_NAME = 'idx_ward_type_id');
SET @sql := IF(@exist = 0,
  'ALTER TABLE `cat_ward` ADD INDEX `idx_ward_type_id` (`ward_type_id`)',
  'SELECT ''idx_ward_type_id 已存在，跳过'' AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 3. 初始化类型数据（按现有 ward_type 取值整理，配色用于列表标签）
-- ------------------------------------------------------------
INSERT IGNORE INTO `cat_ward_type`
  (`type_id`, `type_name`, `tag_type`, `order_num`, `status`, `remark`, `create_by`, `create_time`)
VALUES
  (100, '普通病房', 'primary', 1, '0', '健康状况稳定、无需隔离的猫咪', 'admin', now()),
  (101, '隔离病房', 'danger',  2, '0', '新入站观察、传染病隔离，单独进出与消毒', 'admin', now()),
  (102, '术后病房', 'warning', 3, '0', '绝育或手术后留观，需记录体温与切口情况', 'admin', now()),
  (103, '治疗病房', 'info',    4, '0', '正在接受治疗、需每日用药的猫咪', 'admin', now()),
  (104, '幼猫病房', 'success', 5, '0', '断奶前后幼猫，保温与少量多餐', 'admin', now()),
  (105, '康复区',   'info',    6, '0', '术后或病后恢复期，可自由活动的区域', 'admin', now());

-- ------------------------------------------------------------
-- 4. 回填 cat_ward.ward_type_id（按名称匹配，只补空的）
-- ------------------------------------------------------------
UPDATE `cat_ward` w
  JOIN `cat_ward_type` t ON t.`type_name` = w.`ward_type` AND t.`del_flag` = '0'
   SET w.`ward_type_id` = t.`type_id`
 WHERE w.`ward_type_id` IS NULL
   AND w.`ward_type` IS NOT NULL
   AND w.`ward_type` <> '';

-- ------------------------------------------------------------
-- 5. 菜单：基础信息 → 病房类型
--    列表/查询给全部角色（病房管理页也要读这个字典），增删改导出只给超管 + 管理员
-- ------------------------------------------------------------
INSERT IGNORE INTO `sys_menu`
  (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES
  (2180, '病房类型', 2001, 3, 'wardtype', 'cat/wardtype/index', '', 1, 0, 'C', '0', '0', 'cat:wardtype:list', 'list', 'admin', now(), '病房类型字典'),
  (2181, '病房类型查询', 2180, 1, '', '', '', 1, 0, 'F', '0', '0', 'cat:wardtype:query',  '#', 'admin', now(), ''),
  (2182, '病房类型新增', 2180, 2, '', '', '', 1, 0, 'F', '0', '0', 'cat:wardtype:add',    '#', 'admin', now(), ''),
  (2183, '病房类型修改', 2180, 3, '', '', '', 1, 0, 'F', '0', '0', 'cat:wardtype:edit',   '#', 'admin', now(), ''),
  (2184, '病房类型删除', 2180, 4, '', '', '', 1, 0, 'F', '0', '0', 'cat:wardtype:remove', '#', 'admin', now(), ''),
  (2185, '病房类型导出', 2180, 5, '', '', '', 1, 0, 'F', '0', '0', 'cat:wardtype:export', '#', 'admin', now(), '');

-- 标签管理原本在 order_num = 3，给病房类型让位
UPDATE `sys_menu` SET `order_num` = 4 WHERE `menu_id` = 2030 AND `order_num` = 3;

-- 角色授权：1 超管 / 2 医生 / 3 助理 / 4 管理员
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (1, 2180), (2, 2180), (3, 2180), (4, 2180),
  (1, 2181), (2, 2181), (3, 2181), (4, 2181),
  (1, 2182), (4, 2182),
  (1, 2183), (4, 2183),
  (1, 2184), (4, 2184),
  (1, 2185), (4, 2185);

-- ------------------------------------------------------------
-- 校验
-- ------------------------------------------------------------
-- SELECT COUNT(*) FROM cat_ward_type WHERE del_flag = '0';                                  -- 应为 6
-- SELECT COUNT(*) FROM cat_ward WHERE ward_type <> '' AND ward_type_id IS NULL;             -- 应为 0
-- SELECT COUNT(*) FROM sys_menu WHERE menu_id BETWEEN 2180 AND 2185;                        -- 应为 6
