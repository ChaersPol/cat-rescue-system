-- =============================================================================
-- 2026-11-12 照片增加「预览图」地址（cat_photo.preview_url）
--
-- 背景：相册弹窗里翻页用的是原图，线上实测一张 4MB 的手机照片，
--       公网出口 3~4Mbps，下载要 8.7 秒；每切一张就等一次，用户体感「图片加载很慢」。
--
-- 做法：上传/重建时额外生成一档长边 1400、JPEG q=0.82 的预览图（约 150~250KB），
--       相册弹窗默认加载它；只有用户主动「放大看细节」时才回落到原图。
--       缩略图（400px）和预览图共用一次原图解码，不会多占一倍内存。
--
-- 本脚本只加一个字段，不改任何业务数据，可重复执行。
-- 存量照片的预览图需要发版后调一次「重建缩略图」补生成（只补文件，不动原图）。
-- =============================================================================

SET NAMES utf8mb4;

SET @has_col := (SELECT COUNT(*) FROM information_schema.COLUMNS
                 WHERE TABLE_SCHEMA = DATABASE()
                   AND TABLE_NAME = 'cat_photo'
                   AND COLUMN_NAME = 'preview_url');
SET @ddl := IF(@has_col = 0,
  'ALTER TABLE `cat_photo` ADD COLUMN `preview_url` varchar(500) DEFAULT NULL COMMENT ''预览图地址（长边 1400，相册弹窗翻页用）'' AFTER `thumb_url`',
  'DO 0');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT @has_col AS preview_url_column_existed_before;
