package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatVaccine;

/**
 * 疫苗记录 数据层
 * 
 * @author ruoyi
 */
public interface CatVaccineMapper
{
    public CatVaccine selectCatVaccineById(Long vaccineId);

    public List<CatVaccine> selectCatVaccineList(CatVaccine vaccineId);

    public int insertCatVaccine(CatVaccine vaccineId);

    public int updateCatVaccine(CatVaccine vaccineId);

    public int deleteCatVaccineById(Long vaccineId);

    public int deleteCatVaccineByIds(Long[] vaccineIds);
}
