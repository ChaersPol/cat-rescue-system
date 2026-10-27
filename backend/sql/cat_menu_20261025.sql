-- ================================================================
-- 流浪动物救助管理系统 菜单与权限 SQL
-- 说明：菜单ID从 2000 开始，避免与系统菜单冲突
-- ================================================================

-- 一级目录
insert into sys_menu values('2000', '救助站管理', '0', '5', 'cat', null, '', '', 1, 0, 'M', '0', '0', '', 'cat', 'admin', sysdate(), '', null, '救助站管理目录');

-- 二级目录：基础信息
insert into sys_menu values('2001', '基础信息', '2000', '1', 'base', null, '', '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), '', null, '基础信息目录');
-- 三级目录：猫咪档案
insert into sys_menu values('2002', '猫咪档案', '2000', '2', 'profile', null, '', '', 1, 0, 'M', '0', '0', '', 'cat-profile', 'admin', sysdate(), '', null, '猫咪档案目录');
-- 三级目录：医疗病历
insert into sys_menu values('2003', '医疗病历', '2000', '3', 'medical', null, '', '', 1, 0, 'M', '0', '0', '', 'cat-medical', 'admin', sysdate(), '', null, '医疗病历目录');
-- 三级目录：TNR专项
insert into sys_menu values('2004', 'TNR绝育放归', '2000', '4', 'tnr', null, '', '', 1, 0, 'M', '0', '0', '', 'cat-tnr', 'admin', sysdate(), '', null, 'TNR专项目录');
-- 三级目录：领养回访
insert into sys_menu values('2005', '领养回访', '2000', '5', 'adopt', null, '', '', 1, 0, 'M', '0', '0', '', 'cat-adopt', 'admin', sysdate(), '', null, '领养回访目录');
-- 三级目录：财务物资
insert into sys_menu values('2006', '财务物资', '2000', '6', 'finance', null, '', '', 1, 0, 'M', '0', '0', '', 'cat-finance', 'admin', sysdate(), '', null, '财务物资目录');

-- ============ 基础信息子菜单 ============
-- 基地管理
insert into sys_menu values('2010', '基地管理', '2001', '1', 'base', 'cat/base/index', '', '', 1, 0, 'C', '0', '0', 'cat:base:list', 'tree', 'admin', sysdate(), '', null, '基地管理菜单');
insert into sys_menu values('2011', '基地查询', '2010', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:base:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2012', '基地新增', '2010', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:base:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2013', '基地修改', '2010', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:base:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2014', '基地删除', '2010', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:base:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2015', '基地导出', '2010', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:base:export', '#', 'admin', sysdate(), '', null, '');
-- 病房管理
insert into sys_menu values('2020', '病房管理', '2001', '2', 'ward', 'cat/ward/index', '', '', 1, 0, 'C', '0', '0', 'cat:ward:list', 'tree-table', 'admin', sysdate(), '', null, '病房管理菜单');
insert into sys_menu values('2021', '病房查询', '2020', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:ward:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2022', '病房新增', '2020', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:ward:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2023', '病房修改', '2020', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:ward:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2024', '病房删除', '2020', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:ward:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2025', '病房导出', '2020', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:ward:export', '#', 'admin', sysdate(), '', null, '');
-- 标签管理
insert into sys_menu values('2030', '标签管理', '2001', '3', 'tag', 'cat/tag/index', '', '', 1, 0, 'C', '0', '0', 'cat:tag:list', 'tag', 'admin', sysdate(), '', null, '标签管理菜单');
insert into sys_menu values('2031', '标签查询', '2030', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:tag:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2032', '标签新增', '2030', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:tag:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2033', '标签修改', '2030', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:tag:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2034', '标签删除', '2030', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:tag:remove', '#', 'admin', sysdate(), '', null, '');

-- ============ 猫咪档案子菜单 ============
insert into sys_menu values('2040', '猫咪档案', '2002', '1', 'profile', 'cat/profile/index', '', '', 1, 0, 'C', '0', '0', 'cat:profile:list', 'cat-profile', 'admin', sysdate(), '', null, '猫咪档案菜单');
insert into sys_menu values('2041', '档案查询', '2040', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:profile:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2042', '档案新增', '2040', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:profile:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2043', '档案修改', '2040', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:profile:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2044', '档案删除', '2040', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:profile:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2045', '档案导出', '2040', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:profile:export', '#', 'admin', sysdate(), '', null, '');
-- 流转记录
insert into sys_menu values('2046', '流转记录', '2002', '2', 'flow', 'cat/flow/index', '', '', 1, 0, 'C', '0', '0', 'cat:flow:list', 'route', 'admin', sysdate(), '', null, '流转记录菜单');
insert into sys_menu values('2047', '流转查询', '2046', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:flow:query', '#', 'admin', sysdate(), '', null, '');

-- ============ 医疗病历子菜单 ============
insert into sys_menu values('2050', '疫苗台账', '2003', '1', 'vaccine', 'cat/vaccine/index', '', '', 1, 0, 'C', '0', '0', 'cat:vaccine:list', 'cat-medical', 'admin', sysdate(), '', null, '疫苗台账菜单');
insert into sys_menu values('2051', '疫苗查询', '2050', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:vaccine:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2052', '疫苗新增', '2050', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:vaccine:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2053', '疫苗修改', '2050', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:vaccine:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2054', '疫苗删除', '2050', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:vaccine:remove', '#', 'admin', sysdate(), '', null, '');
-- 驱虫台账
insert into sys_menu values('2055', '驱虫台账', '2003', '2', 'deworm', 'cat/deworm/index', '', '', 1, 0, 'C', '0', '0', 'cat:deworm:list', 'cat-medical', 'admin', sysdate(), '', null, '驱虫台账菜单');
insert into sys_menu values('2056', '驱虫查询', '2055', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:deworm:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2057', '驱虫新增', '2055', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:deworm:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2058', '驱虫修改', '2055', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:deworm:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2059', '驱虫删除', '2055', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:deworm:remove', '#', 'admin', sysdate(), '', null, '');
-- 站内诊断
insert into sys_menu values('2060', '站内诊断', '2003', '3', 'diagnosis', 'cat/diagnosis/index', '', '', 1, 0, 'C', '0', '0', 'cat:diagnosis:list', 'cat-medical', 'admin', sysdate(), '', null, '站内诊断菜单');
insert into sys_menu values('2061', '诊断查询', '2060', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:diagnosis:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2062', '诊断新增', '2060', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:diagnosis:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2063', '诊断修改', '2060', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:diagnosis:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2064', '诊断删除', '2060', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:diagnosis:remove', '#', 'admin', sysdate(), '', null, '');
-- 用药记录
insert into sys_menu values('2065', '用药记录', '2003', '4', 'medication', 'cat/medication/index', '', '', 1, 0, 'C', '0', '0', 'cat:medication:list', 'cat-medical', 'admin', sysdate(), '', null, '用药记录菜单');
insert into sys_menu values('2066', '用药查询', '2065', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:medication:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2067', '用药新增', '2065', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:medication:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2068', '用药修改', '2065', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:medication:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2069', '用药删除', '2065', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:medication:remove', '#', 'admin', sysdate(), '', null, '');
-- 转诊记录
insert into sys_menu values('2070', '转诊记录', '2003', '5', 'referral', 'cat/referral/index', '', '', 1, 0, 'C', '0', '0', 'cat:referral:list', 'cat-medical', 'admin', sysdate(), '', null, '转诊记录菜单');
insert into sys_menu values('2071', '转诊查询', '2070', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:referral:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2072', '转诊新增', '2070', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:referral:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2073', '转诊修改', '2070', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:referral:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2074', '转诊删除', '2070', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:referral:remove', '#', 'admin', sysdate(), '', null, '');
-- 死亡台账
insert into sys_menu values('2075', '死亡台账', '2003', '6', 'death', 'cat/death/index', '', '', 1, 0, 'C', '0', '0', 'cat:death:list', 'cat-medical', 'admin', sysdate(), '', null, '死亡台账菜单');
insert into sys_menu values('2076', '死亡查询', '2075', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:death:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2077', '死亡新增', '2075', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:death:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2078', '死亡修改', '2075', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:death:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2079', '死亡删除', '2075', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:death:remove', '#', 'admin', sysdate(), '', null, '');

-- ============ TNR子菜单 ============
insert into sys_menu values('2080', '抓捕登记', '2004', '1', 'tnr', 'cat/tnr/index', '', '', 1, 0, 'C', '0', '0', 'cat:tnr:list', 'cat-tnr', 'admin', sysdate(), '', null, 'TNR抓捕登记菜单');
insert into sys_menu values('2081', '抓捕查询', '2080', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:tnr:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2082', '抓捕新增', '2080', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:tnr:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2083', '抓捕修改', '2080', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:tnr:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2084', '抓捕删除', '2080', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:tnr:remove', '#', 'admin', sysdate(), '', null, '');
-- 放归登记
insert into sys_menu values('2085', '放归登记', '2004', '2', 'release', 'cat/release/index', '', '', 1, 0, 'C', '0', '0', 'cat:release:list', 'cat-tnr', 'admin', sysdate(), '', null, 'TNR放归登记菜单');
insert into sys_menu values('2086', '放归查询', '2085', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:release:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2087', '放归新增', '2085', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:release:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2088', '放归修改', '2085', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:release:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2089', '放归删除', '2085', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:release:remove', '#', 'admin', sysdate(), '', null, '');

-- ============ 领养回访子菜单 ============
insert into sys_menu values('2090', '领养登记', '2005', '1', 'adoption', 'cat/adoption/index', '', '', 1, 0, 'C', '0', '0', 'cat:adoption:list', 'cat-adopt', 'admin', sysdate(), '', null, '领养登记菜单');
insert into sys_menu values('2091', '领养查询', '2090', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:adoption:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2092', '领养新增', '2090', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:adoption:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2093', '领养修改', '2090', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:adoption:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2094', '领养删除', '2090', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:adoption:remove', '#', 'admin', sysdate(), '', null, '');
-- 回访记录
insert into sys_menu values('2095', '回访记录', '2005', '2', 'visit', 'cat/visit/index', '', '', 1, 0, 'C', '0', '0', 'cat:visit:list', 'cat-adopt', 'admin', sysdate(), '', null, '回访记录菜单');
insert into sys_menu values('2096', '回访查询', '2095', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:visit:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2097', '回访新增', '2095', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:visit:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2098', '回访修改', '2095', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:visit:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2099', '回访删除', '2095', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:visit:remove', '#', 'admin', sysdate(), '', null, '');

-- ============ 财务物资子菜单 ============
insert into sys_menu values('2100', '费用分类', '2006', '1', 'feecategory', 'cat/feecategory/index', '', '', 1, 0, 'C', '0', '0', 'cat:feecategory:list', 'cat-finance', 'admin', sysdate(), '', null, '费用分类菜单');
insert into sys_menu values('2101', '费用分类查询', '2100', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:feecategory:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2102', '费用分类新增', '2100', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:feecategory:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2103', '费用分类修改', '2100', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:feecategory:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2104', '费用分类删除', '2100', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:feecategory:remove', '#', 'admin', sysdate(), '', null, '');
-- 财务支出
insert into sys_menu values('2105', '财务支出', '2006', '2', 'expense', 'cat/expense/index', '', '', 1, 0, 'C', '0', '0', 'cat:expense:list', 'cat-finance', 'admin', sysdate(), '', null, '财务支出菜单');
insert into sys_menu values('2106', '支出查询', '2105', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:expense:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2107', '支出新增', '2105', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:expense:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2108', '支出修改', '2105', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:expense:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2109', '支出删除', '2105', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:expense:remove', '#', 'admin', sysdate(), '', null, '');
-- 物资分类
insert into sys_menu values('2110', '物资分类', '2006', '3', 'materialcategory', 'cat/materialcategory/index', '', '', 1, 0, 'C', '0', '0', 'cat:materialcategory:list', 'cat-finance', 'admin', sysdate(), '', null, '物资分类菜单');
insert into sys_menu values('2111', '物资分类查询', '2110', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:materialcategory:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2112', '物资分类新增', '2110', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:materialcategory:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2113', '物资分类修改', '2110', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:materialcategory:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2114', '物资分类删除', '2110', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:materialcategory:remove', '#', 'admin', sysdate(), '', null, '');
-- 物资管理
insert into sys_menu values('2115', '物资管理', '2006', '4', 'material', 'cat/material/index', '', '', 1, 0, 'C', '0', '0', 'cat:material:list', 'cat-finance', 'admin', sysdate(), '', null, '物资管理菜单');
insert into sys_menu values('2116', '物资查询', '2115', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:material:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2117', '物资新增', '2115', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:material:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2118', '物资修改', '2115', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:material:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2119', '物资删除', '2115', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:material:remove', '#', 'admin', sysdate(), '', null, '');
-- 出入库记录
insert into sys_menu values('2120', '出入库记录', '2006', '5', 'stockrecord', 'cat/stockrecord/index', '', '', 1, 0, 'C', '0', '0', 'cat:stockrecord:list', 'cat-finance', 'admin', sysdate(), '', null, '出入库记录菜单');
insert into sys_menu values('2121', '出入库查询', '2120', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:stockrecord:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2122', '出入库新增', '2120', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:stockrecord:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2123', '出入库修改', '2120', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:stockrecord:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2124', '出入库删除', '2120', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:stockrecord:remove', '#', 'admin', sysdate(), '', null, '');
-- 提醒通知
insert into sys_menu values('2125', '提醒通知', '2000', '7', 'reminder', 'cat/reminder/index', '', '', 1, 0, 'C', '0', '0', 'cat:reminder:list', 'message', 'admin', sysdate(), '', null, '提醒通知菜单');
insert into sys_menu values('2126', '提醒查询', '2125', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:reminder:query', '#', 'admin', sysdate(), '', null, '');

-- ============ 统计看板 ============
insert into sys_menu values('2130', '统计看板', '2000', '0', 'dashboard', 'cat/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'cat:dashboard:view', 'dashboard', 'admin', sysdate(), '', null, '统计看板菜单');

-- ============ 照片相册 ============
insert into sys_menu values('2131', '照片相册', '2002', '9', 'photo', 'cat/photo/index', '', '', 1, 0, 'C', '0', '0', 'cat:photo:list', 'picture', 'admin', sysdate(), '', null, '照片相册菜单');
insert into sys_menu values('2132', '照片查询', '2131', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:photo:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2133', '照片上传', '2131', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:photo:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2134', '照片修改', '2131', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:photo:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2135', '照片删除', '2131', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:photo:remove', '#', 'admin', sysdate(), '', null, '');

-- ============ 病房授权 ============
insert into sys_menu values('2136', '病房授权', '2001', '9', 'wardauth', 'cat/wardauth/index', '', '', 1, 0, 'C', '0', '0', 'cat:wardauth:list', 'lock', 'admin', sysdate(), '', null, '病房授权菜单');
insert into sys_menu values('2137', '授权新增', '2136', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:wardauth:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2138', '授权修改', '2136', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:wardauth:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2139', '授权删除', '2136', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:wardauth:remove', '#', 'admin', sysdate(), '', null, '');

-- ============ 系统配置 ============
insert into sys_menu values('2140', '系统配置', '2000', '9', 'config', 'cat/config/index', '', 'CatConfig', 1, 0, 'C', '0', '0', 'cat:config:list', 'edit', 'admin', sysdate(), '', null, '系统配置菜单');
insert into sys_menu values('2141', '配置查询', '2140', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:config:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2142', '配置新增', '2140', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:config:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2143', '配置修改', '2140', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:config:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2144', '配置删除', '2140', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:config:remove', '#', 'admin', sysdate(), '', null, '');

-- ================================================================
-- 分配菜单权限给管理员角色（role_id = 1）
-- ================================================================
insert into sys_role_menu (role_id, menu_id)
select '1', menu_id from sys_menu where menu_id >= 2000 and menu_id < 3000;
