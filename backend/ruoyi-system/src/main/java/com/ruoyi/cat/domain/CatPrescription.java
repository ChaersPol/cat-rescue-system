package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用药处方（医生开的用药方案）
 * <p>
 * 与「用药记录」的关系：处方是医生开的方案（几味药、几个时间段、连吃几天），
 * 保存时按「天 × 时段 × 药」展开成 cat_medication 明细，助理照着明细执行打卡。
 *
 * @author ruoyi
 */
public class CatPrescription extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 处方id */
    private Long prescriptionId;
    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），非表字段，展示用 */
    private String catRefName;
    /** 来源诊断id */
    private Long diagnosisId;
    /** 开方医生 */
    private String doctor;
    /** 开方医生账号id */
    private Long doctorUserId;
    /** 开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;
    /** 连续天数 */
    private Integer days;
    /** 状态（0进行中 1已停用） */
    private String status;
    /** 处方明细（非表字段） */
    private List<CatPrescriptionItem> items;
    /** 变更留痕（非表字段，详情页展示） */
    private List<CatPrescriptionLog> logs;

    public Long getPrescriptionId()
    {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId)
    {
        this.prescriptionId = prescriptionId;
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

    public Long getDiagnosisId()
    {
        return diagnosisId;
    }

    public void setDiagnosisId(Long diagnosisId)
    {
        this.diagnosisId = diagnosisId;
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

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Integer getDays()
    {
        return days;
    }

    public void setDays(Integer days)
    {
        this.days = days;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public List<CatPrescriptionItem> getItems()
    {
        return items;
    }

    public void setItems(List<CatPrescriptionItem> items)
    {
        this.items = items;
    }

    public List<CatPrescriptionLog> getLogs()
    {
        return logs;
    }

    public void setLogs(List<CatPrescriptionLog> logs)
    {
        this.logs = logs;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("prescriptionId", getPrescriptionId())
            .append("catId", getCatId())
            .append("diagnosisId", getDiagnosisId())
            .append("doctor", getDoctor())
            .append("startDate", getStartDate())
            .append("days", getDays())
            .append("status", getStatus())
            .append("remark", getRemark())
            .toString();
    }
}
