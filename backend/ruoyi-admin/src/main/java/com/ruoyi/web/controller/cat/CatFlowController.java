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
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.service.ICatFlowService;
import com.ruoyi.cat.service.ICatTransferService;
import com.ruoyi.cat.util.CatRefNameFiller;

/**
 * 流转记录 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/flow")
public class CatFlowController extends BaseController
{
    @Autowired
    private ICatFlowService flowIdService;

    @Autowired
    private ICatTransferService catTransferService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    @PreAuthorize("@ss.hasPermi('cat:flow:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatFlow flowId)
    {
        startPage();
        List<CatFlow> list = flowIdService.selectCatFlowList(flowId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:flow:export')")
    @Log(title = "流转记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatFlow flowId)
    {
        List<CatFlow> list = flowIdService.selectCatFlowList(flowId);
        refNameFiller.fill(list);
        ExcelUtil<CatFlow> util = new ExcelUtil<CatFlow>(CatFlow.class);
        util.exportExcel(response, list, "流转记录数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:flow:query')")
    @GetMapping(value = "/{flowId}")
    public AjaxResult getInfo(@PathVariable("flowId") Long flowId)
    {
        return success(flowIdService.selectCatFlowById(flowId));
    }

    /**
     * 发起流转：写入流转记录并联动更新猫咪状态、所在基地/病房
     */
    @PreAuthorize("@ss.hasPermi('cat:flow:add')")
    @Log(title = "流转记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatFlow flowId)
    {
        return toAjax(catTransferService.transferByFlow(flowId));
    }

    @PreAuthorize("@ss.hasPermi('cat:flow:edit')")
    @Log(title = "流转记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatFlow flowId)
    {
        return toAjax(flowIdService.updateCatFlow(flowId));
    }

    /**
     * 流转记录为不可删除的业务凭证，如需更正请新增反向流转记录
     */
    @PreAuthorize("@ss.hasPermi('cat:flow:remove')")
    @DeleteMapping("/{flowIds}")
    public AjaxResult remove(@PathVariable Long[] flowIds)
    {
        return AjaxResult.error("流转记录为不可删除的业务凭证，如需更正请新增一条反向流转记录");
    }
}
