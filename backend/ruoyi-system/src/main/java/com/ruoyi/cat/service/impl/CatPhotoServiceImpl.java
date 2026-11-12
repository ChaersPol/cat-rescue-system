package com.ruoyi.cat.service.impl;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatPhoto;
import com.ruoyi.cat.mapper.CatPhotoMapper;
import com.ruoyi.cat.service.CatThumbnailService;
import com.ruoyi.cat.service.ICatPhotoService;
import com.ruoyi.common.utils.StringUtils;

/**
 * 猫咪照片 服务层实现
 *
 * <p>新增/修改时会自动为图片生成缩放缩略图、为视频抽取封面帧；
 * 缩略图文件存放于原文件同目录的 thumb/ 子目录，随原图目录结构归档。</p>
 *
 * @author ruoyi
 */
@Service
public class CatPhotoServiceImpl implements ICatPhotoService
{
    private static final Logger log = LoggerFactory.getLogger(CatPhotoServiceImpl.class);

    @Autowired
    private CatPhotoMapper photoIdMapper;

    @Autowired
    private CatThumbnailService thumbnailService;

    @Override
    public CatPhoto selectCatPhotoById(Long photoId)
    {
        return photoIdMapper.selectCatPhotoById(photoId);
    }

    @Override
    public List<CatPhoto> selectCatPhotoList(CatPhoto photoId)
    {
        return photoIdMapper.selectCatPhotoList(photoId);
    }

    @Override
    public List<String> selectBreedOptions()
    {
        return photoIdMapper.selectBreedOptions();
    }

    @Override
    public List<Map<String, Object>> selectPhotoCatCards(CatPhoto query)
    {
        return photoIdMapper.selectPhotoCatCards(query);
    }

    @Override
    public int insertCatPhoto(CatPhoto photoId)
    {
        applyThumbnail(photoId, true);
        return photoIdMapper.insertCatPhoto(photoId);
    }

    @Override
    public int updateCatPhoto(CatPhoto photoId)
    {
        // 只有文件地址发生变化时才重新生成缩略图，避免只改分类也重复解码
        boolean urlChanged = false;
        if (StringUtils.isNotEmpty(photoId.getFileUrl()))
        {
            CatPhoto exist = photoId.getPhotoId() == null ? null
                    : photoIdMapper.selectCatPhotoById(photoId.getPhotoId());
            urlChanged = exist == null || !photoId.getFileUrl().equals(exist.getFileUrl());
        }
        applyThumbnail(photoId, urlChanged);
        return photoIdMapper.updateCatPhoto(photoId);
    }

    @Override
    public int deleteCatPhotoByIds(Long[] photoIds)
    {
        return photoIdMapper.deleteCatPhotoByIds(photoIds);
    }

    @Override
    public int rebuildThumbnails(CatPhoto query)
    {
        List<CatPhoto> list = photoIdMapper.selectCatPhotoList(query);
        if (list == null || list.isEmpty())
        {
            return 0;
        }
        int count = 0;
        for (CatPhoto photo : list)
        {
            if (StringUtils.isEmpty(photo.getFileUrl()))
            {
                continue;
            }
            CatThumbnailService.Derivatives derivatives = thumbnailService.generateAll(photo.getFileUrl(),
                    photo.getFileType());
            // 缩略图和预览图一次生成，任一有值就写库：小图只有预览图、视频只有缩略图都算成功
            if (derivatives.getThumbUrl() == null && derivatives.getPreviewUrl() == null)
            {
                continue;
            }
            CatPhoto update = new CatPhoto();
            update.setPhotoId(photo.getPhotoId());
            update.setThumbUrl(derivatives.getThumbUrl() == null ? "" : derivatives.getThumbUrl());
            update.setPreviewUrl(derivatives.getPreviewUrl() == null ? "" : derivatives.getPreviewUrl());
            if (photoIdMapper.updateCatPhoto(update) > 0)
            {
                count++;
            }
        }
        log.info("批量重建照片缩略图/预览图：共 {} 条，成功 {} 条", list.size(), count);
        return count;
    }

    /**
     * 补齐文件类型并根据需要生成缩略图
     *
     * @param photo 照片对象
     * @param regenerate 是否重新生成缩略图（新增或文件地址变更时为 true）
     */
    private void applyThumbnail(CatPhoto photo, boolean regenerate)
    {
        if (StringUtils.isEmpty(photo.getFileUrl()) || !regenerate)
        {
            return;
        }
        // 文件类型未指定时按扩展名推断，避免前端漏传导致视频被当图片处理
        if (StringUtils.isEmpty(photo.getFileType()))
        {
            photo.setFileType(thumbnailService.detectFileType(photo.getFileUrl()));
        }
        // 忽略调用方传入的旧地址，按当前文件重新生成；缩略图与预览图共用一次解码
        CatThumbnailService.Derivatives derivatives = thumbnailService.generateAll(photo.getFileUrl(),
                photo.getFileType());
        // 生成失败时写入空串，确保旧缩略图/预览图不会被残留引用
        photo.setThumbUrl(derivatives.getThumbUrl() == null ? "" : derivatives.getThumbUrl());
        photo.setPreviewUrl(derivatives.getPreviewUrl() == null ? "" : derivatives.getPreviewUrl());
    }
}
