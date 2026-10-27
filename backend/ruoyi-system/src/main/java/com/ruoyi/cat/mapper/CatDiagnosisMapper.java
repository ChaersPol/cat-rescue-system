package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatDiagnosis;

/**
 * 站内诊断记录 数据层
 * 
 * @author ruoyi
 */
public interface CatDiagnosisMapper
{
    public CatDiagnosis selectCatDiagnosisById(Long diagnosisId);

    public List<CatDiagnosis> selectCatDiagnosisList(CatDiagnosis diagnosisId);

    public int insertCatDiagnosis(CatDiagnosis diagnosisId);

    public int updateCatDiagnosis(CatDiagnosis diagnosisId);

    public int deleteCatDiagnosisById(Long diagnosisId);

    public int deleteCatDiagnosisByIds(Long[] diagnosisIds);
}
