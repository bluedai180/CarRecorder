package com.hipad.carrecorder;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
// daiqingchen modified for bug 140323 20170325 start
import android.os.Handler;
// daiqingchen modified for bug 140323 20170325 end
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;
// daiqingchen modified for bug 140382 20170325 start
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.content.Context;
// daiqingchen modified for bug 140382 20170325 end

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by HIPADUSER on 2017/2/15.
 */
public class UsbCameraFragment extends Fragment implements SurfaceHolder.Callback, MyRunnable {

    private final static String TAG = "bluedai_UsbFragment";

    private SurfaceView mSurface;
    private ImageButton mRecord;
    private Chronometer mTime;
    Thread mainLoop = null;

    private boolean mIsOpened = false;
    private boolean mIsPreview = false;
    private boolean mIsRecording = false;
    private boolean shouldStop = false;

    private int winWidth = 0;
    private int winHeight = 0;
    private Rect rect;
    private int dw, dh;
    private float rate;

    private static final int ImageWidth = 640;
    private static final int ImageHeight = 480;

    private Bitmap mBitmap = null;

    public boolean suspend = false;
    public String control = "";
    // daiqingchen modified for bug 140382 20170325 start
    private TelephonyManager telephonyManager;
    // daiqingchen modified for bug 140382 20170325 end


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.usb_fragment, container, false);
        mSurface = (SurfaceView) view.findViewById(R.id.usb_surface);
        mRecord = (ImageButton) view.findViewById(R.id.usb_record_btn);
        mTime = (Chronometer) view.findViewById(R.id.usb_time);
        SurfaceHolder holder = mSurface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecording) {
                    uninitRecord();
                    mTime.setVisibility(View.GONE);
                    mTime.stop();
                    mRecord.setImageResource(R.drawable.btn_shutter_video_default);
                } else {
                    mTime.setVisibility(View.VISIBLE);
                    mTime.setBase(SystemClock.elapsedRealtime());
                    mTime.start();
                    mRecord.setImageResource(R.drawable.btn_shutter_video_recording);
                    initRecord();
                }
            }
        });
        return view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
        // daiqingchen modified for bug 140382 20170325 start
        telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
        // daiqingchen modified for bug 140382 20170325 end
        mainLoop = new Thread(this);
        if (null != mainLoop) {
            setSuspend(true);
            mainLoop.start();
        } else {
            return;
        }
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(ImageWidth, ImageHeight, Bitmap.Config.ARGB_8888);
        }
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed: ");
        uninitPreview();
        uninitRecord();
        // daiqingchen modified for bug 140382 20170325 start
        telephonyManager.listen(new PhoneListener(), PhoneStateListener.LISTEN_NONE);
        // daiqingchen modified for bug 140382 20170325 end
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	if (mIsRecording) {
            uninitRecord();
            mTime.setVisibility(View.GONE);
            mTime.stop();
            mRecord.setImageResource(R.drawable.btn_shutter_video_default);
        }
    	uninitPreview();
        uninitRecord();
        telephonyManager.listen(new PhoneListener(), PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public void setSuspend(boolean susp) {
        if (false == susp) {
            synchronized (control) {
                control.notifyAll();
            }
        }
        suspend = susp;
    }

    @Override
    public boolean isSuspend() {
        return suspend;
    }

    @Override
    public void runPesonelLogic() {
        ImageProc.getPreviewFrame(mBitmap);

        updateRect(ImageWidth, ImageHeight);

        Canvas canvas = mSurface.getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawBitmap(mBitmap, null, rect, null);
            mSurface.getHolder().unlockCanvasAndPost(canvas);
        } else {
            Log.d(TAG, "canvas is null.");
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (control) {
                if (true == shouldStop) {
                    shouldStop = false;
                    Log.e(TAG, "run() stop...");
                    return;
                }

                if (suspend) {
                    try {
                        control.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            runPesonelLogic();
        }
    }

    public void initPreview() {
        if (false == mIsOpened) {
            if (0 == ImageProc.connectCamera()) {
                mIsOpened = true;
                mIsPreview = false;
                shouldStop = false;
                setSuspend(true);
                // daiqingchen modified for bug 138654 20170302 start
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.usb_camera_success), Toast.LENGTH_SHORT).show();
            } else {
                mIsOpened = false;
                mIsPreview = false;
                shouldStop = false;
                setSuspend(true);
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.usb_camera_failed), Toast.LENGTH_SHORT).show();
                // daiqingchen modified for bug 138654 20170302 end
            }
        }
    }

    public void uninitPreview() {
        if (mIsOpened) {
            if (mIsRecording) {
                uninitRecord();
            }

            if (!mIsPreview) {
                setSuspend(false);
            }

            shouldStop = true;
            // daiqingchen modified for bug 140323 20170325 start
//            while (shouldStop) ;
            Log.i(TAG, "start release camera...");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsPreview = false;
                    ImageProc.releaseCamera();
                    Log.i(TAG, "release camera...");
                }
            }, 150);
            // daiqingchen modified for bug 140323 20170325 end
            mIsOpened = false;
        }
    }

    public void startPreview() {
        if (false == mIsOpened) {
            initPreview();
        }

        if (true == mIsOpened) {
            if (false == mIsPreview) {
                setSuspend(false);
                mIsPreview = true;
            } else {
                if (mIsRecording) {
                    uninitRecord();
                }

                setSuspend(true);
                mIsPreview = false;
            }
        }
    }

    public void initRecord() {
        if (true == mIsPreview) {
            if (false == mIsRecording) {
                Log.i(TAG, "init camera record!");
                Date date = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String dateString = simpleDateFormat.format(date);
                Log.i(TAG, dateString);

                if (0 == ImageProc.startRecord("UsbCamera_" + dateString)) {
                    mIsRecording = true;
                } else {
                    mIsRecording = false;
                    Log.e(TAG, "init camera record failed!");
                    Toast.makeText(getActivity().getApplicationContext(), "Failed Recording!", Toast.LENGTH_SHORT).show();
                }
            } else {
                uninitRecord();
            }
        } else {
            Log.e(TAG, "camera has not new frame data!");
        }

        return;
    }

    public void uninitRecord() {
        if (true == mIsRecording) {
            Log.i(TAG, "camera is already recording! So we stop it.");
            ImageProc.stopRecord();
            mIsRecording = false;
            return;
        }
    }

    public boolean isOpen() {
        return mIsPreview;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void updateRect(int frame_w, int frame_h) {
        if (winWidth == 0) {
            winWidth = mSurface.getWidth();
            winHeight = mSurface.getHeight();

            if (winWidth * 3 / 4 <= winHeight) {
                dw = 0;
                dh = (winHeight - winWidth * 3 / 4) / 2;
                rate = ((float) winWidth) / frame_w;
                rect = new Rect(dw, dh, dw + winWidth - 1, dh + winWidth * 3 / 4 - 1);
            } else {
                dw = (winWidth - winHeight * 4 / 3) / 2;
                dh = 0;
                rate = ((float) winHeight) / frame_h;
                rect = new Rect(dw, dh, dw + winHeight * 4 / 3 - 1, dh + winHeight - 1);
            }
        }
    }
    // daiqingchen modified for bug 140382 20170325 start
    private class PhoneListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "onCallStateChanged: CALL_STATE_RINGING");
                    uninitRecord();
                    mTime.setVisibility(View.GONE);
                    mTime.stop();
                    mRecord.setImageResource(R.drawable.btn_shutter_video_default);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "onCallStateChanged: CALL_STATE_OFFHOOK");
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "onCallStateChanged: CALL_STATE_IDLE");
                    break;
            }
        }
    }
    // daiqingchen modified for bug 140382 20170325 end
}
