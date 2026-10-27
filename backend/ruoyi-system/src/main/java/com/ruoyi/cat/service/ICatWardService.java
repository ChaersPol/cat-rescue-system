package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatWard;

/**
 * 病房 服务层
 * 
 * @author ruoyi
 */
public interface ICatWardService
{
    public CatWard selectCatWardById(Long wardId);

    public List<CatWard> selectCatWardList(CatWard wardId);

    public int insertCatWard(CatWard wardId);

    public int updateCatWard(CatWard wardId);

    public int deleteCatWardByIds(Long[] wardIds);
}
