package com.hipad.carrecorder;

import java.io.File;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.support.v4.app.ActivityCompat;



public class MainActivity extends Activity {

	private final static int MY_PERMISSION_CAMERA = 1;
    private final static String TAG = "bluedai";
    private Button mBack;
//    private Button mFront;
    private Button mReturn;
//    private Button mFrontReturn;
    private Button mRecord;
    private FrameLayout mBackLayout;
//    private FrameLayout mFrontLayout;
    private Button mUsb;
    private Button mUsbReturn;
    private FrameLayout mUsbLayout;
    /**
     * the value of open which cameras.
     * back_camera is 0.
     * front_camera is 1.
     */
    public static int CAM_USE = 0;

    public interface RecordBtnClickListener {
        void onRecordBtnClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // daiqingchen modified for bug 140382 20170325 start
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE},
                MY_PERMISSION_CAMERA);
        // daiqingchen modified for bug 140382 20170325 end
        // initFragment();
        mBack = (Button) findViewById(R.id.btn_back);
//        mFront = (Button) findViewById(R.id.btn_front);
        mReturn = (Button) findViewById(R.id.btn_return);
//        mFrontReturn = (Button) findViewById(R.id.btn_front_return);
//        mRecord = (Button) findViewById(R.id.btn_record);
        mBackLayout = (FrameLayout) findViewById(R.id.back_layout);
//        mFrontLayout = (FrameLayout) findViewById(R.id.front_layout);
        mUsb = (Button) findViewById(R.id.btn_usb);
        mUsbReturn = (Button) findViewById(R.id.btn_usb_return);
        mUsbLayout = (FrameLayout) findViewById(R.id.usb_layout);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: back");
                mBackLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0));
                mBack.setVisibility(View.GONE);
                mReturn.setVisibility(View.VISIBLE);
            }
        });
//        mFront.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: front");
//                mFrontLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0));
//                mFront.setVisibility(View.GONE);
//                mFrontReturn.setVisibility(View.VISIBLE);
//            }
//        });
        mUsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsbLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0));
                mUsb.setVisibility(View.GONE);
                mUsbReturn.setVisibility(View.VISIBLE);
            }
        });
        mReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBack.setVisibility(View.VISIBLE);
                mReturn.setVisibility(View.GONE);
                mBackLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            }
        });
//        mFrontReturn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mFront.setVisibility(View.VISIBLE);
//                mFrontReturn.setVisibility(View.GONE);
//                mFrontLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
//            }
//        });
        mUsbReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsb.setVisibility(View.VISIBLE);
                mUsbReturn.setVisibility(View.GONE);
                mUsbLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            }
        });
//        mRecord.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                ((RecordBtnClickListener) new BackCameraFragment()).onRecordBtnClick();
////                ((RecordBtnClickListener) new FrontCameraFragment()).onRecordBtnClick();
//            }
//        });
        
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CarRecorder");
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                	initFragment();
                }
        }
    }

    private void initFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        BackCameraFragment backFragment = new BackCameraFragment();
//        FrontCameraFragment frontFragment = new FrontCameraFragment();
        UsbCameraFragment usbFragment = new UsbCameraFragment();
        transaction.add(R.id.back_camera, backFragment);

        // transaction.add(R.id.front_camera, frontFragment);
        transaction.add(R.id.usb_camera, usbFragment);
        transaction.commitAllowingStateLoss();
    }
}
