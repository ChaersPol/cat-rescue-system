package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 猫咪档案表
 * 
 * @author ruoyi
 */
public class CatProfile extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 猫咪id */
    @Excel(name = "猫咪ID", cellType = ColumnType.NUMERIC)
    private Long catId;
    /** 档案编号 */
    @Excel(name = "档案编号")
    private String archiveNo;
    /** 猫咪名字 */
    @Excel(name = "猫咪名字")
    private String catName;
    /** 别名 */
    @Excel(name = "别名")
    private String alias;
    /** 品种 */
    @Excel(name = "品种")
    private String breed;
    /** 性别（0公 1母 2未知） */
    @Excel(name = "性别", dictType = "cat_sex")
    private String sex;
    /** 大概年龄 */
    @Excel(name = "大概年龄")
    private String ageEstimate;
    /** 体重 */
    @Excel(name = "体重")
    private String weight;
    /** 性格 */
    @Excel(name = "性格")
    private String personality;
    /** 来源（1抓捕/TNR 2救助捡到 3他人移交 4其他） */
    @Excel(name = "来源", dictType = "cat_source")
    private String source;
    /** 抓捕/救助日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "抓捕/救助日期", dateFormat = "yyyy-MM-dd")
    private Date captureDate;
    /** 抓捕点位 */
    @Excel(name = "抓捕点位")
    private String capturePoint;
    /** 是否绝育（0未 1已 2未知） */
    @Excel(name = "是否绝育", dictType = "cat_neutered")
    private String neutered;
    /** 是否驱虫 */
    @Excel(name = "是否驱虫", dictType = "cat_yes_no")
    private String dewormed;
    /** 是否疫苗 */
    @Excel(name = "是否疫苗", dictType = "cat_yes_no")
    private String vaccinated;
    /** 标志性特征 */
    @Excel(name = "标志性特征")
    private String feature;
    /** 猫咪状态 */
    @Excel(name = "猫咪状态", dictType = "cat_status")
    private String status;
    /** 当前所在基地id */
    @Excel(name = "当前所在基地ID", cellType = ColumnType.NUMERIC)
    private Long baseId;
    /** 当前所在病房id */
    @Excel(name = "当前所在病房ID", cellType = ColumnType.NUMERIC)
    private Long wardId;
    /** 归属基地id */
    @Excel(name = "归属基地ID", cellType = ColumnType.NUMERIC)
    private Long ownerBaseId;
    /** 外送医院名称 */
    @Excel(name = "外送医院名称")
    private String hospitalName;
    /** 二维码图片地址（base64，列表与导出不返回，避免长文本撑爆单元格） */
    private String qrCode;
    /** 二维码内容（扫码链接，非表字段，便于前端直观展示二维码指向哪里） */
    private String scanUrl;
    /** 是否已生成二维码（1已生成 0未生成，非表字段；列表接口用它替代 base64 本体） */
    private Integer hasQrCode;
    /** 是否归档（0未归档 1已归档） */
    @Excel(name = "是否归档", dictType = "cat_yes_no")
    private String archived;
    /** 标签id数组（非表字段） */
    private Long[] tagIds;
    /** 可见病房范围（非表字段，助理按病房授权过滤） */
    private Long[] wardIds;
    /** 是否启用病房授权过滤（非表字段） */
    private Boolean wardScope;
    public Long getCatId()
    {
        return catId;
    }
    public void setCatId(Long catId)
    {
        this.catId = catId;
    }
    public String getArchiveNo()
    {
        return archiveNo;
    }
    public void setArchiveNo(String archiveNo)
    {
        this.archiveNo = archiveNo;
    }
    public String getCatName()
    {
        return catName;
    }
    public void setCatName(String catName)
    {
        this.catName = catName;
    }
    public String getAlias()
    {
        return alias;
    }
    public void setAlias(String alias)
    {
        this.alias = alias;
    }
    public String getBreed()
    {
        return breed;
    }
    public void setBreed(String breed)
    {
        this.breed = breed;
    }
    public String getSex()
    {
        return sex;
    }
    public void setSex(String sex)
    {
        this.sex = sex;
    }
    public String getAgeEstimate()
    {
        return ageEstimate;
    }
    public void setAgeEstimate(String ageEstimate)
    {
        this.ageEstimate = ageEstimate;
    }
    public String getWeight()
    {
        return weight;
    }
    public void setWeight(String weight)
    {
        this.weight = weight;
    }
    public String getPersonality()
    {
        return personality;
    }
    public void setPersonality(String personality)
    {
        this.personality = personality;
    }
    public String getSource()
    {
        return source;
    }
    public void setSource(String source)
    {
        this.source = source;
    }
    public Date getCaptureDate()
    {
        return captureDate;
    }
    public void setCaptureDate(Date captureDate)
    {
        this.captureDate = captureDate;
    }
    public String getCapturePoint()
    {
        return capturePoint;
    }
    public void setCapturePoint(String capturePoint)
    {
        this.capturePoint = capturePoint;
    }
    public String getNeutered()
    {
        return neutered;
    }
    public void setNeutered(String neutered)
    {
        this.neutered = neutered;
    }
    public String getDewormed()
    {
        return dewormed;
    }
    public void setDewormed(String dewormed)
    {
        this.dewormed = dewormed;
    }
    public String getVaccinated()
    {
        return vaccinated;
    }
    public void setVaccinated(String vaccinated)
    {
        this.vaccinated = vaccinated;
    }
    public String getFeature()
    {
        return feature;
    }
    public void setFeature(String feature)
    {
        this.feature = feature;
    }
    public String getStatus()
    {
        return status;
    }
    public void setStatus(String status)
    {
        this.status = status;
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
    public Long getOwnerBaseId()
    {
        return ownerBaseId;
    }
    public void setOwnerBaseId(Long ownerBaseId)
    {
        this.ownerBaseId = ownerBaseId;
    }
    public String getHospitalName()
    {
        return hospitalName;
    }
    public void setHospitalName(String hospitalName)
    {
        this.hospitalName = hospitalName;
    }
    public String getQrCode()
    {
        return qrCode;
    }
    public void setQrCode(String qrCode)
    {
        this.qrCode = qrCode;
    }
    public String getScanUrl()
    {
        return scanUrl;
    }
    public void setScanUrl(String scanUrl)
    {
        this.scanUrl = scanUrl;
    }
    public Integer getHasQrCode()
    {
        return hasQrCode;
    }
    public void setHasQrCode(Integer hasQrCode)
    {
        this.hasQrCode = hasQrCode;
    }
    public String getArchived()
    {
        return archived;
    }
    public void setArchived(String archived)
    {
        this.archived = archived;
    }
    public Long[] getTagIds()
    {
        return tagIds;
    }
    public void setTagIds(Long[] tagIds)
    {
        this.tagIds = tagIds;
    }
    public Long[] getWardIds()
    {
        return wardIds;
    }
    public void setWardIds(Long[] wardIds)
    {
        this.wardIds = wardIds;
    }
    public Boolean getWardScope()
    {
        return wardScope;
    }
    public void setWardScope(Boolean wardScope)
    {
        this.wardScope = wardScope;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("catId", getCatId())
            .append("archiveNo", getArchiveNo())
            .append("catName", getCatName())
            .append("alias", getAlias())
            .append("breed", getBreed())
            .append("sex", getSex())
            .append("ageEstimate", getAgeEstimate())
            .append("weight", getWeight())
            .append("personality", getPersonality())
            .append("source", getSource())
            .append("captureDate", getCaptureDate())
            .append("capturePoint", getCapturePoint())
            .append("neutered", getNeutered())
            .append("dewormed", getDewormed())
            .append("vaccinated", getVaccinated())
            .append("feature", getFeature())
            .append("status", getStatus())
            .append("baseId", getBaseId())
            .append("wardId", getWardId())
            .append("ownerBaseId", getOwnerBaseId())
            .append("hospitalName", getHospitalName())
            .append("qrCode", getQrCode())
            .append("archived", getArchived())
            .append("tagIds", getTagIds())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
