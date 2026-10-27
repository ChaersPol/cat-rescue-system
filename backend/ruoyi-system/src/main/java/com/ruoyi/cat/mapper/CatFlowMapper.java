package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatFlow;

/**
 * 流转记录 数据层
 * 
 * @author ruoyi
 */
public interface CatFlowMapper
{
    public CatFlow selectCatFlowById(Long flowId);

    public List<CatFlow> selectCatFlowList(CatFlow flowId);

    public int insertCatFlow(CatFlow flowId);

    public int updateCatFlow(CatFlow flowId);

    public int deleteCatFlowById(Long flowId);

    public int deleteCatFlowByIds(Long[] flowIds);
}
