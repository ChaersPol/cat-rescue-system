package com.ruoyi.cat.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.mapper.CatMedicationMapper;
import com.ruoyi.cat.service.ICatMedicationService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 每日用药记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatMedicationServiceImpl implements ICatMedicationService
{
    @Autowired
    private CatMedicationMapper medicationIdMapper;

    @Override
    public CatMedication selectCatMedicationById(Long medicationId)
    {
        return medicationIdMapper.selectCatMedicationById(medicationId);
    }

    @Override
    public List<CatMedication> selectCatMedicationList(CatMedication medicationId)
    {
        return medicationIdMapper.selectCatMedicationList(medicationId);
    }

    @Override
    public int insertCatMedication(CatMedication medicationId)
    {
        return medicationIdMapper.insertCatMedication(medicationId);
    }

    @Override
    public int updateCatMedication(CatMedication medicationId)
    {
        return medicationIdMapper.updateCatMedication(medicationId);
    }

    /**
     * 批量录入同一只猫连续多天的用药（需求文档 6.3）
     * <p>
     * 从 useDate 开始，按天连续生成 days 条记录；同一天同一时间段同一种药已存在则跳过，
     * 避免重复打卡把用药台账刷成重复数据。
     */
    @Override
    @Transactional
    public int insertCatMedicationBatch(CatMedication medication, Integer days)
    {
        if (medication.getCatId() == null)
        {
            throw new ServiceException("请选择猫咪");
        }
        if (medication.getUseDate() == null)
        {
            throw new ServiceException("请选择用药开始日期");
        }
        if (medication.getDrugName() == null || medication.getDrugName().isEmpty())
        {
            throw new ServiceException("请输入药品名称");
        }
        int total = days == null || days < 1 ? 1 : days;
        if (total > 60)
        {
            throw new ServiceException("单次批量录入不能超过 60 天");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(medication.getUseDate());

        int inserted = 0;
        for (int i = 0; i < total; i++)
        {
            Date day = calendar.getTime();

            CatMedication query = new CatMedication();
            query.setCatId(medication.getCatId());
            query.setUseDate(day);
            query.setDrugName(medication.getDrugName());
            boolean exists = false;
            for (CatMedication exist : medicationIdMapper.selectCatMedicationList(query))
            {
                String existPeriod = exist.getTimePeriod() == null ? "" : exist.getTimePeriod();
                String curPeriod = medication.getTimePeriod() == null ? "" : medication.getTimePeriod();
                if (existPeriod.equals(curPeriod))
                {
                    exists = true;
                    break;
                }
            }
            if (!exists)
            {
                CatMedication record = new CatMedication();
                record.setCatId(medication.getCatId());
                record.setUseDate(day);
                record.setTimePeriod(medication.getTimePeriod());
                record.setDrugName(medication.getDrugName());
                record.setDosage(medication.getDosage());
                record.setAdministration(medication.getAdministration());
                record.setExecutor(medication.getExecutor());
                record.setAdverseReaction(medication.getAdverseReaction());
                record.setPhotos(medication.getPhotos());
                record.setRemark(medication.getRemark());
                record.setCreateBy(medication.getCreateBy());
                inserted += medicationIdMapper.insertCatMedication(record);
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return inserted;
    }

    @Override
    public int deleteCatMedicationByIds(Long[] medicationIds)
    {
        return medicationIdMapper.deleteCatMedicationByIds(medicationIds);
    }
}
