package com.ruoyi.cat.domain;
import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
/**
 * 站内诊断明细（检查项目 / 用药 / 疫苗 / 驱虫）
 *
 * @author ruoyi
 */
public class CatDiagnosisItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细id */
    private Long detailId;

    /** 诊断记录id */
    private Long diagnosisId;

    /** 猫咪id */
    private Long catId;

    /** 目录项id */
    private Long itemId;

    /** 名称快照 */
    @Excel(name = "项目/药品")
    private String itemName;

    /** 类型（1检查项目 2用药 3疫苗 4驱虫） */
    private String itemType;

    /** 关联物资id */
    private Long materialId;

    /** 数量 */
    @Excel(name = "数量")
    private BigDecimal quantity;

    /** 单位 */
    private String unit;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal price;

    /** 金额 */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 用法用量/说明 */
    @Excel(name = "用法/说明")
    private String usageNote;

    public Long getDetailId()
    {
        return detailId;
    }

    public void setDetailId(Long detailId)
    {
        this.detailId = detailId;
    }

    public Long getDiagnosisId()
    {
        return diagnosisId;
    }

    public void setDiagnosisId(Long diagnosisId)
    {
        this.diagnosisId = diagnosisId;
    }

    public Long getCatId()
    {
        return catId;
    }

    public void setCatId(Long catId)
    {
        this.catId = catId;
    }

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public String getItemName()
    {
        return itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public String getItemType()
    {
        return itemType;
    }

    public void setItemType(String itemType)
    {
        this.itemType = itemType;
    }

    public Long getMaterialId()
    {
        return materialId;
    }

    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }

    public BigDecimal getQuantity()
    {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity)
    {
        this.quantity = quantity;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getUsageNote()
    {
        return usageNote;
    }

    public void setUsageNote(String usageNote)
    {
        this.usageNote = usageNote;
    }
}
