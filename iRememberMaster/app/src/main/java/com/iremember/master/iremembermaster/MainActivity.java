package com.iremember.master.iremembermaster;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.iremember.master.iremembermaster.Constants.Command;
import com.iremember.master.iremembermaster.Constants.Protocol;
import com.iremember.master.iremembermaster.Services.NetworkService;
import com.iremember.master.iremembermaster.Utils.PreferenceUtils;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startMaster();
    }

    private void startMaster(){
        log("startMaster()");
        // If the master already has a name...
        String masterName = PreferenceUtils.readMasterName(this);
        //  Then start the service direct.
        if (masterName != null && !masterName.isEmpty()) {
            log("in if...");
            Intent startSettingsIntent = new Intent(this, SettingsActivity.class);
            startSettingsIntent.putExtra(Command.START_SERVICE_DIRECT, Command.START_DIRECT);
            startActivity(startSettingsIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
        setDozeMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        stopNetworkService();
    }

    /**
     * Stops the networkService
     */
    private void stopNetworkService() {
        log("stopNetworkService()");
        Intent stopNetworkServiceIntent = new Intent(this, NetworkService.class);
        stopService(stopNetworkServiceIntent);
    }


    public void onCoffeeClick(View view) {
        Intent netWorkServiceIntent = new Intent(this, NetworkService.class);
        netWorkServiceIntent.putExtra(Command.NETWORKSERVICE_COMMAND, Protocol.COMMAND_COFFEE +
                "$" + PreferenceUtils.readMasterName(this));
        startService(netWorkServiceIntent);
        Toast.makeText(this, R.string.btn_coffee, Toast.LENGTH_SHORT).show();
    }

    public void onLunchClick(View view) {
        Intent netWorkServiceIntent = new Intent(this, NetworkService.class);
        netWorkServiceIntent.putExtra(Command.NETWORKSERVICE_COMMAND, Protocol.COMMAND_MIDDAY +
                "$" + PreferenceUtils.readMasterName(this));
        startService(netWorkServiceIntent);
        Toast.makeText(this, R.string.btn_lunch, Toast.LENGTH_SHORT).show();
    }

    public void onDinnerClick(View view) {
        Intent netWorkServiceIntent = new Intent(this, NetworkService.class);
        netWorkServiceIntent.putExtra(Command.NETWORKSERVICE_COMMAND, Protocol.COMMAND_SUPPER +
                "$" + PreferenceUtils.readMasterName(this));
        startService(netWorkServiceIntent);
        Toast.makeText(this, R.string.btn_dinner, Toast.LENGTH_SHORT).show();
    }

    public void onSettingsClick(View view) {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * Ask user for permission to use unoptimized battery settings.
     */
    private void setDozeMode() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    public void log(String msg) {
        Log.d("MainActivity", msg);
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}