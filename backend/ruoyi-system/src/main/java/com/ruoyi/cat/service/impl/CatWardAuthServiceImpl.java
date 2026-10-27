package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatWardAuth;
import com.ruoyi.cat.mapper.CatWardAuthMapper;
import com.ruoyi.cat.service.ICatWardAuthService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 病房授权 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatWardAuthServiceImpl implements ICatWardAuthService
{
    @Autowired
    private CatWardAuthMapper catWardAuthMapper;

    @Override
    public List<CatWardAuth> selectCatWardAuthList(CatWardAuth wardAuth)
    {
        return catWardAuthMapper.selectCatWardAuthList(wardAuth);
    }

    @Override
    public List<Long> selectWardIdsByUserId(Long userId)
    {
        return catWardAuthMapper.selectWardIdsByUserId(userId);
    }

    @Override
    public List<Long> selectUserIdsByWardId(Long wardId)
    {
        return catWardAuthMapper.selectUserIdsByWardId(wardId);
    }

    @Override
    @Transactional
    public int saveUserWardAuth(Long userId, Long[] wardIds)
    {
        // 先删后增
        catWardAuthMapper.deleteByUserId(userId);
        if (wardIds != null && wardIds.length > 0)
        {
            return catWardAuthMapper.batchInsert(userId, wardIds, SecurityUtils.getUsername());
        }
        return 1;
    }

    @Override
    public int insertCatWardAuth(CatWardAuth wardAuth)
    {
        if (wardAuth.getCreateBy() == null || wardAuth.getCreateBy().isEmpty())
        {
            wardAuth.setCreateBy(SecurityUtils.getUsername());
        }
        return catWardAuthMapper.insertCatWardAuth(wardAuth);
    }

    @Override
    public int deleteByUserId(Long userId)
    {
        return catWardAuthMapper.deleteByUserId(userId);
    }

    @Override
    public int deleteByWardId(Long wardId)
    {
        return catWardAuthMapper.deleteByWardId(wardId);
    }
}
