package com.ruoyi.cat.service.impl;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatConfig;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.mapper.CatConfigMapper;
import com.ruoyi.cat.mapper.CatFlowMapper;
import com.ruoyi.cat.service.ICatProfileService;
import com.ruoyi.cat.service.ICatStaffService;
import com.ruoyi.cat.util.CatAgeUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.QrCodeUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.exception.ServiceException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 猫咪档案 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class CatProfileServiceImpl implements ICatProfileService
{
    /**
     * 手机扫码访问不到的本机主机名
     */
    private static final Set<String> UNREACHABLE_HOSTS = Set.of("localhost", "127.0.0.1", "0.0.0.0", "::1", "[::1]");

    @Autowired
    private CatProfileMapper catProfileMapper;

    @Autowired
    private CatConfigMapper catConfigMapper;

    @Autowired
    private CatFlowMapper catFlowMapper;

    @Autowired
    private ICatStaffService staffService;

    @Override
    public CatProfile selectCatProfileById(Long catId)
    {
        CatProfile profile = catProfileMapper.selectCatProfileById(catId);
        // 编辑档案时回填已选标签，否则界面显示为空、一改动就会丢掉原有标签
        if (profile != null)
        {
            profile.setTagIds(catProfileMapper.selectTagIdsByCatId(catId));
            // 顺带带上二维码内容（扫码链接），前端可直接展示「二维码指向哪里」
            if (StringUtils.isNotEmpty(profile.getArchiveNo()))
            {
                String scanUrl = buildScanUrl(profile.getArchiveNo());
                profile.setScanUrl(scanUrl);
                // 图片按当前访问地址实时生成：库里存的那张可能是换端口/换网址之前画的，
                // 直接用会导致「左边文字说 81、扫出来却是 80」这种对不上的情况
                profile.setQrCode(QrCodeUtils.generateBase64(scanUrl, 300, 300));
            }
            fillAge(profile);
        }
        return profile;
    }

    @Override
    public List<CatProfile> selectCatProfileList(CatProfile catProfile)
    {
        // 「关键词」一个框搜名字/编号/特征/品种/性别/年龄，这里先把它拆成结构化条件
        applyKeyword(catProfile);
        List<CatProfile> list = catProfileMapper.selectCatProfileList(catProfile);
        // 年龄不落库：按出生日期用当前日期实时算，页面每次打开都是最新年龄
        for (CatProfile item : list)
        {
            fillAge(item);
        }
        return list;
    }

    /** 关键词里能识别的年龄说法（「1岁2个月」「3个月」「未知」），整段抓出来，免得被空格拆坏 */
    private static final Pattern AGE_PHRASE =
            Pattern.compile("(\\d+\\s*岁(?:\\s*\\d+\\s*个?月)?|\\d+\\s*个?月|未知|不详)");

    /**
     * 界面只留一个「关键词」输入框：名字、编号、特征、品种、性别、年龄都从这里搜。
     * <p>
     * 规则：空格/逗号分隔的多段表示「同时满足」，例如「橘猫 母 2岁」。
     * 年龄段不走文本匹配 —— 年龄是按出生日期实时算的，库里没有现成文本，
     * 所以转成月份区间交给 SQL 算（「2岁」＝ 2岁0个月～2岁11个月，「3个月」精确到那一个月）。
     * 「未知」沿用原来年龄下拉的口径：只查没有出生日期的档案。
     */
    private void applyKeyword(CatProfile query)
    {
        String raw = query.getKeyword();
        if (StringUtils.isEmpty(raw))
        {
            return;
        }
        Matcher matcher = AGE_PHRASE.matcher(raw);
        while (matcher.find())
        {
            String phrase = matcher.group().replaceAll("\\s+", "");
            if ("未知".equals(phrase) || "不详".equals(phrase))
            {
                query.setAgeUnknown(Boolean.TRUE);
                continue;
            }
            int[] range = parseAgeRange(phrase);
            if (range != null)
            {
                query.setAgeMinMonths(range[0]);
                query.setAgeMaxMonths(range[1]);
            }
        }
        // 年龄短语挖掉之后，剩下的才是要按文本模糊匹配的词
        String rest = AGE_PHRASE.matcher(raw).replaceAll(" ");
        List<String> tokens = new ArrayList<>();
        for (String token : rest.split("[\\s,，、]+"))
        {
            if (!StringUtils.isEmpty(token))
            {
                tokens.add(token);
            }
        }
        if (!tokens.isEmpty())
        {
            query.setKeywords(tokens.toArray(new String[0]));
        }
    }

    /** 把「2岁」「1岁2个月」「3个月」翻译成月份区间，认不出来返回 null */
    private int[] parseAgeRange(String phrase)
    {
        Matcher year = Pattern.compile("^(\\d+)岁(?:(\\d+)个?月)?$").matcher(phrase);
        if (year.matches())
        {
            int y = Integer.parseInt(year.group(1));
            if (year.group(2) != null)
            {
                int m = Integer.parseInt(year.group(2));
                return new int[] { y * 12 + m, y * 12 + m };
            }
            return new int[] { y * 12, y * 12 + 11 };
        }
        Matcher month = Pattern.compile("^(\\d+)个?月$").matcher(phrase);
        if (month.matches())
        {
            int m = Integer.parseInt(month.group(1));
            return new int[] { m, m };
        }
        return null;
    }

    /**
     * 计算并填充实时年龄（ageText / ageMonths）
     * <p>
     * 有出生日期就按今天实时算（估算的带「约」字）；历史数据还没有出生日期时，
     * 退回原始录入描述，避免页面突然一片空白。
     */
    private void fillAge(CatProfile profile)
    {
        if (profile == null)
        {
            return;
        }
        if (profile.getBirthDate() != null)
        {
            profile.setAgeText(CatAgeUtils.formatAge(profile.getBirthDate(), "1".equals(profile.getBirthEstimated())));
            profile.setAgeMonths(CatAgeUtils.monthsBetween(profile.getBirthDate()));
        }
        else
        {
            profile.setAgeText(StringUtils.isNotEmpty(profile.getAgeEstimate()) ? profile.getAgeEstimate() : null);
            profile.setAgeMonths(null);
        }
    }

    /**
     * 出生日期推导：录入「约 2 岁」「3 个月」这类描述时，结合救助日期反推出生日期
     * <p>
     * 只在没有出生日期时才推导，已填准确生日的档案不会被覆盖；
     * 推导出来的出生日期标记为「估算」，展示时带「约」字。
     */
    private void resolveBirthDate(CatProfile profile)
    {
        if (profile.getBirthDate() == null && StringUtils.isNotEmpty(profile.getAgeEstimate()))
        {
            java.time.LocalDate estimated = CatAgeUtils.estimateBirthDate(profile.getAgeEstimate(), profile.getCaptureDate());
            if (estimated != null)
            {
                profile.setBirthDate(java.sql.Date.valueOf(estimated));
                profile.setBirthEstimated("1");
            }
        }
        if (profile.getBirthDate() != null && StringUtils.isEmpty(profile.getBirthEstimated()))
        {
            // 手填了日期但没声明精度：默认按「估算」处理，宁可保守也不谎称准确
            profile.setBirthEstimated("1");
        }
    }

    /**
     * 修改档案时的出生日期处理
     * <p>
     * 年龄描述和救助日期都没变时沿用原出生日期——否则每保存一次（哪怕只改了名字），
     * 基准日「今天」都会往后挪一天，出生日期跟着漂移。
     */
    private void resolveBirthDateForUpdate(CatProfile profile)
    {
        if (profile.getBirthDate() != null)
        {
            if (StringUtils.isEmpty(profile.getBirthEstimated()))
            {
                profile.setBirthEstimated("1");
            }
            return;
        }
        // 年龄描述被清空：交给 SQL 把 birth_date 一起清掉
        if (StringUtils.isEmpty(profile.getAgeEstimate()))
        {
            return;
        }
        CatProfile old = catProfileMapper.selectCatProfileById(profile.getCatId());
        if (old != null && old.getBirthDate() != null
                && StringUtils.equals(profile.getAgeEstimate(), old.getAgeEstimate())
                && sameDay(profile.getCaptureDate(), old.getCaptureDate()))
        {
            profile.setBirthDate(old.getBirthDate());
            profile.setBirthEstimated(StringUtils.isEmpty(old.getBirthEstimated()) ? "1" : old.getBirthEstimated());
            return;
        }
        resolveBirthDate(profile);
    }

    /**
     * 两个日期是否为同一天（只比年月日，忽略时间部分）
     */
    private static boolean sameDay(Date a, Date b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        if (a == null || b == null)
        {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(a).equals(sdf.format(b));
    }

    @Override
    @Transactional
    public int insertCatProfile(CatProfile catProfile)
    {
        // 自动生成档案编号
        if (StringUtils.isEmpty(catProfile.getArchiveNo()))
        {
            catProfile.setArchiveNo(generateArchiveNo());
        }
        // 默认状态：在养
        if (StringUtils.isEmpty(catProfile.getStatus()))
        {
            catProfile.setStatus("1");
        }
        // 默认未归档
        if (StringUtils.isEmpty(catProfile.getArchived()))
        {
            catProfile.setArchived("0");
        }
        // 档案编号唯一性校验
        CatProfile dup = catProfileMapper.selectByArchiveNo(catProfile.getArchiveNo());
        if (dup != null)
        {
            throw new ServiceException("档案编号「" + catProfile.getArchiveNo() + "」已存在，请更换");
        }
        // 只填了「约 2 岁」时按救助日期反推出生日期，年龄后续靠出生日期实时算
        resolveBirthDate(catProfile);
        int rows = catProfileMapper.insertCatProfile(catProfile);
        // 生成二维码
        if (rows > 0 && catProfile.getCatId() != null)
        {
            String qrCode = generateQrCode(catProfile);
            catProfile.setQrCode(qrCode);
            catProfileMapper.updateCatProfile(catProfile);
            // 保存标签关联
            insertCatTag(catProfile.getCatId(), catProfile.getTagIds());
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateCatProfile(CatProfile catProfile)
    {
        // 档案编号唯一性校验（排除自身）
        if (StringUtils.isNotEmpty(catProfile.getArchiveNo()))
        {
            CatProfile dup = catProfileMapper.selectByArchiveNo(catProfile.getArchiveNo());
            if (dup != null && !dup.getCatId().equals(catProfile.getCatId()))
            {
                throw new ServiceException("档案编号「" + catProfile.getArchiveNo() + "」已被其它档案占用");
            }
        }
        // 改了年龄描述（且没填准确生日）时重新反推出生日期，年龄随之更新
        resolveBirthDateForUpdate(catProfile);
        // 更新前先留一份旧位置，用于判断"是不是换了病房 / 刚分配病房"
        CatProfile before = catProfileMapper.selectCatProfileById(catProfile.getCatId());
        int rows = catProfileMapper.updateCatProfile(catProfile);
        // 位置变了就自动补一条流转记录（人在现场放好猫、事后才来补录的场景）
        if (rows > 0)
        {
            writeLocationFlow(before, catProfile);
        }
        // 二维码内容是「扫码地址前缀 + 档案编号」：按最新编号重算一次，和提交上来的旧码不同才写库        // （前端保存时会把 form 里的旧 qrCode 一起提交，所以不能只判断 qrCode 是否为空）
        if (rows > 0 && StringUtils.isNotEmpty(catProfile.getArchiveNo()))
        {
            String freshQrCode = generateQrCode(catProfile);
            if (!freshQrCode.equals(catProfile.getQrCode()))
            {
                CatProfile qrUpdate = new CatProfile();
                qrUpdate.setCatId(catProfile.getCatId());
                qrUpdate.setQrCode(freshQrCode);
                catProfileMapper.updateCatProfile(qrUpdate);
            }
        }
        // 更新标签关联
        if (catProfile.getTagIds() != null)
        {
            catProfileMapper.deleteCatTagByCatId(catProfile.getCatId());
            insertCatTag(catProfile.getCatId(), catProfile.getTagIds());
        }
        return rows;
    }

    /**
     * 位置变更时自动补一条流转记录（2026-11-07 与救助站确认）。
     *
     * 场景：人已经把猫放进了某间屋，当时没在电脑上录，事后再到档案里把「所在病房」改过来。
     * 这里帮他把流水补上，否则系统里"猫在某屋、却没有任何流转记录"，事后查不清是谁放的、哪天放的。
     *
     * 两个时间分开存：
     *   operate_time —— 实际转移时间（补录时由前端填当时的时间，没填就取现在）
     *   create_time  —— 真正的保存时间，由 BaseEntity 自动写入
     * 两个不一样，就能看出这条是补录的。
     *
     * 只在状态是 在养(1) / 待安置(9) 且本次真的落到某个病房时写；
     * 外送、放归、领养、离世导致的位置清空，已由各自的状态流转表达，这里不重复记。
     */
    private void writeLocationFlow(CatProfile before, CatProfile after)
    {
        if (before == null || after.getCatId() == null)
        {
            return;
        }
        // 只处理"落到具体房间"，单纯清空病房不写
        if (after.getWardId() == null)
        {
            return;
        }
        String status = after.getStatus() != null ? after.getStatus() : before.getStatus();
        if (!"1".equals(status) && !"9".equals(status))
        {
            return;
        }
        boolean changed = !sameId(before.getBaseId(), after.getBaseId())
                || !sameId(before.getWardId(), after.getWardId());
        if (!changed)
        {
            return;
        }

        CatFlow flow = new CatFlow();
        flow.setCatId(after.getCatId());
        flow.setFromBaseId(before.getBaseId());
        flow.setFromWardId(before.getWardId());
        flow.setToBaseId(after.getBaseId() != null ? after.getBaseId() : before.getBaseId());
        flow.setToWardId(after.getWardId());
        flow.setFlowType("基地内安置");
        String staffName = staffService.currentStaffName();
        flow.setOperateBy(staffName != null ? staffName : SecurityUtils.getUsername());
        Date moveAt = parseMoveTime(after.getMoveTime());
        flow.setOperateTime(moveAt != null ? moveAt : new Date());
        flow.setFlowReason(before.getWardId() == null
                ? "在档案里补录：从未分配病房安置到具体房间"
                : "在档案里补录：调整所在病房");
        catFlowMapper.insertCatFlow(flow);
    }

    private boolean sameId(Long a, Long b)
    {
        if (a == null && b == null)
        {
            return true;
        }
        if (a == null || b == null)
        {
            return false;
        }
        return a.equals(b);
    }

    private Date parseMoveTime(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return null;
        }
        try
        {
            return DateUtils.parseDate(text);
        }
        catch (Exception e)
        {
            // 格式不对就退回"现在"，不因为一个时间格式让整次保存失败
            return null;
        }
    }

    @Override
    public int deleteCatProfileByIds(Long[] catIds)
    {
        return catProfileMapper.deleteCatProfileByIds(catIds);
    }

    @Override
    public int archiveCatProfile(Long catId)
    {
        return catProfileMapper.archiveCatProfile(catId);
    }

    @Override
    public int unarchiveCatProfile(Long catId)
    {
        return catProfileMapper.updateArchived(catId, "0");
    }

    @Override
    @Transactional
    public Map<String, Object> regenerateQrCode(Long catId)
    {
        CatProfile cat = catProfileMapper.selectCatProfileById(catId);
        if (cat == null)
        {
            return null;
        }
        String scanUrl = buildScanUrl(cat.getArchiveNo());
        String qrCode = QrCodeUtils.generateBase64(scanUrl, 300, 300);
        // 二维码内容只由「扫码地址前缀 + 档案编号」决定，两者都没变时重新生成得到的是同一张图
        boolean changed = !qrCode.equals(cat.getQrCode());
        CatProfile update = new CatProfile();
        update.setCatId(catId);
        update.setQrCode(qrCode);
        catProfileMapper.updateCatProfile(update);

        Map<String, Object> result = new HashMap<>();
        result.put("catId", catId);
        result.put("qrCode", qrCode);
        result.put("scanUrl", scanUrl);
        result.put("changed", changed);
        return result;
    }

    @Override
    public int regenerateAllQrCode()
    {
        List<CatProfile> list = catProfileMapper.selectCatProfileList(new CatProfile());
        int count = 0;
        for (CatProfile cat : list)
        {
            CatProfile update = new CatProfile();
            update.setCatId(cat.getCatId());
            update.setQrCode(generateQrCode(cat));
            count += catProfileMapper.updateCatProfile(update);
        }
        return count;
    }

    /**
     * 生成唯一档案编号：CAT + yyyyMMdd + 4位随机数
     */
    private String generateArchiveNo()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String prefix = "CAT" + sdf.format(new Date());
        String random = String.valueOf((int) (Math.random() * 9000 + 1000));
        return prefix + random;
    }

    /**
     * 生成二维码（指向移动端扫码档案页）
     */
    private String generateQrCode(CatProfile catProfile)
    {
        return QrCodeUtils.generateBase64(buildScanUrl(catProfile.getArchiveNo()), 300, 300);
    }

    @Override
    public String encodeQrCode(String archiveNo)
    {
        if (StringUtils.isEmpty(archiveNo))
        {
            return "";
        }
        return QrCodeUtils.generateBase64(buildScanUrl(archiveNo), 300, 300);
    }

    /**
     * 扫码链接格式：{扫码地址前缀}/m/cat/{archiveNo}（history 路由）
     */
    private String buildScanUrl(String archiveNo)
    {
        return getScanBaseUrl() + "/m/cat/" + archiveNo;
    }

    /**
     * 获取扫码前端地址前缀
     * <p>
     * 取值优先级：
     * 1. 系统配置 scan.base.url（填了真实地址时以配置为准 —— 生产环境把域名/IP 固定下来，
     *    打印出去的贴纸才不会因为换个端口就失效）；
     * 2. 配置为空、或填的是 localhost / 127.0.0.1 这类「手机扫不开」的地址时，
     *    用**浏览器地址栏的地址**（Origin / Referer）：它带的是用户实际在用的协议和端口，
     *    不受开发代理改写 Host 的影响（前端 81 → 代理到后端 8081，Host 会变成 8081）；
     * 3. 拿不到浏览器来源（curl、定时任务）时，退回 Host / X-Forwarded-*；
     * 4. 最后才用配置原值。
     * <p>
     * 兜底的意义：本地开发端口经常变（80 → 81），配置里写死的地址必然过期，
     * 二维码跟着访问地址走才不会被抱怨「端口都换了怎么还是老地址」。
     */
    private String getScanBaseUrl()
    {
        CatConfig config = catConfigMapper.selectCatConfigByKey("scan.base.url");
        String configured = config == null ? "" : stripTrailingSlash(config.getConfigValue());
        if (StringUtils.isNotEmpty(configured) && !isUnreachableUrl(configured))
        {
            return configured;
        }
        // 浏览器地址栏的地址：本机地址也认，至少端口和用户实际用的一致
        String clientOrigin = stripTrailingSlash(ServletUtils.getClientOrigin());
        if (StringUtils.isNotEmpty(clientOrigin))
        {
            return clientOrigin;
        }
        String fromRequest = stripTrailingSlash(ServletUtils.getRequestBaseUrl());
        if (StringUtils.isNotEmpty(fromRequest) && !isUnreachableUrl(fromRequest))
        {
            return fromRequest;
        }
        return configured;
    }

    /**
     * 去掉地址末尾多余的斜杠，避免拼出双斜杠
     */
    private static String stripTrailingSlash(String url)
    {
        String result = url == null ? "" : url.trim();
        while (result.endsWith("/"))
        {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * 判断地址是否为「手机访问不到」的本机地址
     */
    private static boolean isUnreachableUrl(String url)
    {
        String host = extractHost(url);
        return StringUtils.isEmpty(host) || UNREACHABLE_HOSTS.contains(host) || host.startsWith("127.");
    }

    /**
     * 从 URL 中取出主机名（忽略协议、端口、路径、末尾斜杠）
     */
    private static String extractHost(String url)
    {
        if (StringUtils.isBlank(url))
        {
            return "";
        }
        String rest = url.trim();
        int schemeIdx = rest.indexOf("://");
        if (schemeIdx >= 0)
        {
            rest = rest.substring(schemeIdx + 3);
        }
        int slashIdx = rest.indexOf('/');
        if (slashIdx >= 0)
        {
            rest = rest.substring(0, slashIdx);
        }
        if (rest.startsWith("["))
        {
            // IPv6 字面量：[::1]:8080
            int end = rest.indexOf(']');
            return end > 0 ? rest.substring(0, end + 1).toLowerCase() : rest.toLowerCase();
        }
        int colonIdx = rest.indexOf(':');
        if (colonIdx >= 0)
        {
            rest = rest.substring(0, colonIdx);
        }
        return rest.toLowerCase();
    }

    /**
     * 保存猫咪标签关联
     */
    private void insertCatTag(Long catId, Long[] tagIds)
    {
        if (tagIds != null && tagIds.length > 0)
        {
            catProfileMapper.batchCatTag(catId, tagIds);
        }
    }

    @Override
    public List<String> selectHospitalOptions()
    {
        return catProfileMapper.selectHospitalOptions();
    }
}
