package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
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
    @Excel(name = "TNR记录ID", cellType = ColumnType.NUMERIC)
    private Long tnrId;
    /** 关联猫咪档案id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 抓捕时间 */
    @Excel(name = "抓捕时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date captureTime;
    /** 抓捕点位 */
    @Excel(name = "抓捕点位")
    private String capturePoint;
    /** 猫咪信息（未建档时现场登记的名字） */
    @Excel(name = "现场登记名")
    private String catName;
    /** 是否剪耳标记 */
    @Excel(name = "是否剪耳标记", dictType = "cat_yes_no")
    private String earMarked;
    /** 绝育手术日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "绝育手术日期", dateFormat = "yyyy-MM-dd")
    private Date surgeryDate;
    /** 手术医生 */
    @Excel(name = "手术医生")
    private String surgeon;
    /** 手术记录 */
    @Excel(name = "手术记录")
    private String surgeryRecord;
    /** 术前术后检查 */
    @Excel(name = "术前术后检查")
    private String prePostCheck;
    /** 麻醉备注 */
    @Excel(name = "麻醉备注")
    private String anesthesiaNote;
    /** 术后观察记录 */
    @Excel(name = "术后观察记录")
    private String postObservation;
    /** 结果（0待定 1放归 2转入基地饲养） */
    @Excel(name = "结果", dictType = "cat_tnr_result")
    private String result;
    /** 查询用：绝育状态（0 待绝育=未登记手术日期，1 已绝育），非表字段 */
    private String neuterStatus;
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
    public String getCatRefName()
    {
        return catRefName;
    }
    public void setCatRefName(String catRefName)
    {
        this.catRefName = catRefName;
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
    public String getNeuterStatus()
    {
        return neuterStatus;
    }
    public void setNeuterStatus(String neuterStatus)
    {
        this.neuterStatus = neuterStatus;
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
