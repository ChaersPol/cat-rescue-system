package com.ruoyi.cat.service;

import java.util.Date;
import java.util.List;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.domain.CatMedicationExec;
import com.ruoyi.cat.domain.CatMedicationGroup;

/**
 * 每日用药记录 服务层
 * <p>
 * 一条记录里混了两类东西，权限也不同：
 * <ul>
 *   <li>医生方案：猫咪、用药日期、时间段、药品名称、剂量、给药方式 —— 只有医生（和管
 *       理岗）能开、能改，助理只读；</li>
 *   <li>执行情况：执行人、不良反应、用药照片、备注 —— 助理填，医生也能看。</li>
 * </ul>
 *
 * @author ruoyi
 */
public interface ICatMedicationService
{
    public CatMedication selectCatMedicationById(Long medicationId);

    public List<CatMedication> selectCatMedicationList(CatMedication medicationId);

    /** 合并视图：同一只猫 + 同一天 + 同一时间段 一行 */
    public List<CatMedicationGroup> selectCatMedicationGroupList(CatMedicationGroup query);

    /** 合并视图展开：某一组的药品明细 */
    public List<CatMedication> selectGroupDetails(Long catId, Date useDate, String timePeriod);

    public int insertCatMedication(CatMedication medicationId);

    public int updateCatMedication(CatMedication medicationId);

    public int deleteCatMedicationByIds(Long[] medicationIds);

    /**
     * 批量录入连续多天用药（从 useDate 起连续 days 天，重复的日期自动跳过）
     */
    public int insertCatMedicationBatch(CatMedication medication, Integer days);

    /**
     * 保存执行打卡（整组：猫 + 日期 + 时间段）
     * <p>
     * 点「完成」必须已经上传照片；撤回（status=0）不会清掉照片和完成痕迹，
     * 靠 update_time 记下撤回/补照片的时间点。
     */
    public CatMedicationExec saveExec(CatMedicationExec request);

    /**
     * 医生在用药记录里改「这一次」的方案（一组 = 猫 + 日期 + 时间段）
     * <p>
     * 只动这一组，不回头改处方；已完成打卡的组不允许改。
     */
    public void saveGroupPlan(CatMedicationGroup request);
}
