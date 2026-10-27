package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatMaterialCategory;

/**
 * 物资分类 服务层
 * 
 * @author ruoyi
 */
public interface ICatMaterialCategoryService
{
    public CatMaterialCategory selectCatMaterialCategoryById(Long categoryId);

    public List<CatMaterialCategory> selectCatMaterialCategoryList(CatMaterialCategory categoryId);

    public int insertCatMaterialCategory(CatMaterialCategory categoryId);

    public int updateCatMaterialCategory(CatMaterialCategory categoryId);

    public int deleteCatMaterialCategoryByIds(Long[] categoryIds);
}
