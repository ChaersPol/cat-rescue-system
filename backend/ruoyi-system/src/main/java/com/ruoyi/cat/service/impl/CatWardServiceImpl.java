package com.ruoyi.cat.service.impl;

import java.util.List;
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

    @Override
    public int deleteCatWardByIds(Long[] wardIds)
    {
        return wardIdMapper.deleteCatWardByIds(wardIds);
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
