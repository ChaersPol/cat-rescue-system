package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatAdoption;

/**
 * 领养登记 服务层
 * 
 * @author ruoyi
 */
public interface ICatAdoptionService
{
    public CatAdoption selectCatAdoptionById(Long adoptionId);

    public List<CatAdoption> selectCatAdoptionList(CatAdoption adoptionId);

    public int insertCatAdoption(CatAdoption adoptionId);

    public int updateCatAdoption(CatAdoption adoptionId);

    /**
     * 回访结束开关
     * <p>
     * 关闭（closed=true）：该领养下所有「待回访」计划置为「已结束」，并清掉对应未处理提醒；
     * 重新打开（closed=false）：把「已结束」的计划恢复成「待回访」，计划日期已超过
     * {@code visit.void.months} 个月的一律转「已作废」，不再提醒。
     *
     * @return 本次实际改动的回访计划条数
     */
    public int setVisitClosed(Long adoptionId, boolean closed);

    public int deleteCatAdoptionByIds(Long[] adoptionIds);
}
