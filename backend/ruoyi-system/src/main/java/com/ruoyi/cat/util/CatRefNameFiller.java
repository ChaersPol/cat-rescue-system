package com.ruoyi.cat.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.cat.mapper.CatRefNameMapper;

/**
 * 导出前把裸外键 ID 换成可读的「名称（标识）」
 *
 * 用法：Controller 的 export 方法里，查到 list 之后加一行 refNameFiller.fill(list)。
 * 实体的 xxxRefName 字段是影子字段（非表字段），只用于导出展示。
 *
 * 新增外键时只需两步：实体加 xxxRefName 字段 + 本类 RULES 里加一条规则。
 *
 * @author ruoyi
 */
@Component
public class CatRefNameFiller
{
    private static final Logger log = LoggerFactory.getLogger(CatRefNameFiller.class);

    @Autowired
    private CatRefNameMapper refNameMapper;

    /** 规则：源字段 -> 影子字段，kind 对应 Mapper 里的查询 */
    private static class Rule
    {
        final String src;
        final String dest;
        final String kind;

        Rule(String src, String dest, String kind)
        {
            this.src = src;
            this.dest = dest;
            this.kind = kind;
        }
    }

    /** 实体简单类名 -> 该实体的回填规则 */
    private static final Map<String, List<Rule>> RULES = new HashMap<>();

    /** 字段反射缓存，避免每条记录重复查找（null 用 Optional 包一层，ConcurrentHashMap 不允许 null 值） */
    private static final Map<String, Optional<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private static void rule(String entity, String src, String dest, String kind)
    {
        RULES.computeIfAbsent(entity, k -> new ArrayList<>()).add(new Rule(src, dest, kind));
    }

    static
    {
        rule("CatAdoption", "catId", "catRefName", "cat");
        rule("CatVisit", "catId", "catRefName", "cat");
        rule("CatVisit", "adoptionId", "adoptionRefName", "adoption");
        rule("CatDeath", "catId", "catRefName", "cat");
        rule("CatRelease", "catId", "catRefName", "cat");
        rule("CatRelease", "tnrId", "tnrRefName", "tnr");
        rule("CatTnr", "catId", "catRefName", "cat");
        rule("CatVaccine", "catId", "catRefName", "cat");
        rule("CatDeworm", "catId", "catRefName", "cat");
        rule("CatMedication", "catId", "catRefName", "cat");
        rule("CatPhoto", "catId", "catRefName", "cat");
        rule("CatReminder", "catId", "catRefName", "cat");
        rule("CatDiagnosis", "catId", "catRefName", "cat");
        rule("CatDiagnosis", "baseId", "baseRefName", "base");
        rule("CatReferral", "catId", "catRefName", "cat");
        rule("CatReferral", "targetBaseId", "targetBaseRefName", "base");
        rule("CatFlow", "catId", "catRefName", "cat");
        rule("CatFlow", "fromBaseId", "fromBaseRefName", "base");
        rule("CatFlow", "toBaseId", "toBaseRefName", "base");
        rule("CatFlow", "fromWardId", "fromWardRefName", "ward");
        rule("CatFlow", "toWardId", "toWardRefName", "ward");
        rule("CatExpense", "catId", "catRefName", "cat");
        rule("CatExpense", "baseId", "baseRefName", "base");
        rule("CatExpense", "categoryId", "feeCategoryRefName", "feeCategory");
        rule("CatExpense", "referralId", "referralRefName", "referral");
        rule("CatExpense", "stockId", "stockRefName", "stock");
        rule("CatWard", "baseId", "baseRefName", "base");
        rule("CatMaterial", "categoryId", "materialCategoryRefName", "materialCategory");
        rule("CatStockRecord", "materialId", "materialRefName", "material");
        rule("CatStockRecord", "expenseId", "expenseRefName", "expense");
        rule("CatWardAuth", "userId", "userRefName", "user");
        rule("CatWardAuth", "wardId", "wardRefName", "ward");
    }

