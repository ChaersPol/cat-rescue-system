package com.ruoyi.cat.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 病房类型表 cat_ward_type
 *
 * 这是「病房类型」的可维护字典：病房管理里的类型下拉、列表着色都取自这里。
 *
 * @author ruoyi
 */
public class CatWardType extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 病房类型id */
    @Excel(name = "类型ID", cellType = ColumnType.NUMERIC)
    private Long typeId;

    /** 类型名称 */
    @Excel(name = "类型名称")
    private String typeName;

    /** 标签颜色（primary/success/info/warning/danger） */
    @Excel(name = "标签颜色")
    private String tagType;

    /** 显示顺序 */
    @Excel(name = "显示顺序", cellType = ColumnType.NUMERIC)
    private Integer orderNum;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", dictType = "sys_normal_disable")
    private String status;

    /** 使用该类型的病房数量（非表字段，列表展示用） */
    @Excel(name = "病房数量", cellType = ColumnType.NUMERIC)
    private Integer wardCount;

    public Long getTypeId()
    {
        return typeId;
    }

    public void setTypeId(Long typeId)
    {
        this.typeId = typeId;
    }

    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    public String getTagType()
    {
        return tagType;
    }

    public void setTagType(String tagType)
    {
        this.tagType = tagType;
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

    public Integer getWardCount()
    {
        return wardCount;
    }

    public void setWardCount(Integer wardCount)
    {
        this.wardCount = wardCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("typeId", getTypeId())
            .append("typeName", getTypeName())
            .append("tagType", getTagType())
            .append("orderNum", getOrderNum())
            .append("status", getStatus())
            .append("wardCount", getWardCount())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
