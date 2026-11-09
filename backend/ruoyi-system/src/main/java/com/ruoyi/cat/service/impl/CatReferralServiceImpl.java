package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatBase;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatReferral;
import com.ruoyi.cat.domain.CatWard;
import com.ruoyi.cat.mapper.CatBaseMapper;
import com.ruoyi.cat.mapper.CatFlowMapper;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.mapper.CatReferralMapper;
import com.ruoyi.cat.mapper.CatWardMapper;
import com.ruoyi.cat.service.ICatReferralService;
import com.ruoyi.cat.service.ICatTransferService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 外送医院转诊记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatReferralServiceImpl implements ICatReferralService
{
    /** 猫咪状态：外送医院治疗中 */
    private static final String STATUS_OUT = "3";

    /** 猫咪状态：待安置（已回到基地但还没分配病房） */
    private static final String STATUS_PENDING_WARD = "9";

    @Autowired
    private CatReferralMapper catReferralMapper;

    @Autowired
    private CatProfileMapper catProfileMapper;

    @Autowired
    private CatFlowMapper catFlowMapper;

    @Autowired
    private CatWardMapper catWardMapper;

    @Autowired
    private CatBaseMapper catBaseMapper;

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
            CatProfile current = catProfileMapper.selectCatProfileById(catReferral.getCatId());

            CatProfile profile = new CatProfile();
            profile.setCatId(catReferral.getCatId());
            profile.setStatus(STATUS_OUT); // 外送医院治疗中
            profile.setHospitalName(catReferral.getHospitalName());

            CatFlow flow = new CatFlow();
            flow.setFlowType("外送医院");
            flow.setFlowReason("外送合作医院：" + catReferral.getHospitalName());
            flow.setHospitalName(catReferral.getHospitalName());
            if (current != null)
            {
                flow.setFromBaseId(current.getBaseId());
                // 关键：外送会把病房清空，这里必须把离开的病房记进流转记录，
                // 否则出院返院时无从得知猫原来住哪间屋，只能变成"在养但没病房"。
                flow.setFromWardId(current.getWardId());
            }

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
            CatProfile current = catProfileMapper.selectCatProfileById(catReferral.getCatId());

            String dischargeTo = catReferral.getDischargeTo();
            String result = catReferral.getResult();

            if (dischargeTo != null && !dischargeTo.isEmpty())
            {
                applyDischarge(catReferral, current);
            }
            else if ("安乐".equals(result) || "离世".equals(result))
            {
                // 安乐/离世：状态改为已离世并归档
                if (isTerminal(current))
                {
                    // 猫已经放归/领养/离世，重存旧转诊记录不能再动它的状态
                    return rows;
                }
                CatProfile profile = new CatProfile();
                profile.setCatId(catReferral.getCatId());
                profile.setStatus("6");
                profile.setArchived("1");

                CatFlow flow = new CatFlow();
                flow.setFlowType("死亡");
                flow.setFlowReason("医院" + result);
                flow.setHospitalName(catReferral.getHospitalName());
                flow.setHospitalResult(result);
                if (current != null)
                {
                    flow.setFromBaseId(current.getBaseId());
                    flow.setFromWardId(current.getWardId());
                }
                catTransferService.transfer(profile, flow);
            }
        }
        return rows;
    }

    /**
     * 出院登记 → 更新猫咪状态与所在基地/病房。
     *
     * 必须和外送时的处理对称：外送会清空病房（人不在站内），出院返院时就得把病房还原回来。
     * 原来这里只改状态不改病房，外送一次再返院就会产生"状态在养、病房为空"的猫。
     *
     * 还原不到可用病房时不再假装「在养/治疗中」，统一记为「待安置」(9)，
     * 与「流转记录」页 医院返回 → 待安置 → 基地内安置 的口径保持一致。
     */
    private void applyDischarge(CatReferral referral, CatProfile current)
    {
        if (current == null || !STATUS_OUT.equals(current.getStatus()))
        {
            // 只有"正在外送治疗中"的猫才会因为出院登记改状态。
            // 回头修改早已出院的旧记录时保持猫咪现状，免得把猫又搬一次家（甚至搬回已离开的基地）。
            return;
        }

        String dischargeTo = referral.getDischargeTo();
        Long targetBaseId;
        String targetStatus;
        String flowType;
        String flowReason;

        if ("转入指定基地".equals(dischargeTo) || "转入指定基地保守照料".equals(dischargeTo))
        {
            if (referral.getTargetBaseId() == null)
            {
                throw new ServiceException("请选择转入基地后再提交出院登记");
            }
            targetBaseId = referral.getTargetBaseId();
            targetStatus = "8"; // 姑息照料
            flowType = "转入姑息";
            flowReason = "医院评估无法治愈，转入基地长期保守照料";
        }
        else if ("返回医疗基地".equals(dischargeTo))
        {
            targetBaseId = resolveMedicalBaseId(current);
            targetStatus = "2"; // 基地内治疗中
            flowType = "返回医疗基地";
            flowReason = "医院治愈出院，返回医疗基地";
        }
        else if ("返回原归属基地".equals(dischargeTo))
        {
            targetBaseId = current.getOwnerBaseId() != null ? current.getOwnerBaseId() : current.getBaseId();
            if (targetBaseId == null)
            {
                throw new ServiceException("这只猫没有登记归属基地，无法「返回原归属基地」，请改用「转入指定基地」");
            }
            targetStatus = "1"; // 在养
            flowType = "返回原基地";
            flowReason = "医院治愈出院，返回原归属基地";
        }
        else
        {
            // 字典里有、这里没有对应分支的值：不动状态，避免写出没有流转类型的脏记录
            return;
        }

        Long targetWardId = resolveReturnWardId(current, targetBaseId);
        String status = targetWardId != null ? targetStatus : STATUS_PENDING_WARD;
        String reason = targetWardId != null
                ? flowReason
                : flowReason + "；已回到基地但还没有分配病房，状态记为「待安置」，请到档案里安排具体病房";

        CatProfile profile = new CatProfile();
        profile.setCatId(referral.getCatId());
        profile.setStatus(status);
        profile.setBaseId(targetBaseId);
        profile.setWardId(targetWardId);
        profile.setHospitalName("");

        CatFlow flow = new CatFlow();
        flow.setFlowType(flowType);
        flow.setFlowReason(reason);
        flow.setHospitalName(referral.getHospitalName());
        flow.setHospitalResult(referral.getResult());
        flow.setFromBaseId(current.getBaseId());
        flow.setFromWardId(current.getWardId());
        flow.setToBaseId(targetBaseId);
        flow.setToWardId(targetWardId);

        catTransferService.transfer(profile, flow);
    }

    /**
     * 出院返院时该把猫放回哪间病房：
     *   1. 猫现在还有病房（没被外送清空）→ 优先用它，但不覆盖重存旧记录时的现状；
     *   2. 正在外送中（病房已清空）→ 取最近一次外送记录里的转出病房；
     *   3. 那间病房已删除、已停用或不属于目标基地 → 视为没有，返回 null（调用方记「待安置」）。
     */
    private Long resolveReturnWardId(CatProfile current, Long targetBaseId)
    {
        Long wardId = current.getWardId();
        if (wardId == null)
        {
            if (!STATUS_OUT.equals(current.getStatus()))
            {
                return null;
            }
            // 外送时把病房清空了，只有流转记录里还留着"从哪间屋走的"
            wardId = catFlowMapper.selectLastOutWardId(current.getCatId());
            if (wardId == null)
            {
                return null;
            }
        }
        CatWard ward = catWardMapper.selectCatWardById(wardId);
        if (ward == null || "2".equals(ward.getDelFlag()) || !"0".equals(ward.getStatus()))
        {
            return null;
        }
        // 病房必须落在目标基地下，否则就是把猫塞进别人家的屋子
        if (targetBaseId != null && !targetBaseId.equals(ward.getBaseId()))
        {
            return null;
        }
        return wardId;
    }

    /**
     * 「返回医疗基地」的落地基地：猫本来就待在医疗基地就原地返回，否则取启用的医疗基地。
     */
    private Long resolveMedicalBaseId(CatProfile current)
    {
        CatBase query = new CatBase();
        query.setBaseType("2"); // 医疗基地
        query.setStatus("0");   // 正常
        List<CatBase> medicalBases = catBaseMapper.selectCatBaseList(query);
        if (medicalBases == null || medicalBases.isEmpty())
        {
            throw new ServiceException("还没有启用的医疗基地，无法「返回医疗基地」，请先在基础信息里维护基地");
        }
        for (CatBase base : medicalBases)
        {
            if (base.getBaseId() != null && base.getBaseId().equals(current.getBaseId()))
            {
                return base.getBaseId();
            }
        }
        return medicalBases.get(0).getBaseId();
    }

    /** 是否已离开站内链路（已放归/已领养/已离世），这几种状态不允许被转诊记录再改回去 */
    private boolean isTerminal(CatProfile profile)
    {
        if (profile == null || profile.getStatus() == null)
        {
            return false;
        }
        String status = profile.getStatus();
        return "4".equals(status) || "5".equals(status) || "6".equals(status);
    }

    @Override
    public int deleteCatReferralByIds(Long[] referralIds)
    {
        return catReferralMapper.deleteCatReferralByIds(referralIds);
    }
}
