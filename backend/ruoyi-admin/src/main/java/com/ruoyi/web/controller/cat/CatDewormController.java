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
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.service.ICatDewormService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * 驱虫记录 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/deworm")
public class CatDewormController extends BaseController
{
    @Autowired
    private ICatDewormService dewormIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:deworm:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatDeworm dewormId)
    {
        startPage();
        List<CatDeworm> list = dewormIdService.selectCatDewormList(dewormId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:deworm:export')")
    @Log(title = "驱虫记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatDeworm dewormId)
    {
        List<CatDeworm> list = dewormIdService.selectCatDewormList(dewormId);
        refNameFiller.fill(list);
        ExcelUtil<CatDeworm> util = new ExcelUtil<CatDeworm>(CatDeworm.class);
        util.exportExcel(response, list, "驱虫记录数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:deworm:query')")
    @GetMapping(value = "/{dewormId}")
    public AjaxResult getInfo(@PathVariable("dewormId") Long dewormId)
    {
        return success(dewormIdService.selectCatDewormById(dewormId));
    }

    @PreAuthorize("@ss.hasPermi('cat:deworm:add')")
    @Log(title = "驱虫记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatDeworm dewormId)
    {
        return toAjax(dewormIdService.insertCatDeworm(dewormId));
    }

    @PreAuthorize("@ss.hasPermi('cat:deworm:edit')")
    @Log(title = "驱虫记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatDeworm dewormId)
    {
        return toAjax(dewormIdService.updateCatDeworm(dewormId));
    }

    @PreAuthorize("@ss.hasPermi('cat:deworm:remove')")
    @Log(title = "驱虫记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dewormIds}")
    public AjaxResult remove(@PathVariable Long[] dewormIds)
    {
        return toAjax(dewormIdService.deleteCatDewormByIds(dewormIds));
    }
}
