package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatReferral;
import com.ruoyi.cat.mapper.CatReferralMapper;
import com.ruoyi.cat.service.ICatReferralService;
import com.ruoyi.cat.service.ICatTransferService;

/**
 * 外送医院转诊记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatReferralServiceImpl implements ICatReferralService
{
    @Autowired
    private CatReferralMapper catReferralMapper;

    @Autowired
    private ICatTransferService catTransferService;

    @Override
    public CatReferral selectCatReferralById(Long referralId)
    {
        return catReferralMapper.selectCatReferralById(referralId);
    }

    @Override
    public List<CatReferral> selectCatReferralList(CatReferral catReferral)
    {
        return catReferralMapper.selectCatReferralList(catReferral);
    }

    @Override
    @Transactional
    public int insertCatReferral(CatReferral catReferral)
    {
        // 新建转诊时，猫咪状态设为外送医院治疗中
        int rows = catReferralMapper.insertCatReferral(catReferral);
        if (rows > 0 && catReferral.getCatId() != null)
        {
            CatProfile profile = new CatProfile();
            profile.setCatId(catReferral.getCatId());
            profile.setStatus("3"); // 外送医院治疗中
            profile.setHospitalName(catReferral.getHospitalName());

            CatFlow flow = new CatFlow();
            flow.setFlowType("外送医院");
            flow.setFlowReason("外送合作医院：" + catReferral.getHospitalName());
            flow.setHospitalName(catReferral.getHospitalName());

            catTransferService.transfer(profile, flow);
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateCatReferral(CatReferral catReferral)
    {
        int rows = catReferralMapper.updateCatReferral(catReferral);
        // 根据出院去向联动流转
        if (rows > 0 && catReferral.getCatId() != null)
        {
            String dischargeTo = catReferral.getDischargeTo();
            String result = catReferral.getResult();
            if (dischargeTo != null && !dischargeTo.isEmpty())
            {
                CatProfile profile = new CatProfile();
                profile.setCatId(catReferral.getCatId());
                CatFlow flow = new CatFlow();
                flow.setHospitalName(catReferral.getHospitalName());

                if ("转入指定基地".equals(dischargeTo) || "转入指定基地保守照料".equals(dischargeTo))
                {
                    // 转入指定基地长期保守照料
                    profile.setStatus("8"); // 姑息照料
                    profile.setBaseId(catReferral.getTargetBaseId());
                    profile.setHospitalName("");
                    flow.setFlowType("转入姑息");
                    flow.setFlowReason("医院评估无法治愈，转入基地长期保守照料");
                }
                else if ("返回医疗基地".equals(dischargeTo))
                {
                    profile.setStatus("2"); // 基地内治疗中
                    profile.setHospitalName("");
                    flow.setFlowType("返回医疗基地");
                    flow.setFlowReason("医院治愈出院，返回医疗基地");
                }
                else if ("返回原归属基地".equals(dischargeTo))
                {
                    profile.setStatus("1"); // 在养
                    profile.setHospitalName("");
                    flow.setFlowType("返回原基地");
                    flow.setFlowReason("医院治愈出院，返回原归属基地");
                }

                catTransferService.transfer(profile, flow);
            }
            else if ("安乐".equals(result) || "离世".equals(result))
            {
                // 安乐/离世：状态改为已离世并归档
                CatProfile profile = new CatProfile();
                profile.setCatId(catReferral.getCatId());
                profile.setStatus("6");
                profile.setArchived("1");
                CatFlow flow = new CatFlow();
                flow.setFlowType("死亡");
                flow.setFlowReason("医院" + result);
                flow.setHospitalName(catReferral.getHospitalName());
                catTransferService.transfer(profile, flow);
            }
        }
        return rows;
    }

    @Override
    public int deleteCatReferralByIds(Long[] referralIds)
    {
        return catReferralMapper.deleteCatReferralByIds(referralIds);
    }
}
