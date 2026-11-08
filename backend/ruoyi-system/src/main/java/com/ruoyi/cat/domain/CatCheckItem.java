package com.ruoyi.cat.domain;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
/**
 * 检查项目/用药目录（树形）
 *
 * @author ruoyi
 */
public class CatCheckItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 目录id */
    @Excel(name = "目录ID", cellType = ColumnType.NUMERIC)
    private Long itemId;

    /** 父级id（0=顶级） */
    private Long parentId;

    /** 祖级列表 */
    private String ancestors;

    /** 名称 */
    @Excel(name = "名称")
    private String itemName;

    /** 类型（1检查项目 2用药 3疫苗 4驱虫） */
    @Excel(name = "类型", readConverterExp = "1=检查项目,2=用药,3=疫苗,4=驱虫")
    private String itemType;

    /** 关联物资id（关联后开具自动出库并扣库存） */
    private Long materialId;

    /** 关联物资名称（非表字段，列表展示用） */
    @Excel(name = "关联物资")
    private String materialName;

    /** 关联物资当前库存（非表字段，开单时提示剩多少） */
    private BigDecimal stock;

    /** 单位 */
    @Excel(name = "单位")
    private String unit;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal price;

    /** 显示顺序 */
    @Excel(name = "顺序", cellType = ColumnType.NUMERIC)
    private Integer orderNum;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", dictType = "sys_normal_disable")
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

    /** 父级名称（非表字段） */
    private String parentName;

    /** 子节点（非表字段，前端树表用） */
    private List<CatCheckItem> children = new ArrayList<>();

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getAncestors()
    {
        return ancestors;
    }

    public void setAncestors(String ancestors)
    {
        this.ancestors = ancestors;
    }

    public String getItemName()
    {
        return itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    public String getItemType()
    {
        return itemType;
    }

    public void setItemType(String itemType)
    {
        this.itemType = itemType;
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

    public BigDecimal getStock()
    {
        return stock;
    }

    public void setStock(BigDecimal stock)
    {
        this.stock = stock;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
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

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getParentName()
    {
        return parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }

    public List<CatCheckItem> getChildren()
    {
        return children;
    }

    public void setChildren(List<CatCheckItem> children)
    {
        this.children = children;
    }
}
