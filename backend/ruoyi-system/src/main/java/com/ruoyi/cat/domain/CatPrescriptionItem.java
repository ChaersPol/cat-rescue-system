package com.ruoyi.cat.domain;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.common.utils.StringUtils;

/**
 * 处方明细（一味药一行）
 * <p>
 * timePeriods 存该药的给药时段，逗号分隔（如「早,晚」）。同一种药可能早晚各一次，
 * 所以时段是明细上的属性，不拆成多行。
 *
 * @author ruoyi
 */
public class CatPrescriptionItem
{
    /** 明细id */
    private Long itemId;
    /** 处方id */
    private Long prescriptionId;
    /** 药品名称 */
    private String drugName;
    /** 剂量 */
    private String dosage;
    /** 给药方式 */
    private String administration;
    /** 时间段（逗号分隔：早,中,晚） */
    private String timePeriods;
    /** 用法说明 */
    private String usageNote;
    /** 排序 */
    private Integer sort;

    /** 把「早,晚」拆成列表，空值返回空列表（展开用药明细时逐个时段生成记录） */
    public List<String> periodList()
    {
        List<String> periods = new ArrayList<>();
        if (StringUtils.isEmpty(timePeriods))
        {
            return periods;
        }
        for (String part : timePeriods.split(","))
        {
            String p = part == null ? "" : part.trim();
            if (!p.isEmpty() && !periods.contains(p))
            {
                periods.add(p);
            }
        }
        return periods;
    }

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getPrescriptionId()
    {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId)
    {
        this.prescriptionId = prescriptionId;
    }

    public String getDrugName()
    {
        return drugName;
    }

    public void setDrugName(String drugName)
    {
        this.drugName = drugName;
    }

    public String getDosage()
    {
        return dosage;
    }

    public void setDosage(String dosage)
    {
        this.dosage = dosage;
    }

    public String getAdministration()
    {
        return administration;
    }

    public void setAdministration(String administration)
    {
        this.administration = administration;
    }

    public String getTimePeriods()
    {
        return timePeriods;
    }

    public void setTimePeriods(String timePeriods)
    {
        this.timePeriods = timePeriods;
    }

    public String getUsageNote()
    {
        return usageNote;
    }

    public void setUsageNote(String usageNote)
    {
        this.usageNote = usageNote;
    }

    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }
}
