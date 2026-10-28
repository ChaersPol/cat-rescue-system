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
import com.ruoyi.cat.domain.CatRelease;
import com.ruoyi.cat.service.ICatReleaseService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * TNR放归登记 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/release")
public class CatReleaseController extends BaseController
{
    @Autowired
    private ICatReleaseService releaseIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:release:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatRelease releaseId)
    {
        startPage();
        List<CatRelease> list = releaseIdService.selectCatReleaseList(releaseId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:release:export')")
    @Log(title = "TNR放归登记", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatRelease releaseId)
    {
        List<CatRelease> list = releaseIdService.selectCatReleaseList(releaseId);
        refNameFiller.fill(list);
        ExcelUtil<CatRelease> util = new ExcelUtil<CatRelease>(CatRelease.class);
        util.exportExcel(response, list, "TNR放归登记数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:release:query')")
    @GetMapping(value = "/{releaseId}")
    public AjaxResult getInfo(@PathVariable("releaseId") Long releaseId)
    {
        return success(releaseIdService.selectCatReleaseById(releaseId));
    }

    @PreAuthorize("@ss.hasPermi('cat:release:add')")
    @Log(title = "TNR放归登记", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatRelease releaseId)
    {
        return toAjax(releaseIdService.insertCatRelease(releaseId));
    }

    @PreAuthorize("@ss.hasPermi('cat:release:edit')")
    @Log(title = "TNR放归登记", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatRelease releaseId)
    {
        return toAjax(releaseIdService.updateCatRelease(releaseId));
    }

    @PreAuthorize("@ss.hasPermi('cat:release:remove')")
    @Log(title = "TNR放归登记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{releaseIds}")
    public AjaxResult remove(@PathVariable Long[] releaseIds)
    {
        return toAjax(releaseIdService.deleteCatReleaseByIds(releaseIds));
    }
}
