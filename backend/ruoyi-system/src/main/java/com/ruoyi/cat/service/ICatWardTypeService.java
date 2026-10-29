package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatWardType;

/**
 * 病房类型 服务层
 *
 * @author ruoyi
 */
public interface ICatWardTypeService
{
    public CatWardType selectCatWardTypeById(Long typeId);

    public List<CatWardType> selectCatWardTypeList(CatWardType catWardType);

    /** 全部正常状态类型，下拉用 */
    public List<CatWardType> selectCatWardTypeAll();

    public int insertCatWardType(CatWardType catWardType);

    public int updateCatWardType(CatWardType catWardType);

    public int deleteCatWardTypeByIds(Long[] typeIds);
}
