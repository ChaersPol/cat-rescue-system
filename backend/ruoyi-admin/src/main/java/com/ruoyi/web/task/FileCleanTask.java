package com.ruoyi.web.task;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.web.service.FileStorageService;

/**
 * 上传目录孤儿文件定时清理
 *
 * <p>每天凌晨跑一次，把「数据库里已经没有任何记录引用」的上传文件删掉。
 * 覆盖三类残留：附件被替换、编辑时改了图片、业务记录被删除。</p>
 *
 * <p>清理前会重新扫描全库引用，且默认保留最近 24 小时内上传的文件，
 * 所以不会误伤「刚传完还没点保存」的文件。可通过 {@code cat.file-clean.*} 配置调整。</p>
 *
 * @author ruoyi
 */
@Component
public class FileCleanTask
{
    private static final Logger log = LoggerFactory.getLogger(FileCleanTask.class);

    @Autowired
    private FileStorageService fileStorageService;

    /** 总开关 */
    @Value("${cat.file-clean.enabled:true}")
    private boolean enabled;

    /** 宽限期（小时），最近上传的文件不清理 */
    @Value("${cat.file-clean.grace-hours:24}")
    private int graceHours;

    @Scheduled(cron = "${cat.file-clean.cron:0 30 3 * * ?}")
    public void cleanOrphanFiles()
    {
        if (!enabled)
        {
            return;
        }
        try
        {
            Map<String, Object> result = fileStorageService.sweep(false, graceHours);
            if (result.containsKey("error"))
            {
                log.warn("上传文件清理失败：{}", result.get("error"));
            }
        }
        catch (Exception e)
        {
            // 定时任务异常不能影响主线程
            log.error("上传文件清理异常", e);
        }
    }
}
