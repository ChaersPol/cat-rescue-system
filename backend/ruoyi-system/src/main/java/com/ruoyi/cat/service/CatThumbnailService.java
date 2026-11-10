package com.ruoyi.cat.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.config.RuoYiConfig;

/**
 * 猫咪照片/视频缩略图服务
 *
 * <p><b>图片</b>：使用 JDK 自带的 ImageIO 等比缩放到 {@link #MAX_WIDTH} 宽，输出 JPEG，列表加载体积显著减小。</p>
 *
 * <p><b>视频</b>：三级兜底策略 ——</p>
 * <ol>
 *   <li>优先调用系统 ffmpeg 抽取第 1 秒画面（兼容性最好）；</li>
 *   <li>ffmpeg 不可用时，使用纯 Java 的 jcodec 解码 mp4/mov（H.264）取帧，开箱即用；</li>
 *   <li>两者都失败则返回空，由前端用 {@code <video preload="metadata">} 首帧兜底展示。</li>
 * </ol>
 *
 * <p>缩略图与原文件同目录存放于 {@code thumb/} 子目录，访问路径为 {@code /profile/upload/.../thumb/xxx_thumb.jpg}。</p>
 *
 * @author ruoyi
 */
@Service
public class CatThumbnailService
{
    private static final Logger log = LoggerFactory.getLogger(CatThumbnailService.class);

    /** 缩略图最大宽度（像素） */
    private static final int MAX_WIDTH = 400;

    /** JPEG 压缩质量 */
    private static final float JPEG_QUALITY = 0.85f;

    /** 小于该体积且宽度不超标的图片无需生成缩略图 */
    private static final long SMALL_FILE_LIMIT = 120 * 1024L;

    /** 缩略图子目录名 */
    private static final String THUMB_DIR = "thumb";

    /** 缩略图文件名后缀 */
    private static final String THUMB_SUFFIX = "_thumb.jpg";

    /** 可被 ImageIO 直接解码的图片扩展名 */
    private static final Set<String> IMAGE_EXTS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp"));

    /** 视频扩展名 */
    private static final Set<String> VIDEO_EXTS = new HashSet<>(
            Arrays.asList("mp4", "mov", "m4v", "avi", "mkv", "webm", "flv"));

    /** 是否启用缩略图 */
    @Value("${thumbnail.enabled:true}")
    private boolean enabled;

    /** 是否启用视频抽帧 */
    @Value("${thumbnail.video.enabled:true}")
    private boolean videoEnabled;

