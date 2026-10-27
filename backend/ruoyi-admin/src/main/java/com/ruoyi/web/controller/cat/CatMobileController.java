package com.ruoyi.web.controller.cat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.cat.domain.CatReminder;
import com.ruoyi.cat.service.ICatReminderService;
import com.ruoyi.system.service.ISysRoleService;
import org.springframework.web.bind.annotation.PutMapping;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatConfig;
import com.ruoyi.cat.domain.CatDiagnosis;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatReferral;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatDeath;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.mapper.CatDiagnosisMapper;
import com.ruoyi.cat.mapper.CatMedicationMapper;
import com.ruoyi.cat.mapper.CatVaccineMapper;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.mapper.CatReferralMapper;
import com.ruoyi.cat.mapper.CatFlowMapper;
import com.ruoyi.cat.mapper.CatDeathMapper;
import com.ruoyi.cat.mapper.CatConfigMapper;

/**
 * 移动端扫码档案查询控制器（免登录）
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/mobile")
public class CatMobileController
{
    @Autowired
    private CatProfileMapper catProfileMapper;
    @Autowired
    private CatDiagnosisMapper catDiagnosisMapper;
    @Autowired
    private CatMedicationMapper catMedicationMapper;
    @Autowired
    private CatVaccineMapper catVaccineMapper;
    @Autowired
    private CatDewormMapper catDewormMapper;
    @Autowired
    private CatReferralMapper catReferralMapper;
    @Autowired
    private CatFlowMapper catFlowMapper;
    @Autowired
    private CatDeathMapper catDeathMapper;
    @Autowired
    private CatConfigMapper catConfigMapper;
    @Autowired
    private ICatReminderService catReminderService;
    @Autowired
    private ISysRoleService roleService;

    /**
     * 读取扫码权限方案：A=公开模式（仅基础信息），B=内部模式（含全部病历）
     */
    private String getScanPermissionMode()
    {
        CatConfig config = catConfigMapper.selectCatConfigByKey("scan.permission.mode");
        if (config != null && config.getConfigValue() != null && !config.getConfigValue().isEmpty())
        {
            return config.getConfigValue().trim();
        }
        return "B";
    }

    /**
     * 扫码查询猫咪档案 + 病历时间线（匿名访问）
     */
    @Anonymous
    @GetMapping("/profile/{archiveNo}")
    public AjaxResult getProfileByArchiveNo(@PathVariable("archiveNo") String archiveNo)
    {
        CatProfile query = new CatProfile();
        query.setArchiveNo(archiveNo);
        List<CatProfile> profiles = catProfileMapper.selectCatProfileList(query);
        if (profiles == null || profiles.isEmpty())
        {
            return AjaxResult.error("未找到该猫咪档案");
        }
        CatProfile profile = profiles.get(0);
        Long catId = profile.getCatId();

        Map<String, Object> result = new HashMap<>();
        result.put("profile", profile);

        // 扫码权限方案：A 模式仅返回基础信息，医疗病历需登录后台查看
        String scanMode = getScanPermissionMode();
        result.put("scanMode", scanMode);
        if ("A".equalsIgnoreCase(scanMode))
        {
            result.put("needLogin", true);
            result.put("timeline", new ArrayList<>());
            return AjaxResult.success(result);
        }
        result.put("needLogin", false);

        // 病历时间线（按时间倒序合并）
        List<Map<String, Object>> timeline = new ArrayList<>();

        // 诊断记录
        CatDiagnosis diagnosisQuery = new CatDiagnosis();
        diagnosisQuery.setCatId(catId);
        for (CatDiagnosis d : catDiagnosisMapper.selectCatDiagnosisList(diagnosisQuery))
        {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "诊断");
            item.put("time", d.getVisitDate());
            item.put("content", d.getSymptoms());
            item.put("doctor", d.getDoctor());
            item.put("result", d.getDiagnosisResult());
            timeline.add(item);
        }

        // 用药记录
        CatMedication medQuery = new CatMedication();
        medQuery.setCatId(catId);
        for (CatMedication m : catMedicationMapper.selectCatMedicationList(medQuery))
        {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "用药");
            item.put("time", m.getUseDate());
            item.put("content", m.getDrugName() + " " + (m.getDosage() == null ? "" : m.getDosage()));
            item.put("doctor", m.getExecutor());
            timeline.add(item);
        }

        // 疫苗记录
        CatVaccine vaccineQuery = new CatVaccine();
        vaccineQuery.setCatId(catId);
        for (CatVaccine v : catVaccineMapper.selectCatVaccineList(vaccineQuery))
        {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "疫苗");
            item.put("time", v.getVaccineDate());
            item.put("content", v.getVaccineType());
            item.put("doctor", v.getResponsible());
            timeline.add(item);
        }

        // 驱虫记录
        CatDeworm dewormQuery = new CatDeworm();
        dewormQuery.setCatId(catId);
        for (CatDeworm dw : catDewormMapper.selectCatDewormList(dewormQuery))
        {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "驱虫");
            item.put("time", dw.getDewormDate());
            item.put("content", dw.getDewormDrug());
            timeline.add(item);
        }

        // 转诊记录
        CatReferral referralQuery = new CatReferral();
        referralQuery.setCatId(catId);
        for (CatReferral r : catReferralMapper.selectCatReferralList(referralQuery))
        {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "转诊");
            item.put("time", r.getSendDate());
            item.put("content", r.getHospitalName() + " " + (r.getHospitalDiagnosis() == null ? "" : r.getHospitalDiagnosis()));
            timeline.add(item);
        }

        // 流转记录
        CatFlow flowQuery = new CatFlow();
        flowQuery.setCatId(catId);
        for (CatFlow f : catFlowMapper.selectCatFlowList(flowQuery))
        {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "流转");
            item.put("time", f.getOperateTime());
            item.put("content", f.getFlowType() + "：" + (f.getFlowReason() == null ? "" : f.getFlowReason()));
            item.put("doctor", f.getOperateBy());
            timeline.add(item);
        }

        // 死亡记录
        CatDeath deathQuery = new CatDeath();
        deathQuery.setCatId(catId);
        for (CatDeath death : catDeathMapper.selectCatDeathList(deathQuery))
        {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "死亡");
            item.put("time", death.getDeathDate());
            item.put("content", death.getDeathReason());
            timeline.add(item);
        }

        // 按时间倒序排序
        timeline.sort((a, b) -> {
            Object ta = a.get("time");
            Object tb = b.get("time");
            if (ta == null) return 1;
            if (tb == null) return -1;
            return String.valueOf(tb).compareTo(String.valueOf(ta));
        });

        result.put("timeline", timeline);
        return AjaxResult.success(result);
    }

    /**
     * 移动端首页：当前账号的聚合提醒列表（需求文档 12）
     * <p>
     * 按岗位定向派发规则返回与自己相关的提醒，已读状态按账号独立。
     */
    @GetMapping("/reminders")
    public AjaxResult reminders()
    {
        CatReminder scope = currentReminderScope();
        List<CatReminder> list = catReminderService.selectHeaderReminders(scope, 100);
        AjaxResult result = AjaxResult.success(list);
        result.put("unreadCount", catReminderService.selectUnreadCount(scope));
        return result;
    }

    /**
     * 移动端首页：标记单条提醒已读
     */
    @PutMapping("/reminders/read/{reminderId}")
    public AjaxResult markReminderRead(@PathVariable("reminderId") Long reminderId)
    {
        catReminderService.markRead(reminderId, SecurityUtils.getUserId());
        return AjaxResult.success();
    }

    /**
     * 移动端首页：全部标记已读
     */
    @PutMapping("/reminders/readAll")
    public AjaxResult markAllReminderRead()
    {
        int count = catReminderService.markAllRead(currentReminderScope());
        return AjaxResult.success("已标记 " + count + " 条为已读");
    }

    /**
     * 构造当前登录用户的提醒查询范围
     */
    private CatReminder currentReminderScope()
    {
        Long userId = SecurityUtils.getUserId();
        CatReminder scope = new CatReminder();
        scope.setCurrentUserId(userId);
        scope.setCurrentUserName(SecurityUtils.getUsername());
        scope.setScopeAll(hasFullDataScope(userId));
        return scope;
    }

    /**
     * 是否具备全站数据权限（超管或「全部数据权限」角色）
     */
    private boolean hasFullDataScope(Long userId)
    {
        if (SecurityUtils.isAdmin(userId))
        {
            return true;
        }
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        if (roles != null)
        {
            for (SysRole role : roles)
            {
                if ("1".equals(role.getDataScope()))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
