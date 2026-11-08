-- =====================================================================
-- 20261106_01 领养回访：加「回访已结束」开关 + 超期自动作废
-- 影响表：cat_adoption（加 3 个字段）、sys_dict_data（加 2 个状态）、cat_config（加 1 项）
-- 能否回滚：能，回滚语句见文件末尾（注释形式，需要时手工执行）
--
-- 背景（2026-11-06 与救助站确认）：
--   1. 回访不必一直挂着。领养登记自动生成 3 天 / 1 个月 / 3 个月三条计划后，
--      这几条不登记就永远躺在「待回访」里、提醒一直响，没有"这猫不用再回访了"的开关。
--   2. 领养一年后系统还在提醒，那时回访已没意义。
--      规则：计划日期超过 3 个月仍未登记的回访，自动置为「已作废」，不再提醒。
--   3. 开关要能重新打开（领养人反馈有问题，想继续回访）。
--
-- 幂等：字段按 information_schema 判存后再 ALTER；字典按 dict_type + dict_value 判存；
--       配置按 config_key 判存，可重复执行。
-- =====================================================================

USE cat_rescue;

-- ---------------------------------------------------------------------
-- 1. 领养记录加「回访是否已结束」标记
--    visit_closed  0 表示回访进行中（默认），1 表示已结束
--    重新打开时把 1 改回 0，并清空时间与人，保留痕迹靠 cat_visit 状态区分
-- ---------------------------------------------------------------------
SET @ddl := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE cat_adoption ADD COLUMN visit_closed CHAR(1) NOT NULL DEFAULT ''0'' COMMENT ''回访是否已结束（0进行中 1已结束）''',
    'DO 0')
  FROM information_schema.columns
 WHERE table_schema = DATABASE() AND table_name = 'cat_adoption' AND column_name = 'visit_closed');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE cat_adoption ADD COLUMN visit_closed_time DATETIME DEFAULT NULL COMMENT ''回访结束时间''',
    'DO 0')
  FROM information_schema.columns
 WHERE table_schema = DATABASE() AND table_name = 'cat_adoption' AND column_name = 'visit_closed_time');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE cat_adoption ADD COLUMN visit_closed_by VARCHAR(64) DEFAULT NULL COMMENT ''回访结束操作人''',
    'DO 0')
  FROM information_schema.columns
 WHERE table_schema = DATABASE() AND table_name = 'cat_adoption' AND column_name = 'visit_closed_by');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 2. 回访状态增加两种：3 已作废（超期自动）、4 已结束（人工关闭回访）
--    两者都不再生成提醒，区别只在列表里能看出是谁关掉的
-- ---------------------------------------------------------------------
INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 4, '已作废', '3', 'cat_visit_status', '', 'info',
       'N', '0', 'admin', NOW(), '计划日期超过保留月数仍未登记，系统自动作废'
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM sys_dict_data
        WHERE dict_type = 'cat_visit_status'
          AND dict_value = '3'
       );

INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 5, '已结束', '4', 'cat_visit_status', '', 'info',
       'N', '0', 'admin', NOW(), '领养记录勾了「回访已结束」，不再回访'
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM sys_dict_data
        WHERE dict_type = 'cat_visit_status'
          AND dict_value = '4'
       );

-- ---------------------------------------------------------------------
-- 3. 超期作废的保留月数：计划日期超过 N 个月还没登记就不再提醒
-- ---------------------------------------------------------------------
INSERT INTO cat_config
    (config_key, config_value, config_name, remark, create_by, create_time)
SELECT 'visit.void.months', '3', '回访超期作废月数',
       '待回访计划超过该月数（按月对月计算）仍未登记，系统自动置为已作废并清除提醒',
       'admin', NOW()
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM cat_config WHERE config_key = 'visit.void.months'
       );

-- ---------------------------------------------------------------------
-- 校验：应输出 3 个新字段、5 个回访状态、作废月数 3
-- ---------------------------------------------------------------------
SELECT column_name, column_type, column_default, column_comment
  FROM information_schema.columns
 WHERE table_schema = DATABASE() AND table_name = 'cat_adoption'
   AND column_name LIKE 'visit_closed%';

SELECT dict_value, dict_label, dict_sort, list_class
  FROM sys_dict_data
 WHERE dict_type = 'cat_visit_status'
 ORDER BY dict_sort;

SELECT config_key, config_value, config_name
  FROM cat_config
 WHERE config_key = 'visit.void.months';

-- ---------------------------------------------------------------------
-- 回滚（需要时手工执行）
-- ALTER TABLE cat_adoption DROP COLUMN visit_closed_by, DROP COLUMN visit_closed_time, DROP COLUMN visit_closed;
-- DELETE FROM sys_dict_data WHERE dict_type = 'cat_visit_status' AND dict_value IN ('3', '4');
-- DELETE FROM cat_config WHERE config_key = 'visit.void.months';
-- ---------------------------------------------------------------------
