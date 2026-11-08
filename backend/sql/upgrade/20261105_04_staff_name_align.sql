-- ============================================================================
-- 人员姓名与系统账号对齐（数据修正，不做结构变更）
--
-- 背景：用药执行人、疫苗负责人、回访人、死亡登记、TNR 手术医生、物资领用人
--       原来是自由文本，库里出现「王医生」「助理小李」这类对不上账号的名字。
--       现在这些字段改为从系统账号中选择，历史数据里能对上人的先统一成账号昵称。
--
-- 处理原则：
--   1. 能确定对应账号的（王医生 = doctor01 王建国），统一改成账号昵称；
--   2. 流转记录 / 物资操作人原来写的是登录名（admin），统一改成昵称；
--   3. 「助理小李」「助理小周」在系统里没有同名账号（现有助理账号为
--      张小敏 / 赵丽 / 孙倩 / 吴倩 / 周涛），无法判断具体是谁，保持原样，
--      由管理员在页面上重新选择后再保存，避免猜错人改坏台账。
--
-- 幂等：全部按「原值 = 旧称呼」更新，重复执行不会再命中。
-- 执行：mysql -h127.0.0.1 -uroot -p cat_rescue < 20261105_04_staff_name_align.sql
-- ============================================================================

-- 1) 王医生 → 王建国（doctor01 的昵称）
UPDATE `cat_medication`   SET `executor`    = '王建国' WHERE `executor`    = '王医生';
UPDATE `cat_vaccine`      SET `responsible` = '王建国' WHERE `responsible` = '王医生';
UPDATE `cat_death`        SET `doctor`      = '王建国' WHERE `doctor`      = '王医生';
UPDATE `cat_tnr`          SET `surgeon`     = '王建国' WHERE `surgeon`     = '王医生';
UPDATE `cat_stock_record` SET `receiver`    = '王建国' WHERE `receiver`    = '王医生';

-- 2) 记录人原来写登录名 admin，统一成账号昵称「系统超管」
UPDATE `cat_flow`         SET `operate_by` = '系统超管' WHERE `operate_by` = 'admin';
UPDATE `cat_stock_record` SET `operator`   = '系统超管' WHERE `operator`   = 'admin';

-- 3) 校验：看各表现存的人员取值（执行后人工确认一遍）
SELECT '用药执行人' AS field, `executor` AS val, COUNT(*) AS cnt FROM `cat_medication` GROUP BY `executor`
UNION ALL
SELECT '疫苗负责人', `responsible`, COUNT(*) FROM `cat_vaccine` GROUP BY `responsible`
UNION ALL
SELECT '回访人', `visit_by`, COUNT(*) FROM `cat_visit` GROUP BY `visit_by`
UNION ALL
SELECT '死亡-医生', `doctor`, COUNT(*) FROM `cat_death` GROUP BY `doctor`
UNION ALL
SELECT '死亡-登记人', `registrar`, COUNT(*) FROM `cat_death` GROUP BY `registrar`
UNION ALL
SELECT 'TNR-手术医生', `surgeon`, COUNT(*) FROM `cat_tnr` GROUP BY `surgeon`
UNION ALL
SELECT '物资-操作人', `operator`, COUNT(*) FROM `cat_stock_record` GROUP BY `operator`
UNION ALL
SELECT '物资-领用人', `receiver`, COUNT(*) FROM `cat_stock_record` GROUP BY `receiver`
UNION ALL
SELECT '流转-操作人', `operate_by`, COUNT(*) FROM `cat_flow` GROUP BY `operate_by`
UNION ALL
SELECT '基地-负责人', `leader`, COUNT(*) FROM `cat_base` GROUP BY `leader`;

-- 4) 基地负责人也改成从账号里选。这里只列出来供人工确认，不自动改名：
--    name_matched = 0 表示这个名字在系统账号里找不到人，需要在「基础信息-基地」里重新选一次。
SELECT b.`base_id`, b.`base_name`, b.`leader`,
       (SELECT COUNT(*) FROM `sys_user` u
         WHERE u.`del_flag` = '0'
           AND (u.`nick_name` = b.`leader` OR u.`user_name` = b.`leader`)) AS name_matched
FROM `cat_base` b
ORDER BY name_matched, b.`base_id`;
