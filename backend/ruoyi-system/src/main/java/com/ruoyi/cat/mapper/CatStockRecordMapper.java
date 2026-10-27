package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatStockRecord;

/**
 * 物资出入库记录 数据层
 * 
 * @author ruoyi
 */
public interface CatStockRecordMapper
{
    public CatStockRecord selectCatStockRecordById(Long recordId);

    public List<CatStockRecord> selectCatStockRecordList(CatStockRecord recordId);

    public int insertCatStockRecord(CatStockRecord recordId);

    public int updateCatStockRecord(CatStockRecord recordId);

    public int deleteCatStockRecordById(Long recordId);

    public int deleteCatStockRecordByIds(Long[] recordIds);
}
