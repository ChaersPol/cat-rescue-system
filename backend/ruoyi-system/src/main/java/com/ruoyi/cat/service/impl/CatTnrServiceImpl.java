package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatTnr;
import com.ruoyi.cat.mapper.CatTnrMapper;
import com.ruoyi.cat.service.ICatTnrService;

/**
 * TNR抓捕登记 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatTnrServiceImpl implements ICatTnrService
{
    @Autowired
    private CatTnrMapper tnrIdMapper;

    @Override
    public CatTnr selectCatTnrById(Long tnrId)
    {
        return tnrIdMapper.selectCatTnrById(tnrId);
    }

    @Override
    public List<CatTnr> selectCatTnrList(CatTnr tnrId)
    {
        return tnrIdMapper.selectCatTnrList(tnrId);
    }

    @Override
    public int insertCatTnr(CatTnr tnrId)
    {
        return tnrIdMapper.insertCatTnr(tnrId);
    }

    @Override
    public int updateCatTnr(CatTnr tnrId)
    {
        return tnrIdMapper.updateCatTnr(tnrId);
    }

    @Override
    public int deleteCatTnrByIds(Long[] tnrIds)
    {
        return tnrIdMapper.deleteCatTnrByIds(tnrIds);
    }
}
