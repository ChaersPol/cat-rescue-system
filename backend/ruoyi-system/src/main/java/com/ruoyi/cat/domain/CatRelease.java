package com.ruoyi.cat.domain;
import java.util.Date;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
/**
 * TNR放归登记表
 * 
 * @author ruoyi
 */
public class CatRelease extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    /** 放归记录id */
    @Excel(name = "放归记录ID", cellType = ColumnType.NUMERIC)
    private Long releaseId;
    /** 猫咪id */
    private Long catId;
    /** 猫咪（名称+档案号），导出展示用非表字段 */
    @Excel(name = "猫咪")
    private String catRefName;
    /** 关联TNR记录id */
    private Long tnrId;
    /** 关联TNR记录（猫咪+抓捕日期），导出展示用非表字段 */
    @Excel(name = "关联TNR")
    private String tnrRefName;
    /** 放归日期 */
    @Excel(name = "放归日期", dateFormat = "yyyy-MM-dd")
    private Date releaseDate;
    /** 放归点位 */
    @Excel(name = "放归点位")
    private String releasePoint;
    /** 放归时状态 */
    @Excel(name = "放归时状态")
    private String releaseStatus;
    /** 标记状态（1已放归） */
    @Excel(name = "标记状态", readConverterExp = "1=已放归")
    private String marked;
    public Long getReleaseId()
    {
        return releaseId;
    }
    public void setReleaseId(Long releaseId)
    {
        this.releaseId = releaseId;
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
    public Long getTnrId()
    {
        return tnrId;
    }
    public void setTnrId(Long tnrId)
    {
        this.tnrId = tnrId;
    }
    public String getTnrRefName()
    {
        return tnrRefName;
    }
    public void setTnrRefName(String tnrRefName)
    {
        this.tnrRefName = tnrRefName;
    }
    public Date getReleaseDate()
    {
        return releaseDate;
    }
    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
    }
    public String getReleasePoint()
    {
        return releasePoint;
    }
    public void setReleasePoint(String releasePoint)
    {
        this.releasePoint = releasePoint;
    }
    public String getReleaseStatus()
    {
        return releaseStatus;
    }
    public void setReleaseStatus(String releaseStatus)
    {
        this.releaseStatus = releaseStatus;
    }
    public String getMarked()
    {
        return marked;
    }
    public void setMarked(String marked)
    {
        this.marked = marked;
    }
    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("releaseId", getReleaseId())
            .append("catId", getCatId())
            .append("tnrId", getTnrId())
            .append("releaseDate", getReleaseDate())
            .append("releasePoint", getReleasePoint())
            .append("releaseStatus", getReleaseStatus())
            .append("marked", getMarked())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
