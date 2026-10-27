-- ================================================================
-- 流浪动物救助管理系统 业务表结构
-- 说明：基于 RuoYi-Vue springboot3 框架扩展，业务表统一 cat_ 前缀
-- ================================================================

-- ----------------------------
-- 1、基地表
-- ----------------------------
drop table if exists cat_base;
create table cat_base (
  base_id           bigint(20)      not null auto_increment    comment '基地id',
  base_name         varchar(50)     not null                   comment '基地名称',
  base_type         char(1)         default '1'                comment '基地类型（1饲养基地 2医疗基地 3放归点位）',
  leader            varchar(30)     default null               comment '基地负责人',
  phone             varchar(20)     default null               comment '联系电话',
  address           varchar(200)    default null               comment '基地地址',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  primary key (base_id)
) engine=innodb auto_increment=100 comment = '基地表';

-- ----------------------------
-- 2、病房/区域表（基地下层级）
-- ----------------------------
drop table if exists cat_ward;
create table cat_ward (
  ward_id           bigint(20)      not null auto_increment    comment '病房id',
  base_id           bigint(20)      not null                   comment '所属基地id',
  ward_name         varchar(50)     not null                   comment '病房名称',
  ward_type         varchar(30)     default ''                 comment '病房类型（犬房/犬猫房/普通病房等，可自定义）',
  order_num         int(4)          default 0                  comment '显示顺序',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  primary key (ward_id)
) engine=innodb auto_increment=100 comment = '病房/区域表';

-- ----------------------------
-- 3、标签库表
-- ----------------------------
drop table if exists cat_tag;
create table cat_tag (
  tag_id            bigint(20)      not null auto_increment    comment '标签id',
  tag_name          varchar(50)     not null                   comment '标签名称',
  tag_group         varchar(30)     default ''                 comment '标签分组（疾病/性格/年龄等）',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  primary key (tag_id)
) engine=innodb auto_increment=100 comment = '标签库表';

-- ----------------------------
-- 4、猫咪档案表（核心）
-- ----------------------------
drop table if exists cat_profile;
create table cat_profile (
  cat_id            bigint(20)      not null auto_increment    comment '猫咪id',
  archive_no        varchar(32)     not null                   comment '档案编号（唯一）',
  cat_name          varchar(50)     not null                   comment '猫咪名字',
  alias             varchar(50)     default ''                 comment '别名',
  breed             varchar(50)     default ''                 comment '品种',
  sex               char(1)         default '2'                comment '性别（0公 1母 2未知）',
  age_estimate      varchar(30)     default ''                 comment '大概年龄（估算）',
  weight            varchar(30)     default ''                 comment '体重',
  personality       varchar(200)    default ''                 comment '性格',
  source            char(1)         default '1'                comment '来源（1抓捕/TNR 2救助捡到 3他人移交 4其他）',
  capture_date      date                                        comment '抓捕/救助日期',
  capture_point     varchar(200)    default ''                 comment '抓捕点位（投喂点地址）',
  neutered          char(1)         default '0'                comment '是否绝育（0未绝育 1已绝育 2未知）',
  dewormed          char(1)         default '0'                comment '是否驱虫（0未驱虫 1已驱虫）',
  vaccinated        char(1)         default '0'                comment '是否疫苗（0未疫苗 1已疫苗）',
  feature           varchar(500)    default ''                 comment '标志性特征（伤疤缺耳断尾异色眼等）',
  status            char(1)         default '1'                comment '猫咪状态（1在养 2基地内治疗中 3外送医院治疗中 4已放归 5已领养 6已离世 7TNR治疗中 8姑息照料）',
  base_id           bigint(20)      default null               comment '当前所在基地id',
  ward_id           bigint(20)      default null               comment '当前所在病房id',
  owner_base_id     bigint(20)      default null               comment '归属基地id',
  hospital_name     varchar(100)    default ''                 comment '外送医院名称（状态为外送时）',
  qr_code           mediumtext                                 comment '二维码图片地址（Base64）',
  archived          char(1)         default '0'                comment '是否归档（0未归档 1已归档）',
  remark            varchar(1000)   default null               comment '备注（应激程度、是否怕人、特殊习性）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  primary key (cat_id),
  unique key uk_archive_no (archive_no),
  key idx_status (status),
  key idx_base (base_id),
  key idx_owner_base (owner_base_id)
) engine=innodb auto_increment=100 comment = '猫咪档案表';

