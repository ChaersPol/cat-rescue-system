package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatCheckItem;

/**
 * 检查项目/用药目录 服务层
 *
 * @author ruoyi
 */
public interface ICatCheckItemService
{
    public CatCheckItem selectCatCheckItemById(Long itemId);

    public List<CatCheckItem> selectCatCheckItemList(CatCheckItem item);

    /** 开单可选项目（启用的叶子节点，带关联物资库存） */
    public List<CatCheckItem> selectSelectableItems(String itemType, String keyword);

    public int insertCatCheckItem(CatCheckItem item);

    public int updateCatCheckItem(CatCheckItem item);

    public int deleteCatCheckItemByIds(Long[] itemIds);
}
