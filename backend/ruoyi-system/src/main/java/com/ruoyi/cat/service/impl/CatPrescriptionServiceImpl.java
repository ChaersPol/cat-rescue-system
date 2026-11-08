package com.ruoyi.cat.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.domain.CatPrescription;
import com.ruoyi.cat.domain.CatPrescriptionItem;
import com.ruoyi.cat.domain.CatPrescriptionLog;
import com.ruoyi.cat.mapper.CatMedicationMapper;
import com.ruoyi.cat.mapper.CatPrescriptionMapper;
import com.ruoyi.cat.service.ICatPrescriptionService;
import com.ruoyi.cat.service.ICatStaffService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 用药处方 服务层实现
 *
 * @author ruoyi
 */
@Service
public class CatPrescriptionServiceImpl implements ICatPrescriptionService
{
    /** 处方状态：0 进行中 / 1 已停用 */
    private static final String STATUS_ACTIVE = "0";
    private static final String STATUS_STOPPED = "1";

    /** 处方展开出来的用药记录，来源标记沿用「站内诊断」 */
    private static final String SOURCE_PRESCRIPTION = "2";

    /** 单张处方最长天数：和批量的 60 天上限保持一致 */
    private static final int MAX_DAYS = 60;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private CatPrescriptionMapper prescriptionMapper;

    @Autowired
    private CatMedicationMapper medicationMapper;

    @Autowired
    private ICatStaffService staffService;

    @Override
    public CatPrescription selectCatPrescriptionById(Long prescriptionId)
    {
        CatPrescription prescription = prescriptionMapper.selectCatPrescriptionById(prescriptionId);
        if (prescription != null)
        {
            prescription.setItems(prescriptionMapper.selectItemsByPrescriptionId(prescriptionId));
            prescription.setLogs(prescriptionMapper.selectLogsByPrescriptionId(prescriptionId));
        }
        return prescription;
    }

    @Override
    public CatPrescription selectByDiagnosisId(Long diagnosisId)
    {
        if (diagnosisId == null)
        {
            return null;
        }
        CatPrescription prescription = prescriptionMapper.selectByDiagnosisId(diagnosisId);
        if (prescription != null)
        {
            prescription.setItems(prescriptionMapper.selectItemsByPrescriptionId(prescription.getPrescriptionId()));
        }
        return prescription;
    }

    @Override
    public List<CatPrescription> selectCatPrescriptionList(CatPrescription query)
    {
        return prescriptionMapper.selectCatPrescriptionList(query);
    }

    @Override
    @Transactional
    public Long saveCatPrescription(CatPrescription prescription)
    {
        requireDoctor("开处方、改处方");
        validate(prescription);

        LocalDate today = LocalDate.now();
        LocalDate start = toLocalDate(prescription.getStartDate());
        // 昨天的药不能今天补录：生成范围一律从「今天」和「开始日期」里取晚的那个
        LocalDate from = start.isAfter(today) ? start : today;

        if (prescription.getPrescriptionId() == null)
        {
            prescription.setStatus(STATUS_ACTIVE);
            if (StringUtils.isEmpty(prescription.getDoctor()))
            {
                prescription.setDoctor(staffService.currentStaffName());
            }
            prescription.setCreateBy(SecurityUtils.getUsername());
            prescriptionMapper.insertCatPrescription(prescription);
            replaceItems(prescription);
            writeLog(prescription, "新增", describe(prescription));
            expand(prescription, from);
            return prescription.getPrescriptionId();
        }

        CatPrescription old = prescriptionMapper.selectCatPrescriptionById(prescription.getPrescriptionId());
        if (old == null)
        {
            throw new ServiceException("处方不存在或已删除，请刷新后重试");
        }
        old.setItems(prescriptionMapper.selectItemsByPrescriptionId(old.getPrescriptionId()));
        // 先算差异：改完就比不出来了
        String diff = diffText(old, prescription, from);
        prescription.setUpdateBy(SecurityUtils.getUsername());
        prescriptionMapper.updateCatPrescription(prescription);
        replaceItems(prescription);
        // 已完成打卡的历史记录不动，只重建「改动当天及以后、还没完成」的部分
        medicationMapper.deleteUnfinishedByPrescription(prescription.getPrescriptionId(), toDate(from));
        expand(prescription, from);
        writeLog(prescription, "修改", diff);
        return prescription.getPrescriptionId();
    }

