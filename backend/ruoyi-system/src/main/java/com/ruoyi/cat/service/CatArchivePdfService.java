package com.ruoyi.cat.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import com.ruoyi.cat.domain.CatBase;
import com.ruoyi.cat.domain.CatConfig;
import com.ruoyi.cat.domain.CatDeath;
import com.ruoyi.cat.domain.CatDeworm;
import com.ruoyi.cat.domain.CatDiagnosis;
import com.ruoyi.cat.domain.CatFlow;
import com.ruoyi.cat.domain.CatMedication;
import com.ruoyi.cat.domain.CatProfile;
import com.ruoyi.cat.domain.CatReferral;
import com.ruoyi.cat.domain.CatVaccine;
import com.ruoyi.cat.domain.CatWard;
import com.ruoyi.cat.mapper.CatBaseMapper;
import com.ruoyi.cat.mapper.CatConfigMapper;
import com.ruoyi.cat.mapper.CatDeathMapper;
import com.ruoyi.cat.mapper.CatDewormMapper;
import com.ruoyi.cat.mapper.CatDiagnosisMapper;
import com.ruoyi.cat.mapper.CatFlowMapper;
import com.ruoyi.cat.mapper.CatMedicationMapper;
import com.ruoyi.cat.mapper.CatProfileMapper;
import com.ruoyi.cat.mapper.CatReferralMapper;
import com.ruoyi.cat.mapper.CatVaccineMapper;
import com.ruoyi.cat.mapper.CatWardMapper;

/**
 * 单猫档案 PDF 导出服务
 * <p>
 * 汇总基本档案、标签、疫苗、驱虫、诊断、用药、转诊、流转、死亡记录，输出 A4 PDF。
 * 中文字体按「cat_config 配置 -> jar 内置字体 -> 系统字体」顺序自动探测，
 * 可通过 cat_config 的 pdf.font.path 指定任意 CJK TTF 字体文件路径。
 * <p>
 * jar 内已打包 Noto Sans SC Regular（OFL-1.1，覆盖简体中文 + 拉丁字母 + 数字），
 * 部署到没有装中文字体的 Linux 服务器也能直接导出，无需再做任何配置。
 * 内置字体在类路径里，OpenPDF 只吃文件路径，所以启动后首次导出会解压到临时目录。
 *
 * @author ruoyi
 */
@Service
public class CatArchivePdfService
{
    private static final Logger log = LoggerFactory.getLogger(CatArchivePdfService.class);

    /** 内置字体在 jar 内解压出来的临时文件（进程内复用，避免每次导出都写盘） */
    private File bundledFontFile;

    /** 候选中文字体（TTF，不支持 ttc 字体集合） */
    private static final String[] FONT_CANDIDATES = {
        "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
        "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttf",
        "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttf",
        "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
        "C:/Windows/Fonts/simhei.ttf",
        "C:/Windows/Fonts/msyh.ttf",
        "C:/Windows/Fonts/simsun.ttc"
    };

    @Autowired
    private CatProfileMapper catProfileMapper;
    @Autowired
    private CatVaccineMapper catVaccineMapper;
    @Autowired
    private CatDewormMapper catDewormMapper;
    @Autowired
    private CatDiagnosisMapper catDiagnosisMapper;
    @Autowired
    private CatMedicationMapper catMedicationMapper;
    @Autowired
    private CatReferralMapper catReferralMapper;
    @Autowired
    private CatFlowMapper catFlowMapper;
    @Autowired
    private CatDeathMapper catDeathMapper;
    @Autowired
    private CatBaseMapper catBaseMapper;
    @Autowired
    private CatWardMapper catWardMapper;
    @Autowired
    private CatConfigMapper catConfigMapper;

