package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatAdoption;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatVisit;
import com.ruoyi.cat.mapper.CatAdoptionMapper;
import com.ruoyi.cat.mapper.CatVisitMapper;
import com.ruoyi.cat.service.ICatAdoptionService;
import com.ruoyi.cat.service.ICatTransferService;
import java.util.Calendar;
import java.util.Date;

/**
 * 领养登记 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatAdoptionServiceImpl implements ICatAdoptionService
{
    @Autowired
    private CatAdoptionMapper catAdoptionMapper;

    @Autowired
    private CatVisitMapper catVisitMapper;

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
    public int deleteCatAdoptionByIds(Long[] adoptionIds)
    {
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
