package com.ruoyi.cat.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatConfig;
import com.ruoyi.cat.domain.CatMaterial;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatReferral;
import com.ruoyi.cat.domain.CatReminder;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.domain.CatVisit;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.mapper.CatConfigMapper;
import com.ruoyi.cat.mapper.CatMaterialMapper;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.mapper.CatReferralMapper;
import com.ruoyi.cat.mapper.CatReminderMapper;
import com.ruoyi.cat.mapper.CatVaccineMapper;
import com.ruoyi.cat.mapper.CatVisitMapper;

/**
 * 提醒生成服务
 * 扫描疫苗、驱虫、回访、库存、转诊接回，生成到期提醒
 * 同一业务对象（catId + 类型 + 内容）只要存在未处理提醒，就不重复生成
 *
 * @author ruoyi
 */
@Service
public class CatReminderGenerateService
{
    @Autowired
    private CatVaccineMapper catVaccineMapper;
    @Autowired
    private CatDewormMapper catDewormMapper;
    @Autowired
    private CatVisitMapper catVisitMapper;
    @Autowired
    private CatMaterialMapper catMaterialMapper;
    @Autowired
    private CatProfileMapper catProfileMapper;
    @Autowired
    private CatReminderMapper catReminderMapper;
    @Autowired
    private CatReferralMapper catReferralMapper;
    @Autowired
    private CatConfigMapper catConfigMapper;

    /** 疫苗/驱虫到期提醒：默认提前 7 天开始提醒 */
    private static final String KEY_ADVANCE = "reminder.advance.days";
    /** 疫苗/驱虫到期提醒：默认过期后 7 天内仍提醒 */
    private static final String KEY_OVERDUE = "reminder.overdue.days";
    /** 外送医院待接回：默认超过 7 天提醒 */
    private static final String KEY_REFERRAL = "referral.pending.days";

    /** 各类型提醒的收件岗位配置，值为逗号分隔的岗位名称 */
    private static final String KEY_POST_VACCINE = "reminder.post.vaccine";
    private static final String KEY_POST_DEWORM = "reminder.post.deworm";
    private static final String KEY_POST_VISIT = "reminder.post.visit";
    private static final String KEY_POST_STOCK = "reminder.post.stock";
    private static final String KEY_POST_REFERRAL = "reminder.post.referral";

    /**
     * 读取文本配置项，缺失时返回默认值
     */
    private String cfgStr(String key, String def)
    {
        try
        {
            CatConfig query = new CatConfig();
            query.setConfigKey(key);
            List<CatConfig> list = catConfigMapper.selectCatConfigList(query);
            if (list != null && !list.isEmpty() && list.get(0).getConfigValue() != null)
            {
                return list.get(0).getConfigValue().trim();
            }
        }
        catch (Exception ignored)
        {
        }
        return def;
    }

    /**
     * 按岗位配置解析收件人登录名，多个用逗号拼接
     * 配置为空或岗位下没有启用用户时返回 null，表示全员可见
     */
    private String resolveTargetUser(String configKey)
    {
        String posts = cfgStr(configKey, "");
        if (posts.isEmpty())
        {
            return null;
        }
        List<String> postNames = new ArrayList<>();
        for (String p : posts.split(","))
        {
            String name = p.trim();
            if (!name.isEmpty())
            {
                postNames.add(name);
            }
        }
        if (postNames.isEmpty())
        {
            return null;
        }
        List<String> users = catReminderMapper.selectUserNamesByPostNames(postNames);
        if (users == null || users.isEmpty())
        {
            return null;
        }
        return String.join(",", users);
    }

    /**
     * 读取整数配置项，缺失时返回默认值
     */
    private int cfgInt(String key, int def)
    {
        try
        {
            CatConfig query = new CatConfig();
            query.setConfigKey(key);
            List<CatConfig> list = catConfigMapper.selectCatConfigList(query);
            if (list != null && !list.isEmpty() && list.get(0).getConfigValue() != null)
            {
                return Integer.parseInt(list.get(0).getConfigValue().trim());
            }
        }
        catch (Exception ignored)
        {
        }
        return def;
    }

    /**
     * 生成所有到期提醒
     */
    public int generateAll()
    {
        int count = 0;
        count += generateVaccineReminders();
        count += generateDewormReminders();
        count += generateVisitReminders();
        count += generateStockReminders();
        count += generateReferralReminders();
        return count;
    }

    /**
     * 判断同一业务对象是否已经生成过提醒，避免重复插入
     * 这里不看已读状态：已读是按人记录的，读了不代表业务已处理，
     * 只要提醒还在列表里就不应该每天再生成一条新的
     */
    private boolean exists(CatReminder reminder)
    {
        return catReminderMapper.selectCountByBizKey(reminder) > 0;
    }

    private boolean insertIfAbsent(CatReminder reminder)
    {
        if (!exists(reminder))
        {
            catReminderMapper.insertCatReminder(reminder);
            return true;
        }
        return false;
    }

    private String catName(Long catId)
    {
        if (catId == null)
        {
            return "";
        }
        CatProfile cat = catProfileMapper.selectCatProfileById(catId);
        return cat != null && cat.getCatName() != null ? cat.getCatName() : "";
    }

