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

    /**
     * 最近一次外送医院时离开的病房 id
     * （外送会把病房清空，出院返院时用这条记录把猫放回原来的房间）
     *
     * @param catId 猫咪id
     * @return 病房id，没记录过则返回 null
     */
    public Long selectLastOutWardId(Long catId);
}
