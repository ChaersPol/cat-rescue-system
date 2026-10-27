package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatFeeCategory;

/**
 * 费用分类 数据层
 * 
 * @author ruoyi
 */
public interface CatFeeCategoryMapper
{
    public CatFeeCategory selectCatFeeCategoryById(Long categoryId);

    public List<CatFeeCategory> selectCatFeeCategoryList(CatFeeCategory categoryId);

    public int insertCatFeeCategory(CatFeeCategory categoryId);

    public int updateCatFeeCategory(CatFeeCategory categoryId);

    public int deleteCatFeeCategoryById(Long categoryId);

    public int deleteCatFeeCategoryByIds(Long[] categoryIds);
}
