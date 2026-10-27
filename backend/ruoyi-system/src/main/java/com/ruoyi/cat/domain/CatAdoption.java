package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 领养登记表
 * 
 * @author ruoyi
 */
public class CatAdoption extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 领养记录id */
    private Long adoptionId;
    /** 猫咪id */
    private Long catId;
    /** 领养人姓名 */
    private String adopterName;
    /** 领养人电话 */
    private String adopterPhone;
    /** 领养人地址 */
    private String adopterAddress;
    /** 领养日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date adoptionDate;
    /** 领养协议上传地址 */
    private String agreementUrl;
    public Long getAdoptionId()
    {
        return adoptionId;
    }
    public void setAdoptionId(Long adoptionId)
    {
        this.adoptionId = adoptionId;
    }
    public Long getCatId()
    {
        return catId;
    }
    public void setCatId(Long catId)
    {
        this.catId = catId;
    }
    public String getAdopterName()
    {
        return adopterName;
    }
    public void setAdopterName(String adopterName)
    {
        this.adopterName = adopterName;
    }
    public String getAdopterPhone()
    {
        return adopterPhone;
    }
    public void setAdopterPhone(String adopterPhone)
    {
        this.adopterPhone = adopterPhone;
    }
    public String getAdopterAddress()
    {
        return adopterAddress;
    }
    public void setAdopterAddress(String adopterAddress)
    {
        this.adopterAddress = adopterAddress;
    }
    public Date getAdoptionDate()
    {
        return adoptionDate;
    }
    public void setAdoptionDate(Date adoptionDate)
    {
        this.adoptionDate = adoptionDate;
    }
    public String getAgreementUrl()
    {
        return agreementUrl;
    }
    public void setAgreementUrl(String agreementUrl)
    {
        this.agreementUrl = agreementUrl;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("adoptionId", getAdoptionId())
            .append("catId", getCatId())
            .append("adopterName", getAdopterName())
            .append("adopterPhone", getAdopterPhone())
            .append("adopterAddress", getAdopterAddress())
            .append("adoptionDate", getAdoptionDate())
            .append("agreementUrl", getAgreementUrl())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
