-- ============================================================
-- 20261109_02 线上数据一致性修复（4 处，客户确认后执行）
-- ============================================================
-- 背景
--   2026-11-09 对线上库做了全量一致性体检（悬空引用 / 孤儿记录 / 状态矛盾 /
--   金额校验 / 重复绑定 / 系统权限，共 60+ 项），绝大多数为 0，仅发现 4 处：
--
--   ① 毛豆(123) 状态=在养(1)，但 ward_id 为空
--      成因：11-03 走「转诊出院 → 返回原归属基地」返院，该分支只把 status
--      改回 1，没有恢复病房（11-01 外送时病房已被清空）。
--      流转记录显示 11-01 从「小院/幼猫房B(109)」转到「大院/1号屋(103)」，
--      当天下午外送，因此返院应回 大院/1号屋。
--      客户确认：→ 大院(101) 1号屋(103)，并取消归档。
--      影响：按病房筛选与病房猫数看不到这只猫；助理按病房授权也看不到。
--
--   ② 灰灰(107)、三条腿(108) 状态=外送医院治疗中(3)，却仍挂着病房
--      （107→1号屋103，108→2号屋104）。系统设计是外送时清空病房、保留归属基地，
--      否则病房猫数会把不在基地的猫算进去（1号屋显示 6 只，实际 5 只在站内）。
--
--   ③ 奶片(106) dewormed='0'，但存在 1 条体外驱虫记录（2026-09-06 大宠爱）。
--      全库唯一一处「标志位与明细记录互相矛盾」，会导致按「已驱虫」筛选漏掉它。
--
--   ④ 芝麻(122) archived='1'，但状态仍是基地内治疗中(2)、备注「隔离观察中」。
--      （毛豆的归档在 ① 一并取消）
--      来源已确认：doctor01 于 2026-11-02 11:19:11 编辑档案时勾选。
--
-- 影响表：cat_profile（仅这 4 只猫的行，不涉及任何表结构）
-- 回滚（执行前请先备份；回滚语句如下，逐条可逆）
--   update cat_profile set base_id=101, ward_id=null, status='1', archived='1'
--     where cat_id=123;                       -- ① 毛豆恢复原状
--   update cat_profile set ward_id=103 where cat_id=107;  -- ② 灰灰
--   update cat_profile set ward_id=104 where cat_id=108;  -- ② 三条腿
--   update cat_profile set dewormed='0' where cat_id=106; -- ③ 奶片
--   update cat_profile set archived='1' where cat_id=122; -- ④ 芝麻
--
-- 幂等性
--   每条 UPDATE 都限定了「当前仍处于异常状态」才执行，重复跑不会重复改动，
--   也不会把管理员之后做出的正常调整覆盖掉。
-- ============================================================

-- ① 毛豆：回填病房（大院 1号屋）+ 取消归档
update cat_profile
   set base_id = 101,
       ward_id = 103,
       status  = '1',
       archived = '0',
       update_time = sysdate()
 where cat_id = 123
   and del_flag = '0'
   and (ward_id is null or ward_id <> 103
        or base_id is null or base_id <> 101
        or ifnull(archived,'0') <> '0');

-- ② 外送医院中的猫清空病房（保留归属基地）
update cat_profile
   set ward_id = null,
       update_time = sysdate()
 where cat_id in (107, 108)
   and del_flag = '0'
   and status = '3'
   and ward_id is not null;

-- ③ 奶片：补驱虫标志（仅当确实存在有效驱虫记录时才改）
update cat_profile
   set dewormed = '1',
       update_time = sysdate()
 where cat_id = 106
   and del_flag = '0'
   and ifnull(dewormed,'0') <> '1'
   and exists (select 1 from cat_deworm d
                where d.cat_id = 106 and d.del_flag = '0');

-- ④ 芝麻：取消归档（人还在站内隔离观察）
update cat_profile
   set archived = '0',
       update_time = sysdate()
 where cat_id = 122
   and del_flag = '0'
   and ifnull(archived,'0') = '1';
