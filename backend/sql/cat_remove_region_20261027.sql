-- ---------------------------------------------------------------
-- 去掉演示数据里的「简阳市」地名前缀，让系统不绑定具体地区
-- 执行方式：mysql -h127.0.0.1 -P3306 -uroot -p123456 cat_rescue < cat_remove_region_20261027.sql
-- 可重复执行（幂等），不含任何结构变更
-- ---------------------------------------------------------------
USE cat_rescue;

-- cat_adoption.adopter_address
UPDATE `cat_adoption` SET `adopter_address` = TRIM(REPLACE(REPLACE(REPLACE(`adopter_address`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `adopter_address` LIKE '%简阳%';

-- cat_base.address
UPDATE `cat_base` SET `address` = TRIM(REPLACE(REPLACE(REPLACE(`address`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `address` LIKE '%简阳%';

-- cat_death.death_place
UPDATE `cat_death` SET `death_place` = TRIM(REPLACE(REPLACE(REPLACE(`death_place`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `death_place` LIKE '%简阳%';

-- cat_expense.supplier
UPDATE `cat_expense` SET `supplier` = TRIM(REPLACE(REPLACE(REPLACE(`supplier`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `supplier` LIKE '%简阳%';

-- cat_flow.hospital_name
UPDATE `cat_flow` SET `hospital_name` = TRIM(REPLACE(REPLACE(REPLACE(`hospital_name`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `hospital_name` LIKE '%简阳%';

-- cat_profile.capture_point
UPDATE `cat_profile` SET `capture_point` = TRIM(REPLACE(REPLACE(REPLACE(`capture_point`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `capture_point` LIKE '%简阳%';

-- cat_profile.hospital_name
UPDATE `cat_profile` SET `hospital_name` = TRIM(REPLACE(REPLACE(REPLACE(`hospital_name`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `hospital_name` LIKE '%简阳%';

-- cat_referral.hospital_name
UPDATE `cat_referral` SET `hospital_name` = TRIM(REPLACE(REPLACE(REPLACE(`hospital_name`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `hospital_name` LIKE '%简阳%';

-- cat_release.release_point
UPDATE `cat_release` SET `release_point` = TRIM(REPLACE(REPLACE(REPLACE(`release_point`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `release_point` LIKE '%简阳%';

-- cat_reminder.content
UPDATE `cat_reminder` SET `content` = TRIM(REPLACE(REPLACE(REPLACE(`content`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `content` LIKE '%简阳%';

-- cat_stock_record.receiver_base
UPDATE `cat_stock_record` SET `receiver_base` = TRIM(REPLACE(REPLACE(REPLACE(`receiver_base`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `receiver_base` LIKE '%简阳%';

-- cat_stock_record.supplier
UPDATE `cat_stock_record` SET `supplier` = TRIM(REPLACE(REPLACE(REPLACE(`supplier`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `supplier` LIKE '%简阳%';

-- cat_tnr.capture_point
UPDATE `cat_tnr` SET `capture_point` = TRIM(REPLACE(REPLACE(REPLACE(`capture_point`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `capture_point` LIKE '%简阳%';

-- cat_base.base_name
UPDATE `cat_base` SET `base_name` = TRIM(REPLACE(REPLACE(REPLACE(`base_name`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `base_name` LIKE '%简阳%';

-- cat_base.remark
UPDATE `cat_base` SET `remark` = TRIM(REPLACE(REPLACE(REPLACE(`remark`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `remark` LIKE '%简阳%';

-- sys_dept.dept_name
UPDATE `sys_dept` SET `dept_name` = TRIM(REPLACE(REPLACE(REPLACE(`dept_name`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `dept_name` LIKE '%简阳%';

-- sys_oper_log.oper_param
UPDATE `sys_oper_log` SET `oper_param` = TRIM(REPLACE(REPLACE(REPLACE(`oper_param`, '四川省简阳市', ''), '简阳市', ''), '简阳', ''))
 WHERE `oper_param` LIKE '%简阳%';

-- ---------------------------------------------------------------
-- 第二部分：去掉街道 / 河流一级的地域名（同样不含任何地区指向）
--   简城街道 → 城关街道   沱江一桥 → 滨江一桥   沱江二桥 → 滨江二桥
--   沱江路   → 滨江路     其余「沱江」→「滨江」
-- 替换顺序必须从长到短，否则「沱江路」会先被「沱江」吃掉一半
-- ---------------------------------------------------------------

-- cat_adoption.adopter_address
UPDATE `cat_adoption` SET `adopter_address` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`adopter_address`,
      '简城街道', '城关街道'), '沱江一桥', '滨江一桥'), '沱江二桥', '滨江二桥'), '沱江路', '滨江路'), '沱江', '滨江')
 WHERE `adopter_address` REGEXP '简城街道|沱江';

-- cat_base.address
UPDATE `cat_base` SET `address` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`address`,
      '简城街道', '城关街道'), '沱江一桥', '滨江一桥'), '沱江二桥', '滨江二桥'), '沱江路', '滨江路'), '沱江', '滨江')
 WHERE `address` REGEXP '简城街道|沱江';

-- cat_profile.capture_point
UPDATE `cat_profile` SET `capture_point` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`capture_point`,
      '简城街道', '城关街道'), '沱江一桥', '滨江一桥'), '沱江二桥', '滨江二桥'), '沱江路', '滨江路'), '沱江', '滨江')
 WHERE `capture_point` REGEXP '简城街道|沱江';

-- cat_release.release_point
UPDATE `cat_release` SET `release_point` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`release_point`,
      '简城街道', '城关街道'), '沱江一桥', '滨江一桥'), '沱江二桥', '滨江二桥'), '沱江路', '滨江路'), '沱江', '滨江')
 WHERE `release_point` REGEXP '简城街道|沱江';

-- cat_tnr.capture_point
UPDATE `cat_tnr` SET `capture_point` = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(`capture_point`,
      '简城街道', '城关街道'), '沱江一桥', '滨江一桥'), '沱江二桥', '滨江二桥'), '沱江路', '滨江路'), '沱江', '滨江')
 WHERE `capture_point` REGEXP '简城街道|沱江';

-- 校验：以下查询应全部返回 0
-- SELECT COUNT(*) FROM cat_profile WHERE capture_point LIKE '%简阳%';
-- SELECT COUNT(*) FROM cat_stock_record WHERE receiver_base LIKE '%简阳%';
-- SELECT COUNT(*) FROM cat_profile WHERE capture_point REGEXP '简城街道|沱江';
-- SELECT COUNT(*) FROM cat_base    WHERE address       REGEXP '简城街道|沱江';
