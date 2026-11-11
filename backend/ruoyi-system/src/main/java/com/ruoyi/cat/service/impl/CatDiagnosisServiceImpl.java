package com.ruoyi.cat.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatBase;
import com.ruoyi.cat.domain.CatCheckItem;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatDiagnosis;
import com.ruoyi.cat.domain.CatDiagnosisItem;
import com.ruoyi.cat.domain.CatDiagnosisSummary;
import com.ruoyi.cat.domain.CatMaterial;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.domain.CatPrescription;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatStockRecord;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.domain.CatWard;
import com.ruoyi.cat.mapper.CatBaseMapper;
import com.ruoyi.cat.mapper.CatCheckItemMapper;
import com.ruoyi.cat.mapper.CatMaterialMapper;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.mapper.CatDiagnosisItemMapper;
import com.ruoyi.cat.mapper.CatDiagnosisMapper;
import com.ruoyi.cat.mapper.CatMedicationMapper;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.mapper.CatStockRecordMapper;
import com.ruoyi.cat.mapper.CatVaccineMapper;
import com.ruoyi.cat.mapper.CatWardMapper;
import com.ruoyi.cat.service.ICatDeleteImpactService;
import com.ruoyi.cat.service.ICatDiagnosisService;
import com.ruoyi.cat.service.ICatStaffService;
import com.ruoyi.cat.service.ICatPrescriptionService;
import com.ruoyi.cat.service.ICatStockRecordService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.ISysRoleService;

