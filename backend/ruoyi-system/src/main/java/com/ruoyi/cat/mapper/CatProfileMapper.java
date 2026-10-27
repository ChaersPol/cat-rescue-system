package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatProfile;

/**
 * 猫咪档案 数据层
 * 
 * @author ruoyi
 */
public interface CatProfileMapper
{
    public CatProfile selectCatProfileById(Long catId);

    /**
     * 按档案编号精确查询（用于唯一性校验 / 扫码）
     */
    public CatProfile selectByArchiveNo(String archiveNo);

    public List<CatProfile> selectCatProfileList(CatProfile catProfile);

    public int insertCatProfile(CatProfile catProfile);

    public int updateCatProfile(CatProfile catProfile);

    public int deleteCatProfileById(Long catId);

    public int deleteCatProfileByIds(Long[] catIds);

    /**
     * 归档猫咪
     */
    public int archiveCatProfile(Long catId);

    /**
     * 批量删除猫咪标签关联
     */
    public int deleteCatTagByCatId(Long catId);

    /**
     * 查询某只猫咪的标签名称（用于档案 PDF 导出）
     */
    public java.util.List<String> selectTagNamesByCatId(Long catId);

    /**
     * 查询某只猫咪的标签 id（用于编辑档案时回填已选标签）
     */
    public Long[] selectTagIdsByCatId(Long catId);

    /**
     * 按 id 批量查询二维码（列表接口为控制体积不返回 qr_code，打印卡片时按需取）
     */
    public List<CatProfile> selectQrCodeByIds(Long[] catIds);

    /**
     * 更新归档状态（0 未归档 1 已归档）
     */
    public int updateArchived(@Param("catId") Long catId, @Param("archived") String archived);

    /**
     * 批量新增猫咪标签关联
     */
    public int batchCatTag(@Param("catId") Long catId, @Param("tagIds") Long[] tagIds);

    /**
     * 清空猫咪所在病房（外送医院、离世等场景）
     */
    public int clearCatWard(Long catId);

    /**
     * 清空猫咪所在基地与病房（领养、放归等离开基地场景）
     */
    public int clearCatLocation(Long catId);

    /**
     * 清空外送医院名称（医院返回/治愈返回等场景）
     */
    public int clearCatHospital(Long catId);
}
