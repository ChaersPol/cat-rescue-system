package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatDeath;

/**
 * 死亡台账 服务层
 * 
 * @author ruoyi
 */
public interface ICatDeathService
{
    public CatDeath selectCatDeathById(Long deathId);

    public List<CatDeath> selectCatDeathList(CatDeath catDeath);

    public int insertCatDeath(CatDeath catDeath);

    public int updateCatDeath(CatDeath catDeath);

    public int deleteCatDeathByIds(Long[] deathIds);
}
