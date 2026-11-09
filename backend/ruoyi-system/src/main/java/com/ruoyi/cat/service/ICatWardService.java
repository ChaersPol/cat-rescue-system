package com.ruoyi.cat.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.cat.domain.CatWard;

/**
 * 病房 服务层
 * 
 * @author ruoyi
 */
public interface ICatWardService
{
    public CatWard selectCatWardById(Long wardId);

    public List<CatWard> selectCatWardList(CatWard wardId);

    public int insertCatWard(CatWard wardId);

    public int updateCatWard(CatWard wardId);

    public int deleteCatWardByIds(Long[] wardIds);

    /**
     * 删除确认用：返回所选病房各自的在册猫数量（没有猫的也会返回，catCount 为 0）
     */
    public List<Map<String, Object>> selectWardCatCounts(Long[] wardIds);
}
