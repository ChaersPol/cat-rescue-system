package com.ruoyi.cat.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatMaterial;
import com.ruoyi.cat.domain.CatStockRecord;
import com.ruoyi.cat.mapper.CatMaterialMapper;
import com.ruoyi.cat.mapper.CatStockRecordMapper;
import com.ruoyi.cat.service.ICatStaffService;
import com.ruoyi.cat.service.ICatStockRecordService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 台账物资消耗：用药 / 疫苗 / 驱虫 手工录入时关联物资，保存即出库
 *
 * <p>设计口径（2026-11-08 与救助站确认，改之前先看 docs/CONVENTIONS.md）：
 * <ol>
 *   <li><b>一次发药只出库一次</b>：用药记录「连续 N 天」批量录入，天数只决定生成多少条打卡任务，
 *       库存按填写的「本次用量」扣一次，与诊断开方一致；</li>
 *   <li><b>库存允许为负</b>：现场急救不能被「库存不足」卡住，页面红字提示，事后入库自然回正；</li>
 *   <li><b>不重复扣</b>：诊断/处方自动生成的明细不挂物资（库存已由诊断单那条明细扣过），
 *       所以只有手工录入的路径会调用到这里。</li>
 * </ol>
 *
 * <p>冲销用「净值法」而不是逐条反向：先算出这条业务当前的净出库量，
 * 再补一张等量入库单。同一行反复改数量、改物资、删了又改，都不会把库存加多。
 */
@Service
public class CatLedgerStockService
{
    /** 业务类型：用药记录 */
    public static final String REF_MEDICATION = "1";
    /** 业务类型：疫苗台账 */
    public static final String REF_VACCINE = "2";
    /** 业务类型：驱虫台账 */
    public static final String REF_DEWORM = "3";

    @Autowired
    private CatStockRecordMapper stockRecordMapper;

    @Autowired
    private ICatStockRecordService stockRecordService;

    @Autowired
    private CatMaterialMapper materialMapper;

    @Autowired
    private ICatStaffService staffService;

    /**
     * 同步一条台账记录的物资出库
     *
     * @param refType    业务类型（REF_*）
     * @param refId      业务主键
     * @param materialId 关联物资；null 表示不消耗库存（顺带把这条记录以前的出库冲销掉）
     * @param qty        本次消耗数量；null / 0 表示本行不单独出库
     * @param catId      关联猫咪
     * @param happen     发生日期，空则记今天
     * @param title      业务名，写进用途，如「用药」「疫苗」「驱虫」
     * @param detail     明细名，如药品名 / 疫苗类型
     */
    public void sync(String refType, Long refId, Long materialId, BigDecimal qty,
                     Long catId, Date happen, String title, String detail)
    {
        if (refType == null || refId == null)
        {
            return;
        }
        rollback(refType, refId);
        if (materialId == null || qty == null || qty.compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        CatMaterial material = materialMapper.selectCatMaterialById(materialId);
        if (material == null)
        {
            throw new ServiceException("所选物资不存在或已删除，请重新选择");
        }
        CatStockRecord out = new CatStockRecord();
        out.setMaterialId(materialId);
        out.setRecordType("2");
        out.setQuantity(qty);
        out.setRecordDate(happen == null ? new Date() : happen);
        out.setPurpose(cut(buildPurpose(title, detail, material.getMaterialName()), 200));
        out.setOperator(staffService.currentStaffName());
        // 手工录入性质的出库，来源与手工登记一致；真正的「这单从哪来」看 ref_type / ref_id
        out.setSourceType("1");
        out.setRefType(refType);
        out.setRefId(refId);
        out.setCatId(catId);
        out.setRemark("台账自动出库（" + refLabel(refType) + "#" + refId + "）");
        stockRecordService.insertCatStockRecord(out);
    }

    /** 业务记录删掉 / 取消物资关联：把这条记录产生的出库按净值补齐 */
    public void clear(String refType, Long refId, Long catId)
    {
        rollback(refType, refId);
    }

    /**
     * 按净值补入库单。已经冲销过（净额 ≤ 0）的物资跳过，所以重复调用安全。
     */
    private void rollback(String refType, Long refId)
    {
        List<Map<String, Object>> nets = stockRecordMapper.selectNetByRef(refType, refId);
        if (nets == null || nets.isEmpty())
        {
            return;
        }
        for (Map<String, Object> net : nets)
        {
            Object materialId = net.get("materialId");
            Object netQty = net.get("netQty");
            if (materialId == null || netQty == null)
            {
                continue;
            }
            BigDecimal remain = new BigDecimal(String.valueOf(netQty));
            if (remain.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            CatStockRecord back = new CatStockRecord();
            back.setMaterialId(((Number) materialId).longValue());
            back.setRecordType("1");
            back.setQuantity(remain);
            back.setRecordDate(new Date());
            back.setPurpose(cut("台账冲销：" + refLabel(refType) + "#" + refId, 200));
            back.setOperator(staffService.currentStaffName());
            back.setSourceType("1");
            back.setRefType(refType);
            back.setRefId(refId);
            back.setRemark("原出库单已作废，库存回补");
            stockRecordService.insertCatStockRecord(back);
        }
    }

    /** 「用药消耗：阿莫西林（阿莫西林胶囊）」，用途里能直接看出消耗在哪 */
    private String buildPurpose(String title, String detail, String materialName)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.isEmpty(title) ? "台账" : title).append("消耗：");
        if (StringUtils.isNotEmpty(detail))
        {
            sb.append(detail);
        }
        if (StringUtils.isNotEmpty(materialName))
        {
            if (StringUtils.isNotEmpty(detail))
            {
                sb.append("（").append(materialName).append("）");
            }
            else
            {
                sb.append(materialName);
            }
        }
        return sb.toString();
    }

    private String refLabel(String refType)
    {
        if (REF_MEDICATION.equals(refType))
        {
            return "用药记录";
        }
        if (REF_VACCINE.equals(refType))
        {
            return "疫苗台账";
        }
        if (REF_DEWORM.equals(refType))
        {
            return "驱虫台账";
        }
        return "台账";
    }

    private String cut(String text, int max)
    {
        if (text == null)
        {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
