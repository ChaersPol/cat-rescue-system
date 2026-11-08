-- =====================================================================
-- 20261106_02 用药记录：医生处方 + 助理执行打卡 + 组级完成状态
-- 新增表：cat_prescription / cat_prescription_item / cat_prescription_log /
--         cat_medication_exec
-- 影响表：cat_medication（加 prescription_id 字段 + 2 个索引）
-- 能否回滚：能，回滚语句见文件末尾（注释形式，需要时手工执行）
--
-- 背景（2026-11-06 与救助站确认，需求第 9 项）：
--   1. 现在的用药记录是"一次开一条"，医生开一次就只记当天那一天，
--      助理看不出「这药要连吃 7 天、每天早中晚各一次」。需要医生先开处方
--      （什么药 / 几个时间段 / 连吃几天），助理照着执行。
--   2. 完成状态要按「一只猫 + 一个日期 + 一个时间段」整组算，不是每个药单独勾。
--      状态不能放在 cat_medication（一行一个药）上，所以单独建组级打卡表。
--   3. 医生改处方只从改动当天起生效，已完成的历史记录不能跟着变；
--      改了谁、改了什么、几点改的，要留痕。
--   4. 医生方案与助理执行两段分开管：处方表是医生的，打卡表是助理的。
--
-- 幂等：CREATE TABLE IF NOT EXISTS；字段按 information_schema 判存后再 ALTER；
--       索引按 information_schema.statistics 判存。可重复执行。
-- =====================================================================

USE cat_rescue;

-- ---------------------------------------------------------------------
-- 1. 处方主表（医生开的用药方案）
--    一张处方 = 一只猫 + 一个开始日期 + 连续天数 + 若干药品明细
--    status：0 进行中 / 1 已停用（医生手动停掉，助理不再看到待打卡）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cat_prescription` (
  `prescription_id` bigint NOT NULL AUTO_INCREMENT COMMENT '处方id',
  `cat_id`          bigint       NOT NULL                COMMENT '猫咪id',
  `diagnosis_id`    bigint       DEFAULT NULL            COMMENT '来源诊断id',
  `doctor`          varchar(30)  DEFAULT ''              COMMENT '开方医生',
  `doctor_user_id`  bigint       DEFAULT NULL            COMMENT '开方医生账号id',
  `start_date`      date         NOT NULL                COMMENT '开始日期',
  `days`            int          NOT NULL DEFAULT '1'    COMMENT '连续天数',
  `remark`          varchar(500) DEFAULT NULL            COMMENT '处方备注/医嘱',
  `status`          char(1)      DEFAULT '0'             COMMENT '状态（0进行中 1已停用）',
  `create_by`       varchar(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`     datetime     DEFAULT NULL            COMMENT '创建时间',
  `update_by`       varchar(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`     datetime     DEFAULT NULL            COMMENT '更新时间',
  `del_flag`        char(1)      DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  PRIMARY KEY (`prescription_id`),
  KEY `idx_cat` (`cat_id`),
  KEY `idx_diag` (`diagnosis_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用药处方（医生开的用药方案）';

-- ---------------------------------------------------------------------
-- 2. 处方明细：一味药一行，time_periods 存该药的给药时段（逗号分隔，如「早,晚」）
--    之所以允许多时段：同一种药可能早一次晚一次，助理按组打卡
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cat_prescription_item` (
  `item_id`         bigint       NOT NULL AUTO_INCREMENT COMMENT '明细id',
  `prescription_id` bigint       NOT NULL                COMMENT '处方id',
  `drug_name`       varchar(100) NOT NULL                COMMENT '药品名称',
  `dosage`          varchar(50)  DEFAULT ''              COMMENT '剂量',
  `administration`  varchar(30)  DEFAULT '口服'          COMMENT '给药方式',
  `time_periods`    varchar(30)  DEFAULT ''              COMMENT '时间段（逗号分隔：早,中,晚）',
  `usage_note`      varchar(200) DEFAULT ''              COMMENT '用法说明',
  `sort`            int          DEFAULT '0'             COMMENT '排序',
  PRIMARY KEY (`item_id`),
  KEY `idx_ps` (`prescription_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='处方明细';

-- ---------------------------------------------------------------------
-- 3. 处方变更留痕：谁改的、改了什么、几点改的
--    已完成的用药记录不跟着变，改方案只从改动当天起生效（见 service）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cat_prescription_log` (
  `log_id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '日志id',
  `prescription_id` bigint        NOT NULL                COMMENT '处方id',
  `cat_id`          bigint        DEFAULT NULL            COMMENT '猫咪id',
  `action`          varchar(20)   DEFAULT ''              COMMENT '动作（新增/修改/停用）',
  `detail`          varchar(2000) DEFAULT ''              COMMENT '变更内容',
  `operator`        varchar(30)   DEFAULT ''              COMMENT '操作人',
  `oper_time`       datetime      DEFAULT NULL            COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_ps` (`prescription_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='处方变更留痕';

-- ---------------------------------------------------------------------
-- 4. 用药执行打卡（组级：猫 + 日期 + 时间段）
--    一个猫一天一个时段一条记录，助理点「完成」整组变绿，
--    同时记下谁点的、几点点的；照片也挂在这一级（整组共用一组照片）
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `cat_medication_exec` (
  `exec_id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '打卡id',
  `cat_id`           bigint        NOT NULL                COMMENT '猫咪id',
  `use_date`         date          NOT NULL                COMMENT '用药日期',
  `time_period`      varchar(10)   NOT NULL DEFAULT ''     COMMENT '时间段',
  `status`           char(1)       DEFAULT '0'             COMMENT '执行状态（0未完成 1已完成）',
  `executor`         varchar(30)   DEFAULT ''              COMMENT '执行人',
  `exec_time`        datetime      DEFAULT NULL            COMMENT '完成时间',
  `photos`           varchar(2000) DEFAULT ''              COMMENT '用药照片（逗号分隔）',
  `photo_time`       datetime      DEFAULT NULL            COMMENT '照片上传时间',
  `adverse_reaction` varchar(500)  DEFAULT ''              COMMENT '不良反应',
  `remark`           varchar(500)  DEFAULT ''              COMMENT '备注',
  `create_by`        varchar(64)   DEFAULT ''              COMMENT '创建者',
  `create_time`      datetime      DEFAULT NULL            COMMENT '创建时间',
  `update_by`        varchar(64)   DEFAULT ''              COMMENT '更新者',
  `update_time`      datetime      DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`exec_id`),
  UNIQUE KEY `uk_cat_date_period` (`cat_id`,`use_date`,`time_period`),
  KEY `idx_date` (`use_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用药执行打卡（按猫+日期+时间段整组）';

-- ---------------------------------------------------------------------
-- 5. cat_medication 加处方来源
--    处方展开出来的每一天、每个时段、每味药都是一条 cat_medication，
--    靠 prescription_id 才能做到「医生改方案时只重建未执行的那部分」
-- ---------------------------------------------------------------------
SET @ddl := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE cat_medication ADD COLUMN prescription_id BIGINT DEFAULT NULL COMMENT ''来源处方id'' AFTER diagnosis_id',
    'DO 0')
  FROM information_schema.columns
 WHERE table_schema = DATABASE() AND table_name = 'cat_medication' AND column_name = 'prescription_id');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @ddl := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE cat_medication ADD KEY idx_prescription (prescription_id)',
    'DO 0')
  FROM information_schema.statistics
 WHERE table_schema = DATABASE() AND table_name = 'cat_medication' AND index_name = 'idx_prescription');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 合并视图按「猫 + 日期 + 时间段」分组，加联合索引避免全表扫
