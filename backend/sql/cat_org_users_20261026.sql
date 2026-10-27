-- ============================================================
-- 救助站组织架构 / 三类角色 / 测试账号 / 基地病房 / 病房授权
-- 依据《流浪动物救助管理系统 功能需求文档-定档》
--   3.1 角色定义：管理员 / 医生 / 助理
--   4.1 基地档案：A基地、B基地、医疗基地、放归点位
--   15  助理可管理不同病房（A助理管 X 病房，B助理管 Y 病房）
-- 可重复执行（先清理再写入）
-- ============================================================

-- ============================================================
-- 一、部门（组织架构）
-- ============================================================
delete from sys_dept where dept_id between 101 and 299;

insert into sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, leader, phone, email, status, del_flag, create_by, create_time) values
(100, 0,   '0',         '流浪猫救助中心', 0, '李文',   '13800138000', 'office@cat-rescue.local',  '0', '0', 'admin', now()),
(201, 100, '0,100',     '站务管理部',           1, '李文',   '13800138000', 'admin@cat-rescue.local',   '0', '0', 'admin', now()),
(202, 100, '0,100',     '医疗部',               2, '王建国', '13800138001', 'medical@cat-rescue.local', '0', '0', 'admin', now()),
(203, 202, '0,100,202', '医疗基地诊疗组',       1, '王建国', '13800138001', 'medical@cat-rescue.local', '0', '0', 'admin', now()),
(204, 100, '0,100',     '护理部',               3, '张小敏', '13800138003', 'nurse@cat-rescue.local',   '0', '0', 'admin', now()),
(205, 204, '0,100,204', 'A基地区护理组',        1, '张小敏', '13800138003', 'nurse-a@cat-rescue.local', '0', '0', 'admin', now()),
(206, 204, '0,100,204', 'B基地区护理组',        2, '赵丽',   '13800138004', 'nurse-b@cat-rescue.local', '0', '0', 'admin', now()),
(207, 100, '0,100',     '领养回访组',           4, '吴倩',   '13800138007', 'adopt@cat-rescue.local',   '0', '0', 'admin', now()),
(208, 100, '0,100',     '后勤物资部',           5, '周涛',   '13800138006', 'stock@cat-rescue.local',   '0', '0', 'admin', now())
on duplicate key update
  parent_id = values(parent_id), ancestors = values(ancestors), dept_name = values(dept_name),
  order_num = values(order_num), leader = values(leader), phone = values(phone),
  email = values(email), status = '0', del_flag = '0', update_time = now();

-- ============================================================
-- 二、岗位
-- ============================================================
delete from sys_post where post_id > 6;

insert into sys_post (post_id, post_code, post_name, post_sort, status, create_by, create_time, remark) values
(1, 'site_manager',     '站长',         1, '0', 'admin', now(), '救助中心负责人，统筹各基地与部门'),
(2, 'chief_doctor',     '主治医生',     2, '0', 'admin', now(), '医疗基地主治医生，负责诊断、转诊、死亡登记'),
(3, 'resident_doctor',  '住院医师',     3, '0', 'admin', now(), '医疗基地住院医师，负责日常诊疗与病历'),
(4, 'nurse_assistant',  '护理助理',     4, '0', 'admin', now(), '负责病房日常、用药打卡、疫苗驱虫、拍照上传'),
(5, 'stock_manager',    '物资管理员',   5, '0', 'admin', now(), '负责物资出入库登记与库存盘点'),
(6, 'adoption_officer', '领养回访专员', 6, '0', 'admin', now(), '负责领养登记与回访计划执行')
on duplicate key update
  post_code = values(post_code), post_name = values(post_name), post_sort = values(post_sort),
  status = '0', remark = values(remark), update_time = now();

-- ============================================================
-- 三、角色（需求文档 3.1：管理员 / 医生 / 助理）
-- ============================================================
delete from sys_role_menu where role_id in (1, 2, 3);
delete from sys_user_role where role_id in (1, 2, 3);
delete from sys_role_dept where role_id in (1, 2, 3);
delete from sys_role where role_id in (1, 2, 3);

insert into sys_role (role_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark) values
(1, '管理员', 'admin',     1, '1', 1, 1, '0', '0', 'admin', now(), '最高权限，全部功能；账号权限、基地物资财务标签、删除档案、查看全部日志'),
(2, '医生',   'doctor',    2, '2', 1, 1, '0', '0', 'admin', now(), '医疗相关权限；不可改系统设置、不可删档案、物资只能查看、财务只能查看账单'),
(3, '助理',   'assistant', 3, '2', 1, 1, '0', '0', 'admin', now(), '基础信息录入、用药打卡、拍照上传、档案新建、物资出入登记、回访录入；不可改历史诊断、不可删档案、不可管理账号与标签库');

-- ============================================================
-- 四、测试账号（密码统一 admin123）
-- ============================================================
delete from sys_user where user_id > 105;

