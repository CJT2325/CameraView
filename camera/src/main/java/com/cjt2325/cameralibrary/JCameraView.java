package com.cjt2325.cameralibrary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cjt2325.cameralibrary.lisenter.CaptureLisenter;
import com.cjt2325.cameralibrary.lisenter.JCameraLisenter;
import com.cjt2325.cameralibrary.lisenter.ReturnLisenter;
import com.cjt2325.cameralibrary.lisenter.TypeLisenter;
import com.cjt2325.cameralibrary.util.AudioUtil;

import java.io.File;
import java.io.IOException;


/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/4/25
 * 描    述：
 * =====================================
 */
public class JCameraView extends RelativeLayout implements CameraInterface.CamOpenOverCallback, TextureView
        .SurfaceTextureListener {
    private static final String TAG = "CJT";

    private static final int TYPE_PICTURE = 0x001;
    private static final int TYPE_VIDEO = 0x002;

    private JCameraLisenter jCameraLisenter;


    private Context mContext;
    private TextureView mTextureView;
    private ImageView mPhoto;
    private ImageView mSwitchCamera;
    private CaptureLayout mCaptureLayout;
    private FoucsView mFoucsView;

    private MediaPlayer mMediaPlayer;
    private Surface mSurface;

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
        a.recycle();
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
        this.setBackgroundColor(0xff000000);
        /**
         * VideoView
         */
        mTextureView = new TextureView(mContext);
        LayoutParams textureViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textureViewParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mTextureView.setLayoutParams(textureViewParam);
        mTextureView.setBackgroundColor(0xff000000);

        /**
         * mPhoto
         */
        mPhoto = new ImageView(mContext);
        LayoutParams photoParam = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams
                .MATCH_PARENT);
        photoParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        mPhoto.setLayoutParams(photoParam);
        mPhoto.setBackgroundColor(0xff000000);
        mPhoto.setVisibility(INVISIBLE);
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
        this.addView(mTextureView);
        this.addView(mPhoto);
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
                        mPhoto.setImageBitmap(bitmap);
                        mPhoto.setVisibility(VISIBLE);
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
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (mMediaPlayer == null) {
                                        mMediaPlayer = new MediaPlayer();
                                    } else {
                                        mMediaPlayer.reset();
                                    }
                                    mMediaPlayer.setDataSource(url);
                                    mMediaPlayer.setSurface(mSurface);
//                                    mMediaPlayer.setVideoScalingMode(VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                                            .OnVideoSizeChangedListener() {
                                        @Override
                                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                            mVideoHeight = mMediaPlayer.getVideoHeight();
                                            mVideoWidth = mMediaPlayer.getVideoWidth();
                                            updateTextureViewSize(CENTER_MODE);
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
                });
            }
        });
        mCaptureLayout.setTypeLisenter(new TypeLisenter() {
            @Override
            public void cancel() {
                if (CAMERA_STATE == STATE_WAIT) {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                    CameraInterface.getInstance().doOpenCamera(JCameraView.this);
                    handlerPictureOrVideo(type, false);
                }
            }

            @Override
            public void confirm() {
                if (CAMERA_STATE == STATE_WAIT) {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
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
        mTextureView.setSurfaceTextureListener(this);
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
        CameraInterface.getInstance().doStartPreview(mTextureView.getSurfaceTexture(), screenProp);
    }

    /**
     * start preview
     */
    public void onResume() {
        CameraInterface.getInstance().registerSensorManager(mContext);
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
        CameraInterface.getInstance().unregisterSensorManager(mContext);
        CameraInterface.getInstance().doStopCamera();
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
        if (jCameraLisenter == null || type == -1) {
            return;
        }
        switch (type) {
            case TYPE_PICTURE:
                mPhoto.setVisibility(INVISIBLE);
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
                if (original_matrix != null) {
                    mTextureView.setTransform(original_matrix);
                }
                break;
        }
        CAMERA_STATE = STATE_IDLE;
    }

    public void setSaveVideoPath(String path) {
        CameraInterface.getInstance().setSaveVideoPath(path);
    }


    /**
     * textureView lisenter
     *
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.mSurface = new Surface(surface);
        CameraInterface.getInstance().doStartPreview(surface, screenProp);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    /**
     * TextureView resize
     */

    private int mVideoHeight;
    private int mVideoWidth;
    private Matrix original_matrix;

    public static final int CENTER_CROP_MODE = 1;//中心裁剪模式
    public static final int CENTER_MODE = 2;//一边中心填充模式

    public void updateTextureViewSize(int mode) {
        if (mode == CENTER_MODE) {
            updateTextureViewSizeCenter();
        } else if (mode == CENTER_CROP_MODE) {
            updateTextureViewSizeCenterCrop();
        }
    }

    //重新计算video的显示位置，让其全部显示并据中
    private void updateTextureViewSizeCenter() {

        float sx = (float) getWidth() / (float) mVideoWidth;
        float sy = (float) getHeight() / (float) mVideoHeight;

        Matrix matrix = new Matrix();

        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((getWidth() - mVideoWidth) / 2, (getHeight() - mVideoHeight) / 2);

        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(mVideoWidth / (float) getWidth(), mVideoHeight / (float) getHeight());

        //第3步,等比例放大或缩小,直到视频区的一边和View一边相等.如果另一边和view的一边不相等，则留下空隙
        if (sx >= sy) {
            matrix.postScale(sy, sy, getWidth() / 2, getHeight() / 2);
        } else {
            matrix.postScale(sx, sx, getWidth() / 2, getHeight() / 2);
        }
        if (original_matrix == null) {
            original_matrix = mTextureView.getMatrix();
        }
        mTextureView.setTransform(matrix);
        postInvalidate();
    }

    //重新计算video的显示位置，裁剪后全屏显示
    private void updateTextureViewSizeCenterCrop() {
//        float sx = (float) getWidth() / (float) mVideoWidth;
//        float sy = (float) getHeight() / (float) mVideoHeight;

        float sx = (float) getWidth();
        float sy = (float) getHeight();

        Matrix matrix = new Matrix();
        float maxScale = Math.max(sx, sy);

        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate(getWidth(), getHeight());

        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(getWidth(), getHeight());

        //第3步,等比例放大或缩小,直到视频区的一边超过View一边, 另一边与View的另一边相等. 因为超过的部分超出了View的范围,所以是不会显示的,相当于裁剪了.
        matrix.postScale(maxScale, maxScale, getWidth(), getHeight());//后两个参数坐标是以整个View的坐标系以参考的

        mTextureView.setTransform(matrix);
        postInvalidate();
    }

    public void forbiddenAudio(boolean forbidden) {
        if (forbidden) {
            AudioUtil.setAudioManage(mContext);
        }
    }
}
