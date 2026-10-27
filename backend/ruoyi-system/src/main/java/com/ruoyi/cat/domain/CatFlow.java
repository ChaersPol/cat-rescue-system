package com.ruoyi.cat.domain;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 流转记录表
 * 
 * @author ruoyi
 */
public class CatFlow extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 流转记录id */
    private Long flowId;
    /** 猫咪id */
    private Long catId;
    /** 转出基地id */
    private Long fromBaseId;
    /** 转出病房id */
    private Long fromWardId;
    /** 转入基地id */
    private Long toBaseId;
    /** 转入病房id */
    private Long toWardId;
    /** 流转类型 */
    private String flowType;
    /** 流转原因 */
    private String flowReason;
    /** 医院名称 */
    private String hospitalName;
    /** 医院处置结论 */
    private String hospitalResult;
    /** 操作人 */
    private String operateBy;
    /** 操作时间 */
    private Date operateTime;
    public Long getFlowId()
    {
        return flowId;
    }
    public void setFlowId(Long flowId)
    {
        this.flowId = flowId;
    }
    public Long getCatId()
    {
        return catId;
    }
    public void setCatId(Long catId)
    {
        this.catId = catId;
    }
    public Long getFromBaseId()
    {
        return fromBaseId;
    }
    public void setFromBaseId(Long fromBaseId)
    {
        this.fromBaseId = fromBaseId;
    }
    public Long getFromWardId()
    {
        return fromWardId;
    }
    public void setFromWardId(Long fromWardId)
    {
        this.fromWardId = fromWardId;
    }
    public Long getToBaseId()
    {
        return toBaseId;
    }
    public void setToBaseId(Long toBaseId)
    {
        this.toBaseId = toBaseId;
    }
    public Long getToWardId()
    {
        return toWardId;
    }
    public void setToWardId(Long toWardId)
    {
        this.toWardId = toWardId;
    }
    public String getFlowType()
    {
        return flowType;
    }
    public void setFlowType(String flowType)
    {
        this.flowType = flowType;
    }
    public String getFlowReason()
    {
        return flowReason;
    }
    public void setFlowReason(String flowReason)
    {
        this.flowReason = flowReason;
    }
    public String getHospitalName()
    {
        return hospitalName;
    }
    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }
    public String getHospitalResult()
    {
        return hospitalResult;
    }
    public void setHospitalResult(String hospitalResult)
    {
        this.hospitalResult = hospitalResult;
    }
    public String getOperateBy()
    {
        return operateBy;
    }
    public void setOperateBy(String operateBy)
    {
        this.operateBy = operateBy;
    }
    public Date getOperateTime()
    {
        return operateTime;
    }
    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("flowId", getFlowId())
            .append("catId", getCatId())
            .append("fromBaseId", getFromBaseId())
            .append("fromWardId", getFromWardId())
            .append("toBaseId", getToBaseId())
            .append("toWardId", getToWardId())
            .append("flowType", getFlowType())
            .append("flowReason", getFlowReason())
            .append("hospitalName", getHospitalName())
            .append("hospitalResult", getHospitalResult())
            .append("operateBy", getOperateBy())
            .append("operateTime", getOperateTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
