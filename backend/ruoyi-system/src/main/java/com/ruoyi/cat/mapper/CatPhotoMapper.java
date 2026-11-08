package com.ruoyi.cat.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.cat.domain.CatPhoto;

/**
 * 猫咪照片 数据层
 * 
 * @author ruoyi
 */
public interface CatPhotoMapper
{
    public CatPhoto selectCatPhotoById(Long photoId);

    public List<CatPhoto> selectCatPhotoList(CatPhoto photoId);

    /** 档案里出现过的品种清单（相册筛选下拉用） */
    public List<String> selectBreedOptions();

    /** 按猫聚合的相册信息卡（封面取最新照片） */
    public List<Map<String, Object>> selectPhotoCatCards(CatPhoto query);

    public int insertCatPhoto(CatPhoto photoId);

    public int updateCatPhoto(CatPhoto photoId);

    public int deleteCatPhotoById(Long photoId);

    public int deleteCatPhotoByIds(Long[] photoIds);
}
