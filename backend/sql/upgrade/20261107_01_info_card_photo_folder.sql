-- =====================================================================
-- 20261107_01 照片相册增加「信息卡」分类
-- 影响表：sys_dict_data（cat_photo_folder 加 1 项）
-- 能否回滚：能，回滚语句见文件末尾（注释形式，需要时手工执行）
--
-- 背景（2026-11-07 与救助站确认）：
--   信息卡（挂在笼位上的纸质卡片）在出院时回收，回收到系统里留档。
--   之前相册没有对应分类，这类照片只能落在「未分类」，在相册里也筛不出来。
--
-- 幂等：按 dict_type + dict_value 判存，可重复执行。
-- =====================================================================

USE cat_rescue;

INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 10, '信息卡', '信息卡', 'cat_photo_folder', '', 'default',
       'N', '0', 'admin', NOW(), '笼位信息卡照片：入院挂卡、改药手写、出院回收留档'
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM sys_dict_data
        WHERE dict_type = 'cat_photo_folder'
          AND dict_value = '信息卡'
       );

-- 回滚：
--   DELETE FROM sys_dict_data WHERE dict_type = 'cat_photo_folder' AND dict_value = '信息卡';
