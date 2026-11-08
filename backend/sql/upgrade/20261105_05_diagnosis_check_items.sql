-- ============================================================
-- 变更：站内诊断「检查项目 / 用药」自动化（联动库存与各台账）
-- 作者/日期：workbuddy / 2026-11-05
--
-- 背景：
--   医生在站内诊断里开检查项目和药品，原来是手打一段文字，
--   物资库存不会动、用药/疫苗/驱虫台账也不会有记录，两边对不上。
--   现在改成从维护好的目录里选，选完自动：
--     1) 关联了物资的项目 → 生成一条出库记录并扣减库存；
--     2) 用药类 → 自动写用药记录；疫苗类 → 疫苗台账；驱虫类 → 驱虫台账；
--     3) 出入库与三个台账都记「来源」，区分手动录入还是站内诊断开具。
--
-- 幂等：建表用 IF NOT EXISTS；加字段先查 information_schema；
--       菜单/初始目录用固定主键 + INSERT ... WHERE NOT EXISTS。
-- 执行：mysql -h127.0.0.1 -uroot -p cat_rescue < 20261105_05_diagnosis_check_items.sql
-- ============================================================

USE cat_rescue;

-- ------------------------------------------------------------
-- 1. 检查项目 / 用药目录（树形，仿部门管理）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cat_check_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '目录id',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父级id（0=顶级）',
  `ancestors` varchar(500) DEFAULT '' COMMENT '祖级列表',
  `item_name` varchar(100) NOT NULL COMMENT '名称',
  `item_type` char(1) NOT NULL DEFAULT '1' COMMENT '类型（1检查项目 2用药 3疫苗 4驱虫）',
  `material_id` bigint DEFAULT NULL COMMENT '关联物资id（关联后开具自动出库并扣库存）',
  `unit` varchar(20) DEFAULT '' COMMENT '单位（片/支/份等）',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '单价',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`item_id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='检查项目/用药目录';

-- ------------------------------------------------------------
-- 2. 诊断明细（一次诊断开了哪些项目、哪些药）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cat_diagnosis_item` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细id',
  `diagnosis_id` bigint NOT NULL COMMENT '诊断记录id',
  `cat_id` bigint DEFAULT NULL COMMENT '猫咪id（冗余，便于按猫查）',
  `item_id` bigint DEFAULT NULL COMMENT '目录项id（目录被删也保留名称快照）',
  `item_name` varchar(100) NOT NULL COMMENT '项目/药品名称（快照）',
  `item_type` char(1) DEFAULT '1' COMMENT '类型（1检查项目 2用药 3疫苗 4驱虫）',
  `material_id` bigint DEFAULT NULL COMMENT '关联物资id',
  `quantity` decimal(10,2) DEFAULT '1.00' COMMENT '数量',
  `unit` varchar(20) DEFAULT '' COMMENT '单位',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '单价',
  `amount` decimal(10,2) DEFAULT '0.00' COMMENT '金额',
  `usage_note` varchar(200) DEFAULT '' COMMENT '用法用量/说明',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`detail_id`),
  KEY `idx_diagnosis` (`diagnosis_id`),
  KEY `idx_cat` (`cat_id`),
  KEY `idx_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='站内诊断明细（检查项目/用药）';

-- ------------------------------------------------------------
-- 3. 四个台账增加「来源」标识
--    source_type：1 手动录入，2 站内诊断开具
--    diagnosis_id：来自哪张诊断单（手动录入为 NULL）
-- ------------------------------------------------------------
SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_stock_record' AND COLUMN_NAME = 'source_type') = 0,
    'ALTER TABLE `cat_stock_record` ADD COLUMN `source_type` char(1) DEFAULT ''1'' COMMENT ''来源（1手动录入 2站内诊断）'' AFTER `operator`, ADD COLUMN `diagnosis_id` bigint DEFAULT NULL COMMENT ''来源诊断id'' AFTER `source_type`, ADD COLUMN `cat_id` bigint DEFAULT NULL COMMENT ''关联猫咪id'' AFTER `diagnosis_id`',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_medication' AND COLUMN_NAME = 'source_type') = 0,
    'ALTER TABLE `cat_medication` ADD COLUMN `source_type` char(1) DEFAULT ''1'' COMMENT ''来源（1手动录入 2站内诊断）'' AFTER `adverse_reaction`, ADD COLUMN `diagnosis_id` bigint DEFAULT NULL COMMENT ''来源诊断id'' AFTER `source_type`',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_vaccine' AND COLUMN_NAME = 'source_type') = 0,
    'ALTER TABLE `cat_vaccine` ADD COLUMN `source_type` char(1) DEFAULT ''1'' COMMENT ''来源（1手动录入 2站内诊断）'' AFTER `responsible`, ADD COLUMN `diagnosis_id` bigint DEFAULT NULL COMMENT ''来源诊断id'' AFTER `source_type`',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_deworm' AND COLUMN_NAME = 'source_type') = 0,
    'ALTER TABLE `cat_deworm` ADD COLUMN `source_type` char(1) DEFAULT ''1'' COMMENT ''来源（1手动录入 2站内诊断）'' AFTER `deworm_date`, ADD COLUMN `diagnosis_id` bigint DEFAULT NULL COMMENT ''来源诊断id'' AFTER `source_type`',
    'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 索引也幂等补齐（按来源筛选会用到）
SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_stock_record' AND INDEX_NAME = 'idx_diagnosis') = 0,
    'ALTER TABLE `cat_stock_record` ADD KEY `idx_diagnosis` (`diagnosis_id`)', 'SELECT 1'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 4. 菜单：医疗病历 → 检查项目
-- ------------------------------------------------------------
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
SELECT 2190,'检查项目',2003,7,'checkitem','cat/checkitem/index','','CatCheckItem',1,0,'C','0','0','cat:checkitem:list','tree','admin',NOW(),'',NULL,'检查项目/用药目录维护'
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2190);

INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
SELECT 2191,'项目查询',2190,1,'','','','',1,0,'F','0','0','cat:checkitem:query','#','admin',NOW(),'',NULL,''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2191);
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
SELECT 2192,'项目新增',2190,2,'','','','',1,0,'F','0','0','cat:checkitem:add','#','admin',NOW(),'',NULL,''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2192);
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
SELECT 2193,'项目修改',2190,3,'','','','',1,0,'F','0','0','cat:checkitem:edit','#','admin',NOW(),'',NULL,''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2193);
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
SELECT 2194,'项目删除',2190,4,'','','','',1,0,'F','0','0','cat:checkitem:remove','#','admin',NOW(),'',NULL,''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2194);
INSERT INTO `sys_menu` (`menu_id`,`menu_name`,`parent_id`,`order_num`,`path`,`component`,`query`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`)
SELECT 2195,'项目导出',2190,5,'','','','',1,0,'F','0','0','cat:checkitem:export','#','admin',NOW(),'',NULL,''
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2195);

