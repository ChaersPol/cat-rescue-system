package com.ruoyi.cat.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.mapper.CatFlowMapper;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.service.ICatTransferService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 猫咪流转核心业务服务实现
 * 
 * @author ruoyi
 */
@Service
public class CatTransferServiceImpl implements ICatTransferService
{
    @Autowired
    private CatProfileMapper catProfileMapper;

    @Autowired
    private CatFlowMapper catFlowMapper;

    @Override
    @Transactional
    public int transfer(CatProfile profile, CatFlow flow)
    {
        // 1. 更新猫咪状态与所在基地/病房
        CatProfile update = new CatProfile();
        update.setCatId(profile.getCatId());
        update.setStatus(profile.getStatus());
        update.setBaseId(profile.getBaseId());
        update.setWardId(profile.getWardId());
        update.setHospitalName(profile.getHospitalName());
        update.setArchived(profile.getArchived());
        catProfileMapper.updateCatProfile(update);

        // 已领养/已离世：猫咪离开基地，清空所在基地与病房
        if ("5".equals(profile.getStatus()) || "6".equals(profile.getStatus()))
        {
            catProfileMapper.clearCatLocation(profile.getCatId());
        }
        // 外送医院治疗中 / 已放归：人不在病房，清空病房（保留归属基地）
        else if ("3".equals(profile.getStatus()) || "4".equals(profile.getStatus()))
        {
            catProfileMapper.clearCatWard(profile.getCatId());
        }

        // 非外送状态时清空外送医院名称（如医院治愈返回、治愈返回基地）
        if (profile.getStatus() != null && !"3".equals(profile.getStatus()))
        {
            catProfileMapper.clearCatHospital(profile.getCatId());
        }

        // 2. 写入流转记录（强制，不可删除）
        flow.setCatId(profile.getCatId());
        if (flow.getOperateBy() == null || flow.getOperateBy().isEmpty())
        {
            flow.setOperateBy(SecurityUtils.getUsername());
        }
        if (flow.getOperateTime() == null)
        {
            flow.setOperateTime(new Date());
        }
        return catFlowMapper.insertCatFlow(flow);
    }

    @Override
    @Transactional
    public int transferByFlow(CatFlow flow)
    {
        if (flow.getCatId() == null)
        {
            throw new ServiceException("请选择需要流转的猫咪");
        }
        CatProfile profile = catProfileMapper.selectCatProfileById(flow.getCatId());
        if (profile == null)
        {
            throw new ServiceException("猫咪档案不存在");
        }

        // 记录转出位置（前端未传时自动带入当前所在位置）
        if (flow.getFromBaseId() == null)
        {
            flow.setFromBaseId(profile.getBaseId());
        }
        if (flow.getFromWardId() == null)
        {
            flow.setFromWardId(profile.getWardId());
        }

        String type = flow.getFlowType() == null ? "" : flow.getFlowType();

        CatProfile update = new CatProfile();
        update.setCatId(profile.getCatId());
        update.setStatus(profile.getStatus());
        update.setBaseId(profile.getBaseId());
        update.setWardId(profile.getWardId());
        update.setHospitalName(profile.getHospitalName());
        update.setArchived(profile.getArchived());

        boolean clearWard = false;
        boolean clearLocation = false;

        if (type.contains("外送"))
        {
            // 外送医院：状态=外送医院治疗中，人不在病房，记录医院名称
            update.setStatus("3");
            update.setHospitalName(flow.getHospitalName());
            clearWard = true;
        }
        else if (type.contains("医院返回"))
        {
            // 医院返回：状态回到在养，回到指定基地/病房
            update.setStatus("1");
            update.setBaseId(flow.getToBaseId() != null ? flow.getToBaseId() : profile.getOwnerBaseId());
            update.setWardId(flow.getToWardId());
        }
        else if (type.contains("死亡"))
        {
            update.setStatus("6");
            update.setArchived("1");
            clearLocation = true;
        }
        else if (type.contains("领养"))
        {
            update.setStatus("5");
            update.setArchived("1");
            clearLocation = true;
        }
        else if (type.contains("放归"))
        {
            update.setStatus("4");
            update.setBaseId(flow.getToBaseId());
            clearWard = true;
        }
        else if (type.contains("姑息"))
        {
            update.setStatus("8");
            update.setBaseId(flow.getToBaseId() != null ? flow.getToBaseId() : profile.getBaseId());
            update.setWardId(flow.getToWardId());
        }
        else if (type.contains("发病") || type.contains("治疗"))
        {
            update.setStatus("2");
            update.setBaseId(flow.getToBaseId() != null ? flow.getToBaseId() : profile.getBaseId());
            update.setWardId(flow.getToWardId());
        }
        else if (type.contains("入站") || type.contains("治愈返回"))
        {
            update.setStatus("1");
            update.setBaseId(flow.getToBaseId() != null ? flow.getToBaseId() : profile.getBaseId());
            update.setWardId(flow.getToWardId());
        }
        else
        {
            if (flow.getToBaseId() != null)
            {
                update.setBaseId(flow.getToBaseId());
            }
            if (flow.getToWardId() != null)
            {
                update.setWardId(flow.getToWardId());
            }
        }

        int rows = transfer(update, flow);

        // 清空不适用位置时使用专用语句（动态更新无法写入 null）
        if (clearLocation)
        {
            catProfileMapper.clearCatLocation(profile.getCatId());
        }
        else if (clearWard)
        {
            catProfileMapper.clearCatWard(profile.getCatId());
        }
        return rows;
    }
}
