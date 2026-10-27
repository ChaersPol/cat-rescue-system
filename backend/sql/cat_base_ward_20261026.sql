-- ============================================================
-- 基地 / 病房 / 病房授权 测试数据
-- 依据需求文档 4.1（A基地、B基地、医疗基地、放归点位）
-- 与 15（助理分别管理不同病房，管理员可划分）
-- 可重复执行
-- ============================================================

-- ---------- 1. 基地：补 B 基地 ----------
insert into cat_base (base_id, base_name, base_type, leader, phone, address, status, remark, create_by, create_time) values
(100, '流浪猫救助基地', 1, '张小敏', '13800138003', '城关街道滨江路 128 号', '0', 'A基地：主饲养基地，健康待领养猫咪', 'admin', now()),
(101, '合作医疗基地',     2, '王建国', '13800138001', '人民医院后街 26 号',   '0', '医疗基地：站内诊疗、隔离、术后观察', 'admin', now()),
(102, '河湾放归点位',         3, '赵敏',   '13800138008', '滨江一桥东侧河湾绿化带', '0', '野外投喂点，放归猫咪不回基地饲养', 'admin', now()),
(103, '北苑流浪猫救助基地', 1, '赵丽', '13800138004', '北苑小区后巷 8 号',   '0', 'B基地：次饲养基地，接收姑息照料/长期护理猫咪', 'admin', now())
on duplicate key update
  base_name = values(base_name), base_type = values(base_type), leader = values(leader),
  phone = values(phone), address = values(address), status = '0',
  remark = values(remark), update_time = now();

-- ---------- 2. 病房 ----------
insert into cat_ward (ward_id, base_id, ward_name, ward_type, order_num, status, remark, create_by, create_time) values
(100, 100, '普通病房 A 区',   '普通病房',   1, '0', 'A基地健康待领养猫咪', 'admin', now()),
(101, 100, '隔离观察房',      '隔离病房',   2, '0', 'A基地新入站/疑似传染病隔离', 'admin', now()),
(102, 100, '术后观察房',      '术后病房',   3, '0', '绝育/外科术后 72 小时观察', 'admin', now()),
(103, 101, '治疗病房 1 号',   '治疗病房',   1, '0', '医疗基地站内可处理的内科治疗', 'admin', now()),
(104, 101, '传染病隔离房',    '隔离病房',   2, '0', '猫瘟/猫鼻支等传染病专用', 'admin', now()),
(105, 100, '康复活动区',      '康复区',     4, '0', '长期照料/姑息照料猫咪活动区', 'admin', now()),
(106, 100, '幼猫房',          '幼猫病房',   5, '0', 'A基地离乳期幼猫、需要人工喂养', 'admin', now()),
(107, 103, '普通病房 B 区',   '普通病房',   1, '0', 'B基地健康待领养猫咪', 'admin', now()),
(108, 103, '隔离观察房 B',    '隔离病房',   2, '0', 'B基地新入站隔离', 'admin', now()),
(109, 103, '幼猫房 B',        '幼猫病房',   3, '0', 'B基地离乳期幼猫', 'admin', now())
on duplicate key update
  base_id = values(base_id), ward_name = values(ward_name), ward_type = values(ward_type),
  order_num = values(order_num), status = '0', remark = values(remark), update_time = now();

-- ---------- 3. 病房授权（不同助理管不同病房）----------
-- 医生与管理员不参与病房隔离，先清掉可能残留的授权
delete from cat_ward_auth where user_id in (2, 101, 102, 103, 104, 105);

-- 张小敏（assist01，A基地区护理组）：A基地普通病房 / 隔离房 / 幼猫房
insert into cat_ward_auth (user_id, ward_id, create_by, create_time) values
(101, 100, 'admin', now()), (101, 101, 'admin', now()), (101, 106, 'admin', now());

-- 赵丽（assist02，B基地区护理组）：B基地普通病房 / 隔离房 / 幼猫房
insert into cat_ward_auth (user_id, ward_id, create_by, create_time) values
(102, 107, 'admin', now()), (102, 108, 'admin', now()), (102, 109, 'admin', now());

-- 孙倩（assist03，医疗基地诊疗组）：治疗病房 / 传染病隔离房 / 术后观察房 / 康复区
insert into cat_ward_auth (user_id, ward_id, create_by, create_time) values
(103, 103, 'admin', now()), (103, 104, 'admin', now()), (103, 102, 'admin', now()), (103, 105, 'admin', now());

-- ---------- 4. 补齐医疗基地猫咪的病房 ----------
update cat_profile set ward_id = 103 where cat_id = 107 and ward_id is null;  -- 灰灰：治疗病房 1 号
update cat_profile set ward_id = 104 where cat_id = 108 and ward_id is null;  -- 三条腿：传染病隔离房

-- ---------- 5. B 基地（基地 103）在册猫咪 ----------
delete from cat_profile where cat_id in (120, 121, 122, 123);
insert into cat_profile
(cat_id, archive_no, cat_name, alias, breed, sex, age_estimate, weight, personality, source,
 capture_date, capture_point, neutered, dewormed, vaccinated, feature, status,
 base_id, ward_id, owner_base_id, archived, remark, create_by, create_time) values
(120, 'CAT202610100021', '汤圆', '', '中华田园猫', '1', '1岁2个月', '3.2kg', '亲人、爱蹭人', '2',
 '2026-10-10', '北苑小区后巷投喂点', '1', '1', '1', '白色短毛，尾巴末端有灰斑', '1',
 103, 107, 103, '0', '新入B基地，已完成绝育与首针疫苗', 'assist02', '2026-10-10 09:20:00'),
(121, 'CAT202610120022', '小豆', '', '狸花猫', '0', '8个月', '2.6kg', '活泼好动', '1',
 '2026-10-12', '北苑小区垃圾房', '1', '1', '1', '狸花纹，左耳剪耳标记', '1',
 103, 107, 103, '0', 'TNR抓捕后转入B基地等待领养', 'assist02', '2026-10-12 16:40:00'),
(122, 'CAT202610150023', '芝麻', '', '中华田园猫', '0', '2岁', '3.8kg', '胆小怕人', '2',
 '2026-10-15', '北苑菜市场后门', '0', '1', '0', '黑色短毛，右前爪有白点', '2',
 103, 108, 103, '0', '入站后出现轻度猫鼻支，隔离观察中', 'assist02', '2026-10-15 11:05:00'),
(123, 'CAT202610180024', '毛豆', '', '中华田园猫', '1', '3个月', '1.1kg', '黏人、需人工喂食', '2',
 '2026-10-18', '北苑小区绿化带', '0', '1', '0', '黄白花，体型偏小', '1',
 103, 109, 103, '0', '幼猫，单独放幼猫房 B，每日三次喂食', 'assist02', '2026-10-18 08:30:00');
