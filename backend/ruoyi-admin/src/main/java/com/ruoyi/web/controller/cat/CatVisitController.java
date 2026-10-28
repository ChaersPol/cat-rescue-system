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
import com.ruoyi.cat.domain.CatVisit;
import com.ruoyi.cat.service.ICatVisitService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * 回访记录 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/visit")
public class CatVisitController extends BaseController
{
    @Autowired
    private ICatVisitService visitIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:visit:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatVisit visitId)
    {
        startPage();
        List<CatVisit> list = visitIdService.selectCatVisitList(visitId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:visit:export')")
    @Log(title = "回访记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatVisit visitId)
    {
        List<CatVisit> list = visitIdService.selectCatVisitList(visitId);
        refNameFiller.fill(list);
        ExcelUtil<CatVisit> util = new ExcelUtil<CatVisit>(CatVisit.class);
        util.exportExcel(response, list, "回访记录数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:visit:query')")
    @GetMapping(value = "/{visitId}")
    public AjaxResult getInfo(@PathVariable("visitId") Long visitId)
    {
        return success(visitIdService.selectCatVisitById(visitId));
    }

    @PreAuthorize("@ss.hasPermi('cat:visit:add')")
    @Log(title = "回访记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatVisit visitId)
    {
        return toAjax(visitIdService.insertCatVisit(visitId));
    }

    @PreAuthorize("@ss.hasPermi('cat:visit:edit')")
    @Log(title = "回访记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatVisit visitId)
    {
        return toAjax(visitIdService.updateCatVisit(visitId));
    }

    @PreAuthorize("@ss.hasPermi('cat:visit:remove')")
    @Log(title = "回访记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{visitIds}")
    public AjaxResult remove(@PathVariable Long[] visitIds)
    {
        return toAjax(visitIdService.deleteCatVisitByIds(visitIds));
    }
}
