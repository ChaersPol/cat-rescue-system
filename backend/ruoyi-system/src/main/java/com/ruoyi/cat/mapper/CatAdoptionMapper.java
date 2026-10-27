package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatAdoption;

/**
 * 领养登记 数据层
 * 
 * @author ruoyi
 */
public interface CatAdoptionMapper
{
    public CatAdoption selectCatAdoptionById(Long adoptionId);

    public List<CatAdoption> selectCatAdoptionList(CatAdoption adoptionId);

    public int insertCatAdoption(CatAdoption adoptionId);

    public int updateCatAdoption(CatAdoption adoptionId);

    public int deleteCatAdoptionById(Long adoptionId);

    public int deleteCatAdoptionByIds(Long[] adoptionIds);
}
