package com.iremember.master.iremembermaster.oldstuff;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by KEJ on 2018-03-07.
 */

public class CommandSender extends  Thread {
    String mCommand;
    InetAddress host;
    int port;

    public CommandSender(String mCommand, InetAddress host, int port) {
        log("CommandSender, mCommand " + mCommand);
        log("host: " + host);
        log("port: " + port);
        this.mCommand = mCommand;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run(){
        byte[] buffer;
        buffer = mCommand.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length, host, port);
        try {
            DatagramSocket mDatagramSocket = new DatagramSocket();
            mDatagramSocket.send(datagramPacket);
            mDatagramSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void log(String msg) {
        Log.d("CommandHandler", msg);
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }


}
