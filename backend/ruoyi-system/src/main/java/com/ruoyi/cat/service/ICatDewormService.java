package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatDeworm;

/**
 * 驱虫记录 服务层
 * 
 * @author ruoyi
 */
public interface ICatDewormService
{
    public CatDeworm selectCatDewormById(Long dewormId);

    public List<CatDeworm> selectCatDewormList(CatDeworm dewormId);

    public int insertCatDeworm(CatDeworm dewormId);

    public int updateCatDeworm(CatDeworm dewormId);

    public int deleteCatDewormByIds(Long[] dewormIds);
}
