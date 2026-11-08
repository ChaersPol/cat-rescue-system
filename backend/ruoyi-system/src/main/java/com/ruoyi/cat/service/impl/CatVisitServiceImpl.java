package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatAdoption;
import com.ruoyi.cat.domain.CatVisit;
import com.ruoyi.cat.mapper.CatAdoptionMapper;
import com.ruoyi.cat.mapper.CatVisitMapper;
import com.ruoyi.cat.service.ICatStaffService;
import com.ruoyi.cat.service.ICatVisitService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 回访记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatVisitServiceImpl implements ICatVisitService
{
    @Autowired
    private CatVisitMapper visitIdMapper;

    @Autowired
    private CatAdoptionMapper catAdoptionMapper;

    @Autowired
    private ICatStaffService staffService;

    @Override
    public CatVisit selectCatVisitById(Long visitId)
    {
        return visitIdMapper.selectCatVisitById(visitId);
    }

    @Override
    public List<CatVisit> selectCatVisitList(CatVisit visitId)
    {
        return visitIdMapper.selectCatVisitList(visitId);
    }

    @Override
    public int insertCatVisit(CatVisit visitId)
    {
        // 回访人通常是助理/回访专员：本人录入记本人，管理员从助理账号里选。
        // 不做必填：领养登记时会自动生成 3 条待回访计划，那时还没分配给谁
        visitId.setVisitBy(staffService.requireForInsert("回访人", ICatStaffService.SCOPE_ASSISTANT,
                visitId.getVisitBy(), false));
        // 手动新增时常忘了关联领养记录，这里按猫补一次，回访才和领养对得上
        CatAdoption adoption = bindAdoption(visitId);
        if (adoption != null && "1".equals(adoption.getVisitClosed()))
        {
            throw new ServiceException("该领养记录已标记「回访已结束」，如需继续回访请先在领养台账里重新开启");
        }
        return visitIdMapper.insertCatVisit(visitId);
    }

    @Override
    public int updateCatVisit(CatVisit visitId)
    {
        visitId.setVisitBy(staffService.normalizeForUpdate(ICatStaffService.SCOPE_ASSISTANT,
                visitId.getVisitBy(), false));
        CatVisit old = visitId.getVisitId() == null ? null : visitIdMapper.selectCatVisitById(visitId.getVisitId());
        // 把某条「已结束」的计划改回待回访，等于要继续回访：领养记录上的开关同步打开，
        // 否则列表显示「回访已结束」却还挂着待回访，两处对不上
        if (old != null && "4".equals(old.getVisitStatus()) && "0".equals(visitId.getVisitStatus())
                && old.getAdoptionId() != null)
        {
            reopenAdoption(old.getAdoptionId());
        }
        if (visitId.getAdoptionId() == null)
        {
            // 前端只提交表单里有的字段，adoptionId 不传时保住原值，没有原值才按猫补
            if (old != null && old.getAdoptionId() != null)
            {
                visitId.setAdoptionId(old.getAdoptionId());
            }
            else
            {
                bindAdoption(visitId);
            }
        }
        return visitIdMapper.updateCatVisit(visitId);
    }

    @Override
    public int deleteCatVisitByIds(Long[] visitIds)
    {
        return visitIdMapper.deleteCatVisitByIds(visitIds);
    }

    /**
     * 回访记录没带领养 id 时，按猫取最近一次领养补上
     */
    private CatAdoption bindAdoption(CatVisit visit)
    {
        if (visit.getAdoptionId() != null)
        {
            return catAdoptionMapper.selectCatAdoptionById(visit.getAdoptionId());
        }
        if (visit.getCatId() == null)
        {
            return null;
        }
        CatAdoption query = new CatAdoption();
        query.setCatId(visit.getCatId());
        List<CatAdoption> list = catAdoptionMapper.selectCatAdoptionList(query);
        if (list == null || list.isEmpty())
        {
            return null;
        }
        CatAdoption latest = list.get(0);
        visit.setAdoptionId(latest.getAdoptionId());
        return latest;
    }

    private void reopenAdoption(Long adoptionId)
    {
        catAdoptionMapper.updateVisitClosed(adoptionId, "0", null, null);
    }
}
