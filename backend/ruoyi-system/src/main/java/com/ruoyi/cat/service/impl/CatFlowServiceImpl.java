package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.mapper.CatFlowMapper;
import com.ruoyi.cat.service.ICatFlowService;
import com.ruoyi.cat.service.ICatStaffService;

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

    @Autowired
    private ICatStaffService staffService;

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
        // 操作人兜底记当前登录人昵称，保证流转记录与其它页面的人名口径一致
        if (flowId.getOperateBy() == null || flowId.getOperateBy().isEmpty())
        {
            flowId.setOperateBy(staffService.currentStaffName());
        }
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
