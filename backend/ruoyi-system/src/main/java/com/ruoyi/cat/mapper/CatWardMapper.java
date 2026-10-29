package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatWard;

/**
 * 病房 数据层
 * 
 * @author ruoyi
 */
public interface CatWardMapper
{
    public CatWard selectCatWardById(Long wardId);

    public List<CatWard> selectCatWardList(CatWard wardId);

    public int insertCatWard(CatWard wardId);

    public int updateCatWard(CatWard wardId);

    public int deleteCatWardById(Long wardId);

    public int deleteCatWardByIds(Long[] wardIds);

    /** 病房类型改名时，同步在用病房的冗余名称列 */
    public int updateWardTypeNameByTypeId(@Param("typeId") Long typeId, @Param("typeName") String typeName);
}
