-- ============================================================
-- 变更：站内诊断支持「来源基地 / 所在病房 / 就诊类型（初诊复诊）」
-- 影响表：cat_diagnosis
-- 可回滚：是（见文末）
-- 作者/日期：workbuddy / 2026-11-05
-- 说明：
--   1) source_base_id —— 来源基地。就诊时把「猫咪归属基地」快照下来，
--      便于追溯「这只猫是从哪个基地送来的」。猫咪归属基地为空时取当时所在基地。
--      用快照而不是实时 join 档案：猫从医疗基地治愈返回后，历史记录仍要显示当初的来源。
--   2) ward_id —— 所在病房。就诊时猫咪在治疗基地的哪个病房，同样做快照。
--      回填时只填「病房确实属于该条记录的治疗基地」的情况，避免把 A 基地的病房
--      挂到 B 基地的诊断上，造成按病房统计时串数。
--   3) visit_type —— 就诊类型（1初诊 / 2复诊）。医生手填，弹窗默认按「这只猫有没有
--      在本站看过病」自动预选；历史记录一次性补齐：每只猫最早一条=初诊，其余=复诊。
--   4) 幂等：字段先查 information_schema 再 ALTER；回填只处理字段为空的行，
--      重复执行不会改动已填好的数据。
-- ============================================================

USE cat_rescue;

-- ------------------------------------------------------------
-- 1. 增加三个字段
-- ------------------------------------------------------------
SET @sql := (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'cat_diagnosis'
            AND COLUMN_NAME = 'source_base_id') = 0,
        'ALTER TABLE `cat_diagnosis` ADD COLUMN `source_base_id` bigint DEFAULT NULL COMMENT ''来源基地id（就诊时猫咪归属基地快照）'' AFTER `base_id`',
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
            AND TABLE_NAME = 'cat_diagnosis'
            AND COLUMN_NAME = 'ward_id') = 0,
        'ALTER TABLE `cat_diagnosis` ADD COLUMN `ward_id` bigint DEFAULT NULL COMMENT ''所在病房id（就诊时所在病房快照）'' AFTER `source_base_id`',
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
            AND TABLE_NAME = 'cat_diagnosis'
            AND COLUMN_NAME = 'visit_type') = 0,
        'ALTER TABLE `cat_diagnosis` ADD COLUMN `visit_type` char(1) DEFAULT NULL COMMENT ''就诊类型（1初诊 2复诊）'' AFTER `visit_date`',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 按病房筛选/统计都要走 ward_id，补一个索引
SET @sql := (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.STATISTICS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'cat_diagnosis'
            AND INDEX_NAME = 'idx_ward') = 0,
        'ALTER TABLE `cat_diagnosis` ADD INDEX `idx_ward` (`ward_id`)',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2. 回填来源基地：优先归属基地，为空时取猫咪当前所在基地
-- ------------------------------------------------------------
UPDATE `cat_diagnosis` d
  JOIN `cat_profile` p ON p.cat_id = d.cat_id
   SET d.`source_base_id` = COALESCE(p.`owner_base_id`, p.`base_id`)
 WHERE d.`source_base_id` IS NULL;

-- ------------------------------------------------------------
-- 3. 回填所在病房：只填「病房属于该条诊断的治疗基地」的情况
-- ------------------------------------------------------------
UPDATE `cat_diagnosis` d
  JOIN `cat_profile` p ON p.cat_id = d.cat_id
  JOIN `cat_ward` w ON w.ward_id = p.ward_id
   SET d.`ward_id` = p.`ward_id`
 WHERE d.`ward_id` IS NULL
   AND w.base_id = d.base_id;

-- ------------------------------------------------------------
-- 4. 回填就诊类型：每只猫最早一条=初诊，其余=复诊
--    用临时表承接「每猫最早一条」，避免 UPDATE 与子查询同表导致的 1093 报错
-- ------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS `tmp_first_diagnosis`;
CREATE TEMPORARY TABLE `tmp_first_diagnosis` (`diagnosis_id` bigint NOT NULL PRIMARY KEY);

INSERT INTO `tmp_first_diagnosis` (`diagnosis_id`)
SELECT `diagnosis_id` FROM (
    SELECT `diagnosis_id`,
           ROW_NUMBER() OVER (PARTITION BY `cat_id` ORDER BY `visit_date` ASC, `diagnosis_id` ASC) AS rn
      FROM `cat_diagnosis`
     WHERE `del_flag` = '0'
) t
WHERE t.rn = 1;

UPDATE `cat_diagnosis` d
  JOIN `tmp_first_diagnosis` t ON t.`diagnosis_id` = d.`diagnosis_id`
   SET d.`visit_type` = '1'
 WHERE d.`visit_type` IS NULL;

UPDATE `cat_diagnosis`
   SET `visit_type` = '2'
 WHERE `visit_type` IS NULL
   AND `del_flag` = '0';

DROP TEMPORARY TABLE IF EXISTS `tmp_first_diagnosis`;

-- ------------------------------------------------------------
-- 校验（期望：3 个字段存在；来源基地基本都有值；每猫恰好 1 条初诊）
-- ------------------------------------------------------------
-- SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = 'cat_rescue' AND TABLE_NAME = 'cat_diagnosis'
--    AND COLUMN_NAME IN ('source_base_id', 'ward_id', 'visit_type');
-- SELECT COUNT(*) AS total,
--        SUM(source_base_id IS NOT NULL) AS has_source,
--        SUM(ward_id IS NOT NULL) AS has_ward,
--        SUM(visit_type = '1') AS first_visit,
--        SUM(visit_type = '2') AS revisit
--   FROM cat_diagnosis WHERE del_flag = '0';
-- SELECT cat_id, SUM(visit_type = '1') AS first_cnt FROM cat_diagnosis
--  WHERE del_flag = '0' GROUP BY cat_id HAVING first_cnt <> 1;

-- ============================================================
-- 回滚
-- ============================================================
-- ALTER TABLE `cat_diagnosis` DROP INDEX `idx_ward`;
-- ALTER TABLE `cat_diagnosis` DROP COLUMN `visit_type`;
-- ALTER TABLE `cat_diagnosis` DROP COLUMN `ward_id`;
-- ALTER TABLE `cat_diagnosis` DROP COLUMN `source_base_id`;