    /** ffmpeg 可执行文件路径 */
    @Value("${thumbnail.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    /** ffmpeg 抽帧的超时时间（秒） */
    @Value("${thumbnail.ffmpeg.timeout:20}")
    private int ffmpegTimeout;

    /** ffmpeg 可用性缓存：null 表示尚未检测 */
    private volatile Boolean ffmpegAvailable;

    /**
     * 根据文件地址生成缩略图
     *
     * @param fileUrl 原文件地址（完整 URL 或 /profile/ 开头的相对路径）
     * @param fileType 文件类型（1 图片 2 视频），为空时按扩展名推断
     * @return 缩略图访问地址；无需生成或生成失败时返回 null
     */
    public String generate(String fileUrl, String fileType)
    {
        if (!enabled || fileUrl == null || fileUrl.trim().isEmpty())
        {
            return null;
        }
        String url = fileUrl.trim();
        File src = resolveLocalFile(url);
        if (src == null || !src.exists() || !src.isFile())
        {
            log.debug("缩略图跳过，源文件不在本机存储: {}", url);
            return null;
        }
        String type = isBlank(fileType) ? detectFileType(url) : fileType;
        String ext = extensionOf(src.getName());
        try
        {
            if ("2".equals(type) || VIDEO_EXTS.contains(ext))
            {
                return makeVideoThumb(src, url);
            }
            if (IMAGE_EXTS.contains(ext))
            {
                return makeImageThumb(src, url);
            }
            log.debug("缩略图跳过，不支持的格式: {}", ext);
            return null;
        }
        catch (Exception e)
        {
            log.warn("生成缩略图失败: {} ({})", url, e.getMessage());
            return null;
        }
    }

    /**
     * 按扩展名推断文件类型（1 图片 2 视频），未知按图片处理
     */
    public String detectFileType(String fileUrl)
    {
        String ext = extensionOf(fileUrl);
        return VIDEO_EXTS.contains(ext) ? "2" : "1";
    }

    /**
     * ffmpeg 是否可用（结果缓存，避免每次上传都探测）
     */
    public boolean isFfmpegAvailable()
    {
        if (ffmpegAvailable == null)
        {
            synchronized (this)
            {
                if (ffmpegAvailable == null)
                {
                    ffmpegAvailable = probeFfmpeg();
                }
            }
        }
        return ffmpegAvailable;
    }

    // ------------------------------------------------------------------ 图片

    private String makeImageThumb(File src, String originalUrl) throws IOException
    {
        BufferedImage source = ImageIO.read(src);
        if (source == null)
        {
            // ImageIO 无法解码（如 webp/heic），保持原图展示
            log.debug("ImageIO 无法解码，保留原图: {}", src.getName());
            return null;
        }
        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();
        if (srcWidth <= 0 || srcHeight <= 0)
        {
            return null;
        }
        // 小图且体积不大，直接用原图更省事
        if (srcWidth <= MAX_WIDTH && src.length() <= SMALL_FILE_LIMIT)
        {
            return null;
        }
        // 手机拍的照片像素是横着的，靠 EXIF Orientation 让看图软件转正；
        // ImageIO 只读像素、不认这个标记，所以缩略图必须自己转，否则打印卡/相册里全是躺倒的
        BufferedImage thumb = orient(scaleToWidth(source, MAX_WIDTH), readExifOrientation(src));
        writeJpeg(thumb, thumbFileFor(src));
        return thumbUrlFor(originalUrl);
    }

    /**
     * 按 EXIF Orientation 把像素真正转正（1 不用转，2~8 见 EXIF 规范）。
     *
     * <p>只对已经缩小的图做变换，内存占用可控；5~8 会让宽高互换，
     * 所以输出图的长边仍然是 {@link #MAX_WIDTH}。</p>
     */
    private BufferedImage orient(BufferedImage src, int orientation)
    {
        if (orientation < 2 || orientation > 8)
        {
            return src;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        boolean swap = orientation >= 5;
        int dw = swap ? h : w;
        int dh = swap ? w : h;
        AffineTransform t;
        switch (orientation)
        {
            case 2: // 水平镜像
                t = new AffineTransform(-1, 0, 0, 1, w, 0);
                break;
            case 3: // 旋转 180°
                t = new AffineTransform(-1, 0, 0, -1, w, h);
                break;
            case 4: // 垂直镜像
                t = new AffineTransform(1, 0, 0, -1, 0, h);
                break;
            case 5: // 主对角线翻转
                t = new AffineTransform(0, 1, 1, 0, 0, 0);
                break;
            case 6: // 顺时针 90°（手机竖拍最常见）
                t = new AffineTransform(0, 1, -1, 0, h, 0);
                break;
            case 7: // 副对角线翻转
                t = new AffineTransform(0, -1, -1, 0, h, w);
                break;
            default: // 8 逆时针 90°
                t = new AffineTransform(0, -1, 1, 0, 0, w);
                break;
        }
        BufferedImage dst = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        try
        {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, dw, dh);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, t, null);
        }
        finally
        {
            g.dispose();
        }
        return dst;
    }

