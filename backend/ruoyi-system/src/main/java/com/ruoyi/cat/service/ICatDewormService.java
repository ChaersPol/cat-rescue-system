package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatReminderSyncResult;

/**
 * 驱虫记录 服务层
 * 
 * @author ruoyi
 */
public interface ICatDewormService
{
    public CatDeworm selectCatDewormById(Long dewormId);

    public List<CatDeworm> selectCatDewormList(CatDeworm dewormId);

    public int insertCatDeworm(CatDeworm dewormId);

    /**
     * 修改驱虫记录；「下次驱虫」改了会同步对应的提醒日期
     */
    public CatReminderSyncResult updateCatDeworm(CatDeworm dewormId);

    /**
     * 删除驱虫记录；同时清理它生成的提醒
     */
    public CatReminderSyncResult deleteCatDewormByIds(Long[] dewormIds);
}
