package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatRelease;
import com.ruoyi.cat.mapper.CatReleaseMapper;
import com.ruoyi.cat.service.ICatReleaseService;
import com.ruoyi.cat.service.ICatTransferService;
import com.ruoyi.common.utils.StringUtils;

/**
 * TNR放归登记 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatReleaseServiceImpl implements ICatReleaseService
{
    @Autowired
    private CatReleaseMapper catReleaseMapper;

    @Autowired
    private ICatTransferService catTransferService;

    @Override
    public CatRelease selectCatReleaseById(Long releaseId)
    {
        return catReleaseMapper.selectCatReleaseById(releaseId);
    }

    @Override
    public List<CatRelease> selectCatReleaseList(CatRelease catRelease)
    {
        return catReleaseMapper.selectCatReleaseList(catRelease);
    }

    @Override
    @Transactional
    public int insertCatRelease(CatRelease catRelease)
    {
        // 标记状态固定为「已放归」：前端未传或传空时兜底，
        // 否则 <if test="marked != null"> 会把空串写进去、覆盖掉表上的默认值 '1'
        if (StringUtils.isEmpty(catRelease.getMarked()))
        {
            catRelease.setMarked("1");
        }
        int rows = catReleaseMapper.insertCatRelease(catRelease);
        // 放归后：猫咪状态变更为已放归，归档
        if (rows > 0 && catRelease.getCatId() != null)
        {
            CatProfile profile = new CatProfile();
            profile.setCatId(catRelease.getCatId());
            profile.setStatus("4"); // 已放归
            profile.setArchived("1"); // 归档

            CatFlow flow = new CatFlow();
            flow.setFlowType("放归");
            flow.setFlowReason("TNR放归（放归点位：" + catRelease.getReleasePoint() + "）");

            catTransferService.transfer(profile, flow);
        }
        return rows;
    }

    @Override
    public int updateCatRelease(CatRelease catRelease)
    {
        return catReleaseMapper.updateCatRelease(catRelease);
    }

    @Override
    public int deleteCatReleaseByIds(Long[] releaseIds)
    {
        return catReleaseMapper.deleteCatReleaseByIds(releaseIds);
    }
}
