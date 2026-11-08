package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatPrescription;

/**
 * 用药处方 服务层
 * <p>
 * 处方是医生开的用药方案（几味药、几个时间段、连吃几天），保存时展开成
 * 「天 × 时段 × 药」的用药记录明细，助理照着执行打卡。助理只能看、不能开方改方。
 *
 * @author ruoyi
 */
public interface ICatPrescriptionService
{
    CatPrescription selectCatPrescriptionById(Long prescriptionId);

    /** 按诊断单取处方（编辑诊断时回显） */
    CatPrescription selectByDiagnosisId(Long diagnosisId);

    List<CatPrescription> selectCatPrescriptionList(CatPrescription query);

    /**
     * 保存处方（新增或修改）
     * <p>
     * 修改时只重建「今天及以后、尚未完成打卡」的用药明细，
     * 已完成的记录一律不动，并写入变更留痕。
     *
     * @return 处方id
     */
    Long saveCatPrescription(CatPrescription prescription);

    /** 停用处方：不再出现在待打卡里，未执行的明细清掉 */
    void stopCatPrescription(Long prescriptionId);

    /** 删除处方（软删），未执行的明细一起清掉 */
    void deleteCatPrescription(Long prescriptionId);
}
