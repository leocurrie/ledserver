package com.hecknswell.ledserver.handlers;

import com.hecknswell.ledserver.Main;
import com.hecknswell.ledserver.domain.PixelMap;
import com.hecknswell.ledserver.resources.UDPResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Leo on 19/12/2017.
 */
public class ResetRunnable implements Runnable {

    private long expiryTime;
    private static final long timeout = 5 * 60 * 1000;
    private PixelHandler pixelHandler;
    private ImageHandler imageHandler;
    private static final Logger LOG = LoggerFactory.getLogger(ResetRunnable.class);


    public ResetRunnable(PixelHandler pixelHandler, ImageHandler imageHandler) {
        this.pixelHandler = pixelHandler;
        this.imageHandler = imageHandler;
        this.reset();
    }

    public void reset() {
        LOG.info("Resetting timeout");
        expiryTime = new Date().getTime() + timeout;
    }




    @Override
    public void run() {
        boolean running = true;
        while (running) {
            handleTimeout();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }


    private void handleTimeout() {
        if (new Date().getTime() > expiryTime) {
            LOG.info("Timeout triggered");
            //try {
                //Main.handleAnimation(8, imageHandler);
                TextAnimation textAnimation = new TextAnimation(PixelMap.pixelMap, "happy new year 2019", 960, 540);
                Thread textThread = new Thread(textAnimation);
                Main.startAnimation(textThread);
                //Main.handleAnimation(this.idleAnimation);
                //UDPResource.sendData(pixelHandler.clear());

            //} catch (IOException e) {
            //    e.printStackTrace();
            //}

            reset();
        }
    }
}
