package com.ruoyi.common.config;

import java.io.File;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 * 
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "ruoyi")
public class RuoYiConfig
{
    private static final Logger log = LoggerFactory.getLogger(RuoYiConfig.class);

    /** 项目名称 */
    private String name;

    /** 版本 */
    private String version;

    /** 版权年份 */
    private String copyrightYear;

    /** 上传路径 */
    private static String profile;

    /** 获取地址开关 */
    private static boolean addressEnabled;

    /** 验证码类型 */
    private static String captchaType;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getCopyrightYear()
    {
        return copyrightYear;
    }

    public void setCopyrightYear(String copyrightYear)
    {
        this.copyrightYear = copyrightYear;
    }

    /**
     * 启动时把上传目录规范成绝对路径
     * <p>
     * application.yml 里写的是相对路径 ./uploadPath，但相对路径是相对「启动时的工作目录」的：
     * 在 backend 下执行 java -jar 与在 ruoyi-admin 下执行 mvn spring-boot:run，会解析到两个不同目录，
     * 结果就是文件明明上传成功了、/profile/** 却 404（图片、视频、二维码全部空白）。
     * 这里按「工作目录 → 工作目录/ruoyi-admin → 上级目录」依次探测已经存在的 uploadPath，
     * 保证不同启动方式都能指向同一份文件；都不存在时才在工作目录下新建。
     */
    @PostConstruct
    public void initProfilePath()
    {
        String configured = (profile == null || profile.trim().isEmpty()) ? "./uploadPath" : profile.trim();
        File dir = new File(configured);
        if (dir.isAbsolute())
        {
            setProfile(dir.getPath());
            log.info("上传目录：{}", dir.getPath());
            return;
        }

        File workDir = new File(System.getProperty("user.dir"));
        File[] candidates = new File[] {
                new File(workDir, configured),
                new File(new File(workDir, "ruoyi-admin"), configured),
                new File(workDir.getParentFile() == null ? workDir : workDir.getParentFile(), configured)
        };
        File chosen = candidates[0];
        for (File candidate : candidates)
        {
            if (candidate.isDirectory())
            {
                chosen = candidate;
                break;
            }
        }
        // 去掉路径里的 ./ 之类冗余片段，日志回显更直观
        chosen = chosen.toPath().normalize().toFile();
        setProfile(chosen.getAbsolutePath());
        if (!chosen.equals(candidates[0]))
        {
            log.warn("配置的上传目录 {} 在当前工作目录下不存在，已自动改用已存在的：{}", configured, chosen.getAbsolutePath());
        }
        else
        {
            log.info("上传目录：{}", chosen.getAbsolutePath());
        }
    }

    public static String getProfile()
    {
        return profile;
    }

    public void setProfile(String profile)
    {
        RuoYiConfig.profile = profile;
    }

    public static boolean isAddressEnabled()
    {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled)
    {
        RuoYiConfig.addressEnabled = addressEnabled;
    }

    public static String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        RuoYiConfig.captchaType = captchaType;
    }

    /**
     * 获取导入上传路径
     */
    public static String getImportPath()
    {
        return getProfile() + "/import";
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath()
    {
        return getProfile() + "/avatar";
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath()
    {
        return getProfile() + "/download/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath()
    {
        return getProfile() + "/upload";
    }
}
