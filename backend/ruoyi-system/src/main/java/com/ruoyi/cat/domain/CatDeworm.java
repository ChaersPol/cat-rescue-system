package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 驱虫记录表
 * 
 * @author ruoyi
 */
public class CatDeworm extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 驱虫记录id */
    private Long dewormId;
    /** 猫咪id */
    private Long catId;
    /** 驱虫类型 */
    private String dewormType;
    /** 驱虫药 */
    private String dewormDrug;
    /** 驱虫日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date dewormDate;
    /** 下次驱虫时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date nextDate;
    public Long getDewormId()
    {
        return dewormId;
    }
    public void setDewormId(Long dewormId)
    {
        this.dewormId = dewormId;
    }
    public Long getCatId()
    {
        return catId;
    }
    public void setCatId(Long catId)
    {
        this.catId = catId;
    }
    public String getDewormType()
    {
        return dewormType;
    }
    public void setDewormType(String dewormType)
    {
        this.dewormType = dewormType;
    }
    public String getDewormDrug()
    {
        return dewormDrug;
    }
    public void setDewormDrug(String dewormDrug)
    {
        this.dewormDrug = dewormDrug;
    }
    public Date getDewormDate()
    {
        return dewormDate;
    }
    public void setDewormDate(Date dewormDate)
    {
        this.dewormDate = dewormDate;
    }
    public Date getNextDate()
    {
        return nextDate;
    }
    public void setNextDate(Date nextDate)
    {
        this.nextDate = nextDate;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("dewormId", getDewormId())
            .append("catId", getCatId())
            .append("dewormType", getDewormType())
            .append("dewormDrug", getDewormDrug())
            .append("dewormDate", getDewormDate())
            .append("nextDate", getNextDate())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