    /**
     * 读取 JPEG 的 EXIF Orientation；非 JPEG、没有 EXIF 或解析失败都返回 1（正常方向）。
     */
    private int readExifOrientation(File file)
    {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r"))
        {
            long length = raf.length();
            if (length < 12 || raf.readUnsignedShort() != 0xFFD8)
            {
                return 1;
            }
            while (raf.getFilePointer() + 4 <= length)
            {
                if (raf.readUnsignedByte() != 0xFF)
                {
                    return 1;
                }
                int marker = raf.readUnsignedByte();
                while (marker == 0xFF && raf.getFilePointer() < length)
                {
                    marker = raf.readUnsignedByte();
                }
                // 无长度字段的标记：SOI / RSTn / TEM
                if (marker == 0xD8 || (marker >= 0xD0 && marker <= 0xD7) || marker == 0x01)
                {
                    continue;
                }
                // 走到图像数据或结束，后面不会再有 EXIF
                if (marker == 0xDA || marker == 0xD9)
                {
                    return 1;
                }
                if (raf.getFilePointer() + 2 > length)
                {
                    return 1;
                }
                int segmentLength = raf.readUnsignedShort();
                if (segmentLength < 2)
                {
                    return 1;
                }
                int payload = segmentLength - 2;
                if (raf.getFilePointer() + payload > length)
                {
                    return 1;
                }
                if (marker == 0xE1 && payload > 6)
                {
                    byte[] data = new byte[payload];
                    raf.readFully(data);
                    int orientation = parseExifOrientation(data);
                    if (orientation > 0)
                    {
                        return orientation;
                    }
                }
                else
                {
                    raf.seek(raf.getFilePointer() + payload);
                }
            }
        }
        catch (Exception e)
        {
            log.debug("读取照片方向失败: {} ({})", file.getName(), e.getMessage());
        }
        return 1;
    }

    /**
     * 从 APP1 段（Exif\0\0 + TIFF 头）里取 Orientation 标签（0x0112）
     */
    private int parseExifOrientation(byte[] data)
    {
        if (data.length < 14
                || data[0] != 'E' || data[1] != 'x' || data[2] != 'i' || data[3] != 'f'
                || data[4] != 0 || data[5] != 0)
        {
            return 0;
        }
        int tiff = 6;
        boolean little = data[tiff] == 'I' && data[tiff + 1] == 'I';
        boolean big = data[tiff] == 'M' && data[tiff + 1] == 'M';
        if (!little && !big)
        {
            return 0;
        }
        int ifd = tiff + readExifInt(data, tiff + 4, little);
        if (ifd < tiff || ifd + 2 > data.length)
        {
            return 0;
        }
        int count = readExifShort(data, ifd, little);
        for (int i = 0; i < count; i++)
        {
            int entry = ifd + 2 + i * 12;
            if (entry + 12 > data.length)
            {
                return 0;
            }
            if (readExifShort(data, entry, little) == 0x0112)
            {
                int value = readExifShort(data, entry + 8, little);
                return value >= 1 && value <= 8 ? value : 0;
            }
        }
        return 0;
    }

    private int readExifShort(byte[] data, int offset, boolean little)
    {
        int b0 = data[offset] & 0xFF;
        int b1 = data[offset + 1] & 0xFF;
        return little ? (b1 << 8 | b0) : (b0 << 8 | b1);
    }

    private int readExifInt(byte[] data, int offset, boolean little)
    {
        int b0 = data[offset] & 0xFF;
        int b1 = data[offset + 1] & 0xFF;
        int b2 = data[offset + 2] & 0xFF;
        int b3 = data[offset + 3] & 0xFF;
        return little ? (b3 << 24 | b2 << 16 | b1 << 8 | b0) : (b0 << 24 | b1 << 16 | b2 << 8 | b3);
    }

