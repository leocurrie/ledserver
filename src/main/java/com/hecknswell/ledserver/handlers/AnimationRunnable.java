package com.hecknswell.ledserver.handlers;

import com.hecknswell.ledserver.domain.Animation;
import com.hecknswell.ledserver.domain.Frame;
import com.hecknswell.ledserver.domain.Pixel;
import com.hecknswell.ledserver.resources.UDPResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Leo on 18/12/2017.
 */
public class AnimationRunnable implements Runnable {

    private Animation animation;
    private PixelHandler pixelHandler;
    private static final Logger LOG = LoggerFactory.getLogger(AnimationRunnable.class);


    public AnimationRunnable(Animation animation, PixelHandler pixelHandler ) {
        this.animation = animation;
        this.pixelHandler = pixelHandler;
    }

    @Override
    public void run() {
        LOG.info("Starting animation thread");
        long endTime = new Date().getTime() + (1000 * 60 * 5);
        boolean running = true;
        int i = 0;
        Frame f;
        while (running) {
            if (i >= this.animation.getFrameCount()) {
                i = 0;
            }
            f = this.animation.getFrame(i++);
            this.showFrame(f);

            try {
                long now = new Date().getTime();
                if (now > endTime) {
                    running = false;
                }
                Thread.sleep(f.getDurationMs());
            } catch (InterruptedException e) {
                running = false;
            }
        }
        /**
         *no need to clear on end of animation
        byte[] data = pixelHandler.clear();
        try {
            UDPResource.sendData(data);
        } catch (IOException e) {
            e.printStackTrace();
        }**/


        LOG.info("Ending animation thread");
    }

    private void showFrame(Frame f) {
        byte[] data = pixelHandler.handleFrame(f);
        try {
            UDPResource.sendData(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
