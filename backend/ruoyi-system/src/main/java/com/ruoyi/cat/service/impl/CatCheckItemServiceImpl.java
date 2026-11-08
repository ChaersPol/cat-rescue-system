package com.ruoyi.cat.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatCheckItem;
import com.ruoyi.cat.mapper.CatCheckItemMapper;
import com.ruoyi.cat.service.ICatCheckItemService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 检查项目/用药目录 服务层实现
 *
 * @author ruoyi
 */
@Service
public class CatCheckItemServiceImpl implements ICatCheckItemService
{
    @Autowired
    private CatCheckItemMapper checkItemMapper;

    @Override
    public CatCheckItem selectCatCheckItemById(Long itemId)
    {
        return checkItemMapper.selectCatCheckItemById(itemId);
    }

    @Override
    public List<CatCheckItem> selectCatCheckItemList(CatCheckItem item)
    {
        return checkItemMapper.selectCatCheckItemList(item);
    }

    @Override
    public List<CatCheckItem> selectSelectableItems(String itemType, String keyword)
    {
        return checkItemMapper.selectSelectableItems(itemType, keyword);
    }

    @Override
    public int insertCatCheckItem(CatCheckItem item)
    {
        validate(item, false);
        item.setAncestors(buildAncestors(item.getParentId()));
        if (StringUtils.isEmpty(item.getStatus()))
        {
            item.setStatus("0");
        }
        if (item.getOrderNum() == null)
        {
            item.setOrderNum(0);
        }
        if (item.getPrice() == null)
        {
            item.setPrice(BigDecimal.ZERO);
        }
        // 分类节点不挂物资，避免开单时误扣库存
        return checkItemMapper.insertCatCheckItem(item);
    }

    @Override
    public int updateCatCheckItem(CatCheckItem item)
    {
        CatCheckItem old = checkItemMapper.selectCatCheckItemById(item.getItemId());
        if (old == null)
        {
            throw new ServiceException("目录项不存在或已删除");
        }
        // 0 表示"不关联物资"，统一存 null
        if (item.getMaterialId() != null && item.getMaterialId() == 0L)
        {
            item.setMaterialId(null);
        }
        validate(item, true);
        if (item.getParentId() != null && !item.getParentId().equals(old.getParentId()))
        {
            checkParent(item.getParentId(), item.getItemId());
            item.setAncestors(buildAncestors(item.getParentId()));
        }
        return checkItemMapper.updateCatCheckItem(item);
    }

    @Override
    public int deleteCatCheckItemByIds(Long[] itemIds)
    {
        for (Long itemId : itemIds)
        {
            if (checkItemMapper.countChildren(itemId) > 0)
            {
                CatCheckItem item = checkItemMapper.selectCatCheckItemById(itemId);
                throw new ServiceException("「" + (item == null ? itemId : item.getItemName())
                        + "」下面还有子项目，请先删除子项目");
            }
        }
        return checkItemMapper.deleteCatCheckItemByIds(itemIds);
    }

    /** 父级链：0 / 0,3001 / 0,3001,3002 */
    private String buildAncestors(Long parentId)
    {
        if (parentId == null || parentId == 0L)
        {
            return "0";
        }
        CatCheckItem parent = checkItemMapper.selectCatCheckItemById(parentId);
        if (parent == null)
        {
            throw new ServiceException("上级项目不存在");
        }
        String base = StringUtils.isEmpty(parent.getAncestors()) ? "0" : parent.getAncestors();
        return base + "," + parentId;
    }

    private void validate(CatCheckItem item, boolean isUpdate)
    {
        if (item == null || StringUtils.isEmpty(item.getItemName()))
        {
            throw new ServiceException("名称不能为空");
        }
        if (item.getParentId() == null)
        {
            item.setParentId(0L);
        }
        checkParent(item.getParentId(), isUpdate ? item.getItemId() : null);
        if (StringUtils.isEmpty(item.getItemType()))
        {
            // 没选类型时跟着上级走，顶级默认检查项目
            CatCheckItem parent = item.getParentId() == 0L ? null : checkItemMapper.selectCatCheckItemById(item.getParentId());
            item.setItemType(parent == null ? "1" : parent.getItemType());
        }
        int same = checkItemMapper.countSameName(item.getParentId(), item.getItemName(),
                isUpdate ? item.getItemId() : null);
        if (same > 0)
        {
            throw new ServiceException("同一级下已经有「" + item.getItemName() + "」了，换个名字或换上级");
        }
    }

    /** 上级必须存在，且不能挂到自己或自己的子孙下面（否则整棵树断开） */
    private void checkParent(Long parentId, Long selfId)
    {
        if (parentId == null || parentId == 0L)
        {
            return;
        }
        if (selfId != null && parentId.equals(selfId))
        {
            throw new ServiceException("上级不能选自己");
        }
        CatCheckItem parent = checkItemMapper.selectCatCheckItemById(parentId);
        if (parent == null)
        {
            throw new ServiceException("上级项目不存在");
        }
        if (selfId != null && StringUtils.isNotEmpty(parent.getAncestors())
                && ("," + parent.getAncestors() + ",").contains("," + selfId + ","))
        {
            throw new ServiceException("上级不能选自己的下级");
        }
    }
}
