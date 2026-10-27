package com.ruoyi.cat.mapper;

import java.util.List;
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

    public int insertCatPhoto(CatPhoto photoId);

    public int updateCatPhoto(CatPhoto photoId);

    public int deleteCatPhotoById(Long photoId);

    public int deleteCatPhotoByIds(Long[] photoIds);
}
