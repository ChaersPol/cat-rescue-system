package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatWardType;

/**
 * 病房类型 数据层
 *
 * @author ruoyi
 */
public interface CatWardTypeMapper
{
    public CatWardType selectCatWardTypeById(Long typeId);

    public List<CatWardType> selectCatWardTypeList(CatWardType catWardType);

    /** 全部正常状态类型（下拉用，不分页） */
    public List<CatWardType> selectCatWardTypeAll();

    /** 按名称统计（唯一性校验，excludeId 用于修改时排除自己） */
    public int countByTypeName(@Param("typeName") String typeName, @Param("excludeId") Long excludeId);

    public int insertCatWardType(CatWardType catWardType);

    public int updateCatWardType(CatWardType catWardType);

    public int deleteCatWardTypeById(Long typeId);

    public int deleteCatWardTypeByIds(Long[] typeIds);

    /** 被该类型的病房数量（删除前校验） */
    public int countWardByTypeId(Long typeId);
}
