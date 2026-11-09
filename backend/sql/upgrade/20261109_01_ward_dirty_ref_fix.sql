-- ============================================================
-- 20261109_01 修复「猫咪悬挂在已删除病房」的脏引用，并校正助理病房授权
-- ============================================================
-- 背景
--   18号楼（base_id=100）的 3 个旧病房在 2026-11-03 前后被逻辑删除
--   （del_flag='2'），但删除时没有把在册猫咪迁走，5 只猫仍指向已删病房：
--
--     100 普通病房 A 区  ← 花卷(100)、小满(101)、糖糖(103)
--     101 隔离观察房     ← 奶片(106)
--     102 术后观察房     ← 圆圆(118)
--
--   后果一：列表「所在病房」列显示「—」。前端 wardName() 只能从「有效病房
--           列表」里查名字，已删病房查不到，于是退回空串。
--   后果二：助理的可见范围是 ward_id IN (cat_ward_auth 里给自己配的病房)，
--           这些猫站在已删病房上，助理怎么勾都看不到它们。
--           实测 assist01 只授权了 100/101/106，于是 18号楼 7 只猫只看到 4 只
--           （花卷、小满、糖糖、奶片），煤球、瘸子、圆圆不可见。
--
-- 迁移方案（客户确认：按状态归房）
--   在养的 花卷 / 小满 / 糖糖   → 105 客厅（康复区，可自由活动）
--   TNR治疗中的 圆圆            → 106 南间
--   基地内治疗中的 奶片          → 110 北间
--
-- 配套
--   1) 清掉 cat_ward_auth 中所有指向已删病房的授权行
--      （assist01 的 100、101；assist03 的 102），避免保存时把死病房带回来
--   2) 给 assist01(101) 补齐 18号楼 现有 3 个有效病房的授权，7 只猫全部可见
--
-- 幂等性
--   三处 UPDATE 都同时限定 base_id + 旧 ward_id，重跑不会再改一次；
--   DELETE 只删 del_flag='2' 病房上的授权；INSERT 用 INSERT IGNORE
--   （主键是 user_id + ward_id，天然去重）。
--
-- 回滚（本次执行前的状态）
--   UPDATE cat_profile SET ward_id=100 WHERE cat_id IN (100,101,103);
--   UPDATE cat_profile SET ward_id=101 WHERE cat_id=106;
--   UPDATE cat_profile SET ward_id=102 WHERE cat_id=118;
--   DELETE FROM cat_ward_auth WHERE user_id=101;   -- 原为 100,101,106
--   INSERT INTO cat_ward_auth (user_id,ward_id,create_by,create_time)
--     VALUES (103,102,'admin',NOW());              -- 原 assist03 有 102
-- ============================================================

USE `cat_rescue`;

-- 1. 迁移猫咪（限定 base_id 与旧 ward_id，防止误改其它基地的猫）
UPDATE cat_profile SET ward_id = 105, update_time = NOW()
 WHERE cat_id IN (100, 101, 103)
   AND base_id = 100 AND ward_id = 100 AND del_flag = '0';

UPDATE cat_profile SET ward_id = 110, update_time = NOW()
 WHERE cat_id = 106
   AND base_id = 100 AND ward_id = 101 AND del_flag = '0';

UPDATE cat_profile SET ward_id = 106, update_time = NOW()
 WHERE cat_id = 118
   AND base_id = 100 AND ward_id = 102 AND del_flag = '0';

-- 2. 清理指向已删病房的授权行
DELETE a FROM cat_ward_auth a
  JOIN cat_ward w ON w.ward_id = a.ward_id
 WHERE w.del_flag = '2';

-- 3. 给 assist01 补齐 18号楼 现有有效病房的授权
INSERT IGNORE INTO cat_ward_auth (user_id, ward_id, create_by, create_time)
SELECT 101, w.ward_id, 'admin', NOW()
  FROM cat_ward w
 WHERE w.base_id = 100
   AND w.del_flag = '0'
   AND w.ward_id IN (105, 106, 110);
