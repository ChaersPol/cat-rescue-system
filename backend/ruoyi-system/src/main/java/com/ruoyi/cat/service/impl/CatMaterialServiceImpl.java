package com.ruoyi.cat.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatMaterial;
import com.ruoyi.cat.mapper.CatMaterialMapper;
import com.ruoyi.cat.service.ICatMaterialService;

/**
 * 物资 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatMaterialServiceImpl implements ICatMaterialService
{
    @Autowired
    private CatMaterialMapper materialIdMapper;

    @Override
    public CatMaterial selectCatMaterialById(Long materialId)
    {
        return materialIdMapper.selectCatMaterialById(materialId);
    }

    @Override
    public List<CatMaterial> selectCatMaterialList(CatMaterial materialId)
    {
        return materialIdMapper.selectCatMaterialList(materialId);
    }

    @Override
    public int insertCatMaterial(CatMaterial materialId)
    {
        return materialIdMapper.insertCatMaterial(materialId);
    }

    @Override
    public int updateCatMaterial(CatMaterial materialId)
    {
        return materialIdMapper.updateCatMaterial(materialId);
    }

    @Override
    public int deleteCatMaterialByIds(Long[] materialIds)
    {
        return materialIdMapper.deleteCatMaterialByIds(materialIds);
    }
}
