package com.ruoyi.cat.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.mapper.CatDeleteImpactMapper;
import com.ruoyi.cat.service.ICatDeleteImpactService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 删除前引用检查（通用）
 *
 * 把「删除会不会留下脏引用」统一成一次查询，前端据此决定：
 *   有引用 → 弹窗告知 + 确定按钮置灰
 *   没引用 → 普通确认
 * 见 docs/CONVENTIONS.md 第十四节。
 *
 * @author ruoyi
 */
@Service
public class CatDeleteImpactServiceImpl implements ICatDeleteImpactService
{
    /** 白名单：module 会拼进 SQL 的 choose 分支，必须在服务端锁死，不能由前端随意传 */
    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList(
            "base", "wardtype", "tag", "feecategory", "materialcategory", "material", "checkitem",
            "adoption", "prescription", "diagnosis", "profile"));

    /** 猫咪档案名下、带 del_flag 的业务表：删档案时一并软删 */
    private static final String[] PROFILE_SOFT_TABLES = {
            "cat_diagnosis", "cat_medication", "cat_prescription", "cat_vaccine", "cat_deworm",
            "cat_expense", "cat_tnr", "cat_release", "cat_adoption", "cat_death", "cat_referral" };

    /** 猫咪档案名下、没有 del_flag 的表：只能物理删 */
    private static final String[] PROFILE_HARD_TABLES = {
            "cat_photo", "cat_flow", "cat_reminder", "cat_visit" };

    @Autowired
    private CatDeleteImpactMapper deleteImpactMapper;

    @Override
    public List<Map<String, Object>> selectDeleteImpact(String module, Long[] ids)
    {
        if (ids == null || ids.length == 0)
        {
            return Collections.emptyList();
        }
        if (module == null || !ALLOWED.contains(module))
        {
            throw new ServiceException("不支持的检查类型：" + module);
        }
        return deleteImpactMapper.selectDeleteImpact(module, ids);
    }

    @Override
    public void assertNoReference(String module, Long[] ids, String entityLabel)
    {
        List<Map<String, Object>> list = selectDeleteImpact(module, ids);
        StringBuilder tip = new StringBuilder();
        for (Map<String, Object> row : list)
        {
            Object count = row.get("refCount");
            int refCount = count instanceof Number ? ((Number) count).intValue() : 0;
            if (refCount <= 0)
            {
                continue;
            }
            if (tip.length() > 0)
            {
                tip.append("；");
            }
            tip.append(row.get("name")).append("（").append(row.get("detail")).append("）");
        }
        if (tip.length() > 0)
        {
            throw new ServiceException("以下" + entityLabel + "还有被引用的数据，不能删除：" + tip
                    + "。请先处理这些引用后再删除。");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cascadeBeforeDelete(String module, Long[] ids)
    {
        if (ids == null || ids.length == 0 || module == null)
        {
            return;
        }
        switch (module)
        {
            case "adoption":
                // 回访计划属于领养记录本身，一起软删
                deleteImpactMapper.deleteVisitByAdoptionIds(ids);
                break;
            case "prescription":
                // 明细物理删；关联用药只解除关联，用药记录和库存台账都保留
                deleteImpactMapper.deletePrescriptionItemByPrescriptionIds(ids);
                deleteImpactMapper.clearMedicationPrescription(ids);
                break;
            case "diagnosis":
                deleteImpactMapper.deleteDiagnosisItemByDiagnosisIds(ids);
                deleteImpactMapper.clearMedicationDiagnosis(ids);
                deleteImpactMapper.clearVaccineDiagnosis(ids);
                deleteImpactMapper.clearDewormDiagnosis(ids);
                deleteImpactMapper.clearPrescriptionDiagnosis(ids);
                break;
            case "profile":
                deleteImpactMapper.deleteDiagnosisItemByCatIds(ids);
                deleteImpactMapper.deletePrescriptionItemByCatIds(ids);
                deleteImpactMapper.deleteMedicationExecByCatIds(ids);
                deleteImpactMapper.deleteProfileTagByCatIds(ids);
                for (String table : PROFILE_HARD_TABLES)
                {
                    deleteImpactMapper.hardDeleteByCatId(table, ids);
                }
                for (String table : PROFILE_SOFT_TABLES)
                {
                    deleteImpactMapper.softDeleteByCatId(table, ids);
                }
                break;
            default:
                break;
        }
    }
}
