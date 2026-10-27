package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatVaccine;

/**
 * 疫苗记录 服务层
 * 
 * @author ruoyi
 */
public interface ICatVaccineService
{
    public CatVaccine selectCatVaccineById(Long vaccineId);

    public List<CatVaccine> selectCatVaccineList(CatVaccine vaccineId);

    public int insertCatVaccine(CatVaccine vaccineId);

    public int updateCatVaccine(CatVaccine vaccineId);

    public int deleteCatVaccineByIds(Long[] vaccineIds);
}
