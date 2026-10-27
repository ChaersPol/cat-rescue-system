package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatDiagnosis;
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