    /**
     * 批量回填关联名称。任何异常都只记日志，不中断导出。
     *
     * @param list 待导出数据
     */
    public void fill(List<?> list)
    {
        if (refNameMapper == null || list == null || list.isEmpty())
        {
            return;
        }
        try
        {
            // 1. 按 kind 汇总待查 ID
            Map<String, Set<Long>> idsByKind = new LinkedHashMap<>();
            for (Object row : list)
            {
                for (Rule r : rulesOf(row))
                {
                    Long id = readId(row, r.src);
                    if (id != null)
                    {
                        idsByKind.computeIfAbsent(r.kind, k -> new LinkedHashSet<>()).add(id);
                    }
                }
            }
            if (idsByKind.isEmpty())
            {
                return;
            }
            // 2. 每种 kind 一次批量查询
            Map<String, Map<Long, String>> labels = new HashMap<>();
            for (Map.Entry<String, Set<Long>> e : idsByKind.entrySet())
            {
                labels.put(e.getKey(), query(e.getKey(), e.getValue()));
            }
            // 3. 回填
            for (Object row : list)
            {
                for (Rule r : rulesOf(row))
                {
                    Long id = readId(row, r.src);
                    if (id == null)
                    {
                        continue;
                    }
                    String label = labels.getOrDefault(r.kind, Collections.emptyMap()).get(id);
                    if (label != null)
                    {
                        writeField(row, r.dest, label);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            log.warn("导出关联名称回填失败，按原始ID导出：{}", ex.getMessage());
        }
    }

    private Map<Long, String> query(String kind, Set<Long> ids)
    {
        Map<Long, String> result = new HashMap<>();
        List<Map<String, Object>> rows;
        try
        {
            switch (kind)
            {
                case "cat": rows = refNameMapper.selectCatLabels(ids); break;
                case "base": rows = refNameMapper.selectBaseLabels(ids); break;
                case "ward": rows = refNameMapper.selectWardLabels(ids); break;
                case "feeCategory": rows = refNameMapper.selectFeeCategoryLabels(ids); break;
                case "materialCategory": rows = refNameMapper.selectMaterialCategoryLabels(ids); break;
                case "material": rows = refNameMapper.selectMaterialLabels(ids); break;
                case "adoption": rows = refNameMapper.selectAdoptionLabels(ids); break;
                case "referral": rows = refNameMapper.selectReferralLabels(ids); break;
                case "tnr": rows = refNameMapper.selectTnrLabels(ids); break;
                case "stock": rows = refNameMapper.selectStockLabels(ids); break;
                case "expense": rows = refNameMapper.selectExpenseLabels(ids); break;
                case "user": rows = refNameMapper.selectUserLabels(ids); break;
                default: return result;
            }
        }
        catch (Exception ex)
        {
            log.warn("导出关联名称查询失败 kind={}：{}", kind, ex.getMessage());
            return result;
        }
        for (Map<String, Object> row : rows)
        {
            Object id = row.get("id");
            Object label = row.get("label");
            if (id instanceof Number && label != null)
            {
                result.put(((Number) id).longValue(), String.valueOf(label));
            }
        }
        return result;
    }

    private List<Rule> rulesOf(Object row)
    {
        return RULES.getOrDefault(row.getClass().getSimpleName(), Collections.emptyList());
    }

    private Long readId(Object row, String name)
    {
        try
        {
            Field f = fieldOf(row.getClass(), name);
            if (f == null)
            {
                return null;
            }
            Object v = f.get(row);
            return (v instanceof Number) ? ((Number) v).longValue() : null;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private void writeField(Object row, String name, String value)
    {
        try
        {
            Field f = fieldOf(row.getClass(), name);
            if (f != null)
            {
                f.set(row, value);
            }
        }
        catch (Exception ex)
        {
            log.warn("导出关联名称写入失败 {}.{}：{}", row.getClass().getSimpleName(), name, ex.getMessage());
        }
    }

    private Field fieldOf(Class<?> clazz, String name)
    {
        String key = clazz.getName() + "#" + name;
        Optional<Field> cached = FIELD_CACHE.get(key);
        if (cached != null)
        {
            return cached.orElse(null);
        }
        Optional<Field> found;
        try
        {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            found = Optional.of(f);
        }
        catch (NoSuchFieldException ex)
        {
            found = Optional.empty();
        }
        FIELD_CACHE.put(key, found);
        return found.orElse(null);
    }
}
