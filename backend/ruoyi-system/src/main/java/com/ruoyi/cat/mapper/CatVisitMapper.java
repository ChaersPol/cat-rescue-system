package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatVisit;

/**
 * 回访记录 数据层
 * 
 * @author ruoyi
 */
public interface CatVisitMapper
{
    public CatVisit selectCatVisitById(Long visitId);

    public List<CatVisit> selectCatVisitList(CatVisit visitId);

    public int insertCatVisit(CatVisit visitId);

    public int updateCatVisit(CatVisit visitId);

    /**
     * 只改回访状态：回访结束开关、超期自动作废都用它
     * 走整表单更新会带上别的字段，容易被并发编辑覆盖
     */
    public int updateVisitStatus(@Param("visitId") Long visitId, @Param("visitStatus") String visitStatus);

    public int deleteCatVisitById(Long visitId);

    public int deleteCatVisitByIds(Long[] visitIds);
}
