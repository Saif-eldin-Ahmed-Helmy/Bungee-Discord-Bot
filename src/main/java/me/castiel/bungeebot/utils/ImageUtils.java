package me.castiel.bungeebot.utils;

import me.castiel.bungeebot.types.Dimensions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImageUtils {

    public static Dimensions getTextDimensions(String text, Font font) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setFont(font);
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int width = fontMetrics.stringWidth(text);
        int height = fontMetrics.getHeight();
        Dimensions dimensions = new Dimensions(width, height, fontMetrics.getAscent());
        graphics2D.dispose();
        return dimensions;
    }

    public static InputStream createCaptchaImage(String placeholderText, String captchaText) {
        Font captchaFont = new Font("Consolas", Font.PLAIN, 48);
        Font placeholderFont = new Font("Consolas", Font.PLAIN, 22);
        Dimensions dimensions = getTextDimensions(captchaText, captchaFont);
        BufferedImage image = new BufferedImage(dimensions.width + 250, dimensions.height + 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setFont(captchaFont);
        graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics2D.setStroke(new BasicStroke(3));
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int accent = fontMetrics.getAscent();
        {
            Color placeholderColor = new Color(95, 97, 99);
            graphics2D.setFont(placeholderFont);
            graphics2D.setColor(placeholderColor);
            int x = (int) NumUtils.random(3, 15);
            int y = (int) (accent + NumUtils.random(1, 100));
            for (Character character : placeholderText.toCharArray()) {
                graphics2D.drawString(character.toString(), x, y);
                x += fontMetrics.stringWidth(character.toString()) + NumUtils.random(20, 35);
                y = (int) (accent + NumUtils.random(1, 100));
            }
        }
        {
            int x, prevLineX, y, prevLineY;
            x = prevLineX = (int) NumUtils.random(3, 15);
            y = prevLineY = (int) (accent + NumUtils.random(1, 100));
            Color linesColor = new Color(95, 200, 99);
            Color textColor = new Color(120, 255, 120);
            graphics2D.setFont(captchaFont);
            for (Character character : captchaText.toCharArray()) {
                graphics2D.setColor(linesColor);
                graphics2D.drawLine(prevLineX, prevLineY - (accent / 3), x, y - (accent / 3));
                graphics2D.setColor(textColor);
                graphics2D.drawString(character.toString(), x, y);
                prevLineX = x;
                prevLineY = y;
                x += fontMetrics.stringWidth(character.toString()) + NumUtils.random(20, 35);
                y = (int) (accent + NumUtils.random(1, 100));
            }
        }
        graphics2D.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException ignored) {
            return null;
        }
    }
}
