package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
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
    private Long medicationId;
    /** 猫咪id */
    private Long catId;
    /** 用药日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date useDate;
    /** 时间段 */
    private String timePeriod;
    /** 药品名称 */
    private String drugName;
    /** 剂量 */
    private String dosage;
    /** 给药方式 */
    private String administration;
    /** 用药执行人 */
    private String executor;
    /** 不良反应记录 */
    private String adverseReaction;
    /** 照片 */
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
