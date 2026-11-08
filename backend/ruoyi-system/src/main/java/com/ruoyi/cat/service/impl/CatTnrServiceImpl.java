package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatTnr;
import com.ruoyi.cat.mapper.CatTnrMapper;
import com.ruoyi.cat.service.ICatStaffService;
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

    @Autowired
    private ICatStaffService staffService;

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
        // 手术医生只能是医生账号：医生本人录入记本人，其他人从医生列表选
        tnrId.setSurgeon(staffService.requireForInsert("手术医生", ICatStaffService.SCOPE_DOCTOR,
                tnrId.getSurgeon(), true));
        return tnrIdMapper.insertCatTnr(tnrId);
    }

    @Override
    public int updateCatTnr(CatTnr tnrId)
    {
        tnrId.setSurgeon(staffService.normalizeForUpdate(ICatStaffService.SCOPE_DOCTOR,
                tnrId.getSurgeon(), false));
        return tnrIdMapper.updateCatTnr(tnrId);
    }

    @Override
    public int deleteCatTnrByIds(Long[] tnrIds)
    {
        return tnrIdMapper.deleteCatTnrByIds(tnrIds);
    }
}
