package com.ruoyi.cat.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.mapper.CatVaccineMapper;
import com.ruoyi.cat.util.CatImmunizationRule;

/**
 * 免疫与驱虫台账的只读汇总
 * <p>
 * 疫苗弹窗的「该猫接种记录」、驱虫弹窗的「上次体内/体外」、档案页的「免疫概览」
 * 都从这里取数，保证三个地方口径一致。
 *
 * @author ruoyi
 */
@Service
public class CatImmunizationSummaryService
{
    private static final String TYPE_IN = "体内";
    private static final String TYPE_OUT = "体外";

    @Autowired
    private CatVaccineMapper vaccineMapper;

    @Autowired
    private CatDewormMapper dewormMapper;

    /** 该猫全部疫苗记录，按接种日期倒序（最新的在最前面） */
    public List<CatVaccine> vaccinesDesc(Long catId)
    {
        List<CatVaccine> list = vaccinesAsc(catId);
        list.sort(Comparator.comparing(CatVaccine::getVaccineDate,
                Comparator.nullsFirst(Comparator.naturalOrder())).reversed());
        return list;
    }

    /** 该猫全部疫苗记录，按接种日期正序 */
    public List<CatVaccine> vaccinesAsc(Long catId)
    {
        CatVaccine query = new CatVaccine();
        query.setCatId(catId);
        List<CatVaccine> list = vaccineMapper.selectCatVaccineList(query);
        list.sort(Comparator.comparing(CatVaccine::getVaccineDate,
                Comparator.nullsFirst(Comparator.naturalOrder())));
        return list;
    }

    /** 该猫全部驱虫记录，按驱虫日期正序 */
    public List<CatDeworm> dewormsAsc(Long catId)
    {
        CatDeworm query = new CatDeworm();
        query.setCatId(catId);
        List<CatDeworm> list = dewormMapper.selectCatDewormList(query);
        list.sort(Comparator.comparing(CatDeworm::getDewormDate,
                Comparator.nullsFirst(Comparator.naturalOrder())));
        return list;
    }

    /** 该猫最近一次某类型驱虫（体内 / 体外）；没有则 null */
    public CatDeworm lastDeworm(Long catId, String keyword)
    {
        CatDeworm last = null;
        for (CatDeworm d : dewormsAsc(catId))
        {
            if (d.getDewormType() != null && d.getDewormType().contains(keyword))
            {
                last = d;
            }
        }
        return last;
    }

