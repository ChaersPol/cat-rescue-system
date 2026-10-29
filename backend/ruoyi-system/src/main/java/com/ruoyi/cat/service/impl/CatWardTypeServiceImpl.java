package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatWardType;
import com.ruoyi.cat.mapper.CatWardMapper;
import com.ruoyi.cat.mapper.CatWardTypeMapper;
import com.ruoyi.cat.service.ICatWardTypeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 病房类型 服务层实现
 *
 * 两条业务规则：
 * 1) 类型名称不允许重复（重复会让 cat_ward.ward_type 与 ward_type_id 的对应关系变模糊）
 * 2) 还有病房在用的类型不允许删除，必须先把病房改到别的类型
 * 3) 类型改名时，把在用病房的 ward_type（名称冗余列）一起同步，避免两列不一致
 *
 * @author ruoyi
 */
@Service
public class CatWardTypeServiceImpl implements ICatWardTypeService
{
    @Autowired
    private CatWardTypeMapper catWardTypeMapper;

    @Autowired
    private CatWardMapper catWardMapper;

    @Override
    public CatWardType selectCatWardTypeById(Long typeId)
    {
        return catWardTypeMapper.selectCatWardTypeById(typeId);
    }

    @Override
    public List<CatWardType> selectCatWardTypeList(CatWardType catWardType)
    {
        return catWardTypeMapper.selectCatWardTypeList(catWardType);
    }

    @Override
    public List<CatWardType> selectCatWardTypeAll()
    {
        return catWardTypeMapper.selectCatWardTypeAll();
    }

    @Override
    public int insertCatWardType(CatWardType catWardType)
    {
        checkTypeNameUnique(catWardType);
        return catWardTypeMapper.insertCatWardType(catWardType);
    }

    @Override
    @Transactional
    public int updateCatWardType(CatWardType catWardType)
    {
        checkTypeNameUnique(catWardType);
        CatWardType old = catWardTypeMapper.selectCatWardTypeById(catWardType.getTypeId());
        if (old == null)
        {
            throw new ServiceException("病房类型不存在或已删除");
        }
        int rows = catWardTypeMapper.updateCatWardType(catWardType);
        String newName = catWardType.getTypeName();
        if (rows > 0 && StringUtils.isNotEmpty(newName) && !newName.equals(old.getTypeName()))
        {
            // 名称变了，把仍在用这个类型的病房的冗余名称列一起改掉
            catWardMapper.updateWardTypeNameByTypeId(catWardType.getTypeId(), newName);
        }
        return rows;
    }

    @Override
    @Transactional
    public int deleteCatWardTypeByIds(Long[] typeIds)
    {
        for (Long typeId : typeIds)
        {
            int used = catWardTypeMapper.countWardByTypeId(typeId);
            if (used > 0)
            {
                CatWardType type = catWardTypeMapper.selectCatWardTypeById(typeId);
                String name = type == null ? String.valueOf(typeId) : type.getTypeName();
                throw new ServiceException("「" + name + "」还有 " + used + " 个病房在用，请先把这些病房改到其它类型");
            }
        }
        return catWardTypeMapper.deleteCatWardTypeByIds(typeIds);
    }

    /**
     * 校验类型名称唯一
     */
    private void checkTypeNameUnique(CatWardType catWardType)
    {
        String typeName = catWardType.getTypeName();
        if (StringUtils.isEmpty(typeName))
        {
            throw new ServiceException("类型名称不能为空");
        }
        catWardType.setTypeName(typeName.trim());
        int count = catWardTypeMapper.countByTypeName(catWardType.getTypeName(), catWardType.getTypeId());
        if (count > 0)
        {
            throw new ServiceException("类型名称「" + catWardType.getTypeName() + "」已存在");
        }
    }
}
