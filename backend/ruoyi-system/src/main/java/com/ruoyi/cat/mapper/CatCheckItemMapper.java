package com.ruoyi.cat.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.cat.domain.CatCheckItem;

/**
 * 检查项目/用药目录 数据层
 *
 * @author ruoyi
 */
public interface CatCheckItemMapper
{
    public CatCheckItem selectCatCheckItemById(Long itemId);

    /** 列表（树形数据一次性拿全，前端转树） */
    public List<CatCheckItem> selectCatCheckItemList(CatCheckItem item);

    /** 开单用：只取启用的叶子项目（没有子节点的才算可选） */
    public List<CatCheckItem> selectSelectableItems(@Param("itemType") String itemType, @Param("keyword") String keyword);

    public int insertCatCheckItem(CatCheckItem item);

    public int updateCatCheckItem(CatCheckItem item);

    public int deleteCatCheckItemById(Long itemId);

    public int deleteCatCheckItemByIds(Long[] itemIds);

    /** 有没有子节点（有子节点的分类不允许删除，避免挂在下面看不到） */
    public int countChildren(Long itemId);

    /** 同一父级下名称是否重复（排除自己） */
    public int countSameName(@Param("parentId") Long parentId, @Param("itemName") String itemName, @Param("itemId") Long itemId);
}
