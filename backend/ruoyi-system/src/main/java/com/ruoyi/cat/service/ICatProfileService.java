package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatProfile;

/**
 * 猫咪档案 服务层
 * 
 * @author ruoyi
 */
public interface ICatProfileService
{
    public CatProfile selectCatProfileById(Long catId);

    public List<CatProfile> selectCatProfileList(CatProfile catProfile);

    public int insertCatProfile(CatProfile catProfile);

    public int updateCatProfile(CatProfile catProfile);

    public int deleteCatProfileByIds(Long[] catIds);

    /**
     * 归档猫咪（离世、放归、领养）
     */
    public int archiveCatProfile(Long catId);

    /**
     * 取消归档（把猫咪重新放回在养列表，用于误操作或复诊）
     */
    public int unarchiveCatProfile(Long catId);

    /**
     * 重新生成猫咪二维码（按当前扫码地址前缀配置）
     *
     * @return 生成结果：qrCode（新二维码 Base64）、scanUrl（二维码内容）、changed（与旧码相比内容是否变化）；猫咪不存在返回 null
     */
    public java.util.Map<String, Object> regenerateQrCode(Long catId);

    /**
     * 批量重新生成二维码（历史数据补码）
     */
    public int regenerateAllQrCode();
}
