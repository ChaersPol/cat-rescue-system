-- ============================================================
-- 变更：提醒关联来源业务记录，推迟提醒时同步顺延疫苗/驱虫计划日期
-- 影响表：cat_reminder、cat_vaccine、cat_deworm
-- 可回滚：是（见文末）
-- 作者/日期：workbuddy / 2026-11-01
-- 说明：
--   1) 业务场景：猫咪该打第二针疫苗时正在用药，只能推迟几天。
--      上一版推迟只改了 cat_reminder.remind_date，疫苗台账里的
--      「下次接种」还是原日期，两处时间对不上，看台账会误判成已过期。
--   2) cat_reminder 增加 source_type / source_id，记录这条提醒由哪条
--      业务数据生成（vaccine=疫苗记录、deworm=驱虫记录、visit=回访计划），
--      推迟时据此把对应记录的计划日期一起顺延。
--   3) 回填历史提醒的 source_id：按 cat_id + 原始计划日期匹配。
--      已被推迟过的提醒 origin_date 保存着最初日期，用它匹配。
--   4) 修正历史不一致：已经推迟过（defer_count > 0）的提醒，把对应
--      疫苗/驱虫记录的 next_date 对齐到推迟后的提醒日期。
--   5) 幂等：加字段先查 information_schema；回填与修正都带
--      「source_id is null」「next_date <> remind_date」条件，重复执行不再变更。
-- ============================================================

USE cat_rescue;

-- ------------------------------------------------------------
-- 1. cat_reminder 增加来源关联字段
-- ------------------------------------------------------------
SET @sql := (
    SELECT IF(
        (SELECT COUNT(*) FROM information_schema.COLUMNS
          WHERE TABLE_SCHEMA = DATABASE()
            AND TABLE_NAME = 'cat_reminder'
            AND COLUMN_NAME = 'source_type') = 0,
        'ALTER TABLE `cat_reminder` ADD COLUMN `source_type` varchar(20) DEFAULT NULL COMMENT ''来源业务类型（vaccine疫苗 deworm驱虫 visit回访）'' AFTER `cat_id`',
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
            AND COLUMN_NAME = 'source_id') = 0,
        'ALTER TABLE `cat_reminder` ADD COLUMN `source_id` bigint DEFAULT NULL COMMENT ''来源业务主键'' AFTER `source_type`',
        'SELECT 1'
    )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2. 回填历史提醒的来源记录（按 猫 + 原始计划日期 匹配）
--    已被推迟过的用 origin_date（最初日期）匹配，未推迟过的用 remind_date
-- ------------------------------------------------------------
UPDATE cat_reminder r
  JOIN cat_vaccine v
    ON v.cat_id = r.cat_id
   AND v.del_flag = '0'
   AND v.next_date = IFNULL(r.origin_date, r.remind_date)
   SET r.source_type = 'vaccine', r.source_id = v.vaccine_id
 WHERE r.reminder_type = '疫苗'
   AND r.source_id IS NULL;

UPDATE cat_reminder r
  JOIN cat_deworm d
    ON d.cat_id = r.cat_id
   AND d.del_flag = '0'
   AND d.next_date = IFNULL(r.origin_date, r.remind_date)
   SET r.source_type = 'deworm', r.source_id = d.deworm_id
 WHERE r.reminder_type = '驱虫'
   AND r.source_id IS NULL;

UPDATE cat_reminder r
  JOIN cat_visit v
    ON v.cat_id = r.cat_id
   AND v.plan_date = IFNULL(r.origin_date, r.remind_date)
   SET r.source_type = 'visit', r.source_id = v.visit_id
 WHERE r.reminder_type = '回访'
   AND r.source_id IS NULL;

-- ------------------------------------------------------------
-- 3. 修正历史不一致：推迟过的提醒，把业务台账日期对齐到提醒日期
--    （只处理已回填来源的疫苗/驱虫提醒）
-- ------------------------------------------------------------
UPDATE cat_reminder r
  JOIN cat_vaccine v ON v.vaccine_id = r.source_id
   SET v.next_date = r.remind_date, v.update_time = NOW()
 WHERE r.source_type = 'vaccine'
   AND r.reminder_type = '疫苗'
   AND r.defer_count > 0
   AND r.remind_date IS NOT NULL
   AND (v.next_date IS NULL OR v.next_date <> r.remind_date);

UPDATE cat_reminder r
  JOIN cat_deworm d ON d.deworm_id = r.source_id
   SET d.next_date = r.remind_date, d.update_time = NOW()
 WHERE r.source_type = 'deworm'
   AND r.reminder_type = '驱虫'
   AND r.defer_count > 0
   AND r.remind_date IS NOT NULL
   AND (d.next_date IS NULL OR d.next_date <> r.remind_date);

-- ------------------------------------------------------------
-- 校验（期望：2 个字段存在；疫苗/驱虫提醒的 source_id 基本回填满；
--       不存在「提醒日期与台账下次接种日期不一致」的已推迟记录）
-- ------------------------------------------------------------
-- SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = 'cat_rescue' AND TABLE_NAME = 'cat_reminder'
--    AND COLUMN_NAME IN ('source_type', 'source_id');
-- SELECT reminder_type, COUNT(*) total, SUM(source_id IS NULL) no_source
--   FROM cat_reminder WHERE reminder_type IN ('疫苗','驱虫','回访') GROUP BY reminder_type;

-- ============================================================
-- 回滚
-- ============================================================
-- ALTER TABLE `cat_reminder` DROP COLUMN `source_id`;
-- ALTER TABLE `cat_reminder` DROP COLUMN `source_type`;
-- 注意：第 3 步改过的 cat_vaccine.next_date / cat_deworm.next_date 不会自动还原，
--       如需还原，按 origin_date 反向覆盖即可（正常不建议回滚，因为推迟本就是人工确认过的）。
