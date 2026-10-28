package com.ruoyi.web.controller.cat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatPhoto;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatDiagnosis;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatReferral;
import com.ruoyi.cat.domain.CatDeath;
import com.ruoyi.cat.domain.CatTnr;
import com.ruoyi.cat.domain.CatRelease;
import com.ruoyi.cat.domain.CatAdoption;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.mapper.CatPhotoMapper;
import com.ruoyi.cat.mapper.CatFlowMapper;
import com.ruoyi.cat.mapper.CatDiagnosisMapper;
import com.ruoyi.cat.mapper.CatMedicationMapper;
import com.ruoyi.cat.mapper.CatVaccineMapper;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.mapper.CatReferralMapper;
import com.ruoyi.cat.mapper.CatDeathMapper;
import com.ruoyi.cat.mapper.CatTnrMapper;
import com.ruoyi.cat.mapper.CatReleaseMapper;
import com.ruoyi.cat.mapper.CatAdoptionMapper;
import com.ruoyi.cat.service.ICatProfileService;
import com.ruoyi.cat.service.ICatWardAuthService;

/**
 * 猫咪档案 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/profile")
public class CatProfileController extends BaseController
{
    @Autowired
    private ICatProfileService catIdService;

    @Autowired
    private com.ruoyi.cat.service.CatArchivePdfService catArchivePdfService;

    @Autowired
    private ICatWardAuthService catWardAuthService;

    @Autowired
    private CatProfileMapper catProfileMapper;
    @Autowired
    private CatPhotoMapper catPhotoMapper;
    @Autowired
    private CatFlowMapper catFlowMapper;
    @Autowired
    private CatDiagnosisMapper catDiagnosisMapper;
    @Autowired
    private CatMedicationMapper catMedicationMapper;
    @Autowired
    private CatVaccineMapper vaccineMapper;
    @Autowired
    private CatDewormMapper dewormMapper;
    @Autowired
    private CatReferralMapper referralMapper;
    @Autowired
    private CatDeathMapper catDeathMapper;
    @Autowired
    private CatTnrMapper catTnrMapper;
    @Autowired
    private CatReleaseMapper catReleaseMapper;
    @Autowired
    private CatAdoptionMapper catAdoptionMapper;

    /**
     * 单猫完整档案（档案详情页用）
     * <p>
     * 一次性返回基础信息、标签、相册、流转轨迹，以及按时间倒序合并的
     * 诊断 / 用药 / 疫苗 / 驱虫 / 转诊 / 流转 / 死亡 / 领养 / TNR / 放归 时间线，
     * 对应需求文档 6.6「病历时间线展示」与 4.2「档案内可查看完整流转轨迹」。
     */
    @PreAuthorize("@ss.hasPermi('cat:profile:query')")
    @GetMapping("/archive/{catId}")
    public AjaxResult archive(@PathVariable("catId") Long catId)
    {
        CatProfile profile = catIdService.selectCatProfileById(catId);
        if (profile == null)
        {
            return AjaxResult.error("猫咪档案不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("profile", profile);
        result.put("tagNames", catProfileMapper.selectTagNamesByCatId(catId));
        result.put("tagIds", catProfileMapper.selectTagIdsByCatId(catId));

        // 相册（按文件夹分组由前端处理）
        CatPhoto photoQuery = new CatPhoto();
        photoQuery.setCatId(catId);
        result.put("photos", catPhotoMapper.selectCatPhotoList(photoQuery));

        // 流转轨迹
        CatFlow flowQuery = new CatFlow();
        flowQuery.setCatId(catId);
        List<CatFlow> flows = catFlowMapper.selectCatFlowList(flowQuery);
        result.put("flows", flows);

        // 领养、死亡、TNR、放归
        CatAdoption adoptionQuery = new CatAdoption();
        adoptionQuery.setCatId(catId);
        List<CatAdoption> adoptions = catAdoptionMapper.selectCatAdoptionList(adoptionQuery);
        result.put("adoption", adoptions.isEmpty() ? null : adoptions.get(0));

        CatDeath deathQuery = new CatDeath();
        deathQuery.setCatId(catId);
        List<CatDeath> deaths = catDeathMapper.selectCatDeathList(deathQuery);
        result.put("death", deaths.isEmpty() ? null : deaths.get(0));

        CatTnr tnrQuery = new CatTnr();
        tnrQuery.setCatId(catId);
        List<CatTnr> tnrs = catTnrMapper.selectCatTnrList(tnrQuery);
        result.put("tnr", tnrs.isEmpty() ? null : tnrs.get(0));

        CatRelease releaseQuery = new CatRelease();
        releaseQuery.setCatId(catId);
        List<CatRelease> releases = catReleaseMapper.selectCatReleaseList(releaseQuery);
        result.put("release", releases.isEmpty() ? null : releases.get(0));

        // 合并时间线
        result.put("timeline", buildTimeline(catId));
        return success(result);
    }

    /**
     * 构建病历时间线：所有诊疗与流转记录按时间倒序合并
     */
    private List<Map<String, Object>> buildTimeline(Long catId)
    {
        List<Map<String, Object>> timeline = new ArrayList<>();

        CatDiagnosis diagnosisQuery = new CatDiagnosis();
        diagnosisQuery.setCatId(catId);
        for (CatDiagnosis d : catDiagnosisMapper.selectCatDiagnosisList(diagnosisQuery))
        {
            Map<String, Object> item = newItem("诊断", d.getVisitDate(), d.getDoctor());
            item.put("title", d.getDiagnosisResult());
            item.put("content", d.getSymptoms());
            item.put("detail", d.getMedicalAdvice());
            item.put("checkItems", d.getCheckItems());
            item.put("photos", d.getCheckPhotos());
            item.put("correctionNote", d.getCorrectionNote());
            item.put("correctionBy", d.getCorrectionBy());
            item.put("correctionTime", d.getCorrectionTime());
            item.put("recordId", d.getDiagnosisId());
            timeline.add(item);
        }

        CatMedication medQuery = new CatMedication();
        medQuery.setCatId(catId);
        for (CatMedication m : catMedicationMapper.selectCatMedicationList(medQuery))
        {
            Map<String, Object> item = newItem("用药", m.getUseDate(), m.getExecutor());
            item.put("title", m.getDrugName() + (StringUtils.isEmpty(m.getDosage()) ? "" : " " + m.getDosage()));
            item.put("content", (m.getTimePeriod() == null ? "" : m.getTimePeriod()) + " " + (m.getAdministration() == null ? "" : m.getAdministration()));
            item.put("adverseReaction", m.getAdverseReaction());
            item.put("photos", m.getPhotos());
            item.put("recordId", m.getMedicationId());
            timeline.add(item);
        }

        CatVaccine vaccineQuery = new CatVaccine();
        vaccineQuery.setCatId(catId);
        for (CatVaccine v : vaccineMapper.selectCatVaccineList(vaccineQuery))
        {
            Map<String, Object> item = newItem("疫苗", v.getVaccineDate(), v.getResponsible());
            item.put("title", v.getVaccineType());
            item.put("detail", v.getNextDate() == null ? null : "下次接种：" + new SimpleDateFormat("yyyy-MM-dd").format(v.getNextDate()));
            item.put("recordId", v.getVaccineId());
            timeline.add(item);
        }

        CatDeworm dewormQuery = new CatDeworm();
        dewormQuery.setCatId(catId);
        for (CatDeworm dw : dewormMapper.selectCatDewormList(dewormQuery))
        {
            Map<String, Object> item = newItem("驱虫", dw.getDewormDate(), null);
            item.put("title", dw.getDewormDrug());
            item.put("detail", dw.getNextDate() == null ? null : "下次驱虫：" + new SimpleDateFormat("yyyy-MM-dd").format(dw.getNextDate()));
            item.put("recordId", dw.getDewormId());
            timeline.add(item);
        }

        CatReferral referralQuery = new CatReferral();
        referralQuery.setCatId(catId);
        for (CatReferral r : referralMapper.selectCatReferralList(referralQuery))
        {
            Map<String, Object> item = newItem("转诊", r.getSendDate(), r.getCreateBy());
            item.put("title", r.getHospitalName());
            item.put("content", r.getSendReason());
            item.put("detail", r.getHospitalDiagnosis());
            item.put("hospitalResult", r.getResult());
            item.put("photos", r.getFiles());
            item.put("recordId", r.getReferralId());
            timeline.add(item);
        }

        CatFlow flowQuery = new CatFlow();
        flowQuery.setCatId(catId);
        for (CatFlow f : catFlowMapper.selectCatFlowList(flowQuery))
        {
            Map<String, Object> item = newItem("流转", f.getOperateTime(), f.getOperateBy());
            item.put("title", f.getFlowType());
            item.put("content", f.getFlowReason());
            item.put("detail", f.getHospitalName());
            item.put("recordId", f.getFlowId());
            timeline.add(item);
        }

        CatDeath deathQuery = new CatDeath();
        deathQuery.setCatId(catId);
        for (CatDeath death : catDeathMapper.selectCatDeathList(deathQuery))
        {
            Map<String, Object> item = newItem("死亡", death.getDeathDate(), death.getDoctor());
            item.put("title", death.getDeathType());
            item.put("content", death.getDeathReason());
            item.put("detail", death.getBodyDisposal());
            item.put("photos", death.getPhotos());
            item.put("recordId", death.getDeathId());
            timeline.add(item);
        }

        CatTnr tnrQuery = new CatTnr();
        tnrQuery.setCatId(catId);
        for (CatTnr t : catTnrMapper.selectCatTnrList(tnrQuery))
        {
            Map<String, Object> item = newItem("TNR", t.getCaptureTime(), t.getSurgeon());
            item.put("title", "抓捕 / 绝育手术");
            item.put("content", t.getCapturePoint());
            item.put("detail", t.getSurgeryRecord());
            item.put("recordId", t.getTnrId());
            timeline.add(item);
        }

        CatRelease releaseQuery = new CatRelease();
        releaseQuery.setCatId(catId);
        for (CatRelease r : catReleaseMapper.selectCatReleaseList(releaseQuery))
        {
            Map<String, Object> item = newItem("放归", r.getReleaseDate(), r.getCreateBy());
            item.put("title", "放归 " + (r.getReleasePoint() == null ? "" : r.getReleasePoint()));
            item.put("content", r.getReleaseStatus());
            item.put("recordId", r.getReleaseId());
            timeline.add(item);
        }

        CatAdoption adoptionQuery = new CatAdoption();
        adoptionQuery.setCatId(catId);
        for (CatAdoption a : catAdoptionMapper.selectCatAdoptionList(adoptionQuery))
        {
            Map<String, Object> item = newItem("领养", a.getAdoptionDate(), a.getCreateBy());
            item.put("title", "领养人：" + a.getAdopterName());
            item.put("content", a.getAdopterPhone());
            item.put("detail", a.getAdopterAddress());
            item.put("recordId", a.getAdoptionId());
            timeline.add(item);
        }

        // 按时间倒序（空时间排最后）
        timeline.sort((a, b) -> {
            Object ta = a.get("time");
            Object tb = b.get("time");
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return String.valueOf(tb).compareTo(String.valueOf(ta));
        });
        return timeline;
    }

    private Map<String, Object> newItem(String type, Date time, String operator)
    {
        Map<String, Object> item = new HashMap<>();
        item.put("type", type);
        item.put("time", time);
        item.put("operator", operator);
        return item;
    }

    /**
     * 批量取二维码（二维码卡片打印用，列表接口不返回 qr_code 以控制响应体积）
     */
    @PreAuthorize("@ss.hasPermi('cat:profile:query')")
    @GetMapping("/qrcodes")
    public AjaxResult qrcodes(@RequestParam("catIds") Long[] catIds)
    {
        if (catIds == null || catIds.length == 0)
        {
            return success(new ArrayList<>());
        }
        List<CatProfile> list = catProfileMapper.selectQrCodeByIds(catIds);
        List<Map<String, Object>> result = new ArrayList<>();
        for (CatProfile p : list)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("catId", p.getCatId());
            item.put("archiveNo", p.getArchiveNo());
            item.put("catName", p.getCatName());
            item.put("qrCode", p.getQrCode());
            result.add(item);
        }
        return success(result);
    }

    /**
     * 猫咪信息卡打印数据（含二维码、最近疫苗/送医/诊断/用药）
     */
    @GetMapping("/cards")
    public AjaxResult cards(@RequestParam("catIds") Long[] catIds)
    {
        if (catIds == null || catIds.length == 0)
        {
            return success(new ArrayList<>());
        }
        return success(catProfileMapper.selectCardsByIds(catIds));
    }

    /**
     * 归档（离世 / 放归 / 领养后不再出现在在养列表）
     */
    @PreAuthorize("@ss.hasPermi('cat:profile:edit')")
    @Log(title = "猫咪档案", businessType = BusinessType.UPDATE)
    @PostMapping("/archive/{catId}")
    public AjaxResult archiveCat(@PathVariable("catId") Long catId)
    {
        return toAjax(catIdService.archiveCatProfile(catId));
    }

    /**
     * 取消归档
     */
    @PreAuthorize("@ss.hasPermi('cat:profile:edit')")
    @Log(title = "猫咪档案", businessType = BusinessType.UPDATE)
    @PostMapping("/unarchive/{catId}")
    public AjaxResult unarchiveCat(@PathVariable("catId") Long catId)
    {
        return toAjax(catIdService.unarchiveCatProfile(catId));
    }

    /**
     * 导出单猫档案 PDF（完整病历、流转、用药、疫苗、标签、死亡记录）
     */
    @PreAuthorize("@ss.hasPermi('cat:profile:query')")
    @Log(title = "猫咪档案", businessType = BusinessType.EXPORT)
    @GetMapping("/pdf/{catId}")
    public void exportPdf(@PathVariable("catId") Long catId, HttpServletResponse response) throws Exception
    {
        CatProfile profile = catIdService.selectCatProfileById(catId);
        String fileName = "猫咪档案_" + (profile == null ? catId : profile.getArchiveNo()) + ".pdf";
        byte[] data = catArchivePdfService.build(catId);
        response.reset();
        response.setContentType("application/pdf;charset=UTF-8");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20") + "\"");
        response.setHeader("Content-Length", String.valueOf(data.length));
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    @PreAuthorize("@ss.hasPermi('cat:profile:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatProfile catId)
    {
        applyWardScope(catId);
        startPage();
        List<CatProfile> list = catIdService.selectCatProfileList(catId);
        return getDataTable(list);
    }

    /**
     * 助理按病房授权限定可见范围（需求文档 15：助理分别管理不同病房）
     * 超管、管理员、医生不受限制；助理只有配置了病房授权才收敛范围。
     */
    private void applyWardScope(CatProfile query)
    {
        if (!catWardAuthService.isWardScopedUser())
        {
            return;
        }
        List<Long> wardIds = catWardAuthService.selectWardIdsByUserId(SecurityUtils.getUserId());
        query.setWardIds(wardIds.toArray(new Long[0]));
        query.setWardScope(Boolean.TRUE);
    }

    @PreAuthorize("@ss.hasPermi('cat:profile:export')")
    @Log(title = "猫咪档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatProfile catId)
    {
        List<CatProfile> list = catIdService.selectCatProfileList(catId);
        ExcelUtil<CatProfile> util = new ExcelUtil<CatProfile>(CatProfile.class);
        util.exportExcel(response, list, "猫咪档案数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:profile:query')")
    @GetMapping(value = "/{catId}")
    public AjaxResult getInfo(@PathVariable("catId") Long catId)
    {
        return success(catIdService.selectCatProfileById(catId));
    }

    @PreAuthorize("@ss.hasPermi('cat:profile:add')")
    @Log(title = "猫咪档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatProfile catId)
    {
        return toAjax(catIdService.insertCatProfile(catId));
    }

    @PreAuthorize("@ss.hasPermi('cat:profile:edit')")
    @Log(title = "猫咪档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatProfile catId)
    {
        return toAjax(catIdService.updateCatProfile(catId));
    }

    @PreAuthorize("@ss.hasPermi('cat:profile:remove')")
    @Log(title = "猫咪档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{catIds}")
    public AjaxResult remove(@PathVariable Long[] catIds)
    {
        return toAjax(catIdService.deleteCatProfileByIds(catIds));
    }

    /**
     * 重新生成单只猫咪二维码
     *
     * <p>直接返回新二维码（Base64）、扫码链接与「内容是否变化」标志，前端据此就地刷新，
     * 不需要再发一次详情查询。</p>
     */
    @PreAuthorize("@ss.hasPermi('cat:profile:edit')")
    @Log(title = "猫咪档案", businessType = BusinessType.UPDATE)
    @PostMapping("/qrcode/{catId}")
    public AjaxResult regenerateQrCode(@PathVariable("catId") Long catId)
    {
        Map<String, Object> data = catIdService.regenerateQrCode(catId);
        return data == null ? AjaxResult.error("猫咪档案不存在") : AjaxResult.success("二维码已生成", data);
    }

    /**
     * 批量补码：为所有猫咪重新生成二维码（历史数据迁移用）
     */
    @PreAuthorize("@ss.hasPermi('cat:profile:edit')")
    @Log(title = "猫咪档案", businessType = BusinessType.UPDATE)
    @PostMapping("/qrcode/batch")
    public AjaxResult regenerateAllQrCode()
    {
        int count = catIdService.regenerateAllQrCode();
        return success("已为 " + count + " 只猫咪生成二维码");
    }
}
