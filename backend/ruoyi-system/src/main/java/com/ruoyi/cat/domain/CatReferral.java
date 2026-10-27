package com.ruoyi.cat.domain;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 外送医院转诊记录表
 * 
 * @author ruoyi
 */
public class CatReferral extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 转诊记录id */
    private Long referralId;
    /** 猫咪id */
    private Long catId;
    /** 送医时间 */
    private Date sendDate;
    /** 送医原因 */
    private String sendReason;
    /** 送往医院名称 */
    private String hospitalName;
    /** 医院检查结果 */
    private String hospitalCheck;
    /** 医院诊断 */
    private String hospitalDiagnosis;
    /** 医院处方 */
    private String hospitalPrescription;
    /** 病历化验单照片视频 */
    private String files;
    /** 医院处置结论 */
    private String result;
    /** 出院去向 */
    private String dischargeTo;
    /** 转入指定基地id */
    private Long targetBaseId;
    /** 出院医嘱、康复评估 */
    private String dischargeAdvice;
    /** 状态（0治疗中 1已出院 2已离世） */
    private String status;
    public Long getReferralId()
    {
        return referralId;
    }
    public void setReferralId(Long referralId)
    {
        this.referralId = referralId;
    }
    public Long getCatId()
    {
        return catId;
    }
    public void setCatId(Long catId)
    {
        this.catId = catId;
    }
    public Date getSendDate()
    {
        return sendDate;
    }
    public void setSendDate(Date sendDate)
    {
        this.sendDate = sendDate;
    }
    public String getSendReason()
    {
        return sendReason;
    }
    public void setSendReason(String sendReason)
    {
        this.sendReason = sendReason;
    }
    public String getHospitalName()
    {
        return hospitalName;
    }
    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }
    public String getHospitalCheck()
    {
        return hospitalCheck;
    }
    public void setHospitalCheck(String hospitalCheck)
    {
        this.hospitalCheck = hospitalCheck;
    }
    public String getHospitalDiagnosis()
    {
        return hospitalDiagnosis;
    }
    public void setHospitalDiagnosis(String hospitalDiagnosis)
    {
        this.hospitalDiagnosis = hospitalDiagnosis;
    }
    public String getHospitalPrescription()
    {
        return hospitalPrescription;
    }
    public void setHospitalPrescription(String hospitalPrescription)
    {
        this.hospitalPrescription = hospitalPrescription;
    }
    public String getFiles()
    {
        return files;
    }
    public void setFiles(String files)
    {
        this.files = files;
    }
    public String getResult()
    {
        return result;
    }
    public void setResult(String result)
    {
        this.result = result;
    }
    public String getDischargeTo()
    {
        return dischargeTo;
    }
    public void setDischargeTo(String dischargeTo)
    {
        this.dischargeTo = dischargeTo;
    }
    public Long getTargetBaseId()
    {
        return targetBaseId;
    }
    public void setTargetBaseId(Long targetBaseId)
    {
        this.targetBaseId = targetBaseId;
    }
    public String getDischargeAdvice()
    {
        return dischargeAdvice;
    }
    public void setDischargeAdvice(String dischargeAdvice)
    {
        this.dischargeAdvice = dischargeAdvice;
    }
    public String getStatus()
    {
        return status;
    }
    public void setStatus(String status)
    {
        this.status = status;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("referralId", getReferralId())
            .append("catId", getCatId())
            .append("sendDate", getSendDate())
            .append("sendReason", getSendReason())
            .append("hospitalName", getHospitalName())
            .append("hospitalCheck", getHospitalCheck())
            .append("hospitalDiagnosis", getHospitalDiagnosis())
            .append("hospitalPrescription", getHospitalPrescription())
            .append("files", getFiles())
            .append("result", getResult())
            .append("dischargeTo", getDischargeTo())
            .append("targetBaseId", getTargetBaseId())
            .append("dischargeAdvice", getDischargeAdvice())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
