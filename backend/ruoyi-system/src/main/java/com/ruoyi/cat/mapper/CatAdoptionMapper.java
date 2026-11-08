package com.ruoyi.cat.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatAdoption;

/**
 * 领养登记 数据层
 * 
 * @author ruoyi
 */
public interface CatAdoptionMapper
{
    public CatAdoption selectCatAdoptionById(Long adoptionId);

    public List<CatAdoption> selectCatAdoptionList(CatAdoption adoptionId);

    public int insertCatAdoption(CatAdoption adoptionId);

    public int updateCatAdoption(CatAdoption adoptionId);

    /**
     * 回访结束开关：三个字段整体覆盖
     * 重新打开时 visitClosedTime / visitClosedBy 要能置空，所以不能用 updateCatAdoption 的非空判断
     */
    public int updateVisitClosed(@Param("adoptionId") Long adoptionId,
                                 @Param("visitClosed") String visitClosed,
                                 @Param("visitClosedTime") Date visitClosedTime,
                                 @Param("visitClosedBy") String visitClosedBy);

    public int deleteCatAdoptionById(Long adoptionId);

    public int deleteCatAdoptionByIds(Long[] adoptionIds);
}
