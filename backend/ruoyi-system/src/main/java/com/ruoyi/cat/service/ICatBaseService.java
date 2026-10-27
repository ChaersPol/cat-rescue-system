package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatBase;

/**
 * 基地 服务层
 * 
 * @author ruoyi
 */
public interface ICatBaseService
{
    public CatBase selectCatBaseById(Long baseId);

    public List<CatBase> selectCatBaseList(CatBase baseId);

    public int insertCatBase(CatBase baseId);

    public int updateCatBase(CatBase baseId);

    public int deleteCatBaseByIds(Long[] baseIds);
}
