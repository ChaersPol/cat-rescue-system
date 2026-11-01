package com.ruoyi.cat.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Autowired
    private CatReminderConflictService conflictService;

    @Override
    public CatVaccine selectCatVaccineById(Long vaccineId)
    {
        CatVaccine vaccine = vaccineIdMapper.selectCatVaccineById(vaccineId);
        if (vaccine != null && vaccine.getCatId() != null)
        {
            vaccine.setMedicationTip(
                conflictService.medicationTipForCat(vaccine.getCatId(), "接种疫苗，建议推迟"));
        }
        return vaccine;
    }

    @Override
    public List<CatVaccine> selectCatVaccineList(CatVaccine vaccineId)
    {
        List<CatVaccine> list = vaccineIdMapper.selectCatVaccineList(vaccineId);
        // 「下次接种」旁边要提示「该猫正在用药、暂缓接种」，一次批量查完用药再回填
        if (list != null && !list.isEmpty())
        {
            Set<Long> catIds = new LinkedHashSet<>();
            for (CatVaccine v : list)
            {
                if (v.getCatId() != null && v.getNextDate() != null)
                {
                    catIds.add(v.getCatId());
                }
            }
            if (!catIds.isEmpty())
            {
                Map<Long, String> tips = conflictService.medicationTips(
                    new ArrayList<>(catIds), "接种疫苗，建议推迟");
                for (CatVaccine v : list)
                {
                    if (v.getCatId() != null)
                    {
                        v.setMedicationTip(tips.get(v.getCatId()));
                    }
                }
            }
        }
        return list;
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
