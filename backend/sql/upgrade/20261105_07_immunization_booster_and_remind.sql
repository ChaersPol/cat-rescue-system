-- =====================================================================
-- 20261105_07 疫苗驱虫台账：加强针 + 标签配色 + 提前 3 天提醒
-- 影响表：sys_dict_data、cat_config（只改数据，不动结构）
-- 能否回滚：能，回滚语句见文件末尾（注释形式，需要时手工执行）
--
-- 背景（2026-11-05 与救助站确认）：
--   1. 每年要给所有猫打加强针，疫苗类型里要有这一项，打完后按 11 个月进年度提醒
--   2. 驱虫「体内 / 体外」列表里颜色一样容易看混，用不同颜色标签区分
--   3. 到期提醒从提前 7 天改成提前 3 天（沿用现有全局配置，不新增疫苗/驱虫单独配置）
--
-- 幂等：字典按 dict_type + dict_value 判存，配置只改指定 key，可重复执行。
-- =====================================================================

USE cat_rescue;

-- ---------------------------------------------------------------------
-- 1. 疫苗类型增加「加强针」，与第一针 / 第二针 / 第三针 / 狂犬并列
-- ---------------------------------------------------------------------
INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 5, '加强针', '加强针', 'cat_vaccine_type', '', 'warning',
       'N', '0', 'admin', NOW(), '年度加强免疫，按 11 个月周期提醒'
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM sys_dict_data
        WHERE dict_type = 'cat_vaccine_type'
          AND dict_value = '加强针'
       );

-- 已经插过的环境补齐配色（加强针用橙色，比普通针次更显眼）
UPDATE sys_dict_data
   SET list_class = 'warning'
 WHERE dict_type = 'cat_vaccine_type'
   AND dict_value = '加强针'
   AND (list_class IS NULL OR list_class <> 'warning');

-- ---------------------------------------------------------------------
-- 2. 驱虫类型标签配色：体内=绿、体外=蓝，列表里一眼分清
-- ---------------------------------------------------------------------
UPDATE sys_dict_data
   SET list_class = 'success'
 WHERE dict_type = 'cat_deworm_type'
   AND dict_value = '体内驱虫'
   AND (list_class IS NULL OR list_class <> 'success');

UPDATE sys_dict_data
   SET list_class = 'primary'
 WHERE dict_type = 'cat_deworm_type'
   AND dict_value = '体外驱虫'
   AND (list_class IS NULL OR list_class <> 'primary');

-- ---------------------------------------------------------------------
-- 3. 到期提前提醒天数 7 → 3（疫苗、驱虫共用这一个全局配置）
-- ---------------------------------------------------------------------
UPDATE cat_config
   SET config_value = '3'
 WHERE config_key = 'reminder.advance.days'
   AND config_value <> '3';

-- ---------------------------------------------------------------------
-- 校验：应输出 3 行疫苗类型（含加强针）、2 行驱虫类型带颜色、提前天数 3
-- ---------------------------------------------------------------------
SELECT dict_value, dict_label, dict_sort, list_class
  FROM sys_dict_data
 WHERE dict_type = 'cat_vaccine_type'
 ORDER BY dict_sort;

SELECT dict_value, list_class
  FROM sys_dict_data
 WHERE dict_type = 'cat_deworm_type'
 ORDER BY dict_sort;

SELECT config_key, config_value
  FROM cat_config
 WHERE config_key = 'reminder.advance.days';

-- ---------------------------------------------------------------------
-- 回滚（需要时手工执行）
-- DELETE FROM sys_dict_data WHERE dict_type = 'cat_vaccine_type' AND dict_value = '加强针';
-- UPDATE sys_dict_data SET list_class = '' WHERE dict_type = 'cat_deworm_type';
-- UPDATE cat_config SET config_value = '7' WHERE config_key = 'reminder.advance.days';
-- ---------------------------------------------------------------------
