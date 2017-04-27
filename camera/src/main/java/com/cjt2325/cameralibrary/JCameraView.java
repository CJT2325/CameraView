package com.cjt2325.cameralibrary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.cjt2325.cameralibrary.lisenter.CaptureLisenter;
import com.cjt2325.cameralibrary.lisenter.JCameraLisenter;
import com.cjt2325.cameralibrary.lisenter.ReturnLisenter;
import com.cjt2325.cameralibrary.lisenter.TypeLisenter;
import com.cjt2325.cameralibrary.util.AudioUtil;

import java.io.File;


/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/4/25
 * 描    述：
 * =====================================
 */
public class JCameraView extends RelativeLayout implements CameraInterface.CamOpenOverCallback, SurfaceHolder.Callback {
    private static final String TAG = "CJT";

    private static final int TYPE_PICTURE = 0x001;
    private static final int TYPE_VIDEO = 0x002;

    private JCameraLisenter jCameraLisenter;


    private Context mContext;
    private VideoView mVideoView;
    private ImageView mSwitchCamera;
    private CaptureLayout mCaptureLayout;
    private FoucsView mFoucsView;

    private int layout_width;
    private int fouce_size;
    private float screenProp;

    private Bitmap captureBitmap;
    private String videoUrl;
    private int type = -1;


    private int CAMERA_STATE = -1;
    private static final int STATE_IDLE = 0x010;
    private static final int STATE_RUNNING = 0x020;
    private static final int STATE_WAIT = 0x030;

    private Boolean stopping = false;

    /**
     * switch buttom param
     */
    private int iconSize = 0;
    private int iconMargin = 0;
    private int iconSrc = 0;
    private int duration = 0;

    /**
     * constructor
     */
    public JCameraView(Context context) {
        this(context, null);
    }

