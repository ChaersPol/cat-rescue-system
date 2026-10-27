-- ============================================================
-- 超级管理员拆分（可重复执行）
--   1) role_id=1 恢复为「超级管理员」，作为内置角色硬编码保护
--   2) user_id=1 admin 归入超级管理员，并在用户管理中隐藏
--   3) 新建业务角色「管理员」role_id=4，继承全部菜单
--   4) 新建业务管理员账号 manager01
-- 执行：mysql -uroot -p123456 --default-character-set=utf8mb4 cat_rescue < cat_admin_split_20261026.sql
-- ============================================================
set names utf8mb4;

-- ------------------------------------------------------------
-- 1. role_id=1 恢复超级管理员（若依 SysRole.isAdmin() 只认 role_id=1）
-- ------------------------------------------------------------
update sys_role
   set role_name   = '超级管理员',
       role_key    = 'admin',
       role_sort   = 1,
       data_scope  = '1',
       status      = '0',
       del_flag    = '0',
       remark      = '系统内置角色，拥有全部权限；不参与业务分配，不在角色列表中显示'
 where role_id = 1;

-- ------------------------------------------------------------
-- 2. 业务角色「管理员」role_id=4
-- ------------------------------------------------------------
insert into sys_role
    (role_id, role_name, role_key, role_sort, data_scope,
     menu_check_strictly, dept_check_strictly, status, del_flag,
     create_by, create_time, remark)
select 4, '管理员', 'manager', 2, '1', 1, 1, '0', '0',
       'admin', sysdate(), '救助站业务管理员，管理全站业务与基础数据'
  from dual
 where not exists (select 1 from sys_role where role_id = 4);

-- 已存在时复位（避免上次执行残留）
update sys_role
   set role_name  = '管理员',
       role_key   = 'manager',
       role_sort  = 2,
       data_scope = '1',
       status     = '0',
       del_flag   = '0'
 where role_id = 4;

-- ------------------------------------------------------------
-- 3. 医生 / 助理 排序顺延
-- ------------------------------------------------------------
update sys_role set role_sort = 3 where role_key = 'doctor';
update sys_role set role_sort = 4 where role_key = 'assistant';

-- ------------------------------------------------------------
-- 4. 管理员角色继承全部菜单（超管本身靠 isAdmin 放行，不依赖菜单表）
-- ------------------------------------------------------------
delete from sys_role_menu where role_id = 4;
insert into sys_role_menu (role_id, menu_id)
select 4, menu_id from sys_role_menu where role_id = 1;

-- ------------------------------------------------------------
-- 5. admin 只保留超级管理员角色，昵称改为系统超管
-- ------------------------------------------------------------
delete from sys_user_role where user_id = 1;
insert into sys_user_role (user_id, role_id) values (1, 1);

update sys_user
   set nick_name = '系统超管',
       status    = '0',
       del_flag  = '0',
       remark    = '系统内置超级管理员账户，不在用户管理中显示'
 where user_id = 1;

-- ------------------------------------------------------------
-- 6. 业务管理员账号 manager01（密码 admin123）
-- ------------------------------------------------------------
insert into sys_user
    (user_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar,
     password, status, del_flag, login_ip, login_date, pwd_update_date,
     create_by, create_time, update_by, update_time, remark)
select 106, 201, 'manager01', '李文', '00', 'manager01@catrescue.local', '13800000001', '0', '',
       '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '', null, sysdate(),
       'admin', sysdate(), '', null, '站长，业务管理员'
  from dual
 where not exists (select 1 from sys_user where user_name = 'manager01');

-- 已存在时复位关键字段
update sys_user
   set dept_id         = 201,
       nick_name       = '李文',
       password        = '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
       pwd_update_date = sysdate(),
       status          = '0',
       del_flag        = '0',
       remark          = '站长，业务管理员'
 where user_name = 'manager01' and user_id <> 1;

-- ------------------------------------------------------------
-- 7. manager01 岗位 = 站长
-- ------------------------------------------------------------
delete up from sys_user_post up
  join sys_user u on u.user_id = up.user_id
 where u.user_name = 'manager01';

insert into sys_user_post (user_id, post_id)
select u.user_id, 1 from sys_user u where u.user_name = 'manager01';

-- ------------------------------------------------------------
-- 8. manager01 角色 = 管理员（role_id=4）
-- ------------------------------------------------------------
delete ur from sys_user_role ur
  join sys_user u on u.user_id = ur.user_id
 where u.user_name = 'manager01';

insert into sys_user_role (user_id, role_id)
select u.user_id, 4 from sys_user u where u.user_name = 'manager01';

-- ------------------------------------------------------------
-- 9. 结果校验
-- ------------------------------------------------------------
select '=== 角色 ===' as info;
select r.role_id, r.role_name, r.role_key, r.role_sort, r.data_scope,
       (select count(*) from sys_role_menu rm where rm.role_id = r.role_id) as menu_cnt
  from sys_role r order by r.role_sort;

select '=== 用户与角色 ===' as info;
select u.user_id, u.user_name, u.nick_name, d.dept_name,
       group_concat(r.role_name order by r.role_sort) as roles,
       if(u.user_id = 1, '隐藏（硬编码保护）', '列表可见') as visible
  from sys_user u
  left join sys_dept d on d.dept_id = u.dept_id
  left join sys_user_role ur on ur.user_id = u.user_id
  left join sys_role r on r.role_id = ur.role_id
 where u.del_flag = '0'
 group by u.user_id, u.user_name, u.nick_name, d.dept_name
 order by u.user_id;
