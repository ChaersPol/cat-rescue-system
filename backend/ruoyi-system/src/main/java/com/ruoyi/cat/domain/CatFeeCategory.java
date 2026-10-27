package com.ruoyi.cat.domain;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 费用分类表
 * 
 * @author ruoyi
 */
public class CatFeeCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 分类id */
    private Long categoryId;
    /** 费用分类名称 */
    private String categoryName;
    /** 费用类型 */
    private String categoryType;
    /** 显示顺序 */
    private Integer orderNum;
    /** 状态 */
    private String status;
    public Long getCategoryId()
    {
        return categoryId;
    }
    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }
    public String getCategoryName()
    {
        return categoryName;
    }
    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }
    public String getCategoryType()
    {
        return categoryType;
    }
    public void setCategoryType(String categoryType)
    {
        this.categoryType = categoryType;
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
            .append("categoryId", getCategoryId())
            .append("categoryName", getCategoryName())
            .append("categoryType", getCategoryType())
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
