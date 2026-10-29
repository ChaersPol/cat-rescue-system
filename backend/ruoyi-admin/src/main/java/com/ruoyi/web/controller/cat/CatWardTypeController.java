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
import com.ruoyi.cat.domain.CatWardType;
import com.ruoyi.cat.service.ICatWardTypeService;

/**
 * 病房类型 控制器
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/wardtype")
public class CatWardTypeController extends BaseController
{
    @Autowired
    private ICatWardTypeService catWardTypeService;

    @PreAuthorize("@ss.hasPermi('cat:wardtype:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatWardType catWardType)
    {
        startPage();
        List<CatWardType> list = catWardTypeService.selectCatWardTypeList(catWardType);
        return getDataTable(list);
    }

    /**
     * 全部正常类型（病房新增/修改的下拉用）
     * 不挂权限点：凡能进病房页面的角色都要读这个字典
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return success(catWardTypeService.selectCatWardTypeAll());
    }

    @PreAuthorize("@ss.hasPermi('cat:wardtype:export')")
    @Log(title = "病房类型", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatWardType catWardType)
    {
        List<CatWardType> list = catWardTypeService.selectCatWardTypeList(catWardType);
        ExcelUtil<CatWardType> util = new ExcelUtil<CatWardType>(CatWardType.class);
        util.exportExcel(response, list, "病房类型数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:wardtype:query')")
    @GetMapping(value = "/{typeId}")
    public AjaxResult getInfo(@PathVariable("typeId") Long typeId)
    {
        return success(catWardTypeService.selectCatWardTypeById(typeId));
    }

    @PreAuthorize("@ss.hasPermi('cat:wardtype:add')")
    @Log(title = "病房类型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatWardType catWardType)
    {
        return toAjax(catWardTypeService.insertCatWardType(catWardType));
    }

    @PreAuthorize("@ss.hasPermi('cat:wardtype:edit')")
    @Log(title = "病房类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatWardType catWardType)
    {
        return toAjax(catWardTypeService.updateCatWardType(catWardType));
    }

    @PreAuthorize("@ss.hasPermi('cat:wardtype:remove')")
    @Log(title = "病房类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{typeIds}")
    public AjaxResult remove(@PathVariable Long[] typeIds)
    {
        return toAjax(catWardTypeService.deleteCatWardTypeByIds(typeIds));
    }
}
