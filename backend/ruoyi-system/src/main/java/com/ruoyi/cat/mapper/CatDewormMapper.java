package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatDeworm;

/**
 * 驱虫记录 数据层
 * 
 * @author ruoyi
 */
public interface CatDewormMapper
{
    public CatDeworm selectCatDewormById(Long dewormId);

    public List<CatDeworm> selectCatDewormList(CatDeworm dewormId);

    public int insertCatDeworm(CatDeworm dewormId);

    public int updateCatDeworm(CatDeworm dewormId);

    public int deleteCatDewormById(Long dewormId);

    public int deleteCatDewormByIds(Long[] dewormIds);
}
