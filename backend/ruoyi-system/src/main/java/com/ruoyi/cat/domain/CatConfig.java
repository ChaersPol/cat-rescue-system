package com.ruoyi.cat.domain;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 系统配置表
 * 
 * @author ruoyi
 */
public class CatConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 配置id */
    @Excel(name = "配置ID", cellType = ColumnType.NUMERIC)
    private Long configId;
    /** 配置键 */
    @Excel(name = "配置键")
    private String configKey;
    /** 配置值 */
    @Excel(name = "配置值")
    private String configValue;
    /** 配置说明 */
    @Excel(name = "配置说明")
    private String configName;
    public Long getConfigId()
    {
        return configId;
    }
    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }
    public String getConfigKey()
    {
        return configKey;
    }
    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }
    public String getConfigValue()
    {
        return configValue;
    }
    public void setConfigValue(String configValue)
    {
        this.configValue = configValue;
    }
    public String getConfigName()
    {
        return configName;
    }
    public void setConfigName(String configName)
    {
        this.configName = configName;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("configKey", getConfigKey())
            .append("configValue", getConfigValue())
            .append("configName", getConfigName())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
