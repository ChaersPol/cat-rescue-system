package com.ruoyi.cat.domain;
import java.util.Date;
import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
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
    @Excel(name = "记录ID", cellType = ColumnType.NUMERIC)
    private Long recordId;
    /** 物资id */
    private Long materialId;
    /** 物资（名称+规格+单位），导出展示用非表字段 */
    @Excel(name = "物资")
    private String materialRefName;
    /** 类型（1入库 2出库） */
    @Excel(name = "类型", dictType = "cat_stock_type")
    private String recordType;
    /** 数量 */
    @Excel(name = "数量", cellType = ColumnType.NUMERIC)
    private BigDecimal quantity;
    /** 出入库日期 */
    @Excel(name = "出入库日期", dateFormat = "yyyy-MM-dd")
    private Date recordDate;
    /** 供应商 */
    @Excel(name = "供应商")
    private String supplier;
    /** 领用基地 */
    @Excel(name = "领用基地")
    private String receiverBase;
    /** 领用人 */
    @Excel(name = "领用人")
    private String receiver;
    /** 用途 */
    @Excel(name = "用途")
    private String purpose;
    /** 操作人 */
    @Excel(name = "操作人")
    private String operator;
    /** 关联采购财务单id */
    private Long expenseId;
    /** 关联采购单（日期+事由+金额），导出展示用非表字段 */
    @Excel(name = "关联采购单")
    private String expenseRefName;
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
    public String getMaterialRefName()
    {
        return materialRefName;
    }
    public void setMaterialRefName(String materialRefName)
    {
        this.materialRefName = materialRefName;
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
    public String getExpenseRefName()
    {
        return expenseRefName;
    }
    public void setExpenseRefName(String expenseRefName)
    {
        this.expenseRefName = expenseRefName;
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
