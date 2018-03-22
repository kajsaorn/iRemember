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
import android.widget.Toast;

import com.iremember.master.iremembermaster.Constants.Command;
import com.iremember.master.iremembermaster.Constants.Protocol;
import com.iremember.master.iremembermaster.Services.NetworkService;
import com.iremember.master.iremembermaster.Utils.PreferenceUtils;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setContentView(R.layout.activity_main);
        setDozeMode();
    }

    public void onCoffeeClick(View view) {
        Intent netWorkServiceIntent = new Intent(this, NetworkService.class);
        netWorkServiceIntent.putExtra(Command.NETWORKSERVICE_COMMAND, Protocol.COMMAND_COFFEE +
                "\\$" + PreferenceUtils.readMasterName(this));
        startService(netWorkServiceIntent);
    }

    public void onLunchClick(View view) {
        Intent netWorkServiceIntent = new Intent(this, NetworkService.class);
        netWorkServiceIntent.putExtra(Command.NETWORKSERVICE_COMMAND, Protocol.COMMAND_MIDDAY +
                "\\$" + PreferenceUtils.readMasterName(this));
        startService(netWorkServiceIntent);
    }

    public void onDinnerClick(View view) {
        Intent netWorkServiceIntent = new Intent(this, NetworkService.class);
        netWorkServiceIntent.putExtra(Command.NETWORKSERVICE_COMMAND, Protocol.COMMAND_SUPPER +
                "\\$" + PreferenceUtils.readMasterName(this));
        startService(netWorkServiceIntent);
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
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}