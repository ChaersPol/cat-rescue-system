package com.ruoyi.cat.domain;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 基地表
 * 
 * @author ruoyi
 */
public class CatBase extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 基地id */
    private Long baseId;
    /** 基地名称 */
    private String baseName;
    /** 基地类型（1饲养基地 2医疗基地 3放归点位） */
    private String baseType;
    /** 基地负责人 */
    private String leader;
    /** 联系电话 */
    private String phone;
    /** 基地地址 */
    private String address;
    /** 状态（0正常 1停用） */
    private String status;
    public Long getBaseId()
    {
        return baseId;
    }
    public void setBaseId(Long baseId)
    {
        this.baseId = baseId;
    }
    public String getBaseName()
    {
        return baseName;
    }
    public void setBaseName(String baseName)
    {
        this.baseName = baseName;
    }
    public String getBaseType()
    {
        return baseType;
    }
    public void setBaseType(String baseType)
    {
        this.baseType = baseType;
    }
    public String getLeader()
    {
        return leader;
    }
    public void setLeader(String leader)
    {
        this.leader = leader;
    }
    public String getPhone()
    {
        return phone;
    }
    public void setPhone(String phone)
    {
        this.phone = phone;
    }
    public String getAddress()
    {
        return address;
    }
    public void setAddress(String address)
    {
        this.address = address;
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
            .append("baseId", getBaseId())
            .append("baseName", getBaseName())
            .append("baseType", getBaseType())
            .append("leader", getLeader())
            .append("phone", getPhone())
            .append("address", getAddress())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
