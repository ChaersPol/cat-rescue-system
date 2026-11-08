package com.ruoyi.cat.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.domain.CatMedicationGroup;

/**
 * 每日用药记录 数据层
 * 
 * @author ruoyi
 */
public interface CatMedicationMapper
{
    public CatMedication selectCatMedicationById(Long medicationId);

    public List<CatMedication> selectCatMedicationList(CatMedication medicationId);

    /**
     * 批量查询一批猫咪最近 days 天内的用药记录（疫苗/驱虫提醒判断「用药期间」用）
     *
     * @param catIds 猫咪 id 集合，不能为空
     * @param days   回溯天数
     * @return 用药记录，按用药日期倒序
     */
    public List<CatMedication> selectRecentMedicationByCatIds(@Param("catIds") List<Long> catIds,
                                                              @Param("days") int days);

    public int insertCatMedication(CatMedication medicationId);

    public int updateCatMedication(CatMedication medicationId);

    public int deleteCatMedicationById(Long medicationId);

    public int deleteCatMedicationByIds(Long[] medicationIds);

    /** 修改/删除诊断时，软删这张诊断单自动生成的台账记录 */
    public int deleteAutoByDiagnosisId(Long diagnosisId);

    /** 合并视图：按「猫 + 日期 + 时间段」分组分页查询 */
    public List<CatMedicationGroup> selectCatMedicationGroupList(CatMedicationGroup query);

    /** 合并视图展开：查某一组（猫 + 日期 + 时间段）的药品明细 */
    public List<CatMedication> selectGroupDetails(@Param("catId") Long catId,
                                                  @Param("useDate") Date useDate,
                                                  @Param("timePeriod") String timePeriod);

    /** 同一天同一时段同一种药是否已存在（处方展开时去重，避免重复开方刷出重复记录） */
    public int countSameDrug(@Param("catId") Long catId,
                             @Param("useDate") Date useDate,
                             @Param("timePeriod") String timePeriod,
                             @Param("drugName") String drugName,
                             @Param("prescriptionId") Long prescriptionId);

    /**
     * 医生改处方后用：软删某张处方下「fromDate 之后、还没完成打卡」的明细，
     * 已完成的记录一律不动（历史不能跟着变）
     */
    public int deleteUnfinishedByPrescription(@Param("prescriptionId") Long prescriptionId,
                                              @Param("fromDate") Date fromDate);

    /** 停用/删除处方时清理未执行的明细 */
    public int deleteUnfinishedByPrescriptionAll(@Param("prescriptionId") Long prescriptionId);

    /** 某天某组的执行状态（合并视图判断能否改方案用） */
    public String selectExecStatus(@Param("catId") Long catId,
                                   @Param("useDate") Date useDate,
                                   @Param("timePeriod") String timePeriod);
}
