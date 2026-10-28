package com.ruoyi.cat.domain;

/**
 * 猫咪信息卡（打印用）聚合视图
 *
 * 一张卡要展示疫苗、入住医院、病情、用药等多个表的最新记录，
 * 为避免前端多次请求（N+1），统一由 CatCardMapper 一次查出。
 *
 * @author ruoyi
 */
public class CatCardVo
{
    /** 猫咪id */
    private Long catId;

    /** 档案编号 */
    private String archiveNo;

    /** 名字 */
    private String catName;

    /** 性别（0公 1母 2未知） */
    private String sex;

    /** 是否绝育（0未绝育 1已绝育 2未知） */
    private String neutered;

    /** 体重 */
    private String weight;

    /** 状态 */
    private String status;

    /** 所在基地 */
    private String baseName;

    /** 所在病房 */
    private String wardName;

    /** 备注（应激程度、是否怕人、特殊习性）→ 卡片「注意事项」 */
    private String remark;

    /** 二维码 Base64 */
    private String qrCode;

    /** 特征照片（优先缩略图，无缩略图回退原图；卡片左上角识别用） */
    private String photoUrl;

    /** 照片所属文件夹（救助时/治疗中/康复后…），便于核对是不是当前状态的照片 */
    private String photoFolder;

    /** 最近一次疫苗：类型 + 日期 */
    private String vaccineText;

    /** 最近一次送医：医院 + 日期 */
    private String hospitalText;

    /** 最近一次诊断：日期 + 诊断结果 */
    private String diagnosisText;

    /** 最近用药，多条用换行分隔（新的在前） */
    private String medicationText;

    public Long getCatId()
    {
        return catId;
    }

    public void setCatId(Long catId)
    {
        this.catId = catId;
    }

    public String getArchiveNo()
    {
        return archiveNo;
    }

    public void setArchiveNo(String archiveNo)
    {
        this.archiveNo = archiveNo;
    }

    public String getCatName()
    {
        return catName;
    }

    public void setCatName(String catName)
    {
        this.catName = catName;
    }

    public String getSex()
    {
        return sex;
    }

    public void setSex(String sex)
    {
        this.sex = sex;
    }

    public String getNeutered()
    {
        return neutered;
    }

    public void setNeutered(String neutered)
    {
        this.neutered = neutered;
    }

    public String getWeight()
    {
        return weight;
    }

    public void setWeight(String weight)
    {
        this.weight = weight;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getBaseName()
    {
        return baseName;
    }

    public void setBaseName(String baseName)
    {
        this.baseName = baseName;
    }

    public String getWardName()
    {
        return wardName;
    }

    public void setWardName(String wardName)
    {
        this.wardName = wardName;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public String getQrCode()
    {
        return qrCode;
    }

    public void setQrCode(String qrCode)
    {
        this.qrCode = qrCode;
    }

    public String getPhotoUrl()
    {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl)
    {
        this.photoUrl = photoUrl;
    }

    public String getPhotoFolder()
    {
        return photoFolder;
    }

    public void setPhotoFolder(String photoFolder)
    {
        this.photoFolder = photoFolder;
    }

    public String getVaccineText()
    {
        return vaccineText;
    }

    public void setVaccineText(String vaccineText)
    {
        this.vaccineText = vaccineText;
    }

    public String getHospitalText()
    {
        return hospitalText;
    }

    public void setHospitalText(String hospitalText)
    {
        this.hospitalText = hospitalText;
    }

    public String getDiagnosisText()
    {
        return diagnosisText;
    }

    public void setDiagnosisText(String diagnosisText)
    {
        this.diagnosisText = diagnosisText;
    }

    public String getMedicationText()
    {
        return medicationText;
    }

    public void setMedicationText(String medicationText)
    {
        this.medicationText = medicationText;
    }
}
