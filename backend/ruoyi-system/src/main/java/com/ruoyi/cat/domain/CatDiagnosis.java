package com.ruoyi.cat.domain;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
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
    @Excel(name = "诊断记录ID", cellType = ColumnType.NUMERIC)
    private Long diagnosisId;
    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 当前治疗基地id */
    private Long baseId;
    /** 治疗基地（名称+类型），导出展示用非表字段 */
    @Excel(name = "治疗基地")
    private String baseRefName;
    /** 来源基地id（就诊时猫咪归属基地快照，用于追溯「猫是从哪个基地送来的」） */
    private Long sourceBaseId;
    /** 来源基地名称，导出展示用非表字段 */
    @Excel(name = "来源基地")
    private String sourceBaseRefName;
    /** 所在病房id（就诊时所在病房快照） */
    private Long wardId;
    /** 所在病房名称，导出展示用非表字段 */
    @Excel(name = "所在病房")
    private String wardRefName;
    /** 只看「未填病房」的记录（非表字段，病房筛选下拉里的伪选项 wardId=0 转成它） */
    private Boolean wardUnassigned;
    /** 就诊日期 */
    @Excel(name = "就诊日期", dateFormat = "yyyy-MM-dd")
    private Date visitDate;
    /** 就诊类型（1初诊 2复诊） */
    @Excel(name = "就诊类型", readConverterExp = "1=初诊,2=复诊")
    private String visitType;
    /** 操作医生（姓名快照，取接诊医生账号的昵称；改名后历史记录仍保留当时的名字） */
    @Excel(name = "操作医生")
    private String doctor;
    /** 接诊医生账号ID（sys_user.user_id）；历史手填记录可能为空 */
    private Long doctorUserId;
    /** 主诉/症状 */
    @Excel(name = "主诉/症状")
    private String symptoms;
    /** 初步诊断结果 */
    @Excel(name = "初步诊断结果")
    private String diagnosisResult;
    /** 检查项目 */
    @Excel(name = "检查项目")
    private String checkItems;
    /** 医嘱、预后判断 */
    @Excel(name = "医嘱/预后")
    private String medicalAdvice;
    /** 检查照片 */
    @Excel(name = "检查照片")
    private String checkPhotos;
    /** 更正备注（原始记录不可修改，错误只能追加更正说明） */
    @Excel(name = "更正备注")
    private String correctionNote;
    /** 更正人 */
    @Excel(name = "更正人")
    private String correctionBy;
    /** 更正时间 */
    @Excel(name = "更正时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date correctionTime;
    /** 是否已更正（非表字段，供列表标红提示） */
    private Boolean corrected;
    /** 本次开具的检查项目/用药明细（非表字段，前端二级弹窗选择后提交） */
    private java.util.List<CatDiagnosisItem> items;
    /**
     * 本次诊断开的用药处方（非表字段）
     * <p>
     * 医生诊断完开药：填「几味药 / 几个时间段 / 连吃几天」，保存后自动展开成
     * 每一天、每个时间段、每味药的用药记录，助理照着打卡。
     */
    private CatPrescription prescription;
    public CatPrescription getPrescription()
    {
        return prescription;
    }
    public void setPrescription(CatPrescription prescription)
    {
        this.prescription = prescription;
    }
    public java.util.List<CatDiagnosisItem> getItems()
    {
        return items;
    }
    public void setItems(java.util.List<CatDiagnosisItem> items)
    {
        this.items = items;
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
    public Long getSourceBaseId()
    {
        return sourceBaseId;
    }
    public void setSourceBaseId(Long sourceBaseId)
    {
        this.sourceBaseId = sourceBaseId;
    }
    public String getSourceBaseRefName()
    {
        return sourceBaseRefName;
    }
    public void setSourceBaseRefName(String sourceBaseRefName)
    {
        this.sourceBaseRefName = sourceBaseRefName;
    }
    public Long getWardId()
    {
        return wardId;
    }
    public void setWardId(Long wardId)
    {
        this.wardId = wardId;
    }
    public String getWardRefName()
    {
        return wardRefName;
    }
    public void setWardRefName(String wardRefName)
    {
        this.wardRefName = wardRefName;
    }
    public Boolean getWardUnassigned()
    {
        return wardUnassigned;
    }
    public void setWardUnassigned(Boolean wardUnassigned)
    {
        this.wardUnassigned = wardUnassigned;
    }
    public String getVisitType()
    {
        return visitType;
    }
    public void setVisitType(String visitType)
    {
        this.visitType = visitType;
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
    public Long getDoctorUserId()
    {
        return doctorUserId;
    }
    public void setDoctorUserId(Long doctorUserId)
    {
        this.doctorUserId = doctorUserId;
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
            .append("sourceBaseId", getSourceBaseId())
            .append("wardId", getWardId())
            .append("visitDate", getVisitDate())
            .append("visitType", getVisitType())
            .append("doctor", getDoctor())
            .append("doctorUserId", getDoctorUserId())
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
