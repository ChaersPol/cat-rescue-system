package com.ruoyi.cat.domain;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 病房/区域表
 * 
 * @author ruoyi
 */
public class CatWard extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 病房id */
    private Long wardId;
    /** 所属基地id */
    private Long baseId;
    /** 病房名称 */
    private String wardName;
    /** 病房类型 */
    private String wardType;
    /** 显示顺序 */
    private Integer orderNum;
    /** 状态（0正常 1停用） */
    private String status;
    public Long getWardId()
    {
        return wardId;
    }
    public void setWardId(Long wardId)
    {
        this.wardId = wardId;
    }
    public Long getBaseId()
    {
        return baseId;
    }
    public void setBaseId(Long baseId)
    {
        this.baseId = baseId;
    }
    public String getWardName()
    {
        return wardName;
    }
    public void setWardName(String wardName)
    {
        this.wardName = wardName;
    }
    public String getWardType()
    {
        return wardType;
    }
    public void setWardType(String wardType)
    {
        this.wardType = wardType;
    }
    public Integer getOrderNum()
    {
        return orderNum;
    }
    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
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
            .append("wardId", getWardId())
            .append("baseId", getBaseId())
            .append("wardName", getWardName())
            .append("wardType", getWardType())
            .append("orderNum", getOrderNum())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
