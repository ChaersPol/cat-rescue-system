package com.ruoyi.cat.service.impl;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatConfig;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.mapper.CatConfigMapper;
import com.ruoyi.cat.service.ICatProfileService;
import com.ruoyi.common.utils.QrCodeUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.exception.ServiceException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 猫咪档案 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatProfileServiceImpl implements ICatProfileService
{
    /**
     * 手机扫码访问不到的本机主机名
     */
    private static final Set<String> UNREACHABLE_HOSTS = Set.of("localhost", "127.0.0.1", "0.0.0.0", "::1", "[::1]");

    @Autowired
    private CatProfileMapper catProfileMapper;

    @Autowired
    private CatConfigMapper catConfigMapper;

    @Override
    public CatProfile selectCatProfileById(Long catId)
    {
        CatProfile profile = catProfileMapper.selectCatProfileById(catId);
        // 编辑档案时回填已选标签，否则界面显示为空、一改动就会丢掉原有标签
        if (profile != null)
        {
            profile.setTagIds(catProfileMapper.selectTagIdsByCatId(catId));
            // 顺带带上二维码内容（扫码链接），前端可直接展示「二维码指向哪里」
            if (StringUtils.isNotEmpty(profile.getArchiveNo()))
            {
                profile.setScanUrl(buildScanUrl(profile.getArchiveNo()));
            }
        }
        return profile;
    }

    @Override
    public List<CatProfile> selectCatProfileList(CatProfile catProfile)
    {
        return catProfileMapper.selectCatProfileList(catProfile);
    }

    @Override
    @Transactional
    public int insertCatProfile(CatProfile catProfile)
    {
        // 自动生成档案编号
        if (StringUtils.isEmpty(catProfile.getArchiveNo()))
        {
            catProfile.setArchiveNo(generateArchiveNo());
        }
        // 默认状态：在养
        if (StringUtils.isEmpty(catProfile.getStatus()))
        {
            catProfile.setStatus("1");
        }
        // 默认未归档
        if (StringUtils.isEmpty(catProfile.getArchived()))
        {
            catProfile.setArchived("0");
        }
        // 档案编号唯一性校验
        CatProfile dup = catProfileMapper.selectByArchiveNo(catProfile.getArchiveNo());
        if (dup != null)
        {
            throw new ServiceException("档案编号「" + catProfile.getArchiveNo() + "」已存在，请更换");
        }
        int rows = catProfileMapper.insertCatProfile(catProfile);
        // 生成二维码
        if (rows > 0 && catProfile.getCatId() != null)
        {
            String qrCode = generateQrCode(catProfile);
            catProfile.setQrCode(qrCode);
            catProfileMapper.updateCatProfile(catProfile);
            // 保存标签关联
            insertCatTag(catProfile.getCatId(), catProfile.getTagIds());
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateCatProfile(CatProfile catProfile)
    {
        // 档案编号唯一性校验（排除自身）
        if (StringUtils.isNotEmpty(catProfile.getArchiveNo()))
        {
            CatProfile dup = catProfileMapper.selectByArchiveNo(catProfile.getArchiveNo());
            if (dup != null && !dup.getCatId().equals(catProfile.getCatId()))
            {
                throw new ServiceException("档案编号「" + catProfile.getArchiveNo() + "」已被其它档案占用");
            }
        }
        int rows = catProfileMapper.updateCatProfile(catProfile);
        // 二维码内容是「扫码地址前缀 + 档案编号」：按最新编号重算一次，和提交上来的旧码不同才写库
        // （前端保存时会把 form 里的旧 qrCode 一起提交，所以不能只判断 qrCode 是否为空）
        if (rows > 0 && StringUtils.isNotEmpty(catProfile.getArchiveNo()))
        {
            String freshQrCode = generateQrCode(catProfile);
            if (!freshQrCode.equals(catProfile.getQrCode()))
            {
                CatProfile qrUpdate = new CatProfile();
                qrUpdate.setCatId(catProfile.getCatId());
                qrUpdate.setQrCode(freshQrCode);
                catProfileMapper.updateCatProfile(qrUpdate);
            }
        }
        // 更新标签关联
        if (catProfile.getTagIds() != null)
        {
            catProfileMapper.deleteCatTagByCatId(catProfile.getCatId());
            insertCatTag(catProfile.getCatId(), catProfile.getTagIds());
        }
        return rows;
    }

    @Override
    public int deleteCatProfileByIds(Long[] catIds)
    {
        return catProfileMapper.deleteCatProfileByIds(catIds);
    }

    @Override
    public int archiveCatProfile(Long catId)
    {
        return catProfileMapper.archiveCatProfile(catId);
    }

    @Override
    public int unarchiveCatProfile(Long catId)
    {
        return catProfileMapper.updateArchived(catId, "0");
    }

    @Override
    @Transactional
    public Map<String, Object> regenerateQrCode(Long catId)
    {
        CatProfile cat = catProfileMapper.selectCatProfileById(catId);
        if (cat == null)
        {
            return null;
        }
        String scanUrl = buildScanUrl(cat.getArchiveNo());
        String qrCode = QrCodeUtils.generateBase64(scanUrl, 300, 300);
        // 二维码内容只由「扫码地址前缀 + 档案编号」决定，两者都没变时重新生成得到的是同一张图
        boolean changed = !qrCode.equals(cat.getQrCode());
        CatProfile update = new CatProfile();
        update.setCatId(catId);
        update.setQrCode(qrCode);
        catProfileMapper.updateCatProfile(update);

        Map<String, Object> result = new HashMap<>();
        result.put("catId", catId);
        result.put("qrCode", qrCode);
        result.put("scanUrl", scanUrl);
        result.put("changed", changed);
        return result;
    }

    @Override
    public int regenerateAllQrCode()
    {
        List<CatProfile> list = catProfileMapper.selectCatProfileList(new CatProfile());
        int count = 0;
        for (CatProfile cat : list)
        {
            CatProfile update = new CatProfile();
            update.setCatId(cat.getCatId());
            update.setQrCode(generateQrCode(cat));
            count += catProfileMapper.updateCatProfile(update);
        }
        return count;
    }

    /**
     * 生成唯一档案编号：CAT + yyyyMMdd + 4位随机数
     */
    private String generateArchiveNo()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String prefix = "CAT" + sdf.format(new Date());
        String random = String.valueOf((int) (Math.random() * 9000 + 1000));
        return prefix + random;
    }

    /**
     * 生成二维码（指向移动端扫码档案页）
     */
    private String generateQrCode(CatProfile catProfile)
    {
        return QrCodeUtils.generateBase64(buildScanUrl(catProfile.getArchiveNo()), 300, 300);
    }

    /**
     * 扫码链接格式：{扫码地址前缀}/m/cat/{archiveNo}（history 路由）
     */
    private String buildScanUrl(String archiveNo)
    {
        return getScanBaseUrl() + "/m/cat/" + archiveNo;
    }

    /**
     * 获取扫码前端地址前缀
     * <p>
     * 取值优先级：
     * 1. 系统配置 scan.base.url（填了有效地址时以配置为准）；
     * 2. 配置为空或填了 localhost / 127.0.0.1 这类「手机扫不开」的地址时，
     *    自动回退到当前请求的访问地址（走反向代理时取 X-Forwarded-* / Host）；
     * 3. 两者都拿不到时才退回原始配置值。
     * <p>
     * 兜底的意义：用 IP 直连部署时没人会记得去改 scan.base.url，
     * 一旦配错，打印出来的贴纸上二维码就全指向 localhost，扫码必然打不开。
     */
    private String getScanBaseUrl()
    {
        CatConfig config = catConfigMapper.selectCatConfigByKey("scan.base.url");
        String configured = config == null ? "" : stripTrailingSlash(config.getConfigValue());
        if (StringUtils.isNotEmpty(configured) && !isUnreachableUrl(configured))
        {
            return configured;
        }
        String fromRequest = stripTrailingSlash(ServletUtils.getRequestBaseUrl());
        if (StringUtils.isNotEmpty(fromRequest) && !isUnreachableUrl(fromRequest))
        {
            return fromRequest;
        }
        return configured;
    }

    /**
     * 去掉地址末尾多余的斜杠，避免拼出双斜杠
     */
    private static String stripTrailingSlash(String url)
    {
        String result = url == null ? "" : url.trim();
        while (result.endsWith("/"))
        {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * 判断地址是否为「手机访问不到」的本机地址
     */
    private static boolean isUnreachableUrl(String url)
    {
        String host = extractHost(url);
        return StringUtils.isEmpty(host) || UNREACHABLE_HOSTS.contains(host) || host.startsWith("127.");
    }

    /**
     * 从 URL 中取出主机名（忽略协议、端口、路径、末尾斜杠）
     */
    private static String extractHost(String url)
    {
        if (StringUtils.isBlank(url))
        {
            return "";
        }
        String rest = url.trim();
        int schemeIdx = rest.indexOf("://");
        if (schemeIdx >= 0)
        {
            rest = rest.substring(schemeIdx + 3);
        }
        int slashIdx = rest.indexOf('/');
        if (slashIdx >= 0)
        {
            rest = rest.substring(0, slashIdx);
        }
        if (rest.startsWith("["))
        {
            // IPv6 字面量：[::1]:8080
            int end = rest.indexOf(']');
            return end > 0 ? rest.substring(0, end + 1).toLowerCase() : rest.toLowerCase();
        }
        int colonIdx = rest.indexOf(':');
        if (colonIdx >= 0)
        {
            rest = rest.substring(0, colonIdx);
        }
        return rest.toLowerCase();
    }

    /**
     * 保存猫咪标签关联
     */
    private void insertCatTag(Long catId, Long[] tagIds)
    {
        if (tagIds != null && tagIds.length > 0)
        {
            catProfileMapper.batchCatTag(catId, tagIds);
        }
    }

    @Override
    public List<String> selectHospitalOptions()
    {
        return catProfileMapper.selectHospitalOptions();
    }
}
