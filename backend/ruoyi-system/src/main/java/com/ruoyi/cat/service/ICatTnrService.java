package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatTnr;

/**
 * TNR抓捕登记 服务层
 * 
 * @author ruoyi
 */
public interface ICatTnrService
{
    public CatTnr selectCatTnrById(Long tnrId);

    public List<CatTnr> selectCatTnrList(CatTnr tnrId);

    public int insertCatTnr(CatTnr tnrId);

    public int updateCatTnr(CatTnr tnrId);

    public int deleteCatTnrByIds(Long[] tnrIds);
}
