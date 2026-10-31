package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatBase;
import com.ruoyi.cat.domain.CatDiagnosis;
import com.ruoyi.cat.mapper.CatBaseMapper;
import com.ruoyi.cat.mapper.CatDiagnosisMapper;
import com.ruoyi.cat.service.ICatDiagnosisService;
import com.ruoyi.common.utils.StringUtils;

/**
 * 站内诊断记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatDiagnosisServiceImpl implements ICatDiagnosisService
{
    @Autowired
    private CatDiagnosisMapper diagnosisIdMapper;

    @Autowired
    private CatBaseMapper baseMapper;

    /** 基地类型：医疗基地（字典 cat_base_type） */
    private static final String BASE_TYPE_MEDICAL = "2";

    @Override
    public CatDiagnosis selectCatDiagnosisById(Long diagnosisId)
    {
        return diagnosisIdMapper.selectCatDiagnosisById(diagnosisId);
    }

    @Override
    public List<CatDiagnosis> selectCatDiagnosisList(CatDiagnosis diagnosisId)
    {
        return diagnosisIdMapper.selectCatDiagnosisList(diagnosisId);
    }

    @Override
    public int insertCatDiagnosis(CatDiagnosis diagnosisId)
    {
        // 需求文档 6.2：站内诊断记录仅限于医疗基地，且每条记录都要标明当前治疗基地
        if (diagnosisId.getBaseId() == null)
        {
            throw new com.ruoyi.common.exception.ServiceException("请选择当前治疗基地");
        }
        CatBase base = baseMapper.selectCatBaseById(diagnosisId.getBaseId());
        if (base == null || !BASE_TYPE_MEDICAL.equals(base.getBaseType()))
        {
            throw new com.ruoyi.common.exception.ServiceException("站内诊断仅限医疗基地，请选择医疗基地");
        }
        return diagnosisIdMapper.insertCatDiagnosis(diagnosisId);
    }

    @Override
    public int updateCatDiagnosis(CatDiagnosis diagnosisId)
    {
        return diagnosisIdMapper.updateCatDiagnosis(diagnosisId);
    }

    @Override
    public int correctCatDiagnosis(Long diagnosisId, String correctionNote, String operator)
    {
        CatDiagnosis exist = diagnosisIdMapper.selectCatDiagnosisById(diagnosisId);
        if (exist == null)
        {
            throw new com.ruoyi.common.exception.ServiceException("诊断记录不存在");
        }
        if (StringUtils.isEmpty(correctionNote))
        {
            throw new com.ruoyi.common.exception.ServiceException("更正备注内容不能为空");
        }
        // 只追加更正说明，原始诊断字段一律不改，保证病史真实可追溯
        CatDiagnosis update = new CatDiagnosis();
        update.setDiagnosisId(diagnosisId);
        update.setCorrectionNote(correctionNote);
        update.setCorrectionBy(operator);
        update.setCorrectionTime(new java.util.Date());
        return diagnosisIdMapper.updateCatDiagnosis(update);
    }

    @Override
    public int deleteCatDiagnosisByIds(Long[] diagnosisIds)
    {
        return diagnosisIdMapper.deleteCatDiagnosisByIds(diagnosisIds);
    }
}
