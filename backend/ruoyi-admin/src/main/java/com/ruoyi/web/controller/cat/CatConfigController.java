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
import com.ruoyi.cat.domain.CatConfig;
import com.ruoyi.cat.service.ICatConfigService;

/**
 * 系统配置 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/config")
public class CatConfigController extends BaseController
{
    @Autowired
    private ICatConfigService configIdService;

    @PreAuthorize("@ss.hasPermi('cat:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatConfig configId)
    {
        startPage();
        List<CatConfig> list = configIdService.selectCatConfigList(configId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:config:export')")
    @Log(title = "系统配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatConfig configId)
    {
        List<CatConfig> list = configIdService.selectCatConfigList(configId);
        ExcelUtil<CatConfig> util = new ExcelUtil<CatConfig>(CatConfig.class);
        util.exportExcel(response, list, "系统配置数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:config:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@PathVariable("configId") Long configId)
    {
        return success(configIdService.selectCatConfigById(configId));
    }

    @PreAuthorize("@ss.hasPermi('cat:config:add')")
    @Log(title = "系统配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatConfig configId)
    {
        return toAjax(configIdService.insertCatConfig(configId));
    }

    @PreAuthorize("@ss.hasPermi('cat:config:edit')")
    @Log(title = "系统配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatConfig configId)
    {
        return toAjax(configIdService.updateCatConfig(configId));
    }

    @PreAuthorize("@ss.hasPermi('cat:config:remove')")
    @Log(title = "系统配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        return toAjax(configIdService.deleteCatConfigByIds(configIds));
    }
}
