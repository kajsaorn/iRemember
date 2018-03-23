package com.iremember.master.iremembermaster;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.iremember.master.iremembermaster.Constants.Command;
import com.iremember.master.iremembermaster.Utils.PreferenceUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
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
        log("command = " + command);
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
        log("knownSubscribers: " + knownSubscribers.size());

            // Sending the packet. Trying a few times
            for (int i=0; i<5 && (answers.size() != knownSubscribers.size()); i++) {
                log("Try nbr: " + i);
                try{
                    /* For datagram socket */
                    datagramSocket = new DatagramSocket();

                    // Send meal command to every registered subscriber
                    sendBuffer = mCommand.getBytes();
                    for (Map.Entry<String, ?> subscriber : knownSubscribers.entrySet()) {
                        log("for()...");
                        String[] value = ((String) subscriber.getValue()).split("\\$");
                        receiverInetAddress = InetAddress.getByName(value[0]);
                        log("sent to value[0], ip = " + value[0]);
                        receiverPort = Integer.parseInt(value[1]);
                        log("send to value[1], port = " + value[1]);
                        packetSend = new DatagramPacket(sendBuffer, sendBuffer.length,
                            receiverInetAddress, receiverPort);
                        datagramSocket.send(packetSend);
                    }

                    log("After sending...");
                    // A timer will close the socket after a while which causes an exception and
                    // breaking out of while loop if not already broken out if all rooms are found
                    setAnswerTimer();

                    while (true) {
                        log("In while");

                        // Receiving answers
                        try {
                            packetReceived = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                            /* For DatagramSocket */
                            log("before receive()");
                            datagramSocket.receive(packetReceived);
                            log("after receive()");

                            // Store the room name of the answered subscriber, in answer.
                            answer = new String(packetReceived.getData(), 0,
                                packetReceived.getLength());
                            log("Answer from receiver: " + answer);
                            String[] splitedMessage = answer.split("\\$");
                            answers.put(splitedMessage[1], "answered");
                            if (answers.size() == knownSubscribers.size()) {
                                log("Breaking out, all subscribers have got the message");
                                break;
                            }
                        } catch (Exception e) {
                            log("Exception in while...");
                            e.printStackTrace();
                            break;
                        }
                    }
                    log("has left while()");

                    }catch (Exception e){
                        log("Exception when socket is closed");
                        e.printStackTrace();
                }
            } // end for()

        /* For DatagramSocket */
        if (datagramSocket != null) {
            datagramSocket.close();
        }


        // Kolla hur många svar som kommit och jämför med hur många enheter som borde svarat.
        log("Kolla hur många svar som inkommit...");
        log("answers.size() = " + answers.size());
        log("knownSubscribers.size() = " + knownSubscribers.size());
/*        if (answers.size() == knownSubscribers.size()) {
//            Toast.makeText(sContext, "Alla rum har fått besked om att maten är klar",
//                    Toast.LENGTH_SHORT).show();
            log("Alla subscribers har svarat");
        } else {
            log("not everyone has answered...");
            findNonRespondedRooms();
        }
        */
        showResults();

    }

    private void setAnswerTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                /* For DatagramSocket */
                datagramSocket.close();
            }
        };
        new Timer().schedule(task, Command.DURATION);
    }

    /**
     * Displays the result from subscribers answers
     */
    private void showResults() {
        String[] noRespond = findNonRespondedRooms();
        Intent answerIntent = new Intent(sContext, AnswersActivity.class);
        String[] responds = getRespondedRooms();
        answerIntent.putExtra(Command.ANSWERS, responds);
        answerIntent.putExtra(Command.NO_ANSWERS, noRespond);
        answerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sContext.startActivity(answerIntent);
    }

    /**
     * Find the rooms that did not respond
     */
    private String[] findNonRespondedRooms() {
        log("findNonRespondedRooms");
        log("alla som svarat: ");

        LinkedList<String> lstNoRespond = new LinkedList<String>();
        for (String roomName : knownSubscribers.keySet()) {
            if (!answers.containsKey(roomName)) {
                log("roomName: " + roomName + " finns inte i answers");
                lstNoRespond.addLast(roomName);
            }
        }

        log("storlek på lstNoRespond = " + lstNoRespond.size());
        if (lstNoRespond.size() == 0) {
            log("den är 0");
            return new String[0];
        }
        log("innan return");
        String[] arrNoRespond = new String[lstNoRespond.size()];
        for (int i = 0; i < arrNoRespond.length; i++) {
            arrNoRespond[i] = lstNoRespond.get(i);
        }
        return arrNoRespond;
    }

    private String[] getRespondedRooms() {
        if (answers.size() == 0) {
            return new String[0];
        }
//        String[] arrRespond = new String[answers.size()];

        return answers.keySet().toArray(new String[0]);
    }

    public void log(String msg) {
        Log.d("CommandHandler", msg);
        //Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
