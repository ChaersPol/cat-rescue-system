package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 每日用药记录表
 * 
 * @author ruoyi
 */
public class CatMedication extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 用药记录id */
    @Excel(name = "用药记录ID", cellType = ColumnType.NUMERIC)
    private Long medicationId;
    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 用药日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "用药日期", dateFormat = "yyyy-MM-dd")
    private Date useDate;
    /** 时间段 */
    @Excel(name = "时间段", dictType = "cat_time_period")
    private String timePeriod;
    /** 药品名称 */
    @Excel(name = "药品名称")
    private String drugName;
    /** 剂量 */
    @Excel(name = "剂量")
    private String dosage;
    /** 给药方式 */
    @Excel(name = "给药方式", dictType = "cat_administration")
    private String administration;
    /** 用药执行人 */
    @Excel(name = "用药执行人")
    private String executor;
    /** 不良反应记录 */
    @Excel(name = "不良反应")
    private String adverseReaction;
    /** 照片 */
    @Excel(name = "照片")
    private String photos;
    public Long getMedicationId()
    {
        return medicationId;
    }
    public void setMedicationId(Long medicationId)
    {
        this.medicationId = medicationId;
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
    public Date getUseDate()
    {
        return useDate;
    }
    public void setUseDate(Date useDate)
    {
        this.useDate = useDate;
    }
    public String getTimePeriod()
    {
        return timePeriod;
    }
    public void setTimePeriod(String timePeriod)
    {
        this.timePeriod = timePeriod;
    }
    public String getDrugName()
    {
        return drugName;
    }
    public void setDrugName(String drugName)
    {
        this.drugName = drugName;
    }
    public String getDosage()
    {
        return dosage;
    }
    public void setDosage(String dosage)
    {
        this.dosage = dosage;
    }
    public String getAdministration()
    {
        return administration;
    }
    public void setAdministration(String administration)
    {
        this.administration = administration;
    }
    public String getExecutor()
    {
        return executor;
    }
    public void setExecutor(String executor)
    {
        this.executor = executor;
    }
    public String getAdverseReaction()
    {
        return adverseReaction;
    }
    public void setAdverseReaction(String adverseReaction)
    {
        this.adverseReaction = adverseReaction;
    }
    public String getPhotos()
    {
        return photos;
    }
    public void setPhotos(String photos)
    {
        this.photos = photos;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("medicationId", getMedicationId())
            .append("catId", getCatId())
            .append("useDate", getUseDate())
            .append("timePeriod", getTimePeriod())
            .append("drugName", getDrugName())
            .append("dosage", getDosage())
            .append("administration", getAdministration())
            .append("executor", getExecutor())
            .append("adverseReaction", getAdverseReaction())
            .append("photos", getPhotos())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
