package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatMaterial;

/**
 * 物资 服务层
 * 
 * @author ruoyi
 */
public interface ICatMaterialService
{
    public CatMaterial selectCatMaterialById(Long materialId);

    public List<CatMaterial> selectCatMaterialList(CatMaterial materialId);

    public int insertCatMaterial(CatMaterial materialId);

    public int updateCatMaterial(CatMaterial materialId);

    public int deleteCatMaterialByIds(Long[] materialIds);
}
