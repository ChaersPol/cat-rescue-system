package com.ruoyi.cat.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.cat.domain.CatDiagnosis;
import com.ruoyi.cat.domain.CatDiagnosisSummary;

/**
 * 站内诊断记录 数据层
 * 
 * @author ruoyi
 */
public interface CatDiagnosisMapper
{
    public CatDiagnosis selectCatDiagnosisById(Long diagnosisId);

    public List<CatDiagnosis> selectCatDiagnosisList(CatDiagnosis diagnosisId);

    /** 按猫合并的诊断概览（一只猫一行） */
    public List<CatDiagnosisSummary> selectCatDiagnosisSummaryList(CatDiagnosis diagnosisId);

    /** 当前筛选条件下的记录条数 */
    public int countCatDiagnosis(CatDiagnosis diagnosisId);

    /** 当前筛选范围内的猫数（已放归/已领养/已离世不计入） */
    public int countCatsInScope(CatDiagnosis diagnosisId);

    /** 病房列表 + 每个病房当前在册猫数（筛选下拉显示「病房名（N）」） */
    public List<Map<String, Object>> selectWardStats(CatDiagnosis diagnosisId);

    /**
     * 病房/基地里「在册但一次都没接诊过」的猫（列表为空时提示「还有哪几只待接诊」）
     */
    public List<Map<String, Object>> selectPendingCats(CatDiagnosis diagnosisId);

    /** 这只猫在本站的诊断次数（用于判断初诊/复诊） */
    public int countVisitByCatId(Long catId);

    /** 接诊医生候选（「医生」角色的账号） */
    public List<Map<String, Object>> selectDoctorOptions();

    /** 按账号ID校验是否医生角色账号 */
    public Map<String, Object> selectDoctorUserById(Long userId);

    /** 按昵称反查医生账号 */
    public Map<String, Object> selectDoctorUserByNickName(String nickName);

    public int insertCatDiagnosis(CatDiagnosis diagnosisId);

    public int updateCatDiagnosis(CatDiagnosis diagnosisId);

    /** 清空所在病房（编辑时把病房改回「未填」） */
    public int clearWard(Long diagnosisId);

    public int deleteCatDiagnosisById(Long diagnosisId);

    public int deleteCatDiagnosisByIds(Long[] diagnosisIds);
}
