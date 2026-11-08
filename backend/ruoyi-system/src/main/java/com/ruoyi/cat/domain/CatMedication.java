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
 * 每日用药记录表
 * 
 * @author ruoyi
 */
public class CatMedication extends BaseEntity
{
    /** 猫所属基地 ID（筛选条件，非本表字段） */
    private Long catBaseId;

    /** 猫所属病房 ID（筛选条件，非本表字段） */
    private Long catWardId;

    /**
     * 助理负责人 ID（筛选条件，非本表字段）
     * <p>
     * 语义同合并视图：按 cat_ward_auth 的病房授权反查猫，跟「执行人」不是一回事。
     * 明细视图（一个药一行）以前漏了这个字段，前端选了也不生效，列表不会变。
     */
    private Long assistantUserId;

    /**
     * 完成状态（筛选条件，非本表字段）
     * <p>
     * 打卡状态是「组级」的（猫 + 日期 + 时间段，存 cat_medication_exec），
     * 明细行要按它所属那一组的状态来筛，所以只能在 SQL 里 EXISTS 判断，不能存本表。
     */
    private String execStatus;

    private static final long serialVersionUID = 1L;
    /** 用药记录id */
    @Excel(name = "用药记录ID", cellType = ColumnType.NUMERIC)
    private Long medicationId;
    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 用药日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "用药日期", dateFormat = "yyyy-MM-dd")
    private Date useDate;
    /** 时间段 */
    @Excel(name = "时间段", dictType = "cat_time_period")
    private String timePeriod;
    /** 药品名称 */
    @Excel(name = "药品名称")
    private String drugName;
    /** 剂量 */
    @Excel(name = "剂量")
    private String dosage;
    /** 给药方式 */
    @Excel(name = "给药方式", dictType = "cat_administration")
    private String administration;
    /** 用药执行人 */
    @Excel(name = "用药执行人")
    private String executor;
    /** 不良反应记录 */
    @Excel(name = "不良反应")
    private String adverseReaction;
    /** 来源（1手动录入 2站内诊断开具） */
    @Excel(name = "来源", readConverterExp = "1=手动录入,2=站内诊断")
    private String sourceType;
    /** 来源诊断id */
    private Long diagnosisId;
    /** 来源处方id（医生开的用药方案展开出来的明细才有） */
    private Long prescriptionId;
    /** 照片 */
    @Excel(name = "照片")
    private String photos;
    /**
     * 关联物资 id（为空表示这次用药不消耗库存）
     * <p>
     * 手工录入时可以挂一个药品/耗材，保存后自动生成一张出库单；
     * 处方和诊断展开出来的明细不挂物资——库存已经由诊断单那条明细扣过了。
     */
    @Excel(name = "关联物资")
    private Long materialId;
    /** 物资名称快照（物资后来改名/删除，历史记录仍能看出当时用的是哪个） */
    private String materialName;
    /** 本次消耗数量（挂在物资上才有意义；空表示不扣库存） */
    @Excel(name = "消耗数量")
    private BigDecimal stockQty;
    /** 非表字段：前端取消关联物资时置 true，否则动态 update 写不进 null */
    private Boolean clearMaterial;

    public Long getPrescriptionId()
    {
        return prescriptionId;
    }
    public void setPrescriptionId(Long prescriptionId)
    {
        this.prescriptionId = prescriptionId;
    }
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
    public Long getMedicationId()
    {
        return medicationId;
    }
    public void setMedicationId(Long medicationId)
    {
        this.medicationId = medicationId;
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
    public Date getUseDate()
    {
        return useDate;
    }
    public void setUseDate(Date useDate)
    {
        this.useDate = useDate;
    }
    public String getTimePeriod()
    {
        return timePeriod;
    }
    public void setTimePeriod(String timePeriod)
    {
        this.timePeriod = timePeriod;
    }
    public String getDrugName()
    {
        return drugName;
    }
    public void setDrugName(String drugName)
    {
        this.drugName = drugName;
    }
    public String getDosage()
    {
        return dosage;
    }
    public void setDosage(String dosage)
    {
        this.dosage = dosage;
    }
    public String getAdministration()
    {
        return administration;
    }
    public void setAdministration(String administration)
    {
        this.administration = administration;
    }
    public String getExecutor()
    {
        return executor;
    }
    public void setExecutor(String executor)
    {
        this.executor = executor;
    }
    public String getAdverseReaction()
    {
        return adverseReaction;
    }
    public void setAdverseReaction(String adverseReaction)
    {
        this.adverseReaction = adverseReaction;
    }
    public String getPhotos()
    {
        return photos;
    }
    public void setPhotos(String photos)
    {
        this.photos = photos;
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

    public Long getAssistantUserId()
    {
        return assistantUserId;
    }

    public void setAssistantUserId(Long assistantUserId)
    {
        this.assistantUserId = assistantUserId;
    }

    public String getExecStatus()
    {
        return execStatus;
    }

    public void setExecStatus(String execStatus)
    {
        this.execStatus = execStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("medicationId", getMedicationId())
            .append("catId", getCatId())
            .append("useDate", getUseDate())
            .append("timePeriod", getTimePeriod())
            .append("drugName", getDrugName())
            .append("dosage", getDosage())
            .append("administration", getAdministration())
            .append("executor", getExecutor())
            .append("adverseReaction", getAdverseReaction())
            .append("photos", getPhotos())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
