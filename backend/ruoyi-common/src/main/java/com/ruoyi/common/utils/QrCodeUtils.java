package com.ruoyi.common.utils;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;

/**
 * 二维码生成工具类
 * 
 * @author ruoyi
 */
public class QrCodeUtils
{
    /**
     * 生成二维码，返回 Base64 编码的 PNG 图片数据
     * 
     * @param content 二维码内容
     * @param width 宽度
     * @param height 高度
     * @return Base64 编码图片（含 data:image/png;base64, 前缀）
     */
    public static String generateBase64(String content, int width, int height)
    {
        try
        {
            BufferedImage image = generateImage(content, width, height);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        }
        catch (Exception e)
        {
            throw new RuntimeException("二维码生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成二维码 BufferedImage
     */
    public static BufferedImage generateImage(String content, int width, int height) throws Exception
    {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                if (bitMatrix.get(x, y))
                {
                    g.fillRect(x, y, 1, 1);
                }
            }
        }
        g.dispose();
        return image;
    }
}
