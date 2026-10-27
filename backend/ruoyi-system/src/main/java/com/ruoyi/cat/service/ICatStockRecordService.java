package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatStockRecord;

/**
 * 物资出入库记录 服务层
 * 
 * @author ruoyi
 */
public interface ICatStockRecordService
{
    public CatStockRecord selectCatStockRecordById(Long recordId);

    public List<CatStockRecord> selectCatStockRecordList(CatStockRecord recordId);

    public int insertCatStockRecord(CatStockRecord recordId);

    public int updateCatStockRecord(CatStockRecord recordId);

    public int deleteCatStockRecordByIds(Long[] recordIds);

    /**
     * 冲销记录：新增一条反向出入库记录并回滚库存
     */
    public int reverseCatStockRecord(Long recordId, String operator);
}
