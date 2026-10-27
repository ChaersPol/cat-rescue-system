package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatDeath;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.mapper.CatDeathMapper;
import com.ruoyi.cat.service.ICatDeathService;
import com.ruoyi.cat.service.ICatTransferService;

/**
 * 死亡台账 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatDeathServiceImpl implements ICatDeathService
{
    @Autowired
    private CatDeathMapper catDeathMapper;

    @Autowired
    private ICatTransferService catTransferService;

    @Override
    public CatDeath selectCatDeathById(Long deathId)
    {
        return catDeathMapper.selectCatDeathById(deathId);
    }

    @Override
    public List<CatDeath> selectCatDeathList(CatDeath catDeath)
    {
        return catDeathMapper.selectCatDeathList(catDeath);
    }

    @Override
    @Transactional
    public int insertCatDeath(CatDeath catDeath)
    {
        int rows = catDeathMapper.insertCatDeath(catDeath);
        // 提交后猫咪状态自动变更为已离世，自动归档
        if (rows > 0 && catDeath.getCatId() != null)
        {
            CatProfile profile = new CatProfile();
            profile.setCatId(catDeath.getCatId());
            profile.setStatus("6"); // 已离世
            profile.setArchived("1"); // 归档

            CatFlow flow = new CatFlow();
            flow.setFlowType("死亡");
            flow.setFlowReason("死亡登记（" + deathTypeName(catDeath.getDeathType()) + "）");

            catTransferService.transfer(profile, flow);
        }
        return rows;
    }

    @Override
    public int updateCatDeath(CatDeath catDeath)
    {
        return catDeathMapper.updateCatDeath(catDeath);
    }

    @Override
    public int deleteCatDeathByIds(Long[] deathIds)
    {
        return catDeathMapper.deleteCatDeathByIds(deathIds);
    }

    private String deathTypeName(String type)
    {
        if (type == null) return "未知";
        switch (type)
        {
            case "1": return "自然寿终";
            case "2": return "病重不治";
            case "3": return "安乐";
            case "4": return "意外";
            default: return "未知";
        }
    }
}
