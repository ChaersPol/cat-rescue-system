package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatVisit;
import com.ruoyi.cat.mapper.CatVisitMapper;
import com.ruoyi.cat.service.ICatVisitService;

/**
 * 回访记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatVisitServiceImpl implements ICatVisitService
{
    @Autowired
    private CatVisitMapper visitIdMapper;

    @Override
    public CatVisit selectCatVisitById(Long visitId)
    {
        return visitIdMapper.selectCatVisitById(visitId);
    }

    @Override
    public List<CatVisit> selectCatVisitList(CatVisit visitId)
    {
        return visitIdMapper.selectCatVisitList(visitId);
    }

    @Override
    public int insertCatVisit(CatVisit visitId)
    {
        return visitIdMapper.insertCatVisit(visitId);
    }

    @Override
    public int updateCatVisit(CatVisit visitId)
    {
        return visitIdMapper.updateCatVisit(visitId);
    }

    @Override
    public int deleteCatVisitByIds(Long[] visitIds)
    {
        return visitIdMapper.deleteCatVisitByIds(visitIds);
    }
}
