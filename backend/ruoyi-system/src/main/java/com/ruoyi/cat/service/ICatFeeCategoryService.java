package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatFeeCategory;

/**
 * 费用分类 服务层
 * 
 * @author ruoyi
 */
public interface ICatFeeCategoryService
{
    public CatFeeCategory selectCatFeeCategoryById(Long categoryId);

    public List<CatFeeCategory> selectCatFeeCategoryList(CatFeeCategory categoryId);

    public int insertCatFeeCategory(CatFeeCategory categoryId);

    public int updateCatFeeCategory(CatFeeCategory categoryId);

    public int deleteCatFeeCategoryByIds(Long[] categoryIds);
}
