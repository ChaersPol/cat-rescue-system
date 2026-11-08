package com.ruoyi.web.controller.cat;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.cat.service.ICatStaffService;

/**
 * 在职员工账号 控制器
 * <p>
 * 供用药、疫苗、回访、死亡、TNR、物资等页面的「人员」下拉使用。
 * 不做权限点限制：只要登录了就能读取姓名列表（不返回手机号等敏感信息）。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/staff")
public class CatStaffController extends BaseController
{
    @Autowired
    private ICatStaffService staffService;

    /**
     * 人员候选
     *
     * @param scope doctor / assistant / all
     */
    @GetMapping("/options")
    public AjaxResult options(@RequestParam(required = false, defaultValue = "all") String scope)
    {
        List<Map<String, Object>> list = staffService.selectOptions(scope);
        return success(list);
    }
}
