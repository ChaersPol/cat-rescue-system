package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatDeath;

/**
 * 死亡台账 数据层
 * 
 * @author ruoyi
 */
public interface CatDeathMapper
{
    public CatDeath selectCatDeathById(Long deathId);

    public List<CatDeath> selectCatDeathList(CatDeath deathId);

    public int insertCatDeath(CatDeath deathId);

    public int updateCatDeath(CatDeath deathId);

    public int deleteCatDeathById(Long deathId);

    public int deleteCatDeathByIds(Long[] deathIds);
}
