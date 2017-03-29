package com.cjt2325.cameralibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * 445263848@qq.com.
 */
public class JCameraView extends RelativeLayout implements SurfaceHolder.Callback, Camera.AutoFocusCallback, CameraFocusListener {

    public final String TAG = "JCameraView";

    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;
    private Context mContext;
    private VideoView mVideoView;
    private ImageView mImageView;
    private FoucsView mFoucsView;
    private CaptureButton mCaptureButtom;

    private int iconWidth = 0;
    private int iconMargin = 0;
    private int iconSrc = 0;

    private String saveVideoPath = "";
    private String videoFileName = "";


    private MediaRecorder mediaRecorder;
    private SurfaceHolder mHolder = null;
    private Camera mCamera;
    private Camera.Parameters mParam;
    private int previewWidth;
    private int previewHeight;
    private int pictureWidth;
    private int pictureHeight;

    private boolean autoFoucs;
    private boolean isPlay = false;
    private boolean isRecorder = false;
    private float screenProp;

    private String fileName;
    private Bitmap pictureBitmap;


    private int SELECTED_CAMERA = -1;
    private int CAMERA_POST_POSITION = -1;
    private int CAMERA_FRONT_POSITION = -1;

    private CameraViewListener cameraViewListener;

    public void setCameraViewListener(CameraViewListener cameraViewListener) {
        this.cameraViewListener = cameraViewListener;
    }

    public JCameraView(Context context) {
        this(context, null);
    }

