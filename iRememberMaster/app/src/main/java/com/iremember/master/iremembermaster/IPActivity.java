package com.iremember.master.iremembermaster;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class IPActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);
 //       showIP();
        wifiIpAddress(getApplicationContext());
    }

    public void onToSettingsClick(View view) {
        finish();
    }

    private void wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
//            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
        }

        TextView mTv = (TextView) findViewById(R.id.tv_ip);
        mTv.setText(ipAddressString);
    }
    private void showIP() {
//        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        String ip = wm.getConnectionInfo().getIpAddress();

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipAddress = BigInteger.valueOf(wm.getDhcpInfo().netmask).toString();
        TextView mTv = (TextView) findViewById(R.id.tv_ip);
        mTv.setText(ipAddress);
    }
}