    /**
     * 疫苗弹窗用：接种记录（倒序）+ 免疫进度
     *
     * @param excludeId 编辑当前记录时传自己的 id，进度里不会被自己重复计数
     */
    public Map<String, Object> vaccineHistory(Long catId, Long excludeId)
    {
        List<CatVaccine> all = vaccinesAsc(catId);
        List<Map<String, Object>> rows = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        int baseNo = 0;
        for (int i = all.size() - 1; i >= 0; i--)
        {
            CatVaccine v = all.get(i);
            Map<String, Object> row = new HashMap<>();
            row.put("vaccineId", v.getVaccineId());
            row.put("vaccineType", v.getVaccineType());
            row.put("vaccineDate", v.getVaccineDate() == null ? null : fmt.format(v.getVaccineDate()));
            row.put("nextDate", v.getNextDate() == null ? null : fmt.format(v.getNextDate()));
            row.put("daysLeft", CatImmunizationRule.daysLeft(v.getNextDate()));
            row.put("responsible", v.getResponsible());
            row.put("sourceType", v.getSourceType());
            rows.add(row);
        }
        // 针次按时间正序编号，弹窗与档案时间线的「第 N 针」对得上
        for (CatVaccine v : all)
        {
            if (CatImmunizationRule.isBaseDose(v.getVaccineType()))
            {
                baseNo++;
                for (Map<String, Object> row : rows)
                {
                    if (v.getVaccineId() != null && v.getVaccineId().equals(row.get("vaccineId")))
                    {
                        row.put("doseNo", baseNo);
                    }
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("rows", rows);
        result.put("overview", overview(catId, excludeId));
        return result;
    }

    /** 驱虫弹窗用：上一次体内 / 上一次体外 */
    public Map<String, Object> dewormHistory(Long catId)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("lastIn", dewormRow(lastDeworm(catId, TYPE_IN)));
        result.put("lastOut", dewormRow(lastDeworm(catId, TYPE_OUT)));
        return result;
    }

    private Map<String, Object> dewormRow(CatDeworm d)
    {
        if (d == null)
        {
            return null;
        }
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Object> row = new HashMap<>();
        row.put("dewormId", d.getDewormId());
        row.put("dewormType", d.getDewormType());
        row.put("dewormDrug", d.getDewormDrug());
        row.put("dewormDate", d.getDewormDate() == null ? null : fmt.format(d.getDewormDate()));
        row.put("nextDate", d.getNextDate() == null ? null : fmt.format(d.getNextDate()));
        row.put("daysLeft", CatImmunizationRule.daysLeft(d.getNextDate()));
        return row;
    }

    /**
     * 免疫概览：基础免疫进度、狂犬到期日、加强针到期日、上次体内/体外驱虫
     */
    public Map<String, Object> overview(Long catId, Long excludeVaccineId)
    {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Object> overview = new HashMap<>();

        // ---- 基础免疫：第一针 / 第二针 / 第三针 ----
        int baseDone = 0;
        Date baseNextDate = null;
        CatVaccine rabies = null;
        CatVaccine booster = null;
        for (CatVaccine v : vaccinesAsc(catId))
        {
            if (excludeVaccineId != null && excludeVaccineId.equals(v.getVaccineId()))
            {
                continue;
            }
            if (CatImmunizationRule.isBaseDose(v.getVaccineType()))
            {
                baseDone++;
                baseNextDate = v.getNextDate();
            }
            else if (CatImmunizationRule.isAnnual(v.getVaccineType()))
            {
                if (v.getVaccineType().contains("加强"))
                {
                    booster = v;
                }
                else
                {
                    rabies = v;
                }
            }
        }
        overview.put("baseDoseDone", Math.min(baseDone, CatImmunizationRule.BASE_DOSE_TOTAL));
        overview.put("baseDoseTotal", CatImmunizationRule.BASE_DOSE_TOTAL);
        // 三针打完就不再提示「下一针」
        overview.put("baseDoseNextDate", baseDone >= CatImmunizationRule.BASE_DOSE_TOTAL || baseNextDate == null
                ? null : fmt.format(baseNextDate));
        overview.put("baseDoseNextDaysLeft", baseDone >= CatImmunizationRule.BASE_DOSE_TOTAL
                ? null : CatImmunizationRule.daysLeft(baseNextDate));

        putAnnual(overview, "rabies", rabies, fmt);
        putAnnual(overview, "booster", booster, fmt);

        CatDeworm in = lastDeworm(catId, TYPE_IN);
        CatDeworm out = lastDeworm(catId, TYPE_OUT);
        putDeworm(overview, "lastIn", in, fmt);
        putDeworm(overview, "lastOut", out, fmt);
        return overview;
    }

    private void putAnnual(Map<String, Object> overview, String prefix, CatVaccine v, SimpleDateFormat fmt)
    {
        overview.put(prefix + "LastDate", v == null || v.getVaccineDate() == null ? null : fmt.format(v.getVaccineDate()));
        overview.put(prefix + "NextDate", v == null || v.getNextDate() == null ? null : fmt.format(v.getNextDate()));
        overview.put(prefix + "DaysLeft", v == null ? null : CatImmunizationRule.daysLeft(v.getNextDate()));
    }

    private void putDeworm(Map<String, Object> overview, String prefix, CatDeworm d, SimpleDateFormat fmt)
    {
        overview.put(prefix + "Date", d == null || d.getDewormDate() == null ? null : fmt.format(d.getDewormDate()));
        overview.put(prefix + "NextDate", d == null || d.getNextDate() == null ? null : fmt.format(d.getNextDate()));
        overview.put(prefix + "DaysLeft", d == null ? null : CatImmunizationRule.daysLeft(d.getNextDate()));
        overview.put(prefix + "Drug", d == null ? null : d.getDewormDrug());
    }
}
