package com.ruoyi.cat.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatMaterial;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.domain.CatMedicationExec;
import com.ruoyi.cat.domain.CatMedicationGroup;
import com.ruoyi.cat.mapper.CatMaterialMapper;
import com.ruoyi.cat.mapper.CatMedicationExecMapper;
import com.ruoyi.cat.mapper.CatMedicationMapper;
import com.ruoyi.cat.service.ICatMedicationService;
import com.ruoyi.cat.service.ICatStaffService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 每日用药记录 服务层实现
 *
 * @author ruoyi
 */
@Service
public class CatMedicationServiceImpl implements ICatMedicationService
{
    /** 执行状态：0 未完成 / 1 已完成 */
    private static final String EXEC_DONE = "1";

    @Autowired
    private CatMedicationMapper medicationIdMapper;

    @Autowired
    private CatMedicationExecMapper execMapper;

    @Autowired
    private ICatStaffService staffService;

    @Autowired
    private CatMaterialMapper materialMapper;

    @Autowired
    private CatLedgerStockService ledgerStockService;

    @Override
    public CatMedication selectCatMedicationById(Long medicationId)
    {
        CatMedication medication = medicationIdMapper.selectCatMedicationById(medicationId);
        return medication;
    }

    @Override
    public List<CatMedication> selectCatMedicationList(CatMedication medicationId)
    {
        return medicationIdMapper.selectCatMedicationList(medicationId);
    }

    @Override
    public List<CatMedicationGroup> selectCatMedicationGroupList(CatMedicationGroup query)
    {
        return medicationIdMapper.selectCatMedicationGroupList(query);
    }

    @Override
    public List<CatMedication> selectGroupDetails(Long catId, Date useDate, String timePeriod)
    {
        return medicationIdMapper.selectGroupDetails(catId, useDate, timePeriod == null ? "" : timePeriod);
    }

    /**
     * 新增用药方案：助理不能开药（需求确认：助理只按医生处方打卡，临时加药也要医生补录）
     * <p>挂了物资就同步出一张出库单（库存允许为负，页面提示即可，不阻断）。
     */
    @Override
    @Transactional
    public int insertCatMedication(CatMedication medicationId)
    {
        denyAssistant("新增用药记录");
        if (medicationId.getUseDate() == null)
        {
            throw new ServiceException("请选择用药开始日期");
        }
        // 执行人由助理打卡时记录，医生开方案时允许留空
        medicationId.setExecutor(staffService.validateOptional("用药执行人",
                ICatStaffService.SCOPE_ALL, medicationId.getExecutor()));
        normalizeMaterial(medicationId);
        int rows = medicationIdMapper.insertCatMedication(medicationId);
        if (rows > 0)
        {
            ledgerStockService.sync(CatLedgerStockService.REF_MEDICATION, medicationId.getMedicationId(),
                    medicationId.getMaterialId(), medicationId.getStockQty(), medicationId.getCatId(),
                    medicationId.getUseDate(), "用药", medicationId.getDrugName());
        }
        return rows;
    }

    /**
     * 物资关联归一化
     * <ul>
     *   <li>materialId = 0：前端点了「不关联」，转成 null 并置 clearMaterial，动态 update 才会写空；</li>
     *   <li>materialId 有值：按物资表补齐名称快照，物资被删过就报错，别让历史记录挂着死 id。</li>
     * </ul>
     */
    private void normalizeMaterial(CatMedication medication)
    {
        Long materialId = medication.getMaterialId();
        if (materialId != null && materialId == 0L)
        {
            medication.setMaterialId(null);
            medication.setMaterialName(null);
            medication.setStockQty(null);
            medication.setClearMaterial(true);
            return;
        }
        if (materialId == null)
        {
            return;
        }
        CatMaterial material = materialMapper.selectCatMaterialById(materialId);
        if (material == null)
        {
            throw new ServiceException("所选物资不存在或已删除，请重新选择");
        }
        medication.setMaterialName(material.getMaterialName());
    }

