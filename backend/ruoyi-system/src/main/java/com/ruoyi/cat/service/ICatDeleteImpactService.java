package com.ruoyi.cat.service;

import java.util.List;
import java.util.Map;

/**
 * 删除前引用检查（通用）
 *
 * @author ruoyi
 */
public interface ICatDeleteImpactService
{
    /**
     * 查询所选数据被谁引用（没有引用的行也会返回，refCount = 0）
     *
     * @param module 模块标识，非法值直接抛异常
     * @param ids    待删除主键
     */
    public List<Map<String, Object>> selectDeleteImpact(String module, Long[] ids);

    /**
     * 删除前的后端兜底校验：只要有一条被引用就抛 ServiceException。
     * 前端弹窗只是体验层，并发/绕过请求时这里才是最终防线。
     *
     * @param module      模块标识
     * @param ids         待删除主键
     * @param entityLabel 文案里的对象名，如「基地」「物资分类」
     */
    public void assertNoReference(String module, Long[] ids, String entityLabel);

    /**
     * 删除主记录之前，先把它自己的明细/子记录清掉（必须在同一事务里调）。
     * 只处理「属于我自己的子记录」；别人对我的引用只解除关联，记录本身保留。
     *
     * @param module adoption / prescription / diagnosis / profile
     * @param ids    待删除主键
     */
    public void cascadeBeforeDelete(String module, Long[] ids);
}
