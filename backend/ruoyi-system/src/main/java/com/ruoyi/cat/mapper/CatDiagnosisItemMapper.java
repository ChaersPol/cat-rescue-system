package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatDiagnosisItem;

/**
 * 站内诊断明细 数据层
 *
 * @author ruoyi
 */
public interface CatDiagnosisItemMapper
{
    public List<CatDiagnosisItem> selectByDiagnosisId(Long diagnosisId);

    public int batchInsert(@Param("list") List<CatDiagnosisItem> list);

    public int deleteByDiagnosisId(Long diagnosisId);
}
