package com.hipad.carrecorder;

import android.app.Fragment;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by HIPADUSER on 2017/1/14.
 */
public class FrontCameraFragment extends Fragment implements SurfaceHolder.Callback, MainActivity.RecordBtnClickListener {
    private Button mFrontBtn;
    private SurfaceView mSurface;
    private Camera mCamera;
    private final static String TAG = "bluedai_FrontCamera";
    private boolean isRecording = false;
    private MediaRecorder mMediaRecorder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.front_fragment, container, false);
        //mFrontBtn = (Button) view.findViewById(R.id.front_btn);
        mSurface = (SurfaceView) view.findViewById(R.id.front_surface);
        SurfaceHolder holder = mSurface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);

        mSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
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
                } else {
                    Log.d(TAG, "onClick: null");
                }
            }
        });
        ViewTreeObserver vto = mSurface.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSurface.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Log.d(TAG, "onGlobalLayout: " + mSurface.getHeight() + mSurface.getWidth());
            }
        });
        return view;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Method m = Camera.class.getMethod("openLegacy", int.class, int.class);
            mCamera = (Camera) m.invoke(null, Integer.valueOf(1), 0x100);
            mCamera.setPreviewDisplay(holder);
            Camera.Parameters parameters = mCamera.getParameters();
            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                Log.d(TAG, "surfaceCreated: " + size.width + size.height);
            }
            parameters.setPreviewSize(480, 360);
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed: ");
        if (mCamera != null) {
            holder.removeCallback(this);
            mCamera.stopPreview();
            try {
                mCamera.setPreviewDisplay(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera.release();
            mCamera = null;
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.lock();
            try {
                mCamera.setPreviewDisplay(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onRecordBtnClick() {
        Log.d(TAG, "onRecordBtnClick: ");
    }

    private boolean prepareVideoRecorder(){
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        mMediaRecorder.setOutputFile(String.valueOf(getOutputMediaFile(1)));
        mMediaRecorder.setPreviewDisplay(mSurface.getHolder().getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
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
                    "FrontCamera_"+ timeStamp + ".mp4");
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
}