    /**
     * 等比缩放到目标宽度，透明区域统一铺白（避免 PNG 转 JPEG 出现黑块）
     */
    private BufferedImage scaleToWidth(BufferedImage source, int maxWidth)
    {
        int srcWidth = source.getWidth();
        int srcHeight = source.getHeight();
        int targetWidth = Math.min(maxWidth, srcWidth);
        int targetHeight = Math.max(1, (int) Math.round(srcHeight * (double) targetWidth / srcWidth));

        BufferedImage target = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = target.createGraphics();
        try
        {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, targetWidth, targetHeight);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        }
        finally
        {
            g.dispose();
        }
        return target;
    }

    private void writeJpeg(BufferedImage image, File out) throws IOException
    {
        File parent = out.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs())
        {
            throw new IOException("无法创建缩略图目录: " + parent.getAbsolutePath());
        }
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed())
        {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(JPEG_QUALITY);
        }
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out))
        {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        }
        finally
        {
            writer.dispose();
        }
    }

    // ------------------------------------------------------------------ 视频

    private String makeVideoThumb(File src, String originalUrl) throws Exception
    {
        if (!videoEnabled)
        {
            return null;
        }
        File out = thumbFileFor(src);
        File parent = out.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs())
        {
            throw new IOException("无法创建缩略图目录: " + parent.getAbsolutePath());
        }

        // 1) ffmpeg 优先
        if (isFfmpegAvailable())
        {
            // 先尝试第 1 秒，短视频（不足 1 秒）再退回第 0 帧
            for (String seek : new String[] { "1", "0" })
            {
                if (runFfmpeg(src, out, seek) && out.exists() && out.length() > 0)
                {
                    return thumbUrlFor(originalUrl);
                }
            }
        }

        // 2) 纯 Java（jcodec）兜底
        if (grabFrameByJcodec(src, out) && out.exists() && out.length() > 0)
        {
            return thumbUrlFor(originalUrl);
        }

        // 抽帧失败时清理残留空文件
        if (out.exists() && out.length() == 0)
        {
            //noinspection ResultOfMethodCallIgnored
            out.delete();
        }
        log.debug("视频封面生成失败，交由前端首帧展示: {}", src.getName());
        return null;
    }

    /**
     * 使用 jcodec（纯 Java）解码视频取帧，无需外部依赖
     */
    private boolean grabFrameByJcodec(File src, File out)
    {
        try
        {
            Picture picture = null;
            // 先定位到第 1 秒，短视频会失败再退回首帧
            try (org.jcodec.common.io.SeekableByteChannel ch = NIOUtils.readableChannel(src))
            {
                FrameGrab grab = FrameGrab.createFrameGrab(ch);
                grab.seekToSecondPrecise(1.0);
                picture = grab.getNativeFrame();
            }
            if (picture == null)
            {
                try (org.jcodec.common.io.SeekableByteChannel ch = NIOUtils.readableChannel(src))
                {
                    picture = FrameGrab.createFrameGrab(ch).getNativeFrame();
                }
            }
            if (picture == null)
            {
                return false;
            }
            BufferedImage frame = AWTUtil.toBufferedImage(picture);
            if (frame == null)
            {
                return false;
            }
            writeJpeg(scaleToWidth(frame, MAX_WIDTH), out);
            return true;
        }
        catch (Throwable e)
        {
            // jcodec 对部分编码（如 HEVC）不支持，属预期内的降级路径
            log.debug("jcodec 抽帧失败: {} ({})", src.getName(), e.getMessage());
            return false;
        }
    }

    private boolean runFfmpeg(File src, File out, String seekSecond)
    {
        //noinspection ResultOfMethodCallIgnored
        out.delete();
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath, "-y",
                "-ss", seekSecond,
                "-i", src.getAbsolutePath(),
                "-frames:v", "1",
                "-vf", "scale=" + MAX_WIDTH + ":-2",
                "-q:v", "3",
                "-f", "image2",
                out.getAbsolutePath());
        // 直接丢弃输出：既不阻塞进程，也不会因等待 EOF 导致超时失效
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        Process process = null;
        try
        {
            process = pb.start();
            if (!process.waitFor(ffmpegTimeout, TimeUnit.SECONDS))
            {
                process.destroyForcibly();
                log.warn("ffmpeg 抽帧超时: {}", src.getName());
                return false;
            }
            return process.exitValue() == 0 && out.exists() && out.length() > 0;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return false;
        }
        catch (Exception e)
        {
            log.debug("ffmpeg 抽帧失败: {} ({})", src.getName(), e.getMessage());
            return false;
        }
        finally
        {
            if (process != null && process.isAlive())
            {
                process.destroyForcibly();
            }
        }
    }

    private boolean probeFfmpeg()
    {
        try
        {
            ProcessBuilder pb = new ProcessBuilder(ffmpegPath, "-version");
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process process = pb.start();
            boolean ok = process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
            log.info("ffmpeg 检测结果: {}", ok ? "可用，视频将优先用 ffmpeg 抽帧" : "不可用，视频改用 jcodec 取帧");
            return ok;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            return false;
        }
        catch (Exception e)
        {
            log.info("未检测到 ffmpeg（{}），视频改用 jcodec 取帧", ffmpegPath);
            return false;
        }
    }

    // ------------------------------------------------------------------ 路径

    /**
     * 把访问地址解析为磁盘文件。同时支持三种写法：
     * <ul>
     *   <li>{@code http://host/profile/upload/2026/10/25/a.jpg}（上传组件返回的完整地址）</li>
     *   <li>{@code /profile/upload/2026/10/25/a.jpg}（相对地址，RuoYi 上传默认返回值）</li>
     *   <li>{@code /upload/2026/10/25/a.jpg}（省略 /profile 前缀）</li>
     * </ul>
     */
    private File resolveLocalFile(String url)
    {
        if (url == null)
        {
            return null;
        }
        String path = url.trim();
        int query = path.indexOf('?');
        if (query > 0)
        {
            path = path.substring(0, query);
        }

        String relative;
        String marker = "/profile/";
        int idx = path.indexOf(marker);
        if (idx >= 0)
        {
            relative = path.substring(idx + marker.length());
        }
        else if (!path.startsWith("http://") && !path.startsWith("https://"))
        {
            relative = path.replaceFirst("^/+", "");
            if (relative.startsWith("profile/"))
            {
                relative = relative.substring("profile/".length());
            }
        }
        else
        {
            // 外部链接，不在本机存储范围
            return null;
        }

        if (relative.isEmpty() || relative.contains(".."))
        {
            return null;
        }
        String profile = RuoYiConfig.getProfile();
        if (isBlank(profile))
        {
            return null;
        }
        return new File(profile, relative);
    }

    /**
     * 根据原文件路径计算缩略图文件路径：同目录下 thumb/ 子目录，文件名追加 _thumb.jpg
     */
    private File thumbFileFor(File src)
    {
        File parent = src.getParentFile();
        File thumbDir = parent == null ? new File(RuoYiConfig.getProfile(), THUMB_DIR)
                : new File(parent, THUMB_DIR);
        String baseName = src.getName();
        int dot = baseName.lastIndexOf('.');
        if (dot > 0)
        {
            baseName = baseName.substring(0, dot);
        }
        return new File(thumbDir, baseName + THUMB_SUFFIX);
    }

    /**
     * 由原文件地址推导缩略图访问地址：同目录下 thumb/ 子目录，沿用原域名
     */
    private String thumbUrlFor(String originalUrl)
    {
        String marker = "/profile/";
        int idx = originalUrl.indexOf(marker);
        if (idx < 0)
        {
            return null;
        }
        String head = originalUrl.substring(0, idx + marker.length());
        String relative = originalUrl.substring(idx + marker.length());
        int query = relative.indexOf('?');
        if (query > 0)
        {
            relative = relative.substring(0, query);
        }
        int slash = relative.lastIndexOf('/');
        String dir = slash >= 0 ? relative.substring(0, slash + 1) : "";
        String name = slash >= 0 ? relative.substring(slash + 1) : relative;
        int dot = name.lastIndexOf('.');
        if (dot > 0)
        {
            name = name.substring(0, dot);
        }
        return head + dir + THUMB_DIR + "/" + name + THUMB_SUFFIX;
    }

    private String extensionOf(String name)
    {
        if (name == null)
        {
            return "";
        }
        String path = name;
        int query = path.indexOf('?');
        if (query > 0)
        {
            path = path.substring(0, query);
        }
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1)
        {
            return "";
        }
        return path.substring(dot + 1).toLowerCase();
    }

    private boolean isBlank(String s)
    {
        return s == null || s.trim().isEmpty();
    }
}
