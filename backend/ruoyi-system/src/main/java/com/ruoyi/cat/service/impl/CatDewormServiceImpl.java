package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatReminderSyncResult;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.service.ICatDewormService;

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
    public int insertCatDeworm(CatDeworm dewormId)
    {
        return dewormIdMapper.insertCatDeworm(dewormId);
    }

    @Override
    public CatReminderSyncResult updateCatDeworm(CatDeworm dewormId)
    {
        CatReminderSyncResult result = new CatReminderSyncResult();
        CatDeworm old = dewormId.getDewormId() != null
            ? dewormIdMapper.selectCatDewormById(dewormId.getDewormId())
            : null;
        int rows = dewormIdMapper.updateCatDeworm(dewormId);
        result.setRows(rows);
        if (rows > 0 && old != null)
        {
            Long catId = dewormId.getCatId() != null ? dewormId.getCatId() : old.getCatId();
            reminderSyncService.syncFromPlan(CatReminderSyncService.SOURCE_DEWORM, dewormId.getDewormId(),
                catId, old.getNextDate(), dewormId.getNextDate(), result);
        }
        return result;
    }

    @Override
    public CatReminderSyncResult deleteCatDewormByIds(Long[] dewormIds)
    {
        CatReminderSyncResult result = new CatReminderSyncResult();
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
}
