package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 回访记录表
 * 
 * @author ruoyi
 */
public class CatVisit extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 回访记录id */
    private Long visitId;
    /** 领养记录id */
    private Long adoptionId;
    /** 猫咪id */
    private Long catId;
    /** 回访计划 */
    private String planType;
    /** 计划回访日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planDate;
    /** 回访状态 */
    private String visitStatus;
    /** 回访内容 */
    private String visitContent;
    /** 回访照片 */
    private String photos;
    /** 回访人 */
    private String visitBy;
    /** 实际回访时间 */
    private Date visitTime;
    public Long getVisitId()
    {
        return visitId;
    }
    public void setVisitId(Long visitId)
    {
        this.visitId = visitId;
    }
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
    public String getPlanType()
    {
        return planType;
    }
    public void setPlanType(String planType)
    {
        this.planType = planType;
    }
    public Date getPlanDate()
    {
        return planDate;
    }
    public void setPlanDate(Date planDate)
    {
        this.planDate = planDate;
    }
    public String getVisitStatus()
    {
        return visitStatus;
    }
    public void setVisitStatus(String visitStatus)
    {
        this.visitStatus = visitStatus;
    }
    public String getVisitContent()
    {
        return visitContent;
    }
    public void setVisitContent(String visitContent)
    {
        this.visitContent = visitContent;
    }
    public String getPhotos()
    {
        return photos;
    }
    public void setPhotos(String photos)
    {
        this.photos = photos;
    }
    public String getVisitBy()
    {
        return visitBy;
    }
    public void setVisitBy(String visitBy)
    {
        this.visitBy = visitBy;
    }
    public Date getVisitTime()
    {
        return visitTime;
    }
    public void setVisitTime(Date visitTime)
    {
        this.visitTime = visitTime;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("visitId", getVisitId())
            .append("adoptionId", getAdoptionId())
            .append("catId", getCatId())
            .append("planType", getPlanType())
            .append("planDate", getPlanDate())
            .append("visitStatus", getVisitStatus())
            .append("visitContent", getVisitContent())
            .append("photos", getPhotos())
            .append("visitBy", getVisitBy())
            .append("visitTime", getVisitTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
