package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatRelease;

/**
 * TNR放归登记 服务层
 * 
 * @author ruoyi
 */
public interface ICatReleaseService
{
    public CatRelease selectCatReleaseById(Long releaseId);

    public List<CatRelease> selectCatReleaseList(CatRelease releaseId);

    public int insertCatRelease(CatRelease releaseId);

    public int updateCatRelease(CatRelease releaseId);

    public int deleteCatReleaseByIds(Long[] releaseIds);
}
