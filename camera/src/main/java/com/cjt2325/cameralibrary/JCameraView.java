package com.cjt2325.cameralibrary;

import android.annotation.TargetApi;
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
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
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
public class JCameraView extends RelativeLayout implements SurfaceHolder.Callback {

    public final String TAG = "JCameraView";

    private Context mContext;
    private VideoView mVideoView;
    private ImageView mImageView;
    private ImageView photoImageView;
    private CaptureButton mCaptureButtom;
    private int iconWidth = 0;
    private int iconMargin = 0;
    private int iconSrc = 0;

    private String saveVideoPath="";
    private String videoFileName="";


    private MediaRecorder mediaRecorder;
    private SurfaceHolder mHolder = null;
    private Camera mCamera;
    private Camera.Parameters mParam;
    private int width;
    private int height;
    private String fileName;
    private Bitmap pictureBitmap;

    private int SELECTED_CAMERA = 0;
    private final int CAMERA_POST_POSITION = 0;
    private final int CAMERA_FRONT_POSITION = 1;

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
        AudioUtil.setAudioManage(mContext);
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
                photoImageView.setVisibility(INVISIBLE);
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
                photoImageView.setVisibility(INVISIBLE);
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
            }

            @Override
            public void scale(float scaleValue) {
                if (scaleValue >= 0) {
                    int scaleRate = (int) (scaleValue / 50);
                    if (scaleRate < 10 && scaleRate >= 0) {
//                        mCamera.startSmoothZoom(scaleRate);
                        mParam.setZoom(scaleRate);
                        mCamera.setParameters(mParam);
                    }
//                        Log.i(TAG, "scaleValue = " + (int) scaleValue + " = scaleRate" + scaleRate);
                }
            }
        });
    }


    private void initView() {
        this.setBackgroundColor(Color.BLACK);
        /*
        Surface
         */
        mVideoView = new VideoView(mContext);
        LayoutParams videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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


        photoImageView = new ImageView(mContext);
        final LayoutParams photoImageViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        photoImageView.setLayoutParams(photoImageViewParam);
        photoImageView.setBackgroundColor(0xFF000000);
        photoImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoImageView.setVisibility(INVISIBLE);


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
                    setStartPreview(mCamera, mHolder);
                }
            }
        });


        this.addView(mVideoView);
        this.addView(photoImageView);
        this.addView(mCaptureButtom);
        this.addView(mImageView);
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
        if (camera==null){
            Log.i(TAG,"Camera is null");
            return;
        }
        try {
            mParam = camera.getParameters();
            mParam.setPictureFormat(ImageFormat.JPEG);
            List<Camera.Size> sizeList = mParam.getSupportedPreviewSizes();
            Iterator<Camera.Size> itor = sizeList.iterator();
            while (itor.hasNext()) {
                Camera.Size cur = itor.next();
                if (cur.width >= width && cur.height >= height) {
                    width = cur.width;
                    height = cur.height;
                }
            }
            mParam.setPreviewSize(width, height);
            mParam.setPictureSize(width, height);
            mParam.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
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
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (SELECTED_CAMERA == CAMERA_POST_POSITION && success) {
                    mCamera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            Matrix matrix = new Matrix();
                            matrix.setRotate(90);
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            pictureBitmap = bitmap;
                            photoImageView.setImageBitmap(bitmap);
                            photoImageView.setVisibility(VISIBLE);
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
                            matrix.postScale(-1, 1);   //镜像水平翻转
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            pictureBitmap = bitmap;
                            photoImageView.setImageBitmap(bitmap);
                            photoImageView.setVisibility(VISIBLE);
                            mImageView.setVisibility(INVISIBLE);
                            mCaptureButtom.captureSuccess();
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, holder);
        Log.i("Camera", "surfaceCreated");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHolder = holder;
        if (mCamera != null) {
            mCamera.stopPreview();
            setStartPreview(mCamera, holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
        Log.i("Camera", "surfaceDestroyed");
    }

    public void onResume() {
        mCamera = getCamera(SELECTED_CAMERA);
        if (mCamera!=null) {
            setStartPreview(mCamera, mHolder);
        }else{
            Log.i(TAG,"Camera is null!");
        }
    }

    public void onPause() {
        releaseCamera();
    }


    private void startRecord() {
        if (mCamera==null){
            Log.i(TAG,"Camera is null");
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
        mediaRecorder.setVideoSize(width, height);
        if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
            mediaRecorder.setOrientationHint(270);
        } else {
            mediaRecorder.setOrientationHint(90);
        }
        mediaRecorder.setMaxDuration(10000);
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mediaRecorder.setVideoFrameRate(20);
        mediaRecorder.setPreviewDisplay(mHolder.getSurface());

        videoFileName = "video_" + System.currentTimeMillis() + ".mp4";
        if (saveVideoPath.equals("")) {
            saveVideoPath = Environment.getExternalStorageDirectory().getPath();
            Log.i("Path", saveVideoPath +"/"+ videoFileName);
        }
        mediaRecorder.setOutputFile(saveVideoPath +"/"+ videoFileName);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecord() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            releaseCamera();
            fileName = saveVideoPath +"/"+ videoFileName;
            mVideoView.setVideoPath(fileName);
            mVideoView.start();
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
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

    public interface CameraViewListener {
        public void quit();

        public void captureSuccess(Bitmap bitmap);

        public void recordSuccess(String url);
    }
}