/**
 * 站内诊断记录 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatDiagnosisServiceImpl implements ICatDiagnosisService
{
    @Autowired
    private ICatDeleteImpactService deleteImpactService;

    @Autowired
    private CatDiagnosisMapper diagnosisIdMapper;

    @Autowired
    private CatBaseMapper baseMapper;

    @Autowired
    private CatWardMapper wardMapper;

    @Autowired
    private CatProfileMapper profileMapper;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private CatDiagnosisItemMapper diagnosisItemMapper;

    @Autowired
    private CatCheckItemMapper checkItemMapper;

    @Autowired
    private CatMaterialMapper materialMapper;

    @Autowired
    private CatStockRecordMapper stockRecordMapper;

    @Autowired
    private ICatStockRecordService stockRecordService;

    @Autowired
    private CatMedicationMapper medicationMapper;

    @Autowired
    private CatVaccineMapper vaccineMapper;

    @Autowired
    private CatDewormMapper dewormMapper;

    @Autowired
    private ICatStaffService staffService;

    @Autowired
    private ICatPrescriptionService prescriptionService;

    /** 明细类型：1检查项目 2用药 3疫苗 4驱虫 */
    private static final String ITEM_TYPE_DRUG = "2";
    private static final String ITEM_TYPE_VACCINE = "3";
    private static final String ITEM_TYPE_DEWORM = "4";

    /** 来源：站内诊断开具 */
    private static final String SOURCE_DIAGNOSIS = "2";

    /** 医生角色标识（sys_role.role_key），用于判断登录人是不是医生 */
    private static final String ROLE_KEY_DOCTOR = "doctor";

    /** 基地类型：医疗基地（字典 cat_base_type） */
    private static final String BASE_TYPE_MEDICAL = "2";

    /** 就诊类型：初诊 / 复诊 */
    private static final String VISIT_FIRST = "1";
    private static final String VISIT_REVISIT = "2";

    /** 处方状态：已停用 */
    private static final String STOPPED = "1";

    @Override
    public CatDiagnosis selectCatDiagnosisById(Long diagnosisId)
    {
        CatDiagnosis diagnosis = diagnosisIdMapper.selectCatDiagnosisById(diagnosisId);
        if (diagnosis != null)
        {
            // 开单明细：编辑时要回显医生上次选的项目，且不能重复扣库存
            diagnosis.setItems(diagnosisItemMapper.selectByDiagnosisId(diagnosisId));
            // 处方：编辑时要回显上次开的用药方案
            diagnosis.setPrescription(prescriptionService.selectByDiagnosisId(diagnosisId));
        }
        return diagnosis;
    }

    @Override
    public List<CatDiagnosis> selectCatDiagnosisList(CatDiagnosis diagnosisId)
    {
        return diagnosisIdMapper.selectCatDiagnosisList(diagnosisId);
    }

    @Override
    public List<CatDiagnosisSummary> selectCatDiagnosisSummaryList(CatDiagnosis diagnosisId)
    {
        return diagnosisIdMapper.selectCatDiagnosisSummaryList(diagnosisId);
    }

    @Override
    public Map<String, Object> selectDiagnosisStat(CatDiagnosis diagnosisId)
    {
        Map<String, Object> stat = new LinkedHashMap<>();
        // 猫数：按病房/基地当前在册情况统计，已放归、已领养、已离世的猫不算
        stat.put("catCount", diagnosisIdMapper.countCatsInScope(diagnosisId));
        // 记录数：当前筛选条件下的诊断流水条数
        stat.put("recordCount", diagnosisIdMapper.countCatDiagnosis(diagnosisId));
        // 待接诊的猫：只在「仅按病房/基地筛选」时给，列表为空时用来告诉医生
        // 病房里还有哪几只猫没录过诊断，避免「有猫却 0 条记录」看起来像丢数据。
        // 一旦带上日期/医生/类型等条件，「没接诊」的语义就不成立了，直接返回空。
        boolean onlyByLocation = (diagnosisId.getWardId() != null || diagnosisId.getBaseId() != null)
                && !hasFilterBesidesLocation(diagnosisId);
        stat.put("pendingCats", onlyByLocation
                ? diagnosisIdMapper.selectPendingCats(diagnosisId)
                : new java.util.ArrayList<>());
        return stat;
    }

    /**
     * 除「病房 / 基地」之外是否还带了别的筛选条件
     * <p>
     * 猫数按位置统计、记录数按条件统计，两者口径本来就不同；
     * 只有「仅按位置筛」时，「这只猫没接诊过」才是个确定的结论。
     */
    private boolean hasFilterBesidesLocation(CatDiagnosis d)
    {
        if (d == null)
        {
            return true;
        }
        if (d.getCatId() != null
                || d.getSourceBaseId() != null
                || d.getDoctorUserId() != null
                || d.getVisitDate() != null
                || Boolean.TRUE.equals(d.getWardUnassigned()))
        {
            return true;
        }
        if (StringUtils.isNotEmpty(d.getVisitType())
                || StringUtils.isNotEmpty(d.getDoctor())
                || StringUtils.isNotEmpty(d.getSymptoms())
                || StringUtils.isNotEmpty(d.getDiagnosisResult()))
        {
            return true;
        }
        Map<String, Object> params = d.getParams();
        return params != null
                && (params.get("beginTime") != null || params.get("endTime") != null);
    }

    @Override
    public List<Map<String, Object>> selectWardStats(CatDiagnosis diagnosisId)
    {
        return diagnosisIdMapper.selectWardStats(diagnosisId);
    }

    @Override
    public Map<String, Object> selectCatHint(Long catId)
    {
        Map<String, Object> hint = new LinkedHashMap<>();
        CatProfile profile = catId == null ? null : profileMapper.selectCatProfileById(catId);
        if (profile == null)
        {
            throw new ServiceException("猫咪档案不存在");
        }
        int visits = diagnosisIdMapper.countVisitByCatId(catId);
        hint.put("catId", catId);
        hint.put("catName", profile.getCatName());
        hint.put("archiveNo", profile.getArchiveNo());
        // 来源基地：优先归属基地（猫本来在哪个基地），没有归属基地时取当前所在基地
        hint.put("sourceBaseId", profile.getOwnerBaseId() != null ? profile.getOwnerBaseId() : profile.getBaseId());
        hint.put("baseId", profile.getBaseId());
        hint.put("wardId", profile.getWardId());
        hint.put("visitCount", visits);
        hint.put("hasHistory", visits > 0);
        // 默认类型：在本站看过病就是复诊，第一次来就是初诊；医生可以改
        hint.put("defaultVisitType", visits > 0 ? VISIT_REVISIT : VISIT_FIRST);
        return hint;
    }

    @Override
    public List<Map<String, Object>> selectDoctorOptions()
    {
        return diagnosisIdMapper.selectDoctorOptions();
    }

    /**
     * 新增诊断时确定接诊医生
     * <p>
     * 1. 登录人是「医生」角色：一律记本人，忽略前端传值。
     *    前端本来就只读显示，这里再兜一层，防止绕过页面调接口冒填别人的名字。
     * 2. 其他角色（超管 / 管理员）：只能填系统里真实存在的医生账号，
     *    传了账号ID就按ID校验，只传姓名就按昵称反查；查不到直接报错，
     *    不再允许「王医生」这种对不上账号的自由文本。
     */
    private void applyDoctorOnInsert(CatDiagnosis diagnosisId)
    {
        SysUser loginUser = SecurityUtils.getLoginUser().getUser();
        if (isDoctorUser(loginUser))
        {
            diagnosisId.setDoctorUserId(loginUser.getUserId());
            diagnosisId.setDoctor(displayName(loginUser));
            return;
        }
        if (diagnosisId.getDoctorUserId() != null)
        {
            Map<String, Object> doctor = diagnosisIdMapper.selectDoctorUserById(diagnosisId.getDoctorUserId());
            if (doctor == null)
            {
                throw new ServiceException("所选医生不存在或已停用，请重新选择");
            }
            diagnosisId.setDoctor(toStr(doctor.get("nickName")));
            return;
        }
        if (StringUtils.isNotEmpty(diagnosisId.getDoctor()))
        {
            Map<String, Object> doctor = diagnosisIdMapper.selectDoctorUserByNickName(diagnosisId.getDoctor());
            if (doctor == null)
            {
                throw new ServiceException("医生「" + diagnosisId.getDoctor() + "」不在系统医生列表中，请从下拉选择");
            }
            Object userId = doctor.get("userId");
            if (userId instanceof Number)
            {
                diagnosisId.setDoctorUserId(((Number) userId).longValue());
            }
            diagnosisId.setDoctor(toStr(doctor.get("nickName")));
            return;
        }
        throw new ServiceException("请选择本次接诊医生");
    }

    /**
     * 修改诊断时的医生处理（只有超管能走到这里）
     * <p>
     * 传了账号ID就校验并同步姓名；只传姓名能匹配到医生就补上ID；
     * 匹配不到（比如历史手填的「助理小李」）保持原样，不阻断超管的纠错操作。
     */
    private void applyDoctorOnUpdate(CatDiagnosis diagnosisId)
    {
        if (diagnosisId.getDoctorUserId() != null)
        {
            Map<String, Object> doctor = diagnosisIdMapper.selectDoctorUserById(diagnosisId.getDoctorUserId());
            if (doctor == null)
            {
                throw new ServiceException("所选医生不存在或已停用，请重新选择");
            }
            diagnosisId.setDoctor(toStr(doctor.get("nickName")));
            return;
        }
        if (StringUtils.isNotEmpty(diagnosisId.getDoctor()))
        {
            Map<String, Object> doctor = diagnosisIdMapper.selectDoctorUserByNickName(diagnosisId.getDoctor());
            if (doctor != null)
            {
                Object userId = doctor.get("userId");
                if (userId instanceof Number)
                {
                    diagnosisId.setDoctorUserId(((Number) userId).longValue());
                }
                diagnosisId.setDoctor(toStr(doctor.get("nickName")));
            }
        }
    }

    /** 当前登录人是否拥有「医生」角色 */
    private boolean isDoctorUser(SysUser user)
    {
        if (user == null || user.getUserId() == null)
        {
            return false;
        }
        return roleService.selectRolePermissionByUserId(user.getUserId()).contains(ROLE_KEY_DOCTOR);
    }

    /** 账号展示名：优先昵称，昵称为空时退回登录名 */
    private String displayName(SysUser user)
    {
        return StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName();
    }

    private String toStr(Object value)
    {
        return value == null ? null : String.valueOf(value);
    }

    @Override
    @Transactional
    public int insertCatDiagnosis(CatDiagnosis diagnosisId)
    {
        // 岗位限制：助理只负责用药打卡、疫苗驱虫、拍照，诊断必须由医生开（管理岗不受限）
        if (staffService.hasCurrentRole(ICatStaffService.SCOPE_ASSISTANT)
                && !staffService.isCurrentManager()
                && !staffService.hasCurrentRole(ICatStaffService.SCOPE_DOCTOR))
        {
            throw new ServiceException("助理不能新增诊断记录，请由医生开单");
        }
        // 需求文档 6.2：站内诊断记录仅限于医疗基地，且每条记录都要标明当前治疗基地
        if (diagnosisId.getBaseId() == null)
        {
            throw new ServiceException("请选择当前治疗基地");
        }
        CatBase base = baseMapper.selectCatBaseById(diagnosisId.getBaseId());
        if (base == null || !BASE_TYPE_MEDICAL.equals(base.getBaseType()))
        {
            throw new ServiceException("站内诊断仅限医疗基地，请选择医疗基地");
        }
        fillSnapshot(diagnosisId, true);
        // 接诊医生必须落到真实账号上：医生登录就自动填本人，管理员只能从医生账号里选
        applyDoctorOnInsert(diagnosisId);
        if (diagnosisId.getCatId() != null && StringUtils.isEmpty(diagnosisId.getVisitType()))
        {
            // 没选就诊类型时按「这只猫有没有看过病」兜底，保证每条记录都有初诊/复诊
            diagnosisId.setVisitType(diagnosisIdMapper.countVisitByCatId(diagnosisId.getCatId()) > 0
                    ? VISIT_REVISIT : VISIT_FIRST);
        }
        // 「检查项目及用药」只能从目录选，不接受手写：没有明细就直接拒绝
        prepareItems(diagnosisId);
        int rows = diagnosisIdMapper.insertCatDiagnosis(diagnosisId);
        // 开单：写明细 + 自动出库扣库存 + 写对应台账（开了处方的话，用药台账交给处方展开）
        saveItems(diagnosisId);
        // 处方保存成功后按「天 × 时段 × 药」展开成用药记录，助理照着打卡
        savePrescription(diagnosisId);
        return rows;    }

    @Override
    @Transactional
    public int updateCatDiagnosis(CatDiagnosis diagnosisId)
    {
        // 修改正文只有超管能做：医生名允许同步成所选账号，但不做「医生本人强制覆盖」
        applyDoctorOnUpdate(diagnosisId);
        // 修改时不再从档案补病房：医生把病房清空就是清空，不能又自动填回去
        fillSnapshot(diagnosisId, false);
        // 提交了明细就按目录重新校验并重算「检查项目及用药」文字，杜绝手写残留
        boolean itemsTouched = diagnosisId.getItems() != null;
        if (itemsTouched && diagnosisId.getItems().isEmpty()
                && StringUtils.isNotEmpty(diagnosisId.getCheckItems()))
        {
            // 老记录只有手写的一段文字、没有明细：只要不去改它，就原样保留，不阻断其他字段的修改
            itemsTouched = false;
        }
        if (itemsTouched)
        {
            prepareItems(diagnosisId);
        }
        int rows = diagnosisIdMapper.updateCatDiagnosis(diagnosisId);
        // 病房被清空时（前端传空），上面的动态 SQL 不会写 null，这里单独清一次
        if (diagnosisId.getWardId() == null && diagnosisId.getDiagnosisId() != null)
        {
            diagnosisIdMapper.clearWard(diagnosisId.getDiagnosisId());
        }
        // 明细有提交才重建；没提交（老接口/老记录）就不碰库存，避免把手工台账搅乱
        if (itemsTouched && diagnosisId.getDiagnosisId() != null)
        {
            revertAutoEffects(diagnosisId.getDiagnosisId());
            saveItems(diagnosisId);
        }
        if (diagnosisId.getDiagnosisId() != null)
        {
            if (hasPrescription(diagnosisId))
            {
                savePrescription(diagnosisId);
            }
            else if (itemsTouched)
            {
                // 前端没带处方但动了开单明细：原来的处方明细刚被回滚清掉，得重新展开，
                // 否则助理那边待打卡的用药会凭空消失
                CatPrescription existing = prescriptionService.selectByDiagnosisId(diagnosisId.getDiagnosisId());
                if (existing != null && !STOPPED.equals(existing.getStatus()))
                {
                    prescriptionService.saveCatPrescription(existing);
                }
            }
        }
        return rows;
    }

    /**
     * 「检查项目及用药」只允许从目录选择，不接受手写：
     * 1. 明细为空直接拒绝；
     * 2. 以目录数据补齐名称/单价，保证快照真实；
     * 3. 把明细名称拼回 check_items 字段，库里永远与明细一致。
     */
    private void prepareItems(CatDiagnosis diagnosis)
    {
        List<CatDiagnosisItem> items = diagnosis.getItems();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("请从「检查项目及用药」目录中选择，该栏不支持手写");
        }
        for (CatDiagnosisItem item : items)
        {
            normalizeItem(item, diagnosis);
        }
        diagnosis.setCheckItems(items.stream()
                .map(CatDiagnosisItem::getItemName)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining("、")));
    }

    /**
     * 保存开单明细，并触发自动化：
     * 1. 明细落库（cat_diagnosis_item，名称/单价做快照，目录以后改名不影响历史）；
     * 2. 关联了物资的项目 → 生成出库记录，库存自动扣减（来源标「站内诊断」）；
     * 3. 用药 / 疫苗 / 驱虫类项目 → 自动写对应台账，同样标来源。
     */
    private void saveItems(CatDiagnosis diagnosis)
    {
        List<CatDiagnosisItem> items = diagnosis.getItems();
        if (items == null || items.isEmpty())
        {
            return;
        }
        Date happen = diagnosis.getVisitDate() == null ? new Date() : diagnosis.getVisitDate();
        for (CatDiagnosisItem item : items)
        {
            normalizeItem(item, diagnosis);
        }
        diagnosisItemMapper.batchInsert(items);
        // 开了处方就用处方展开的明细，别再按「只记当天一条」的旧逻辑重复写一遍用药台账
        boolean skipDrugLedger = hasPrescription(diagnosis);
        for (CatDiagnosisItem item : items)
        {
            applyStockOut(diagnosis, item, happen);
            applyLedger(diagnosis, item, happen, skipDrugLedger);
        }
    }

    /** 本次提交里是否带了有效的处方（有药品明细才算） */
    private boolean hasPrescription(CatDiagnosis diagnosis)
    {
        CatPrescription prescription = diagnosis.getPrescription();
        return prescription != null && prescription.getItems() != null && !prescription.getItems().isEmpty();
    }

    /**
     * 保存本次诊断开的处方
     * <p>
     * 处方挂在诊断单上：新增时用新诊断id，修改时沿用原来的处方（改方只从今天起生效）。
     */
    private void savePrescription(CatDiagnosis diagnosis)
    {
        if (!hasPrescription(diagnosis))
        {
            return;
        }
        CatPrescription prescription = diagnosis.getPrescription();
        prescription.setCatId(diagnosis.getCatId());
        prescription.setDiagnosisId(diagnosis.getDiagnosisId());
        if (StringUtils.isEmpty(prescription.getDoctor()))
        {
            prescription.setDoctor(diagnosis.getDoctor());
        }
        if (prescription.getDoctorUserId() == null)
        {
            prescription.setDoctorUserId(diagnosis.getDoctorUserId());
        }
        prescriptionService.saveCatPrescription(prescription);
    }

    /** 以目录为准补齐名称、类型、单位、单价，前端只传 id / 数量 / 用法 */
    private void normalizeItem(CatDiagnosisItem item, CatDiagnosis diagnosis)
    {
        if (item.getItemId() != null)
        {
            CatCheckItem catalog = checkItemMapper.selectCatCheckItemById(item.getItemId());
            if (catalog != null)
            {
                item.setItemName(catalog.getItemName());
                item.setItemType(catalog.getItemType());
                item.setMaterialId(catalog.getMaterialId());
                if (StringUtils.isNotEmpty(catalog.getUnit()))
                {
                    item.setUnit(catalog.getUnit());
                }
                // 关联了物资就按物资单位落库：库存是按片/支/瓶扣的，
                // 明细若记成「盒」就会出现「开 1 盒只扣 1 片」的账实不符
                if (catalog.getMaterialId() != null)
                {
                    CatMaterial material = materialMapper.selectCatMaterialById(catalog.getMaterialId());
                    if (material != null && StringUtils.isNotEmpty(material.getUnit()))
                    {
                        item.setUnit(material.getUnit());
                    }
                }
                if (catalog.getPrice() != null)
                {
                    item.setPrice(catalog.getPrice());
                }
            }
        }
        if (StringUtils.isEmpty(item.getItemName()))
        {
            throw new ServiceException("开单项目名称不能为空");
        }
        if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            item.setQuantity(BigDecimal.ONE);
        }
        if (item.getPrice() == null)
        {
            item.setPrice(BigDecimal.ZERO);
        }
        item.setAmount(item.getQuantity().multiply(item.getPrice()));
        item.setDiagnosisId(diagnosis.getDiagnosisId());
        item.setCatId(diagnosis.getCatId());
        if (StringUtils.isEmpty(item.getItemType()))
        {
            item.setItemType("1");
        }
    }

    /** 关联物资就出库：走出入库服务，库存扣减逻辑与手工登记完全一致 */
    private void applyStockOut(CatDiagnosis diagnosis, CatDiagnosisItem item, Date happen)
    {
        if (item.getMaterialId() == null)
        {
            return;
        }
        CatStockRecord record = new CatStockRecord();
        record.setMaterialId(item.getMaterialId());
        record.setRecordType("2");
        record.setQuantity(item.getQuantity());
        record.setRecordDate(happen);
        record.setPurpose("站内诊断开具：" + item.getItemName());
        record.setOperator(staffService.currentStaffName());
        record.setSourceType(SOURCE_DIAGNOSIS);
        record.setDiagnosisId(diagnosis.getDiagnosisId());
        record.setCatId(diagnosis.getCatId());
        record.setRemark("诊断单#" + diagnosis.getDiagnosisId());
        stockRecordService.insertCatStockRecord(record);
    }

    /** 用药/疫苗/驱虫自动落台账，医生开完单，对应台账就有一条来源为「站内诊断」的记录 */
    private void applyLedger(CatDiagnosis diagnosis, CatDiagnosisItem item, Date happen, boolean skipDrugLedger)
    {
        String type = item.getItemType();
        java.sql.Date dateOnly = new java.sql.Date(happen.getTime());
        String remark = "站内诊断开具（诊断单#" + diagnosis.getDiagnosisId() + "）"
                + (StringUtils.isEmpty(item.getUsageNote()) ? "" : "：" + item.getUsageNote());
        if (ITEM_TYPE_DRUG.equals(type))
        {
            if (skipDrugLedger)
            {
                // 本次开了处方：用药明细由处方按「天 × 时段」展开，这里不再生成单天的一条
                return;
            }
            CatMedication medication = new CatMedication();
            medication.setCatId(diagnosis.getCatId());
            medication.setUseDate(dateOnly);
            medication.setDrugName(item.getItemName());
            medication.setDosage(buildDosage(item));
            medication.setExecutor(diagnosis.getDoctor());
            medication.setSourceType(SOURCE_DIAGNOSIS);
            medication.setDiagnosisId(diagnosis.getDiagnosisId());
            medication.setRemark(cut(remark, 500));
            medication.setCreateBy(SecurityUtils.getUsername());
            medicationMapper.insertCatMedication(medication);
        }
        else if (ITEM_TYPE_VACCINE.equals(type))
        {
            CatVaccine vaccine = new CatVaccine();
            vaccine.setCatId(diagnosis.getCatId());
            vaccine.setVaccineType(item.getItemName());
            vaccine.setVaccineDate(dateOnly);
            vaccine.setResponsible(diagnosis.getDoctor());
            vaccine.setSourceType(SOURCE_DIAGNOSIS);
            vaccine.setDiagnosisId(diagnosis.getDiagnosisId());
            vaccine.setRemark(cut(remark, 500));
            vaccine.setCreateBy(SecurityUtils.getUsername());
            vaccineMapper.insertCatVaccine(vaccine);
        }
        else if (ITEM_TYPE_DEWORM.equals(type))
        {
            CatDeworm deworm = new CatDeworm();
            deworm.setCatId(diagnosis.getCatId());
            deworm.setDewormDrug(item.getItemName());
            deworm.setDewormDate(dateOnly);
            deworm.setSourceType(SOURCE_DIAGNOSIS);
            deworm.setDiagnosisId(diagnosis.getDiagnosisId());
            deworm.setRemark(cut(remark, 500));
            deworm.setCreateBy(SecurityUtils.getUsername());
            dewormMapper.insertCatDeworm(deworm);
        }
    }

    /**
     * 撤销这张诊断单之前产生的自动效果（修改明细、删除诊断时用）：
     * 按「出库-入库」的净值补一张反向单，库存回滚且痕迹保留；
     * 自动生成的用药/疫苗/驱虫台账软删。手工录入的记录一律不动。
     * <p>
     * 用净值而不是逐条冲销：改同一个诊断多次、或有人手工冲销过某条，
     * 都不会重复把库存加回去。
     */
    private void revertAutoEffects(Long diagnosisId)
    {
        List<Map<String, Object>> nets = stockRecordMapper.selectNetByDiagnosisId(diagnosisId);
        if (nets != null && !nets.isEmpty())
        {
            CatDiagnosis exist = diagnosisIdMapper.selectCatDiagnosisById(diagnosisId);
            Long catId = exist == null ? null : exist.getCatId();
            for (Map<String, Object> net : nets)
            {
                Object materialId = net.get("materialId");
                Object netQty = net.get("netQty");
                if (materialId == null || netQty == null)
                {
                    continue;
                }
                BigDecimal remain = new BigDecimal(String.valueOf(netQty));
                if (remain.compareTo(BigDecimal.ZERO) <= 0)
                {
                    continue;
                }
                // 还欠着库存的，补一张等量入库单；类型记为入库，来源仍是这张诊断单
                CatStockRecord back = new CatStockRecord();
                back.setMaterialId(((Number) materialId).longValue());
                back.setRecordType("1");
                back.setQuantity(remain);
                back.setRecordDate(new Date());
                back.setPurpose("诊断单撤销冲销");
                back.setOperator(staffService.currentStaffName());
                back.setSourceType(SOURCE_DIAGNOSIS);
                back.setDiagnosisId(diagnosisId);
                back.setCatId(catId);
                back.setRemark("冲销原记录（诊断单#" + diagnosisId + "）");
                stockRecordService.insertCatStockRecord(back);
            }
        }
        medicationMapper.deleteAutoByDiagnosisId(diagnosisId);
        vaccineMapper.deleteAutoByDiagnosisId(diagnosisId);
        dewormMapper.deleteAutoByDiagnosisId(diagnosisId);
        diagnosisItemMapper.deleteByDiagnosisId(diagnosisId);
    }

    /** 剂量列 50 字符，装不下就截断，避免整单保存失败 */
    private String buildDosage(CatDiagnosisItem item)
    {
        StringBuilder sb = new StringBuilder();
        if (item.getQuantity() != null)
        {
            sb.append(item.getQuantity().stripTrailingZeros().toPlainString());
        }
        if (StringUtils.isNotEmpty(item.getUnit()))
        {
            sb.append(item.getUnit());
        }
        if (StringUtils.isNotEmpty(item.getUsageNote()))
        {
            if (sb.length() > 0)
            {
                sb.append("，");
            }
            sb.append(item.getUsageNote());
        }
        return cut(sb.toString(), 50);
    }

    private String cut(String text, int max)
    {
        if (text == null)
        {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    /**
     * 补齐就诊时点的快照信息（来源基地、所在病房）
     * <p>
     * 来源基地：猫咪归属基地优先，没有归属基地时取当时所在基地。
     * 所在病房：只有病房确实属于本次治疗基地时才带出来，否则宁可留空，
     * 免得 A 基地的病房被挂到 B 基地的诊断上，按病房统计时会串数。
     *
     * @param fillWard 是否允许从档案自动带出病房（新增允许，修改不允许）
     */
    private void fillSnapshot(CatDiagnosis diagnosisId, boolean fillWard)
    {
        if (diagnosisId.getCatId() == null)
        {
            return;
        }
        CatProfile profile = profileMapper.selectCatProfileById(diagnosisId.getCatId());
        if (profile == null)
        {
            throw new ServiceException("猫咪档案不存在");
        }
        if (diagnosisId.getSourceBaseId() == null)
        {
            diagnosisId.setSourceBaseId(profile.getOwnerBaseId() != null ? profile.getOwnerBaseId() : profile.getBaseId());
        }
        if (fillWard && diagnosisId.getWardId() == null && profile.getWardId() != null && diagnosisId.getBaseId() != null)
        {
            CatWard ward = wardMapper.selectCatWardById(profile.getWardId());
            if (ward != null && diagnosisId.getBaseId().equals(ward.getBaseId()))
            {
                diagnosisId.setWardId(ward.getWardId());
            }
        }
        checkWardInBase(diagnosisId.getBaseId(), diagnosisId.getWardId());
    }

    /** 校验病房属于所选治疗基地，避免选错基地后挂上别的基地的病房 */
    private void checkWardInBase(Long baseId, Long wardId)
    {
        if (baseId == null || wardId == null)
        {
            return;
        }
        CatWard ward = wardMapper.selectCatWardById(wardId);
        if (ward == null)
        {
            throw new ServiceException("所选病房不存在");
        }
        if (!baseId.equals(ward.getBaseId()))
        {
            throw new ServiceException("病房「" + ward.getWardName() + "」不属于所选治疗基地，请重新选择");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCatDiagnosisByIds(Long[] diagnosisIds)
    {
        deleteImpactService.cascadeBeforeDelete("diagnosis", diagnosisIds);
        if (diagnosisIds != null)
        {
            for (Long id : diagnosisIds)
            {
                // 删单等于作废这次开药：出库冲销回去，自动生成的台账一并软删，手工录入的不动
                revertAutoEffects(id);
            }
        }
        return diagnosisIdMapper.deleteCatDiagnosisByIds(diagnosisIds);
    }

    @Override
    public int correctCatDiagnosis(Long diagnosisId, String correctionNote, String operator)
    {
        CatDiagnosis exist = diagnosisIdMapper.selectCatDiagnosisById(diagnosisId);
        if (exist == null)
        {
            throw new ServiceException("诊断记录不存在");
        }
        if (StringUtils.isEmpty(correctionNote))
        {
            throw new ServiceException("更正备注内容不能为空");
        }
        // 只追加更正说明，原始诊断字段一律不改，保证病史真实可追溯
        CatDiagnosis update = new CatDiagnosis();
        update.setDiagnosisId(diagnosisId);
        update.setCorrectionNote(correctionNote);
        update.setCorrectionBy(operator);
        update.setCorrectionTime(new java.util.Date());
        return diagnosisIdMapper.updateCatDiagnosis(update);
    }
}
