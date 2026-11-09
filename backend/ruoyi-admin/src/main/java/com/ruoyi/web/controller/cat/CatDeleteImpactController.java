package com.ruoyi.web.controller.cat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.cat.service.ICatDeleteImpactService;

/**
 * 删除前引用检查（通用）
 *
 * 前端点「删除」时先调这里，拿到每个对象的引用数：
 *   有引用 → 弹窗告知「还有 N 只猫咪在用」，确定按钮置灰
 *   没引用 → 普通确认，允许删除
 * 具体规则见 docs/CONVENTIONS.md 第十四节。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/deleteImpact")
public class CatDeleteImpactController extends BaseController
{
    @Autowired
    private ICatDeleteImpactService deleteImpactService;

    /**
     * @param module base / wardtype / tag / feecategory / materialcategory / material / checkitem
     * @param ids    逗号分隔的主键，如 100,101
     */
    @GetMapping("/{module}")
    public AjaxResult impact(@PathVariable String module, @RequestParam Long[] ids)
    {
        return success(deleteImpactService.selectDeleteImpact(module, ids));
    }
}
