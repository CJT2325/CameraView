package com.cjt2325.cameralibrary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import com.cjt2325.cameralibrary.lisenter.CaptureLisenter;
import com.cjt2325.cameralibrary.lisenter.ErrorLisenter;
import com.cjt2325.cameralibrary.lisenter.JCameraLisenter;
import com.cjt2325.cameralibrary.lisenter.ReturnLisenter;
import com.cjt2325.cameralibrary.lisenter.TypeLisenter;
import com.cjt2325.cameralibrary.state.CameraMachine;
import com.cjt2325.cameralibrary.util.LogUtil;
import com.cjt2325.cameralibrary.view.CameraView;

import java.io.File;
import java.io.IOException;


/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.0.4
 * 创建日期：2017/4/25
 * 描    述：
 * =====================================
 */
public class JCameraView extends FrameLayout implements CameraInterface.CameraOpenOverCallback, SurfaceHolder.Callback, CameraView {
    private static final String TAG = "JCameraView";

    //Camera状态机
    private CameraMachine machine;

    //拍照浏览时候的类型
    public static final int TYPE_PICTURE = 0x001;
    public static final int TYPE_VIDEO = 0x002;
    public static final int TYPE_SHORT = 0x003;

    //录制视频比特率
    public static final int MEDIA_QUALITY_HIGH = 20 * 100000;
    public static final int MEDIA_QUALITY_MIDDLE = 16 * 100000;
    public static final int MEDIA_QUALITY_LOW = 12 * 100000;
    public static final int MEDIA_QUALITY_POOR = 8 * 100000;
    public static final int MEDIA_QUALITY_FUNNY = 4 * 100000;
    public static final int MEDIA_QUALITY_DESPAIR = 2 * 100000;
    public static final int MEDIA_QUALITY_SORRY = 1 * 80000;


    public static final int BUTTON_STATE_ONLY_CAPTURE = 0x101;      //只能拍照
    public static final int BUTTON_STATE_ONLY_RECORDER = 0x102;     //只能录像
    public static final int BUTTON_STATE_BOTH = 0x103;              //两者都可以

    //回调监听
    private JCameraLisenter jCameraLisenter;

    private Context mContext;
    private VideoView mVideoView;
    private ImageView mPhoto;
    private ImageView mSwitchCamera;
    private CaptureLayout mCaptureLayout;
    private FoucsView mFoucsView;
    private MediaPlayer mMediaPlayer;

    private int layout_width;
    private int fouce_size;
    private float screenProp;

    private Bitmap captureBitmap;   //捕获的图片
    private Bitmap firstFrame;      //第一帧图片
    private String videoUrl;        //视频URL

    private int type = -1;
    private boolean onlyPause = false;

    private int CAMERA_STATE = -1;
    private static final int STATE_IDLE = 0x010;
    private static final int STATE_RUNNING = 0x020;
    private static final int STATE_WAIT = 0x030;

    private boolean stopping = false;
    private boolean isBorrow = false;
    private boolean takePictureing = false;
    private boolean forbiddenSwitch = false;

    //切换摄像头按钮的参数
    private int iconSize = 0;       //图标大小
    private int iconMargin = 0;     //右上边距
    private int iconSrc = 0;        //图标资源
    private int duration = 0;       //录制时间

    public JCameraView(Context context) {
        this(context, null);
    }

