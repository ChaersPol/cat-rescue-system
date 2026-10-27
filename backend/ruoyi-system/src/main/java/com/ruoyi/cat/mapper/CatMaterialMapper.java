package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatMaterial;

/**
 * 物资 数据层
 * 
 * @author ruoyi
 */
public interface CatMaterialMapper
{
    public CatMaterial selectCatMaterialById(Long materialId);

    public List<CatMaterial> selectCatMaterialList(CatMaterial materialId);

    public int insertCatMaterial(CatMaterial materialId);

    public int updateCatMaterial(CatMaterial materialId);

    public int deleteCatMaterialById(Long materialId);

    public int deleteCatMaterialByIds(Long[] materialIds);

    /**
     * 更新库存（增减）
     * @param materialId 物资id
     * @param quantity 变动数量（入库正数，出库负数）
     */
    public int updateStock(@Param("materialId") Long materialId, @Param("quantity") java.math.BigDecimal quantity);
}