-- ----------------------------
-- 5、猫咪标签关联表
-- ----------------------------
drop table if exists cat_profile_tag;
create table cat_profile_tag (
  cat_id            bigint(20)      not null                   comment '猫咪id',
  tag_id            bigint(20)      not null                   comment '标签id',
  primary key (cat_id, tag_id)
) engine=innodb comment = '猫咪标签关联表';

-- ----------------------------
-- 6、猫咪照片相册表
-- ----------------------------
drop table if exists cat_photo;
create table cat_photo (
  photo_id          bigint(20)      not null auto_increment    comment '照片id',
  cat_id            bigint(20)      not null                   comment '猫咪id',
  folder            varchar(30)     default '正面照'            comment '文件夹（正面照/侧面/伤口/术前/术后）',
  file_type         char(1)         default '1'                comment '文件类型（1图片 2视频）',
  file_url          varchar(500)    not null                   comment '文件地址',
  thumb_url         varchar(500)    default ''                 comment '缩略图地址（列表/相册展示用，图片自动缩放、视频自动抽帧）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (photo_id),
  key idx_cat (cat_id)
) engine=innodb auto_increment=100 comment = '猫咪照片相册表';

-- ----------------------------
-- 7、流转记录表（不可删除）
-- ----------------------------
drop table if exists cat_flow;
create table cat_flow (
  flow_id           bigint(20)      not null auto_increment    comment '流转记录id',
  cat_id            bigint(20)      not null                   comment '猫咪id',
  from_base_id      bigint(20)      default null               comment '转出基地id',
  from_ward_id      bigint(20)      default null               comment '转出病房id',
  to_base_id        bigint(20)      default null               comment '转入基地id',
  to_ward_id        bigint(20)      default null               comment '转入病房id',
  flow_type         varchar(30)     not null                   comment '流转类型（发病转入治疗/治愈返回/外送医院/医院返回/转入姑息/领养/放归/死亡）',
  flow_reason       varchar(500)    default ''                 comment '流转原因',
  hospital_name     varchar(100)    default ''                 comment '医院名称（外送时）',
  hospital_result   varchar(30)     default ''                 comment '医院处置结论（治愈出院/安乐/离世/预后不良）',
  operate_by        varchar(64)     default ''                 comment '操作人',
  operate_time      datetime                                   comment '操作时间',
  remark            varchar(500)    default null               comment '备注',
  create_time       datetime                                   comment '创建时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (flow_id),
  key idx_cat (cat_id)
) engine=innodb auto_increment=100 comment = '流转记录表';

-- ----------------------------
-- 8、疫苗记录表
-- ----------------------------
drop table if exists cat_vaccine;
create table cat_vaccine (
  vaccine_id        bigint(20)      not null auto_increment    comment '疫苗记录id',
  cat_id            bigint(20)      not null                   comment '猫咪id',
  vaccine_type      varchar(50)     default ''                 comment '疫苗类型（第一针/第二针/狂犬等）',
  vaccine_date      date                                        comment '接种日期',
  next_date         date                                        comment '下次接种时间',
  responsible       varchar(30)     default ''                 comment '负责人（助理）',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  primary key (vaccine_id),
  key idx_cat (cat_id),
  key idx_next_date (next_date)
) engine=innodb auto_increment=100 comment = '疫苗记录表';

-- ----------------------------
-- 9、驱虫记录表
-- ----------------------------
drop table if exists cat_deworm;
create table cat_deworm (
  deworm_id         bigint(20)      not null auto_increment    comment '驱虫记录id',
  cat_id            bigint(20)      not null                   comment '猫咪id',
  deworm_type       varchar(30)     default '体内驱虫'          comment '驱虫类型（体内/体外）',
  deworm_drug       varchar(100)    default ''                 comment '驱虫药',
  deworm_date       date                                        comment '驱虫日期',
  next_date         date                                        comment '下次驱虫时间',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  primary key (deworm_id),
  key idx_cat (cat_id),
  key idx_next_date (next_date)
) engine=innodb auto_increment=100 comment = '驱虫记录表';

