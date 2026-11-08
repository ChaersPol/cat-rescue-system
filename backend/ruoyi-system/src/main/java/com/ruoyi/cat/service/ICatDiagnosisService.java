package com.ruoyi.cat.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.cat.domain.CatDiagnosis;
import com.ruoyi.cat.domain.CatDiagnosisSummary;

/**
 * 站内诊断记录 服务层
 * 
 * @author ruoyi
 */
public interface ICatDiagnosisService
{
    public CatDiagnosis selectCatDiagnosisById(Long diagnosisId);

    public List<CatDiagnosis> selectCatDiagnosisList(CatDiagnosis diagnosisId);

    /** 按猫合并的诊断概览（一只猫一行，含就诊次数与最近一次诊断） */
    public List<CatDiagnosisSummary> selectCatDiagnosisSummaryList(CatDiagnosis diagnosisId);

    /** 当前筛选条件下的统计：病房（或基地）里的猫数 + 诊断记录条数 */
    public Map<String, Object> selectDiagnosisStat(CatDiagnosis diagnosisId);

    /** 病房下拉：每个病房当前在册猫数（已放归/已领养/已离世不算） */
    public List<Map<String, Object>> selectWardStats(CatDiagnosis diagnosisId);

    /**
     * 新增弹窗选中猫咪后的自动带出项：
     * 来源基地、当前基地/病房，以及默认就诊类型（看过病=复诊）
     */
    public Map<String, Object> selectCatHint(Long catId);

    /**
     * 接诊医生候选列表（「医生」角色的账号），管理员新增诊断时下拉选择
     */
    public List<Map<String, Object>> selectDoctorOptions();

    public int insertCatDiagnosis(CatDiagnosis diagnosisId);

    public int updateCatDiagnosis(CatDiagnosis diagnosisId);

    public int deleteCatDiagnosisByIds(Long[] diagnosisIds);

    /**
     * 追加更正备注（原始诊断记录不可修改，错误只能通过更正说明补充）
     */
    public int correctCatDiagnosis(Long diagnosisId, String correctionNote, String operator);
}
