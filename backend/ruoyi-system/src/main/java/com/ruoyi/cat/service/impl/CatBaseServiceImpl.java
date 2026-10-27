package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatBase;
import com.ruoyi.cat.mapper.CatBaseMapper;
import com.ruoyi.cat.service.ICatBaseService;

/**
 * 基地 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatBaseServiceImpl implements ICatBaseService
{
    @Autowired
    private CatBaseMapper baseIdMapper;

    @Override
    public CatBase selectCatBaseById(Long baseId)
    {
        return baseIdMapper.selectCatBaseById(baseId);
    }

    @Override
    public List<CatBase> selectCatBaseList(CatBase baseId)
    {
        return baseIdMapper.selectCatBaseList(baseId);
    }

    @Override
    public int insertCatBase(CatBase baseId)
    {
        return baseIdMapper.insertCatBase(baseId);
    }

    @Override
    public int updateCatBase(CatBase baseId)
    {
        return baseIdMapper.updateCatBase(baseId);
    }

    @Override
    public int deleteCatBaseByIds(Long[] baseIds)
    {
        return baseIdMapper.deleteCatBaseByIds(baseIds);
    }
}
