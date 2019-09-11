# Tree Painting Java Server

## What is this?
This is a Java-based webserver that provides a API to control a string of LEDs.

* It supports individual pixels, as well as short arrays of pixels for the 'painting' mode
* It supports the upload of animated GIF files, and can 'play' the animation on the LEDs
* There are a number of pre-set animated GIFs that are buffered in memory ready to be selected by index

## How does it control the LEDs?
The LEDs are not controlled directly by this server - instead the server expects to be able to control the string of LEDs by sending data to a UDP server running on the local network.

A UDP server can be built using an ESP8266 module connected to a string of WS2811 LEDs, and the Arduino source code for this is included in this repo (esp8266.ino).

## How do I use it?

Before you can use this code, you will need:

1. A pixel 'map' file. 
   You need to create the map using the calibrate.py script in [this repo](https://github.com/leocurrie/xmastree-painting)
   Once you have the mapping js file, use it to update the map that is currently hard-coded in src/main/java/com/hecknswell/ledserver/domain/PixelMap.java

2. A working UDP server
   I used an ESP8266, and the source code for this is included.
   You could use something else - maybe a Raspberry PI?
   The UDP server is very simple - it takes a UDP packet which simply contains the RGB data for the strip and displays it immediately on the string.
   
3. Edit com.hecknswell.ledserver.resources.UDPResource.java
   You need to set the correct IP address for your UDP server / EDP8266 module.
   Tip: It's a good idea to setup a static IP address for your ESP8266. Using the MAC address of the ESP8266, add it to the 'static lease' config in your WiFi Router DHCP config.

4. Edit line 26 of Main.java
   Set this value to match the number of LEDs in your chain, this should also match the value that your ESP8266 is configured to accept

5. Edit line 39 of Main.java
   You need to set the width and height of your webcam image.
   For example I was using a webcam video resolution of 960x540 but you might be using something else.

6. Build and run the server!
   First make sure you have a Java JDK installed. You need JDK 8 or newer.
   This project is setup to use Gradle, so you should be able to do something like
   `$ ./gradlew run`