    /**
     * 疫苗到期提醒（到期前 N 天 ~ 过期 N 天内，N 由 cat_config 配置）
     */
    private int generateVaccineReminders()
    {
        int count = 0;
        int advance = cfgInt(KEY_ADVANCE, 7);
        int overdue = cfgInt(KEY_OVERDUE, 7);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, advance);
        Date deadline = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -overdue);
        Date earliest = c.getTime();

        List<CatVaccine> vaccines = catVaccineMapper.selectCatVaccineList(new CatVaccine());
        String target = resolveTargetUser(KEY_POST_VACCINE);
        for (CatVaccine v : vaccines)
        {
            if (v.getNextDate() == null || v.getNextDate().before(earliest) || v.getNextDate().after(deadline))
            {
                continue;
            }
            CatReminder reminder = new CatReminder();
            reminder.setCatId(v.getCatId());
            reminder.setReminderType("疫苗");
            reminder.setTitle("疫苗到期提醒");
            reminder.setContent("猫咪「" + catName(v.getCatId()) + "」疫苗到期：" + nvl(v.getVaccineType()));
            reminder.setRemindDate(v.getNextDate());
            reminder.setStatus("0");
            reminder.setTargetUser(target);
            if (insertIfAbsent(reminder))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * 驱虫到期提醒（到期前 N 天 ~ 过期 N 天内，N 由 cat_config 配置）
     */
    private int generateDewormReminders()
    {
        int count = 0;
        int advance = cfgInt(KEY_ADVANCE, 7);
        int overdue = cfgInt(KEY_OVERDUE, 7);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, advance);
        Date deadline = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -overdue);
        Date earliest = c.getTime();

        List<CatDeworm> deworms = catDewormMapper.selectCatDewormList(new CatDeworm());
        String target = resolveTargetUser(KEY_POST_DEWORM);
        for (CatDeworm d : deworms)
        {
            if (d.getNextDate() == null || d.getNextDate().before(earliest) || d.getNextDate().after(deadline))
            {
                continue;
            }
            CatReminder reminder = new CatReminder();
            reminder.setCatId(d.getCatId());
            reminder.setReminderType("驱虫");
            reminder.setTitle("驱虫到期提醒");
            reminder.setContent("猫咪「" + catName(d.getCatId()) + "」驱虫到期：" + nvl(d.getDewormType()));
            reminder.setRemindDate(d.getNextDate());
            reminder.setStatus("0");
            reminder.setTargetUser(target);
            if (insertIfAbsent(reminder))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * 领养回访到期提醒
     */
    private int generateVisitReminders()
    {
        int count = 0;
        CatVisit query = new CatVisit();
        query.setVisitStatus("0"); // 待回访
        List<CatVisit> visits = catVisitMapper.selectCatVisitList(query);
        String target = resolveTargetUser(KEY_POST_VISIT);
        for (CatVisit v : visits)
        {
            if (v.getPlanDate() == null)
            {
                continue;
            }
            CatReminder reminder = new CatReminder();
            reminder.setCatId(v.getCatId());
            reminder.setReminderType("回访");
            reminder.setTitle("领养回访提醒");
            reminder.setContent("猫咪「" + catName(v.getCatId()) + "」领养回访计划「" + nvl(v.getPlanType()) + "」到期，请登记回访记录");
            reminder.setRemindDate(v.getPlanDate());
            reminder.setStatus("0");
            reminder.setTargetUser(target);
            if (insertIfAbsent(reminder))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * 库存不足提醒
     */
    private int generateStockReminders()
    {
        int count = 0;
        List<CatMaterial> materials = catMaterialMapper.selectCatMaterialList(new CatMaterial());
        String target = resolveTargetUser(KEY_POST_STOCK);
        for (CatMaterial m : materials)
        {
            if (m.getStock() != null && m.getMinStock() != null
                && m.getStock().compareTo(m.getMinStock()) <= 0)
            {
                CatReminder reminder = new CatReminder();
                reminder.setReminderType("库存");
                reminder.setTitle("库存不足提醒");
                // 内容里不放实时库存数，否则每次盘点数量变化都会生成一条新提醒
                reminder.setContent("物资「" + nvl(m.getMaterialName()) + "」库存低于预警值 " + m.getMinStock() + " "
                    + nvl(m.getUnit()) + "，请及时补货");
                reminder.setRemindDate(new Date());
                reminder.setStatus("0");
                reminder.setTargetUser(target);
                if (insertIfAbsent(reminder))
                {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 外送医院待接回提醒（外送超过 N 天仍未出院/接回）
     */
    private int generateReferralReminders()
    {
        int count = 0;
        int pendingDays = cfgInt(KEY_REFERRAL, 7);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -pendingDays);
        Date deadline = c.getTime();

        CatReferral query = new CatReferral();
        query.setStatus("0"); // 外送中
        List<CatReferral> referrals = catReferralMapper.selectCatReferralList(query);
        String target = resolveTargetUser(KEY_POST_REFERRAL);
        for (CatReferral r : referrals)
        {
            if (r.getSendDate() == null || r.getSendDate().after(deadline))
            {
                continue;
            }
            CatReminder reminder = new CatReminder();
            reminder.setCatId(r.getCatId());
            reminder.setReminderType("转诊");
            reminder.setTitle("外送待接回提醒");
            reminder.setContent("猫咪「" + catName(r.getCatId()) + "」在「" + nvl(r.getHospitalName()) + "」治疗已超" + pendingDays
                + "天，请跟进接回或出院登记");
            reminder.setRemindDate(new Date());
            reminder.setStatus("0");
            reminder.setTargetUser(target);
            if (insertIfAbsent(reminder))
            {
                count++;
            }
        }
        return count;
    }

    private String nvl(String s)
    {
        return s == null ? "" : s;
    }
}
