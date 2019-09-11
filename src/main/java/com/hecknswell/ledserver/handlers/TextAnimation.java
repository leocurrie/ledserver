package com.hecknswell.ledserver.handlers;

import com.hecknswell.ledserver.resources.UDPResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;

public class TextAnimation implements Runnable {

    private static final int delay = 15; //ms
    private int[][] pixelMap;
    private BufferedImage img;
    private int xOffset;
    private int viewportX;

    private static final Logger LOG = LoggerFactory.getLogger(TextAnimation.class);

    public TextAnimation(int[][] pixelMap, String text, int viewportX, int viewportY) {
        this.pixelMap = pixelMap;
        this.img = resize(renderText(text), pixelMap, viewportX, viewportY);
        this.xOffset = 0;
        this.viewportX = viewportX;
    }

    @Override
    public void run() {
        LOG.info("Starting text animation");
        boolean running = true;
        while(running) {
            byte[] data = getNextFrame();
            try {
                UDPResource.sendData(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                running = false;
            }
        }
        LOG.info("Ended idle animation");

    }

    private byte[] getNextFrame() {
        byte[] data = new byte[pixelMap.length * 3];

        for (int i=0; i<pixelMap.length; i++) {
            int x = pixelMap[i][0];
            int y = pixelMap[i][1];

            Color c = new Color(img.getRGB(x+xOffset, y));
            int p = i * 3;
            data[p] = (byte) c.getRed();
            data[p + 1] = (byte) c.getGreen();
            data[p + 2] = (byte) c.getBlue();
        }

        this.xOffset+=3;
        if (this.xOffset > (this.img.getWidth() - this.viewportX)) {
            LOG.info("Text animation - resetting x offset");
            this.xOffset = 0;
        }


        return data;
    }

    private static BufferedImage renderText(String text) {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        Font font = new Font("Arial", Font.PLAIN, 48);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text);
        int height = fm.getHeight();
        g2d.dispose();

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, img.getWidth(), img.getHeight());
        g2d.setFont(font);
        fm = g2d.getFontMetrics();
        g2d.setColor(new Color(0,0,0));
        g2d.drawString(text, 0, fm.getAscent());
        g2d.dispose();

        return img;

    }

    public static BufferedImage resize(BufferedImage img, int[][] pixelMap, int viewportX, int viewportY) {
        LOG.info("Original text image is {}x{}",img.getWidth(), img.getHeight());

        int minY=Integer.MAX_VALUE;
        int maxY=0;

        for (int p=0; p<pixelMap.length; p++) {
            int yVal = pixelMap[p][1];
            if (yVal > maxY) {
                maxY = yVal;
            }
            if (yVal < minY) {
                minY = yVal;
            }
        }
        maxY -=20;
        minY +=20;

        int newHeight = maxY - minY;
        int newWidth = (newHeight/img.getHeight()) * img.getWidth();
        LOG.info("Resizing text image to {}x{}", newWidth, newHeight);
        Image tmp = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        BufferedImage dimg = new BufferedImage(newWidth + (viewportX*2), viewportY, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, dimg.getWidth(), dimg.getHeight());
        g2d.drawImage(tmp, viewportX, (viewportY - maxY + 20) , null);
        g2d.dispose();

        // blur image
        Kernel kernel = new Kernel(3, 3, new float[] { 1f / 9f, 1f / 9f, 1f / 9f,
                1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f });
        BufferedImageOp op = new ConvolveOp(kernel);
        dimg = op.filter(dimg, null);

        return dimg;
    }
}
