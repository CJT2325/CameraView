package com.cameraview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.util.Log;
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
 * 作者: 陈嘉桐 on 2017/2/2
 * 邮箱: 445263848@qq.com.
 */
public class JCameraView extends RelativeLayout implements SurfaceHolder.Callback {

    public final String TAG = "JCameraView";

    private Context mContext;
    private VideoView mVideoView;
    private ImageView mImageView;
    private ImageView photoImageView;
    private CaptureButtom mCaptureButtom;


    private MediaRecorder mediaRecorder;
    private SurfaceHolder mHolder = null;
    private Camera mCamera;
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

    /*
            构造函数
             */
    public JCameraView(Context context) {
        this(context, null);
    }

    public JCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public JCameraView(final Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;

        //取消提示音
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);


        SELECTED_CAMERA = CAMERA_POST_POSITION;
        //设置回调
        this.setBackgroundColor(Color.BLACK);
        /*
        初始化Surface
         */
        mVideoView = new VideoView(context);
        RelativeLayout.LayoutParams videoViewParam = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        videoViewParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//        videoViewParam.setMargins(10,10,10,10);
        mVideoView.setLayoutParams(videoViewParam);
        /*
        初始化CaptureButtom
         */
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mCaptureButtom = new CaptureButtom(context);
        mCaptureButtom.setLayoutParams(btnParams);

        /*
        初始化结果图片
         */
        photoImageView = new ImageView(context);
        final RelativeLayout.LayoutParams photoImageViewParam = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        photoImageView.setLayoutParams(photoImageViewParam);
        photoImageView.setBackgroundColor(0xFF000000);
        photoImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoImageView.setVisibility(INVISIBLE);


        /*
        初始化ImageView
         */
        mImageView = new ImageView(context);
        RelativeLayout.LayoutParams imageViewParam = new RelativeLayout.LayoutParams(60, 60);
        imageViewParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        imageViewParam.setMargins(0, 40, 40, 0);
        mImageView.setLayoutParams(imageViewParam);
        mImageView.setImageResource(R.drawable.ic_repeat_black_24dp);
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //摄像头切换
//                Toast.makeText(mContext, "mImageView", Toast.LENGTH_SHORT).show();
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

        mHolder = mVideoView.getHolder();
        mHolder.addCallback(this);


        mCaptureButtom.setCaptureListener(new CaptureButtom.CaptureListener() {
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
//                Toast.makeText(mContext,"cancel",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void determine() {
                if (cameraViewListener != null) {
                    FileUtil.saveBitmap(pictureBitmap);
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
                //删除视频
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
                Log.i(TAG, "scaleValue = " + scaleValue);
            }
        });
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


    //启动相机浏览
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);
            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();//获取所有支持的camera尺寸
            Iterator<Camera.Size> itor = sizeList.iterator();
            while (itor.hasNext()) {
                Camera.Size cur = itor.next();
                Log.i("CJT", "所有的  width = " + cur.width + " height = " + cur.height);
                if (cur.width >= width&& cur.height >= height) {
                    width = cur.width;
                    height = cur.height;
                }
            }
            Log.i("size", "width : height" + width + " : " + height + " ==== " + getWidth() + " : " + getHeight());
            parameters.setPreviewSize(width, height);//把camera.size赋值到parameters
            parameters.setPictureSize(width, height);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(parameters);

            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    //拍照
    public void capture() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
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
        //默认启动后置摄像头
        mCamera = getCamera(SELECTED_CAMERA);
        setStartPreview(mCamera, mHolder);
    }

    public void onPause() {
        releaseCamera();
    }

    /*
    开始录制
     */
    private void startRecord() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        mCamera.unlock();
        // 设置录制视频源为Camera(相机)
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 设置录制的视频编码h263 h264
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
        mediaRecorder.setVideoSize(width, height);
        // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
        if (SELECTED_CAMERA == CAMERA_FRONT_POSITION) {
            mediaRecorder.setOrientationHint(270);
        } else {
            mediaRecorder.setOrientationHint(90);
        }
        mediaRecorder.setMaxDuration(10000);
        mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
        mediaRecorder.setVideoFrameRate(20);
        mediaRecorder.setPreviewDisplay(mHolder.getSurface());
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
    }

    /*
    停止录制
     */
    private void stopRecord() {
        if (mediaRecorder != null) {
            //停止录制
            mediaRecorder.stop();
            //释放资源
            mediaRecorder.release();
            mediaRecorder = null;
            releaseCamera();
            fileName = "/sdcard/love.mp4";
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
//            Toast.makeText(mContext, mVideoView.isPlaying() + "=", Toast.LENGTH_SHORT).show();
        }
    }


    public interface CameraViewListener {
        public void quit();

        public void captureSuccess(Bitmap bitmap);

        public void recordSuccess(String url);
    }
}
