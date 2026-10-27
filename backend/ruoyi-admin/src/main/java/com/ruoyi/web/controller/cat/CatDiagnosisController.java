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
import com.ruoyi.cat.service.ICatDiagnosisService;

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

    @PreAuthorize("@ss.hasPermi('cat:diagnosis:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatDiagnosis diagnosisId)
    {
        startPage();
        List<CatDiagnosis> list = diagnosisIdService.selectCatDiagnosisList(diagnosisId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:diagnosis:export')")
    @Log(title = "站内诊断记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatDiagnosis diagnosisId)
    {
        List<CatDiagnosis> list = diagnosisIdService.selectCatDiagnosisList(diagnosisId);
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
                diagnosisId.getCorrectionNote(), SecurityUtils.getUsername());
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
