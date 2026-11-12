-- =============================================================================
-- 2026-11-12 站内诊断增加「体重」（cat_diagnosis.weight）
--
-- 背景：信息卡（笼位卡）上的体重原本直接取 cat_profile.weight，
--       而这个字段是建档时录的「初始体重」，猫住进来以后掉膘、长肉它都不会变，
--       卡上写的数会越来越不准（喂药、麻醉都按它算，风险实在）。
--
-- 做法：体重改成按次记录 —— 每次接诊登记「本次实测体重」，信息卡取最近一次就诊的体重；
--       cat_profile.weight 保留为「初始体重」，不再参与卡面展示。
--
-- 本脚本只加一个字段，不改任何业务数据，可重复执行。
-- 历史诊断不回填（当时没称就是没称，不能拿初始体重冒充当次实测）。
-- =============================================================================

SET NAMES utf8mb4;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'cat_diagnosis'
                   AND COLUMN_NAME = 'weight');
SET @ddl := IF(@has_col = 0,
  'ALTER TABLE `cat_diagnosis` ADD COLUMN `weight` varchar(30) DEFAULT '''' COMMENT ''体重（本次就诊实测，如 3.5kg）'' AFTER `diagnosis_result`',
  'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT @has_col AS weight_column_existed_before;
