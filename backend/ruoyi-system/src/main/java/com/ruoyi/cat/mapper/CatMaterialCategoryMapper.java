package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatMaterialCategory;

/**
 * 物资分类 数据层
 * 
 * @author ruoyi
 */
public interface CatMaterialCategoryMapper
{
    public CatMaterialCategory selectCatMaterialCategoryById(Long categoryId);

    public List<CatMaterialCategory> selectCatMaterialCategoryList(CatMaterialCategory categoryId);

    public int insertCatMaterialCategory(CatMaterialCategory categoryId);

    public int updateCatMaterialCategory(CatMaterialCategory categoryId);

    public int deleteCatMaterialCategoryById(Long categoryId);

    public int deleteCatMaterialCategoryByIds(Long[] categoryIds);
}
