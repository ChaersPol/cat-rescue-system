package com.ruoyi.cat.service.impl;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
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
     * 获取扫码前端地址前缀（从配置读取，默认空）
     */
    private String getScanBaseUrl()
    {
        CatConfig config = catConfigMapper.selectCatConfigByKey("scan.base.url");
        if (config != null && StringUtils.isNotEmpty(config.getConfigValue()))
        {
            return config.getConfigValue();
        }
        return "";
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
}
