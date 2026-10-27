package com.ruoyi.web.controller.cat;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.cat.domain.CatPhoto;
import com.ruoyi.cat.service.CatThumbnailService;
import com.ruoyi.cat.service.ICatPhotoService;

/**
 * 猫咪照片 控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/cat/photo")
public class CatPhotoController extends BaseController
{
    @Autowired
    private ICatPhotoService photoIdService;

    @Autowired
    private CatThumbnailService thumbnailService;

    /**
     * 批量重建缩略图（历史数据补齐 / 安装 ffmpeg 后回补视频封面）
     */
    @PreAuthorize("@ss.hasPermi('cat:photo:edit')")
    @Log(title = "猫咪照片", businessType = BusinessType.UPDATE)
    @PostMapping("/rebuildThumb")
    public AjaxResult rebuildThumb(@RequestBody(required = false) CatPhoto query)
    {
        int count = photoIdService.rebuildThumbnails(query == null ? new CatPhoto() : query);
        return success("已生成 " + count + " 条缩略图");
    }

    /**
     * 缩略图能力状态，供前端提示（如视频是否支持自动封面）
     */
    @PreAuthorize("@ss.hasPermi('cat:photo:list')")
    @GetMapping("/thumbStatus")
    public AjaxResult thumbStatus()
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("ffmpegAvailable", thumbnailService.isFfmpegAvailable());
        return ajax;
    }

    @PreAuthorize("@ss.hasPermi('cat:photo:list')")
    @GetMapping("/list")
    public TableDataInfo list(CatPhoto photoId)
    {
        startPage();
        List<CatPhoto> list = photoIdService.selectCatPhotoList(photoId);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('cat:photo:export')")
    @Log(title = "猫咪照片", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CatPhoto photoId)
    {
        List<CatPhoto> list = photoIdService.selectCatPhotoList(photoId);
        ExcelUtil<CatPhoto> util = new ExcelUtil<CatPhoto>(CatPhoto.class);
        util.exportExcel(response, list, "猫咪照片数据");
    }

    @PreAuthorize("@ss.hasPermi('cat:photo:query')")
    @GetMapping(value = "/{photoId}")
    public AjaxResult getInfo(@PathVariable("photoId") Long photoId)
    {
        return success(photoIdService.selectCatPhotoById(photoId));
    }

    @PreAuthorize("@ss.hasPermi('cat:photo:add')")
    @Log(title = "猫咪照片", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CatPhoto photoId)
    {
        return toAjax(photoIdService.insertCatPhoto(photoId));
    }

    @PreAuthorize("@ss.hasPermi('cat:photo:edit')")
    @Log(title = "猫咪照片", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CatPhoto photoId)
    {
        return toAjax(photoIdService.updateCatPhoto(photoId));
    }

    @PreAuthorize("@ss.hasPermi('cat:photo:remove')")
    @Log(title = "猫咪照片", businessType = BusinessType.DELETE)
    @DeleteMapping("/{photoIds}")
    public AjaxResult remove(@PathVariable Long[] photoIds)
    {
        return toAjax(photoIdService.deleteCatPhotoByIds(photoIds));
    }
}
