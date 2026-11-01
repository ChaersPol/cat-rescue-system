package com.ruoyi.cat.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.domain.CatReminderSyncResult;
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

    @Autowired
    private CatReminderSyncService reminderSyncService;

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
    public CatReminderSyncResult updateCatVaccine(CatVaccine vaccineId)
    {
        CatReminderSyncResult result = new CatReminderSyncResult();
        // 改之前先取出旧日期，用来判断「下次接种」到底有没有变
        CatVaccine old = vaccineId.getVaccineId() != null
            ? vaccineIdMapper.selectCatVaccineById(vaccineId.getVaccineId())
            : null;
        int rows = vaccineIdMapper.updateCatVaccine(vaccineId);
        result.setRows(rows);
        if (rows > 0 && old != null)
        {
            Long catId = vaccineId.getCatId() != null ? vaccineId.getCatId() : old.getCatId();
            reminderSyncService.syncFromPlan(CatReminderSyncService.SOURCE_VACCINE, vaccineId.getVaccineId(),
                catId, old.getNextDate(), vaccineId.getNextDate(), result);
        }
        return result;
    }

    @Override
    public CatReminderSyncResult deleteCatVaccineByIds(Long[] vaccineIds)
    {
        CatReminderSyncResult result = new CatReminderSyncResult();
        int rows = vaccineIdMapper.deleteCatVaccineByIds(vaccineIds);
        result.setRows(rows);
        if (rows > 0)
        {
            int cleaned = reminderSyncService.removeBySource(CatReminderSyncService.SOURCE_VACCINE, vaccineIds);
            if (cleaned > 0)
            {
                result.setSynced(Boolean.TRUE);
                result.setSyncCount(cleaned);
                result.setSyncTip("删除成功，同时清理了 " + cleaned + " 条关联的疫苗提醒");
            }
        }
        return result;
    }
}
