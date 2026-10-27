-- ----------------------------------------------------------------------------
-- 站内诊断记录：新增「更正备注」字段
-- 对应需求文档 15：历史病历只允许新增补充，禁止直接修改旧诊断，
-- 记录错误时新增一条【更正备注】，原始记录保留。
-- 脚本可重复执行。
-- ----------------------------------------------------------------------------

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS add_diagnosis_correction_columns;

DELIMITER $$
CREATE PROCEDURE add_diagnosis_correction_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_diagnosis'
                     AND COLUMN_NAME = 'correction_note') THEN
        ALTER TABLE cat_diagnosis ADD COLUMN correction_note VARCHAR(1000) NULL COMMENT '更正备注' AFTER check_photos;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_diagnosis'
                     AND COLUMN_NAME = 'correction_by') THEN
        ALTER TABLE cat_diagnosis ADD COLUMN correction_by VARCHAR(64) NULL COMMENT '更正人' AFTER correction_note;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.COLUMNS
                   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_diagnosis'
                     AND COLUMN_NAME = 'correction_time') THEN
        ALTER TABLE cat_diagnosis ADD COLUMN correction_time DATETIME NULL COMMENT '更正时间' AFTER correction_by;
    END IF;
END$$
DELIMITER ;

CALL add_diagnosis_correction_columns();
DROP PROCEDURE IF EXISTS add_diagnosis_correction_columns;
