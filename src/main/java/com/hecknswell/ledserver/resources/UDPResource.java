package com.hecknswell.ledserver.resources;

import java.io.IOException;
import java.net.*;

/**
 * Created by Leo on 16/12/2017.
 */
public class UDPResource {

    private static final String ipAddress = "192.168.1.20";
    private static final int port = 2390;

    public static void sendData(byte[] data) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress addr = InetAddress.getByName(ipAddress);
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, addr, port);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }


}
