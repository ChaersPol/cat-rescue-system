-- ============================================================
-- 站内诊断：接诊医生与系统账号绑定
-- 日期：2026-11-05
-- 说明：
--   原来 doctor 是纯文本，谁都能随手填，库里出现「王医生」「助理小李」
--   这类系统里根本不存在的名字，导致按医生统计工作量无效。
--   现在增加 doctor_user_id 指向 sys_user.user_id，doctor 继续存姓名快照
--   （医生改名后历史记录仍显示当时的姓名，同时能按 id 准确统计）。
--
--   录入规则（后端强制）：
--     登录人是「医生」角色 -> doctor / doctor_user_id 一律取当前登录账号
--     登录人是超管/管理员 -> 只能从系统里真实的医生账号中选
--
--   幂等：可重复执行
-- ============================================================

-- ------------------------------------------------------------
-- 1) 加字段
-- ------------------------------------------------------------
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_diagnosis' AND COLUMN_NAME = 'doctor_user_id') = 0,
    'ALTER TABLE `cat_diagnosis` ADD COLUMN `doctor_user_id` bigint(20) NULL COMMENT ''接诊医生账号ID（sys_user.user_id）'' AFTER `doctor`',
    'SELECT ''doctor_user_id 已存在，跳过'' AS msg'));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2) 历史数据归一：能对上账号的补 id
--    「王医生」= doctor01 王建国（用户确认）
--    「助理小李」无对应账号，保持原文本、id 留空（以后用更正备注修）
-- ------------------------------------------------------------
UPDATE `cat_diagnosis` d
  JOIN `sys_user` u ON u.`del_flag` = '0' AND u.`nick_name` = '王建国'
   SET d.`doctor_user_id` = u.`user_id`
 WHERE d.`doctor` = '王医生'
   AND d.`doctor_user_id` IS NULL;

-- 顺带把「文本已经是真实医生昵称」的记录也补上 id，避免新老写法并存
UPDATE `cat_diagnosis` d
  JOIN `sys_user` u ON u.`del_flag` = '0' AND u.`nick_name` = d.`doctor`
  JOIN `sys_user_role` ur ON ur.`user_id` = u.`user_id`
  JOIN `sys_role` r ON r.`role_id` = ur.`role_id` AND r.`role_key` = 'doctor' AND r.`del_flag` = '0'
   SET d.`doctor_user_id` = u.`user_id`
 WHERE d.`doctor_user_id` IS NULL
   AND d.`doctor` IS NOT NULL AND d.`doctor` <> '';

-- ------------------------------------------------------------
-- 3) 校验（期望：字段存在；王医生那 4 条已带上 id；助理小李 3 条 id 为空）
-- ------------------------------------------------------------
SHOW COLUMNS FROM `cat_diagnosis` LIKE 'doctor_user_id';

SELECT `doctor`,
       COUNT(*) AS total,
       SUM(`doctor_user_id` IS NOT NULL) AS has_user_id
  FROM `cat_diagnosis`
 WHERE `del_flag` = '0'
 GROUP BY `doctor`;

-- ============================================================
-- 回滚
-- ============================================================
-- ALTER TABLE `cat_diagnosis` DROP COLUMN `doctor_user_id`;