-- ----------------------------
-- 10、站内诊断记录表
-- ----------------------------
drop table if exists cat_diagnosis;
create table cat_diagnosis (
  diagnosis_id      bigint(20)      not null auto_increment    comment '诊断记录id',
  cat_id            bigint(20)      not null                   comment '猫咪id',
  base_id           bigint(20)      default null               comment '当前治疗基地id',
  visit_date        datetime                                   comment '就诊日期',
  doctor            varchar(30)     default ''                 comment '操作医生',
  symptoms          varchar(500)    default ''                 comment '主诉/症状',
  diagnosis_result  varchar(500)    default ''                 comment '初步诊断结果',
  check_items       varchar(500)    default ''                 comment '检查项目（体温血常规皮肤镜检等）',
  medical_advice    varchar(1000)   default ''                 comment '医嘱、预后判断',
  check_photos      varchar(2000)   default ''                 comment '检查照片/化验单（逗号分隔）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  remark            varchar(500)    default null               comment '备注',
  primary key (diagnosis_id),
  key idx_cat (cat_id)
) engine=innodb auto_increment=100 comment = '站内诊断记录表';

-- ----------------------------
-- 11、每日用药记录表
-- ----------------------------
drop table if exists cat_medication;
create table cat_medication (
  medication_id     bigint(20)      not null auto_increment    comment '用药记录id',
  cat_id            bigint(20)      not null                   comment '猫咪id',
  use_date          date                                        comment '用药日期',
  time_period       varchar(10)     default ''                 comment '时间段（早/中/晚）',
  drug_name         varchar(100)    default ''                 comment '药品名称',
  dosage            varchar(50)     default ''                 comment '剂量',
  administration    varchar(30)     default '口服'              comment '给药方式（口服/注射/外用）',
  executor          varchar(30)     default ''                 comment '用药执行人',
  adverse_reaction  varchar(500)    default ''                 comment '不良反应记录',
  remark            varchar(500)    default null               comment '用药备注',
  photos            varchar(2000)   default ''                 comment '照片（逗号分隔）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  primary key (medication_id),
  key idx_cat (cat_id),
  key idx_date (use_date)
) engine=innodb auto_increment=100 comment = '每日用药记录表';

-- ----------------------------
-- 12、外送医院转诊记录表
-- ----------------------------
drop table if exists cat_referral;
create table cat_referral (
  referral_id       bigint(20)      not null auto_increment    comment '转诊记录id',
  cat_id            bigint(20)      not null                   comment '猫咪id',
  send_date         datetime                                   comment '送医时间',
  send_reason       varchar(500)    default ''                 comment '送医原因（站内无法处理）',
  hospital_name     varchar(100)    default ''                 comment '送往医院名称',
  hospital_check    varchar(500)    default ''                 comment '医院检查结果',
  hospital_diagnosis varchar(500)   default ''                 comment '医院诊断',
  hospital_prescription varchar(1000) default ''               comment '医院处方',
  files             varchar(2000)   default ''                 comment '病历化验单照片视频（逗号分隔）',
  result            varchar(30)     default ''                 comment '医院处置结论（治愈出院/预后不良转基地/安乐/离世）',
  discharge_to      varchar(30)     default ''                 comment '出院去向（返回医疗基地/返回原基地/转入指定基地）',
  target_base_id    bigint(20)      default null               comment '转入指定基地id',
  discharge_advice  varchar(1000)   default ''                 comment '出院医嘱、康复评估',
  status            char(1)         default '0'                comment '状态（0治疗中 1已出院 2已离世）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  remark            varchar(500)    default null               comment '备注',
  primary key (referral_id),
  key idx_cat (cat_id)
) engine=innodb auto_increment=100 comment = '外送医院转诊记录表';

