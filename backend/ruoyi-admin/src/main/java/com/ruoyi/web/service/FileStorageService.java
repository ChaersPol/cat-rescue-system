package com.ruoyi.web.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;

/**
 * 上传文件的真实删除与孤儿文件清理
 *
 * <p>系统里所有附件、照片、协议最终都落在 {@code ruoyi.profile} 目录下，数据库只保存
 * {@code /profile/upload/2026/10/29/xxx.jpg} 这样的访问地址。RuoYi 原生的上传组件
 * 只从页面列表里移除，<b>从不调用后端删文件</b>，于是：
 * <ul>
 *   <li>页面上删掉的附件仍然留在服务器上（假删除）；</li>
 *   <li>重新上传替换后，旧文件永远没人管；</li>
 *   <li>业务记录被删除后，它引用的图片也成了孤儿。</li>
 * </ul>
 *
 * <p>本服务提供两个能力：
 * <ol>
 *   <li>{@link #deleteByUrl(String)} —— 用户点删除时立即物理删掉该文件（连带缩略图）；</li>
 *   <li>{@link #sweep(boolean, int)} —— 扫描全库文本字段，把磁盘上没有任何记录引用、
 *       且已过宽限期的文件删掉。用来兜底「替换 / 编辑 / 记录删除」产生的残留。</li>
 * </ol>
 *
 * <p>判定「是否被引用」用的是<b>子串包含</b>而不是正则解析：把所有数据拼成一份语料，
 * 只要语料里出现过该文件的相对路径就算在用。这样文件名带空格、中文、括号都不会漏判，
 * 宁可多留一个文件，也不会误删还在用的文件。</p>
 *
 * @author ruoyi
 */
@Service
public class FileStorageService
{
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    /** 演示素材目录，随仓库分发，不参与清理 */
    private static final String DEMO_DIR = "upload/demo";

    /** 参与清理的顶层目录 */
    private static final String[] SCAN_ROOTS = { "upload", "avatar" };

    /** 返回清单最多列出多少条，避免响应过大 */
    private static final int MAX_LIST = 500;

    /**
     * 语料上限（字符）。
     *
     * <p>实测本项目全部资源引用不到 100KB，8MB 已是极宽裕的上限。原值 80MB 在
     * 512MB 堆上很危险：StringBuilder 内部是 UTF-16，80M 字符占 160MB，
     * 再 toString 复制一份就是 320MB 峰值。</p>
     *
     * <p><b>注意</b>：语料一旦被截断就不再完整，此时若继续清理，会把
     * 「引用没被收进来」的文件误判成孤儿删掉。所以超限时直接放弃本次清理。</p>
     */
    private static final int MAX_CORPUS = 8 * 1024 * 1024;

    /**
     * 单表单列最多扫描的行数。
     *
     * <p>同样出于「安全第一」：超限说明扫描不完整，必须放弃本次清理而不是删文件。</p>
     */
    private static final int MAX_ROWS_PER_COLUMN = 50000;

    /** 记录操作日志的表，内容全是历史请求参数，不能作为「文件仍被引用」的依据 */
    private static final String[] SKIP_TABLES = { "sys_oper_log", "sys_logininfor" };

    @Autowired
    private DataSource dataSource;

    // ------------------------------------------------------------------ 单个删除

    /**
     * 按访问地址删除单个文件（连带同目录 thumb/ 下的缩略图）
     *
     * @param url /profile/upload/2026/10/29/xxx.jpg、完整 URL 或相对路径
     * @return true 表示文件确实被删掉了；文件本来就不存在时返回 false
     */
    public boolean deleteByUrl(String url)
    {
        File file = resolveLocalFile(url);
        if (file == null)
        {
            return false;
        }
        boolean deleted = deleteQuietly(file);
        // 缩略图是上传时额外生成的，删原图时必须一起删，否则 thumb/ 目录会越堆越多
        File thumb = thumbnailOf(file);
        if (thumb != null)
        {
            deleteQuietly(thumb);
        }
        if (deleted)
        {
            log.info("已删除上传文件: {}", file.getAbsolutePath());
        }
        return deleted;
    }

