package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatFlow;

/**
 * 流转记录 服务层
 * 
 * @author ruoyi
 */
public interface ICatFlowService
{
    public CatFlow selectCatFlowById(Long flowId);

    public List<CatFlow> selectCatFlowList(CatFlow flowId);

    public int insertCatFlow(CatFlow flowId);

    public int updateCatFlow(CatFlow flowId);

    public int deleteCatFlowByIds(Long[] flowIds);
}
