package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatConfig;

/**
 * 系统配置 数据层
 * 
 * @author ruoyi
 */
public interface CatConfigMapper
{
    public CatConfig selectCatConfigById(Long configId);

    public List<CatConfig> selectCatConfigList(CatConfig configId);

    public int insertCatConfig(CatConfig configId);

    public int updateCatConfig(CatConfig configId);

    public int deleteCatConfigById(Long configId);

    public int deleteCatConfigByIds(Long[] configIds);

    /**
     * 按配置键查询
     */
    public CatConfig selectCatConfigByKey(String configKey);
}
