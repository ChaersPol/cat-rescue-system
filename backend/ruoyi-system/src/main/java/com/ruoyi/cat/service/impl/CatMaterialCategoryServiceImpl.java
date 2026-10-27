package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatMaterialCategory;
import com.ruoyi.cat.mapper.CatMaterialCategoryMapper;
import com.ruoyi.cat.service.ICatMaterialCategoryService;

/**
 * 物资分类 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatMaterialCategoryServiceImpl implements ICatMaterialCategoryService
{
    @Autowired
    private CatMaterialCategoryMapper categoryIdMapper;

    @Override
    public CatMaterialCategory selectCatMaterialCategoryById(Long categoryId)
    {
        return categoryIdMapper.selectCatMaterialCategoryById(categoryId);
    }

    @Override
    public List<CatMaterialCategory> selectCatMaterialCategoryList(CatMaterialCategory categoryId)
    {
        return categoryIdMapper.selectCatMaterialCategoryList(categoryId);
    }

    @Override
    public int insertCatMaterialCategory(CatMaterialCategory categoryId)
    {
        return categoryIdMapper.insertCatMaterialCategory(categoryId);
    }

    @Override
    public int updateCatMaterialCategory(CatMaterialCategory categoryId)
    {
        return categoryIdMapper.updateCatMaterialCategory(categoryId);
    }

    @Override
    public int deleteCatMaterialCategoryByIds(Long[] categoryIds)
    {
        return categoryIdMapper.deleteCatMaterialCategoryByIds(categoryIds);
    }
}
