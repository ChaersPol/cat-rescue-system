-- =====================================================================
-- 20261107_02 出院拆两步：新增「待安置」状态 + 「基地内安置」流转类型
-- 影响表：sys_dict_data（cat_status 加 1 项、cat_flow_type 加 1 项）
-- 能否回滚：能，回滚语句见文件末尾（注释形式，需要时手工执行）
--
-- 背景（2026-11-07 与救助站确认）：
--   现实中"出院"是两步：先把猫接回原基地（人到了，但还没决定放哪间屋），
--   过一阵再把它放进具体病房。原来一步到位，病房可以空着但状态仍是「在养」，
--   结果"看起来一切正常、实际找不到猫在哪个屋"。
--
--   第一步 医院返回 → 状态「待安置」（只定回哪个基地，病房允许空）
--   第二步 基地内安置 → 状态回到「在养」，写一条流转记录（从基地-无病房 到 基地-病房）
--
--   补录场景：人已经把猫放进屋但没在电脑上录，事后补录时 operate_time 填实际时间，
--   create_time 自动记录入时间，两者分开，能查清"哪天放的、哪天补的"。
--   cat_flow 已有这两列，无需加字段。
--
-- 幂等：按 dict_type + dict_value 判存，可重复执行。
-- =====================================================================

USE cat_rescue;

-- 1. 猫咪状态：9 待安置
INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 9, '待安置', '9', 'cat_status', '', 'warning',
       'N', '0', 'admin', NOW(), '已回到基地但还没分配病房，需要尽快安置到具体房间'
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM sys_dict_data
        WHERE dict_type = 'cat_status'
          AND dict_value = '9'
       );

-- 2. 流转类型：基地内安置
INSERT INTO sys_dict_data
    (dict_sort, dict_label, dict_value, dict_type, css_class, list_class,
     is_default, status, create_by, create_time, remark)
SELECT 10, '基地内安置', '基地内安置', 'cat_flow_type', '', 'success',
       'N', '0', 'admin', NOW(), '待安置的猫分配具体病房，可补录实际转移时间'
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM sys_dict_data
        WHERE dict_type = 'cat_flow_type'
          AND dict_value = '基地内安置'
       );

-- 回滚：
--   DELETE FROM sys_dict_data WHERE dict_type = 'cat_status'    AND dict_value = '9';
--   DELETE FROM sys_dict_data WHERE dict_type = 'cat_flow_type' AND dict_value = '基地内安置';
