package com.ruoyi.web.controller.cat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.cat.service.ICatWardAuthService;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatExpense;
import com.ruoyi.cat.domain.CatTnr;
import com.ruoyi.cat.domain.CatVisit;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.mapper.CatExpenseMapper;
import com.ruoyi.cat.mapper.CatTnrMapper;
import com.ruoyi.cat.mapper.CatVisitMapper;
import com.ruoyi.cat.mapper.CatDeathMapper;
import com.ruoyi.cat.mapper.CatReferralMapper;
import com.ruoyi.cat.domain.CatDeath;
import com.ruoyi.cat.domain.CatReferral;
import com.ruoyi.cat.domain.CatBase;
import com.ruoyi.cat.domain.CatTag;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatAdoption;
import com.ruoyi.cat.mapper.CatBaseMapper;
import com.ruoyi.cat.mapper.CatTagMapper;
import com.ruoyi.cat.mapper.CatVaccineMapper;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.mapper.CatAdoptionMapper;
import java.math.BigDecimal;

/**
 * 统计看板控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/dashboard")
public class CatDashboardController
{
    @Autowired
    private CatProfileMapper catProfileMapper;
    @Autowired
    private CatExpenseMapper catExpenseMapper;
    @Autowired
    private CatTnrMapper catTnrMapper;
    @Autowired
    private CatVisitMapper catVisitMapper;
    @Autowired
    private CatDeathMapper catDeathMapper;
    @Autowired
    private CatReferralMapper catReferralMapper;

    @Autowired
    private CatBaseMapper catBaseMapper;
    @Autowired
    private CatTagMapper catTagMapper;
    @Autowired
    private CatVaccineMapper catVaccineMapper;
    @Autowired
    private CatDewormMapper catDewormMapper;
    @Autowired
    private CatAdoptionMapper catAdoptionMapper;

    @Autowired
    private ICatWardAuthService catWardAuthService;

    /**
     * 助理按病房授权限定统计范围（管理员、医生不受限制）
     */
    private CatProfile scopeQuery(CatProfile query)
    {
        if (!catWardAuthService.isWardScopedUser())
        {
            return query;
        }
        List<Long> wardIds = catWardAuthService.selectWardIdsByUserId(SecurityUtils.getUserId());
        query.setWardIds(wardIds.toArray(new Long[0]));
        query.setWardScope(Boolean.TRUE);
        return query;
    }

    /**
     * 统计看板数据
     *
     * 看板是所有人的首页（/index），因此不限具体权限串，登录即可访问；
     * 数据范围由 scopeQuery 按病房授权收敛（助理只看自己被授权的病房）。
     * 注：原先写的 cat:dashboard:view 在 sys_menu 里没有对应菜单/按钮，
     * 除超管(*:*:*)外任何角色都拿不到数据，首页图表会全空并抛 403。
     */
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        Map<String, Object> result = new HashMap<>();

        // 猫咪总数
        CatProfile totalQuery = scopeQuery(new CatProfile());
        result.put("totalCats", catProfileMapper.selectCatProfileList(totalQuery).size());

        // 各状态数量
        Map<String, Integer> statusCount = new HashMap<>();
        String[] statuses = {"1", "2", "3", "4", "5", "6", "7", "8"};
        String[] statusNames = {"在养", "基地内治疗中", "外送医院治疗中", "已放归", "已领养", "已离世", "TNR治疗中", "姑息照料"};
        for (int i = 0; i < statuses.length; i++)
        {
            CatProfile q = scopeQuery(new CatProfile());
            q.setStatus(statuses[i]);
            statusCount.put(statusNames[i], catProfileMapper.selectCatProfileList(q).size());
        }
        result.put("statusCount", statusCount);

        // TNR 抓捕总数
        CatTnr tnrQuery = new CatTnr();
        List<CatTnr> tnrList = catTnrMapper.selectCatTnrList(tnrQuery);
        result.put("tnrCount", tnrList.size());

        // TNR 已绝育（已登记手术日期）
        int tnrNeutered = 0;
        for (CatTnr t : tnrList)
        {
            if (t.getSurgeryDate() != null)
            {
                tnrNeutered++;
            }
        }
        result.put("tnrNeutered", tnrNeutered);

        // 待回访 / 回访异常
        CatVisit visitQuery = new CatVisit();
        visitQuery.setVisitStatus("0");
        result.put("pendingVisit", catVisitMapper.selectCatVisitList(visitQuery).size());

        CatVisit abnormalQuery = new CatVisit();
        abnormalQuery.setVisitStatus("2");
        result.put("visitAbnormal", catVisitMapper.selectCatVisitList(abnormalQuery).size());

        // 待疫苗 / 待驱虫（7 天内到期或已过期未接种）
        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, 7);
        Date limitDate = limit.getTime();
        List<CatVaccine> vaccines = catVaccineMapper.selectCatVaccineList(new CatVaccine());
        java.util.Set<Long> dueVaccineCats = new java.util.HashSet<>();
        for (CatVaccine v : vaccines)
        {
            if (v.getNextDate() != null && !v.getNextDate().after(limitDate))
            {
                dueVaccineCats.add(v.getCatId());
            }
        }
        result.put("dueVaccine", dueVaccineCats.size());

        List<CatDeworm> deworms = catDewormMapper.selectCatDewormList(new CatDeworm());
        java.util.Set<Long> dueDewormCats = new java.util.HashSet<>();
        for (CatDeworm d : deworms)
        {
            if (d.getNextDate() != null && !d.getNextDate().after(limitDate))
            {
                dueDewormCats.add(d.getCatId());
            }
        }
        result.put("dueDeworm", dueDewormCats.size());

        // 领养数量
        result.put("adoptionCount", catAdoptionMapper.selectCatAdoptionList(new CatAdoption()).size());

        // 死亡数量 + 死因分布
        CatDeath deathQuery = new CatDeath();
        List<CatDeath> deaths = catDeathMapper.selectCatDeathList(deathQuery);
        result.put("deathCount", deaths.size());
        Map<String, Integer> deathTypeCount = new LinkedHashMap<>();
        String[] deathTypes = {"1", "2", "3", "4"};
        String[] deathTypeNames = {"自然寿终", "病重不治", "安乐", "意外"};
        for (int i = 0; i < deathTypes.length; i++)
        {
            int count = 0;
            for (CatDeath d : deaths)
            {
                if (deathTypes[i].equals(d.getDeathType()))
                {
                    count++;
                }
            }
            deathTypeCount.put(deathTypeNames[i], count);
        }
        result.put("deathTypeCount", deathTypeCount);

        // 外送医院治疗中数量
        CatReferral referralQuery = new CatReferral();
        referralQuery.setStatus("0");
        result.put("referralCount", catReferralMapper.selectCatReferralList(referralQuery).size());

        // 各基地猫咪数量（只在养 / 治疗中 / 姑息照料这类在基地的猫计入）
        List<Map<String, Object>> baseCount = new ArrayList<>();
        for (CatBase base : catBaseMapper.selectCatBaseList(new CatBase()))
        {
            CatProfile q = scopeQuery(new CatProfile());
            q.setBaseId(base.getBaseId());
            Map<String, Object> item = new HashMap<>();
            item.put("baseId", base.getBaseId());
            item.put("baseName", base.getBaseName());
            item.put("baseType", base.getBaseType());
            item.put("count", catProfileMapper.selectCatProfileList(q).size());
            baseCount.add(item);
        }
        result.put("baseCount", baseCount);

        // 标签分布（慢性病、口炎等标签的猫咪总数，供标签筛选统计）
        List<Map<String, Object>> tagCount = new ArrayList<>();
        for (CatTag tag : catTagMapper.selectCatTagList(new CatTag()))
        {
            CatProfile q = scopeQuery(new CatProfile());
            q.setTagIds(new Long[] { tag.getTagId() });
            Map<String, Object> item = new HashMap<>();
            item.put("tagId", tag.getTagId());
            item.put("tagName", tag.getTagName());
            item.put("tagGroup", tag.getTagGroup());
            item.put("count", catProfileMapper.selectCatProfileList(q).size());
            tagCount.add(item);
        }
        tagCount.sort((a, b) -> Integer.compare((Integer) b.get("count"), (Integer) a.get("count")));
        result.put("tagCount", tagCount);

        // 财务支出统计（累计 + 本期）
        CatExpense expenseQuery = new CatExpense();
        List<CatExpense> expenses = catExpenseMapper.selectCatExpenseList(expenseQuery);
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal medicalExpense = BigDecimal.ZERO;
        BigDecimal materialExpense = BigDecimal.ZERO;
        BigDecimal monthExpense = BigDecimal.ZERO;
        String currentMonth = new SimpleDateFormat("yyyy-MM").format(new Date());
        for (CatExpense e : expenses)
        {
            BigDecimal amount = e.getAmount() == null ? BigDecimal.ZERO : e.getAmount();
            totalExpense = totalExpense.add(amount);
            if (e.getExpenseDate() != null
                    && currentMonth.equals(new SimpleDateFormat("yyyy-MM").format(e.getExpenseDate())))
            {
                monthExpense = monthExpense.add(amount);
            }
            if ("2".equals(e.getExpenseType()))
            {
                medicalExpense = medicalExpense.add(amount);
            }
            else if ("1".equals(e.getExpenseType()))
            {
                materialExpense = materialExpense.add(amount);
            }
        }
        result.put("totalExpense", totalExpense);
        result.put("medicalExpense", medicalExpense);
        result.put("materialExpense", materialExpense);
        result.put("monthExpense", monthExpense);

        return AjaxResult.success(result);
    }
}
