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
import com.ruoyi.cat.domain.CatWard;
import com.ruoyi.cat.service.ICatWardService;

/**
 * 病房 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/ward")
public class CatWardController extends BaseController
{
    @Autowired
    private ICatWardService wardIdService;

    @PreAuthorize("@ss.hasPermi('cat:ward:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatWard wardId)
    {
        startPage();
        List<CatWard> list = wardIdService.selectCatWardList(wardId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:ward:export')")
    @Log(title = "病房", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatWard wardId)
    {
        List<CatWard> list = wardIdService.selectCatWardList(wardId);
        ExcelUtil<CatWard> util = new ExcelUtil<CatWard>(CatWard.class);
        util.exportExcel(response, list, "病房数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:ward:query')")
    @GetMapping(value = "/{wardId}")
    public AjaxResult getInfo(@PathVariable("wardId") Long wardId)
    {
        return success(wardIdService.selectCatWardById(wardId));
    }

    @PreAuthorize("@ss.hasPermi('cat:ward:add')")
    @Log(title = "病房", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatWard wardId)
    {
        return toAjax(wardIdService.insertCatWard(wardId));
    }

    @PreAuthorize("@ss.hasPermi('cat:ward:edit')")
    @Log(title = "病房", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatWard wardId)
    {
        return toAjax(wardIdService.updateCatWard(wardId));
    }

    @PreAuthorize("@ss.hasPermi('cat:ward:remove')")
    @Log(title = "病房", businessType = BusinessType.DELETE)
    @DeleteMapping("/{wardIds}")
    public AjaxResult remove(@PathVariable Long[] wardIds)
    {
        return toAjax(wardIdService.deleteCatWardByIds(wardIds));
    }
}
