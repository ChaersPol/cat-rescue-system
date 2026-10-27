package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatTag;

/**
 * 标签 服务层
 * 
 * @author ruoyi
 */
public interface ICatTagService
{
    public CatTag selectCatTagById(Long tagId);

    public List<CatTag> selectCatTagList(CatTag tagId);

    public int insertCatTag(CatTag tagId);

    public int updateCatTag(CatTag tagId);

    public int deleteCatTagByIds(Long[] tagIds);
}
