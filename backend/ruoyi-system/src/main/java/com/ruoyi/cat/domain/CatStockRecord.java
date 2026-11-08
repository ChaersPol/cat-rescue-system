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
    /** 猫所属基地 ID（筛选条件，非本表字段） */
    private Long catBaseId;

    /** 猫所属病房 ID（筛选条件，非本表字段） */
    private Long catWardId;

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
    /** 来源（1手动录入 2站内诊断开具） */
    @Excel(name = "来源", readConverterExp = "1=手动录入,2=站内诊断")
    private String sourceType;
    /** 来源诊断id */
    private Long diagnosisId;
    /** 关联猫咪id */
    private Long catId;
    /** 关联猫咪（名字+档案编号），导出展示用非表字段 */
    @Excel(name = "关联猫咪")
    private String catRefName;
    /** 关联采购单（日期+事由+金额），导出展示用非表字段 */
    @Excel(name = "关联采购单")
    private String expenseRefName;
    /**
     * 业务来源类型（1 用药记录 2 疫苗台账 3 驱虫台账）
     * <p>
     * 台账手工录入挂物资时，出库单上记这个，改数量或删除记录时才能精确冲销，
     * 不会误伤同一只猫同一天的其它出库。
     */
    private String refType;
    /** 业务主键（对应 cat_medication / cat_vaccine / cat_deworm 的主键） */
    private Long refId;
    public String getSourceType()
    {
        return sourceType;
    }
    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }
    public String getRefType()
    {
        return refType;
    }
    public void setRefType(String refType)
    {
        this.refType = refType;
    }
    public Long getRefId()
    {
        return refId;
    }
    public void setRefId(Long refId)
    {
        this.refId = refId;
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
    public String getCatRefName()
    {
        return catRefName;
    }
    public void setCatRefName(String catRefName)
    {
        this.catRefName = catRefName;
    }
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
    public Long getCatBaseId()
    {
        return catBaseId;
    }

    public void setCatBaseId(Long catBaseId)
    {
        this.catBaseId = catBaseId;
    }

    public Long getCatWardId()
    {
        return catWardId;
    }

    public void setCatWardId(Long catWardId)
    {
        this.catWardId = catWardId;
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