SET @ddl := (SELECT IF(COUNT(*) = 0,
    'ALTER TABLE cat_medication ADD KEY idx_cat_date_period (cat_id, use_date, time_period)',
    'DO 0')
  FROM information_schema.statistics
 WHERE table_schema = DATABASE() AND table_name = 'cat_medication' AND index_name = 'idx_cat_date_period');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 校验：应输出 4 张新表、cat_medication 的新字段、3 个新索引
-- ---------------------------------------------------------------------
SELECT table_name, table_comment
  FROM information_schema.tables
 WHERE table_schema = DATABASE()
   AND table_name IN ('cat_prescription', 'cat_prescription_item',
                      'cat_prescription_log', 'cat_medication_exec');

SELECT column_name, column_type, column_comment
  FROM information_schema.columns
 WHERE table_schema = DATABASE() AND table_name = 'cat_medication'
   AND column_name = 'prescription_id';

SELECT index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) cols
  FROM information_schema.statistics
 WHERE table_schema = DATABASE() AND table_name = 'cat_medication'
   AND index_name IN ('idx_prescription', 'idx_cat_date_period')
 GROUP BY index_name;

-- ---------------------------------------------------------------------
-- 回滚（需要时手工执行；会丢掉处方与打卡数据）
-- DROP TABLE IF EXISTS cat_medication_exec;
-- DROP TABLE IF EXISTS cat_prescription_log;
-- DROP TABLE IF EXISTS cat_prescription_item;
-- DROP TABLE IF EXISTS cat_prescription;
-- ALTER TABLE cat_medication DROP INDEX idx_cat_date_period, DROP INDEX idx_prescription, DROP COLUMN prescription_id;
-- ---------------------------------------------------------------------
