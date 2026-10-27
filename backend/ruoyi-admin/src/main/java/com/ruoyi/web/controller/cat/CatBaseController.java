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
import com.ruoyi.cat.domain.CatBase;
import com.ruoyi.cat.service.ICatBaseService;

/**
 * 基地 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/base")
public class CatBaseController extends BaseController
{
    @Autowired
    private ICatBaseService baseIdService;

    @PreAuthorize("@ss.hasPermi('cat:base:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatBase baseId)
    {
        startPage();
        List<CatBase> list = baseIdService.selectCatBaseList(baseId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:base:export')")
    @Log(title = "基地", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatBase baseId)
    {
        List<CatBase> list = baseIdService.selectCatBaseList(baseId);
        ExcelUtil<CatBase> util = new ExcelUtil<CatBase>(CatBase.class);
        util.exportExcel(response, list, "基地数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:base:query')")
    @GetMapping(value = "/{baseId}")
    public AjaxResult getInfo(@PathVariable("baseId") Long baseId)
    {
        return success(baseIdService.selectCatBaseById(baseId));
    }

    @PreAuthorize("@ss.hasPermi('cat:base:add')")
    @Log(title = "基地", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatBase baseId)
    {
        return toAjax(baseIdService.insertCatBase(baseId));
    }

    @PreAuthorize("@ss.hasPermi('cat:base:edit')")
    @Log(title = "基地", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatBase baseId)
    {
        return toAjax(baseIdService.updateCatBase(baseId));
    }

    @PreAuthorize("@ss.hasPermi('cat:base:remove')")
    @Log(title = "基地", businessType = BusinessType.DELETE)
    @DeleteMapping("/{baseIds}")
    public AjaxResult remove(@PathVariable Long[] baseIds)
    {
        return toAjax(baseIdService.deleteCatBaseByIds(baseIds));
    }
}
