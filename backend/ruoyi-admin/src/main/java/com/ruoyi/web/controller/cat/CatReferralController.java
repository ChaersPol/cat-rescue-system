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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.cat.domain.CatReferral;
import com.ruoyi.cat.service.ICatReferralService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * 外送医院转诊记录 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/referral")
public class CatReferralController extends BaseController
{
    @Autowired
    private ICatReferralService referralIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:referral:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatReferral referralId)
    {
        startPage();
        List<CatReferral> list = referralIdService.selectCatReferralList(referralId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:referral:export')")
    @Log(title = "外送医院转诊记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatReferral referralId)
    {
        List<CatReferral> list = referralIdService.selectCatReferralList(referralId);
        refNameFiller.fill(list);
        ExcelUtil<CatReferral> util = new ExcelUtil<CatReferral>(CatReferral.class);
        util.exportExcel(response, list, "外送医院转诊记录数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:referral:query')")
    @GetMapping(value = "/{referralId}")
    public AjaxResult getInfo(@PathVariable("referralId") Long referralId)
    {
        return success(referralIdService.selectCatReferralById(referralId));
    }

    @PreAuthorize("@ss.hasPermi('cat:referral:add')")
    @Log(title = "外送医院转诊记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatReferral referralId)
    {
        return toAjax(referralIdService.insertCatReferral(referralId));
    }

    @PreAuthorize("@ss.hasPermi('cat:referral:edit')")
    @Log(title = "外送医院转诊记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatReferral referralId)
    {
        return toAjax(referralIdService.updateCatReferral(referralId));
    }

    @PreAuthorize("@ss.hasPermi('cat:referral:remove')")
    @Log(title = "外送医院转诊记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{referralIds}")
    public AjaxResult remove(@PathVariable Long[] referralIds)
    {
        return toAjax(referralIdService.deleteCatReferralByIds(referralIds));
    }
}
