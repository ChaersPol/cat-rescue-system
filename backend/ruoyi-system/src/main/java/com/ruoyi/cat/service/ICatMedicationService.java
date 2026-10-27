package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatMedication;

/**
 * 每日用药记录 服务层
 * 
 * @author ruoyi
 */
public interface ICatMedicationService
{
    public CatMedication selectCatMedicationById(Long medicationId);

    public List<CatMedication> selectCatMedicationList(CatMedication medicationId);

    public int insertCatMedication(CatMedication medicationId);

    public int updateCatMedication(CatMedication medicationId);

    public int deleteCatMedicationByIds(Long[] medicationIds);

    /**
     * 批量录入连续多天用药（从 useDate 起连续 days 天，重复的日期自动跳过）
     */
    public int insertCatMedicationBatch(CatMedication medication, Integer days);
}