    public JCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        powerManager = (PowerManager) mContext.getSystemService(mContext.POWER_SERVICE);
        wakeLock = this.powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
        findAvailableCameras();
        SELECTED_CAMERA = CAMERA_POST_POSITION;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.JCameraView, defStyleAttr, 0);

        iconWidth = a.getDimensionPixelSize(R.styleable.JCameraView_iconWidth, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 35, getResources().getDisplayMetrics()));
        iconMargin = a.getDimensionPixelSize(R.styleable.JCameraView_iconMargin, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics()));
        iconSrc = a.getResourceId(R.styleable.JCameraView_iconSrc, R.drawable.ic_repeat_black_24dp);

        initView();
        mHolder = mVideoView.getHolder();
        mHolder.addCallback(this);
        mCaptureButtom.setCaptureListener(new CaptureButton.CaptureListener() {
            @Override
            public void capture() {
                JCameraView.this.capture();
            }

            @Override
            public void cancel() {
//                photoImageView.setVisibility(INVISIBLE);
                mImageView.setVisibility(VISIBLE);
                releaseCamera();
                mCamera = getCamera(SELECTED_CAMERA);
                setStartPreview(mCamera, mHolder);
            }

            @Override
            public void determine() {

                if (cameraViewListener != null) {
//                    FileUtil.saveBitmap(pictureBitmap);
                    cameraViewListener.captureSuccess(pictureBitmap);
                }
//                photoImageView.setVisibility(INVISIBLE);
                mImageView.setVisibility(VISIBLE);
                releaseCamera();
                mCamera = getCamera(SELECTED_CAMERA);
                setStartPreview(mCamera, mHolder);
            }

            @Override
            public void quit() {
                if (cameraViewListener != null) {
                    cameraViewListener.quit();
                }
            }

            @Override
            public void record() {
                startRecord();
            }

            @Override
            public void rencodEnd() {
                stopRecord();
            }

            @Override
            public void getRecordResult() {
                if (cameraViewListener != null) {
                    cameraViewListener.recordSuccess(fileName);
                }
                mVideoView.stopPlayback();
                releaseCamera();
                mCamera = getCamera(SELECTED_CAMERA);
                setStartPreview(mCamera, mHolder);
                isPlay = false;
            }

            @Override
            public void deleteRecordResult() {

                File file = new File(fileName);
                if (file.exists()) {
                    file.delete();
                }
                mVideoView.stopPlayback();
                releaseCamera();
                mCamera = getCamera(SELECTED_CAMERA);
                setStartPreview(mCamera, mHolder);
                isPlay = false;
            }

            @Override
            public void scale(float scaleValue) {
                if (scaleValue >= 0) {
                    int scaleRate = (int) (scaleValue / 50);

                    if (scaleRate < 10 && scaleRate >= 0 && mParam != null && mCamera != null && mParam.isSmoothZoomSupported()) {
                        mParam = mCamera.getParameters();
                        mParam.setZoom(scaleRate);
                        mCamera.setParameters(mParam);
                    }
//                    Log.i(TAG, "scaleValue = " + (int) scaleValue + " = scaleRate" + scaleRate);
                }
            }
        });

    }


    private void initView() {
        setWillNotDraw(false);
        this.setBackgroundColor(Color.BLACK);
        /*
        Surface
         */
        mVideoView = new VideoView(mContext);
        LayoutParams videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        videoViewParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mVideoView.setLayoutParams(videoViewParam);
        /*
        CaptureButtom
         */
        LayoutParams btnParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mCaptureButtom = new CaptureButton(mContext);
        mCaptureButtom.setLayoutParams(btnParams);


        mImageView = new ImageView(mContext);
        Log.i("CJT", this.getMeasuredWidth() + " ==================================");
        LayoutParams imageViewParam = new LayoutParams(iconWidth, iconWidth);
        imageViewParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        imageViewParam.setMargins(0, iconMargin, iconMargin, 0);
        mImageView.setLayoutParams(imageViewParam);
        mImageView.setImageResource(iconSrc);
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera != null) {
                    releaseCamera();
                    if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
                        SELECTED_CAMERA = CAMERA_FRONT_POSITION;
                    } else {
                        SELECTED_CAMERA = CAMERA_POST_POSITION;
                    }
                    mCamera = getCamera(SELECTED_CAMERA);
                    previewWidth = previewHeight = 0;
                    pictureWidth = pictureHeight = 0;
                    setStartPreview(mCamera, mHolder);
                }
            }
        });


        mFoucsView = new FoucsView(mContext, 120);
        mFoucsView.setVisibility(INVISIBLE);
        this.addView(mVideoView);
        this.addView(mCaptureButtom);
        this.addView(mImageView);
        this.addView(mFoucsView);


        mVideoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(JCameraView.this);
                Log.i(TAG, "Touch To Focus");
            }
        });

        //初始化为自动对焦
        autoFoucs = true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    //获取Camera
    private Camera getCamera(int position) {
        Camera camera;
        try {
            camera = Camera.open(position);
        } catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }

    public void btnReturn() {
        setStartPreview(mCamera, mHolder);
    }


    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        if (camera == null) {
            Log.i(TAG, "Camera is null");
            return;
        }
        try {
            mParam = camera.getParameters();
//
            Camera.Size previewSize = CameraParamUtil.getInstance().getPreviewSize(mParam.getSupportedPreviewSizes(), 1000, screenProp);
            Camera.Size pictureSize = CameraParamUtil.getInstance().getPictureSize(mParam.getSupportedPictureSizes(), 1200, screenProp);

            mParam.setPreviewSize(previewSize.width, previewSize.height);
            mParam.setPictureSize(pictureSize.width, pictureSize.height);

            if (CameraParamUtil.getInstance().isSupportedFocusMode(mParam.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_AUTO)) {
                mParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            if (CameraParamUtil.getInstance().isSupportedPictureFormats(mParam.getSupportedPictureFormats(), ImageFormat.JPEG)) {
                mParam.setPictureFormat(ImageFormat.JPEG);
                mParam.setJpegQuality(100);
            }
            camera.setParameters(mParam);
            mParam = camera.getParameters();
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    public void capture() {
        if (autoFoucs) {
            mCamera.autoFocus(this);
        } else {
            if (SELECTED_CAMERA == CAMERA_POST_POSITION) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        pictureBitmap = bitmap;
                        mImageView.setVisibility(INVISIBLE);
                        mCaptureButtom.captureSuccess();
                    }
                });
            } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(270);
                        matrix.postScale(-1, 1);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        pictureBitmap = bitmap;
                        mImageView.setVisibility(INVISIBLE);
                        mCaptureButtom.captureSuccess();
                    }
                });
            }
        }
    }

    //自动对焦
    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (autoFoucs) {
            if (SELECTED_CAMERA == CAMERA_POST_POSITION && success) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(90);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        pictureBitmap = bitmap;
                        mImageView.setVisibility(INVISIBLE);
                        mCaptureButtom.captureSuccess();
                    }
                });
            } else if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        matrix.setRotate(270);
                        matrix.postScale(-1, 1);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        pictureBitmap = bitmap;
                        mImageView.setVisibility(INVISIBLE);
                        mCaptureButtom.captureSuccess();
                    }
                });
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = MeasureSpec.getSize(widthMeasureSpec);
        float heightSize = MeasureSpec.getSize(heightMeasureSpec);
        screenProp = heightSize / widthSize;
        Log.i(TAG, "ScreenProp = " + screenProp + " " + widthSize + " " + heightSize);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, holder);
        Log.i("Camera", "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
        Log.i("Camera", "surfaceDestroyed");
    }

    public void onResume() {
        mCamera = getCamera(SELECTED_CAMERA);
        if (mCamera != null) {
//            setStartPreview(mCamera, mHolder);
        } else {
            Log.i(TAG, "Camera is null!");
        }
        wakeLock.acquire();
    }

    public void onPause() {
        releaseCamera();
        wakeLock.release();
    }


    private void startRecord() {
        if (isRecorder) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        if (mCamera == null) {
            Log.i(TAG, "Camera is null");
            stopRecord();
            return;
        }
        mCamera.unlock();
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
        mediaRecorder.reset();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        if (mParam == null) {
            mParam = mCamera.getParameters();
        }
        Camera.Size videoSize = CameraParamUtil.getInstance().getPictureSize(mParam.getSupportedVideoSizes(), 1000, screenProp);

        mediaRecorder.setVideoSize(videoSize.width, videoSize.height);
        if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
            mediaRecorder.setOrientationHint(270);
        } else {
            mediaRecorder.setOrientationHint(90);
        }
        mediaRecorder.setMaxDuration(10000);
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mediaRecorder.setPreviewDisplay(mHolder.getSurface());

        videoFileName = "video_" + System.currentTimeMillis() + ".mp4";
        if (saveVideoPath.equals("")) {
            saveVideoPath = Environment.getExternalStorageDirectory().getPath();
        }
        mediaRecorder.setOutputFile(saveVideoPath + "/" + videoFileName);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecorder = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        if (mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            try {
                mediaRecorder.stop();
                isRecorder = false;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaRecorder.release();
            mediaRecorder = null;
            releaseCamera();
            fileName = saveVideoPath + "/" + videoFileName;
            mVideoView.setVideoPath(fileName);
            mVideoView.start();
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    isPlay = true;
                    mp.start();
                    mp.setLooping(true);
                }
            });
            mVideoView
                    .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mVideoView.setVideoPath(fileName);
                            mVideoView.start();
                        }
                    });
        }
    }


    public void setSaveVideoPath(String saveVideoPath) {
        this.saveVideoPath = saveVideoPath;
    }

    /**
     * 获得可用的相机，并设置前后摄像机的ID
     */
    private void findAvailableCameras() {

        Camera.CameraInfo info = new Camera.CameraInfo();
        int numCamera = Camera.getNumberOfCameras();
        for (int i = 0; i < numCamera; i++) {
            Camera.getCameraInfo(i, info);
            // 找到了前置摄像头
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                CAMERA_FRONT_POSITION = info.facing;
                Log.i(TAG, "POSITION = " + CAMERA_FRONT_POSITION);
            }
            // 找到了后置摄像头
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                CAMERA_POST_POSITION = info.facing;
                Log.i(TAG, "POSITION = " + CAMERA_POST_POSITION);
            }
        }
    }


    public void setAutoFoucs(boolean autoFoucs) {
        this.autoFoucs = autoFoucs;
    }

    @Override
    public void onFocusBegin(float x, float y) {
        mFoucsView.setVisibility(VISIBLE);
        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mCamera.cancelAutoFocus();
                    onFocusEnd();
                }
            }
        });
    }

    //手动对焦结束
    @Override
    public void onFocusEnd() {
        mFoucsView.setVisibility(INVISIBLE);
    }

    public interface CameraViewListener {
        public void quit();

        public void captureSuccess(Bitmap bitmap);

        public void recordSuccess(String url);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!autoFoucs && event.getAction() == MotionEvent.ACTION_DOWN && SELECTED_CAMERA == CAMERA_POST_POSITION && !isPlay) {
            onFocusBegin(event.getX(), event.getY());
        }
        return super.onTouchEvent(event);
    }

    public void cancelAudio(){
        AudioUtil.setAudioManage(mContext);
    }
}
