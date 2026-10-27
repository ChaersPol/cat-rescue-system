-- ----------------------------
-- 猫咪救助系统：定时任务 & 可配置阈值
-- 可重复执行
-- ----------------------------

-- 1. 到期提醒自动生成任务（每天 08:00）
delete from sys_job where job_id = 100;
insert into sys_job (job_id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
values (100, '猫咪到期提醒自动生成', 'DEFAULT', 'catTask.generateReminder()', '0 0 8 * * ?', '1', '1', '0', 'admin', sysdate(), '每天 08:00 扫描疫苗/驱虫/回访/库存/外送，自动生成提醒');

-- 2. 提醒与阈值配置项
delete from cat_config where config_key in ('reminder.advance.days', 'reminder.overdue.days', 'referral.pending.days', 'reminder.auto.enabled');
insert into cat_config (config_key, config_value, config_name, create_by, create_time) values
('reminder.advance.days', '7', '疫苗/驱虫到期提前提醒天数', 'admin', sysdate()),
('reminder.overdue.days', '7', '疫苗/驱虫过期后仍提醒天数', 'admin', sysdate()),
('referral.pending.days', '7', '外送医院待接回提醒天数', 'admin', sysdate()),
('reminder.auto.enabled', '1', '是否启用提醒自动生成（1开启 0关闭）', 'admin', sysdate());
