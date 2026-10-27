package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatBase;

/**
 * 基地 数据层
 * 
 * @author ruoyi
 */
public interface CatBaseMapper
{
    public CatBase selectCatBaseById(Long baseId);

    public List<CatBase> selectCatBaseList(CatBase baseId);

    public int insertCatBase(CatBase baseId);

    public int updateCatBase(CatBase baseId);

    public int deleteCatBaseById(Long baseId);

    public int deleteCatBaseByIds(Long[] baseIds);
}
