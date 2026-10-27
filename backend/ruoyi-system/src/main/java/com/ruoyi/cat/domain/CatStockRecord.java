package com.ruoyi.cat.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 物资出入库记录表
 * 
 * @author ruoyi
 */
public class CatStockRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 记录id */
    private Long recordId;
    /** 物资id */
    private Long materialId;
    /** 类型（1入库 2出库） */
    private String recordType;
    /** 数量 */
    private BigDecimal quantity;
    /** 出入库日期 */
    private Date recordDate;
    /** 供应商 */
    private String supplier;
    /** 领用基地 */
    private String receiverBase;
    /** 领用人 */
    private String receiver;
    /** 用途 */
    private String purpose;
    /** 操作人 */
    private String operator;
    /** 关联采购财务单id */
    private Long expenseId;
    public Long getRecordId()
    {
        return recordId;
    }
    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }
    public Long getMaterialId()
    {
        return materialId;
    }
    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }
    public String getRecordType()
    {
        return recordType;
    }
    public void setRecordType(String recordType)
    {
        this.recordType = recordType;
    }
    public BigDecimal getQuantity()
    {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity)
    {
        this.quantity = quantity;
    }
    public Date getRecordDate()
    {
        return recordDate;
    }
    public void setRecordDate(Date recordDate)
    {
        this.recordDate = recordDate;
    }
    public String getSupplier()
    {
        return supplier;
    }
    public void setSupplier(String supplier)
    {
        this.supplier = supplier;
    }
    public String getReceiverBase()
    {
        return receiverBase;
    }
    public void setReceiverBase(String receiverBase)
    {
        this.receiverBase = receiverBase;
    }
    public String getReceiver()
    {
        return receiver;
    }
    public void setReceiver(String receiver)
    {
        this.receiver = receiver;
    }
    public String getPurpose()
    {
        return purpose;
    }
    public void setPurpose(String purpose)
    {
        this.purpose = purpose;
    }
    public String getOperator()
    {
        return operator;
    }
    public void setOperator(String operator)
    {
        this.operator = operator;
    }
    public Long getExpenseId()
    {
        return expenseId;
    }
    public void setExpenseId(Long expenseId)
    {
        this.expenseId = expenseId;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("materialId", getMaterialId())
            .append("recordType", getRecordType())
            .append("quantity", getQuantity())
            .append("recordDate", getRecordDate())
            .append("supplier", getSupplier())
            .append("receiverBase", getReceiverBase())
            .append("receiver", getReceiver())
            .append("purpose", getPurpose())
            .append("operator", getOperator())
            .append("expenseId", getExpenseId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