    /**
     * 修改用药方案（药品、剂量、给药方式等）：只有医生和管​理岗能改
     */
    @Override
    @Transactional
    public int updateCatMedication(CatMedication medicationId)
    {
        requireDoctor("修改用药方案");
        CatMedication old = medicationId.getMedicationId() == null
                ? null : medicationIdMapper.selectCatMedicationById(medicationId.getMedicationId());
        if (old == null)
        {
            throw new ServiceException("用药记录不存在或已删除，请刷新后重试");
        }
        // 已经整组完成打卡的那一天属于历史，不能跟着改
        if (EXEC_DONE.equals(medicationIdMapper.selectExecStatus(old.getCatId(), old.getUseDate(), old.getTimePeriod())))
        {
            throw new ServiceException("该组用药已完成打卡，历史记录不能修改；如需调整请由医生从今天起重新开处方");
        }
        medicationId.setExecutor(staffService.validateOptional("用药执行人",
                ICatStaffService.SCOPE_ALL, medicationId.getExecutor()));
        normalizeMaterial(medicationId);
        // 只有本次确实动了物资（重新挂 / 主动取消）才去核库存，避免别的字段保存时误冲销出库单
        boolean materialTouched = medicationId.getMaterialId() != null
                || Boolean.TRUE.equals(medicationId.getClearMaterial());
        int rows = medicationIdMapper.updateCatMedication(medicationId);
        // 医生表单里也有执行情况那一栏：执行人 / 不良反应 / 照片
        syncExec(old, medicationId);
        // 诊断/处方展开出来的明细不碰库存——那批药在开单时已经按诊断明细出库过一次了
        if (rows > 0 && materialTouched && old.getDiagnosisId() == null && old.getPrescriptionId() == null)
        {
            ledgerStockService.sync(CatLedgerStockService.REF_MEDICATION, old.getMedicationId(),
                    medicationId.getMaterialId(), medicationId.getStockQty(), old.getCatId(),
                    old.getUseDate(), "用药",
                    medicationId.getDrugName() != null ? medicationId.getDrugName() : old.getDrugName());
        }
        return rows;
    }

