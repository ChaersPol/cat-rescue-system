package com.ruoyi.cat.mapper;

import java.util.List;
import com.ruoyi.cat.domain.CatTag;

/**
 * 标签 数据层
 * 
 * @author ruoyi
 */
public interface CatTagMapper
{
    public CatTag selectCatTagById(Long tagId);

    public List<CatTag> selectCatTagList(CatTag tagId);

    public int insertCatTag(CatTag tagId);

    public int updateCatTag(CatTag tagId);

    public int deleteCatTagById(Long tagId);

    public int deleteCatTagByIds(Long[] tagIds);
}
