package com.ruoyi.cat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 驱虫记录表
 * 
 * @author ruoyi
 */
public class CatDeworm extends BaseEntity
{
    /** 猫所属基地 ID（筛选条件，非本表字段） */
    private Long catBaseId;

    /** 猫所属病房 ID（筛选条件，非本表字段） */
    private Long catWardId;

    private static final long serialVersionUID = 1L;
    /** 驱虫记录id */
    @Excel(name = "驱虫记录ID", cellType = ColumnType.NUMERIC)
    private Long dewormId;
    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 驱虫类型 */
    @Excel(name = "驱虫类型", dictType = "cat_deworm_type")
    private String dewormType;
    /** 驱虫药 */
    @Excel(name = "驱虫药")
    private String dewormDrug;
    /** 驱虫日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "驱虫日期", dateFormat = "yyyy-MM-dd")
    private Date dewormDate;
    /** 来源（1手动录入 2站内诊断开具） */
    @Excel(name = "来源", readConverterExp = "1=手动录入,2=站内诊断")
    private String sourceType;
    /** 来源诊断id */
    private Long diagnosisId;
    /** 下次驱虫时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "下次驱虫时间", dateFormat = "yyyy-MM-dd")
    private Date nextDate;
    /** 查询用：N 天内到期（含已过期），非表字段，对应「待驱虫」看板口径 */
    private Integer dueDays;
    /** 驱虫间隔天数（30 / 60），非表字段：前端选了周期后据此推算「下次驱虫」，不入库 */
    private Integer cycleDays;
    /** 关联物资 id（空表示不扣库存），手工录入时可挂驱虫药 */
    @Excel(name = "关联物资")
    private Long materialId;
    /** 物资名称快照 */
    private String materialName;
    /** 本次消耗数量 */
    @Excel(name = "消耗数量")
    private BigDecimal stockQty;
    /** 非表字段：前端取消关联物资时置 true，动态 update 才能写进 null */
    private Boolean clearMaterial;

    public String getSourceType()
    {
        return sourceType;
    }
    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }
    public Long getDiagnosisId()
    {
        return diagnosisId;
    }
    public void setDiagnosisId(Long diagnosisId)
    {
        this.diagnosisId = diagnosisId;
    }
    public Long getDewormId()
    {
        return dewormId;
    }
    public void setDewormId(Long dewormId)
    {
        this.dewormId = dewormId;
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
    public String getDewormType()
    {
        return dewormType;
    }
    public void setDewormType(String dewormType)
    {
        this.dewormType = dewormType;
    }
    public String getDewormDrug()
    {
        return dewormDrug;
    }
    public void setDewormDrug(String dewormDrug)
    {
        this.dewormDrug = dewormDrug;
    }
    public Date getDewormDate()
    {
        return dewormDate;
    }
    public void setDewormDate(Date dewormDate)
    {
        this.dewormDate = dewormDate;
    }
    public Date getNextDate()
    {
        return nextDate;
    }
    public void setNextDate(Date nextDate)
    {
        this.nextDate = nextDate;
    }
    public Integer getDueDays()
    {
        return dueDays;
    }
    public void setDueDays(Integer dueDays)
    {
        this.dueDays = dueDays;
    }
    public Integer getCycleDays()
    {
        return cycleDays;
    }
    public void setCycleDays(Integer cycleDays)
    {
        this.cycleDays = cycleDays;
    }
    public Long getMaterialId()
    {
        return materialId;
    }
    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }
    public String getMaterialName()
    {
        return materialName;
    }
    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }
    public BigDecimal getStockQty()
    {
        return stockQty;
    }
    public void setStockQty(BigDecimal stockQty)
    {
        this.stockQty = stockQty;
    }
    public Boolean getClearMaterial()
    {
        return clearMaterial;
    }
    public void setClearMaterial(Boolean clearMaterial)
    {
        this.clearMaterial = clearMaterial;
    }
    public Long getCatBaseId()
    {
        return catBaseId;
    }

    public void setCatBaseId(Long catBaseId)
    {
        this.catBaseId = catBaseId;
    }

    public Long getCatWardId()
    {
        return catWardId;
    }

    public void setCatWardId(Long catWardId)
    {
        this.catWardId = catWardId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("dewormId", getDewormId())
            .append("catId", getCatId())
            .append("dewormType", getDewormType())
            .append("dewormDrug", getDewormDrug())
            .append("dewormDate", getDewormDate())
            .append("nextDate", getNextDate())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
