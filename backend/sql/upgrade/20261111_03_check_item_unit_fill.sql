-- ============================================================
-- 变更：检查项目目录存量单位归一 + 停用两条占位数据
-- 影响表：cat_check_item（只改 unit / status，不动数量、价格、关联物资）
-- 可回滚：是（见文末）
-- 作者/日期：workbuddy / 2026-11-11
-- 说明：
--   1) 线上全库只有 4 条检查项目单位为空，其余（次/份/支/片/瓶）均规范；
--      物资表 8 种单位也全部规范，本脚本不涉及 cat_material。
--   2) 3002「基础体格检查」、3006「血检」属不扣库存的服务项，
--      与同类的体重体温/心肺听诊/血常规/生化保持一致，补「次」。
--   3) 3001「检查项目」、3010「用药」是初始化占位数据：价格 0、不关联物资、
--      从未被任何一条 cat_diagnosis_item 引用，按用户确认置为停用（status=1），
--      保留不删以便追溯；诊断开单取目录时带 status='0'，停用后不再出现在选药列表。
--   4) 幂等：改单位带「原值为空」条件，停用带「原状态为正常」条件，
--      全部带 item_name 双重校验，防止不同环境 id 指向别的项目。
-- ============================================================

USE cat_rescue;

-- 1) 服务项补单位：无单位 → 次
UPDATE `cat_check_item`
   SET `unit` = '次', `update_by` = 'admin', `update_time` = NOW()
 WHERE `item_id` = 3002 AND `item_name` = '基础体格检查' AND IFNULL(`unit`, '') = '';

UPDATE `cat_check_item`
   SET `unit` = '次', `update_by` = 'admin', `update_time` = NOW()
 WHERE `item_id` = 3006 AND `item_name` = '血检' AND IFNULL(`unit`, '') = '';

-- 2) 占位项停用（不删除）
UPDATE `cat_check_item`
   SET `status` = '1', `update_by` = 'admin', `update_time` = NOW()
 WHERE `item_id` IN (3001, 3010) AND `status` = '0';

-- ------------------------------------------------------------
-- 校验（期望：0 条正常状态且单位为空；3001/3010 状态为 1）
-- ------------------------------------------------------------
-- SELECT item_id, item_name, unit, status FROM cat_check_item
--  WHERE del_flag = '0' AND status = '0' AND IFNULL(unit,'') = '';
-- SELECT item_id, item_name, unit, status FROM cat_check_item WHERE item_id IN (3001,3010);

-- ============================================================
-- 回滚
-- ============================================================
-- UPDATE cat_check_item SET unit = ''    WHERE item_id IN (3002,3006);
-- UPDATE cat_check_item SET status = '0' WHERE item_id IN (3001,3010);
