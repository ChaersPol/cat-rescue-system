package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatMedication;

/**
 * 每日用药记录 数据层
 * 
 * @author ruoyi
 */
public interface CatMedicationMapper
{
    public CatMedication selectCatMedicationById(Long medicationId);

    public List<CatMedication> selectCatMedicationList(CatMedication medicationId);

    /**
     * 批量查询一批猫咪最近 days 天内的用药记录（疫苗/驱虫提醒判断「用药期间」用）
     *
     * @param catIds 猫咪 id 集合，不能为空
     * @param days   回溯天数
     * @return 用药记录，按用药日期倒序
     */
    public List<CatMedication> selectRecentMedicationByCatIds(@Param("catIds") List<Long> catIds,
                                                              @Param("days") int days);

    public int insertCatMedication(CatMedication medicationId);

    public int updateCatMedication(CatMedication medicationId);

    public int deleteCatMedicationById(Long medicationId);

    public int deleteCatMedicationByIds(Long[] medicationIds);
}
