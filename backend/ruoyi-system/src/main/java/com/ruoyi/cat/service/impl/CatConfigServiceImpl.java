package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatConfig;
import com.ruoyi.cat.mapper.CatConfigMapper;
import com.ruoyi.cat.service.ICatConfigService;

/**
 * 系统配置 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatConfigServiceImpl implements ICatConfigService
{
    @Autowired
    private CatConfigMapper configIdMapper;

    @Override
    public CatConfig selectCatConfigById(Long configId)
    {
        return configIdMapper.selectCatConfigById(configId);
    }

    @Override
    public List<CatConfig> selectCatConfigList(CatConfig configId)
    {
        return configIdMapper.selectCatConfigList(configId);
    }

    @Override
    public int insertCatConfig(CatConfig configId)
    {
        return configIdMapper.insertCatConfig(configId);
    }

    @Override
    public int updateCatConfig(CatConfig configId)
    {
        return configIdMapper.updateCatConfig(configId);
    }

    @Override
    public int deleteCatConfigByIds(Long[] configIds)
    {
        return configIdMapper.deleteCatConfigByIds(configIds);
    }
}
