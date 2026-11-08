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

    /**
     * 按当前访问地址生成二维码图片（Base64，不落库）
     * <p>
     * 二维码是「指到哪儿」的东西，访问地址一变（换端口、换域名）老图就废了。
     * 所以详情页、打印卡片页都走这个方法实时画，保证图和地址永远是同一份。
     *
     * @param archiveNo 档案编号
     * @return data:image/png;base64,...；档案编号为空返回空串
     */
    public String encodeQrCode(String archiveNo);

    /**
     * 外送医院候选（档案与转诊里用过的医院名去重），供表单下拉选择
     */
    public List<String> selectHospitalOptions();
}
