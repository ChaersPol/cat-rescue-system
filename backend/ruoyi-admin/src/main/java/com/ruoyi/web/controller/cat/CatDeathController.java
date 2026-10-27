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
import com.ruoyi.cat.domain.CatDeath;
import com.ruoyi.cat.service.ICatDeathService;

/**
 * 死亡台账 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/death")
public class CatDeathController extends BaseController
{
    @Autowired
    private ICatDeathService deathIdService;

    @PreAuthorize("@ss.hasPermi('cat:death:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatDeath deathId)
    {
        startPage();
        List<CatDeath> list = deathIdService.selectCatDeathList(deathId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:death:export')")
    @Log(title = "死亡台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatDeath deathId)
    {
        List<CatDeath> list = deathIdService.selectCatDeathList(deathId);
        ExcelUtil<CatDeath> util = new ExcelUtil<CatDeath>(CatDeath.class);
        util.exportExcel(response, list, "死亡台账数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:death:query')")
    @GetMapping(value = "/{deathId}")
    public AjaxResult getInfo(@PathVariable("deathId") Long deathId)
    {
        return success(deathIdService.selectCatDeathById(deathId));
    }

    @PreAuthorize("@ss.hasPermi('cat:death:add')")
    @Log(title = "死亡台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatDeath deathId)
    {
        return toAjax(deathIdService.insertCatDeath(deathId));
    }

    @PreAuthorize("@ss.hasPermi('cat:death:edit')")
    @Log(title = "死亡台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatDeath deathId)
    {
        return toAjax(deathIdService.updateCatDeath(deathId));
    }

    @PreAuthorize("@ss.hasPermi('cat:death:remove')")
    @Log(title = "死亡台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deathIds}")
    public AjaxResult remove(@PathVariable Long[] deathIds)
    {
        return toAjax(deathIdService.deleteCatDeathByIds(deathIds));
    }
}
