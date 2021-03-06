package com.hipad.carrecorder;

import android.app.Fragment;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
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
// daiqingchen modified for bug 139232 20170316 start
import android.os.Handler;
// daiqingchen modified for bug 139232 20170316 end

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by HIPADUSER on 2017/1/14.
 */
public class BackCameraFragment extends Fragment implements SurfaceHolder.Callback, MainActivity.RecordBtnClickListener{
    private ImageButton mRecord;
    private Chronometer mTime;
    private SurfaceView mSurface;
    private Camera mCamera;
    private final static String TAG = "bluedai_BackCamera";
    private boolean isRecording = false;
    private MediaRecorder mMediaRecorder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.back_fragment, container, false);
        mSurface = (SurfaceView) view.findViewById(R.id.back_surface);
        mRecord = (ImageButton) view.findViewById(R.id.back_record_btn);
        mTime = (Chronometer) view.findViewById(R.id.back_time);
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    if (!isRecording) {
                        if (prepareVideoRecorder()) {
                            mTime.setVisibility(View.VISIBLE);
                            mTime.setBase(SystemClock.elapsedRealtime());
                            mTime.start();
                            mRecord.setImageResource(R.drawable.btn_shutter_video_recording);
                            // daiqingchen modified for bug 139232 20170316 start
                            isRecording = true;
                            try {
                                mMediaRecorder.start();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            new Handler().postDelayed(new Runnable(){
                                public void run() {
                                    mRecord.setClickable(true);
                                }
                            }, 1500);
                            // daiqingchen modified for bug 139232 20170316 end
                        } else {
                            releaseMediaRecorder();
                        }
                    } else {
                        mTime.setVisibility(View.GONE);
                        mTime.stop();
                        mRecord.setImageResource(R.drawable.btn_shutter_video_default);
                        // daiqingchen modified for bug 139225 20170316 start
                        try {
                            mMediaRecorder.stop();
                        } catch (RuntimeException e) {
                            Log.d(TAG, "RuntimeException MediaRecorder Stop: " + e.getMessage());
                        }
                        // daiqingchen modified for bug 139225 20170316 end
                        releaseMediaRecorder();
                        mCamera.lock();
                        isRecording = false;
                    }
                } else {
                    Log.d(TAG, "onClick: mcamera == null");
                }
            }
        });
        SurfaceHolder surfaceHolder = mSurface.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(this);
//        ViewTreeObserver vto = mSurface.getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                mSurface.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                Log.d(TAG, "onGlobalLayout: " + mSurface.getWidth() + mSurface.getHeight());
//            }
//        });
        Log.d(TAG, "onCreateView: ");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        if (mCamera != null) {
        	if (isRecording) {
        		pauseRecording();
			}
            mCamera.stopPreview();
            try {
                mCamera.setPreviewDisplay(null);
            } catch (Exception e) {
                Log.d(TAG, "surfaceDestroyed: " + e.toString());
            }
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
        try {
//            Method m = Camera.class.getMethod("openLegacy", int.class, int.class);
//            mCamera = (Camera) m.invoke(null, Integer.valueOf(0), 0x100);
            // daiqingchen modified for bug 138914 20170316 start
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mCamera = Camera.open(0);
                }
            }).run();
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                Camera.Parameters parameters = mCamera.getParameters();
                for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                    Log.d(TAG, "surfaceCreated: " + size.width + size.height);
                }
                parameters.setPreviewSize(480, 360);
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            }
            // daiqingchen modified for bug 138914 20170316 end
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        if (mSupport == 0) {
//            Log.d(TAG, "surfaceDestroyed: ");
//            if (mCamera != null) {
//                holder.removeCallback(this);
//                mCamera.stopPreview();
//
//                try {
//                    mCamera.setPreviewDisplay(null);
//                } catch (Exception e) {
//                    Log.d(TAG, "surfaceDestroyed: " + e.toString());
//                }
//                mCamera.release();
//                mCamera = null;
//            }
//        }
    }

    private Camera.Size getSupportSize(List<Camera.Size> sizes) {
        Log.d(TAG, "getSupportSize: ");
        Camera.Size largestSize = sizes.get(0);
        int largestArea = sizes.get(0).height * sizes.get(0).width;
        for (Camera.Size s : sizes) {
            int area = s.height * s.width;
            if (area > largestArea) {
                largestArea = area;
                largestSize = s;
            }
        }
        return largestSize;
    }

    @Override
    public void onRecordBtnClick() {
        Log.d(TAG, "onRecordBtnClick: ");
        if (!isRecording) {
            if (prepareVideoRecorder()) {
                mMediaRecorder.start();
                isRecording = true;
            } else {
                releaseMediaRecorder();
            }
        } else {
            mMediaRecorder.stop();
            releaseMediaRecorder();
            mCamera.lock();
            isRecording = false;
        }
    }

    private boolean prepareVideoRecorder(){
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();

//        try {
//            Method m = Camera.class.getMethod("openLegacy", int.class, int.class);
//            mCamera = (Camera) m.invoke(null, Integer.valueOf(0), 0x100);
//        } catch (Exception ex) {
//
//        }

        // daiqingchen modified for bug 139233 20170316 end
//        //zhaohaiyun modify for bug138906 2017/03/11 start
//        try {
//            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
//        } catch (RuntimeException ex) {
//            Log.d(TAG, "setAudioSource RuntimeException");
//        }
//        //zhaohaiyun modify for bug138906 2017/03/11 end

        try {
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
            mMediaRecorder.setOutputFile(String.valueOf(getOutputMediaFile(1)));
            Log.d(TAG, "prepareVideoRecorder: " + String.valueOf(getOutputMediaFile(1)));
            mMediaRecorder.setPreviewDisplay(mSurface.getHolder().getSurface());
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (RuntimeException e) {
            Log.d(TAG, "RuntimeException unlock Camera: " + e.getMessage());
            return false;
        }
        // daiqingchen modified for bug 139233 20170316 end
        return true;
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Carcorder");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if(type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "BackCamera_"+ timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }
    
    private void pauseRecording() {
    	mTime.setVisibility(View.GONE);
        mTime.stop();
        mRecord.setImageResource(R.drawable.btn_shutter_video_default);
        // daiqingchen modified for bug 139225 20170316 start
        try {
            mMediaRecorder.stop();
        } catch (RuntimeException e) {
            Log.d(TAG, "RuntimeException MediaRecorder Stop: " + e.getMessage());
        }
        // daiqingchen modified for bug 139225 20170316 end
        releaseMediaRecorder();
        mCamera.lock();
        isRecording = false;
	}
}
