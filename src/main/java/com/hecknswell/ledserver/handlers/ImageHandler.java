package com.hecknswell.ledserver.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hecknswell.ledserver.domain.Animation;
import com.hecknswell.ledserver.domain.Frame;
import com.hecknswell.ledserver.domain.Pixel;
import com.hecknswell.ledserver.resources.UDPResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Leo on 17/12/2017.
 */
public class ImageHandler {

    private byte[] data;
    private int[][] pixelMap;
    private int width;
    private int height;
    private PixelHandler pixelHandler;
    private static final Logger LOG = LoggerFactory.getLogger(ImageHandler.class);

    public ImageHandler(int[][] pixelMap, int width, int height, PixelHandler pixelHandler) {
        this.pixelMap = pixelMap;
        this.width = width;
        this.height = height;
        this.pixelHandler = pixelHandler;
    }

    public Frame toFrame(BufferedImage image) {
        BufferedImage img = image;
        if ((img.getWidth() != width) || (img.getHeight() != height)) {
            img = resize(image, width, height);
        }
        Pixel[] pixels = new Pixel[pixelMap.length];
        for (int i=0; i<pixelMap.length; i++) {
            Color c = new Color(img.getRGB(pixelMap[i][0], pixelMap[i][1]));
            Pixel p = new Pixel();
            p.setP(i);
            p.setR(c.getRed());
            p.setG(c.getGreen());
            p.setB(c.getBlue());
            pixels[i] = p;
        }
        Frame f = new Frame();
        f.setPixels(pixels);

        return f;

    }

    public byte[] handleImage(BufferedImage image) {
        BufferedImage img = image;
        if ((img.getWidth() != width) || (img.getHeight() != height)) {
            img = resize(image, width, height);
        }
        data = new byte[pixelMap.length * 3];
        for (int i=0; i<pixelMap.length; i++) {
            int x = pixelMap[i][0];
            int y = pixelMap[i][1];

            Color c = new Color(img.getRGB(x, y));
            int p = i * 3;
            data[p] = (byte) c.getRed();
            data[p + 1] = (byte) c.getGreen();
            data[p + 2] = (byte) c.getBlue();
        }
        return data;
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }


    public Animation loadAnimation(InputStream inputStream, String fileName) throws IOException {
        Animation anim = new Animation();

        if (fileName.toUpperCase().endsWith(".GIF")) {
            LOG.info("Found a GIF - parsing frames");
            ImageFrame[] images = AnimatedGifHandler.readGif(inputStream);
            LOG.info("GIF has " + images.length + " frames");
            for (int i = 0; i < images.length; i++) {
                BufferedImage image = images[i].getImage();
                Frame f = toFrame(image);
                int delay = images[i].getDelay();
                f.setDurationMs(60);
                anim.addFrame(f);
            }
        } else {
            LOG.info("Handling single image");
            BufferedImage imBuff = ImageIO.read(inputStream);
            Frame f = toFrame(imBuff);
            f.setDurationMs(1000);
            anim.addFrame(f);
        }

        return anim;
    }

    public Thread handleAnimation(Animation animation) throws IOException {
        Thread animationThread = null;
        LOG.info("Creating animation");
        AnimationRunnable animRunnable = new AnimationRunnable(animation, pixelHandler);
        return new Thread(animRunnable);
    }
}
