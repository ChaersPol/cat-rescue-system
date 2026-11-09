package com.ruoyi.cat.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatAdoption;
import com.ruoyi.cat.domain.CatConfig;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatVisit;
import com.ruoyi.cat.mapper.CatAdoptionMapper;
import com.ruoyi.cat.mapper.CatConfigMapper;
import com.ruoyi.cat.mapper.CatReminderMapper;
import com.ruoyi.cat.mapper.CatVisitMapper;
import com.ruoyi.cat.service.ICatDeleteImpactService;
import com.ruoyi.cat.service.ICatAdoptionService;
import com.ruoyi.cat.service.ICatTransferService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 领养登记 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatAdoptionServiceImpl implements ICatAdoptionService
{
    @Autowired
    private ICatDeleteImpactService deleteImpactService;

    /** 回访状态：待回访 */
    private static final String VISIT_PENDING = "0";
    /** 回访状态：已作废（超期未登记，系统自动关掉） */
    private static final String VISIT_VOID = "3";
    /** 回访状态：已结束（领养记录上人工勾了「回访已结束」） */
    private static final String VISIT_CLOSED = "4";
    /** 超期作废的保留月数配置项 */
    private static final String KEY_VISIT_VOID_MONTHS = "visit.void.months";

    @Autowired
    private CatAdoptionMapper catAdoptionMapper;

    @Autowired
    private CatVisitMapper catVisitMapper;

    @Autowired
    private CatReminderMapper catReminderMapper;

    @Autowired
    private CatConfigMapper catConfigMapper;

    @Autowired
    private ICatTransferService catTransferService;

    @Override
    public CatAdoption selectCatAdoptionById(Long adoptionId)
    {
        return catAdoptionMapper.selectCatAdoptionById(adoptionId);
    }

    @Override
    public List<CatAdoption> selectCatAdoptionList(CatAdoption catAdoption)
    {
        return catAdoptionMapper.selectCatAdoptionList(catAdoption);
    }

    @Override
    @Transactional
    public int insertCatAdoption(CatAdoption catAdoption)
    {
        int rows = catAdoptionMapper.insertCatAdoption(catAdoption);
        // 猫咪状态变更为已领养，归档
        if (rows > 0 && catAdoption.getCatId() != null)
        {
            CatProfile profile = new CatProfile();
            profile.setCatId(catAdoption.getCatId());
            profile.setStatus("5"); // 已领养
            profile.setArchived("1"); // 归档

            CatFlow flow = new CatFlow();
            flow.setFlowType("领养");
            flow.setFlowReason("领养登记（领养人：" + catAdoption.getAdopterName() + "）");

            catTransferService.transfer(profile, flow);

            // 自动生成回访计划：3天、1个月、3个月
            if (catAdoption.getAdoptionId() != null && catAdoption.getAdoptionDate() != null)
            {
                createVisitPlans(catAdoption);
            }
        }
        return rows;
    }

    @Override
    public int updateCatAdoption(CatAdoption catAdoption)
    {
        return catAdoptionMapper.updateCatAdoption(catAdoption);
    }

    @Override
    @Transactional
    public int setVisitClosed(Long adoptionId, boolean closed)
    {
        CatAdoption adoption = catAdoptionMapper.selectCatAdoptionById(adoptionId);
        if (adoption == null)
        {
            throw new ServiceException("领养记录不存在或已删除");
        }
        // 开关本身整体覆盖写：重新打开时要把结束时间和操作人一起清空
        catAdoptionMapper.updateVisitClosed(adoptionId, closed ? "1" : "0",
                closed ? new Date() : null, closed ? SecurityUtils.getUsername() : null);

        CatVisit query = new CatVisit();
        query.setAdoptionId(adoptionId);
        List<CatVisit> visits = catVisitMapper.selectCatVisitList(query);

        int affected = 0;
        List<Long> changed = new ArrayList<>();
        if (closed)
        {
            // 关闭：待回访 → 已结束，一并清掉这些计划的提醒
            for (CatVisit v : visits)
            {
                if (!VISIT_PENDING.equals(v.getVisitStatus()))
                {
                    continue;
                }
                catVisitMapper.updateVisitStatus(v.getVisitId(), VISIT_CLOSED);
                changed.add(v.getVisitId());
                affected++;
            }
        }
        else
        {
            // 重新打开：结束期间早就该回访的那几条直接作废，只恢复还来得及的
            Date limit = monthsAgo(voidMonths());
            for (CatVisit v : visits)
            {
                if (!VISIT_CLOSED.equals(v.getVisitStatus()))
                {
                    continue;
                }
                boolean tooLate = v.getPlanDate() != null && v.getPlanDate().before(limit);
                catVisitMapper.updateVisitStatus(v.getVisitId(), tooLate ? VISIT_VOID : VISIT_PENDING);
                if (!tooLate)
                {
                    affected++;
                }
            }
        }
        if (!changed.isEmpty())
        {
            clearReminders(changed);
        }
        return affected;
    }

    /**
     * 清掉这批回访计划对应的提醒（含已读记录）
     * 只删提醒，不动回访记录本身，历史回访仍能在列表里查到
     */
    private void clearReminders(List<Long> visitIds)
    {
        Long[] ids = visitIds.toArray(new Long[0]);
        List<Long> reminderIds = catReminderMapper.selectReminderIdsBySourceIds("visit", ids);
        if (reminderIds != null && !reminderIds.isEmpty())
        {
            catReminderMapper.deleteReminderReadByReminderIds(reminderIds.toArray(new Long[0]));
        }
        catReminderMapper.deleteRemindersBySourceIds("visit", ids);
    }

    /**
     * 超期作废的保留月数，默认 3 个月
     */
    private int voidMonths()
    {
        try
        {
            CatConfig query = new CatConfig();
            query.setConfigKey(KEY_VISIT_VOID_MONTHS);
            List<CatConfig> list = catConfigMapper.selectCatConfigList(query);
            if (list != null && !list.isEmpty() && list.get(0).getConfigValue() != null)
            {
                return Integer.parseInt(list.get(0).getConfigValue().trim());
            }
        }
        catch (Exception ignored)
        {
        }
        return 3;
    }

    private Date monthsAgo(int months)
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -Math.max(months, 1));
        return c.getTime();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCatAdoptionByIds(Long[] adoptionIds)
    {
        deleteImpactService.cascadeBeforeDelete("adoption", adoptionIds);
        return catAdoptionMapper.deleteCatAdoptionByIds(adoptionIds);
    }

    /**
     * 创建回访计划
     */
    private void createVisitPlans(CatAdoption adoption)
    {
        createVisit(adoption, "3天", addDays(adoption.getAdoptionDate(), 3));
        createVisit(adoption, "1个月", addMonths(adoption.getAdoptionDate(), 1));
        createVisit(adoption, "3个月", addMonths(adoption.getAdoptionDate(), 3));
    }

    private void createVisit(CatAdoption adoption, String planType, Date planDate)
    {
        CatVisit visit = new CatVisit();
        visit.setAdoptionId(adoption.getAdoptionId());
        visit.setCatId(adoption.getCatId());
        visit.setPlanType(planType);
        visit.setPlanDate(planDate);
        visit.setVisitStatus("0"); // 待回访
        catVisitMapper.insertCatVisit(visit);
    }

    private Date addDays(Date date, int days)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, days);
        return c.getTime();
    }

    private Date addMonths(Date date, int months)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, months);
        return c.getTime();
    }
}
