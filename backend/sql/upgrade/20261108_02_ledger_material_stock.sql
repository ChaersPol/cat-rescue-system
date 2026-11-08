-- =====================================================================
-- 20261108_02 台账关联物资：用药 / 疫苗 / 驱虫 手工录入也能出库扣库存
-- 影响表：cat_medication、cat_vaccine、cat_deworm、cat_stock_record（只加列）
-- 能否回滚：能，回滚语句见文件末尾（注释形式，需要时手工执行）
--
-- 背景（2026-11-08 与救助站确认）：
--   原来只有「站内诊断」开单会按目录里关联的物资自动出库，其余三个台账
--   （用药记录、疫苗台账、驱虫台账）手工录入时既不能选物资、也不动库存，
--   导致「药已经用了，库存账还是满的」。本次给三张台账加上物资关联，
--   手工录入时可选物资并填本次用量，保存即生成一张出库单。
--
--   口径（关键，别改）：
--   1. 一次发药只出库一次。用药记录「连续 N 天」批量录入时，库存只按
--      填写的「本次用量」扣一次，天数只决定生成多少条打卡任务。
--      与诊断开方一致（诊断按明细数量出库一次，处方写几天不影响出库量）。
--   2. 库存允许为负。现场急救不能被"库存不足"卡住，页面红字提示，
--      事后补入库时库存自然回正。
--   3. 处方/诊断自动生成的明细不重复出库：那些行的 material_id 留空，
--      出库已经由诊断单那条明细完成了。
--
-- 幂等：按 information_schema 判断列是否齐全，四张表各判一次，可重复执行。
-- =====================================================================

USE cat_rescue;

-- 1. 用药记录：关联物资 + 名称快照 + 本次消耗数量
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_medication'
              AND COLUMN_NAME IN ('material_id', 'material_name', 'stock_qty'));
SET @sql := IF(@c = 3, 'DO 0',
  'ALTER TABLE cat_medication
     ADD COLUMN material_id BIGINT NULL COMMENT ''关联物资ID（空=不扣库存）'',
     ADD COLUMN material_name VARCHAR(100) NULL COMMENT ''物资名称快照'',
     ADD COLUMN stock_qty DECIMAL(12,2) NULL COMMENT ''本次消耗数量''');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 2. 疫苗台账
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_vaccine'
              AND COLUMN_NAME IN ('material_id', 'material_name', 'stock_qty'));
SET @sql := IF(@c = 3, 'DO 0',
  'ALTER TABLE cat_vaccine
     ADD COLUMN material_id BIGINT NULL COMMENT ''关联物资ID（空=不扣库存）'',
     ADD COLUMN material_name VARCHAR(100) NULL COMMENT ''物资名称快照'',
     ADD COLUMN stock_qty DECIMAL(12,2) NULL COMMENT ''本次消耗数量''');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3. 驱虫台账
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_deworm'
              AND COLUMN_NAME IN ('material_id', 'material_name', 'stock_qty'));
SET @sql := IF(@c = 3, 'DO 0',
  'ALTER TABLE cat_deworm
     ADD COLUMN material_id BIGINT NULL COMMENT ''关联物资ID（空=不扣库存）'',
     ADD COLUMN material_name VARCHAR(100) NULL COMMENT ''物资名称快照'',
     ADD COLUMN stock_qty DECIMAL(12,2) NULL COMMENT ''本次消耗数量''');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 4. 出入库记录：记住这张出库单是哪条业务产生的，改/删时才能准确冲销
--    ref_type：1 用药记录 2 疫苗台账 3 驱虫台账（诊断仍走 diagnosis_id，不动）
SET @c := (SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_stock_record'
              AND COLUMN_NAME IN ('ref_type', 'ref_id'));
SET @sql := IF(@c = 2, 'DO 0',
  'ALTER TABLE cat_stock_record
     ADD COLUMN ref_type CHAR(1) NULL COMMENT ''业务类型：1用药 2疫苗 3驱虫'',
     ADD COLUMN ref_id BIGINT NULL COMMENT ''业务主键（对应台账表主键）''');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @c := (SELECT COUNT(*) FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cat_stock_record'
              AND INDEX_NAME = 'idx_stock_ref');
SET @sql := IF(@c > 0, 'DO 0',
  'ALTER TABLE cat_stock_record ADD INDEX idx_stock_ref (ref_type, ref_id)');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ---------------------------------------------------------------------
-- 回滚（需要时手工执行）：
-- ALTER TABLE cat_medication DROP COLUMN material_id, DROP COLUMN material_name, DROP COLUMN stock_qty;
-- ALTER TABLE cat_vaccine    DROP COLUMN material_id, DROP COLUMN material_name, DROP COLUMN stock_qty;
-- ALTER TABLE cat_deworm     DROP COLUMN material_id, DROP COLUMN material_name, DROP COLUMN stock_qty;
-- ALTER TABLE cat_stock_record DROP INDEX idx_stock_ref, DROP COLUMN ref_type, DROP COLUMN ref_id;
-- ---------------------------------------------------------------------
