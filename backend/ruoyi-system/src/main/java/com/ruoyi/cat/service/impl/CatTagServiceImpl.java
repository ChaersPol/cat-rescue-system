package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatTag;
import com.ruoyi.cat.mapper.CatTagMapper;
import com.ruoyi.cat.service.ICatTagService;

/**
 * 标签 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatTagServiceImpl implements ICatTagService
{
    @Autowired
    private CatTagMapper tagIdMapper;

    @Override
    public CatTag selectCatTagById(Long tagId)
    {
        return tagIdMapper.selectCatTagById(tagId);
    }

    @Override
    public List<CatTag> selectCatTagList(CatTag tagId)
    {
        return tagIdMapper.selectCatTagList(tagId);
    }

    @Override
    public int insertCatTag(CatTag tagId)
    {
        return tagIdMapper.insertCatTag(tagId);
    }

    @Override
    public int updateCatTag(CatTag tagId)
    {
        return tagIdMapper.updateCatTag(tagId);
    }

    @Override
    public int deleteCatTagByIds(Long[] tagIds)
    {
        return tagIdMapper.deleteCatTagByIds(tagIds);
    }
}
