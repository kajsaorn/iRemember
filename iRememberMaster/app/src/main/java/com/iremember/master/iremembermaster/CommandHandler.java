package com.iremember.master.iremembermaster;

import android.content.Context;
import android.util.Log;

import com.iremember.master.iremembermaster.Constants.Command;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by KEJ on 2018-03-09.
 */

public class CommandHandler extends Thread {
    DatagramSocket datagramSocket;
// For MulticastSocket    MulticastSocket multicastSocket;
    private String mCommand;
    private Hashtable<String, String> answers = new Hashtable<String, String>();
    private Hashtable<String, String> registered = new Hashtable<String, String>();

    public CommandHandler(String command, Context context){
        mCommand = command;
        populateRegistered();
        this.start();
    }

    @Override
    public void run() {
        log("Start of run");
        DatagramPacket packetSend;
        DatagramPacket packetReceived;
        InetAddress receiverInetAddress;
        int receiverPort = 12345;
        byte[] sendBuffer = new byte[1024];
        String answer = null;
        byte[] receiveBuffer = new byte[1024];

        try{
            // Sending the packet. Trying a few times
            for (int i=0; i<10 && (answers.size() != registered.size()); i++) {
                log("Try nbr: " + i);
                // Set up for sending meal command to receivers
                setDeviceDiscoveryTimer();

                /* For datagram socket */
                datagramSocket = new DatagramSocket();
                datagramSocket.setBroadcast(true);


//                receiverInetAddress = InetAddress.getByName("255.255.255.255");
                receiverInetAddress = InetAddress.getByName("192.168.0.173");
                sendBuffer = mCommand.getBytes();
                packetSend = new DatagramPacket(sendBuffer, sendBuffer.length,
                        receiverInetAddress, receiverPort);
                datagramSocket.send(packetSend);


                /* For multicast socket
                multicastSocket = new MulticastSocket();
                multicastSocket.setBroadcast(true);
                receiverInetAddress = InetAddress.getByName("224.2.2.2");
                sendBuffer = mCommand.getBytes();
                packetSend = new DatagramPacket(sendBuffer, sendBuffer.length,
                        receiverInetAddress, receiverPort);
                multicastSocket.send(packetSend);
                 End for multicast sockets */

                Thread.sleep(100);
                log("After sending...");

                // A timer will close the socket after a while which causes an exception and
                // breaking out of while loop if not already broken out if all rooms are found
                while (true) {
                    log("In while");
                    // Receiving answers
                    try {
                        packetReceived = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        /* For DatagramSocket */
                        datagramSocket.receive(packetReceived);

                        /* For MulticastSocket
                        multicastSocket.receive(packetReceived);
                        End MulticastSocket */

                        answer = new String(packetReceived.getData(), 0,
                                packetReceived.getLength());
                        log("Answer from receiver: " + answer);
                        answers.put(answer, "found");
                        if (answers.size() == registered.size()) {
                            log("Breaking out");
                            break;
                        }
                    } catch (Exception e) {
                        log("Exception in while...");
                        break;
                    }
                }

            }

        }catch (Exception e){
            log("Exception when socket is closed");
        }
        /* For DatagramSocket */
        datagramSocket.close();


        /* For MulticastSocket
        multicastSocket.close();
        For MulticastSocket */

        // Kolla hur många svar som kommit och jämför med hur många enheter som borde svarat.
        log("Kolla hur många svar som inkommit...");
    }

    private void populateRegistered(){
        registered.put("222", "room");
//        registered.put("333", "room");
    }

    private void setDeviceDiscoveryTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                /* For DatagramSocket */
                datagramSocket.close();


                /* For MulticastSocket
                multicastSocket.close();
                End MulticastSocket */
            }
        };
        new Timer().schedule(task, Command.DURATION);
    }


    public void log(String msg) {
        Log.d("CommandHandler", msg);
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