    /**
     * 批量录入同一只猫连续多天的用药（需求文档 6.3）
     * <p>
     * 从 useDate 开始，按天连续生成 days 条记录；同一天同一时间段同一种药已存在则跳过，
     * 避免重复打卡把用药台账刷成重复数据。
     */
    @Override
    @Transactional
    public int insertCatMedicationBatch(CatMedication medication, Integer days)
    {
        denyAssistant("新增用药记录");
        if (medication.getCatId() == null)
        {
            throw new ServiceException("请选择猫咪");
        }
        if (medication.getUseDate() == null)
        {
            throw new ServiceException("请选择用药开始日期");
        }
        if (medication.getDrugName() == null || medication.getDrugName().isEmpty())
        {
            throw new ServiceException("请输入药品名称");
        }
        medication.setExecutor(staffService.validateOptional("用药执行人",
                ICatStaffService.SCOPE_ALL, medication.getExecutor()));
        normalizeMaterial(medication);
        int total = days == null || days < 1 ? 1 : days;
        if (total > 60)
        {
            throw new ServiceException("单次批量录入不能超过 60 天");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(medication.getUseDate());

        int inserted = 0;
        Long firstId = null;
        for (int i = 0; i < total; i++)
        {
            Date day = calendar.getTime();

            CatMedication query = new CatMedication();
            query.setCatId(medication.getCatId());
            query.setUseDate(day);
            query.setDrugName(medication.getDrugName());
            boolean exists = false;
            for (CatMedication exist : medicationIdMapper.selectCatMedicationList(query))
            {
                String existPeriod = exist.getTimePeriod() == null ? "" : exist.getTimePeriod();
                String curPeriod = medication.getTimePeriod() == null ? "" : medication.getTimePeriod();
                if (existPeriod.equals(curPeriod))
                {
                    exists = true;
                    break;
                }
            }
            if (!exists)
            {
                CatMedication record = new CatMedication();
                record.setCatId(medication.getCatId());
                record.setUseDate(day);
                record.setTimePeriod(medication.getTimePeriod());
                record.setDrugName(medication.getDrugName());
                record.setDosage(medication.getDosage());
                record.setAdministration(medication.getAdministration());
                record.setExecutor(medication.getExecutor());
                record.setAdverseReaction(medication.getAdverseReaction());
                record.setPhotos(medication.getPhotos());
                record.setRemark(medication.getRemark());
                // 物资关联每行都带上（列表能看出这批药用的是什么），但数量只写第一行：
                // 一次发药只出一张出库单，后面几行 stock_qty 为空表示「本行不单独扣库存」。
                record.setMaterialId(medication.getMaterialId());
                record.setMaterialName(medication.getMaterialName());
                if (firstId == null)
                {
                    record.setStockQty(medication.getStockQty());
                }
                record.setCreateBy(medication.getCreateBy());
                inserted += medicationIdMapper.insertCatMedication(record);
                if (firstId == null)
                {
                    firstId = record.getMedicationId();
                }
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        // 只按第一行出库一次，与诊断开方「一次发药」口径一致
        if (firstId != null && medication.getMaterialId() != null)
        {
            ledgerStockService.sync(CatLedgerStockService.REF_MEDICATION, firstId,
                    medication.getMaterialId(), medication.getStockQty(), medication.getCatId(),
                    medication.getUseDate(), "用药", medication.getDrugName());
        }
        return inserted;
    }

    @Override
    @Transactional
    public int deleteCatMedicationByIds(Long[] medicationIds)
    {
        denyAssistant("删除用药记录");
        // 删掉用药记录等于这次药没用上：把生成的出库单按净值冲销，库存回补
        if (medicationIds != null)
        {
            for (Long id : medicationIds)
            {
                if (id != null)
                {
                    ledgerStockService.clear(CatLedgerStockService.REF_MEDICATION, id, null);
                }
            }
        }
        return medicationIdMapper.deleteCatMedicationByIds(medicationIds);
    }

    /**
     * 执行打卡（整组：猫 + 日期 + 时间段）
     * <p>
     * 1. 点「完成」必须已经上传照片（需求确认第 3 条）；
     * 2. 撤回不清照片，只把状态改回未完成，靠 update_time 记下撤回时间点；
     * 3. 照片有变化就刷新上传时间，「用药照片要显示上传时间」。
     */
    @Override
    @Transactional
    public CatMedicationExec saveExec(CatMedicationExec request)
    {
        if (request.getCatId() == null || request.getUseDate() == null)
        {
            throw new ServiceException("缺少猫咪或用药日期");
        }
        String period = request.getTimePeriod() == null ? "" : request.getTimePeriod();
        String status = request.getStatus();
        boolean complete = EXEC_DONE.equals(status);

        CatMedicationExec exist = execMapper.selectExec(request.getCatId(), request.getUseDate(), period);
        String oldPhotos = exist == null ? null : exist.getPhotos();
        String newPhotos = request.getPhotos();

        if (complete)
        {
            String photos = newPhotos != null ? newPhotos : oldPhotos;
            if (StringUtils.isEmpty(photos))
            {
                throw new ServiceException("请先上传本次用药照片，再点「完成」");
            }
            request.setPhotos(photos);
        }
        // 照片变了就记一次上传时间（补照片也要能看到是什么时候传的）
        if (newPhotos != null && !newPhotos.equals(oldPhotos == null ? "" : oldPhotos))
        {
            request.setPhotoTime(new Date());
        }
        if (complete)
        {
            // 完成时记谁点的：非管理岗一律记本人，管理岗可以从医生/助理里指定
            request.setExecutor(staffService.requireForInsert("用药执行人",
                    ICatStaffService.SCOPE_ALL, request.getExecutor(), true));
            request.setExecTime(new Date());
        }
        request.setUpdateBy(SecurityUtils.getUsername());

        if (exist == null)
        {
            request.setStatus(status == null ? "0" : status);
            request.setTimePeriod(period);
            request.setCreateBy(SecurityUtils.getUsername());
            execMapper.insertExec(request);
        }
        else
        {
            request.setExecId(exist.getExecId());
            if ("0".equals(status))
            {
                // 撤回：保留原来的执行人，只把状态退回去
                request.setExecutor(null);
            }
            execMapper.updateExec(request);
        }
        return execMapper.selectExec(request.getCatId(), request.getUseDate(), period);
    }

    /**
     * 医生在用药记录里改「这一次」的方案（合并视图一行 = 一只猫+一天+一个时段）
     * <p>
     * 只动这一组，不回头改处方；长期调整请到诊断单里改处方（那边有变更留痕、
     * 且只从改动当天起生效）。已完成打卡的组不允许改。
     */
    @Override
    @Transactional
    public void saveGroupPlan(CatMedicationGroup request)
    {
        requireDoctor("修改用药方案");
        if (request.getCatId() == null || request.getUseDate() == null)
        {
            throw new ServiceException("缺少猫咪或用药日期");
        }
        String period = request.getTimePeriod() == null ? "" : request.getTimePeriod();
        if (EXEC_DONE.equals(medicationIdMapper.selectExecStatus(request.getCatId(), request.getUseDate(), period)))
        {
            throw new ServiceException("该组用药已完成打卡，历史记录不能修改；如需调整请从今天起的处方里改");
        }
        List<CatMedication> details = request.getDetails();
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("请至少保留一种药品；整组都不要了请用「删除」");
        }
        List<CatMedication> exist = medicationIdMapper.selectGroupDetails(request.getCatId(), request.getUseDate(), period);
        Set<Long> keep = new HashSet<>();
        for (CatMedication detail : details)
        {
            if (detail.getMedicationId() != null)
            {
                keep.add(detail.getMedicationId());
            }
        }
        List<Long> removed = new ArrayList<>();
        for (CatMedication row : exist)
        {
            if (!keep.contains(row.getMedicationId()))
            {
                removed.add(row.getMedicationId());
            }
        }
        if (!removed.isEmpty())
        {
            medicationIdMapper.deleteCatMedicationByIds(removed.toArray(new Long[0]));
            // 组里被去掉的那几味药：各自产生的出库单冲销掉
            for (Long id : removed)
            {
                ledgerStockService.clear(CatLedgerStockService.REF_MEDICATION, id, request.getCatId());
            }
        }
        String operator = SecurityUtils.getUsername();
        for (CatMedication detail : details)
        {
            if (StringUtils.isEmpty(detail.getDrugName()))
            {
                throw new ServiceException("药品名称不能为空");
            }
            if (detail.getMedicationId() != null)
            {
                CatMedication update = new CatMedication();
                update.setMedicationId(detail.getMedicationId());
                update.setDrugName(detail.getDrugName());
                update.setDosage(detail.getDosage());
                update.setAdministration(detail.getAdministration());
                update.setRemark(detail.getRemark());
                update.setUpdateBy(operator);
                // 组内改某一味药的用量/物资时同步库存（处方展开的行没有物资，不受影响）
                normalizeMaterial(detail);
                if (detail.getMaterialId() != null || Boolean.TRUE.equals(detail.getClearMaterial()))
                {
                    update.setMaterialId(detail.getMaterialId());
                    update.setMaterialName(detail.getMaterialName());
                    update.setStockQty(detail.getStockQty());
                    update.setClearMaterial(detail.getClearMaterial());
                }
                medicationIdMapper.updateCatMedication(update);
                if (detail.getMaterialId() != null || Boolean.TRUE.equals(detail.getClearMaterial()))
                {
                    ledgerStockService.sync(CatLedgerStockService.REF_MEDICATION, detail.getMedicationId(),
                            detail.getMaterialId(), detail.getStockQty(), request.getCatId(),
                            request.getUseDate(), "用药", detail.getDrugName());
                }
            }
            else
            {
                CatMedication add = new CatMedication();
                add.setCatId(request.getCatId());
                add.setUseDate(request.getUseDate());
                add.setTimePeriod(period);
                add.setDrugName(detail.getDrugName());
                add.setDosage(detail.getDosage());
                add.setAdministration(detail.getAdministration());
                add.setRemark(detail.getRemark());
                // 医生手工加的这一次用药，标成「手动录入」，与处方展开的明细区分
                add.setSourceType("1");
                add.setCreateBy(operator);
                normalizeMaterial(add);
                medicationIdMapper.insertCatMedication(add);
                ledgerStockService.sync(CatLedgerStockService.REF_MEDICATION, add.getMedicationId(),
                        add.getMaterialId(), add.getStockQty(), request.getCatId(),
                        request.getUseDate(), "用药", add.getDrugName());
            }
        }
        // 同一波提交里带了执行情况就一起落
        if (request.getExecutor() != null || request.getPhotos() != null || request.getAdverseReaction() != null)
        {
            CatMedicationExec exec = new CatMedicationExec();
            exec.setCatId(request.getCatId());
            exec.setUseDate(request.getUseDate());
            exec.setTimePeriod(period);
            exec.setExecutor(request.getExecutor());
            exec.setPhotos(request.getPhotos());
            exec.setAdverseReaction(request.getAdverseReaction());
            saveExec(exec);
        }
    }

    /** 医生改方案时顺带把执行情况（执行人/不良反应/照片）落到打卡表 */
    private void syncExec(CatMedication old, CatMedication now)
    {
        if (now.getExecutor() == null && now.getPhotos() == null && now.getAdverseReaction() == null)
        {
            return;
        }
        CatMedicationExec exec = new CatMedicationExec();
        exec.setCatId(old.getCatId());
        exec.setUseDate(old.getUseDate());
        exec.setTimePeriod(old.getTimePeriod() == null ? "" : old.getTimePeriod());
        exec.setExecutor(now.getExecutor());
        exec.setPhotos(now.getPhotos());
        exec.setAdverseReaction(now.getAdverseReaction());
        saveExec(exec);
    }

    /** 助理不能开药、不能删药：只能按医生处方打卡 */
    private void denyAssistant(String action)
    {
        if (staffService.hasCurrentRole(ICatStaffService.SCOPE_ASSISTANT)
                && !staffService.isCurrentManager()
                && !staffService.hasCurrentRole(ICatStaffService.SCOPE_DOCTOR))
        {
            throw new ServiceException("助理不能" + action + "；用药方案请医生在站内诊断里开，助理只按处方打卡");
        }
    }

    /** 只有医生和管理岗能改用药方案（需求确认：管理员也能改） */
    private void requireDoctor(String action)
    {
        if (staffService.hasCurrentRole(ICatStaffService.SCOPE_DOCTOR) || staffService.isCurrentManager())
        {
            return;
        }
        throw new ServiceException("「" + action + "」只有医生和管理员有权限；助理只能填执行人、不良反应和照片");
    }
}
