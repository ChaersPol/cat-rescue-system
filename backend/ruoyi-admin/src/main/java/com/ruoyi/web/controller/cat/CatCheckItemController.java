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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.cat.domain.CatCheckItem;
import com.ruoyi.cat.service.ICatCheckItemService;

/**
 * 检查项目/用药目录 控制器
 * <p>
 * 维护「站内诊断」开单时可选的检查项目与药品，项目可关联物资，
 * 医生开具后自动出库并扣库存。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/checkitem")
public class CatCheckItemController extends BaseController
{
    @Autowired
    private ICatCheckItemService checkItemService;

    @PreAuthorize("@ss.hasPermi('cat:checkitem:list')")
    @GetMapping("/list")
    public AjaxResult list(CatCheckItem item)
    {
        // 树形数据一次取全，前端用 handleTree 转树；不分页
        return success(checkItemService.selectCatCheckItemList(item));
    }

    /**
     * 开单可选项目：启用的叶子节点，带关联物资当前库存。
     * 权限挂在「新增诊断」上，医生、助理都能取到，不依赖目录维护权限。
     */
    @PreAuthorize("@ss.hasPermi('cat:diagnosis:add')")
    @GetMapping("/options")
    public AjaxResult options(@RequestParam(required = false) String itemType,
                              @RequestParam(required = false) String keyword)
    {
        return success(checkItemService.selectSelectableItems(itemType, keyword));
    }

    @PreAuthorize("@ss.hasPermi('cat:checkitem:export')")
    @Log(title = "检查项目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatCheckItem item)
    {
        List<CatCheckItem> list = checkItemService.selectCatCheckItemList(item);
        ExcelUtil<CatCheckItem> util = new ExcelUtil<CatCheckItem>(CatCheckItem.class);
        util.exportExcel(response, list, "检查项目");
    }

    @PreAuthorize("@ss.hasPermi('cat:checkitem:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId)
    {
        return success(checkItemService.selectCatCheckItemById(itemId));
    }

    @PreAuthorize("@ss.hasPermi('cat:checkitem:add')")
    @Log(title = "检查项目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatCheckItem item)
    {
        return toAjax(checkItemService.insertCatCheckItem(item));
    }

    @PreAuthorize("@ss.hasPermi('cat:checkitem:edit')")
    @Log(title = "检查项目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatCheckItem item)
    {
        return toAjax(checkItemService.updateCatCheckItem(item));
    }

    @PreAuthorize("@ss.hasPermi('cat:checkitem:remove')")
    @Log(title = "检查项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        return toAjax(checkItemService.deleteCatCheckItemByIds(itemIds));
    }
}
