-- =====================================================================
-- 20261108_03 补齐「形象照目录」演示照片（花卷 4 张）
-- 影响表：cat_photo（新增 4 行；不新增表、不改结构）
-- 能否回滚：能，回滚语句见文件末尾（注释形式，需要时手工执行）
--
-- 背景（2026-11-08）：
--   2026-11-07 上线「相册按文件夹归类」（治疗中/康复后/信息卡）后，本地库补了
--   4 张演示照片用来展示分组效果，随 11-08 的基线快照一起留在本地 cat_rescue 里。
--   线上只跑了增量脚本，这 4 行没带过去，导致线上「花卷」的相册只有 3 张、且
--   没有多文件夹分组的示例。此处按幂等方式补上。
--
-- 配套文件（必须先落地，否则页面上是死链）：
--   /opt/cat-rescue/uploadPath/upload/demo/cat/photo-200.jpg ~ 203.jpg
--   /opt/cat-rescue/uploadPath/upload/demo/cat/thumb/photo-200_thumb.jpg ~ 203_thumb.jpg
--   md5 见本次部署记录。
--
-- 幂等：按 cat_id + file_url 判存，可重复执行；photo_id 交给自增，不写死。
-- =====================================================================

USE cat_rescue;

INSERT INTO cat_photo
    (cat_id, folder, file_type, file_url, thumb_url,
     create_by, create_time, remark)
SELECT 100, '治疗中', '1',
       '/profile/upload/demo/cat/photo-200.jpg',
       '/profile/upload/demo/cat/thumb/photo-200_thumb.jpg',
       'admin', '2026-08-20 14:10:00', '治疗第二周，戴软圈'
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM cat_photo
        WHERE cat_id = 100
          AND file_url = '/profile/upload/demo/cat/photo-200.jpg'
       );

INSERT INTO cat_photo
    (cat_id, folder, file_type, file_url, thumb_url,
     create_by, create_time, remark)
SELECT 100, '康复后', '1',
       '/profile/upload/demo/cat/photo-201.jpg',
       '/profile/upload/demo/cat/thumb/photo-201_thumb.jpg',
       'admin', '2026-09-10 10:20:00', '毛长回来了，胃口好'
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM cat_photo
        WHERE cat_id = 100
          AND file_url = '/profile/upload/demo/cat/photo-201.jpg'
       );

INSERT INTO cat_photo
    (cat_id, folder, file_type, file_url, thumb_url,
     create_by, create_time, remark)
SELECT 100, '信息卡', '1',
       '/profile/upload/demo/cat/photo-202.jpg',
       '/profile/upload/demo/cat/thumb/photo-202_thumb.jpg',
       'admin', '2026-09-20 09:15:00', '纸质信息卡留档'
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM cat_photo
        WHERE cat_id = 100
          AND file_url = '/profile/upload/demo/cat/photo-202.jpg'
       );

INSERT INTO cat_photo
    (cat_id, folder, file_type, file_url, thumb_url,
     create_by, create_time, remark)
SELECT 100, '康复后', '1',
       '/profile/upload/demo/cat/photo-203.jpg',
       '/profile/upload/demo/cat/thumb/photo-203_thumb.jpg',
       'admin', '2026-09-28 16:40:00', '出院前定妆照'
  FROM DUAL
 WHERE NOT EXISTS (
       SELECT 1 FROM cat_photo
        WHERE cat_id = 100
          AND file_url = '/profile/upload/demo/cat/photo-203.jpg'
       );

-- =====================================================================
-- 回滚（需要时手工执行；先确认这 4 行确实是本次插的，别误删客户自己传的）
-- DELETE FROM cat_photo
--  WHERE cat_id = 100
--    AND file_url IN ('/profile/upload/demo/cat/photo-200.jpg',
--                     '/profile/upload/demo/cat/photo-201.jpg',
--                     '/profile/upload/demo/cat/photo-202.jpg',
--                     '/profile/upload/demo/cat/photo-203.jpg');
-- 文件同步删除：
--   rm -f /opt/cat-rescue/uploadPath/upload/demo/cat/photo-20{0,1,2,3}.jpg
--   rm -f /opt/cat-rescue/uploadPath/upload/demo/cat/thumb/photo-20{0,1,2,3}_thumb.jpg
-- =====================================================================