    private static final SimpleDateFormat DATE = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * 解析可用的中文字体，找不到时抛出明确异常
     */
    private BaseFont resolveFont()
    {
        List<String> paths = new ArrayList<>();
        // 1. 系统配置指定的字体
        try
        {
            CatConfig q = new CatConfig();
            q.setConfigKey("pdf.font.path");
            List<CatConfig> list = catConfigMapper.selectCatConfigList(q);
            if (list != null && !list.isEmpty() && list.get(0).getConfigValue() != null)
            {
                paths.add(list.get(0).getConfigValue().trim());
            }
        }
        catch (Exception ignored)
        {
        }
        // 2. 打包在 resources/fonts/cjk.ttf 的字体（随 jar 分发，换服务器不需要额外装字体）
        File bundled = bundledFont();
        if (bundled != null)
        {
            paths.add(bundled.getAbsolutePath());
        }
        // 3. 常见系统字体
        for (String p : FONT_CANDIDATES)
        {
            paths.add(p);
        }

        for (String p : paths)
        {
            try
            {
                if (p == null || p.endsWith(".ttc"))
                {
                    continue;
                }
                if (p.startsWith("file:"))
                {
                    p = new File(java.net.URI.create(p)).getAbsolutePath();
                }
                if (!new File(p).exists())
                {
                    continue;
                }
                return BaseFont.createFont(p, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
            catch (Exception ignored)
            {
            }
        }
        throw new IllegalStateException("未找到可用的中文字体，无法导出 PDF。请在「系统配置」中新增 pdf.font.path，指向一个中文 TTF 字体文件");
    }

    /**
     * 把 jar 里的 fonts/cjk.ttf 释放成磁盘临时文件
     * <p>
     * OpenPDF 的 {@code BaseFont.createFont} 只接受文件路径，而 jar 内资源
     * 用 {@code getResource(...).getFile()} 拿到的是 {@code jar:file:...!/fonts/cjk.ttf}，
     * 不是真实路径（打成 jar 后必然失败，本地 IDE 里跑却是好的）。
     * 所以这里读成流写到临时目录，进程内只解压一次。
     *
     * @return 临时字体文件；资源不存在或解压失败时返回 null
     */
    private synchronized File bundledFont()
    {
        if (bundledFontFile != null && bundledFontFile.isFile() && bundledFontFile.length() > 0)
        {
            return bundledFontFile;
        }
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("fonts/cjk.ttf"))
        {
            if (in == null)
            {
                return null;
            }
            File tmp = File.createTempFile("cat-rescue-cjk-", ".ttf");
            tmp.deleteOnExit();
            Files.copy(in, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            bundledFontFile = tmp;
            log.info("已释放内置中文字体: {} ({} KB)", tmp.getAbsolutePath(), tmp.length() / 1024);
            return tmp;
        }
        catch (Exception e)
        {
            log.warn("内置中文字体释放失败，将回退到系统字体: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 生成单猫档案 PDF
     *
     * @param catId 猫咪ID
     * @return PDF 字节流
     */
    public byte[] build(Long catId) throws Exception
    {
        CatProfile profile = catProfileMapper.selectCatProfileById(catId);
        if (profile == null)
        {
            throw new IllegalArgumentException("猫咪档案不存在");
        }

        BaseFont bf = resolveFont();
        Font title = new Font(bf, 18, Font.BOLD, new Color(0x1F, 0x2D, 0x3D));
        Font h2 = new Font(bf, 12, Font.BOLD, new Color(0x1F, 0x2D, 0x3D));
        Font normal = new Font(bf, 9, Font.NORMAL, Color.BLACK);
        Font label = new Font(bf, 9, Font.BOLD, new Color(0x55, 0x5F, 0x6D));
        Font small = new Font(bf, 8, Font.NORMAL, new Color(0x77, 0x77, 0x77));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 42, 42);
        PdfWriter.getInstance(doc, out);
        doc.open();

        // 标题
        Paragraph p = new Paragraph("流浪动物救助管理系统 · 猫咪档案", title);
        p.setAlignment(Element.ALIGN_CENTER);
        p.setSpacingAfter(4);
        doc.add(p);
        Paragraph sub = new Paragraph("档案编号：" + nvl(profile.getArchiveNo()) + "    导出时间：" + DATETIME.format(new Date()), small);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(12);
        doc.add(sub);

        // 基本信息 + 二维码
        PdfPTable head = new PdfPTable(new float[] { 78f, 22f });
        head.setWidthPercentage(100);
        head.setSpacingAfter(10);
        head.addCell(basicInfoTable(profile, label, normal));

        PdfPCell qrCell = new PdfPCell();
        qrCell.setBorder(Rectangle.NO_BORDER);
        qrCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Image qr = decodeQr(profile.getQrCode());
        if (qr != null)
        {
            qr.scaleToFit(96, 96);
            qrCell.addElement(qr);
        }
        Paragraph qrTip = new Paragraph("扫码查看最新档案", small);
        qrTip.setAlignment(Element.ALIGN_CENTER);
        qrCell.addElement(qrTip);
        head.addCell(qrCell);
        doc.add(head);

        // 标签
        String tags = tagNames(profile);
        doc.add(section("标签", h2));
        doc.add(para("　" + (tags.isEmpty() ? "无" : tags), normal));

        // 疫苗
        CatVaccine vq = new CatVaccine();
        vq.setCatId(catId);
        List<CatVaccine> vaccines = catVaccineMapper.selectCatVaccineList(vq);
        doc.add(section("疫苗记录（" + size(vaccines) + "）", h2));
        if (isEmpty(vaccines))
        {
            doc.add(emptyLine(normal));
        }
        else
        {
            PdfPTable t = table(new float[] { 22f, 30f, 24f, 24f }, label);
            t.addCell(th("接种日期", label));
            t.addCell(th("疫苗类型", label));
            t.addCell(th("下次到期", label));
            t.addCell(th("负责人", label));
            for (CatVaccine v : vaccines)
            {
                t.addCell(td(fmt(v.getVaccineDate()), normal));
                t.addCell(td(nvl(v.getVaccineType()), normal));
                t.addCell(td(fmt(v.getNextDate()), normal));
                t.addCell(td(nvl(v.getResponsible()), normal));
            }
            doc.add(t);
        }

        // 驱虫
        CatDeworm dq = new CatDeworm();
        dq.setCatId(catId);
        List<CatDeworm> deworms = catDewormMapper.selectCatDewormList(dq);
        doc.add(section("驱虫记录（" + size(deworms) + "）", h2));
        if (isEmpty(deworms))
        {
            doc.add(emptyLine(normal));
        }
        else
        {
            PdfPTable t = table(new float[] { 22f, 26f, 28f, 24f }, label);
            t.addCell(th("驱虫日期", label));
            t.addCell(th("驱虫类型", label));
            t.addCell(th("用药", label));
            t.addCell(th("下次到期", label));
            for (CatDeworm d : deworms)
            {
                t.addCell(td(fmt(d.getDewormDate()), normal));
                t.addCell(td(nvl(d.getDewormType()), normal));
                t.addCell(td(nvl(d.getDewormDrug()), normal));
                t.addCell(td(fmt(d.getNextDate()), normal));
            }
            doc.add(t);
        }

        // 诊断
        CatDiagnosis gq = new CatDiagnosis();
        gq.setCatId(catId);
        List<CatDiagnosis> diagnoses = catDiagnosisMapper.selectCatDiagnosisList(gq);
        doc.add(section("诊断记录（" + size(diagnoses) + "）", h2));
        if (isEmpty(diagnoses))
        {
            doc.add(emptyLine(normal));
        }
        else
        {
            PdfPTable t = table(new float[] { 16f, 14f, 26f, 22f, 22f }, label);
            t.addCell(th("就诊日期", label));
            t.addCell(th("医生", label));
            t.addCell(th("症状", label));
            t.addCell(th("诊断结果", label));
            t.addCell(th("医嘱", label));
            for (CatDiagnosis d : diagnoses)
            {
                t.addCell(td(fmt(d.getVisitDate()), normal));
                t.addCell(td(nvl(d.getDoctor()), normal));
                t.addCell(td(nvl(d.getSymptoms()), normal));
                t.addCell(td(nvl(d.getDiagnosisResult()), normal));
                t.addCell(td(nvl(d.getMedicalAdvice()), normal));
            }
            doc.add(t);
        }

        // 用药
        CatMedication mq = new CatMedication();
        mq.setCatId(catId);
        List<CatMedication> medications = catMedicationMapper.selectCatMedicationList(mq);
        doc.add(section("用药记录（" + size(medications) + "）", h2));
        if (isEmpty(medications))
        {
            doc.add(emptyLine(normal));
        }
        else
        {
            PdfPTable t = table(new float[] { 16f, 14f, 26f, 16f, 16f, 12f }, label);
            t.addCell(th("用药日期", label));
            t.addCell(th("时段", label));
            t.addCell(th("药品", label));
            t.addCell(th("剂量", label));
            t.addCell(th("给药方式", label));
            t.addCell(th("执行人", label));
            for (CatMedication m : medications)
            {
                t.addCell(td(fmt(m.getUseDate()), normal));
                t.addCell(td(nvl(m.getTimePeriod()), normal));
                t.addCell(td(nvl(m.getDrugName()), normal));
                t.addCell(td(nvl(m.getDosage()), normal));
                t.addCell(td(nvl(m.getAdministration()), normal));
                t.addCell(td(nvl(m.getExecutor()), normal));
            }
            doc.add(t);
        }

        // 转诊
        CatReferral rq = new CatReferral();
        rq.setCatId(catId);
        List<CatReferral> referrals = catReferralMapper.selectCatReferralList(rq);
        doc.add(section("外送转诊记录（" + size(referrals) + "）", h2));
        if (isEmpty(referrals))
        {
            doc.add(emptyLine(normal));
        }
        else
        {
            PdfPTable t = table(new float[] { 14f, 20f, 20f, 26f, 20f }, label);
            t.addCell(th("送医日期", label));
            t.addCell(th("医院", label));
            t.addCell(th("送医原因", label));
            t.addCell(th("医院诊断", label));
            t.addCell(th("结论/去向", label));
            for (CatReferral r : referrals)
            {
                t.addCell(td(fmt(r.getSendDate()), normal));
                t.addCell(td(nvl(r.getHospitalName()), normal));
                t.addCell(td(nvl(r.getSendReason()), normal));
                t.addCell(td(nvl(r.getHospitalDiagnosis()), normal));
                t.addCell(td(nvl(r.getResult()) + keep(r.getDischargeTo()), normal));
            }
            doc.add(t);
        }

        // 流转轨迹
        CatFlow fq = new CatFlow();
        fq.setCatId(catId);
        List<CatFlow> flows = catFlowMapper.selectCatFlowList(fq);
        doc.add(section("流转轨迹（" + size(flows) + "）", h2));
        if (isEmpty(flows))
        {
            doc.add(emptyLine(normal));
        }
        else
        {
            PdfPTable t = table(new float[] { 18f, 18f, 20f, 32f, 12f }, label);
            t.addCell(th("时间", label));
            t.addCell(th("类型", label));
            t.addCell(th("位置变化", label));
            t.addCell(th("原因/结论", label));
            t.addCell(th("操作人", label));
            for (CatFlow f : flows)
            {
                t.addCell(td(fmt(f.getOperateTime()), normal));
                t.addCell(td(nvl(f.getFlowType()), normal));
                t.addCell(td(baseName(f.getFromBaseId()) + keep(wardName(f.getFromWardId())) + " → "
                    + baseName(f.getToBaseId()) + keep(wardName(f.getToWardId())), normal));
                t.addCell(td(nvl(f.getFlowReason()) + keep(f.getHospitalName()) + keep(f.getHospitalResult()), normal));
                t.addCell(td(nvl(f.getOperateBy()), normal));
            }
            doc.add(t);
        }

        // 死亡记录
        CatDeath dq2 = new CatDeath();
        dq2.setCatId(catId);
        List<CatDeath> deaths = catDeathMapper.selectCatDeathList(dq2);
        if (!isEmpty(deaths))
        {
            doc.add(section("死亡记录", h2));
            for (CatDeath d : deaths)
            {
                PdfPTable t = table(new float[] { 22f, 78f }, label);
                t.addCell(th("死亡日期", label));
                t.addCell(td(fmt(d.getDeathDate()), normal));
                t.addCell(th("死亡地点", label));
                t.addCell(td(nvl(d.getDeathPlace()), normal));
                t.addCell(th("死亡类型", label));
                t.addCell(td(nvl(d.getDeathType()), normal));
                t.addCell(th("死因描述", label));
                t.addCell(td(nvl(d.getDeathReason()), normal));
                t.addCell(th("遗体处理", label));
                t.addCell(td(nvl(d.getBodyDisposal()), normal));
                t.addCell(th("登记人", label));
                t.addCell(td(nvl(d.getRegistrar()), normal));
                doc.add(t);
            }
        }

        // 页脚
        Paragraph foot = new Paragraph("本档案由流浪动物救助管理系统自动生成，数据以系统实时记录为准。", small);
        foot.setAlignment(Element.ALIGN_CENTER);
        foot.setSpacingBefore(16);
        doc.add(foot);

        doc.close();
        return out.toByteArray();
    }

    // ------------------------------------------------------------------ 工具方法

    private PdfPTable basicInfoTable(CatProfile p, Font label, Font normal)
    {
        PdfPTable t = new PdfPTable(new float[] { 16f, 34f, 16f, 34f });
        t.setWidthPercentage(100);
        row(t, label, normal, "档案编号", nvl(p.getArchiveNo()), "猫咪昵称", nvl(p.getCatName()));
        row(t, label, normal, "别名", nvl(p.getAlias()), "品种/花色", nvl(p.getBreed()));
        row(t, label, normal, "性别", sex(p.getSex()), "年龄", nvl(p.getAgeEstimate()));
        row(t, label, normal, "体重", nvl(p.getWeight()), "当前状态", status(p.getStatus()));
        row(t, label, normal, "所在基地", baseName(p.getBaseId()), "所在病房", wardName(p.getWardId()));
        row(t, label, normal, "来源", source(p.getSource()), "捕捉日期", fmt(p.getCaptureDate()));
        row(t, label, normal, "捕捉地点", nvl(p.getCapturePoint()), "是否绝育", yesNo(p.getNeutered()));
        row(t, label, normal, "驱虫状态", yesNo(p.getDewormed()), "疫苗状态", yesNo(p.getVaccinated()));
        row(t, label, normal, "外送医院", nvl(p.getHospitalName()), "是否归档", "1".equals(p.getArchived()) ? "已归档" : "在册");
        // 跨列展示较长文本
        t.addCell(th("性格特征", label));
        PdfPCell c1 = td(nvl(p.getPersonality()), normal);
        c1.setColspan(3);
        t.addCell(c1);
        t.addCell(th("外观特征", label));
        PdfPCell c2 = td(nvl(p.getFeature()), normal);
        c2.setColspan(3);
        t.addCell(c2);
        try
        {
            t.setWidths(new float[] { 16f, 34f, 16f, 34f });
        }
        catch (Exception ignored)
        {
        }
        return t;
    }

    private void row(PdfPTable t, Font label, Font normal, String k1, String v1, String k2, String v2)
    {
        t.addCell(th(k1, label));
        t.addCell(td(v1, normal));
        t.addCell(th(k2, label));
        t.addCell(td(v2, normal));
    }

    private Paragraph section(String text, Font h2)
    {
        Paragraph p = new Paragraph(text, h2);
        p.setSpacingBefore(12);
        p.setSpacingAfter(5);
        return p;
    }

    private Paragraph para(String text, Font f)
    {
        Paragraph p = new Paragraph(text, f);
        p.setSpacingAfter(2);
        return p;
    }

    private Paragraph emptyLine(Font f)
    {
        return para("　无记录", f);
    }

    private PdfPTable table(float[] widths, Font f)
    {
        PdfPTable t = new PdfPTable(widths);
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        return t;
    }

    private PdfPCell th(String text, Font f)
    {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(new Color(0xF2, 0xF5, 0xF9));
        c.setPadding(4);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private PdfPCell td(String text, Font f)
    {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setPadding(3);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return c;
    }

    private Image decodeQr(String base64)
    {
        try
        {
            if (base64 == null || base64.isEmpty())
            {
                return null;
            }
            String data = base64.contains(",") ? base64.substring(base64.indexOf(',') + 1) : base64;
            byte[] bytes = Base64.getDecoder().decode(data);
            return Image.getInstance(bytes);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private String tagNames(CatProfile p)
    {
        try
        {
            List<String> names = catProfileMapper.selectTagNamesByCatId(p.getCatId());
            if (names == null || names.isEmpty())
            {
                return "";
            }
            return String.join("、", names);
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private String baseName(Long baseId)
    {
        if (baseId == null)
        {
            return "-";
        }
        try
        {
            CatBase b = catBaseMapper.selectCatBaseById(baseId);
            return b == null ? "-" : nvl(b.getBaseName());
        }
        catch (Exception e)
        {
            return "-";
        }
    }

    private String wardName(Long wardId)
    {
        if (wardId == null)
        {
            return "";
        }
        try
        {
            CatWard w = catWardMapper.selectCatWardById(wardId);
            return w == null ? "" : "/" + nvl(w.getWardName());
        }
        catch (Exception e)
        {
            return "";
        }
    }

    private String sex(String v)
    {
        if ("1".equals(v))
        {
            return "公";
        }
        if ("2".equals(v))
        {
            return "母";
        }
        return "未知";
    }

    private String status(String v)
    {
        if (v == null)
        {
            return "-";
        }
        switch (v)
        {
            case "1": return "在养";
            case "2": return "外送就医中";
            case "3": return "待领养";
            case "4": return "医院评估无法治愈/姑息照料";
            case "5": return "已领养";
            case "6": return "已离世";
            case "7": return "已放归";
            default: return v;
        }
    }

    private String source(String v)
    {
        if (v == null)
        {
            return "-";
        }
        switch (v)
        {
            case "1": return "流浪救助";
            case "2": return "他人送养";
            case "3": return "繁殖";
            case "4": return "其他";
            default: return v;
        }
    }

    private String yesNo(String v)
    {
        if ("1".equals(v))
        {
            return "是";
        }
        if ("0".equals(v))
        {
            return "否";
        }
        return "-";
    }

    private String keep(String s)
    {
        return s == null || s.isEmpty() ? "" : " " + s;
    }

    private String nvl(String s)
    {
        return s == null || s.trim().isEmpty() ? "-" : s;
    }

    private String fmt(Date d)
    {
        return d == null ? "-" : DATE.format(d);
    }

    private boolean isEmpty(List<?> list)
    {
        return list == null || list.isEmpty();
    }

    private int size(List<?> list)
    {
        return list == null ? 0 : list.size();
    }
}
