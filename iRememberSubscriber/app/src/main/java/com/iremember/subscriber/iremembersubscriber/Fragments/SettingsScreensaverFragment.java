package com.iremember.subscriber.iremembersubscriber.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iremember.subscriber.iremembersubscriber.R;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingsScreensaverFragment extends Fragment {

    View mContent;
    private final int REQUEST_CODE = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContent = inflater.inflate(R.layout.fragment_settings_screensaver, container, false);
        initListeners();
        return mContent;
    }

    private void initListeners() {
        mContent.findViewById(R.id.btn_upload_screensaver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askPermissionWriteReadStorage();
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), REQUEST_CODE);
            }
        });
    }

    private void askPermissionWriteReadStorage() {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return;
        }

        int permissionWrite = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int permissionRead = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);

        boolean hasPermission = (permissionWrite == PackageManager.PERMISSION_GRANTED)
                && (permissionRead == PackageManager.PERMISSION_GRANTED);

        String[] permissions = { Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE };

        if (!hasPermission) {
            ActivityCompat.requestPermissions(getActivity(), permissions, 101);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri uri = data.getData();
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};

            Cursor cursor = getContext().getContentResolver().query(uri, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();

            Log.d("Screensaver", "File path: " + imagePath);
            PreferenceUtils.writeScreensaverPath(getContext(), imagePath);

        }
    }

    private void saveImage(Bitmap image) {


        FileOutputStream out = null;
        try {
            out = new FileOutputStream(image.toString());
            image.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}
