package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatRelease;

/**
 * TNR放归登记 数据层
 * 
 * @author ruoyi
 */
public interface CatReleaseMapper
{
    public CatRelease selectCatReleaseById(Long releaseId);

    public List<CatRelease> selectCatReleaseList(CatRelease releaseId);

    public int insertCatRelease(CatRelease releaseId);

    public int updateCatRelease(CatRelease releaseId);

    public int deleteCatReleaseById(Long releaseId);

    public int deleteCatReleaseByIds(Long[] releaseIds);
}
