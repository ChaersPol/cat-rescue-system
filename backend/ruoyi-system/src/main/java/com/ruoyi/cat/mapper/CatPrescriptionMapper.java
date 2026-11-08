package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatPrescription;
import com.ruoyi.cat.domain.CatPrescriptionItem;
import com.ruoyi.cat.domain.CatPrescriptionLog;

/**
 * 用药处方 数据层
 *
 * @author ruoyi
 */
public interface CatPrescriptionMapper
{
    CatPrescription selectCatPrescriptionById(Long prescriptionId);

    /** 按诊断单取处方（一次诊断最多一张处方，编辑诊断时回显用） */
    CatPrescription selectByDiagnosisId(Long diagnosisId);

    List<CatPrescription> selectCatPrescriptionList(CatPrescription query);

    int insertCatPrescription(CatPrescription prescription);

    int updateCatPrescription(CatPrescription prescription);

    int deleteCatPrescriptionById(Long prescriptionId);

    List<CatPrescriptionItem> selectItemsByPrescriptionId(Long prescriptionId);

    int batchInsertItems(List<CatPrescriptionItem> items);

    int deleteItemsByPrescriptionId(Long prescriptionId);

    int insertLog(CatPrescriptionLog log);

    List<CatPrescriptionLog> selectLogsByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