    public JCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //get AttributeSet
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.JCameraView, defStyleAttr, 0);
        iconSize = a.getDimensionPixelSize(R.styleable.JCameraView_iconSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 35, getResources().getDisplayMetrics()));
        iconMargin = a.getDimensionPixelSize(R.styleable.JCameraView_iconMargin, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics()));
        iconSrc = a.getResourceId(R.styleable.JCameraView_iconSrc, R.drawable.ic_sync_black_24dp);
        duration = a.getInteger(R.styleable.JCameraView_duration_max, 10 * 1000);       //没设置默认为10s
        a.recycle();
        initData();
        initView();
    }

    private void initData() {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        layout_width = outMetrics.widthPixels;
        fouce_size = layout_width / 4;
        CAMERA_STATE = STATE_IDLE;

        machine = new CameraMachine(getContext(), this, this);
    }


    private void initView() {
        setWillNotDraw(false);
        this.setBackgroundColor(0xff000000);
        //VideoView
        mVideoView = new VideoView(mContext);
        LayoutParams videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mVideoView.setLayoutParams(videoViewParam);

        //mPhoto
        mPhoto = new ImageView(mContext);
        LayoutParams photoParam = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                .MATCH_PARENT);
        mPhoto.setLayoutParams(photoParam);
        mPhoto.setBackgroundColor(0xff000000);
        mPhoto.setVisibility(INVISIBLE);

        //switchCamera
        mSwitchCamera = new ImageView(mContext);
        LayoutParams imageViewParam = new LayoutParams(iconSize + 2 * iconMargin, iconSize + 2 * iconMargin);
        imageViewParam.gravity = Gravity.RIGHT;
        mSwitchCamera.setPadding(iconMargin, iconMargin, iconMargin, iconMargin);
        mSwitchCamera.setLayoutParams(imageViewParam);
        mSwitchCamera.setImageResource(iconSrc);
        mSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                machine.swtich();
            }
        });
        //CaptureLayout
        mCaptureLayout = new CaptureLayout(mContext);
        LayoutParams layout_param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout_param.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        mCaptureLayout.setLayoutParams(layout_param);
        mCaptureLayout.setDuration(duration);

        //mFoucsView
        mFoucsView = new FoucsView(mContext, fouce_size);
        LayoutParams foucs_param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        foucs_param.gravity = Gravity.CENTER;
        mFoucsView.setLayoutParams(foucs_param);
        mFoucsView.setVisibility(INVISIBLE);

        //add view to ParentLayout
        this.addView(mVideoView);
        this.addView(mPhoto);
        this.addView(mSwitchCamera);
        this.addView(mCaptureLayout);
        this.addView(mFoucsView);
        //START >>>>>>> captureLayout lisenter callback
        mCaptureLayout.setCaptureLisenter(new CaptureLisenter() {
            @Override
            public void takePictures() {
                machine.capture();
            }

            @Override
            public void recordStart() {
                machine.record(mVideoView.getHolder().getSurface());
//                if (CAMERA_STATE != STATE_IDLE && stopping) {
//                    return;
//                }
//                mSwitchCamera.setVisibility(GONE);
//                isBorrow = true;
//                CAMERA_STATE = STATE_RUNNING;
//                mFoucsView.setVisibility(INVISIBLE);
//                CameraInterface.getInstance().startRecord(mVideoView.getHolder().getSurface(), new CameraInterface
//                        .ErrorCallback() {
//                    @Override
//                    public void onError() {
//                        Log.i("CJT", "startRecorder error");
//                        CAMERA_STATE = STATE_WAIT;
//                        stopping = false;
//                        isBorrow = false;
//                    }
//                });
            }

            @Override
            public void recordShort(final long time) {
//                if (CAMERA_STATE != STATE_RUNNING && stopping) {
//                    return;
//                }
//                stopping = true;
                mCaptureLayout.setTextWithAnimation("录制时间过短");
//                mSwitchCamera.setRotation(0);
                mSwitchCamera.setVisibility(VISIBLE);
//                CameraInterface.getInstance().setSwitchView(mSwitchCamera);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        machine.stopRecord(true, time);
                    }
                }, 1500 - time);
            }


            @Override
            public void recordEnd(long time) {
                machine.stopRecord(false, time);
//                CameraInterface.getInstance().stopRecord(false, new CameraInterface.StopRecordCallback() {
//                    @Override
//                    public void recordResult(final String url, Bitmap firstFrame) {
//                        CAMERA_STATE = STATE_WAIT;
//                        videoUrl = url;
//                        type = TYPE_VIDEO;
//                        JCameraView.this.firstFrame = firstFrame;
//                        new Thread(new Runnable() {
//                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//                            @Override
//                            public void run() {
//                                try {
//                                    if (mMediaPlayer == null) {
//                                        mMediaPlayer = new MediaPlayer();
//                                    } else {
//                                        mMediaPlayer.reset();
//                                    }
//                                    Log.i("CJT", "URL = " + url);
//                                    mMediaPlayer.setDataSource(url);
//                                    mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
//                                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
//                                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
//                                            .OnVideoSizeChangedListener() {
//                                        @Override
//                                        public void
//                                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//                                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
//                                                    .getVideoHeight());
//                                        }
//                                    });
//                                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                                        @Override
//                                        public void onPrepared(MediaPlayer mp) {
//                                            mMediaPlayer.start();
//                                        }
//                                    });
//                                    mMediaPlayer.setLooping(true);
//                                    mMediaPlayer.prepare();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }).start();
//                    }
//                });
            }

            @Override
            public void recordZoom(float zoom) {
                machine.zoom(zoom, CameraInterface.TYPE_RECORDER);
//                CameraInterface.getInstance().setZoom(zoom, CameraInterface.TYPE_RECORDER);
            }

            @Override
            public void recordError() {
                //错误回调
                if (errorLisenter != null) {
                    errorLisenter.AudioPermissionError();
                }
            }
        });
        mCaptureLayout.setTypeLisenter(new TypeLisenter() {
            @Override
            public void cancel() {
                machine.cancle(mVideoView.getHolder(), screenProp);
//                LogUtil.i("handlerPicure");
//                handlerPictureOrVideo(type, false);
//                if (CAMERA_STATE == STATE_WAIT) {
//                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
//                        mMediaPlayer.stop();
//                        mMediaPlayer.release();
//                        mMediaPlayer = null;
//                    }
//                }
            }

            @Override
            public void confirm() {
                machine.confirm();
//                handlerPictureOrVideo(type, true);
//                if (CAMERA_STATE == STATE_WAIT) {
//                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
//                        mMediaPlayer.stop();
//                        mMediaPlayer.release();
//                        mMediaPlayer = null;
//                    }
//                    handlerPictureOrVideo(type, true);
//                }
            }
        });
        mCaptureLayout.setReturnLisenter(new ReturnLisenter() {
            @Override
            public void onReturn() {
                if (jCameraLisenter != null && !takePictureing) {
                    jCameraLisenter.quit();
                }
            }
        });
        //END >>>>>>> captureLayout lisenter callback
        mVideoView.getHolder().addCallback(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float widthSize = MeasureSpec.getSize(widthMeasureSpec);
        float heightSize = MeasureSpec.getSize(heightMeasureSpec);
        screenProp = heightSize / widthSize;
    }

    @Override
    public void cameraHasOpened() {
        CameraInterface.getInstance().doStartPreview(mVideoView.getHolder(), screenProp);
    }

    private boolean switching = false;

    @Override
    public void cameraSwitchSuccess() {
        switching = false;
    }

    /**
     * start preview
     */
    public void onResume() {
        CameraInterface.getInstance().registerSensorManager(mContext);
        CameraInterface.getInstance().setSwitchView(mSwitchCamera);
        if (onlyPause) {
//            if (isBorrow && type == TYPE_VIDEO) {
//                new Thread(new Runnable() {
//                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//                    @Override
//                    public void run() {
//                        try {
//                            if (mMediaPlayer == null) {
//                                mMediaPlayer = new MediaPlayer();
//                            } else {
//                                mMediaPlayer.reset();
//                            }
//                            Log.i("CJT", "URL = " + videoUrl);
//                            mMediaPlayer.setDataSource(videoUrl);
//                            mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
//                            mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
//                            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                            mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
//                                    .OnVideoSizeChangedListener() {
//                                @Override
//                                public void
//                                onVideoSizeChanged(MediaPlayer mp, int width, int height) {
//                                    updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
//                                            .getVideoHeight());
//                                }
//                            });
//                            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                                @Override
//                                public void onPrepared(MediaPlayer mp) {
//                                    mMediaPlayer.start();
//                                }
//                            });
//                            mMediaPlayer.setLooping(true);
//                            mMediaPlayer.prepare();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//            } else {
            new Thread() {
                @Override
                public void run() {
                    CameraInterface.getInstance().doOpenCamera(JCameraView.this);
                }
            }.start();
            mFoucsView.setVisibility(INVISIBLE);
//            }
        }
    }

    /**
     * stop preview
     */
    public void onPause() {
        onlyPause = true;
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        CameraInterface.getInstance().unregisterSensorManager(mContext);
    }

    private boolean firstTouch = true;
    private float firstTouchLength = 0;
    private int zoomScale = 0;

    /**
     * handler touch focus
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    //显示对焦指示器
                    setFocusViewWidthAnimation(event.getX(), event.getY());
                }
                if (event.getPointerCount() == 2) {
                    Log.i("CJT", "ACTION_DOWN = " + 2);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    firstTouch = true;
                }
                if (event.getPointerCount() == 2) {
                    //第一个点
                    float point_1_X = event.getX(0);
                    float point_1_Y = event.getY(0);
                    //第二个点
                    float point_2_X = event.getX(1);
                    float point_2_Y = event.getY(1);

                    float result = (float) Math.sqrt(Math.pow(point_1_X - point_2_X, 2) + Math.pow(point_1_Y -
                            point_2_Y, 2));

                    if (firstTouch) {
                        firstTouchLength = result;
                        firstTouch = false;
                    }
                    if ((int) (result - firstTouchLength) / 40 != 0) {
                        firstTouch = true;
                        machine.zoom(result - firstTouchLength, CameraInterface.TYPE_CAPTURE);
                    }
                    Log.i("CJT", "result = " + (result - firstTouchLength));
                }
                break;
            case MotionEvent.ACTION_UP:
                firstTouch = true;
                break;
        }
        return true;
    }

    /**
     * focusview animation
     */
    private void setFocusViewWidthAnimation(float x, float y) {
        machine.foucs(x, y, new CameraInterface.FocusCallback() {
            @Override
            public void focusSuccess() {
                mFoucsView.setVisibility(INVISIBLE);
            }
        });
    }

    public void setJCameraLisenter(JCameraLisenter jCameraLisenter) {
        this.jCameraLisenter = jCameraLisenter;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void handlerPictureOrVideo(int type, boolean confirm) {
        if (confirm) {
            machine.confirm();
        } else {
            mPhoto.setVisibility(INVISIBLE);
            machine.cancle(mVideoView.getHolder(), screenProp);
        }
        if (jCameraLisenter == null || type == -1) {
            return;
        }
//        switch (type) {
//            case TYPE_PICTURE:
//                if (confirm && captureBitmap != null) {
//                    jCameraLisenter.captureSuccess(captureBitmap);
//                } else {
//                    mPhoto.setVisibility(INVISIBLE);
//                    if (captureBitmap != null) {
//                        captureBitmap.recycle();
//                    }
//                    captureBitmap = null;
//                }
//                break;
//            case TYPE_VIDEO:
//                Log.i("CJT", "TYPE VIDEO");
//                if (confirm) {
//                    //回调录像成功后的URL
//                    jCameraLisenter.recordSuccess(videoUrl, firstFrame);
//                } else {
//                    //删除视频
//                    File file = new File(videoUrl);
//                    if (file.exists()) {
//                        file.delete();
//                    }
//                }
//                LayoutParams videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//                mVideoView.setLayoutParams(videoViewParam);
//                CameraInterface.getInstance().doOpenCamera(JCameraView.this);
//                mSwitchCamera.setRotation(0);
//                CameraInterface.getInstance().setSwitchView(mSwitchCamera);
//                break;
//        }
//        isBorrow = false;
//        mSwitchCamera.setVisibility(VISIBLE);
//        CAMERA_STATE = STATE_IDLE;
//        mFoucsView.setVisibility(VISIBLE);
//        mCaptureLayout.showTip();
        setFocusViewWidthAnimation(getWidth() / 2, getHeight() / 2);
    }

    public void setSaveVideoPath(String path) {
        CameraInterface.getInstance().setSaveVideoPath(path);
    }

    /**
     * TextureView resize
     */
    public void updateVideoViewSize(float videoWidth, float videoHeight) {
        if (videoWidth > videoHeight) {
            LayoutParams videoViewParam;
            int height = (int) ((videoHeight / videoWidth) * getWidth());
            videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT,
                    height);
            videoViewParam.gravity = Gravity.CENTER;
//            videoViewParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            mVideoView.setLayoutParams(videoViewParam);
        }
    }


    /**************************************************
     *                对外提供的API                     *
     **************************************************/

    public void enableshutterSound(boolean enable) {
    }

    public void forbiddenSwitchCamera(boolean forbiddenSwitch) {
        this.forbiddenSwitch = forbiddenSwitch;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtil.i("surfaceCreated");
        new Thread() {
            @Override
            public void run() {
                CameraInterface.getInstance().doOpenCamera(JCameraView.this);
            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        onlyPause = false;
        LogUtil.i("surfaceDestroyed");
        CameraInterface.getInstance().doDestroyCamera();
    }

    private ErrorLisenter errorLisenter;

    //启动Camera错误回调
    public void setErrorLisenter(ErrorLisenter errorLisenter) {
        this.errorLisenter = errorLisenter;
        CameraInterface.getInstance().setErrorLinsenter(errorLisenter);
    }

    //设置CaptureButton功能（拍照和录像）
    public void setFeatures(int state) {
        this.mCaptureLayout.setButtonFeatures(state);
    }

    //设置录制质量
    public void setMediaQuality(int quality) {
        CameraInterface.getInstance().setMediaQuality(quality);
    }

    @Override
    public void reset(int type) {
        switch (type) {
            case TYPE_VIDEO:
                stopVideo();
                LayoutParams videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                mVideoView.setLayoutParams(videoViewParam);
                CameraInterface.getInstance().doOpenCamera(JCameraView.this);
//                mSwitchCamera.setRotation(0);
//                CameraInterface.getInstance().setSwitchView(mSwitchCamera);
                break;
            case TYPE_PICTURE:
                break;
            case TYPE_SHORT:
                break;
        }
        mCaptureLayout.resetCaptureLayout();
    }

    @Override
    public void showPicture(Bitmap bitmap, boolean isVertical) {
        if (isVertical)
            mPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        else
            mPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
        captureBitmap = bitmap;
//        type = TYPE_PICTURE;
//        isBorrow = true;
//        CAMERA_STATE = STATE_WAIT;
        mPhoto.setImageBitmap(bitmap);
        mPhoto.setVisibility(VISIBLE);
        mCaptureLayout.startAlphaAnimation();
        mCaptureLayout.startTypeBtnAnimator();
//        takePictureing = false;
//        mSwitchCamera.setVisibility(INVISIBLE);
//        CameraInterface.getInstance().doOpenCamera(JCameraView.this);
//        mCaptureLayout.setCaptureButtomState(CaptureButton.STATE_IDLE);
    }

    @Override
    public void playVideo(Bitmap firstFrame, final String url) {
        CAMERA_STATE = STATE_WAIT;
        videoUrl = url;
        type = TYPE_VIDEO;
        JCameraView.this.firstFrame = firstFrame;
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                try {
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                    } else {
                        mMediaPlayer.reset();
                    }
                    Log.i("CJT", "URL = " + url);
                    mMediaPlayer.setDataSource(url);
                    mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                            .OnVideoSizeChangedListener() {
                        @Override
                        public void
                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
                                    .getVideoHeight());
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                        }
                    });
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void stopVideo() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void setTip(String tip) {
        mCaptureLayout.setTip(tip);
    }

    @Override
    public boolean handlerFoucs(float x, float y) {
        LogUtil.i("handlerFouces```");
        if (y > mCaptureLayout.getTop()) {
            return false;
        }
        mFoucsView.setVisibility(VISIBLE);
        if (x < mFoucsView.getWidth() / 2) {
            x = mFoucsView.getWidth() / 2;
        }
        if (x > layout_width - mFoucsView.getWidth() / 2) {
            x = layout_width - mFoucsView.getWidth() / 2;
        }
        if (y < mFoucsView.getWidth() / 2) {
            y = mFoucsView.getWidth() / 2;
        }
        if (y > mCaptureLayout.getTop() - mFoucsView.getWidth() / 2) {
            y = mCaptureLayout.getTop() - mFoucsView.getWidth() / 2;
        }
        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.3f, 1f, 0.3f, 1f, 0.3f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
        return true;
    }
}