    @Override
    @Transactional
    public void stopCatPrescription(Long prescriptionId)
    {
        requireDoctor("停用处方");
        CatPrescription old = prescriptionMapper.selectCatPrescriptionById(prescriptionId);
        if (old == null)
        {
            throw new ServiceException("处方不存在或已删除，请刷新后重试");
        }
        CatPrescription update = new CatPrescription();
        update.setPrescriptionId(prescriptionId);
        update.setStatus(STATUS_STOPPED);
        update.setUpdateBy(SecurityUtils.getUsername());
        prescriptionMapper.updateCatPrescription(update);
        medicationMapper.deleteUnfinishedByPrescriptionAll(prescriptionId);
        CatPrescription logTarget = new CatPrescription();
        logTarget.setPrescriptionId(prescriptionId);
        logTarget.setCatId(old.getCatId());
        writeLog(logTarget, "停用", "处方已停用，未执行的用药明细已清除（已完成的打卡记录保留）");
    }

    @Override
    @Transactional
    public void deleteCatPrescription(Long prescriptionId)
    {
        requireDoctor("删除处方");
        CatPrescription old = prescriptionMapper.selectCatPrescriptionById(prescriptionId);
        if (old == null)
        {
            return;
        }
        medicationMapper.deleteUnfinishedByPrescriptionAll(prescriptionId);
        prescriptionMapper.deleteItemsByPrescriptionId(prescriptionId);
        prescriptionMapper.deleteCatPrescriptionById(prescriptionId);
    }

    /** 只有医生和管理岗能开方 / 改方：助理只负责照着执行 */
    private void requireDoctor(String action)
    {
        if (staffService.hasCurrentRole(ICatStaffService.SCOPE_DOCTOR) || staffService.isCurrentManager())
        {
            return;
        }
        throw new ServiceException("「" + action + "」只有医生有权限；助理只能在用药记录里按处方打卡");
    }