insert into sys_user (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark) values
(1,   201, 'admin',     '李文',   '00', 'liwen@cat-rescue.local',   '13800138000', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now(), '站长，超级管理员'),
(2,   203, 'doctor01',  '王建国', '00', 'wangjg@cat-rescue.local',  '13800138001', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now(), '医疗基地主治医生'),
(100, 203, 'doctor02',  '陈静',   '00', 'chenjing@cat-rescue.local','13800138002', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now(), '医疗基地住院医师'),
(101, 205, 'assist01',  '张小敏', '00', 'zhangxm@cat-rescue.local', '13800138003', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now(), 'A基地区护理助理，管理普通病房A区/隔离观察房/幼猫房'),
(102, 206, 'assist02',  '赵丽',   '00', 'zhaoli@cat-rescue.local',  '13800138004', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now(), 'B基地区护理助理，管理普通病房B区/隔离观察房B/幼猫房B'),
(103, 203, 'assist03',  '孙倩',   '00', 'sunqian@cat-rescue.local', '13800138005', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now(), '医疗基地护理助理，管理治疗病房/传染病隔离房/术后观察房'),
(104, 208, 'stock01',   '周涛',   '00', 'zhoutao@cat-rescue.local', '13800138006', '0', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now(), '物资管理员，负责出入库登记'),
(105, 207, 'adopt01',   '吴倩',   '00', 'wuqian@cat-rescue.local',  '13800138007', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', 'admin', now(), '领养回访专员')
on duplicate key update
  dept_id = values(dept_id), nick_name = values(nick_name), email = values(email),
  phonenumber = values(phonenumber), sex = values(sex), password = values(password),
  status = '0', del_flag = '0', remark = values(remark), update_time = now();

-- 用户 ↔ 角色
insert into sys_user_role (user_id, role_id) values
(1, 1), (2, 2), (100, 2), (101, 3), (102, 3), (103, 3), (104, 3), (105, 3);

-- 用户 ↔ 岗位
delete from sys_user_post where user_id in (1, 2, 100, 101, 102, 103, 104, 105);
insert into sys_user_post (user_id, post_id) values
(1, 1), (2, 2), (100, 3), (101, 4), (102, 4), (103, 4), (104, 5), (105, 6);

-- ============================================================
-- 五、角色菜单权限
-- ============================================================

-- 管理员：全部菜单
insert into sys_role_menu (role_id, menu_id) select 1, menu_id from sys_menu;

-- 医生：医疗为主，基础/档案/物资/财务只读，不含系统设置、标签维护、病房授权、账号管理
insert into sys_role_menu (role_id, menu_id) values
-- 目录
(2,2000),(2,2001),(2,2002),(2,2003),(2,2004),(2,2005),(2,2006),
-- 基础信息（只读）
(2,2010),(2,2011),(2,2020),(2,2021),(2,2030),(2,2031),
-- 猫咪档案（可查看、修改、导出，不可新增/删除）
(2,2040),(2,2041),(2,2043),(2,2045),
(2,2046),(2,2047),
(2,2131),(2,2132),(2,2133),
-- 医疗病历（全权限：疫苗/驱虫/诊断/用药/转诊/死亡）
(2,2050),(2,2051),(2,2052),(2,2053),(2,2054),
(2,2055),(2,2056),(2,2057),(2,2058),(2,2059),
(2,2060),(2,2061),(2,2062),(2,2063),(2,2064),
(2,2065),(2,2066),(2,2067),(2,2068),(2,2069),
(2,2070),(2,2071),(2,2072),(2,2073),(2,2074),
(2,2075),(2,2076),(2,2077),(2,2078),(2,2079),
-- TNR 抓捕/放归
(2,2080),(2,2081),(2,2082),(2,2083),(2,2084),
(2,2085),(2,2086),(2,2087),(2,2088),
-- 领养回访（只读）
(2,2090),(2,2091),(2,2095),(2,2096),
-- 财务物资（只读）
(2,2100),(2,2101),(2,2105),(2,2106),(2,2110),(2,2111),
(2,2115),(2,2116),(2,2120),(2,2121),
-- 提醒 + 看板
(2,2125),(2,2126),(2,2130),
-- 医疗相关导出
(2,2156),(2,2157),(2,2158),(2,2159),(2,2160),(2,2161),(2,2162),(2,2163);

-- 助理：基础录入、用药打卡、拍照、物资出入、回访；不含诊断修改、删档案、标签库、财务录入、病房授权、账号管理
insert into sys_role_menu (role_id, menu_id) values
(3,2000),(3,2001),(3,2002),(3,2003),(3,2004),(3,2005),(3,2006),
-- 基础信息（只读）
(3,2010),(3,2011),(3,2020),(3,2021),(3,2030),(3,2031),
-- 猫咪档案（可新建、修改，不可删除）
(3,2040),(3,2041),(3,2042),(3,2043),
(3,2046),(3,2047),
(3,2131),(3,2132),(3,2133),(3,2134),
-- 疫苗 / 驱虫（可录入修改）
(3,2050),(3,2051),(3,2052),(3,2053),
(3,2055),(3,2056),(3,2057),(3,2058),
-- 诊断（只读，不可改历史诊断）
(3,2060),(3,2061),
-- 用药（每日打卡）
(3,2065),(3,2066),(3,2067),(3,2068),
-- 转诊 / 死亡（只读）
(3,2070),(3,2071),(3,2075),(3,2076),
-- TNR 抓捕 / 放归
(3,2080),(3,2081),(3,2082),(3,2085),(3,2086),
-- 领养（只读）/ 回访（可录入）
(3,2090),(3,2091),(3,2095),(3,2096),(3,2097),(3,2098),
-- 财务只读 / 物资查看 / 出入库登记
(3,2100),(3,2101),(3,2105),(3,2106),(3,2110),(3,2111),
(3,2115),(3,2116),(3,2120),(3,2121),(3,2122),
-- 提醒 + 看板
(3,2125),(3,2126),(3,2130);

-- 若依默认演示账号 ry 改造为 doctor01（user_name 唯一，需单独更新）
update sys_user set user_name = 'doctor01' where user_id = 2 and user_name = 'ry';

-- 测试账号视为已修改过初始密码，避免登录后弹出「密码还是初始密码」提示
update sys_user set pwd_update_date = now()
where user_id in (1, 2, 100, 101, 102, 103, 104, 105) and pwd_update_date is null;
