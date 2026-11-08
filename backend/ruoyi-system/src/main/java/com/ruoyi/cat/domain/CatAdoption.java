package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 领养登记表
 * 
 * @author ruoyi
 */
public class CatAdoption extends BaseEntity
{
    /** 猫所属基地 ID（筛选条件，非本表字段） */
    private Long catBaseId;

    /** 猫所属病房 ID（筛选条件，非本表字段） */
    private Long catWardId;

    private static final long serialVersionUID = 1L;
    /** 领养记录id */
    @Excel(name = "领养记录ID", cellType = ColumnType.NUMERIC)
    private Long adoptionId;
    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 领养人姓名 */
    @Excel(name = "领养人姓名")
    private String adopterName;
    /** 领养人电话 */
    @Excel(name = "领养人电话")
    private String adopterPhone;
    /** 领养人地址 */
    @Excel(name = "领养人地址")
    private String adopterAddress;
    /** 领养日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "领养日期", dateFormat = "yyyy-MM-dd")
    private Date adoptionDate;
    /** 领养协议上传地址 */
    @Excel(name = "领养协议")
    private String agreementUrl;
    /** 回访是否已结束（0进行中 1已结束） */
    @Excel(name = "回访是否已结束", readConverterExp = "0=进行中,1=已结束")
    private String visitClosed;
    /** 回访结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "回访结束时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date visitClosedTime;
    /** 回访结束操作人 */
    @Excel(name = "回访结束操作人")
    private String visitClosedBy;
    /** 回访计划总数（列表统计，非表字段） */
    @Excel(name = "回访计划数")
    private Integer visitTotal;
    /** 已回访数（列表统计，非表字段） */
    @Excel(name = "已回访数")
    private Integer visitDone;
    /** 待回访数（列表统计，非表字段） */
    @Excel(name = "待回访数")
    private Integer visitPending;
    public Long getAdoptionId()
    {
        return adoptionId;
    }
    public void setAdoptionId(Long adoptionId)
    {
        this.adoptionId = adoptionId;
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
    public String getAdopterName()
    {
        return adopterName;
    }
    public void setAdopterName(String adopterName)
    {
        this.adopterName = adopterName;
    }
    public String getAdopterPhone()
    {
        return adopterPhone;
    }
    public void setAdopterPhone(String adopterPhone)
    {
        this.adopterPhone = adopterPhone;
    }
    public String getAdopterAddress()
    {
        return adopterAddress;
    }
    public void setAdopterAddress(String adopterAddress)
    {
        this.adopterAddress = adopterAddress;
    }
    public Date getAdoptionDate()
    {
        return adoptionDate;
    }
    public void setAdoptionDate(Date adoptionDate)
    {
        this.adoptionDate = adoptionDate;
    }
    public String getAgreementUrl()
    {
        return agreementUrl;
    }
    public void setAgreementUrl(String agreementUrl)
    {
        this.agreementUrl = agreementUrl;
    }
    public String getVisitClosed()
    {
        return visitClosed;
    }
    public void setVisitClosed(String visitClosed)
    {
        this.visitClosed = visitClosed;
    }
    public Date getVisitClosedTime()
    {
        return visitClosedTime;
    }
    public void setVisitClosedTime(Date visitClosedTime)
    {
        this.visitClosedTime = visitClosedTime;
    }
    public String getVisitClosedBy()
    {
        return visitClosedBy;
    }
    public void setVisitClosedBy(String visitClosedBy)
    {
        this.visitClosedBy = visitClosedBy;
    }
    public Integer getVisitTotal()
    {
        return visitTotal;
    }
    public void setVisitTotal(Integer visitTotal)
    {
        this.visitTotal = visitTotal;
    }
    public Integer getVisitDone()
    {
        return visitDone;
    }
    public void setVisitDone(Integer visitDone)
    {
        this.visitDone = visitDone;
    }
    public Integer getVisitPending()
    {
        return visitPending;
    }
    public void setVisitPending(Integer visitPending)
    {
        this.visitPending = visitPending;
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
            .append("adoptionId", getAdoptionId())
            .append("catId", getCatId())
            .append("adopterName", getAdopterName())
            .append("adopterPhone", getAdopterPhone())
            .append("adopterAddress", getAdopterAddress())
            .append("adoptionDate", getAdoptionDate())
            .append("agreementUrl", getAgreementUrl())
            .append("visitClosed", getVisitClosed())
            .append("visitClosedTime", getVisitClosedTime())
            .append("visitClosedBy", getVisitClosedBy())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
