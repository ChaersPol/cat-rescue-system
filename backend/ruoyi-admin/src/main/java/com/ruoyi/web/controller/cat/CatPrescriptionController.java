package com.ruoyi.web.controller.cat;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.cat.domain.CatPrescription;
import com.ruoyi.cat.service.ICatPrescriptionService;
import com.ruoyi.cat.util.CatRefNameFiller;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 用药处方 控制器
 * <p>
 * 医生在站内诊断里开方，助理在用药记录里照着打卡；
 * 写操作的角色限制在 service 里卡（助理有菜单权限也不能开方改方）。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/prescription")
public class CatPrescriptionController extends BaseController
{
    @Autowired
    private ICatPrescriptionService prescriptionService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:medication:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatPrescription query)
    {
        startPage();
        List<CatPrescription> list = prescriptionService.selectCatPrescriptionList(query);
        refNameFiller.fill(list);
        return getDataTable(list);
    }

    /** 编辑诊断时回显这张诊断单开的处方（没有则 data 为空） */
    @PreAuthorize("@ss.hasPermi('cat:medication:list')")
    @GetMapping("/byDiagnosis/{diagnosisId}")
    public AjaxResult byDiagnosis(@PathVariable Long diagnosisId)
    {
        return success(prescriptionService.selectByDiagnosisId(diagnosisId));
    }

    @PreAuthorize("@ss.hasPermi('cat:medication:list')")
    @GetMapping("/{prescriptionId}")
    public AjaxResult getInfo(@PathVariable Long prescriptionId)
    {
        return success(prescriptionService.selectCatPrescriptionById(prescriptionId));
    }

    /** 开处方：保存后按「天 × 时段 × 药」展开成用药记录明细 */
    @PreAuthorize("@ss.hasPermi('cat:medication:edit')")
    @Log(title = "用药处方", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatPrescription prescription)
    {
        Long id = prescriptionService.saveCatPrescription(prescription);
        return success(prescriptionService.selectCatPrescriptionById(id));
    }

    /** 改处方：只重建今天及以后、尚未完成打卡的明细，历史不动，并写变更留痕 */
    @PreAuthorize("@ss.hasPermi('cat:medication:edit')")
    @Log(title = "用药处方", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatPrescription prescription)
    {
        Long id = prescriptionService.saveCatPrescription(prescription);
        return success(prescriptionService.selectCatPrescriptionById(id));
    }

    @PreAuthorize("@ss.hasPermi('cat:medication:edit')")
    @Log(title = "用药处方", businessType = BusinessType.UPDATE)
    @PutMapping("/stop/{prescriptionId}")
    public AjaxResult stop(@PathVariable Long prescriptionId)
    {
        prescriptionService.stopCatPrescription(prescriptionId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('cat:medication:remove')")
    @Log(title = "用药处方", businessType = BusinessType.DELETE)
    @DeleteMapping("/{prescriptionId}")
    public AjaxResult remove(@PathVariable Long prescriptionId)
    {
        prescriptionService.deleteCatPrescription(prescriptionId);
        return success();
    }
}
