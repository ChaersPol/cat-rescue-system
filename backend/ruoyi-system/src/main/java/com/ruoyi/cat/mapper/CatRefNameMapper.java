package com.ruoyi.cat.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 导出用的关联名称批量查询
 *
 * 只给导出场景使用，列表接口不受影响。
 * 展示格式集中在本文件里维护（如「毛豆（CAT202610180024）」），要调整措辞只改这里。
 *
 * @author ruoyi
 */
public interface CatRefNameMapper
{
    /** 猫咪：名字（档案号） */
    @Select("<script>SELECT cat_id AS id, CONCAT(IFNULL(NULLIF(cat_name,''),'未命名'),'（',IFNULL(NULLIF(archive_no,''),'无档案号'),'）') AS label "
            + "FROM cat_profile WHERE cat_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectCatLabels(@Param("ids") Collection<Long> ids);

    /** 基地：名称（类型中文） */
    @Select("<script>SELECT b.base_id AS id, CONCAT(IFNULL(b.base_name,''),'（',IFNULL(d.dict_label,b.base_type),'）') AS label "
            + "FROM cat_base b LEFT JOIN sys_dict_data d ON d.dict_type='cat_base_type' AND d.dict_value=b.base_type "
            + "WHERE b.base_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectBaseLabels(@Param("ids") Collection<Long> ids);

    /** 病房：名称（所属基地） */
    @Select("<script>SELECT w.ward_id AS id, CONCAT(IFNULL(w.ward_name,''),'（',IFNULL(b.base_name,'未分配基地'),'）') AS label "
            + "FROM cat_ward w LEFT JOIN cat_base b ON b.base_id=w.base_id "
            + "WHERE w.ward_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectWardLabels(@Param("ids") Collection<Long> ids);

    /** 费用分类：名称（费用类型中文） */
    @Select("<script>SELECT c.category_id AS id, CONCAT(IFNULL(c.category_name,''),'（',IFNULL(d.dict_label,c.category_type),'）') AS label "
            + "FROM cat_fee_category c LEFT JOIN sys_dict_data d ON d.dict_type='cat_expense_type' AND d.dict_value=c.category_type "
            + "WHERE c.category_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectFeeCategoryLabels(@Param("ids") Collection<Long> ids);

    /** 物资分类：名称 */
    @Select("<script>SELECT category_id AS id, IFNULL(category_name,'') AS label FROM cat_material_category "
            + "WHERE category_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectMaterialCategoryLabels(@Param("ids") Collection<Long> ids);

    /** 物资：名称（规格，单位） */
    @Select("<script>SELECT material_id AS id, CONCAT(IFNULL(material_name,''),'（',IFNULL(NULLIF(spec,''),'无规格'),'，',IFNULL(unit,''),'）') AS label "
            + "FROM cat_material WHERE material_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectMaterialLabels(@Param("ids") Collection<Long> ids);

    /** 领养记录：领养人（领养日期） */
    @Select("<script>SELECT adoption_id AS id, CONCAT(IFNULL(adopter_name,''),'（',IFNULL(DATE_FORMAT(adoption_date,'%Y-%m-%d'),'未填日期'),'）') AS label "
            + "FROM cat_adoption WHERE adoption_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectAdoptionLabels(@Param("ids") Collection<Long> ids);

    /** 转诊记录：医院（送医日期） */
    @Select("<script>SELECT referral_id AS id, CONCAT(IFNULL(hospital_name,''),'（',IFNULL(DATE_FORMAT(send_date,'%Y-%m-%d'),'未填日期'),'）') AS label "
            + "FROM cat_referral WHERE referral_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectReferralLabels(@Param("ids") Collection<Long> ids);

    /** TNR 记录：猫咪信息（抓捕日期） */
    @Select("<script>SELECT tnr_id AS id, CONCAT(IFNULL(NULLIF(cat_name,''),'未命名'),'（',IFNULL(DATE_FORMAT(capture_time,'%Y-%m-%d'),'未填日期'),'）') AS label "
            + "FROM cat_tnr WHERE tnr_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectTnrLabels(@Param("ids") Collection<Long> ids);

    /** 出入库单：日期 类型 （用途） */
    @Select("<script>SELECT r.record_id AS id, CONCAT(IFNULL(DATE_FORMAT(r.record_date,'%Y-%m-%d'),''),' ',"
            + "(CASE r.record_type WHEN '1' THEN '入库' WHEN '2' THEN '出库' ELSE '出入库' END),'（',IFNULL(NULLIF(r.purpose,''),'无用途'),'）') AS label "
            + "FROM cat_stock_record r WHERE r.record_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectStockLabels(@Param("ids") Collection<Long> ids);

    /** 支出单：日期 事由（金额） */
    @Select("<script>SELECT expense_id AS id, CONCAT(IFNULL(DATE_FORMAT(expense_date,'%Y-%m-%d'),''),' ',IFNULL(NULLIF(items,''),'支出'),'（¥',IFNULL(amount,'0'),'）') AS label "
            + "FROM cat_expense WHERE expense_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectExpenseLabels(@Param("ids") Collection<Long> ids);

    /** 用户：昵称（登录名） */
    @Select("<script>SELECT user_id AS id, CONCAT(IFNULL(nick_name,''),'（',IFNULL(user_name,''),'）') AS label "
            + "FROM sys_user WHERE user_id IN <foreach collection='ids' item='i' open='(' separator=',' close=')'>#{i}</foreach></script>")
    List<Map<String, Object>> selectUserLabels(@Param("ids") Collection<Long> ids);
}
