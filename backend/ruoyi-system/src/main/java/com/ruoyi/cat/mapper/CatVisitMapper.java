package com.ruoyi.cat.mapper;

import java.util.List;
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

    public int deleteCatVisitById(Long visitId);

    public int deleteCatVisitByIds(Long[] visitIds);
}
