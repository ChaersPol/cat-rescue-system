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
import com.ruoyi.cat.domain.CatTnr;
import com.ruoyi.cat.service.ICatTnrService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * TNR抓捕登记 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/tnr")
public class CatTnrController extends BaseController
{
    @Autowired
    private ICatTnrService tnrIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:tnr:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatTnr tnrId)
    {
        startPage();
        List<CatTnr> list = tnrIdService.selectCatTnrList(tnrId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:tnr:export')")
    @Log(title = "TNR抓捕登记", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatTnr tnrId)
    {
        List<CatTnr> list = tnrIdService.selectCatTnrList(tnrId);
        refNameFiller.fill(list);
        ExcelUtil<CatTnr> util = new ExcelUtil<CatTnr>(CatTnr.class);
        util.exportExcel(response, list, "TNR抓捕登记数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:tnr:query')")
    @GetMapping(value = "/{tnrId}")
    public AjaxResult getInfo(@PathVariable("tnrId") Long tnrId)
    {
        return success(tnrIdService.selectCatTnrById(tnrId));
    }

    @PreAuthorize("@ss.hasPermi('cat:tnr:add')")
    @Log(title = "TNR抓捕登记", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatTnr tnrId)
    {
        return toAjax(tnrIdService.insertCatTnr(tnrId));
    }

    @PreAuthorize("@ss.hasPermi('cat:tnr:edit')")
    @Log(title = "TNR抓捕登记", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatTnr tnrId)
    {
        return toAjax(tnrIdService.updateCatTnr(tnrId));
    }

    @PreAuthorize("@ss.hasPermi('cat:tnr:remove')")
    @Log(title = "TNR抓捕登记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tnrIds}")
    public AjaxResult remove(@PathVariable Long[] tnrIds)
    {
        return toAjax(tnrIdService.deleteCatTnrByIds(tnrIds));
    }
}
