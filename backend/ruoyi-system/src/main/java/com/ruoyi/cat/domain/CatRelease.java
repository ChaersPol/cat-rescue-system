package com.ruoyi.cat.domain;
import java.util.Date;
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
    private Long releaseId;
    /** 猫咪id */
    private Long catId;
    /** 关联TNR记录id */
    private Long tnrId;
    /** 放归日期 */
    private Date releaseDate;
    /** 放归点位 */
    private String releasePoint;
    /** 放归时状态 */
    private String releaseStatus;
    /** 标记状态 */
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
    public Long getTnrId()
    {
        return tnrId;
    }
    public void setTnrId(Long tnrId)
    {
        this.tnrId = tnrId;
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
