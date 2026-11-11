-- ============================================================
-- 变更：新增「计量单位」字典 material_unit，物资页单位改为下拉选择
-- 影响表：sys_dict_type、sys_dict_data（只插不改）
-- 可回滚：是（见文末）
-- 作者/日期：workbuddy / 2026-11-11
-- 说明：
--   1) 单位此前是自由输入框，虽然线上现存 8 种单位恰好都规范，但没有任何约束：
--      写「片剂」「粒」「KG」都能存进去，而 unit 决定库存扣减口径、开单显示、
--      诊断明细快照与信息卡文案，一旦分叉账就对不上。
--   2) 本脚本只建字典，不改任何业务数据；物资页与检查项目页改为可选可输入的下拉。
--   3) 幂等：dict_type 有唯一索引，插入前用 NOT EXISTS 判断，可重复执行。
-- ============================================================

USE cat_rescue;

-- 1) 字典类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '计量单位', 'material_unit', '0', 'admin', NOW(), '物资与检查项目共用，统一库存口径'
  FROM DUAL
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'material_unit');

-- 2) 字典数据：药品最小单位 → 包装单位 → 服务单位 → 质量/体积
--    顺序按「日常录入频率」排，下拉第一项就是最常用的
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `is_default`, `status`, `create_by`, `create_time`)
SELECT s.sort, s.lbl, s.val, 'material_unit', 'N', '0', 'admin', NOW()
  FROM (
        SELECT 1  AS sort, '片'  AS lbl, '片'  AS val UNION ALL
        SELECT 2,  '支',  '支'  UNION ALL
        SELECT 3,  '瓶',  '瓶'  UNION ALL
        SELECT 4,  '盒',  '盒'  UNION ALL
        SELECT 5,  '袋',  '袋'  UNION ALL
        SELECT 6,  '包',  '包'  UNION ALL
        SELECT 7,  '粒',  '粒'  UNION ALL
        SELECT 8,  '份',  '份'  UNION ALL
        SELECT 9,  '次',  '次'  UNION ALL
        SELECT 10, '个',  '个'  UNION ALL
        SELECT 11, '套',  '套'  UNION ALL
        SELECT 12, '张',  '张'  UNION ALL
        SELECT 13, '双',  '双'  UNION ALL
        SELECT 14, 'kg', 'kg' UNION ALL
        SELECT 15, 'g',  'g'  UNION ALL
        SELECT 16, 'ml', 'ml' UNION ALL
        SELECT 17, 'L',  'L'
       ) s
 WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` d
                    WHERE d.`dict_type` = 'material_unit' AND d.`dict_value` = s.val);

-- ------------------------------------------------------------
-- 校验（期望：1 条字典类型，17 条字典数据）
-- ------------------------------------------------------------
-- SELECT dict_type, dict_name FROM sys_dict_type WHERE dict_type = 'material_unit';
-- SELECT dict_sort, dict_label, dict_value FROM sys_dict_data
--  WHERE dict_type = 'material_unit' ORDER BY dict_sort;

-- ============================================================
-- 回滚
-- ============================================================
-- DELETE FROM sys_dict_data WHERE dict_type = 'material_unit';
-- DELETE FROM sys_dict_type WHERE dict_type = 'material_unit';
