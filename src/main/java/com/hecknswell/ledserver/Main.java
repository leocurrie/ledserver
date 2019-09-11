package com.hecknswell.ledserver;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hecknswell.ledserver.domain.*;
import com.hecknswell.ledserver.handlers.*;
import com.hecknswell.ledserver.resources.UDPResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


import static spark.Spark.*;

public class Main {

    private static final int pixelCount = 400;
    private static Thread animationThread;
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static final ObjectMapper mapper = new ObjectMapper();


    public static final String[] gifFilenames = new String[]{ "0.gif", "1.gif", "2.gif", "3.gif", "4.gif", "5.gif", "6.gif", "7.gif", "8.gif"};

    public static Animation[] animations;

    public static void main(String[] args) throws IOException, InterruptedException {

        PixelHandler pixelHandler = new PixelHandler(pixelCount);
        ImageHandler imageHandler = new ImageHandler(PixelMap.pixelMap, 960, 540, pixelHandler);
        preloadAnimations(imageHandler);

        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        ResetRunnable timeout = new ResetRunnable(pixelHandler, imageHandler);
        Thread resetThread = new Thread(timeout);
        resetThread.start();


        staticFiles.location("/"); // Static files
        init();

        after((request, response) -> {
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);
            LOG.info("{} {} {} \"{}\" {}", reportDate, request.raw().getRemoteAddr(), request.uri(), request.userAgent(), response.status());
        });

        post("/pixels", (req, res) -> {
            LOG.info("Handling POST to /pixels");
            res.type("application/json");

            stopAnimation();
            timeout.reset();
            try {
                byte[] data;
                Pixels pixels = mapper.readValue(req.body(), Pixels.class);
                LOG.info("Found {} new pixels", pixels.getPaint().length);
                if (pixels.getPaint().length > 400) {
                    throw new Exception("Too many pixels in post");
                }
                data = pixelHandler.handlePixels(pixels);
                UDPResource.sendData(data);
            } catch (Exception e) {
                LOG.info("Error parsing JSON in /pixels POST");
                return "{\"status\":\"ERROR\"}";
            }
            LOG.info("Finished handling POST to /pixels");
            return "{\"status\":\"OK\"}";
        });

        // zero all pixels
        post("/image", (req, res) -> {
            LOG.info("Handling POST to /image");
            res.type("application/json");
            String location = "upload";
            long maxFileSize = 20000000;
            long maxRequestSize = 60100000;
            int fileSizeThreshold = 1024;
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
                    location, maxFileSize, maxRequestSize, fileSizeThreshold);
            req.attribute("org.eclipse.jetty.multipartConfig",
                    multipartConfigElement);
            Part uploadedFile = req.raw().getPart("file");
            LOG.info("Found uploaded file " + uploadedFile.getSubmittedFileName());

            Animation animation = imageHandler.loadAnimation(uploadedFile.getInputStream(), uploadedFile.getSubmittedFileName());
            stopAnimation();
            timeout.reset();
            startAnimation(imageHandler.handleAnimation(animation));

            LOG.info("finished handling POST to /image");
            return "{\"status\":\"OK\"}";
        });

        // zero all pixels
        get("/animation", (req, res) -> {
            LOG.info("Handling GET to /animation");
            res.type("application/json");
            stopAnimation();
            timeout.reset();
            int animationId = Integer.parseInt(req.queryParams("id"));
            if ((animationId >= 0) && (animationId <gifFilenames.length)) {
                handleAnimation(animationId, imageHandler);
            } else {
                return "{\"status\":\"ERROR\"}";
            }
            LOG.info("finished handling GET to /animation");

            return "{\"status\":\"OK\"}";

        });

        get("/idle", (req, res) -> {
            LOG.info("Handling GET to /idle");
            stopAnimation();
            timeout.reset();
            IdleAnimation idleAnimation = new IdleAnimation(PixelMap.pixelMap, 662,540, 326, 0);
            Thread idle = new Thread(idleAnimation);
            startAnimation(idle);
            LOG.info("Finished handling GET to /idle");
            return "OK";

        });


        get("/text", (req, res) -> {
            LOG.info("Handling GET to /text");
            stopAnimation();
            timeout.reset();
            String text = req.queryParams("message");
            TextAnimation textAnimation = new TextAnimation(PixelMap.pixelMap, text, 960, 540);
            Thread textThread = new Thread(textAnimation);
            startAnimation(textThread);
            LOG.info("Finished handling GET to /text");
            return "OK";

        });

    }

    public static synchronized  void startAnimation(Thread t) {
        LOG.info("Starting thread");

        if (animationThread != null) {
            animationThread.interrupt();
        }
        animationThread = t;
        t.start();
    }

    private static synchronized void stopAnimation() {
        if (animationThread != null) {
            LOG.info("Stopping existing animation");
            animationThread.interrupt();
            animationThread = null;
        }
    }

    public static void handleAnimation(int animationId, ImageHandler imageHandler) throws IOException {
        Class clazz = Main.class;
        LOG.info("Starting animation {}", animationId);
        startAnimation(imageHandler.handleAnimation(animations[animationId]));
    }


    private static void preloadAnimations(ImageHandler imageHandler) throws IOException {
        Class clazz = Main.class;
        animations = new Animation[gifFilenames.length];
        for (int i=0; i<gifFilenames.length; i++) {
            LOG.info("Pre-loading animation {}", i);
            InputStream inputStream = clazz.getResourceAsStream("/images/"+gifFilenames[i]);
            animations[i] = imageHandler.loadAnimation(inputStream, gifFilenames[i]);
        }
    }


}


