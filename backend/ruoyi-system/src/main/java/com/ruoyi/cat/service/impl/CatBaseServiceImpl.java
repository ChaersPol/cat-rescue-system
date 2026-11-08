package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatBase;
import com.ruoyi.cat.mapper.CatBaseMapper;
import com.ruoyi.cat.service.ICatBaseService;
import com.ruoyi.cat.service.ICatStaffService;
import com.ruoyi.common.utils.StringUtils;

/**
 * 基地 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatBaseServiceImpl implements ICatBaseService
{
    @Autowired
    private CatBaseMapper baseIdMapper;

    @Autowired
    private ICatStaffService staffService;

    @Override
    public CatBase selectCatBaseById(Long baseId)
    {
        return baseIdMapper.selectCatBaseById(baseId);
    }

    @Override
    public List<CatBase> selectCatBaseList(CatBase baseId)
    {
        return baseIdMapper.selectCatBaseList(baseId);
    }

    @Override
    public int insertCatBase(CatBase baseId)
    {
        // 负责人是基地的固定属性，不是操作留痕：不锁本人，但名字必须对得上账号
        baseId.setLeader(staffService.validateOptional("负责人", ICatStaffService.SCOPE_ALL, baseId.getLeader()));
        return baseIdMapper.insertCatBase(baseId);
    }

    @Override
    public int updateCatBase(CatBase baseId)
    {
        // 名字没改动就放行（历史手填的老数据允许先改别的字段）；
        // 一旦重新选了人，就必须是系统账号，避免又写进对不上的名字
        String leader = baseId.getLeader();
        if (StringUtils.isNotEmpty(leader) && baseId.getBaseId() != null)
        {
            CatBase old = baseIdMapper.selectCatBaseById(baseId.getBaseId());
            boolean unchanged = old != null && leader.equals(old.getLeader());
            if (!unchanged)
            {
                baseId.setLeader(staffService.validateOptional("负责人", ICatStaffService.SCOPE_ALL, leader));
            }
        }
        return baseIdMapper.updateCatBase(baseId);
    }

    @Override
    public int deleteCatBaseByIds(Long[] baseIds)
    {
        return baseIdMapper.deleteCatBaseByIds(baseIds);
    }
}
