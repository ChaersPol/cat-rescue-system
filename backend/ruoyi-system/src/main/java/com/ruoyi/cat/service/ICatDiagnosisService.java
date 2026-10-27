package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatDiagnosis;

/**
 * 站内诊断记录 服务层
 * 
 * @author ruoyi
 */
public interface ICatDiagnosisService
{
    public CatDiagnosis selectCatDiagnosisById(Long diagnosisId);

    public List<CatDiagnosis> selectCatDiagnosisList(CatDiagnosis diagnosisId);

    public int insertCatDiagnosis(CatDiagnosis diagnosisId);

    public int updateCatDiagnosis(CatDiagnosis diagnosisId);

    public int deleteCatDiagnosisByIds(Long[] diagnosisIds);

    /**
     * 追加更正备注（原始诊断记录不可修改，错误只能通过更正说明补充）
     */
    public int correctCatDiagnosis(Long diagnosisId, String correctionNote, String operator);
}
