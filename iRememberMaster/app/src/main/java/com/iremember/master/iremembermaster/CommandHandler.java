package com.iremember.master.iremembermaster;

import android.content.Context;
import android.util.Log;

import com.iremember.master.iremembermaster.Constants.Command;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by KEJ on 2018-03-09.
 */

public class CommandHandler extends Thread {
    DatagramSocket datagramSocket;
    private String mCommand;
    private LinkedList<String> answers = new LinkedList<String>();

    public CommandHandler(String command, Context context){
        mCommand = command;
        setDeviceDiscoveryTimer();
        this.start();
    }

    @Override
    public void run() {
        log("Start of run");
        DatagramPacket packetSend;
        DatagramPacket packetReceived;
        InetAddress receiverInetAddress;
        int receiverPort = 12345;
        byte[] buffer = new byte[1024];
        String answer = null;

        try{
            receiverInetAddress = InetAddress.getByName("255.255.255.255");
            datagramSocket = new DatagramSocket();
            datagramSocket.setBroadcast(true);
            buffer = mCommand.getBytes();
            packetSend = new DatagramPacket(buffer, buffer.length,
                    receiverInetAddress, receiverPort);
            datagramSocket.send(packetSend);
            log("After sending...");

            // Ta emot svar från rumsenheter
            byte[] readBuffer = new byte[1024];

            while (true) {
                log("In while");
                try {
                    packetReceived = new DatagramPacket(readBuffer, readBuffer.length);
                    datagramSocket.receive(packetReceived);
                    answer = new String(packetReceived.getData(),0, packetReceived.getLength());
                    answers.addLast(answer);
                }catch (Exception e) {
                    log("Exception in while...");
                    break;
                }
            }
        }catch (Exception e){
            log("Exception when socket is closed");
        }

        // Kolla hur många svar som kommit och jämför med hur många enheter som borde svarat.
        log("Kolla hur många svar som inkommit...");
    }


    private void setDeviceDiscoveryTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                datagramSocket.close();
            }
        };
        new Timer().schedule(task, Command.DURATION);
    }


    public void log(String msg) {
        Log.d("CommandHandler", msg);
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
