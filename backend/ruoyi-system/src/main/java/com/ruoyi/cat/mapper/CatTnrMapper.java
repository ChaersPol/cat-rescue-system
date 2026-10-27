package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatTnr;

/**
 * TNR抓捕登记 数据层
 * 
 * @author ruoyi
 */
public interface CatTnrMapper
{
    public CatTnr selectCatTnrById(Long tnrId);

    public List<CatTnr> selectCatTnrList(CatTnr tnrId);

    public int insertCatTnr(CatTnr tnrId);

    public int updateCatTnr(CatTnr tnrId);

    public int deleteCatTnrById(Long tnrId);

    public int deleteCatTnrByIds(Long[] tnrIds);
}
