package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatWard;
import com.ruoyi.cat.mapper.CatWardMapper;
import com.ruoyi.cat.service.ICatWardService;

/**
 * 病房 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatWardServiceImpl implements ICatWardService
{
    @Autowired
    private CatWardMapper wardIdMapper;

    @Override
    public CatWard selectCatWardById(Long wardId)
    {
        return wardIdMapper.selectCatWardById(wardId);
    }

    @Override
    public List<CatWard> selectCatWardList(CatWard wardId)
    {
        return wardIdMapper.selectCatWardList(wardId);
    }

    @Override
    public int insertCatWard(CatWard wardId)
    {
        return wardIdMapper.insertCatWard(wardId);
    }

    @Override
    public int updateCatWard(CatWard wardId)
    {
        return wardIdMapper.updateCatWard(wardId);
    }

    @Override
    public int deleteCatWardByIds(Long[] wardIds)
    {
        return wardIdMapper.deleteCatWardByIds(wardIds);
    }
}
