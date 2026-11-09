package com.ruoyi.cat.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * 删除前的「引用检查」（通用）。
 *
 * 只有一张表能说明「谁在引用我」的时候，每个模块各写一个 count 方法会大量重复，
 * 所以集中在这里，按 module 分支查询，统一返回：
 *   { id, name, refCount, detail }
 * detail 是给人看的文案（如「2 只猫咪、1 间病房」），refCount 用于前端判断能不能删。
 *
 * @author ruoyi
 */
public interface CatDeleteImpactMapper
{
    /**
     * @param module 模块标识（base / wardtype / tag / feecategory / materialcategory / material / checkitem）
     * @param ids    待删除的主键
     */
    public List<Map<String, Object>> selectDeleteImpact(@Param("module") String module, @Param("ids") Long[] ids);

    // ===== 级联清理（与 impact 配套，规则见 docs/CONVENTIONS.md 第十四节） =====

    /** 删领养 → 一并软删回访计划 */
    public int deleteVisitByAdoptionIds(@Param("ids") Long[] ids);

    /** 删处方 → 物理删处方明细 */
    public int deletePrescriptionItemByPrescriptionIds(@Param("ids") Long[] ids);

    /** 删处方 → 关联用药只解除关联 */
    public int clearMedicationPrescription(@Param("ids") Long[] ids);

    /** 删诊断单 → 物理删检查明细 */
    public int deleteDiagnosisItemByDiagnosisIds(@Param("ids") Long[] ids);

    public int clearMedicationDiagnosis(@Param("ids") Long[] ids);

    public int clearVaccineDiagnosis(@Param("ids") Long[] ids);

    public int clearDewormDiagnosis(@Param("ids") Long[] ids);

    public int clearPrescriptionDiagnosis(@Param("ids") Long[] ids);

    /** 删猫咪档案 → 名下业务记录软删（table 必须在 Service 端白名单内） */
    public int softDeleteByCatId(@Param("table") String table, @Param("ids") Long[] ids);

    /** 删猫咪档案 → 没有 del_flag 的表只能物理删（table 同样白名单控制） */
    public int hardDeleteByCatId(@Param("table") String table, @Param("ids") Long[] ids);

    public int deleteProfileTagByCatIds(@Param("ids") Long[] ids);

    public int deleteDiagnosisItemByCatIds(@Param("ids") Long[] ids);

    public int deletePrescriptionItemByCatIds(@Param("ids") Long[] ids);

    public int deleteMedicationExecByCatIds(@Param("ids") Long[] ids);
}
