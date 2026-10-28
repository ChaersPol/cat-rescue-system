package com.ruoyi.cat.domain;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * 猫咪照片相册表
 * 
 * @author ruoyi
 */
public class CatPhoto extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 照片id */
    @Excel(name = "照片ID", cellType = ColumnType.NUMERIC)
    private Long photoId;
    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 文件夹 */
    @Excel(name = "相册分类", dictType = "cat_photo_folder")
    private String folder;
    /** 文件类型（1图片 2视频） */
    @Excel(name = "文件类型", readConverterExp = "1=图片,2=视频")
    private String fileType;
    /** 文件地址 */
    @Excel(name = "文件地址")
    private String fileUrl;
    /** 缩略图地址（列表/相册展示用，图片自动缩放、视频自动抽帧） */
    @Excel(name = "缩略图地址")
    private String thumbUrl;
    public Long getPhotoId()
    {
        return photoId;
    }
    public void setPhotoId(Long photoId)
    {
        this.photoId = photoId;
    }
    public Long getCatId()
    {
        return catId;
    }
    public void setCatId(Long catId)
    {
        this.catId = catId;
    }
    public String getCatRefName()
    {
        return catRefName;
    }
    public void setCatRefName(String catRefName)
    {
        this.catRefName = catRefName;
    }
    public String getFolder()
    {
        return folder;
    }
    public void setFolder(String folder)
    {
        this.folder = folder;
    }
    public String getFileType()
    {
        return fileType;
    }
    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }
    public String getFileUrl()
    {
        return fileUrl;
    }
    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getThumbUrl()
    {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl)
    {
        this.thumbUrl = thumbUrl;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("photoId", getPhotoId())
            .append("catId", getCatId())
            .append("folder", getFolder())
            .append("fileType", getFileType())
            .append("fileUrl", getFileUrl())
            .append("thumbUrl", getThumbUrl())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
