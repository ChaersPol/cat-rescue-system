package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 财务支出表
 * 
 * @author ruoyi
 */
public class CatExpense extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 支出id */
    private Long expenseId;
    /** 费用分类id */
    private Long categoryId;
    /** 费用类型 */
    private String expenseType;
    /** 支出日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expenseDate;
    /** 供应商 */
    private String supplier;
    /** 采购物资清单 */
    private String items;
    /** 总金额 */
    private BigDecimal amount;
    /** 付款方式 */
    private String payMethod;
    /** 付款截图/票据 */
    private String receiptUrl;
    /** 归属基地id */
    private Long baseId;
    /** 关联猫咪id */
    private Long catId;
    /** 关联转诊记录id */
    private Long referralId;
    /** 关联入库单id */
    private Long stockId;
    public Long getExpenseId()
    {
        return expenseId;
    }
    public void setExpenseId(Long expenseId)
    {
        this.expenseId = expenseId;
    }
    public Long getCategoryId()
    {
        return categoryId;
    }
    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }
    public String getExpenseType()
    {
        return expenseType;
    }
    public void setExpenseType(String expenseType)
    {
        this.expenseType = expenseType;
    }
    public Date getExpenseDate()
    {
        return expenseDate;
    }
    public void setExpenseDate(Date expenseDate)
    {
        this.expenseDate = expenseDate;
    }
    public String getSupplier()
    {
        return supplier;
    }
    public void setSupplier(String supplier)
    {
        this.supplier = supplier;
    }
    public String getItems()
    {
        return items;
    }
    public void setItems(String items)
    {
        this.items = items;
    }
    public BigDecimal getAmount()
    {
        return amount;
    }
    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }
    public String getPayMethod()
    {
        return payMethod;
    }
    public void setPayMethod(String payMethod)
    {
        this.payMethod = payMethod;
    }
    public String getReceiptUrl()
    {
        return receiptUrl;
    }
    public void setReceiptUrl(String receiptUrl)
    {
        this.receiptUrl = receiptUrl;
    }
    public Long getBaseId()
    {
        return baseId;
    }
    public void setBaseId(Long baseId)
    {
        this.baseId = baseId;
    }
    public Long getCatId()
    {
        return catId;
    }
    public void setCatId(Long catId)
    {
        this.catId = catId;
    }
    public Long getReferralId()
    {
        return referralId;
    }
    public void setReferralId(Long referralId)
    {
        this.referralId = referralId;
    }
    public Long getStockId()
    {
        return stockId;
    }
    public void setStockId(Long stockId)
    {
        this.stockId = stockId;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("expenseId", getExpenseId())
            .append("categoryId", getCategoryId())
            .append("expenseType", getExpenseType())
            .append("expenseDate", getExpenseDate())
            .append("supplier", getSupplier())
            .append("items", getItems())
            .append("amount", getAmount())
            .append("payMethod", getPayMethod())
            .append("receiptUrl", getReceiptUrl())
            .append("baseId", getBaseId())
            .append("catId", getCatId())
            .append("referralId", getReferralId())
            .append("stockId", getStockId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