    // ------------------------------------------------------------------ 孤儿扫描

    /**
     * 清理孤儿文件
     *
     * @param dryRun     true 只统计不删除（试跑）
     * @param graceHours 宽限期（小时），最近上传的文件不动，防止「刚传完还没点保存」被误删
     * @return 统计结果（扫描数、被引用数、孤儿数、删除数、文件清单）
     */
    public Map<String, Object> sweep(boolean dryRun, int graceHours)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        long begin = System.currentTimeMillis();

        String profile = RuoYiConfig.getProfile();
        if (StringUtils.isEmpty(profile))
        {
            result.put("error", "上传目录未初始化，无法执行清理");
            return result;
        }
        File root = new File(profile).getAbsoluteFile();

        String corpus;
        try
        {
            corpus = collectCorpus();
        }
        catch (Exception e)
        {
            log.error("扫描数据库文件引用失败", e);
            result.put("error", "扫描数据库失败：" + e.getMessage());
            return result;
        }

        List<File> candidates = listCandidateFiles(root);
        long graceMs = Math.max(0, graceHours) * 3600_000L;
        long now = System.currentTimeMillis();

        List<String> orphans = new ArrayList<>();
        List<String> deleted = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        List<String> inGrace = new ArrayList<>();
        long orphanBytes = 0L;
        int referencedCount = 0;

        for (File file : candidates)
        {
            String relative = relativize(root, file);
            if (relative == null || isSkip(relative))
            {
                continue;
            }
            if (isReferenced(corpus, relative))
            {
                referencedCount++;
                continue;
            }
            if (now - file.lastModified() < graceMs)
            {
                if (inGrace.size() < MAX_LIST)
                {
                    inGrace.add(relative);
                }
                continue;
            }
            orphanBytes += file.length();
            if (orphans.size() < MAX_LIST)
            {
                orphans.add(relative);
            }
            if (dryRun)
            {
                continue;
            }
            if (file.delete())
            {
                deleted.add(relative);
            }
            else
            {
                failed.add(relative);
            }
        }

        if (!dryRun)
        {
            cleanEmptyDirs(root);
        }

