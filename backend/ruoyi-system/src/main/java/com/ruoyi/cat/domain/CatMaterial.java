package com.ruoyi.cat.domain;
import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 物资表
 * 
 * @author ruoyi
 */
public class CatMaterial extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 物资id */
    @Excel(name = "物资ID", cellType = ColumnType.NUMERIC)
    private Long materialId;
    /** 物资分类id */
    private Long categoryId;
    /** 物资分类名称（导出展示用，非表字段） */
    @Excel(name = "物资分类")
    private String materialCategoryRefName;
    /** 物资名称 */
    @Excel(name = "物资名称")
    private String materialName;
    /** 规格 */
    @Excel(name = "规格")
    private String spec;
    /** 单位 */
    @Excel(name = "单位")
    private String unit;
    /** 当前库存 */
    @Excel(name = "当前库存", cellType = ColumnType.NUMERIC)
    private BigDecimal stock;
    /** 库存预警阈值 */
    @Excel(name = "库存预警阈值", cellType = ColumnType.NUMERIC)
    private BigDecimal minStock;
    /** 库存状态筛选（low=库存不足 ok=库存正常），非持久化字段 */
    private String stockStatus;

    public String getStockStatus()
    {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus)
    {
        this.stockStatus = stockStatus;
    }
    public Long getMaterialId()
    {
        return materialId;
    }
    public void setMaterialId(Long materialId)
    {
        this.materialId = materialId;
    }
    public Long getCategoryId()
    {
        return categoryId;
    }
    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }
    public String getMaterialCategoryRefName()
    {
        return materialCategoryRefName;
    }
    public void setMaterialCategoryRefName(String materialCategoryRefName)
    {
        this.materialCategoryRefName = materialCategoryRefName;
    }
    public String getMaterialName()
    {
        return materialName;
    }
    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }
    public String getSpec()
    {
        return spec;
    }
    public void setSpec(String spec)
    {
        this.spec = spec;
    }
    public String getUnit()
    {
        return unit;
    }
    public void setUnit(String unit)
    {
        this.unit = unit;
    }
    public BigDecimal getStock()
    {
        return stock;
    }
    public void setStock(BigDecimal stock)
    {
        this.stock = stock;
    }
    public BigDecimal getMinStock()
    {
        return minStock;
    }
    public void setMinStock(BigDecimal minStock)
    {
        this.minStock = minStock;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("materialId", getMaterialId())
            .append("categoryId", getCategoryId())
            .append("materialName", getMaterialName())
            .append("spec", getSpec())
            .append("unit", getUnit())
            .append("stock", getStock())
            .append("minStock", getMinStock())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
