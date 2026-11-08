-- ============================================================
-- 变更：猫咪档案支持「年龄随时间自动更新」
-- 影响表：cat_profile
-- 可回滚：是（见文末）
-- 作者/日期：workbuddy / 2026-11-05
-- 说明：
--   1) 业务背景：原来档案存的是「大概年龄」文本（如「2岁」），一年后还是 2 岁，
--      必须人工改。现在改为只存「出生日期」，年龄每次查询时用当前日期实时计算，
--      不需要任何定时任务，也不会再出现「年龄过期」。
--   2) 新增两个字段：
--      birth_date      —— 出生日期。有准确生日就是真实日期；只知道「约 2 岁」时
--                         按救助日期反推（建卡时由程序算），展示带「约」字。
--      birth_estimated —— 出生日期是否估算（0 准确 / 1 估算），决定要不要显示「约」。
--   3) age_estimate 保留不动：它是录入时的原始描述，编辑档案时用它回显输入框，
--      也让「没有出生日期」的历史数据仍能显示年龄。
--   4) 幂等：字段先查 information_schema 再 ALTER；回填只处理 birth_date 为空的行。
--   5) 回填精度到月（「3个月」「1岁2个月」都支持），「10天」这类按天回推。
--   6) 顺带落地「满 7 岁自动提醒」：出生日期是自己算的，到期由每日定时任务自动命中，
--      不需要人工打「老龄」标记。阈值与收件岗位都做成配置项，0 可关闭。
-- ============================================================

USE cat_rescue;

-- ------------------------------------------------------------
-- 1. 增加出生日期字段
-- ------------------------------------------------------------
SET @sql := (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'cat_profile'
            AND COLUMN_NAME = 'birth_date') = 0,
        'ALTER TABLE `cat_profile` ADD COLUMN `birth_date` date DEFAULT NULL COMMENT ''出生日期（准确生日或按录入年龄反推）'' AFTER `age_estimate`',
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
            AND TABLE_NAME = 'cat_profile'
            AND COLUMN_NAME = 'birth_estimated') = 0,
        'ALTER TABLE `cat_profile` ADD COLUMN `birth_estimated` char(1) DEFAULT ''1'' COMMENT ''出生日期是否估算（0准确 1估算）'' AFTER `birth_date`',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2. 历史数据回填：按「录入年龄 + 救助日期」反推出生日期
--    基准日：救助日期 → 建档日期 → 今天（依次兜底）
--    回填出来的都标记为估算（birth_estimated = '1'）
-- ------------------------------------------------------------

-- 2.1 「2岁」「约2岁」「1岁2个月」：N 岁 × 12 + M 个月
UPDATE `cat_profile`
   SET `birth_date` = DATE_SUB(
           COALESCE(`capture_date`, DATE(`create_time`), CURDATE()),
           INTERVAL (
               COALESCE(CAST(REGEXP_SUBSTR(`age_estimate`, '[0-9]+([.][0-9]+)?') AS DECIMAL(6,2)), 0) * 12
               + COALESCE(CAST(REGEXP_SUBSTR(SUBSTRING_INDEX(`age_estimate`, '岁', -1), '[0-9]+') AS UNSIGNED), 0)
           ) MONTH
       ),
       `birth_estimated` = '1'
 WHERE `birth_date` IS NULL
   AND `age_estimate` LIKE '%岁%'
   AND `age_estimate` REGEXP '[0-9]';

-- 2.2 只有月份：「3个月」「3月龄」「8月」
UPDATE `cat_profile`
   SET `birth_date` = DATE_SUB(
           COALESCE(`capture_date`, DATE(`create_time`), CURDATE()),
           INTERVAL COALESCE(CAST(REGEXP_SUBSTR(`age_estimate`, '[0-9]+([.][0-9]+)?') AS UNSIGNED), 0) MONTH
       ),
       `birth_estimated` = '1'
 WHERE `birth_date` IS NULL
   AND `age_estimate` NOT LIKE '%岁%'
   AND `age_estimate` LIKE '%月%'
   AND `age_estimate` REGEXP '[0-9]';

-- 2.3 只有天数：「10天」
UPDATE `cat_profile`
   SET `birth_date` = DATE_SUB(
           COALESCE(`capture_date`, DATE(`create_time`), CURDATE()),
           INTERVAL COALESCE(CAST(REGEXP_SUBSTR(`age_estimate`, '[0-9]+') AS UNSIGNED), 0) DAY
       ),
       `birth_estimated` = '1'
 WHERE `birth_date` IS NULL
   AND `age_estimate` NOT LIKE '%岁%'
   AND `age_estimate` NOT LIKE '%月%'
   AND (`age_estimate` LIKE '%天%' OR `age_estimate` LIKE '%日%')
   AND `age_estimate` REGEXP '[0-9]';

-- 2.4 「半年」「半个月」这类口语说法（数量很少，单独处理）
UPDATE `cat_profile`
   SET `birth_date` = DATE_SUB(COALESCE(`capture_date`, DATE(`create_time`), CURDATE()), INTERVAL 6 MONTH),
       `birth_estimated` = '1'
 WHERE `birth_date` IS NULL AND `age_estimate` LIKE '%半年%';

UPDATE `cat_profile`
   SET `birth_date` = DATE_SUB(COALESCE(`capture_date`, DATE(`create_time`), CURDATE()), INTERVAL 15 DAY),
       `birth_estimated` = '1'
 WHERE `birth_date` IS NULL AND (`age_estimate` LIKE '%半月%' OR `age_estimate` LIKE '%半个月%');

-- ------------------------------------------------------------
-- 3. 老龄猫关注提醒的配置项
--    age = 0 表示关闭；post 为空表示全员可见（按岗位过滤）
-- ------------------------------------------------------------
INSERT INTO `cat_config` (`config_key`, `config_value`, `config_name`, `create_by`, `create_time`)
VALUES ('reminder.senior.age', '7', '老龄猫关注提醒年龄（岁，0=关闭）', 'admin', NOW())
ON DUPLICATE KEY UPDATE `config_name` = VALUES(`config_name`);

INSERT INTO `cat_config` (`config_key`, `config_value`, `config_name`, `create_by`, `create_time`)
VALUES ('reminder.post.senior', '', '老龄猫提醒收件岗位（逗号分隔，空=全员可见）', 'admin', NOW())
ON DUPLICATE KEY UPDATE `config_name` = VALUES(`config_name`);

-- ------------------------------------------------------------
-- 校验（期望：2 个字段存在；大部分在册猫 birth_date 不为空）
-- ------------------------------------------------------------
-- SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_DEFAULT, COLUMN_COMMENT FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = 'cat_rescue' AND TABLE_NAME = 'cat_profile'
--    AND COLUMN_NAME IN ('birth_date', 'birth_estimated');
-- SELECT COUNT(*) AS total,
--        SUM(birth_date IS NOT NULL) AS has_birth,
--        SUM(birth_estimated = '1') AS estimated
--   FROM cat_profile WHERE del_flag = '0';
-- SELECT cat_id, cat_name, age_estimate, capture_date, birth_date, birth_estimated
--   FROM cat_profile WHERE del_flag = '0' ORDER BY cat_id DESC LIMIT 10;

-- ============================================================
-- 回滚
-- ============================================================
-- DELETE FROM `cat_config` WHERE `config_key` IN ('reminder.senior.age', 'reminder.post.senior');
-- DELETE FROM `cat_reminder` WHERE `source_type` = 'senior';
-- ALTER TABLE `cat_profile` DROP COLUMN `birth_estimated`;
-- ALTER TABLE `cat_profile` DROP COLUMN `birth_date`;
