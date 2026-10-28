package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
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
    @Excel(name = "支出ID", cellType = ColumnType.NUMERIC)
    private Long expenseId;
    /** 费用分类id */
    private Long categoryId;
    /** 费用分类（名称+费用类型），导出展示用非表字段 */
    @Excel(name = "费用分类")
    private String feeCategoryRefName;
    /** 费用类型 */
    @Excel(name = "费用类型", dictType = "cat_expense_type")
    private String expenseType;
    /** 支出日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "支出日期", dateFormat = "yyyy-MM-dd")
    private Date expenseDate;
    /** 供应商 */
    @Excel(name = "供应商")
    private String supplier;
    /** 采购物资清单 */
    @Excel(name = "采购物资清单")
    private String items;
    /** 总金额 */
    @Excel(name = "总金额", cellType = ColumnType.NUMERIC, scale = 2)
    private BigDecimal amount;
    /** 付款方式 */
    @Excel(name = "付款方式")
    private String payMethod;
    /** 付款截图/票据 */
    @Excel(name = "付款票据")
    private String receiptUrl;
    /** 归属基地id */
    private Long baseId;
    /** 归属基地（名称+类型），导出展示用非表字段 */
    @Excel(name = "归属基地")
    private String baseRefName;
    /** 关联猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 关联转诊记录id */
    private Long referralId;
    /** 关联转诊（医院+送医日期），导出展示用非表字段 */
    @Excel(name = "关联转诊")
    private String referralRefName;
    /** 关联入库单id */
    private Long stockId;
    /** 关联入库单（日期+类型+用途），导出展示用非表字段 */
    @Excel(name = "关联入库单")
    private String stockRefName;
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
    public String getFeeCategoryRefName()
    {
        return feeCategoryRefName;
    }
    public void setFeeCategoryRefName(String feeCategoryRefName)
    {
        this.feeCategoryRefName = feeCategoryRefName;
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
    public String getBaseRefName()
    {
        return baseRefName;
    }
    public void setBaseRefName(String baseRefName)
    {
        this.baseRefName = baseRefName;
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
    public Long getReferralId()
    {
        return referralId;
    }
    public void setReferralId(Long referralId)
    {
        this.referralId = referralId;
    }
    public String getReferralRefName()
    {
        return referralRefName;
    }
    public void setReferralRefName(String referralRefName)
    {
        this.referralRefName = referralRefName;
    }
    public Long getStockId()
    {
        return stockId;
    }
    public void setStockId(Long stockId)
    {
        this.stockId = stockId;
    }
    public String getStockRefName()
    {
        return stockRefName;
    }
    public void setStockRefName(String stockRefName)
    {
        this.stockRefName = stockRefName;
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
