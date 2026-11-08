package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用药记录「合并视图」一行：同一只猫 + 同一天 + 同一时间段
 * <p>
 * 明细视图里一个药一行（统计药品消耗要按条算），合并视图里一次喂药一行：
 * 「禾牙仔（CAT2026…）｜2026-10-20｜早｜3 种药｜助理小李｜已完成」。
 * 展开这一行才看每味药的名称、剂量、给药方式和照片。
 *
 * @author ruoyi
 */
public class CatMedicationGroup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），非表字段 */
    private String catRefName;
    /** 用药日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date useDate;
    /** 时间段 */
    private String timePeriod;
    /** 该组药品数 */
    private Integer drugCount;
    /** 该组药品名称（顿号拼接，列表里给个概览） */
    private String drugNames;
    /** 来源处方id（组内任一条带处方即算） */
    private Long prescriptionId;
    /** 执行状态（0未完成 1已完成），同时兼作筛选条件 */
    private String execStatus;
    /** 执行人 */
    private String executor;
    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date execTime;
    /** 用药照片 */
    private String photos;
    /** 照片上传时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date photoTime;
    /** 不良反应 */
    private String adverseReaction;
    /** 执行备注 */
    private String execRemark;
    /** 最近一次修改时间（撤回、补照片都会刷新） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date execUpdateTime;

    /** 筛选：猫当前所在基地（非本表字段） */
    private Long catBaseId;
    /** 筛选：猫当前所在病房（非本表字段） */
    private Long catWardId;
    /** 筛选：助理负责人（按病房授权 cat_ward_auth 取猫，非本表字段） */
    private Long assistantUserId;
    /** 筛选：来源（1手动录入 2站内诊断/处方） */
    private String sourceType;
    /** 筛选：只看某天（yyyy-MM-dd），手机端「今日待打卡」用 */
    private String useDateStr;

    /** 展开明细（非表字段，点开这一行才查） */
    private List<CatMedication> details;

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

    public Integer getDrugCount()
    {
        return drugCount;
    }

    public void setDrugCount(Integer drugCount)
    {
        this.drugCount = drugCount;
    }

    public String getDrugNames()
    {
        return drugNames;
    }

    public void setDrugNames(String drugNames)
    {
        this.drugNames = drugNames;
    }

    public Long getPrescriptionId()
    {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId)
    {
        this.prescriptionId = prescriptionId;
    }

    public String getExecStatus()
    {
        return execStatus;
    }

    public void setExecStatus(String execStatus)
    {
        this.execStatus = execStatus;
    }

    public String getExecutor()
    {
        return executor;
    }

    public void setExecutor(String executor)
    {
        this.executor = executor;
    }

    public Date getExecTime()
    {
        return execTime;
    }

    public void setExecTime(Date execTime)
    {
        this.execTime = execTime;
    }

    public String getPhotos()
    {
        return photos;
    }

    public void setPhotos(String photos)
    {
        this.photos = photos;
    }

    public Date getPhotoTime()
    {
        return photoTime;
    }

    public void setPhotoTime(Date photoTime)
    {
        this.photoTime = photoTime;
    }

    public String getAdverseReaction()
    {
        return adverseReaction;
    }

    public void setAdverseReaction(String adverseReaction)
    {
        this.adverseReaction = adverseReaction;
    }

    public String getExecRemark()
    {
        return execRemark;
    }

    public void setExecRemark(String execRemark)
    {
        this.execRemark = execRemark;
    }

    public Date getExecUpdateTime()
    {
        return execUpdateTime;
    }

    public void setExecUpdateTime(Date execUpdateTime)
    {
        this.execUpdateTime = execUpdateTime;
    }

    public Long getCatBaseId()
    {
        return catBaseId;
    }

    public void setCatBaseId(Long catBaseId)
    {
        this.catBaseId = catBaseId;
    }

    public Long getCatWardId()
    {
        return catWardId;
    }

    public void setCatWardId(Long catWardId)
    {
        this.catWardId = catWardId;
    }

    public Long getAssistantUserId()
    {
        return assistantUserId;
    }

    public void setAssistantUserId(Long assistantUserId)
    {
        this.assistantUserId = assistantUserId;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public String getUseDateStr()
    {
        return useDateStr;
    }

    public void setUseDateStr(String useDateStr)
    {
        this.useDateStr = useDateStr;
    }

    public List<CatMedication> getDetails()
    {
        return details;
    }

    public void setDetails(List<CatMedication> details)
    {
        this.details = details;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("catId", getCatId())
            .append("useDate", getUseDate())
            .append("timePeriod", getTimePeriod())
            .append("drugCount", getDrugCount())
            .append("execStatus", getExecStatus())
            .append("executor", getExecutor())
            .toString();
    }
}
