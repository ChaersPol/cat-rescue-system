package com.ruoyi.cat.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.cat.service.impl.CatReminderGenerateService;

/**
 * 猫咪到期提醒定时任务
 * <p>
 * 在「系统监控 - 定时任务」中配置调用目标 {@code catTask.generateReminder()} 即可，
 * 建议 Cron 表达式：0 0 8 * * ?（每天 08:00 自动扫描一次）
 *
 * @author ruoyi
 */
@Component("catTask")
public class CatReminderTask
{
    private static final Logger log = LoggerFactory.getLogger(CatReminderTask.class);

    @Autowired
    private CatReminderGenerateService catReminderGenerateService;

    /**
     * 扫描疫苗到期、驱虫到期、领养回访、库存不足、外送待接回，生成提醒通知
     */
    public void generateReminder()
    {
        log.info("开始执行猫咪到期提醒定时任务");
        int count = catReminderGenerateService.generateAll();
        log.info("猫咪到期提醒定时任务执行完成，新增 {} 条提醒", count);
    }
}
