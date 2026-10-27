package com.ruoyi.web.controller.cat;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
import com.ruoyi.cat.domain.CatWardAuth;
import com.ruoyi.cat.service.ICatWardAuthService;

/**
 * 病房授权 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/wardauth")
public class CatWardAuthController extends BaseController
{
    @Autowired
    private ICatWardAuthService catWardAuthService;

    /**
     * 查询病房授权列表
     */
    @PreAuthorize("@ss.hasPermi('cat:wardauth:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatWardAuth wardAuth)
    {
        startPage();
        List<CatWardAuth> list = catWardAuthService.selectCatWardAuthList(wardAuth);
        return getDataTable(list);
    }

    /**
     * 查询某用户授权的病房id列表
     */
    @PreAuthorize("@ss.hasPermi('cat:wardauth:list')")
    @GetMapping("/wards/{userId}")
    public AjaxResult wardsByUser(@PathVariable("userId") Long userId)
    {
        return success(catWardAuthService.selectWardIdsByUserId(userId));
    }

    /**
     * 查询某病房授权的用户id列表
     */
    @PreAuthorize("@ss.hasPermi('cat:wardauth:list')")
    @GetMapping("/users/{wardId}")
    public AjaxResult usersByWard(@PathVariable("wardId") Long wardId)
    {
        return success(catWardAuthService.selectUserIdsByWardId(wardId));
    }

    /**
     * 保存某用户的病房授权（先删后增）
     */
    @PreAuthorize("@ss.hasPermi('cat:wardauth:edit')")
    @Log(title = "病房授权", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    public AjaxResult save(@RequestBody Map<String, Object> body)
    {
        Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
        List<?> wardList = (List<?>) body.get("wardIds");
        Long[] wardIds = wardList == null ? new Long[0] : wardList.stream()
                .map(o -> Long.valueOf(o.toString())).toArray(Long[]::new);
        return toAjax(catWardAuthService.saveUserWardAuth(userId, wardIds));
    }

    /**
     * 新增单条授权
     */
    @PreAuthorize("@ss.hasPermi('cat:wardauth:add')")
    @Log(title = "病房授权", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatWardAuth wardAuth)
    {
        return toAjax(catWardAuthService.insertCatWardAuth(wardAuth));
    }

    /**
     * 删除某用户全部授权
     */
    @PreAuthorize("@ss.hasPermi('cat:wardauth:remove')")
    @Log(title = "病房授权", businessType = BusinessType.DELETE)
    @PostMapping("/removeUser/{userId}")
    public AjaxResult removeByUser(@PathVariable("userId") Long userId)
    {
        return toAjax(catWardAuthService.deleteByUserId(userId));
    }
}
