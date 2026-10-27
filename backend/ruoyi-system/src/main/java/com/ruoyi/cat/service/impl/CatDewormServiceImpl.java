package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.service.ICatDewormService;

/**
 * 驱虫记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatDewormServiceImpl implements ICatDewormService
{
    @Autowired
    private CatDewormMapper dewormIdMapper;

    @Override
    public CatDeworm selectCatDewormById(Long dewormId)
    {
        return dewormIdMapper.selectCatDewormById(dewormId);
    }

    @Override
    public List<CatDeworm> selectCatDewormList(CatDeworm dewormId)
    {
        return dewormIdMapper.selectCatDewormList(dewormId);
    }

    @Override
    public int insertCatDeworm(CatDeworm dewormId)
    {
        return dewormIdMapper.insertCatDeworm(dewormId);
    }

    @Override
    public int updateCatDeworm(CatDeworm dewormId)
    {
        return dewormIdMapper.updateCatDeworm(dewormId);
    }

    @Override
    public int deleteCatDewormByIds(Long[] dewormIds)
    {
        return dewormIdMapper.deleteCatDewormByIds(dewormIds);
    }
}
