package com.ruoyi.cat.domain;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 站内诊断记录表
 * 
 * @author ruoyi
 */
public class CatDiagnosis extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 诊断记录id */
    private Long diagnosisId;
    /** 猫咪id */
    private Long catId;
    /** 当前治疗基地id */
    private Long baseId;
    /** 就诊日期 */
    private Date visitDate;
    /** 操作医生 */
    private String doctor;
    /** 主诉/症状 */
    private String symptoms;
    /** 初步诊断结果 */
    private String diagnosisResult;
    /** 检查项目 */
    private String checkItems;
    /** 医嘱、预后判断 */
    private String medicalAdvice;
    /** 检查照片 */
    private String checkPhotos;
    /** 更正备注（原始记录不可修改，错误只能追加更正说明） */
    private String correctionNote;
    /** 更正人 */
    private String correctionBy;
    /** 更正时间 */
    private Date correctionTime;
    /** 是否已更正（非表字段，供列表标红提示） */
    private Boolean corrected;
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
    public Long getBaseId()
    {
        return baseId;
    }
    public void setBaseId(Long baseId)
    {
        this.baseId = baseId;
    }
    public Date getVisitDate()
    {
        return visitDate;
    }
    public void setVisitDate(Date visitDate)
    {
        this.visitDate = visitDate;
    }
    public String getDoctor()
    {
        return doctor;
    }
    public void setDoctor(String doctor)
    {
        this.doctor = doctor;
    }
    public String getSymptoms()
    {
        return symptoms;
    }
    public void setSymptoms(String symptoms)
    {
        this.symptoms = symptoms;
    }
    public String getDiagnosisResult()
    {
        return diagnosisResult;
    }
    public void setDiagnosisResult(String diagnosisResult)
    {
        this.diagnosisResult = diagnosisResult;
    }
    public String getCheckItems()
    {
        return checkItems;
    }
    public void setCheckItems(String checkItems)
    {
        this.checkItems = checkItems;
    }
    public String getMedicalAdvice()
    {
        return medicalAdvice;
    }
    public void setMedicalAdvice(String medicalAdvice)
    {
        this.medicalAdvice = medicalAdvice;
    }
    public String getCheckPhotos()
    {
        return checkPhotos;
    }
    public void setCheckPhotos(String checkPhotos)
    {
        this.checkPhotos = checkPhotos;
    }
    public String getCorrectionNote()
    {
        return correctionNote;
    }
    public void setCorrectionNote(String correctionNote)
    {
        this.correctionNote = correctionNote;
    }
    public String getCorrectionBy()
    {
        return correctionBy;
    }
    public void setCorrectionBy(String correctionBy)
    {
        this.correctionBy = correctionBy;
    }
    public Date getCorrectionTime()
    {
        return correctionTime;
    }
    public void setCorrectionTime(Date correctionTime)
    {
        this.correctionTime = correctionTime;
    }
    public Boolean getCorrected()
    {
        return corrected;
    }
    public void setCorrected(Boolean corrected)
    {
        this.corrected = corrected;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("diagnosisId", getDiagnosisId())
            .append("catId", getCatId())
            .append("baseId", getBaseId())
            .append("visitDate", getVisitDate())
            .append("doctor", getDoctor())
            .append("symptoms", getSymptoms())
            .append("diagnosisResult", getDiagnosisResult())
            .append("checkItems", getCheckItems())
            .append("medicalAdvice", getMedicalAdvice())
            .append("checkPhotos", getCheckPhotos())
            .append("correctionNote", getCorrectionNote())
            .append("correctionBy", getCorrectionBy())
            .append("correctionTime", getCorrectionTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