        result.put("dryRun", dryRun);
        result.put("graceHours", graceHours);
        result.put("root", root.getAbsolutePath());
        result.put("scanned", candidates.size());
        result.put("referenced", referencedCount);
        result.put("orphan", orphans.size());
        result.put("orphanBytes", orphanBytes);
        result.put("deleted", deleted.size());
        result.put("failed", failed.size());
        result.put("inGrace", inGrace.size());
        result.put("orphans", orphans);
        result.put("deletedFiles", deleted);
        result.put("failedFiles", failed);
        result.put("inGraceFiles", inGrace);
        result.put("elapsedMs", System.currentTimeMillis() - begin);
        log.info("上传文件清理完成：扫描 {}，在用 {}，孤儿 {}（{} KB），删除 {}，宽限期跳过 {}，耗时 {}ms",
                candidates.size(), referencedCount, orphans.size(), orphanBytes / 1024,
                dryRun ? 0 : deleted.size(), inGrace.size(), System.currentTimeMillis() - begin);
        return result;
    }

    /**
     * 把所有业务表里的文本内容拼成一份语料，用于判断文件是否仍被引用
     */
    private String collectCorpus() throws Exception
    {
        StringBuilder corpus = new StringBuilder(1024 * 1024);
        try (Connection conn = dataSource.getConnection())
        {
            String catalog = conn.getCatalog();
            List<String[]> columns = new ArrayList<>();
            String sql = "SELECT table_name, column_name FROM information_schema.columns "
                    + "WHERE table_schema = ? "
                    + "AND data_type IN ('char','varchar','tinytext','text','mediumtext','longtext') "
                    + "ORDER BY table_name, ordinal_position";
            try (PreparedStatement ps = conn.prepareStatement(sql))
            {
                ps.setString(1, catalog);
                try (ResultSet rs = ps.executeQuery())
                {
                    while (rs.next())
                    {
                        String table = rs.getString(1);
                        if (containsStr(SKIP_TABLES, table))
                        {
                            continue;
                        }
                        columns.add(new String[] { table, rs.getString(2) });
                    }
                }
            }
            for (String[] column : columns)
            {
                if (corpus.length() > MAX_CORPUS)
                {
                    // 语料不完整 -> 引用判定不可信 -> 必须放弃清理，否则会误删在用文件
                    throw new IllegalStateException(
                            "语料已达上限 " + MAX_CORPUS + " 字符，扫描不完整，本次清理已放弃（避免误删）");
                }
                String query = "SELECT `" + column[1] + "` FROM `" + column[0] + "`";
                try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(query))
                {
                    int rows = 0;
                    while (rs.next())
                    {
                        if (++rows > MAX_ROWS_PER_COLUMN)
                        {
                            throw new IllegalStateException(
                                    "表 " + column[0] + " 行数超过 " + MAX_ROWS_PER_COLUMN
                                            + "，扫描不完整，本次清理已放弃（避免误删）");
                        }
                        String value = rs.getString(1);
                        if (value == null || value.isEmpty())
                        {
                            continue;
                        }
                        // 绝大多数内容跟文件无关，带 upload/ 或 avatar/ 的才值得进语料
                        if (value.indexOf("upload/") < 0 && value.indexOf("avatar/") < 0)
                        {
                            continue;
                        }
                        corpus.append(value).append('\n');
                    }
                }
                catch (IllegalStateException e)
                {
                    // 扫描不完整必须往上层抛，绝不能在这里被当成「字段读不到」跳过，
                    // 否则后续会拿残缺语料去删文件
                    throw e;
                }
                catch (Exception e)
                {
                    // 个别字段读不到（权限、字符集）不影响整体清理
                    log.debug("跳过字段 {}.{}：{}", column[0], column[1], e.getMessage());
                }
            }
        }
        return corpus.toString();
    }

    /**
     * 判断文件是否仍被数据库引用
     *
     * <p>正常存的是 {@code /profile/upload/2026/10/29/xxx.jpg}，相对路径
     * {@code upload/2026/10/29/xxx.jpg} 一定出现在语料里；少数情况存的是 URL 编码后的中文名，
     * 所以再比一次编码后的形式。</p>
     */
    private boolean isReferenced(String corpus, String relative)
    {
        if (corpus.contains(relative))
        {
            return true;
        }
        String encoded = encodePath(relative);
        return encoded != null && corpus.contains(encoded);
    }

    /** 把相对路径转成 URL 编码形式（斜杠保留），用于匹配编码后存库的场景 */
    private String encodePath(String relative)
    {
        boolean hasNonAscii = false;
        for (int i = 0; i < relative.length(); i++)
        {
            if (relative.charAt(i) > 127)
            {
                hasNonAscii = true;
                break;
            }
        }
        if (!hasNonAscii)
        {
            // 纯 ASCII 路径编码后与原文一致，无需再比
            return null;
        }
        try
        {
            return URLEncoder.encode(relative, StandardCharsets.UTF_8.name())
                    .replace("+", "%20")
                    .replace("%2F", "/")
                    .replace("%2f", "/");
        }
        catch (UnsupportedEncodingException e)
        {
            return null;
        }
    }

    // ------------------------------------------------------------------ 磁盘遍历

    /** 列出上传目录下所有待检查的文件（upload/** 与 avatar/**） */
    private List<File> listCandidateFiles(File root)
    {
        List<File> files = new ArrayList<>();
        for (String scanRoot : SCAN_ROOTS)
        {
            File dir = new File(root, scanRoot);
            if (!dir.isDirectory())
            {
                continue;
            }
            try (java.util.stream.Stream<Path> stream = Files.walk(dir.toPath()))
            {
                stream.filter(Files::isRegularFile).forEach(p -> files.add(p.toFile()));
            }
            catch (IOException e)
            {
                log.warn("遍历目录失败: {} ({})", dir.getAbsolutePath(), e.getMessage());
            }
        }
        return files;
    }

    /** 计算文件相对上传根目录的路径，统一用 / 分隔 */
    private String relativize(File root, File file)
    {
        Path rootPath = root.toPath().toAbsolutePath().normalize();
        Path filePath = file.toPath().toAbsolutePath().normalize();
        if (!filePath.startsWith(rootPath))
        {
            return null;
        }
        return rootPath.relativize(filePath).toString().replace(File.separatorChar, '/');
    }

    /** 演示素材、隐藏文件不参与清理 */
    private boolean isSkip(String relative)
    {
        if (relative.startsWith(DEMO_DIR + "/"))
        {
            return true;
        }
        String name = relative.substring(relative.lastIndexOf('/') + 1);
        return name.startsWith(".") || name.startsWith("._");
    }

    /** 删掉清理后变空的日期目录，保持目录树干净 */
    private void cleanEmptyDirs(File root)
    {
        for (String scanRoot : SCAN_ROOTS)
        {
            File dir = new File(root, scanRoot);
            if (!dir.isDirectory())
            {
                continue;
            }
            try (java.util.stream.Stream<Path> stream = Files.walk(dir.toPath()))
            {
                stream.sorted(Comparator.reverseOrder())
                        .filter(Files::isDirectory)
                        .filter(p -> !p.equals(dir.toPath()))
                        .forEach(p -> {
                            File f = p.toFile();
                            String[] children = f.list();
                            if (children != null && children.length == 0)
                            {
                                //noinspection ResultOfMethodCallIgnored
                                f.delete();
                            }
                        });
            }
            catch (IOException e)
            {
                log.debug("清理空目录失败: {}", e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------------ 路径解析

    /**
     * 把访问地址解析成磁盘文件，并确保解析结果落在上传根目录内（防 ../ 穿越）
     */
    private File resolveLocalFile(String url)
    {
        if (StringUtils.isEmpty(url))
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
        int idx = path.indexOf("/profile/");
        if (idx >= 0)
        {
            relative = path.substring(idx + "/profile/".length());
        }
        else if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("//"))
        {
            // 外链，不归本机管
            return null;
        }
        else
        {
            relative = path.replaceFirst("^/+", "");
            if (relative.startsWith("profile/"))
            {
                relative = relative.substring("profile/".length());
            }
        }
        if (relative.isEmpty() || relative.contains(".."))
        {
            return null;
        }

        String profile = RuoYiConfig.getProfile();
        if (StringUtils.isEmpty(profile))
        {
            return null;
        }
        Path root = new File(profile).getAbsoluteFile().toPath().normalize();
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root))
        {
            return null;
        }
        File file = target.toFile();
        return file.isFile() ? file : null;
    }

    /** 原文件对应的缩略图（同目录 thumb/<name>_thumb.jpg） */
    private File thumbnailOf(File src)
    {
        File parent = src.getParentFile();
        if (parent == null)
        {
            return null;
        }
        String name = src.getName();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        return new File(new File(parent, "thumb"), base + "_thumb.jpg");
    }

    private boolean deleteQuietly(File file)
    {
        try
        {
            return file != null && file.isFile() && file.delete();
        }
        catch (Exception e)
        {
            log.warn("删除文件失败: {} ({})", file, e.getMessage());
            return false;
        }
    }

    private boolean containsStr(String[] array, String value)
    {
        for (String item : array)
        {
            if (item.equals(value))
            {
                return true;
            }
        }
        return false;
    }
}
