package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatConfig;

/**
 * 系统配置 服务层
 * 
 * @author ruoyi
 */
public interface ICatConfigService
{
    public CatConfig selectCatConfigById(Long configId);

    public List<CatConfig> selectCatConfigList(CatConfig configId);

    public int insertCatConfig(CatConfig configId);

    public int updateCatConfig(CatConfig configId);

    public int deleteCatConfigByIds(Long[] configIds);
}
