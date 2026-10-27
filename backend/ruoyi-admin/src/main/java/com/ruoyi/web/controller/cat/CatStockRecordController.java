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
import com.ruoyi.cat.domain.CatStockRecord;
import com.ruoyi.cat.service.ICatStockRecordService;

/**
 * 物资出入库记录 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/stockrecord")
public class CatStockRecordController extends BaseController
{
    @Autowired
    private ICatStockRecordService recordIdService;

    @PreAuthorize("@ss.hasPermi('cat:stockrecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatStockRecord recordId)
    {
        startPage();
        List<CatStockRecord> list = recordIdService.selectCatStockRecordList(recordId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:stockrecord:export')")
    @Log(title = "物资出入库记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatStockRecord recordId)
    {
        List<CatStockRecord> list = recordIdService.selectCatStockRecordList(recordId);
        ExcelUtil<CatStockRecord> util = new ExcelUtil<CatStockRecord>(CatStockRecord.class);
        util.exportExcel(response, list, "物资出入库记录数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:stockrecord:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(recordIdService.selectCatStockRecordById(recordId));
    }

    @PreAuthorize("@ss.hasPermi('cat:stockrecord:add')")
    @Log(title = "物资出入库记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatStockRecord recordId)
    {
        return toAjax(recordIdService.insertCatStockRecord(recordId));
    }

    /**
     * 出入库记录不允许修改，如需更正请使用冲销
     */
    @PreAuthorize("@ss.hasPermi('cat:stockrecord:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody CatStockRecord recordId)
    {
        return AjaxResult.error("出入库记录不允许修改，如需更正请使用「冲销」生成反向记录");
    }

    /**
     * 出入库记录不允许删除，只能冲销
     */
    @PreAuthorize("@ss.hasPermi('cat:stockrecord:remove')")
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return AjaxResult.error("出入库记录不允许删除，请使用「冲销」生成反向记录");
    }

    /**
     * 冲销出入库记录（生成反向记录并回滚库存）
     */
    @PreAuthorize("@ss.hasPermi('cat:stockrecord:edit')")
    @Log(title = "物资出入库记录", businessType = BusinessType.UPDATE)
    @PostMapping("/reverse/{recordId}")
    public AjaxResult reverse(@PathVariable("recordId") Long recordId)
    {
        int rows = recordIdService.reverseCatStockRecord(recordId, getUsername());
        return rows > 0 ? success("冲销成功，库存已回滚") : AjaxResult.error("冲销失败，原记录不存在");
    }
}