-- ----------------------------
-- 13、死亡台账表
-- ----------------------------
drop table if exists cat_death;
create table cat_death (
  death_id          bigint(20)      not null auto_increment    comment '死亡记录id',
  cat_id            bigint(20)      not null                   comment '猫咪id',
  death_date        datetime                                   comment '死亡日期',
  death_place       varchar(100)    default ''                 comment '死亡地点（XX基地/合作宠物医院）',
  death_type        char(1)         default '1'                comment '死亡类型（1自然寿终 2病重不治 3安乐 4意外）',
  death_reason      varchar(1000)   default ''                 comment '死因描述',
  history           varchar(2000)   default ''                 comment '既往病史自动带入（可补充）',
  body_disposal     varchar(500)    default ''                 comment '遗体处理方式备注',
  doctor            varchar(30)     default ''                 comment '操作医生',
  registrar         varchar(30)     default ''                 comment '登记人',
  photos            varchar(2000)   default ''                 comment '照片（逗号分隔）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  remark            varchar(500)    default null               comment '备注',
  primary key (death_id),
  key idx_cat (cat_id)
) engine=innodb auto_increment=100 comment = '死亡台账表';

-- ----------------------------
-- 14、TNR抓捕登记表
-- ----------------------------
drop table if exists cat_tnr;
create table cat_tnr (
  tnr_id            bigint(20)      not null auto_increment    comment 'TNR记录id',
  cat_id            bigint(20)      default null               comment '关联猫咪档案id',
  capture_time      datetime                                   comment '抓捕时间',
  capture_point     varchar(200)    default ''                 comment '抓捕点位（投喂点）',
  cat_name          varchar(50)     default ''                 comment '猫咪信息（未建档时临时记录）',
  ear_marked        char(1)         default '0'                comment '是否剪耳标记（0否 1是）',
  surgery_date      date                                        comment '绝育手术日期',
  surgeon           varchar(30)     default ''                 comment '手术医生',
  surgery_record    varchar(1000)   default ''                 comment '手术记录',
  pre_post_check    varchar(1000)   default ''                 comment '术前术后检查',
  anesthesia_note   varchar(500)    default ''                 comment '麻醉备注',
  post_observation  varchar(1000)   default ''                 comment '术后观察记录',
  result            char(1)         default '0'                comment '结果（0待定 1放归 2转入基地饲养）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  remark            varchar(500)    default null               comment '备注',
  primary key (tnr_id),
  key idx_cat (cat_id)
) engine=innodb auto_increment=100 comment = 'TNR抓捕登记表';

-- ----------------------------
-- 15、TNR放归登记表
-- ----------------------------
drop table if exists cat_release;
create table cat_release (
  release_id        bigint(20)      not null auto_increment    comment '放归记录id',
  cat_id            bigint(20)      not null                   comment '猫咪id',
  tnr_id            bigint(20)      default null               comment '关联TNR记录id',
  release_date      datetime                                   comment '放归日期',
  release_point     varchar(200)    default ''                 comment '放归点位（投喂点）',
  release_status    varchar(200)    default ''                 comment '放归时状态',
  marked            char(1)         default '1'                comment '标记状态（1已放归）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  remark            varchar(500)    default null               comment '备注',
  primary key (release_id),
  key idx_cat (cat_id)
) engine=innodb auto_increment=100 comment = 'TNR放归登记表';

-- ----------------------------
-- 16、领养登记表
-- ----------------------------
drop table if exists cat_adoption;
create table cat_adoption (
  adoption_id       bigint(20)      not null auto_increment    comment '领养记录id',
  cat_id            bigint(20)      not null                   comment '猫咪id',
  adopter_name      varchar(50)     default ''                 comment '领养人姓名',
  adopter_phone     varchar(20)     default ''                 comment '领养人电话',
  adopter_address   varchar(200)    default ''                 comment '领养人地址',
  adoption_date     date                                        comment '领养日期',
  agreement_url     varchar(500)    default ''                 comment '领养协议上传地址',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  remark            varchar(500)    default null               comment '备注',
  primary key (adoption_id),
  key idx_cat (cat_id)
) engine=innodb auto_increment=100 comment = '领养登记表';

-- ----------------------------
-- 17、回访记录表
-- ----------------------------
drop table if exists cat_visit;
create table cat_visit (
  visit_id          bigint(20)      not null auto_increment    comment '回访记录id',
  adoption_id       bigint(20)      not null                   comment '领养记录id',
  cat_id            bigint(20)      default null               comment '猫咪id',
  plan_type         varchar(20)     default ''                 comment '回访计划（3天/1个月/3个月）',
  plan_date         date                                        comment '计划回访日期',
  visit_status      char(1)         default '0'                comment '回访状态（0待回访 1已回访 2回访异常）',
  visit_content     varchar(1000)   default ''                 comment '回访内容',
  photos            varchar(2000)   default ''                 comment '回访照片（逗号分隔）',
  visit_by          varchar(30)     default ''                 comment '回访人（助理）',
  visit_time        datetime                                   comment '实际回访时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (visit_id),
  key idx_adoption (adoption_id),
  key idx_plan_date (plan_date)
) engine=innodb auto_increment=100 comment = '回访记录表';

