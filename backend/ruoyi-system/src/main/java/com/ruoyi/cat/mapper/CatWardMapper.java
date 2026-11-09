package com.ruoyi.cat.mapper;

import java.util.List;
import java.util.Map;
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

    /**
     * 删除病房前的占用校验：返回还挂着在册猫咪的病房及其数量
     * （key：wardId / wardName / catCount），没有猫的病房不会出现
     */
    public List<Map<String, Object>> selectWardCatCounts(Long[] wardIds);

    /** 病房删除时连带清掉授权行 */
    public int deleteWardAuthByWardIds(Long[] wardIds);

    /** 病房类型改名时，同步在用病房的冗余名称列 */
    public int updateWardTypeNameByTypeId(@Param("typeId") Long typeId, @Param("typeName") String typeName);
}
