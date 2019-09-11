package com.hecknswell.ledserver.domain;

/**
 * Created by Leo on 18/12/2017.
 */
public class Frame {

    private long durationMs;
    private Pixel[] pixels;

    public Frame() {
        this.pixels = new Pixel[0];
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public void setPixels(Pixel[] pixels) {
        this.pixels = pixels;
    }

    public Pixel getPixel(int i) {
        return this.pixels[i];
    }


}
