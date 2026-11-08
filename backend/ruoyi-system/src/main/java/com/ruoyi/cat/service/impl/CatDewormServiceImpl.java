package com.ruoyi.cat.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatMaterial;
import com.ruoyi.cat.domain.CatReminderSyncResult;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.mapper.CatMaterialMapper;
import com.ruoyi.cat.service.ICatDewormService;
import com.ruoyi.cat.util.CatImmunizationRule;

/**
 * 驱虫记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatDewormServiceImpl implements ICatDewormService
{
    @Autowired
    private CatDewormMapper dewormIdMapper;

    @Autowired
    private CatReminderSyncService reminderSyncService;

    @Autowired
    private CatImmunizationSummaryService summaryService;

    @Autowired
    private CatMaterialMapper materialMapper;

    @Autowired
    private CatLedgerStockService ledgerStockService;

    @Override
    public CatDeworm selectCatDewormById(Long dewormId)
    {
        return dewormIdMapper.selectCatDewormById(dewormId);
    }

    @Override
    public List<CatDeworm> selectCatDewormList(CatDeworm dewormId)
    {
        return dewormIdMapper.selectCatDewormList(dewormId);
    }

    @Override
    @Transactional
    public int insertCatDeworm(CatDeworm dewormId)
    {
        requireDewormType(dewormId);
        // 页面按「本次驱虫日期 + 间隔（30/60 天）」自动算好，这里兜底：
        // 没传日期时退回到该猫上一次同类驱虫日期，避免扫码快速录入漏填就没了提醒
        if (dewormId.getNextDate() == null)
        {
            Date base = dewormId.getDewormDate();
            if (base == null && dewormId.getCatId() != null)
            {
                CatDeworm last = summaryService.lastDeworm(dewormId.getCatId(), typeKeyword(dewormId.getDewormType()));
                base = last == null ? null : last.getDewormDate();
            }
            dewormId.setNextDate(CatImmunizationRule.dewormNextDate(base, dewormId.getCycleDays()));
        }
        normalizeMaterial(dewormId);
        int rows = dewormIdMapper.insertCatDeworm(dewormId);
        if (rows > 0)
        {
            ledgerStockService.sync(CatLedgerStockService.REF_DEWORM, dewormId.getDewormId(),
                    dewormId.getMaterialId(), dewormId.getStockQty(), dewormId.getCatId(),
                    dewormId.getDewormDate(), "驱虫", dewormId.getDewormDrug());
        }
        return rows;
    }

    /**
     * 驱虫类型必须手选「体内驱虫 / 体外驱虫」
     * <p>
     * 以前新增默认就选中「体内驱虫」，医生不改就保存，台账里全是体内、体外永远空着；
     * 现在不预选并强制校验，缺了直接拦下来。
     */
    private void requireDewormType(CatDeworm deworm)
    {
        String type = deworm.getDewormType();
        if (type == null || type.trim().isEmpty())
        {
            throw new ServiceException("请选择驱虫类型：体内驱虫或体外驱虫");
        }
        if (!type.contains("体内") && !type.contains("体外"))
        {
            throw new ServiceException("驱虫类型只能是「体内驱虫」或「体外驱虫」");
        }
    }

    private String typeKeyword(String dewormType)
    {
        return dewormType != null && dewormType.contains("体外") ? "体外" : "体内";
    }

    @Override
    @Transactional
    public CatReminderSyncResult updateCatDeworm(CatDeworm dewormId)
    {
        CatReminderSyncResult result = new CatReminderSyncResult();
        CatDeworm old = dewormId.getDewormId() != null
            ? dewormIdMapper.selectCatDewormById(dewormId.getDewormId())
            : null;
        normalizeMaterial(dewormId);
        boolean materialTouched = dewormId.getMaterialId() != null
                || Boolean.TRUE.equals(dewormId.getClearMaterial());
        int rows = dewormIdMapper.updateCatDeworm(dewormId);
        result.setRows(rows);
        if (rows > 0 && materialTouched)
        {
            Long catId = dewormId.getCatId() != null ? dewormId.getCatId()
                    : (old == null ? null : old.getCatId());
            Date happen = dewormId.getDewormDate() != null ? dewormId.getDewormDate()
                    : (old == null ? null : old.getDewormDate());
            ledgerStockService.sync(CatLedgerStockService.REF_DEWORM, dewormId.getDewormId(),
                    dewormId.getMaterialId(), dewormId.getStockQty(), catId, happen,
                    "驱虫", dewormId.getDewormDrug() != null ? dewormId.getDewormDrug()
                            : (old == null ? null : old.getDewormDrug()));
        }
        if (rows > 0 && old != null)
        {
            Long catId = dewormId.getCatId() != null ? dewormId.getCatId() : old.getCatId();
            reminderSyncService.syncFromPlan(CatReminderSyncService.SOURCE_DEWORM, dewormId.getDewormId(),
                catId, old.getNextDate(), dewormId.getNextDate(), result);
        }
        return result;
    }

    @Override
    @Transactional
    public CatReminderSyncResult deleteCatDewormByIds(Long[] dewormIds)
    {
        CatReminderSyncResult result = new CatReminderSyncResult();
        // 删除驱虫记录：冲销它产生的出库单，库存回补
        if (dewormIds != null)
        {
            for (Long id : dewormIds)
            {
                if (id != null)
                {
                    ledgerStockService.clear(CatLedgerStockService.REF_DEWORM, id, null);
                }
            }
        }
        int rows = dewormIdMapper.deleteCatDewormByIds(dewormIds);
        result.setRows(rows);
        if (rows > 0)
        {
            int cleaned = reminderSyncService.removeBySource(CatReminderSyncService.SOURCE_DEWORM, dewormIds);
            if (cleaned > 0)
            {
                result.setSynced(Boolean.TRUE);
                result.setSyncCount(cleaned);
                result.setSyncTip("删除成功，同时清理了 " + cleaned + " 条关联的驱虫提醒");
            }
        }
        return result;
    }

    /**
     * 关联物资归一化：0 表示「不关联」（转 null 并置 clearMaterial），有值则按物资表补齐名称快照
     */
    private void normalizeMaterial(CatDeworm deworm)
    {
        Long materialId = deworm.getMaterialId();
        if (materialId != null && materialId == 0L)
        {
            deworm.setMaterialId(null);
            deworm.setMaterialName(null);
            deworm.setStockQty(null);
            deworm.setClearMaterial(true);
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
        deworm.setMaterialName(material.getMaterialName());
    }
}
