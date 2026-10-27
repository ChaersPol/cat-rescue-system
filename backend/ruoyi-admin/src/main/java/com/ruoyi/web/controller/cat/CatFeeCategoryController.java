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
import com.ruoyi.cat.domain.CatFeeCategory;
import com.ruoyi.cat.service.ICatFeeCategoryService;

/**
 * 费用分类 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/feecategory")
public class CatFeeCategoryController extends BaseController
{
    @Autowired
    private ICatFeeCategoryService categoryIdService;

    @PreAuthorize("@ss.hasPermi('cat:feecategory:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatFeeCategory categoryId)
    {
        startPage();
        List<CatFeeCategory> list = categoryIdService.selectCatFeeCategoryList(categoryId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:feecategory:export')")
    @Log(title = "费用分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatFeeCategory categoryId)
    {
        List<CatFeeCategory> list = categoryIdService.selectCatFeeCategoryList(categoryId);
        ExcelUtil<CatFeeCategory> util = new ExcelUtil<CatFeeCategory>(CatFeeCategory.class);
        util.exportExcel(response, list, "费用分类数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:feecategory:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return success(categoryIdService.selectCatFeeCategoryById(categoryId));
    }

    @PreAuthorize("@ss.hasPermi('cat:feecategory:add')")
    @Log(title = "费用分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatFeeCategory categoryId)
    {
        return toAjax(categoryIdService.insertCatFeeCategory(categoryId));
    }

    @PreAuthorize("@ss.hasPermi('cat:feecategory:edit')")
    @Log(title = "费用分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatFeeCategory categoryId)
    {
        return toAjax(categoryIdService.updateCatFeeCategory(categoryId));
    }

    @PreAuthorize("@ss.hasPermi('cat:feecategory:remove')")
    @Log(title = "费用分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(categoryIdService.deleteCatFeeCategoryByIds(categoryIds));
    }
}
