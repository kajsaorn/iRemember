package com.iremember.master.iremembermaster;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.iremember.master.iremembermaster.Constants.Command;
import com.iremember.master.iremembermaster.Utils.PreferenceUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by KEJ on 2018-03-09.
 */

public class CommandHandler extends Thread {
    private DatagramSocket datagramSocket;
    private String mCommand;
    private Hashtable<String, String> answers = new Hashtable<String, String>();
    private Map<String, ?> knownSubscribers;
    private Context sContext;
    public CommandHandler(String command, Context context){
        mCommand = command;
        sContext = context;
        this.start();
    }

    @Override
    public void run() {
        log("Start of run");
        DatagramPacket packetSend;
        DatagramPacket packetReceived;
        InetAddress receiverInetAddress = null;
        int receiverPort = 0;
        byte[] sendBuffer = new byte[1024];
        String answer = null;
        byte[] receiveBuffer = new byte[1024];
        knownSubscribers = PreferenceUtils.getAllSubscribers(sContext);

        try{
            // Sending the packet. Trying a few times
            for (int i=0; i<10 && (answers.size() != knownSubscribers.size()); i++) {
                log("Try nbr: " + i);
                // Set up for sending meal command to receivers
                setDeviceDiscoveryTimer();

                /* For datagram socket */
                datagramSocket = new DatagramSocket();

                // Send meal command to every registered subscriber
                sendBuffer = mCommand.getBytes();
                for (Map.Entry<String, ?> subscriber : knownSubscribers.entrySet()) {
                    String[] value = ((String) subscriber.getValue()).split("$");
                    receiverInetAddress = InetAddress.getByName(value[0]);
                    receiverPort = Integer.parseInt(value[1]);
                    packetSend = new DatagramPacket(sendBuffer, sendBuffer.length,
                            receiverInetAddress, receiverPort);
                    datagramSocket.send(packetSend);
                }

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

                        // Store the room name of the answered subscriber, in answer.
                        answer = new String(packetReceived.getData(), 0,
                                packetReceived.getLength());
                        log("Answer from receiver: " + answer);
                        answers.put(answer, "found");
                        if (answers.size() == knownSubscribers.size()) {
                            log("Breaking out, all subscribers have got the message");
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
        if (datagramSocket != null) {
            datagramSocket.close();
        }


        // Kolla hur många svar som kommit och jämför med hur många enheter som borde svarat.
        log("Kolla hur många svar som inkommit...");
        if (answers.size() == knownSubscribers.size()) {
//            Toast.makeText(sContext, "Alla rum har fått besked om att maten är klar",
//                    Toast.LENGTH_SHORT).show();
            log("Alla subscribers har svarat");
        } else {
            findNonRespondedRooms();
        }

    }

    private void setDeviceDiscoveryTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                /* For DatagramSocket */
                datagramSocket.close();
            }
        };
        new Timer().schedule(task, Command.DURATION);
    }

    /**
     * Find the rooms that did not respond
     */
    private void findNonRespondedRooms() {
        String notResponded = "The following rooms did not respond:\n";
        for (Map.Entry<String, ?> subscriber : knownSubscribers.entrySet()) {
            String roomName = (String) subscriber.getKey();
            if (!answers.containsKey(roomName)) {
                notResponded = notResponded + roomName + "\n";
            }
        }
        log(notResponded);
    }

    public void log(String msg) {
        Log.d("CommandHandler", msg);
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
