-- ============================================================
-- 变更：提醒支持「用药冲突提示」与「推迟 N 天再提醒」
-- 影响表：cat_reminder、cat_config
-- 可回滚：是（见文末）
-- 作者/日期：workbuddy / 2026-11-01
-- 说明：
--   1) 业务场景：猫咪到了该打第 N 针疫苗的日期，但该猫正在用药期，
--      按兽医要求不能接种，只能往后推迟几天；推迟后需要到期再提醒一次。
--   2) cat_reminder 增加 origin_date（首次推迟前的提醒日期）与
--      defer_count（累计推迟次数），推迟只改 remind_date，原始到期日不会丢。
--   3) cat_config 增加两项配置：
--      reminder.medication.check.days —— 判断「用药期间」的回溯天数（默认 7）
--      reminder.defer.default.days    —— 推迟按钮的默认天数（默认 7）
--   4) 幂等：字段用「先判断 information_schema 再 ALTER」的方式，配置用
--      INSERT ... ON DUPLICATE KEY UPDATE，重复执行不会报错。
-- ============================================================

USE cat_rescue;

-- ------------------------------------------------------------
-- 1. cat_reminder 增加推迟相关字段
-- ------------------------------------------------------------
SET @sql := (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'cat_reminder'
            AND COLUMN_NAME = 'origin_date') = 0,
        'ALTER TABLE `cat_reminder` ADD COLUMN `origin_date` date DEFAULT NULL COMMENT ''原始提醒日期（首次推迟前，空表示未推迟）'' AFTER `remind_date`',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'cat_reminder'
            AND COLUMN_NAME = 'defer_count') = 0,
        'ALTER TABLE `cat_reminder` ADD COLUMN `defer_count` int NOT NULL DEFAULT 0 COMMENT ''累计推迟次数'' AFTER `origin_date`',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2. cat_config 增加配置项（config_key 已有唯一索引 uk_config_key）
--    已存在时只更新说明文字，不改运营调整过的 config_value
-- ------------------------------------------------------------
INSERT INTO `cat_config` (`config_key`, `config_value`, `config_name`, `create_by`, `create_time`)
VALUES ('reminder.medication.check.days', '7', '判定用药期间的回溯天数（疫苗/驱虫提醒用）', 'admin', NOW())
ON DUPLICATE KEY UPDATE `config_name` = VALUES(`config_name`);

INSERT INTO `cat_config` (`config_key`, `config_value`, `config_name`, `create_by`, `create_time`)
VALUES ('reminder.defer.default.days', '7', '提醒默认推迟天数', 'admin', NOW())
ON DUPLICATE KEY UPDATE `config_name` = VALUES(`config_name`);

-- ------------------------------------------------------------
-- 校验（期望：2 个字段存在；2 条配置存在）
-- ------------------------------------------------------------
-- SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = 'cat_rescue' AND TABLE_NAME = 'cat_reminder'
--    AND COLUMN_NAME IN ('origin_date', 'defer_count');
-- SELECT config_id, config_key, config_value, config_name FROM cat_config WHERE config_id IN (112, 113);

-- ============================================================
-- 回滚
-- ============================================================
-- ALTER TABLE `cat_reminder` DROP COLUMN `defer_count`;
-- ALTER TABLE `cat_reminder` DROP COLUMN `origin_date`;
-- DELETE FROM `cat_config` WHERE `config_key` IN ('reminder.medication.check.days', 'reminder.defer.default.days');
