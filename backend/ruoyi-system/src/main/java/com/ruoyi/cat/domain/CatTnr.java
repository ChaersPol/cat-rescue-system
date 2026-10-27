package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * TNR抓捕登记表
 * 
 * @author ruoyi
 */
public class CatTnr extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** TNR记录id */
    private Long tnrId;
    /** 关联猫咪档案id */
    private Long catId;
    /** 抓捕时间 */
    private Date captureTime;
    /** 抓捕点位 */
    private String capturePoint;
    /** 猫咪信息 */
    private String catName;
    /** 是否剪耳标记 */
    private String earMarked;
    /** 绝育手术日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date surgeryDate;
    /** 手术医生 */
    private String surgeon;
    /** 手术记录 */
    private String surgeryRecord;
    /** 术前术后检查 */
    private String prePostCheck;
    /** 麻醉备注 */
    private String anesthesiaNote;
    /** 术后观察记录 */
    private String postObservation;
    /** 结果（0待定 1放归 2转入基地饲养） */
    private String result;
    public Long getTnrId()
    {
        return tnrId;
    }
    public void setTnrId(Long tnrId)
    {
        this.tnrId = tnrId;
    }
    public Long getCatId()
    {
        return catId;
    }
    public void setCatId(Long catId)
    {
        this.catId = catId;
    }
    public Date getCaptureTime()
    {
        return captureTime;
    }
    public void setCaptureTime(Date captureTime)
    {
        this.captureTime = captureTime;
    }
    public String getCapturePoint()
    {
        return capturePoint;
    }
    public void setCapturePoint(String capturePoint)
    {
        this.capturePoint = capturePoint;
    }
    public String getCatName()
    {
        return catName;
    }
    public void setCatName(String catName)
    {
        this.catName = catName;
    }
    public String getEarMarked()
    {
        return earMarked;
    }
    public void setEarMarked(String earMarked)
    {
        this.earMarked = earMarked;
    }
    public Date getSurgeryDate()
    {
        return surgeryDate;
    }
    public void setSurgeryDate(Date surgeryDate)
    {
        this.surgeryDate = surgeryDate;
    }
    public String getSurgeon()
    {
        return surgeon;
    }
    public void setSurgeon(String surgeon)
    {
        this.surgeon = surgeon;
    }
    public String getSurgeryRecord()
    {
        return surgeryRecord;
    }
    public void setSurgeryRecord(String surgeryRecord)
    {
        this.surgeryRecord = surgeryRecord;
    }
    public String getPrePostCheck()
    {
        return prePostCheck;
    }
    public void setPrePostCheck(String prePostCheck)
    {
        this.prePostCheck = prePostCheck;
    }
    public String getAnesthesiaNote()
    {
        return anesthesiaNote;
    }
    public void setAnesthesiaNote(String anesthesiaNote)
    {
        this.anesthesiaNote = anesthesiaNote;
    }
    public String getPostObservation()
    {
        return postObservation;
    }
    public void setPostObservation(String postObservation)
    {
        this.postObservation = postObservation;
    }
    public String getResult()
    {
        return result;
    }
    public void setResult(String result)
    {
        this.result = result;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("tnrId", getTnrId())
            .append("catId", getCatId())
            .append("captureTime", getCaptureTime())
            .append("capturePoint", getCapturePoint())
            .append("catName", getCatName())
            .append("earMarked", getEarMarked())
            .append("surgeryDate", getSurgeryDate())
            .append("surgeon", getSurgeon())
            .append("surgeryRecord", getSurgeryRecord())
            .append("prePostCheck", getPrePostCheck())
            .append("anesthesiaNote", getAnesthesiaNote())
            .append("postObservation", getPostObservation())
            .append("result", getResult())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
