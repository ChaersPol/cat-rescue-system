package com.ruoyi.cat.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.mapper.CatFlowMapper;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.service.ICatStaffService;
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

    @Autowired
    private ICatStaffService staffService;

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
            // 原来写的是登录账号（admin / assist01），跟其它页面的「姓名」对不上；
            // 统一记昵称，没有昵称才退回登录名
            String staffName = staffService.currentStaffName();
            flow.setOperateBy(staffName != null ? staffName : SecurityUtils.getUsername());
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
            // 出院分两步（2026-11-07 定）：
            //   第一步只定"回哪个基地"，病房允许空 —— 这种状态单独标成「待安置」，
            //   否则状态显示「在养」却查不到房间，实际会出现"数据正常、找不到猫"。
            //   第二步再用「基地内安置」补录具体病房。
            update.setBaseId(flow.getToBaseId() != null ? flow.getToBaseId() : profile.getOwnerBaseId());
            update.setWardId(flow.getToWardId());
            update.setStatus(flow.getToWardId() != null ? "1" : "9");
        }
        else if (type.contains("安置"))
        {
            // 基地内安置：把待安置的猫放进具体房间，状态回到在养
            update.setStatus("1");
            update.setBaseId(flow.getToBaseId() != null ? flow.getToBaseId() : profile.getBaseId());
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
