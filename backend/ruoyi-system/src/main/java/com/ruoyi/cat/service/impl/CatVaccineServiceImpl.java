package com.ruoyi.cat.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.domain.CatMaterial;
import com.ruoyi.cat.domain.CatReminderSyncResult;
import com.ruoyi.cat.mapper.CatMaterialMapper;
import com.ruoyi.cat.mapper.CatVaccineMapper;
import com.ruoyi.cat.service.ICatStaffService;
import com.ruoyi.cat.service.ICatVaccineService;
import com.ruoyi.cat.util.CatImmunizationRule;
import com.ruoyi.common.exception.ServiceException;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private ICatStaffService staffService;

    @Autowired
    private CatMaterialMapper materialMapper;

    @Autowired
    private CatLedgerStockService ledgerStockService;

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
    @Transactional
    public int insertCatVaccine(CatVaccine vaccineId)
    {
        // 负责人由助理承担：助理本人录入强制记本人，管理员只能从助理账号里选
        vaccineId.setResponsible(staffService.requireForInsert("负责人", ICatStaffService.SCOPE_ASSISTANT,
                vaccineId.getResponsible(), true));
        // 页面会自动算好「下次接种」，这里兜底一次：接口直接调用（扫码快速记一笔等）也不会漏
        if (vaccineId.getNextDate() == null)
        {
            vaccineId.setNextDate(CatImmunizationRule.nextDate(vaccineId.getVaccineType(), vaccineId.getVaccineDate()));
        }
        normalizeMaterial(vaccineId);
        int rows = vaccineIdMapper.insertCatVaccine(vaccineId);
        if (rows > 0)
        {
            ledgerStockService.sync(CatLedgerStockService.REF_VACCINE, vaccineId.getVaccineId(),
                    vaccineId.getMaterialId(), vaccineId.getStockQty(), vaccineId.getCatId(),
                    vaccineId.getVaccineDate(), "疫苗", vaccineId.getVaccineType());
        }
        return rows;
    }

    @Override
    @Transactional
    public CatReminderSyncResult updateCatVaccine(CatVaccine vaccineId)
    {
        CatReminderSyncResult result = new CatReminderSyncResult();
        // 改之前先取出旧日期，用来判断「下次接种」到底有没有变
        CatVaccine old = vaccineId.getVaccineId() != null
            ? vaccineIdMapper.selectCatVaccineById(vaccineId.getVaccineId())
            : null;
        vaccineId.setResponsible(staffService.normalizeForUpdate(ICatStaffService.SCOPE_ASSISTANT,
                vaccineId.getResponsible(), false));
        normalizeMaterial(vaccineId);
        boolean materialTouched = vaccineId.getMaterialId() != null
                || Boolean.TRUE.equals(vaccineId.getClearMaterial());
        int rows = vaccineIdMapper.updateCatVaccine(vaccineId);
        result.setRows(rows);
        if (rows > 0 && materialTouched)
        {
            Long catId = vaccineId.getCatId() != null ? vaccineId.getCatId()
                    : (old == null ? null : old.getCatId());
            java.util.Date happen = vaccineId.getVaccineDate() != null ? vaccineId.getVaccineDate()
                    : (old == null ? null : old.getVaccineDate());
            ledgerStockService.sync(CatLedgerStockService.REF_VACCINE, vaccineId.getVaccineId(),
                    vaccineId.getMaterialId(), vaccineId.getStockQty(), catId, happen,
                    "疫苗", vaccineId.getVaccineType() != null ? vaccineId.getVaccineType()
                            : (old == null ? null : old.getVaccineType()));
        }
        if (rows > 0 && old != null)
        {
            Long catId = vaccineId.getCatId() != null ? vaccineId.getCatId() : old.getCatId();
            reminderSyncService.syncFromPlan(CatReminderSyncService.SOURCE_VACCINE, vaccineId.getVaccineId(),
                catId, old.getNextDate(), vaccineId.getNextDate(), result);
        }
        return result;
    }

    @Override
    @Transactional
    public CatReminderSyncResult deleteCatVaccineByIds(Long[] vaccineIds)
    {
        CatReminderSyncResult result = new CatReminderSyncResult();
        // 删掉接种记录等于这批疫苗没用上：冲销它产生的出库单
        if (vaccineIds != null)
        {
            for (Long id : vaccineIds)
            {
                if (id != null)
                {
                    ledgerStockService.clear(CatLedgerStockService.REF_VACCINE, id, null);
                }
            }
        }
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

    /**
     * 关联物资归一化：0 表示「不关联」（转 null 并置 clearMaterial），有值则按物资表补齐名称快照
     */
    private void normalizeMaterial(CatVaccine vaccine)
    {
        Long materialId = vaccine.getMaterialId();
        if (materialId != null && materialId == 0L)
        {
            vaccine.setMaterialId(null);
            vaccine.setMaterialName(null);
            vaccine.setStockQty(null);
            vaccine.setClearMaterial(true);
            return;
        }
        if (materialId == null)
        {
            return;
        }
        CatMaterial material = materialMapper.selectCatMaterialById(materialId);
        if (material == null)
        {
            throw new ServiceException("所选物资不存在或已删除，请重新选择");
        }
        vaccine.setMaterialName(material.getMaterialName());
    }
}
