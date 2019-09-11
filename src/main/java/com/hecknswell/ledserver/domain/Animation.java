package com.hecknswell.ledserver.domain;

/**
 * Created by Leo on 18/12/2017.
 */
public class Animation {

    private Frame[] frames;

    public Animation() {
        this.frames = new Frame[0];
    }

    public void addFrame(Frame f) {
        Frame[] newFrames = new Frame[frames.length + 1];
        for (int i=0; i<frames.length; i++) {
            newFrames[i] = frames[i];
        }
        newFrames[newFrames.length-1] = f;
        this.frames = newFrames;
    }

    public Frame[] getFrames() {
        return frames;
    }

    public void setFrames(Frame[] frames) {
        this.frames = frames;
    }

    public int getFrameCount() {
        if (this.frames != null) {
            return this.getFrames().length;
        } else {
            return 0;
        }
    }

    public synchronized Frame getFrame(int frameIndex) {
        if (frameIndex < this.getFrameCount()) {
            return this.getFrames()[frameIndex];
        } else {
            return null;
        }
    }

}