-- ----------------------------
-- 18、费用分类表
-- ----------------------------
drop table if exists cat_fee_category;
create table cat_fee_category (
  category_id       bigint(20)      not null auto_increment    comment '分类id',
  category_name     varchar(50)     not null                   comment '费用分类名称',
  category_type     char(1)         default '1'                comment '费用类型（1物资采购 2医疗外送 3其他支出）',
  order_num         int(4)          default 0                  comment '显示顺序',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  primary key (category_id)
) engine=innodb auto_increment=100 comment = '费用分类表';

-- ----------------------------
-- 19、财务支出表（采购/医疗外送/其他）
-- ----------------------------
drop table if exists cat_expense;
create table cat_expense (
  expense_id        bigint(20)      not null auto_increment    comment '支出id',
  category_id       bigint(20)      default null               comment '费用分类id',
  expense_type      char(1)         default '1'                comment '费用类型（1物资采购 2医疗外送 3其他支出）',
  expense_date      date                                        comment '支出日期（采购日期/外送费用日期）',
  supplier          varchar(100)    default ''                 comment '供应商',
  items             varchar(1000)   default ''                 comment '采购物资清单/费用说明',
  amount            decimal(12,2)   default 0                  comment '总金额',
  pay_method        varchar(30)     default ''                 comment '付款方式',
  receipt_url       varchar(500)    default ''                 comment '付款截图/票据上传',
  base_id           bigint(20)      default null               comment '归属基地id',
  cat_id            bigint(20)      default null               comment '关联猫咪id（医疗外送时）',
  referral_id       bigint(20)      default null               comment '关联转诊记录id（医疗外送时）',
  stock_id          bigint(20)      default null               comment '关联入库单id（物资采购时）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  remark            varchar(500)    default null               comment '备注',
  primary key (expense_id),
  key idx_type (expense_type),
  key idx_cat (cat_id)
) engine=innodb auto_increment=100 comment = '财务支出表';

-- ----------------------------
-- 20、物资分类表
-- ----------------------------
drop table if exists cat_material_category;
create table cat_material_category (
  category_id       bigint(20)      not null auto_increment    comment '分类id',
  category_name     varchar(50)     not null                   comment '物资分类名称',
  order_num         int(4)          default 0                  comment '显示顺序',
  status            char(1)         default '0'                comment '状态（0正常 1停用）',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  primary key (category_id)
) engine=innodb auto_increment=100 comment = '物资分类表';

-- ----------------------------
-- 21、物资表（含库存）
-- ----------------------------
drop table if exists cat_material;
create table cat_material (
  material_id       bigint(20)      not null auto_increment    comment '物资id',
  category_id       bigint(20)      default null               comment '物资分类id',
  material_name     varchar(100)    not null                   comment '物资名称',
  spec              varchar(100)    default ''                 comment '规格',
  unit              varchar(20)     default ''                 comment '单位',
  stock             decimal(12,2)   default 0                  comment '当前库存',
  min_stock         decimal(12,2)   default 0                  comment '库存预警阈值',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  del_flag          char(1)         default '0'                comment '删除标志（0存在 2删除）',
  remark            varchar(500)    default null               comment '备注',
  primary key (material_id),
  key idx_category (category_id)
) engine=innodb auto_increment=100 comment = '物资表';

-- ----------------------------
-- 22、物资出入库记录表（不可删除）
-- ----------------------------
drop table if exists cat_stock_record;
create table cat_stock_record (
  record_id         bigint(20)      not null auto_increment    comment '记录id',
  material_id       bigint(20)      not null                   comment '物资id',
  record_type       char(1)         not null                   comment '类型（1入库 2出库）',
  quantity          decimal(12,2)   not null                   comment '数量',
  record_date       datetime                                   comment '出入库日期',
  supplier          varchar(100)    default ''                 comment '供应商（入库）',
  receiver_base     varchar(100)    default ''                 comment '领用基地（出库）',
  receiver          varchar(30)     default ''                 comment '领用人（出库）',
  purpose           varchar(200)    default ''                 comment '用途',
  operator          varchar(30)     default ''                 comment '操作人',
  expense_id        bigint(20)      default null               comment '关联采购财务单id',
  remark            varchar(500)    default null               comment '备注',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (record_id),
  key idx_material (material_id)
) engine=innodb auto_increment=100 comment = '物资出入库记录表';

