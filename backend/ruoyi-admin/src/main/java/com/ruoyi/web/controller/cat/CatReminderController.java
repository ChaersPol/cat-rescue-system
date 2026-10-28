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
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.cat.domain.CatReminder;
import com.ruoyi.cat.service.ICatReminderService;
import com.ruoyi.cat.service.impl.CatReminderGenerateService;
import com.ruoyi.cat.util.CatRefNameFiller;
import com.ruoyi.system.service.ISysRoleService;

/**
 * 提醒通知 控制器
 *
 * 已读状态按登录用户独立记录，管理员不受目标用户范围限制。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/reminder")
public class CatReminderController extends BaseController
{
    /** 顶栏铃铛最多展示的条数 */
    private static final int HEADER_LIMIT = 5;

    @Autowired
    private ICatReminderService reminderIdService;

    @Autowired
    private CatReminderGenerateService catReminderGenerateService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private CatRefNameFiller refNameFiller;

    /**
     * 手动生成到期提醒
     */
    @PreAuthorize("@ss.hasPermi('cat:reminder:list')")
    @PostMapping("/generate")
    public AjaxResult generate()
    {
        int count = catReminderGenerateService.generateAll();
        return success("生成提醒成功，共 " + count + " 条");
    }

    /**
     * 顶栏铃铛数据：当前用户可见的前 5 条 + 未读数
     * 只要求登录，不校验具体菜单权限，保证任何账号都能看到与自己相关的提醒
     */
    @GetMapping("/header")
    public AjaxResult header()
    {
        CatReminder scope = currentScope();
        List<CatReminder> list = reminderIdService.selectHeaderReminders(scope, HEADER_LIMIT);
        AjaxResult result = AjaxResult.success(list);
        result.put("unreadCount", reminderIdService.selectUnreadCount(scope));
        return result;
    }

    /**
     * 标记单条提醒为已读（只影响当前登录用户）
     */
    @PutMapping("/read/{reminderId}")
    public AjaxResult markRead(@PathVariable("reminderId") Long reminderId)
    {
        // 幂等：已读过时 insert ignore 不会产生影响
        reminderIdService.markRead(reminderId, getUserId());
        return success();
    }

    /**
     * 批量标记提醒为已读（只影响当前登录用户）
     */
    @PutMapping("/batchRead")
    public AjaxResult markReadBatch(@RequestBody Long[] reminderIds)
    {
        int count = reminderIdService.markReadBatch(reminderIds, getUserId());
        return success("已标记 " + count + " 条为已读");
    }

    /**
     * 当前用户一键全部已读
     */
    @PutMapping("/readAll")
    public AjaxResult markAllRead()
    {
        int count = reminderIdService.markAllRead(currentScope());
        return success("已全部标记为已读，共 " + count + " 条");
    }

    @PreAuthorize("@ss.hasPermi('cat:reminder:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatReminder reminderId)
    {
        applyScope(reminderId);
        startPage();
        List<CatReminder> list = reminderIdService.selectCatReminderList(reminderId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:reminder:export')")
    @Log(title = "提醒通知", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatReminder reminderId)
    {
        applyScope(reminderId);
        List<CatReminder> list = reminderIdService.selectCatReminderList(reminderId);
        ExcelUtil<CatReminder> util = new ExcelUtil<CatReminder>(CatReminder.class);
        util.exportExcel(response, list, "提醒通知数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:reminder:query')")
    @GetMapping(value = "/{reminderId}")
    public AjaxResult getInfo(@PathVariable("reminderId") Long reminderId)
    {
        return success(reminderIdService.selectCatReminderById(reminderId));
    }

    @PreAuthorize("@ss.hasPermi('cat:reminder:add')")
    @Log(title = "提醒通知", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatReminder reminderId)
    {
        return toAjax(reminderIdService.insertCatReminder(reminderId));
    }

    @PreAuthorize("@ss.hasPermi('cat:reminder:edit')")
    @Log(title = "提醒通知", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatReminder reminderId)
    {
        return toAjax(reminderIdService.updateCatReminder(reminderId));
    }

    @PreAuthorize("@ss.hasPermi('cat:reminder:remove')")
    @Log(title = "提醒通知", businessType = BusinessType.DELETE)
    @DeleteMapping("/{reminderIds}")
    public AjaxResult remove(@PathVariable Long[] reminderIds)
    {
        return toAjax(reminderIdService.deleteCatReminderByIds(reminderIds));
    }

    /**
     * 构造一个只带用户范围信息的查询对象
     */
    private CatReminder currentScope()
    {
        CatReminder scope = new CatReminder();
        applyScope(scope);
        return scope;
    }

    /**
     * 把当前登录用户信息写入查询条件
     */
    private void applyScope(CatReminder reminder)
    {
        Long userId = getUserId();
        reminder.setCurrentUserId(userId);
        reminder.setCurrentUserName(getUsername());
        reminder.setScopeAll(hasFullDataScope(userId));
    }

    /**
     * 是否具备全站数据权限：内置超级管理员，或角色数据范围为「全部数据权限」
     * 拥有全站视野的人应当能看到全部提醒，否则会漏掉其他岗位的待办
     */
    private boolean hasFullDataScope(Long userId)
    {
        if (SecurityUtils.isAdmin(userId))
        {
            return true;
        }
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        if (roles != null)
        {
            for (SysRole role : roles)
            {
                if ("1".equals(role.getDataScope()))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
