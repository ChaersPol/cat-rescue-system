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
import com.ruoyi.cat.domain.CatExpense;
import com.ruoyi.cat.service.ICatExpenseService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * 财务支出 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/expense")
public class CatExpenseController extends BaseController
{
    @Autowired
    private ICatExpenseService expenseIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:expense:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatExpense expenseId)
    {
        startPage();
        List<CatExpense> list = expenseIdService.selectCatExpenseList(expenseId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:expense:export')")
    @Log(title = "财务支出", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatExpense expenseId)
    {
        List<CatExpense> list = expenseIdService.selectCatExpenseList(expenseId);
        refNameFiller.fill(list);
        ExcelUtil<CatExpense> util = new ExcelUtil<CatExpense>(CatExpense.class);
        util.exportExcel(response, list, "财务支出数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:expense:query')")
    @GetMapping(value = "/{expenseId}")
    public AjaxResult getInfo(@PathVariable("expenseId") Long expenseId)
    {
        return success(expenseIdService.selectCatExpenseById(expenseId));
    }

    @PreAuthorize("@ss.hasPermi('cat:expense:add')")
    @Log(title = "财务支出", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatExpense expenseId)
    {
        return toAjax(expenseIdService.insertCatExpense(expenseId));
    }

    @PreAuthorize("@ss.hasPermi('cat:expense:edit')")
    @Log(title = "财务支出", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatExpense expenseId)
    {
        return toAjax(expenseIdService.updateCatExpense(expenseId));
    }

    @PreAuthorize("@ss.hasPermi('cat:expense:remove')")
    @Log(title = "财务支出", businessType = BusinessType.DELETE)
    @DeleteMapping("/{expenseIds}")
    public AjaxResult remove(@PathVariable Long[] expenseIds)
    {
        return toAjax(expenseIdService.deleteCatExpenseByIds(expenseIds));
    }
}
