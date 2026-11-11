-- ============================================================================
-- 2026-11-11 单位统一：检查项目目录 / 诊断明细的单位一律以关联物资为准
--
-- 背景：
--   cat_check_item.unit（开单目录、手填）与 cat_material.unit（库存单位）各存一份，
--   线上出现「物资=片、目录=盒」的分叉：医生开单显示 1 盒，库存却按 1 片扣，
--   卡片和开单页也显示 ×1盒。库存只认物资单位，所以物资单位是唯一真值。
--
-- 影响范围（执行前可用文末的核对语句确认）：
--   cat_check_item     : 关联物资的目录项，单位同步成物资单位
--   cat_diagnosis_item : 历史开单明细的快照单位，同样纠正（数量本来就是按物资单位扣的）
--
-- 幂等：两条都是「只在单位不一致时更新」，可重复执行。
-- 不含 DROP / TRUNCATE，不动任何业务数量与库存。
-- ============================================================================

-- 1) 检查项目 / 用药目录：关联了物资就跟随物资单位
UPDATE cat_check_item i
  JOIN cat_material m ON m.material_id = i.material_id AND m.del_flag = '0'
   SET i.unit = m.unit
 WHERE i.del_flag = '0'
   AND IFNULL(i.unit, '') <> IFNULL(m.unit, '');

-- 2) 历史诊断明细：单位快照跟着物资走（数量不变，只是把「盒」改成实际的「片/支/瓶」）
UPDATE cat_diagnosis_item d
  JOIN cat_material m ON m.material_id = d.material_id AND m.del_flag = '0'
   SET d.unit = m.unit
 WHERE IFNULL(d.unit, '') <> IFNULL(m.unit, '');

-- ----------------------------------------------------------------------------
-- 执行后核对（应为 0 行）：
--   SELECT i.item_id, i.item_name, i.unit, m.unit
--     FROM cat_check_item i JOIN cat_material m ON m.material_id = i.material_id
--    WHERE i.del_flag = '0' AND IFNULL(i.unit,'') <> IFNULL(m.unit,'');
--
--   SELECT d.detail_id, d.item_name, d.unit, m.unit
--     FROM cat_diagnosis_item d JOIN cat_material m ON m.material_id = d.material_id
--    WHERE IFNULL(d.unit,'') <> IFNULL(m.unit,'');
-- ----------------------------------------------------------------------------
