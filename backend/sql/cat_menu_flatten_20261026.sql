-- ============================================================
-- 菜单扁平化：取消「救助站管理」一级目录，其下二级菜单提升为一级
-- 执行库：cat_rescue        可重复执行
-- 说明：仅调整 sys_menu 的 parent_id / order_num，并删除空目录 2000
--       菜单 ID 不变，因此角色授权（sys_role_menu）自动保留
-- ============================================================

-- ---------- 1. 二级菜单提升为一级 ----------
update sys_menu set parent_id = 0 where parent_id = 2000;

-- ---------- 2. 一级菜单重新排序 ----------
update sys_menu set order_num = 2  where menu_id = 2001;  -- 基础信息
update sys_menu set order_num = 3  where menu_id = 2002;  -- 猫咪档案
update sys_menu set order_num = 4  where menu_id = 2003;  -- 医疗病历
update sys_menu set order_num = 5  where menu_id = 2004;  -- TNR绝育放归
update sys_menu set order_num = 6  where menu_id = 2005;  -- 领养回访
update sys_menu set order_num = 7  where menu_id = 2006;  -- 财务物资
update sys_menu set order_num = 8  where menu_id = 2125;  -- 提醒通知
update sys_menu set order_num = 9  where menu_id = 2140;  -- 系统配置
update sys_menu set order_num = 10 where menu_id = 1;     -- 系统管理
update sys_menu set order_num = 11 where menu_id = 3;     -- 系统工具（隐藏）
update sys_menu set order_num = 12 where menu_id = 2;     -- 系统监控

-- ---------- 3. 删除已空的「救助站管理」目录 ----------
delete from sys_role_menu where menu_id = 2000;
delete from sys_menu where menu_id = 2000;

-- ---------- 3.4 删除「统计看板」菜单（首页已替代为统计看板内容） ----------
-- 前端 constantRoutes 的首页 /index 已指向 cat/dashboard/index（统计看板组件），
-- 因此数据库里的「统计看板」菜单（menu_id=2130）不再需要，彻底删除避免重复入口。
delete from sys_role_menu where menu_id = 2130;
delete from sys_menu where menu_id = 2130;

-- ---------- 3.5 顶级单页菜单改为「菜单类型 C + is_frame=1」 ----------
-- 关键：这些是「有 component 的单页菜单」（提醒通知/系统配置），
-- 若被配成 menu_type=M（目录），getComponent 会直接返回 cat/xxx/index 而不包 Layout，
-- 导致点击后侧边栏/顶栏消失（全屏）。正确做法是 menu_type=C + is_frame=1（NO_FRAME），
-- 命中 isMenuFrame() 分支，生成 {path:'/', component:Layout, children:[{path:'xxx'}]}，
-- 侧边栏和顶栏才会正常显示。
update sys_menu set menu_type = 'C', is_frame = '1', path = 'reminder'  where menu_id = 2125;  -- 提醒通知
update sys_menu set menu_type = 'C', is_frame = '1', path = 'config'    where menu_id = 2140;  -- 系统配置

-- ---------- 3.6 消除「父目录 path 与子菜单 path 重名」导致的路由 name 冲突 ----------
-- 目录名 = capitalize(path)，子菜单 route_name 为空时也取 capitalize(path)，
-- 二者 path 相同（base/profile/tnr）时 name 重复，addRoute 报 "nested route cannot use the same name"
update sys_menu set route_name = 'BaseIndex'   where menu_id = 2010;  -- 基地管理（父目录 base）
update sys_menu set route_name = 'ProfileIndex' where menu_id = 2040;  -- 猫咪档案（父目录 profile）
update sys_menu set route_name = 'TnrCatch'     where menu_id = 2080;  -- 抓捕登记（父目录 tnr）

-- ---------- 4. 结果自查 ----------
select menu_id, menu_name, menu_type, order_num, path, icon, visible
from sys_menu where parent_id = 0 order by order_num;

select count(*) as 一级菜单数,
       sum(menu_type = 'M') as 目录数,
       sum(menu_type = 'C') as 单页数
from sys_menu where parent_id = 0;

select rm.role_id, r.role_name, count(*) as 菜单数
from sys_role_menu rm join sys_role r on r.role_id = rm.role_id
group by rm.role_id, r.role_name order by rm.role_id;
