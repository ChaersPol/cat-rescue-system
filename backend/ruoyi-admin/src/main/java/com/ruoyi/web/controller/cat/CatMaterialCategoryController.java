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
import com.ruoyi.cat.domain.CatMaterialCategory;
import com.ruoyi.cat.service.ICatMaterialCategoryService;

/**
 * 物资分类 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/materialcategory")
public class CatMaterialCategoryController extends BaseController
{
    @Autowired
    private ICatMaterialCategoryService categoryIdService;

    @PreAuthorize("@ss.hasPermi('cat:materialcategory:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatMaterialCategory categoryId)
    {
        startPage();
        List<CatMaterialCategory> list = categoryIdService.selectCatMaterialCategoryList(categoryId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:materialcategory:export')")
    @Log(title = "物资分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatMaterialCategory categoryId)
    {
        List<CatMaterialCategory> list = categoryIdService.selectCatMaterialCategoryList(categoryId);
        ExcelUtil<CatMaterialCategory> util = new ExcelUtil<CatMaterialCategory>(CatMaterialCategory.class);
        util.exportExcel(response, list, "物资分类数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:materialcategory:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return success(categoryIdService.selectCatMaterialCategoryById(categoryId));
    }

    @PreAuthorize("@ss.hasPermi('cat:materialcategory:add')")
    @Log(title = "物资分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatMaterialCategory categoryId)
    {
        return toAjax(categoryIdService.insertCatMaterialCategory(categoryId));
    }

    @PreAuthorize("@ss.hasPermi('cat:materialcategory:edit')")
    @Log(title = "物资分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatMaterialCategory categoryId)
    {
        return toAjax(categoryIdService.updateCatMaterialCategory(categoryId));
    }

    @PreAuthorize("@ss.hasPermi('cat:materialcategory:remove')")
    @Log(title = "物资分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(categoryIdService.deleteCatMaterialCategoryByIds(categoryIds));
    }
}
