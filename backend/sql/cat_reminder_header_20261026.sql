-- =============================================================
-- 提醒通知接入顶栏铃铛 / 按人已读 / 目标用户指派
-- 说明：
--   1. 新建 cat_reminder_read 已读记录表，把「已读」从全局单状态改为按用户独立
--   2. 补充 reminder.post.* 收件岗位配置，生成提醒时按岗位写入 target_user
--   3. 顶栏铃铛改为展示业务提醒，把若依自带的演示公告替换为本系统真实公告
-- 可重复执行
-- =============================================================

use cat_rescue;

-- -------------------------------------------------------------
-- 1. 提醒已读记录表（每人对每条提醒各记一条）
-- -------------------------------------------------------------
create table if not exists cat_reminder_read (
  `read_id`     bigint   not null auto_increment comment '已读主键',
  `reminder_id` bigint   not null              comment '提醒id',
  `user_id`     bigint   not null              comment '用户id',
  `read_time`   datetime not null              comment '阅读时间',
  primary key (`read_id`),
  unique key `uk_user_reminder` (`user_id`, `reminder_id`) comment '同一用户同一提醒只记一次',
  key `idx_reminder` (`reminder_id`)
) engine=InnoDB default charset=utf8mb4 collate=utf8mb4_general_ci comment='提醒已读记录表';

-- -------------------------------------------------------------
-- 2. 历史已读状态迁移
--    原 status='1' 的记录统一视为 admin 已读，其余用户仍显示未读
-- -------------------------------------------------------------
insert ignore into cat_reminder_read (reminder_id, user_id, read_time)
select r.reminder_id, 1, ifnull(r.read_time, sysdate())
from cat_reminder r
where r.status = '1';

-- -------------------------------------------------------------
-- 3. 收件岗位配置
--    值为逗号分隔的岗位名称，留空表示全员可见
--    修改后无需重启，下次生成提醒时生效
-- -------------------------------------------------------------
insert ignore into cat_config (config_key, config_value, config_name, remark, create_time) values
('reminder.post.vaccine',  '主治医生,住院医师', '疫苗提醒收件岗位', '逗号分隔的岗位名称，留空则全员可见', sysdate()),
('reminder.post.deworm',   '主治医生,住院医师', '驱虫提醒收件岗位', '逗号分隔的岗位名称，留空则全员可见', sysdate()),
('reminder.post.visit',    '领养回访专员',      '回访提醒收件岗位', '逗号分隔的岗位名称，留空则全员可见', sysdate()),
('reminder.post.stock',    '物资管理员',        '库存提醒收件岗位', '逗号分隔的岗位名称，留空则全员可见', sysdate()),
('reminder.post.referral', '主治医生,住院医师', '转诊提醒收件岗位', '逗号分隔的岗位名称，留空则全员可见', sysdate());

-- -------------------------------------------------------------
-- 4. 顶栏不再显示公告，清理若依自带演示公告，换成真实运营公告
--    （公告管理页仍在「系统管理 - 通知公告」，供发布正式公告用）
-- -------------------------------------------------------------
delete from sys_notice
where notice_title like '温馨提醒：2018-07-01 若依%'
   or notice_title like '维护通知：2018-07-01 若依%'
   or notice_title like '若依开源框架介绍%';

delete from sys_notice_read
where notice_id not in (select notice_id from sys_notice);

insert into sys_notice (notice_id, notice_title, notice_type, notice_content, status, create_by, create_time)
values
(1, '流浪猫救助管理系统正式启用', '1',
 '各位同事：\n\n流浪猫救助管理系统即日起正式启用，档案登记、疫苗驱虫、诊疗用药、外送转诊、领养回访、物资库存等业务统一在本系统办理。\n\n原纸质台账请于本周内完成补录，之后不再并行维护。',
 '0', 'admin', sysdate()),
(2, '业务数据录入规范', '2',
 '为保证台账可追溯，请遵守以下录入要求：\n\n一、猫咪入站当天完成档案登记，同步录入毛色、品种、入站来源。\n二、疫苗、驱虫当天接种当天登记，下次到期日必须填写。\n三、领养回访按 3 天、1 个月、3 个月三个节点安排计划。\n四、物资出入库当日登记，库存低于预警值会生成提醒。',
 '0', 'admin', sysdate())
on duplicate key update
  notice_title   = values(notice_title),
  notice_content = values(notice_content),
  status         = values(status);

-- -------------------------------------------------------------
-- 5. 结果核对
-- -------------------------------------------------------------
select '已读记录表' item, count(*) cnt from cat_reminder_read
union all select '收件岗位配置', count(*) from cat_config where config_key like 'reminder.post.%'
union all select '公告总数', count(*) from sys_notice
union all select '提醒总数', count(*) from cat_reminder;
