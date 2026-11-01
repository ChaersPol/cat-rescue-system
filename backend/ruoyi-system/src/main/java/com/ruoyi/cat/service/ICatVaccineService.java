package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.domain.CatReminderSyncResult;

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

    /**
     * 修改疫苗记录；「下次接种」改了会同步对应的提醒日期
     */
    public CatReminderSyncResult updateCatVaccine(CatVaccine vaccineId);

    /**
     * 删除疫苗记录；同时清理它生成的提醒
     */
    public CatReminderSyncResult deleteCatVaccineByIds(Long[] vaccineIds);
}
