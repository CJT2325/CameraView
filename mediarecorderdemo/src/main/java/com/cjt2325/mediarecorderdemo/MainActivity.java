package com.cjt2325.mediarecorderdemo;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {

    private VideoView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Button btn_start;
    private Button btn_stop;

    private MediaRecorder mediaRecorder;
    private Camera mCamera;
    private int width,height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        mSurfaceView = (VideoView) findViewById(R.id.srufaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_stop.setClickable(false);

        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                startRecord();
                break;
            case R.id.btn_stop:
                stopRecord();
                break;
        }
    }
    private void stopRecord() {
        if (mediaRecorder != null) {
            //停止录制
            mediaRecorder.stop();
            //释放资源
            mediaRecorder.release();
            mediaRecorder = null;
            releaseCamera();
            mSurfaceView.setVideoPath("/sdcard/love.mp4");
            mSurfaceView.start();
            mSurfaceView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    mp.setLooping(true);
                }
            });
            mSurfaceView
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mSurfaceView.setVideoPath("/sdcard/love.mp4");
                            mSurfaceView.start();
                        }
                    });
        }

        btn_start.setClickable(true);
        btn_stop.setClickable(false);
    }

    private void startRecord() {

        mediaRecorder = new MediaRecorder();
//        mCamera=getCamera();
        mCamera.unlock();
        // 设置录制视频源为Camera(相机)
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 设置录制的视频编码h263 h264
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder .AudioEncoder.AMR_NB);
        // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
        mediaRecorder.setVideoSize(width,height);
        // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setMaxDuration(1000);
        mediaRecorder.setVideoEncodingBitRate(5*1024*1024);
        mediaRecorder.setVideoFrameRate(20);
        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        // 设置视频文件输出的路径
        mediaRecorder.setOutputFile("/sdcard/love.mp4");

        try {
            //准备录制
            mediaRecorder.prepare();
            // 开始录制
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        btn_start.setClickable(false);
        btn_stop.setClickable(true);
    }


    //获取Camera
    private Camera getCamera() {
        Camera camera;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }

    //启动相机浏览
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);
            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();//获取所有支持的camera尺寸
            Camera.Size optionSize = getOptimalPreviewSize(sizeList, mSurfaceView.getWidth(), mSurfaceView.getHeight());//获取一个最为适配的camera.size
            width=optionSize.width;
            height=optionSize.height;
            Log.i("size","width : height " + width +" : "+height);
            parameters.setPreviewSize(optionSize.width, optionSize.height);//把camera.size赋值到parameters
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(parameters);

            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    //释放资源
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder=holder;
        setStartPreview(mCamera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder=holder;
        if (mCamera!=null) {
            mCamera.stopPreview();
            setStartPreview(mCamera, holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // surfaceDestroyed的时候同时对象设置为null
        mSurfaceHolder = null;
        mSurfaceView = null;
        mediaRecorder = null;
        releaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera=getCamera();
        setStartPreview(mCamera,mSurfaceHolder);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }
}
