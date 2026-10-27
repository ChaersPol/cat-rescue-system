package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatFeeCategory;
import com.ruoyi.cat.mapper.CatFeeCategoryMapper;
import com.ruoyi.cat.service.ICatFeeCategoryService;

/**
 * 费用分类 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatFeeCategoryServiceImpl implements ICatFeeCategoryService
{
    @Autowired
    private CatFeeCategoryMapper categoryIdMapper;

    @Override
    public CatFeeCategory selectCatFeeCategoryById(Long categoryId)
    {
        return categoryIdMapper.selectCatFeeCategoryById(categoryId);
    }

    @Override
    public List<CatFeeCategory> selectCatFeeCategoryList(CatFeeCategory categoryId)
    {
        return categoryIdMapper.selectCatFeeCategoryList(categoryId);
    }

    @Override
    public int insertCatFeeCategory(CatFeeCategory categoryId)
    {
        return categoryIdMapper.insertCatFeeCategory(categoryId);
    }

    @Override
    public int updateCatFeeCategory(CatFeeCategory categoryId)
    {
        return categoryIdMapper.updateCatFeeCategory(categoryId);
    }

    @Override
    public int deleteCatFeeCategoryByIds(Long[] categoryIds)
    {
        return categoryIdMapper.deleteCatFeeCategoryByIds(categoryIds);
    }
}
