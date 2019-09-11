package com.hecknswell.ledserver.handlers;

import com.hecknswell.ledserver.domain.Animation;
import com.hecknswell.ledserver.domain.Frame;
import com.hecknswell.ledserver.domain.Pixel;
import com.hecknswell.ledserver.domain.PixelMap;
import com.hecknswell.ledserver.resources.UDPResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.IOException;


/**
 * Created by Leo on 20/12/2017.
 */
public class IdleAnimation implements Runnable {

    private static final int maxR = 150;
    private static final int maxL = 150;
    private static final int stepR = 1;
    private static final int stepL = 2;
    private static final int brush = 4;
    private static final int delay = 15; //ms

    private int[][] pixelMap;
    private int maxX;
    private int maxY;
    private int minX;
    private int minY;

    private int x;
    private int y;
    private int r;
    private int l;

    private static final Logger LOG = LoggerFactory.getLogger(IdleAnimation.class);


    public IdleAnimation(int[][] pixelMap, int maxX, int maxY, int minX, int minY) {
        this.pixelMap = pixelMap;
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = minX;
        this.minY = minY;
        x = 0;
        y = 0;
        r = maxR;
        l = 0;
    }

    @Override
    public void run() {
        LOG.info("Starting idle animation");
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
        for (int i=0; i<pixelMap.length * 3; i++) {
            data[i] = (byte) 50;
        }

        r += stepR;
        l -= stepL;
        if ((r >= maxR) || (l < 1)) {
            // new circle
            x = minX + (int) (Math.random() * (maxX-minX));
            y = minY + (int) (Math.random() * (maxY-minY));
            LOG.info("New circle, X={}, Y={}", x, y);
            r = 1;
            l = maxL;
        }
        int cx, cy;
        Color colour = new Color(l,l,l);
        for (int a=0; a<360; a+=(brush/2)) {
            cx = x + (int) (r * Math.cos(Math.toRadians(a)));
            cy = y + (int) (r * Math.sin(Math.toRadians(a)));
            paint(cx, cy, brush, colour, data, pixelMap);
        }
        return data;
    }

    private static void paint(int x, int y, int brushRadius, Color colour, byte[] data, int[][] pixelMap) {
        for (int i=0; i<pixelMap.length; i++) {
            // check if pixel is inside brush radius
            int x0 = pixelMap[i][0];
            int y0 = pixelMap[i][1];
            if (Math.sqrt((x-x0)*(x-x0) + (y-y0)*(y-y0)) < brushRadius) {
                colourPixel(i, colour, data);
            }
        }
    }

    private static void colourPixel(int pixelIndex, Color colour, byte[] data) {
        int p = pixelIndex * 3;
        data[p] = (byte) colour.getRed();
        data[p+1] = (byte) colour.getGreen();
        data[p+2] = (byte) colour.getBlue();
    }

}
