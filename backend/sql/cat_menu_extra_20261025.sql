-- ================================================================
-- 流浪动物救助管理系统 补充菜单 SQL（可重复执行）
-- 新增：统计看板、照片相册、病房授权、系统配置
-- ================================================================

delete from sys_role_menu where menu_id between 2130 and 2199;
delete from sys_menu where menu_id between 2130 and 2199;

insert into sys_menu values('2130', '统计看板', '2000', '0', 'dashboard', 'cat/dashboard/index', '', '', 1, 0, 'C', '0', '0', 'cat:dashboard:view', 'dashboard', 'admin', sysdate(), '', null, '统计看板菜单');

insert into sys_menu values('2131', '照片相册', '2002', '9', 'photo', 'cat/photo/index', '', '', 1, 0, 'C', '0', '0', 'cat:photo:list', 'picture', 'admin', sysdate(), '', null, '照片相册菜单');
insert into sys_menu values('2132', '照片查询', '2131', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:photo:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2133', '照片上传', '2131', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:photo:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2134', '照片修改', '2131', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:photo:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2135', '照片删除', '2131', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:photo:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('2136', '病房授权', '2001', '9', 'wardauth', 'cat/wardauth/index', '', '', 1, 0, 'C', '0', '0', 'cat:wardauth:list', 'lock', 'admin', sysdate(), '', null, '病房授权菜单');
insert into sys_menu values('2137', '授权新增', '2136', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:wardauth:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2138', '授权修改', '2136', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:wardauth:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2139', '授权删除', '2136', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:wardauth:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('2140', '系统配置', '2000', '9', 'config', 'cat/config/index', '', 'CatConfig', 1, 0, 'C', '0', '0', 'cat:config:list', 'edit', 'admin', sysdate(), '', null, '系统配置菜单');
insert into sys_menu values('2141', '配置查询', '2140', '1', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:config:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2142', '配置新增', '2140', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:config:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2143', '配置修改', '2140', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:config:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2144', '配置删除', '2140', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:config:remove', '#', 'admin', sysdate(), '', null, '');

-- ============ 按钮权限补齐（导出等） ============
insert into sys_menu values('2150', '标签导出', '2030', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:tag:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2151', '流转记录新增', '2046', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:flow:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2152', '流转记录修改', '2046', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:flow:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2153', '流转记录删除', '2046', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:flow:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2154', '流转记录导出', '2046', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:flow:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2155', '疫苗台账导出', '2050', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:vaccine:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2156', '驱虫台账导出', '2055', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:deworm:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2157', '站内诊断导出', '2060', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:diagnosis:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2158', '用药记录导出', '2065', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:medication:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2159', '转诊记录导出', '2070', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:referral:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2160', '死亡台账导出', '2075', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:death:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2161', '抓捕登记导出', '2080', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:tnr:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2162', '放归登记导出', '2085', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:release:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2163', '领养登记导出', '2090', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:adoption:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2164', '回访记录导出', '2095', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:visit:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2165', '费用分类导出', '2100', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:feecategory:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2166', '财务支出导出', '2105', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:expense:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2167', '物资分类导出', '2110', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:materialcategory:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2168', '物资导出', '2115', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:material:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2169', '出入库记录导出', '2120', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:stockrecord:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2170', '提醒通知新增', '2125', '2', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:reminder:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2171', '提醒通知修改', '2125', '3', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:reminder:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2172', '提醒通知删除', '2125', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:reminder:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2173', '提醒通知导出', '2125', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:reminder:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2174', '照片相册导出', '2131', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:photo:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2175', '病房授权查询', '2136', '4', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:wardauth:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2176', '病房授权导出', '2136', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:wardauth:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2177', '系统配置导出', '2140', '5', '', '', '', '', 1, 0, 'F', '0', '0', 'cat:config:export', '#', 'admin', sysdate(), '', null, '');

-- 管理员角色全量授权（2000-2999 段）
delete from sys_role_menu where role_id = '1' and menu_id between 2000 and 2999;
insert into sys_role_menu (role_id, menu_id)
select '1', menu_id from sys_menu where menu_id between 2000 and 2999 and menu_id not in (select menu_id from sys_role_menu where role_id = '1');
