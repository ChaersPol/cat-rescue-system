package com.ruoyi.cat.service;

import java.util.List;
import com.ruoyi.cat.domain.CatPhoto;

/**
 * 猫咪照片 服务层
 * 
 * @author ruoyi
 */
public interface ICatPhotoService
{
    public CatPhoto selectCatPhotoById(Long photoId);

    public List<CatPhoto> selectCatPhotoList(CatPhoto photoId);

    public int insertCatPhoto(CatPhoto photoId);

    public int updateCatPhoto(CatPhoto photoId);

    public int deleteCatPhotoByIds(Long[] photoIds);

    /**
     * 批量重建缩略图（用于历史数据补齐，或切换 ffmpeg 后回补视频封面）
     *
     * @param query 过滤条件，为空时处理全部
     * @return 成功生成缩略图的条数
     */
    public int rebuildThumbnails(CatPhoto query);
}
