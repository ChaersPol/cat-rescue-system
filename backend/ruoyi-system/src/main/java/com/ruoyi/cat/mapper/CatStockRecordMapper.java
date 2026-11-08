package com.ruoyi.cat.mapper;

import java.util.List;
import java.util.Map;
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

    /**
     * 某张诊断单自动出入库的净值（出库为正、入库为负），按物资汇总。
     * 修改/删除诊断时按净值回滚库存：先算净额再补一张反向单，
     * 反复改同一个诊断也不会重复回滚。
     */
    public List<Map<String, Object>> selectNetByDiagnosisId(Long diagnosisId);

    /**
     * 某条台账记录（用药/疫苗/驱虫）产生的出入库净值（出库为正、冲销入库为负），按物资汇总。
     * 改用量、换物资、删记录时按净值补一张反向单，重复调用不会重复回滚。
     *
     * @param refType 业务类型：1 用药 2 疫苗 3 驱虫
     * @param refId   业务主键
     */
    public List<Map<String, Object>> selectNetByRef(@org.apache.ibatis.annotations.Param("refType") String refType,
                                                    @org.apache.ibatis.annotations.Param("refId") Long refId);
}
