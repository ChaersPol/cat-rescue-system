-- ================================================================
-- 流浪动物救助管理系统 补充字典（2026-10-25）
-- 说明：原有 cat_dict 只覆盖猫咪状态/性别/来源等，尚有一批“数字编码”字段
--       在页面上直接显示 0/1/2，这里补齐字典，供前端 dict-tag 与下拉框使用。
--       可重复执行（先删后插）。
-- ================================================================

set names utf8mb4;

-- 字典类型 ----------------------------------------------------------
delete from sys_dict_type where dict_type in (
  'cat_neutered', 'cat_yes_no', 'cat_referral_status', 'cat_tnr_result',
  'cat_reminder_status', 'cat_time_period', 'cat_flow_type',
  'cat_reminder_type', 'cat_plan_type', 'cat_photo_folder');

insert into sys_dict_type values(213, '是否绝育', 'cat_neutered',        '0', 'admin', sysdate(), '', null, '猫咪档案：是否绝育');
insert into sys_dict_type values(214, '是否',     'cat_yes_no',          '0', 'admin', sysdate(), '', null, '通用是否');
insert into sys_dict_type values(215, '转诊状态', 'cat_referral_status', '0', 'admin', sysdate(), '', null, '外送转诊单状态');
insert into sys_dict_type values(216, 'TNR结果',  'cat_tnr_result',      '0', 'admin', sysdate(), '', null, 'TNR 抓捕登记结果');
insert into sys_dict_type values(217, '提醒状态', 'cat_reminder_status', '0', 'admin', sysdate(), '', null, '提醒通知已读状态');
insert into sys_dict_type values(218, '用药时段', 'cat_time_period',     '0', 'admin', sysdate(), '', null, '每日用药时段');
insert into sys_dict_type values(219, '流转类型', 'cat_flow_type',       '0', 'admin', sysdate(), '', null, '猫咪流转记录类型');
insert into sys_dict_type values(220, '提醒类型', 'cat_reminder_type',   '0', 'admin', sysdate(), '', null, '提醒通知来源类型');
insert into sys_dict_type values(221, '回访计划', 'cat_plan_type',       '0', 'admin', sysdate(), '', null, '领养回访计划周期');
insert into sys_dict_type values(222, '相册分类', 'cat_photo_folder',    '0', 'admin', sysdate(), '', null, '猫咪相册文件夹分类');

-- 字典数据 ----------------------------------------------------------
delete from sys_dict_data where dict_type in (
  'cat_neutered', 'cat_yes_no', 'cat_referral_status', 'cat_tnr_result',
  'cat_reminder_status', 'cat_time_period', 'cat_flow_type',
  'cat_reminder_type', 'cat_plan_type', 'cat_photo_folder');

-- 是否绝育（0未绝育 1已绝育 2未知）
insert into sys_dict_data values(2300, 1, '未绝育', '0', 'cat_neutered', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2301, 2, '已绝育', '1', 'cat_neutered', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2302, 3, '未知',   '2', 'cat_neutered', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');

-- 通用是否（0否 1是）
insert into sys_dict_data values(2310, 1, '否', '0', 'cat_yes_no', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2311, 2, '是', '1', 'cat_yes_no', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');

-- 转诊状态（0治疗中 1已出院 2已离世）
insert into sys_dict_data values(2320, 1, '治疗中', '0', 'cat_referral_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2321, 2, '已出院', '1', 'cat_referral_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2322, 3, '已离世', '2', 'cat_referral_status', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '');

-- TNR结果（0待定 1放归 2转入基地饲养）
insert into sys_dict_data values(2330, 1, '待定',       '0', 'cat_tnr_result', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2331, 2, '已放归',     '1', 'cat_tnr_result', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2332, 3, '转入基地饲养', '2', 'cat_tnr_result', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');

-- 提醒状态（0未读 1已读）
insert into sys_dict_data values(2340, 1, '未读', '0', 'cat_reminder_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2341, 2, '已读', '1', 'cat_reminder_status', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');

-- 用药时段（早/中/晚）
insert into sys_dict_data values(2350, 1, '早', '早', 'cat_time_period', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2351, 2, '中', '中', 'cat_time_period', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2352, 3, '晚', '晚', 'cat_time_period', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 流转类型
insert into sys_dict_data values(2360, 1, '入站',         '入站',         'cat_flow_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2361, 2, '发病转入治疗', '发病转入治疗', 'cat_flow_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2362, 3, '治愈返回',     '治愈返回',     'cat_flow_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2363, 4, '外送医院',     '外送医院',     'cat_flow_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2364, 5, '医院返回',     '医院返回',     'cat_flow_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2365, 6, '转入姑息',     '转入姑息',     'cat_flow_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2366, 7, '领养',         '领养',         'cat_flow_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2367, 8, '放归',         '放归',         'cat_flow_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2368, 9, '死亡',         '死亡',         'cat_flow_type', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '');

-- 提醒类型
insert into sys_dict_data values(2370, 1, '疫苗',   '疫苗',   'cat_reminder_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2371, 2, '驱虫',   '驱虫',   'cat_reminder_type', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2372, 3, '转诊',   '转诊',   'cat_reminder_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2373, 4, '库存',   '库存',   'cat_reminder_type', '', 'danger',  'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2374, 5, '回访',   '回访',   'cat_reminder_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2375, 6, '其他',   '其他',   'cat_reminder_type', '', 'info',    'N', '0', 'admin', sysdate(), '', null, '');

-- 回访计划
insert into sys_dict_data values(2380, 1, '3天',   '3天',   'cat_plan_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2381, 2, '1个月', '1个月', 'cat_plan_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2382, 3, '3个月', '3个月', 'cat_plan_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 相册分类
insert into sys_dict_data values(2390, 1,  '正面照', '正面照', 'cat_photo_folder', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2391, 2,  '侧面',   '侧面',   'cat_photo_folder', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2392, 3,  '伤口',   '伤口',   'cat_photo_folder', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2393, 4,  '术前',   '术前',   'cat_photo_folder', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2394, 5,  '术后',   '术后',   'cat_photo_folder', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2395, 6,  '救助时', '救助时', 'cat_photo_folder', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2396, 7,  '治疗中', '治疗中', 'cat_photo_folder', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2397, 8,  '康复后', '康复后', 'cat_photo_folder', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2398, 9,  '领养后', '领养后', 'cat_photo_folder', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
