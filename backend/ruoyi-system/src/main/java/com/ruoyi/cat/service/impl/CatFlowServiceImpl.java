package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.mapper.CatFlowMapper;
import com.ruoyi.cat.service.ICatFlowService;

/**
 * 流转记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatFlowServiceImpl implements ICatFlowService
{
    @Autowired
    private CatFlowMapper flowIdMapper;

    @Override
    public CatFlow selectCatFlowById(Long flowId)
    {
        return flowIdMapper.selectCatFlowById(flowId);
    }

    @Override
    public List<CatFlow> selectCatFlowList(CatFlow flowId)
    {
        return flowIdMapper.selectCatFlowList(flowId);
    }

    @Override
    public int insertCatFlow(CatFlow flowId)
    {
        return flowIdMapper.insertCatFlow(flowId);
    }

    @Override
    public int updateCatFlow(CatFlow flowId)
    {
        return flowIdMapper.updateCatFlow(flowId);
    }

    @Override
    public int deleteCatFlowByIds(Long[] flowIds)
    {
        return flowIdMapper.deleteCatFlowByIds(flowIds);
    }
}
