package com.ruoyi.cat.domain;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 病房授权表
 * 
 * @author ruoyi
 */
public class CatWardAuth extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 用户id */
    private Long userId;
    /** 用户（昵称+登录名），导出展示用非表字段 */
    @Excel(name = "用户")
    private String userRefName;
    /** 病房id */
    private Long wardId;
    /** 病房（病房+所属基地），导出展示用非表字段 */
    @Excel(name = "病房")
    private String wardRefName;
    public Long getUserId()
    {
        return userId;
    }
    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
    public String getUserRefName()
    {
        return userRefName;
    }
    public void setUserRefName(String userRefName)
    {
        this.userRefName = userRefName;
    }
    public Long getWardId()
    {
        return wardId;
    }
    public void setWardId(Long wardId)
    {
        this.wardId = wardId;
    }
    public String getWardRefName()
    {
        return wardRefName;
    }
    public void setWardRefName(String wardRefName)
    {
        this.wardRefName = wardRefName;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("userId", getUserId())
            .append("wardId", getWardId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
