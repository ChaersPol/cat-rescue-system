package com.ruoyi.cat.domain;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 死亡台账表
 * 
 * @author ruoyi
 */
public class CatDeath extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 死亡记录id */
    @Excel(name = "死亡记录ID", cellType = ColumnType.NUMERIC)
    private Long deathId;
    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 死亡日期 */
    @Excel(name = "死亡日期", dateFormat = "yyyy-MM-dd")
    private Date deathDate;
    /** 死亡地点 */
    @Excel(name = "死亡地点")
    private String deathPlace;
    /** 死亡类型 */
    @Excel(name = "死亡类型", dictType = "cat_death_type")
    private String deathType;
    /** 死因描述 */
    @Excel(name = "死因描述")
    private String deathReason;
    /** 既往病史 */
    @Excel(name = "既往病史")
    private String history;
    /** 遗体处理方式 */
    @Excel(name = "遗体处理方式")
    private String bodyDisposal;
    /** 操作医生 */
    @Excel(name = "操作医生")
    private String doctor;
    /** 登记人 */
    @Excel(name = "登记人")
    private String registrar;
    /** 照片 */
    @Excel(name = "照片")
    private String photos;
    public Long getDeathId()
    {
        return deathId;
    }
    public void setDeathId(Long deathId)
    {
        this.deathId = deathId;
    }
    public Long getCatId()
    {
        return catId;
    }
    public void setCatId(Long catId)
    {
        this.catId = catId;
    }
    public String getCatRefName()
    {
        return catRefName;
    }
    public void setCatRefName(String catRefName)
    {
        this.catRefName = catRefName;
    }
    public Date getDeathDate()
    {
        return deathDate;
    }
    public void setDeathDate(Date deathDate)
    {
        this.deathDate = deathDate;
    }
    public String getDeathPlace()
    {
        return deathPlace;
    }
    public void setDeathPlace(String deathPlace)
    {
        this.deathPlace = deathPlace;
    }
    public String getDeathType()
    {
        return deathType;
    }
    public void setDeathType(String deathType)
    {
        this.deathType = deathType;
    }
    public String getDeathReason()
    {
        return deathReason;
    }
    public void setDeathReason(String deathReason)
    {
        this.deathReason = deathReason;
    }
    public String getHistory()
    {
        return history;
    }
    public void setHistory(String history)
    {
        this.history = history;
    }
    public String getBodyDisposal()
    {
        return bodyDisposal;
    }
    public void setBodyDisposal(String bodyDisposal)
    {
        this.bodyDisposal = bodyDisposal;
    }
    public String getDoctor()
    {
        return doctor;
    }
    public void setDoctor(String doctor)
    {
        this.doctor = doctor;
    }
    public String getRegistrar()
    {
        return registrar;
    }
    public void setRegistrar(String registrar)
    {
        this.registrar = registrar;
    }
    public String getPhotos()
    {
        return photos;
    }
    public void setPhotos(String photos)
    {
        this.photos = photos;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("deathId", getDeathId())
            .append("catId", getCatId())
            .append("deathDate", getDeathDate())
            .append("deathPlace", getDeathPlace())
            .append("deathType", getDeathType())
            .append("deathReason", getDeathReason())
            .append("history", getHistory())
            .append("bodyDisposal", getBodyDisposal())
            .append("doctor", getDoctor())
            .append("registrar", getRegistrar())
            .append("photos", getPhotos())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
