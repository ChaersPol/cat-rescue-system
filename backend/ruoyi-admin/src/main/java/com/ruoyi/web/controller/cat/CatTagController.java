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
import com.ruoyi.cat.domain.CatTag;
import com.ruoyi.cat.service.ICatTagService;

/**
 * 标签 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/tag")
public class CatTagController extends BaseController
{
    @Autowired
    private ICatTagService tagIdService;

    @PreAuthorize("@ss.hasPermi('cat:tag:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatTag tagId)
    {
        startPage();
        List<CatTag> list = tagIdService.selectCatTagList(tagId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:tag:export')")
    @Log(title = "标签", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatTag tagId)
    {
        List<CatTag> list = tagIdService.selectCatTagList(tagId);
        ExcelUtil<CatTag> util = new ExcelUtil<CatTag>(CatTag.class);
        util.exportExcel(response, list, "标签数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:tag:query')")
    @GetMapping(value = "/{tagId}")
    public AjaxResult getInfo(@PathVariable("tagId") Long tagId)
    {
        return success(tagIdService.selectCatTagById(tagId));
    }

    @PreAuthorize("@ss.hasPermi('cat:tag:add')")
    @Log(title = "标签", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatTag tagId)
    {
        return toAjax(tagIdService.insertCatTag(tagId));
    }

    @PreAuthorize("@ss.hasPermi('cat:tag:edit')")
    @Log(title = "标签", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatTag tagId)
    {
        return toAjax(tagIdService.updateCatTag(tagId));
    }

    @PreAuthorize("@ss.hasPermi('cat:tag:remove')")
    @Log(title = "标签", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tagIds}")
    public AjaxResult remove(@PathVariable Long[] tagIds)
    {
        return toAjax(tagIdService.deleteCatTagByIds(tagIds));
    }
}