-- 已有「医生 / 助理 / 管理员」角色先开只读，避免上线后看不到菜单（可按需再收紧）
INSERT INTO `sys_role_menu` (`role_id`,`menu_id`)
SELECT r.`role_id`, m.`menu_id`
FROM `sys_role` r
JOIN `sys_menu` m ON m.`menu_id` IN (2190,2191)
WHERE r.`role_key` IN ('doctor','assistant','manager')
  AND r.`del_flag` = '0'
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = r.`role_id` AND rm.`menu_id` = m.`menu_id`);

-- ------------------------------------------------------------
-- 5. 初始目录（可按需在页面上增删改）
--    3001 检查项目（顶级）
--      3002 基础体格检查 → 体重体温 / 心肺听诊 / 体表触诊
--      3006 血检 → 血常规 / 生化 / 病毒筛查（关联猫瘟抗原测试板）
--    3010 用药（顶级）→ 阿莫西林 / 皮肤抗真菌喷剂 / 大宠爱驱虫滴剂
-- ------------------------------------------------------------
INSERT INTO `cat_check_item` (`item_id`,`parent_id`,`ancestors`,`item_name`,`item_type`,`material_id`,`unit`,`price`,`order_num`,`status`,`create_by`,`create_time`,`del_flag`,`remark`)
SELECT * FROM (
  SELECT 3001 AS a,0 AS b,'0' AS c,'检查项目' AS d,'1' AS e,NULL AS f,'' AS g,0.00 AS h,1 AS i,'0' AS j,'admin' AS k,NOW() AS l,'0' AS m,'医生开具的检查项目目录' AS n
  UNION ALL SELECT 3002,3001,'0,3001','基础体格检查','1',NULL,'',0.00,1,'0','admin',NOW(),'0',''
  UNION ALL SELECT 3003,3002,'0,3001,3002','体重体温','1',NULL,'次',0.00,1,'0','admin',NOW(),'0',''
  UNION ALL SELECT 3004,3002,'0,3001,3002','心肺听诊','1',NULL,'次',0.00,2,'0','admin',NOW(),'0',''
  UNION ALL SELECT 3005,3002,'0,3001,3002','体表触诊','1',NULL,'次',0.00,3,'0','admin',NOW(),'0',''
  UNION ALL SELECT 3006,3001,'0,3001','血检','1',NULL,'',0.00,2,'0','admin',NOW(),'0',''
  UNION ALL SELECT 3007,3006,'0,3001,3006','血常规','1',NULL,'次',0.00,1,'0','admin',NOW(),'0',''
  UNION ALL SELECT 3008,3006,'0,3001,3006','生化','1',NULL,'次',0.00,2,'0','admin',NOW(),'0',''
  UNION ALL SELECT 3009,3006,'0,3001,3006','病毒筛查','1',106,'份',0.00,3,'0','admin',NOW(),'0','开具即出库：猫瘟抗原测试板'
  UNION ALL SELECT 3010,0,'0','用药','2',NULL,'',0.00,2,'0','admin',NOW(),'0','医生开具的药品目录'
  UNION ALL SELECT 3011,3010,'0,3010','阿莫西林克拉维酸钾','2',100,'盒',0.00,1,'0','admin',NOW(),'0',''
  UNION ALL SELECT 3012,3010,'0,3010','皮肤抗真菌喷剂','2',107,'瓶',0.00,2,'0','admin',NOW(),'0',''
  UNION ALL SELECT 3013,3010,'0,3010','大宠爱驱虫滴剂','4',101,'支',0.00,3,'0','admin',NOW(),'0','驱虫类：开具同时写驱虫台账'
) t
WHERE NOT EXISTS (SELECT 1 FROM `cat_check_item` WHERE `item_id` = 3001);

-- ------------------------------------------------------------
-- 6. 校验
-- ------------------------------------------------------------
SELECT 'cat_check_item' AS t, COUNT(*) AS cnt FROM `cat_check_item` WHERE `del_flag` = '0'
UNION ALL SELECT 'cat_diagnosis_item', COUNT(*) FROM `cat_diagnosis_item`
UNION ALL SELECT 'menu_2190', COUNT(*) FROM `sys_menu` WHERE `menu_id` = 2190;
