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
import com.ruoyi.cat.domain.CatAdoption;
import com.ruoyi.cat.service.ICatAdoptionService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * 领养登记 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/adoption")
public class CatAdoptionController extends BaseController
{
    @Autowired
    private ICatAdoptionService adoptionIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:adoption:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatAdoption adoptionId)
    {
        startPage();
        List<CatAdoption> list = adoptionIdService.selectCatAdoptionList(adoptionId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:adoption:export')")
    @Log(title = "领养登记", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatAdoption adoptionId)
    {
        List<CatAdoption> list = adoptionIdService.selectCatAdoptionList(adoptionId);
        refNameFiller.fill(list);
        ExcelUtil<CatAdoption> util = new ExcelUtil<CatAdoption>(CatAdoption.class);
        util.exportExcel(response, list, "领养登记数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:adoption:query')")
    @GetMapping(value = "/{adoptionId}")
    public AjaxResult getInfo(@PathVariable("adoptionId") Long adoptionId)
    {
        return success(adoptionIdService.selectCatAdoptionById(adoptionId));
    }

    @PreAuthorize("@ss.hasPermi('cat:adoption:add')")
    @Log(title = "领养登记", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatAdoption adoptionId)
    {
        return toAjax(adoptionIdService.insertCatAdoption(adoptionId));
    }

    @PreAuthorize("@ss.hasPermi('cat:adoption:edit')")
    @Log(title = "领养登记", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatAdoption adoptionId)
    {
        return toAjax(adoptionIdService.updateCatAdoption(adoptionId));
    }

    @PreAuthorize("@ss.hasPermi('cat:adoption:remove')")
    @Log(title = "领养登记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{adoptionIds}")
    public AjaxResult remove(@PathVariable Long[] adoptionIds)
    {
        return toAjax(adoptionIdService.deleteCatAdoptionByIds(adoptionIds));
    }

    /**
     * 回访结束开关（可反复开关）
     * 关闭时把该领养下待回访的计划一起收掉并清提醒；重新打开时恢复还来得及的计划
     */
    @PreAuthorize("@ss.hasPermi('cat:adoption:edit')")
    @Log(title = "领养登记-回访结束开关", businessType = BusinessType.UPDATE)
    @PutMapping("/visitClosed/{adoptionId}/{closed}")
    public AjaxResult visitClosed(@PathVariable("adoptionId") Long adoptionId,
                                  @PathVariable("closed") Boolean closed)
    {
        int affected = adoptionIdService.setVisitClosed(adoptionId, Boolean.TRUE.equals(closed));
        if (Boolean.TRUE.equals(closed))
        {
            return success("回访已结束，已把 " + affected + " 条待回访计划标记为「已结束」并清除提醒");
        }
        return success("回访已重新开启，已恢复 " + affected + " 条待回访计划（计划日期已超期的转为已作废）");
    }
}