-- ----------------------------
-- 23、提醒通知表
-- ----------------------------
drop table if exists cat_reminder;
create table cat_reminder (
  reminder_id       bigint(20)      not null auto_increment    comment '提醒id',
  cat_id            bigint(20)      default null               comment '关联猫咪id',
  reminder_type     varchar(30)     not null                   comment '提醒类型（疫苗/驱虫/转诊出院/库存/回访）',
  title             varchar(200)    default ''                 comment '提醒标题',
  content           varchar(500)    default ''                 comment '提醒内容',
  remind_date       date                                        comment '提醒日期',
  status            char(1)         default '0'                comment '状态（0未读 1已读）',
  target_user       varchar(64)     default ''                 comment '目标用户（为空则全员）',
  read_time         datetime                                   comment '已读时间',
  create_time       datetime                                   comment '创建时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (reminder_id),
  key idx_remind_date (remind_date),
  key idx_cat (cat_id)
) engine=innodb auto_increment=100 comment = '提醒通知表';

-- ----------------------------
-- 24、病房授权表（助理-病房管理关系）
-- ----------------------------
drop table if exists cat_ward_auth;
create table cat_ward_auth (
  user_id           bigint(20)      not null                   comment '用户id（助理）',
  ward_id           bigint(20)      not null                   comment '病房id',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  primary key (user_id, ward_id)
) engine=innodb comment = '病房授权表';

-- ----------------------------
-- 25、系统配置表（扫码权限等开关）
-- ----------------------------
drop table if exists cat_config;
create table cat_config (
  config_id         bigint(20)      not null auto_increment    comment '配置id',
  config_key        varchar(50)     not null                   comment '配置键',
  config_value      varchar(500)    default ''                 comment '配置值',
  config_name       varchar(100)    default ''                 comment '配置说明',
  remark            varchar(500)    default null               comment '备注',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  primary key (config_id),
  unique key uk_config_key (config_key)
) engine=innodb auto_increment=100 comment = '系统配置表';

-- ----------------------------
-- 初始化数据
-- ----------------------------
-- 扫码权限配置：A=扫码仅看基础信息，医疗记录需登录；B=内部模式直接看全部病历
insert into cat_config (config_key, config_value, config_name) values ('scan.permission.mode', 'B', '扫码权限方案（A/B）');
-- 扫码前端地址前缀（部署后配置，例如 https://cat.example.com）
insert into cat_config (config_key, config_value, config_name) values ('scan.base.url', '', '扫码前端地址前缀');

-- 默认费用分类
insert into cat_fee_category (category_name, category_type, order_num) values ('猫粮', '1', 1);
insert into cat_fee_category (category_name, category_type, order_num) values ('猫砂', '1', 2);
insert into cat_fee_category (category_name, category_type, order_num) values ('药品', '1', 3);
insert into cat_fee_category (category_name, category_type, order_num) values ('耗材', '1', 4);
insert into cat_fee_category (category_name, category_type, order_num) values ('消毒用品', '1', 5);
insert into cat_fee_category (category_name, category_type, order_num) values ('医疗外送费用', '2', 6);
insert into cat_fee_category (category_name, category_type, order_num) values ('运输', '3', 7);
insert into cat_fee_category (category_name, category_type, order_num) values ('笼具采购', '3', 8);

-- 默认物资分类
insert into cat_material_category (category_name, order_num) values ('药品', 1);
insert into cat_material_category (category_name, order_num) values ('耗材', 2);
insert into cat_material_category (category_name, order_num) values ('猫粮', 3);
insert into cat_material_category (category_name, order_num) values ('猫砂', 4);
insert into cat_material_category (category_name, order_num) values ('消毒用品', 5);
insert into cat_material_category (category_name, order_num) values ('笼具', 6);