    private void validate(CatPrescription prescription)
    {
        if (prescription.getCatId() == null)
        {
            throw new ServiceException("请选择猫咪");
        }
        if (prescription.getStartDate() == null)
        {
            throw new ServiceException("请选择处方开始日期");
        }
        if (prescription.getDays() == null || prescription.getDays() < 1)
        {
            throw new ServiceException("请填写连续用药天数");
        }
        if (prescription.getDays() > MAX_DAYS)
        {
            throw new ServiceException("单张处方连续天数不能超过 " + MAX_DAYS + " 天");
        }
        List<CatPrescriptionItem> items = prescription.getItems();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("请至少添加一种药品");
        }
        for (int i = 0; i < items.size(); i++)
        {
            CatPrescriptionItem item = items.get(i);
            if (StringUtils.isEmpty(item.getDrugName()))
            {
                throw new ServiceException("第 " + (i + 1) + " 行药品名称为空");
            }
            if (item.periodList().isEmpty())
            {
                throw new ServiceException("药品「" + item.getDrugName() + "」请选择用药时间段（早/中/晚）");
            }
            if (StringUtils.isEmpty(item.getAdministration()))
            {
                item.setAdministration("口服");
            }
            if (item.getTimePeriods() != null)
            {
                // 统一成「早,中,晚」的写法，避免前端传「早、中」这种全角分隔符存进去
                item.setTimePeriods(String.join(",", item.periodList()));
            }
            item.setSort(i);
        }
    }

    private void replaceItems(CatPrescription prescription)
    {
        prescriptionMapper.deleteItemsByPrescriptionId(prescription.getPrescriptionId());
        List<CatPrescriptionItem> items = prescription.getItems();
        for (CatPrescriptionItem item : items)
        {
            item.setPrescriptionId(prescription.getPrescriptionId());
        }
        prescriptionMapper.batchInsertItems(items);
    }

    /**
     * 把处方展开成「天 × 时段 × 药」的用药记录明细
     * <p>
     * 同一天同一时段同一种药（来自别的处方）已存在就跳过，避免重复开方把台账刷重。
     */
    private void expand(CatPrescription prescription, LocalDate from)
    {
        List<CatPrescriptionItem> items = prescription.getItems();
        if (items == null || items.isEmpty())
        {
            return;
        }
        LocalDate start = toLocalDate(prescription.getStartDate());
        int days = prescription.getDays() == null ? 1 : prescription.getDays();
        String operator = SecurityUtils.getUsername();
        for (CatPrescriptionItem item : items)
        {
            List<String> periods = item.periodList();
            if (periods.isEmpty())
            {
                periods = Collections.singletonList("");
            }
            for (int i = 0; i < days; i++)
            {
                LocalDate day = start.plusDays(i);
                if (day.isBefore(from))
                {
                    // 昨天以前的药不补录
                    continue;
                }
                for (String period : periods)
                {
                    if (medicationMapper.countSameDrug(prescription.getCatId(), toDate(day), period,
                            item.getDrugName(), prescription.getPrescriptionId()) > 0)
                    {
                        continue;
                    }
                    CatMedication medication = new CatMedication();
                    medication.setCatId(prescription.getCatId());
                    medication.setUseDate(toDate(day));
                    medication.setTimePeriod(period);
                    medication.setDrugName(item.getDrugName());
                    medication.setDosage(item.getDosage());
                    medication.setAdministration(item.getAdministration());
                    medication.setSourceType(SOURCE_PRESCRIPTION);
                    medication.setDiagnosisId(prescription.getDiagnosisId());
                    medication.setPrescriptionId(prescription.getPrescriptionId());
                    medication.setRemark(cut(item.getUsageNote(), 400));
                    medication.setCreateBy(operator);
                    medicationMapper.insertCatMedication(medication);
                }
            }
        }
    }

    private void writeLog(CatPrescription prescription, String action, String detail)
    {
        CatPrescriptionLog log = new CatPrescriptionLog();
        log.setPrescriptionId(prescription.getPrescriptionId());
        log.setCatId(prescription.getCatId());
        log.setAction(action);
        log.setDetail(cut(detail, 1900));
        log.setOperator(staffService.currentStaffName());
        prescriptionMapper.insertLog(log);
    }

    /** 处方可读描述，新增时写进留痕 */
    private String describe(CatPrescription prescription)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("开始 ").append(fmt(prescription.getStartDate()))
          .append("，连续 ").append(prescription.getDays()).append(" 天；");
        sb.append(itemsText(prescription.getItems()));
        return sb.toString();
    }

    /** 变更内容：开始日期 / 天数 / 医嘱 / 药品，逐项对比 */
    private String diffText(CatPrescription old, CatPrescription now, LocalDate from)
    {
        List<String> parts = new ArrayList<>();
        if (!sameDay(old.getStartDate(), now.getStartDate()))
        {
            parts.add("开始日期 " + fmt(old.getStartDate()) + " → " + fmt(now.getStartDate()));
        }
        if (!Objects.equals(old.getDays(), now.getDays()))
        {
            parts.add("连续天数 " + old.getDays() + " → " + now.getDays());
        }
        String oldRemark = old.getRemark() == null ? "" : old.getRemark();
        String newRemark = now.getRemark() == null ? "" : now.getRemark();
        if (!oldRemark.equals(newRemark))
        {
            parts.add("医嘱「" + oldRemark + "」→「" + newRemark + "」");
        }
        String oldItems = itemsText(old.getItems());
        String newItems = itemsText(now.getItems());
        if (!oldItems.equals(newItems))
        {
            parts.add("药品「" + oldItems + "」→「" + newItems + "」");
        }
        if (parts.isEmpty())
        {
            parts.add("方案内容无变化，重新生成未执行的用药明细");
        }
        parts.add("从 " + from.format(DATE_FMT) + " 起生效，已完成打卡的历史记录不变");
        return String.join("；", parts);
    }

    private String itemsText(List<CatPrescriptionItem> items)
    {
        if (items == null || items.isEmpty())
        {
            return "无";
        }
        List<String> lines = new ArrayList<>();
        for (CatPrescriptionItem item : items)
        {
            StringBuilder line = new StringBuilder(item.getDrugName() == null ? "" : item.getDrugName());
            if (StringUtils.isNotEmpty(item.getDosage()))
            {
                line.append(" ").append(item.getDosage());
            }
            if (StringUtils.isNotEmpty(item.getAdministration()))
            {
                line.append(" ").append(item.getAdministration());
            }
            if (StringUtils.isNotEmpty(item.getTimePeriods()))
            {
                line.append(" [").append(item.getTimePeriods()).append("]");
            }
            lines.add(line.toString());
        }
        return String.join("、", lines);
    }

    private boolean sameDay(Date a, Date b)
    {
        return Objects.equals(fmt(a), fmt(b));
    }

    private String fmt(Date date)
    {
        if (date == null)
        {
            return "—";
        }
        return toLocalDate(date).format(DATE_FMT);
    }

    private LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private Date toDate(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private String cut(String text, int max)
    {
        if (text == null)
        {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
