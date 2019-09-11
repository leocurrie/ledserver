package com.hecknswell.ledserver.handlers;

import com.hecknswell.ledserver.domain.Frame;
import com.hecknswell.ledserver.domain.Pixel;
import com.hecknswell.ledserver.domain.Pixels;
import com.hecknswell.ledserver.resources.UDPResource;

import java.io.IOException;

/**
 * Created by Leo on 17/12/2017.
 */
public class PixelHandler {

    private int pixelCount;
    private byte[] data;

    public PixelHandler(int pixelCount) {
        this.pixelCount = pixelCount;
        this.data = new byte[pixelCount * 3];
        for (int i=0; i<pixelCount*3; i++) {
            this.data[i] = 0;
        }
    }

    public byte[] handlePixels(Pixels pixels) {

        for (int j=0; j<pixels.getPaint().length; j++) {
            Pixel p = pixels.getPaint()[j];
            data[(p.getP()*3)] = (byte)p.getR();
            data[(p.getP()*3) + 1] = (byte)p.getG();
            data[(p.getP()*3) + 2] = (byte)p.getB();
        }
        return data;
    }

    public byte[] handleFrame(Frame f) {
        for (int i=0; i<pixelCount; i++) {
            int j = i * 3;
            Pixel p = f.getPixel(i);
            data[j] = (byte) p.getR();
            data[j + 1] = (byte) p.getG();
            data[j + 2] = (byte) p.getB();
        }
        return data;
    }

    public byte[] clear() {
        for (int k=0; k<pixelCount*3; k++) {
            data[k] = 0;
        }
        return data;
    }

}
