package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.mapper.CatVaccineMapper;
import com.ruoyi.cat.service.ICatVaccineService;

/**
 * 疫苗记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatVaccineServiceImpl implements ICatVaccineService
{
    @Autowired
    private CatVaccineMapper vaccineIdMapper;

    @Override
    public CatVaccine selectCatVaccineById(Long vaccineId)
    {
        return vaccineIdMapper.selectCatVaccineById(vaccineId);
    }

    @Override
    public List<CatVaccine> selectCatVaccineList(CatVaccine vaccineId)
    {
        return vaccineIdMapper.selectCatVaccineList(vaccineId);
    }

    @Override
    public int insertCatVaccine(CatVaccine vaccineId)
    {
        return vaccineIdMapper.insertCatVaccine(vaccineId);
    }

    @Override
    public int updateCatVaccine(CatVaccine vaccineId)
    {
        return vaccineIdMapper.updateCatVaccine(vaccineId);
    }

    @Override
    public int deleteCatVaccineByIds(Long[] vaccineIds)
    {
        return vaccineIdMapper.deleteCatVaccineByIds(vaccineIds);
    }
}
