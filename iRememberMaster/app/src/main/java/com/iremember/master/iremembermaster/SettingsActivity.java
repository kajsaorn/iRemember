package com.iremember.master.iremembermaster;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iremember.master.iremembermaster.Constants.Command;
import com.iremember.master.iremembermaster.Services.NetworkService;
import com.iremember.master.iremembermaster.Utils.PreferenceUtils;

public class SettingsActivity extends AppCompatActivity {
    private EditText mEtMasterName;
    private boolean networkServiceIsRunning = false;
    Button btnStartMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mEtMasterName = (EditText) findViewById(R.id.et_master_name);
        mEtMasterName.setText(PreferenceUtils.readMasterName(this));
        btnStartMaster = findViewById(R.id.btn_start_master);
        if (isServiceRunning(NetworkService.class)) {
            btnStartMaster.setText(R.string.btn_stop_master);
        } else {
            btnStartMaster.setText(R.string.btn_start_master);
        }
    }

    /**
     * Save the name of the master
     * @param view
     */
    public void onStoreClick(View view) {
        String mMasterName = mEtMasterName.getText().toString();

        if ((mMasterName != null) && (!mMasterName.equals(""))) {
            PreferenceUtils.writeMasterName(this, mMasterName);
        } else {
            showUserMessage(getString(R.string.toast_mastername_invalid));
            return;
        }
    }

    /**
     * Display message to user as Android Toast.
     */
    private void showUserMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Start service for making the master discoverable
     * @param view
     */
    public void onStartClick(View view) {
        if (!networkServiceIsRunning) {
            startNetworkService();
        } else {
            stopNetworkService();
        }
    }

    /**
     * Starts the networkService
     */
    private void startNetworkService() {
        log("startNetworkService()");
        Intent netWorkServiceIntent = new Intent(this, NetworkService.class);
        netWorkServiceIntent.putExtra(Command.NETWORKSERVICE_COMMAND, Command.REGISTER_COMMAND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(netWorkServiceIntent);
            updateBtnAndServiceStatus(R.string.btn_stop_master, true);
        } else {
            startService(netWorkServiceIntent);
            updateBtnAndServiceStatus(R.string.btn_stop_master, true);
        }
    }

    /**
     * Stops the networkService
     */
    private void stopNetworkService() {
        log("stopNetworkService()");
        Intent stopNetworkServiceIntent = new Intent(this, NetworkService.class);
        stopService(stopNetworkServiceIntent);
        updateBtnAndServiceStatus(R.string.btn_start_master, false);
    }

    private void updateBtnAndServiceStatus(int btnText, boolean runStatus) {
        btnStartMaster.setText(btnText);
        networkServiceIsRunning = runStatus;
        PreferenceUtils.writeNetworkServiceRunState(this, runStatus);
    }

    public void onGoToMenuClick(View view) {
        finish();
    }

    /**
     * Check if an Android service is running.
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Display message to user as Android Toast.
     */
    private void log(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", message);
    }

    public void onRegSubClick(View view) {
        Intent intent = new Intent(this, RegisteredSubscribersActivity.class);
        startActivity(intent);
    }
}
