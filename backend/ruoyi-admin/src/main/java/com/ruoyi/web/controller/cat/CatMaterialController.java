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
import com.ruoyi.cat.domain.CatMaterial;
import com.ruoyi.cat.service.ICatMaterialService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * 物资 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/material")
public class CatMaterialController extends BaseController
{
    @Autowired
    private ICatMaterialService materialIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatMaterial materialId)
    {
        startPage();
        List<CatMaterial> list = materialIdService.selectCatMaterialList(materialId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:material:export')")
    @Log(title = "物资", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatMaterial materialId)
    {
        List<CatMaterial> list = materialIdService.selectCatMaterialList(materialId);
        refNameFiller.fill(list);
        ExcelUtil<CatMaterial> util = new ExcelUtil<CatMaterial>(CatMaterial.class);
        util.exportExcel(response, list, "物资数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:material:query')")
    @GetMapping(value = "/{materialId}")
    public AjaxResult getInfo(@PathVariable("materialId") Long materialId)
    {
        return success(materialIdService.selectCatMaterialById(materialId));
    }

    @PreAuthorize("@ss.hasPermi('cat:material:add')")
    @Log(title = "物资", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatMaterial materialId)
    {
        return toAjax(materialIdService.insertCatMaterial(materialId));
    }

    @PreAuthorize("@ss.hasPermi('cat:material:edit')")
    @Log(title = "物资", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatMaterial materialId)
    {
        return toAjax(materialIdService.updateCatMaterial(materialId));
    }

    @PreAuthorize("@ss.hasPermi('cat:material:remove')")
    @Log(title = "物资", businessType = BusinessType.DELETE)
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable Long[] materialIds)
    {
        return toAjax(materialIdService.deleteCatMaterialByIds(materialIds));
    }
}
