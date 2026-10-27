package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatMedication;

/**
 * 每日用药记录 数据层
 * 
 * @author ruoyi
 */
public interface CatMedicationMapper
{
    public CatMedication selectCatMedicationById(Long medicationId);

    public List<CatMedication> selectCatMedicationList(CatMedication medicationId);

    public int insertCatMedication(CatMedication medicationId);

    public int updateCatMedication(CatMedication medicationId);

    public int deleteCatMedicationById(Long medicationId);

    public int deleteCatMedicationByIds(Long[] medicationIds);
}
