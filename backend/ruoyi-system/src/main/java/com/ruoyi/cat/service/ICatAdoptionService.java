package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatAdoption;

/**
 * 领养登记 服务层
 * 
 * @author ruoyi
 */
public interface ICatAdoptionService
{
    public CatAdoption selectCatAdoptionById(Long adoptionId);

    public List<CatAdoption> selectCatAdoptionList(CatAdoption adoptionId);

    public int insertCatAdoption(CatAdoption adoptionId);

    public int updateCatAdoption(CatAdoption adoptionId);

    public int deleteCatAdoptionByIds(Long[] adoptionIds);
}