    /**
     * constructor
     */
    public JCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * constructor
     */
    public JCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        AudioUtil.setAudioManage(mContext);
        /**
         * get AttributeSet
         */
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.JCameraView, defStyleAttr, 0);
        iconSize = a.getDimensionPixelSize(R.styleable.JCameraView_iconSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 35, getResources().getDisplayMetrics()));
        iconMargin = a.getDimensionPixelSize(R.styleable.JCameraView_iconMargin, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics()));
        iconSrc = a.getResourceId(R.styleable.JCameraView_iconSrc, R.drawable.ic_sync_black_24dp);
        duration = a.getInteger(R.styleable.JCameraView_duration_max, 10 * 1000);
        initData();
        initView();
    }

    private void initData() {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        layout_width = outMetrics.widthPixels;
        fouce_size = (int) (layout_width / 4.5);


        CAMERA_STATE = STATE_IDLE;
    }


    private void initView() {
        setWillNotDraw(false);
        /**
         * VideoView
         */
        mVideoView = new VideoView(mContext);
        LayoutParams videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        videoViewParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mVideoView.setLayoutParams(videoViewParam);
        mVideoView.getHolder().setKeepScreenOn(true);

        /**
         * switchCamera
         */
        mSwitchCamera = new ImageView(mContext);
        LayoutParams imageViewParam = new LayoutParams(iconSize, iconSize);
        imageViewParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        imageViewParam.setMargins(0, iconMargin, iconMargin, 0);
        mSwitchCamera.setLayoutParams(imageViewParam);
        mSwitchCamera.setImageResource(iconSrc);
        mSwitchCamera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    /**
                     * switch camera
                     */
                    @Override
                    public void run() {
                        CameraInterface.getInstance().switchCamera();
                    }
                }.start();
            }
        });

        /**
         * CaptureLayout
         */
        mCaptureLayout = new CaptureLayout(mContext);
        LayoutParams layout_param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layout_param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layout_param.setMargins(0, 0, 0, 40);
        mCaptureLayout.setLayoutParams(layout_param);
        mCaptureLayout.setDuration(duration);

        /**
         * mFoucsView
         */
        mFoucsView = new FoucsView(mContext, fouce_size);
        LayoutParams foucs_param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mFoucsView.setLayoutParams(foucs_param);
        mFoucsView.setVisibility(INVISIBLE);

        /**
         * add view to ParentLayout
         */
        this.addView(mVideoView);
        this.addView(mSwitchCamera);
        this.addView(mCaptureLayout);
        this.addView(mFoucsView);

        /**
         * START >>>>>>> captureLayout lisenter callback
         */


        mCaptureLayout.setCaptureLisenter(new CaptureLisenter() {
            @Override
            public void takePictures() {
                if (CAMERA_STATE != STATE_IDLE) {
                    return;
                }
                CAMERA_STATE = STATE_RUNNING;
                CameraInterface.getInstance().takePicture(new CameraInterface.TakePictureCallback() {
                    @Override
                    public void captureResult(Bitmap bitmap) {
                        captureBitmap = bitmap;
                        CameraInterface.getInstance().doStopCamera();
                        type = TYPE_PICTURE;
                        CAMERA_STATE = STATE_WAIT;
                    }
                });
            }

            @Override
            public void recordShort(long time) {
                if (CAMERA_STATE != STATE_RUNNING && stopping) {
                    return;
                }
                stopping = true;
                Log.i(TAG, "time = " + time);
                mCaptureLayout.setTextWithAnimation();
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CameraInterface.getInstance().stopRecord(true, new CameraInterface.StopRecordCallback() {
                            @Override
                            public void recordResult(String url) {
                                Log.i(TAG, "stopping ...");
                                CAMERA_STATE = STATE_IDLE;
                                stopping = false;
                            }
                        });
                    }
                }, 1500 - time);
            }

            @Override
            public void recordStart() {
                if (CAMERA_STATE != STATE_IDLE && stopping) {
                    return;
                }
                CAMERA_STATE = STATE_RUNNING;
                CameraInterface.getInstance().startRecord();
            }

            @Override
            public void recordEnd(long time) {
                CameraInterface.getInstance().stopRecord(false, new CameraInterface.StopRecordCallback() {
                    @Override
                    public void recordResult(final String url) {
                        CAMERA_STATE = STATE_WAIT;
                        videoUrl = url;
                        type = TYPE_VIDEO;
                        mVideoView.setVideoPath(url);
                        mVideoView.start();
                        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.start();
                                mp.setLooping(true);
                            }
                        });
                        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mVideoView.setVideoPath(url);
                                mVideoView.start();
                            }
                        });
                    }
                });
            }
        });
        mCaptureLayout.setTypeLisenter(new TypeLisenter() {
            @Override
            public void cancel() {
                if (CAMERA_STATE == STATE_WAIT) {
                    mVideoView.stopPlayback();
                    CameraInterface.getInstance().doOpenCamera(JCameraView.this);
                    handlerPictureOrVideo(type, false);
                }
            }

            @Override
            public void confirm() {
                if (CAMERA_STATE == STATE_WAIT) {
                    mVideoView.stopPlayback();
                    CameraInterface.getInstance().doOpenCamera(JCameraView.this);
                    handlerPictureOrVideo(type, true);
                }
            }
        });
        mCaptureLayout.setReturnLisenter(new ReturnLisenter() {
            @Override
            public void onReturn() {
                if (jCameraLisenter != null) {
                    jCameraLisenter.quit();
                }
            }
        });
        /**
         * END >>>>>>> captureLayout lisenter callback
         */
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
        SurfaceHolder holder = mVideoView.getHolder();
        CameraInterface.getInstance().doStartPreview(holder, screenProp);
    }

    /**
     * holder callback
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraInterface.getInstance().doStopCamera();
    }

    /**
     * start preview
     */
    public void onResume() {
        if (!CameraInterface.getInstance().isPreviewing()) {
            new Thread() {
                @Override
                public void run() {
                    CameraInterface.getInstance().doStopCamera();
                    CameraInterface.getInstance().doOpenCamera(JCameraView.this);
                }
            }.start();
        }
    }

    /**
     * stop preview
     */
    public void onPause() {
        CameraInterface.getInstance().doStopCamera();
        mVideoView.stopPlayback();
        mVideoView.resume();
    }

    /**
     * handler touch focus
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() == 1) {
                    setFocusViewWidthAnimation(event.getX(), event.getY());
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * focusview animation
     */
    private void setFocusViewWidthAnimation(float x, float y) {
        if (y > mCaptureLayout.getTop()) {
            return;
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
        CameraInterface.getInstance().handleFocus(x, y, new CameraInterface.FocusCallback() {
            @Override
            public void focusSuccess() {
                mFoucsView.setVisibility(INVISIBLE);
            }
        });

        mFoucsView.setX(x - mFoucsView.getWidth() / 2);
        mFoucsView.setY(y - mFoucsView.getHeight() / 2);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.3f, 1f, 0.3f, 1f, 0.3f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(scaleX).with(scaleY).before(alpha);
        animSet.setDuration(400);
        animSet.start();
    }

    public void setJCameraLisenter(JCameraLisenter jCameraLisenter) {
        this.jCameraLisenter = jCameraLisenter;
    }


    private void handlerPictureOrVideo(int type, boolean confirm) {
        mVideoView.pause();
        mVideoView.stopPlayback();
        if (jCameraLisenter == null || type == -1) {
            return;
        }
        switch (type) {
            case TYPE_PICTURE:
                if (confirm && captureBitmap != null) {
                    jCameraLisenter.captureSuccess(captureBitmap);
                } else {
                    captureBitmap.recycle();
                    captureBitmap = null;
                }
                break;
            case TYPE_VIDEO:
                if (confirm) {
                    jCameraLisenter.recordSuccess(videoUrl);
                } else {
                    /**
                     * delete video file
                     */
                    File file = new File(videoUrl);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                break;
        }
        CAMERA_STATE = STATE_IDLE;
    }

    public void setSaveVideoPath(String path) {
        CameraInterface.getInstance().setSaveVideoPath(path);
    }
}
