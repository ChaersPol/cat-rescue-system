-- ================================================================
-- 流浪动物救助管理系统 字典数据 SQL
-- ================================================================

-- 猫咪状态
insert into sys_dict_type values(200, '猫咪状态', 'cat_status', '0', 'admin', sysdate(), '', null, '猫咪全局状态');
insert into sys_dict_data values(2000, 1, '在养', '1', 'cat_status', '', 'primary', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2001, 2, '基地内治疗中', '2', 'cat_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2002, 3, '外送医院治疗中', '3', 'cat_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2003, 4, '已放归', '4', 'cat_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2004, 5, '已领养', '5', 'cat_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2005, 6, '已离世', '6', 'cat_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2006, 7, 'TNR治疗中', '7', 'cat_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2007, 8, '姑息照料', '8', 'cat_status', '', 'info', 'N', '0', 'admin', sysdate(), '', null, '');

-- 猫咪性别
insert into sys_dict_type values(201, '猫咪性别', 'cat_sex', '0', 'admin', sysdate(), '', null, '猫咪性别');
insert into sys_dict_data values(2010, 1, '公', '0', 'cat_sex', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2011, 2, '母', '1', 'cat_sex', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2012, 3, '未知', '2', 'cat_sex', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 猫咪来源
insert into sys_dict_type values(202, '猫咪来源', 'cat_source', '0', 'admin', sysdate(), '', null, '猫咪来源');
insert into sys_dict_data values(2020, 1, '抓捕/TNR', '1', 'cat_source', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2021, 2, '救助捡到', '2', 'cat_source', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2022, 3, '他人移交', '3', 'cat_source', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2023, 4, '其他', '4', 'cat_source', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 死亡类型
insert into sys_dict_type values(203, '死亡类型', 'cat_death_type', '0', 'admin', sysdate(), '', null, '死亡类型');
insert into sys_dict_data values(2030, 1, '自然寿终', '1', 'cat_death_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2031, 2, '病重不治', '2', 'cat_death_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2032, 3, '安乐', '3', 'cat_death_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2033, 4, '意外', '4', 'cat_death_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 回访状态
insert into sys_dict_type values(204, '回访状态', 'cat_visit_status', '0', 'admin', sysdate(), '', null, '回访状态');
insert into sys_dict_data values(2040, 1, '待回访', '0', 'cat_visit_status', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2041, 2, '已回访', '1', 'cat_visit_status', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2042, 3, '回访异常', '2', 'cat_visit_status', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');

-- 基地类型
insert into sys_dict_type values(205, '基地类型', 'cat_base_type', '0', 'admin', sysdate(), '', null, '基地类型');
insert into sys_dict_data values(2050, 1, '饲养基地', '1', 'cat_base_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2051, 2, '医疗基地', '2', 'cat_base_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2052, 3, '放归点位', '3', 'cat_base_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 用药给药方式
insert into sys_dict_type values(206, '给药方式', 'cat_administration', '0', 'admin', sysdate(), '', null, '给药方式');
insert into sys_dict_data values(2060, 1, '口服', '口服', 'cat_administration', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2061, 2, '注射', '注射', 'cat_administration', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2062, 3, '外用', '外用', 'cat_administration', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 费用类型
insert into sys_dict_type values(207, '费用类型', 'cat_expense_type', '0', 'admin', sysdate(), '', null, '费用类型');
insert into sys_dict_data values(2070, 1, '物资采购', '1', 'cat_expense_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2071, 2, '医疗外送', '2', 'cat_expense_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2072, 3, '其他支出', '3', 'cat_expense_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 出入库类型
insert into sys_dict_type values(208, '出入库类型', 'cat_stock_type', '0', 'admin', sysdate(), '', null, '出入库类型');
insert into sys_dict_data values(2080, 1, '入库', '1', 'cat_stock_type', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2081, 2, '出库', '2', 'cat_stock_type', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');

-- 医院处置结论
insert into sys_dict_type values(209, '医院处置结论', 'cat_hospital_result', '0', 'admin', sysdate(), '', null, '医院处置结论');
insert into sys_dict_data values(2090, 1, '治愈出院', '治愈出院', 'cat_hospital_result', '', 'success', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2091, 2, '预后不良，转基地保守照料', '预后不良', 'cat_hospital_result', '', 'warning', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2092, 3, '安乐', '安乐', 'cat_hospital_result', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2093, 4, '离世', '离世', 'cat_hospital_result', '', 'danger', 'N', '0', 'admin', sysdate(), '', null, '');

-- 出院去向
insert into sys_dict_type values(210, '出院去向', 'cat_discharge_to', '0', 'admin', sysdate(), '', null, '出院去向');
insert into sys_dict_data values(2100, 1, '返回医疗基地', '返回医疗基地', 'cat_discharge_to', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2101, 2, '返回原归属基地', '返回原归属基地', 'cat_discharge_to', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2102, 3, '转入指定基地', '转入指定基地', 'cat_discharge_to', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 疫苗类型
insert into sys_dict_type values(211, '疫苗类型', 'cat_vaccine_type', '0', 'admin', sysdate(), '', null, '疫苗类型');
insert into sys_dict_data values(2110, 1, '第一针', '第一针', 'cat_vaccine_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2111, 2, '第二针', '第二针', 'cat_vaccine_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2112, 3, '第三针', '第三针', 'cat_vaccine_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2113, 4, '狂犬', '狂犬', 'cat_vaccine_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');

-- 驱虫类型
insert into sys_dict_type values(212, '驱虫类型', 'cat_deworm_type', '0', 'admin', sysdate(), '', null, '驱虫类型');
insert into sys_dict_data values(2120, 1, '体内驱虫', '体内驱虫', 'cat_deworm_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
insert into sys_dict_data values(2121, 2, '体外驱虫', '体外驱虫', 'cat_deworm_type', '', '', 'N', '0', 'admin', sysdate(), '', null, '');
