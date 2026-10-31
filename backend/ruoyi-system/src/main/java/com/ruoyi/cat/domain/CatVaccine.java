package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 疫苗记录表
 * 
 * @author ruoyi
 */
public class CatVaccine extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 疫苗记录id */
    @Excel(name = "疫苗记录ID", cellType = ColumnType.NUMERIC)
    private Long vaccineId;
    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 疫苗类型 */
    @Excel(name = "疫苗类型", dictType = "cat_vaccine_type")
    private String vaccineType;
    /** 接种日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "接种日期", dateFormat = "yyyy-MM-dd")
    private Date vaccineDate;
    /** 下次接种时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "下次接种时间", dateFormat = "yyyy-MM-dd")
    private Date nextDate;
    /** 负责人 */
    @Excel(name = "负责人")
    private String responsible;
    /** 查询用：N 天内到期（含已过期），非表字段，对应「待疫苗」看板口径 */
    private Integer dueDays;
    public Long getVaccineId()
    {
        return vaccineId;
    }
    public void setVaccineId(Long vaccineId)
    {
        this.vaccineId = vaccineId;
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
    public String getVaccineType()
    {
        return vaccineType;
    }
    public void setVaccineType(String vaccineType)
    {
        this.vaccineType = vaccineType;
    }
    public Date getVaccineDate()
    {
        return vaccineDate;
    }
    public void setVaccineDate(Date vaccineDate)
    {
        this.vaccineDate = vaccineDate;
    }
    public Date getNextDate()
    {
        return nextDate;
    }
    public void setNextDate(Date nextDate)
    {
        this.nextDate = nextDate;
    }
    public String getResponsible()
    {
        return responsible;
    }
    public void setResponsible(String responsible)
    {
        this.responsible = responsible;
    }
    public Integer getDueDays()
    {
        return dueDays;
    }
    public void setDueDays(Integer dueDays)
    {
        this.dueDays = dueDays;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("vaccineId", getVaccineId())
            .append("catId", getCatId())
            .append("vaccineType", getVaccineType())
            .append("vaccineDate", getVaccineDate())
            .append("nextDate", getNextDate())
            .append("responsible", getResponsible())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
