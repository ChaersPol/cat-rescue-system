package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatVisit;

/**
 * 回访记录 服务层
 * 
 * @author ruoyi
 */
public interface ICatVisitService
{
    public CatVisit selectCatVisitById(Long visitId);

    public List<CatVisit> selectCatVisitList(CatVisit visitId);

    public int insertCatVisit(CatVisit visitId);

    public int updateCatVisit(CatVisit visitId);

    public int deleteCatVisitByIds(Long[] visitIds);
}
