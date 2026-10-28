package com.ruoyi.cat.domain;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 标签库表
 * 
 * @author ruoyi
 */
public class CatTag extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 标签id */
    @Excel(name = "标签ID", cellType = ColumnType.NUMERIC)
    private Long tagId;
    /** 标签名称 */
    @Excel(name = "标签名称")
    private String tagName;
    /** 标签分组 */
    @Excel(name = "标签分组")
    private String tagGroup;
    /** 状态（0正常 1停用） */
    @Excel(name = "状态", dictType = "sys_normal_disable")
    private String status;
    public Long getTagId()
    {
        return tagId;
    }
    public void setTagId(Long tagId)
    {
        this.tagId = tagId;
    }
    public String getTagName()
    {
        return tagName;
    }
    public void setTagName(String tagName)
    {
        this.tagName = tagName;
    }
    public String getTagGroup()
    {
        return tagGroup;
    }
    public void setTagGroup(String tagGroup)
    {
        this.tagGroup = tagGroup;
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
            .append("tagId", getTagId())
            .append("tagName", getTagName())
            .append("tagGroup", getTagGroup())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
