package com.ruoyi.web.controller.cat;

import java.util.Date;
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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.domain.CatMedicationExec;
import com.ruoyi.cat.domain.CatMedicationGroup;
import com.ruoyi.cat.service.ICatMedicationService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * 每日用药记录 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/medication")
public class CatMedicationController extends BaseController
{
    @Autowired
    private ICatMedicationService medicationIdService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:medication:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatMedication medicationId)
    {
        startPage();
        List<CatMedication> list = medicationIdService.selectCatMedicationList(medicationId);
        return getDataTable(list);
    }

    /**
     * 合并视图：同一只猫 + 同一天 + 同一时间段 一行
     * <p>
     * 明细视图（/list）保留：一次喂三味药在明细里是三行，统计药品消耗要按条算。
     */
    @PreAuthorize("@ss.hasPermi('cat:medication:list')")
    @GetMapping("/group/list")
    public TableDataInfo groupList(CatMedicationGroup query)
    {
        startPage();
        List<CatMedicationGroup> list = medicationIdService.selectCatMedicationGroupList(query);
        refNameFiller.fill(list);
        return getDataTable(list);
    }

    /** 合并视图展开：某一组（猫 + 日期 + 时间段）的药品明细 */
    @PreAuthorize("@ss.hasPermi('cat:medication:list')")
    @GetMapping("/group/details")
    public AjaxResult groupDetails(@RequestParam Long catId,
                                   @RequestParam String useDate,
                                   @RequestParam(required = false, defaultValue = "") String timePeriod)
    {
        Date date = parseDate(useDate);
        if (date == null)
        {
            return error("用药日期格式不正确");
        }
        return success(medicationIdService.selectGroupDetails(catId, date, timePeriod));
    }

    private Date parseDate(String text)
    {
        try
        {
            return DateUtils.parseDate(text);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 执行打卡（整组）：助理点「完成」/「撤回」/「补照片」，医生也能替助理记录
     * <p>
     * 点完成必须已经上传照片；状态记在「猫 + 日期 + 时间段」这一组上。
     */
    @PreAuthorize("@ss.hasPermi('cat:medication:edit')")
    @Log(title = "用药执行打卡", businessType = BusinessType.UPDATE)
    @PutMapping("/exec")
    public AjaxResult saveExec(@RequestBody CatMedicationExec request)
    {
        return success(medicationIdService.saveExec(request));
    }

    /** 医生改「这一次」的用药方案（一组内可增删药品），已完成打卡的组不允许改 */
    @PreAuthorize("@ss.hasPermi('cat:medication:edit')")
    @Log(title = "每日用药记录", businessType = BusinessType.UPDATE)
    @PutMapping("/group")
    public AjaxResult saveGroupPlan(@RequestBody CatMedicationGroup request)
    {
        medicationIdService.saveGroupPlan(request);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('cat:medication:export')")
    @Log(title = "每日用药记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatMedication medicationId)
    {
        List<CatMedication> list = medicationIdService.selectCatMedicationList(medicationId);
        refNameFiller.fill(list);
        ExcelUtil<CatMedication> util = new ExcelUtil<CatMedication>(CatMedication.class);
        util.exportExcel(response, list, "每日用药记录数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:medication:query')")
    @GetMapping(value = "/{medicationId}")
    public AjaxResult getInfo(@PathVariable("medicationId") Long medicationId)
    {
        return success(medicationIdService.selectCatMedicationById(medicationId));
    }

    @PreAuthorize("@ss.hasPermi('cat:medication:add')")
    @Log(title = "每日用药记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatMedication medicationId)
    {
        return toAjax(medicationIdService.insertCatMedication(medicationId));
    }

    /**
     * 批量录入连续多天用药：同一只猫、同一种药，从 useDate 起连续 days 天各生成一条记录
     * （需求文档 6.3「可批量快捷录入同一只猫连续多天用药」）
     */
    @PreAuthorize("@ss.hasPermi('cat:medication:add')")
    @Log(title = "每日用药记录", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult addBatch(@RequestBody CatMedication medication, @RequestParam(value = "days", defaultValue = "1") Integer days)
    {
        int count = medicationIdService.insertCatMedicationBatch(medication, days);
        return success("已生成 " + count + " 天用药记录");
    }

    @PreAuthorize("@ss.hasPermi('cat:medication:edit')")
    @Log(title = "每日用药记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatMedication medicationId)
    {
        return toAjax(medicationIdService.updateCatMedication(medicationId));
    }

    @PreAuthorize("@ss.hasPermi('cat:medication:remove')")
    @Log(title = "每日用药记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{medicationIds}")
    public AjaxResult remove(@PathVariable Long[] medicationIds)
    {
        return toAjax(medicationIdService.deleteCatMedicationByIds(medicationIds));
    }
}
