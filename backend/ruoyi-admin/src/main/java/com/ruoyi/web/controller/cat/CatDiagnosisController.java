package com.ruoyi.web.controller.cat;

import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.cat.domain.CatDiagnosis;
import com.ruoyi.cat.domain.CatDiagnosisSummary;
import com.ruoyi.cat.service.ICatDiagnosisService;
import com.ruoyi.cat.service.ICatStaffService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * 站内诊断记录 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/diagnosis")
public class CatDiagnosisController extends BaseController
{
    @Autowired
    private ICatDiagnosisService diagnosisIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @Autowired
    private ICatStaffService staffService;

    @PreAuthorize("@ss.hasPermi('cat:diagnosis:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatDiagnosis diagnosisId)
    {
        startPage();
        List<CatDiagnosis> list = diagnosisIdService.selectCatDiagnosisList(diagnosisId);
        return getDataTable(list);
    }

    /**
     * 按猫合并视图：一只猫一行（就诊次数、首次/最近就诊、最近一次诊断结果）
     * <p>
     * 原来的流水视图保留：查流水、统计医生工作量仍然按次数算。
     */
    @PreAuthorize("@ss.hasPermi('cat:diagnosis:list')")
    @GetMapping("/summary")
    public TableDataInfo summary(CatDiagnosis diagnosisId)
    {
        startPage();
        List<CatDiagnosisSummary> list = diagnosisIdService.selectCatDiagnosisSummaryList(diagnosisId);
        return getDataTable(list);
    }

    /**
     * 当前筛选范围的统计：该病房（基地）多少只猫 + 共多少条诊断记录
     */
    @PreAuthorize("@ss.hasPermi('cat:diagnosis:list')")
    @GetMapping("/stat")
    public AjaxResult stat(CatDiagnosis diagnosisId)
    {
        return success(diagnosisIdService.selectDiagnosisStat(diagnosisId));
    }

    /**
     * 病房下拉数据：每个病房当前在册猫数（已放归 / 已领养 / 已离世不计入）
     */
    @PreAuthorize("@ss.hasPermi('cat:diagnosis:list')")
    @GetMapping("/wardStats")
    public AjaxResult wardStats(CatDiagnosis diagnosisId)
    {
        return success(diagnosisIdService.selectWardStats(diagnosisId));
    }

    /**
     * 新增弹窗选中猫咪后带出：来源基地、当前基地/病房、默认就诊类型
     */
    @PreAuthorize("@ss.hasPermi('cat:diagnosis:list')")
    @GetMapping("/catInfo/{catId}")
    public AjaxResult catInfo(@PathVariable("catId") Long catId)
    {
        return success(diagnosisIdService.selectCatHint(catId));
    }

    /**
     * 接诊医生候选：只返回「医生」角色的账号
     * <p>
     * 医生本人登录时前端直接显示自己的名字，只有超管/管理员才会用到这个下拉。
     * 权限挂在 add 上，不依赖 system:user:list，避免以后给别的角色开录入权限时 403。
     */
    @PreAuthorize("@ss.hasPermi('cat:diagnosis:add')")
    @GetMapping("/doctorOptions")
    public AjaxResult doctorOptions()
    {
        return success(diagnosisIdService.selectDoctorOptions());
    }

    @PreAuthorize("@ss.hasPermi('cat:diagnosis:export')")
    @Log(title = "站内诊断记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatDiagnosis diagnosisId)
    {
        List<CatDiagnosis> list = diagnosisIdService.selectCatDiagnosisList(diagnosisId);
        refNameFiller.fill(list);
        ExcelUtil<CatDiagnosis> util = new ExcelUtil<CatDiagnosis>(CatDiagnosis.class);
        util.exportExcel(response, list, "站内诊断记录数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:diagnosis:query')")
    @GetMapping(value = "/{diagnosisId}")
    public AjaxResult getInfo(@PathVariable("diagnosisId") Long diagnosisId)
    {
        return success(diagnosisIdService.selectCatDiagnosisById(diagnosisId));
    }

    @PreAuthorize("@ss.hasPermi('cat:diagnosis:add')")
    @Log(title = "站内诊断记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatDiagnosis diagnosisId)
    {
        return toAjax(diagnosisIdService.insertCatDiagnosis(diagnosisId));
    }

    /**
     * 修改诊断
     * <p>
     * 需求文档 15：历史病历只允许新增补充，禁止直接修改旧诊断，避免病历被篡改。
     * 因此只有内置超级管理员才能修改正文，其他角色只能通过「更正备注」补充说明。
     */
    @PreAuthorize("@ss.hasPermi('cat:diagnosis:edit')")
    @Log(title = "站内诊断记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatDiagnosis diagnosisId)
    {
        if (!SecurityUtils.isAdmin())
        {
            return AjaxResult.error("历史诊断不允许直接修改，请使用「更正备注」补充说明");
        }
        return toAjax(diagnosisIdService.updateCatDiagnosis(diagnosisId));
    }

    /**
     * 追加更正备注（原始记录保留，仅追加更正说明）
     */
    @PreAuthorize("@ss.hasPermi('cat:diagnosis:edit')")
    @Log(title = "站内诊断记录", businessType = BusinessType.UPDATE)
    @PutMapping("/correction")
    public AjaxResult correction(@RequestBody CatDiagnosis diagnosisId)
    {
        int rows = diagnosisIdService.correctCatDiagnosis(diagnosisId.getDiagnosisId(),
                diagnosisId.getCorrectionNote(), staffService.currentStaffName());
        return rows > 0 ? success("已追加更正备注，原始记录保持不变") : AjaxResult.error("更正失败");
    }

    /**
     * 删除诊断记录：仅为内置超级管理员保留的纠错通道，业务角色不可删除病历
     */
    @PreAuthorize("@ss.hasPermi('cat:diagnosis:remove')")
    @Log(title = "站内诊断记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{diagnosisIds}")
    public AjaxResult remove(@PathVariable Long[] diagnosisIds)
    {
        if (!SecurityUtils.isAdmin())
        {
            return AjaxResult.error("病历记录不可删除，如有错误请使用「更正备注」");
        }
        return toAjax(diagnosisIdService.deleteCatDiagnosisByIds(diagnosisIds));
    }
}
