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
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.service.ICatVaccineService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * 疫苗记录 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/vaccine")
public class CatVaccineController extends BaseController
{
    @Autowired
    private ICatVaccineService vaccineIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @Autowired
    private com.ruoyi.cat.service.impl.CatReminderConflictService conflictService;

    @PreAuthorize("@ss.hasPermi('cat:vaccine:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatVaccine vaccineId)
    {
        startPage();
        List<CatVaccine> list = vaccineIdService.selectCatVaccineList(vaccineId);
        return getDataTable(list);
    }

    /**
     * 新增/修改疫苗记录时，选了猫咪就能立刻知道它是不是在用药期
     * 在用药期则不能接种，需要推迟，返回提示文案供页面红字显示
     */
    @PreAuthorize("@ss.hasPermi('cat:vaccine:list')")
    @GetMapping("/medicationTip/{catId}")
    public AjaxResult medicationTip(@PathVariable("catId") Long catId)
    {
        String tip = conflictService.medicationTipForCat(catId, "接种疫苗，建议推迟");
        AjaxResult result = AjaxResult.success();
        result.put("inMedication", tip != null);
        result.put("tip", tip);
        return result;
    }

    @PreAuthorize("@ss.hasPermi('cat:vaccine:export')")
    @Log(title = "疫苗记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatVaccine vaccineId)
    {
        List<CatVaccine> list = vaccineIdService.selectCatVaccineList(vaccineId);
        refNameFiller.fill(list);
        ExcelUtil<CatVaccine> util = new ExcelUtil<CatVaccine>(CatVaccine.class);
        util.exportExcel(response, list, "疫苗记录数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:vaccine:query')")
    @GetMapping(value = "/{vaccineId}")
    public AjaxResult getInfo(@PathVariable("vaccineId") Long vaccineId)
    {
        return success(vaccineIdService.selectCatVaccineById(vaccineId));
    }

    @PreAuthorize("@ss.hasPermi('cat:vaccine:add')")
    @Log(title = "疫苗记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatVaccine vaccineId)
    {
        return toAjax(vaccineIdService.insertCatVaccine(vaccineId));
    }

    @PreAuthorize("@ss.hasPermi('cat:vaccine:edit')")
    @Log(title = "疫苗记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatVaccine vaccineId)
    {
        return toAjax(vaccineIdService.updateCatVaccine(vaccineId));
    }

    @PreAuthorize("@ss.hasPermi('cat:vaccine:remove')")
    @Log(title = "疫苗记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{vaccineIds}")
    public AjaxResult remove(@PathVariable Long[] vaccineIds)
    {
        return toAjax(vaccineIdService.deleteCatVaccineByIds(vaccineIds));
    }
}
