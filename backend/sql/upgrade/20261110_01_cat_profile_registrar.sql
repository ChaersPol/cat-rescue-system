-- =============================================================================
-- 2026-11-10 猫咪档案增加「登记人」（registrar）
--
-- 背景：档案详情页一直有「登记人」一栏，但 cat_profile 没有对应字段，
--       录入表单里也没地方填 → 线上永远显示「—」。
--       同时 create_by 从来没被赋值（新增接口只写了业务字段），所以连兜底都没有。
--
-- 本脚本只做三件事：
--   1. 加列 registrar（已存在则跳过，可重复执行）
--   2. 用 create_by → sys_user.nick_name 回填历史档案的登记人
--   3. create_by 为空的（如 2026-10-29 / 11-10 那两只），从 sys_oper_log 的建档日志补人
--
-- 不含任何 DROP / TRUNCATE，不动业务数据以外的东西。
-- =============================================================================

SET NAMES utf8mb4;

-- 1. 加字段 -------------------------------------------------------------------
SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'cat_profile'
                   AND COLUMN_NAME = 'registrar');
SET @ddl := IF(@has_col = 0,
  'ALTER TABLE `cat_profile` ADD COLUMN `registrar` varchar(50) DEFAULT '''' COMMENT ''登记人（谁登记这只猫，可为志愿者姓名）'' AFTER `feature`',
  'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 按 create_by 回填昵称 ----------------------------------------------------
UPDATE cat_profile p
  LEFT JOIN sys_user u ON u.user_name = p.create_by
   SET p.registrar = COALESCE(NULLIF(u.nick_name, ''), NULLIF(p.create_by, ''), '')
 WHERE (p.registrar IS NULL OR p.registrar = '')
   AND p.create_by IS NOT NULL
   AND p.create_by <> '';

-- 3. create_by 为空的，用同一时刻的「猫咪档案-新增」操作日志补人 ------------------
--    匹配条件：日志里的 oper_param 含该档案编号，且日志时间与建档时间相差 5 秒内。
UPDATE cat_profile p
  JOIN sys_oper_log l
    ON l.title = '猫咪档案'
   AND l.business_type = 1
   AND l.oper_param LIKE CONCAT('%"archiveNo":"', p.archive_no, '"%')
   AND ABS(TIMESTAMPDIFF(SECOND, l.oper_time, p.create_time)) <= 5
  LEFT JOIN sys_user u ON u.user_name = l.oper_name
   SET p.create_by = l.oper_name,
       p.registrar = COALESCE(NULLIF(p.registrar, ''), NULLIF(u.nick_name, ''), l.oper_name)
 WHERE (p.registrar IS NULL OR p.registrar = '')
   AND (p.create_by IS NULL OR p.create_by = '')
   AND l.oper_name IS NOT NULL
   AND l.oper_name <> '';

-- 校验输出（执行后人工看一眼）
SELECT COUNT(*) AS total_profile,
       SUM(CASE WHEN registrar IS NULL OR registrar = '' THEN 1 ELSE 0 END) AS without_registrar
  FROM cat_profile
 WHERE del_flag = '0';
