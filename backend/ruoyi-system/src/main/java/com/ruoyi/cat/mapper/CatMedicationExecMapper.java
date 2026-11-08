package com.ruoyi.cat.mapper;

import java.util.Date;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatMedicationExec;

/**
 * 用药执行打卡 数据层
 *
 * @author ruoyi
 */
public interface CatMedicationExecMapper
{
    /** 取某一组（猫 + 日期 + 时间段）的打卡记录，没有返回 null */
    CatMedicationExec selectExec(@Param("catId") Long catId,
                                 @Param("useDate") Date useDate,
                                 @Param("timePeriod") String timePeriod);

    int insertExec(CatMedicationExec exec);

    int updateExec(CatMedicationExec exec);
}
