package com.ruoyi.cat.service.impl;

import java.util.List;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatStockRecord;
import com.ruoyi.cat.mapper.CatStockRecordMapper;
import com.ruoyi.cat.mapper.CatMaterialMapper;
import com.ruoyi.cat.service.ICatStockRecordService;

/**
 * 物资出入库记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatStockRecordServiceImpl implements ICatStockRecordService
{
    @Autowired
    private CatStockRecordMapper catStockRecordMapper;

    @Autowired
    private CatMaterialMapper catMaterialMapper;

    @Override
    public CatStockRecord selectCatStockRecordById(Long recordId)
    {
        return catStockRecordMapper.selectCatStockRecordById(recordId);
    }

    @Override
    public List<CatStockRecord> selectCatStockRecordList(CatStockRecord catStockRecord)
    {
        return catStockRecordMapper.selectCatStockRecordList(catStockRecord);
    }

    @Override
    @Transactional
    public int insertCatStockRecord(CatStockRecord catStockRecord)
    {
        int rows = catStockRecordMapper.insertCatStockRecord(catStockRecord);
        // 联动更新库存
        if (rows > 0 && catStockRecord.getMaterialId() != null && catStockRecord.getQuantity() != null)
        {
            BigDecimal quantity = catStockRecord.getQuantity();
            // 出库为负
            if ("2".equals(catStockRecord.getRecordType()))
            {
                quantity = quantity.negate();
            }
            catMaterialMapper.updateStock(catStockRecord.getMaterialId(), quantity);
        }
        return rows;
    }

    @Override
    public int updateCatStockRecord(CatStockRecord catStockRecord)
    {
        // 出入库记录不允许修改（只允许冲销），这里直接返回失败提示由前端控制
        return 0;
    }

    @Override
    public int deleteCatStockRecordByIds(Long[] recordIds)
    {
        // 出入库记录不允许删除（只允许新增反向冲销记录）
        return 0;
    }

    /**
     * 冲销记录：新增一条反向记录
     */
    @Transactional
    public int reverseCatStockRecord(Long recordId, String operator)
    {
        CatStockRecord original = catStockRecordMapper.selectCatStockRecordById(recordId);
        if (original == null)
        {
            return 0;
        }
        CatStockRecord reverse = new CatStockRecord();
        reverse.setMaterialId(original.getMaterialId());
        // 反向类型
        reverse.setRecordType("1".equals(original.getRecordType()) ? "2" : "1");
        reverse.setQuantity(original.getQuantity());
        reverse.setRecordDate(new java.util.Date());
        reverse.setOperator(operator);
        reverse.setRemark("冲销原记录" + recordId);
        int rows = catStockRecordMapper.insertCatStockRecord(reverse);
        // 联动更新库存
        if (rows > 0)
        {
            BigDecimal quantity = reverse.getQuantity();
            if ("2".equals(reverse.getRecordType()))
            {
                quantity = quantity.negate();
            }
            catMaterialMapper.updateStock(reverse.getMaterialId(), quantity);
        }
        return rows;
    }
}
