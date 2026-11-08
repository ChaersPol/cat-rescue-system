package com.ruoyi.cat.domain;

import java.util.Date;

/**
 * 按猫合并的站内诊断概览
 * <p>
 * 一只猫一行：就诊次数、首次/最近就诊时间、最近一次诊断结果，
 * 点开后可查看该猫的全部病史。原「一次就诊一行」的流水视图仍然保留。
 *
 * @author ruoyi
 */
public class CatDiagnosisSummary
{
    /** 猫咪id */
    private Long catId;
    /** 猫咪名字 */
    private String catName;
    /** 档案编号 */
    private String archiveNo;
    /** 来源基地id（猫咪归属基地） */
    private Long sourceBaseId;
    /** 当前所在基地id */
    private Long baseId;
    /** 当前所在病房id */
    private Long wardId;
    /** 猫咪当前状态（字典 cat_status） */
    private String catStatus;
    /** 就诊次数（当前筛选条件下的记录数） */
    private Integer visitCount;
    /** 其中标记为「初诊」的条数，>1 说明有人填错，列表会给出提示 */
    private Integer initialCount;
    /** 首次就诊时间 */
    private Date firstVisitDate;
    /** 最近一次就诊时间 */
    private Date lastVisitTime;
    /** 最近一次就诊的医生 */
    private String lastDoctor;
    /** 最近一次就诊的诊断结果 */
    private String lastDiagnosisResult;
    /** 最近一次就诊的类型（1初诊 2复诊） */
    private String lastVisitType;

    public Long getCatId()
    {
        return catId;
    }

    public void setCatId(Long catId)
    {
        this.catId = catId;
    }

    public String getCatName()
    {
        return catName;
    }

    public void setCatName(String catName)
    {
        this.catName = catName;
    }

    public String getArchiveNo()
    {
        return archiveNo;
    }

    public void setArchiveNo(String archiveNo)
    {
        this.archiveNo = archiveNo;
    }

    public Long getSourceBaseId()
    {
        return sourceBaseId;
    }

    public void setSourceBaseId(Long sourceBaseId)
    {
        this.sourceBaseId = sourceBaseId;
    }

    public Long getBaseId()
    {
        return baseId;
    }

    public void setBaseId(Long baseId)
    {
        this.baseId = baseId;
    }

    public Long getWardId()
    {
        return wardId;
    }

    public void setWardId(Long wardId)
    {
        this.wardId = wardId;
    }

    public String getCatStatus()
    {
        return catStatus;
    }

    public void setCatStatus(String catStatus)
    {
        this.catStatus = catStatus;
    }

    public Integer getVisitCount()
    {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount)
    {
        this.visitCount = visitCount;
    }

    public Integer getInitialCount()
    {
        return initialCount;
    }

    public void setInitialCount(Integer initialCount)
    {
        this.initialCount = initialCount;
    }

    public Date getFirstVisitDate()
    {
        return firstVisitDate;
    }

    public void setFirstVisitDate(Date firstVisitDate)
    {
        this.firstVisitDate = firstVisitDate;
    }

    public Date getLastVisitTime()
    {
        return lastVisitTime;
    }

    public void setLastVisitTime(Date lastVisitTime)
    {
        this.lastVisitTime = lastVisitTime;
    }

    public String getLastDoctor()
    {
        return lastDoctor;
    }

    public void setLastDoctor(String lastDoctor)
    {
        this.lastDoctor = lastDoctor;
    }

    public String getLastDiagnosisResult()
    {
        return lastDiagnosisResult;
    }

    public void setLastDiagnosisResult(String lastDiagnosisResult)
    {
        this.lastDiagnosisResult = lastDiagnosisResult;
    }

    public String getLastVisitType()
    {
        return lastVisitType;
    }

    public void setLastVisitType(String lastVisitType)
    {
        this.lastVisitType = lastVisitType;
    }
}
