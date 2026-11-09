package com.ruoyi.cat.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatWard;
import com.ruoyi.cat.domain.CatWardType;
import com.ruoyi.cat.mapper.CatWardMapper;
import com.ruoyi.cat.service.ICatWardService;
import com.ruoyi.cat.service.ICatWardTypeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 病房 服务层实现
 *
 * 病房类型是「基础信息 → 病房类型」维护的字典，这里负责把 wardTypeId
 * 翻译成 ward_type 名称（冗余列，供导出与历史数据使用），保证两列一致。
 *
 * @author ruoyi
 */
@Service
public class CatWardServiceImpl implements ICatWardService
{
    @Autowired
    private CatWardMapper wardIdMapper;

    @Autowired
    private ICatWardTypeService catWardTypeService;

    @Override
    public CatWard selectCatWardById(Long wardId)
    {
        return wardIdMapper.selectCatWardById(wardId);
    }

    @Override
    public List<CatWard> selectCatWardList(CatWard wardId)
    {
        return wardIdMapper.selectCatWardList(wardId);
    }

    @Override
    public int insertCatWard(CatWard wardId)
    {
        fillWardTypeName(wardId);
        return wardIdMapper.insertCatWard(wardId);
    }

    @Override
    public int updateCatWard(CatWard wardId)
    {
        fillWardTypeName(wardId);
        return wardIdMapper.updateCatWard(wardId);
    }

    /**
     * 删除病房。
     *
     * 病房是逻辑删除，而 cat_profile.ward_id 不会跟着清空，所以「还有猫住着就把它删掉」
     * 会留下指向已删病房的脏引用：
     *   ① 档案列表「所在病房」显示「—」（前端只能从有效病房列表里查名字）
     *   ② 助理按病房授权过滤（ward_id IN 授权病房），这些猫永远不可见
     * 2026-11-09 线上就因此出现过「管理员看到 7 只、助理只看到 4 只」。
     * 所以这里先在册校验，并连带清掉指向这些病房的授权行。
     */
    @Override
    public int deleteCatWardByIds(Long[] wardIds)
    {
        if (wardIds == null || wardIds.length == 0)
        {
            return 0;
        }
        List<Map<String, Object>> occupied = wardIdMapper.selectWardCatCounts(wardIds);
        if (occupied != null && !occupied.isEmpty())
        {
            StringBuilder tip = new StringBuilder();
            for (Map<String, Object> row : occupied)
            {
                if (tip.length() > 0)
                {
                    tip.append("、");
                }
                tip.append(row.get("wardName")).append("（").append(row.get("catCount")).append("只）");
            }
            throw new ServiceException("以下病房还有在册猫咪，不能删除：" + tip
                    + "。请先到「猫咪档案」把这些猫咪转到其它病房，再删除病房。");
        }
        // 没有猫了，顺手把指向这些病房的授权行清掉，避免助理侧残留死授权
        wardIdMapper.deleteWardAuthByWardIds(wardIds);
        return wardIdMapper.deleteCatWardByIds(wardIds);
    }

    /**
     * 删除确认用：返回所选病房各自的在册猫数量。
     * 与删除校验的 selectWardCatCounts 不同，这里「0 只」的病房也会返回，
     * 前端据此区分「还能删」和「不能删」，不能只靠有没有返回行来判断。
     */
    @Override
    public List<Map<String, Object>> selectWardCatCounts(Long[] wardIds)
    {
        if (wardIds == null || wardIds.length == 0)
        {
            return java.util.Collections.emptyList();
        }
        return wardIdMapper.selectWardCatCountList(wardIds);
    }

    /**
     * 由 wardTypeId 回填 wardType 名称。
     * 没传类型时保持原样（兼容只改状态之类的局部更新）。
     */
    private void fillWardTypeName(CatWard ward)
    {
        if (ward.getWardTypeId() == null)
        {
            return;
        }
        CatWardType type = catWardTypeService.selectCatWardTypeById(ward.getWardTypeId());
        if (type == null)
        {
            throw new ServiceException("所选病房类型不存在或已删除，请刷新后重选");
        }
        if (StringUtils.isEmpty(type.getTypeName()))
        {
            throw new ServiceException("所选病房类型名称为空，请检查「基础信息 → 病房类型」");
        }
        ward.setWardType(type.getTypeName());
    }
}
