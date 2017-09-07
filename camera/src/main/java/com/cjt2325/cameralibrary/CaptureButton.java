package com.cjt2325.cameralibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;

import com.cjt2325.cameralibrary.lisenter.CaptureLisenter;
import com.cjt2325.cameralibrary.util.CheckPermission;
import com.cjt2325.cameralibrary.util.LogUtil;

import static com.cjt2325.cameralibrary.JCameraView.BUTTON_STATE_BOTH;
import static com.cjt2325.cameralibrary.JCameraView.BUTTON_STATE_ONLY_CAPTURE;
import static com.cjt2325.cameralibrary.JCameraView.BUTTON_STATE_ONLY_RECORDER;


/**
 * =====================================
 * 作    者: 陈嘉桐 445263848@qq.com
 * 版    本：1.0.4
 * 创建日期：2017/4/25
 * 描    述：拍照按钮
 * =====================================
 */
public class CaptureButton extends View {

    //按钮可执行的功能状态（拍照,录制,两者）
    private int button_state;

    //当前按钮状态
    private int state;
    //空状态
    public static final int STATE_NULL = 0x000;
    //点击后松开时候的状态
    public static final int STATE_UNPRESS_CLICK = 0X002;
    //点击按下时候的状态
    public static final int STATE_PRESS_CLICK = 0X001;
    //长按按下时候的状态
    public static final int STATE_PRESS_LONG_CLICK = 0x003;
    //长按后松开时候的状态
    public static final int STATE_UNPRESS_LONG_CLICK = 0x004;

    private static final int STATE_IDLE = 0x001;        //空闲状态
    private static final int STATE_PRESS = 0x002;       //按下状态
    private static final int STATE_LONG_PRESS = 0x003;  //长按状态
    private static final int STATE_RECORDERING = 0x004; //录制状态

    private int progress_color = 0xEE16AE16;            //进度条颜色
    private int outside_color = 0xEECCCCCC;             //外圆背景色
    private int inside_color = 0xFFFFFFFF;              //内圆背景色


    //长按后处理的逻辑Runnable
    private LongPressRunnable longPressRunnable;
    //录制视频的Runnable
//    private RecordRunnable recordRunnable;
    //录视频进度条动画
    private ValueAnimator record_anim = ValueAnimator.ofFloat(0, 362);


    private Paint mPaint;
    //进度条宽度
    private float strokeWidth;
    //长按外圆半径变大的Size
    private int outside_add_size;
    //长安内圆缩小的Size
    private int inside_reduce_size;

    //中心坐标
    private float center_X;
    private float center_Y;

    //按钮半径
    private float button_radius;

    //外圆半径
    private float button_outside_radius;
    //内圆半径
    private float button_inside_radius;

    //按钮大小
    private int button_size;
    //录制视频的进度
    private float progress;
    private RectF rectF;
    //录制视频最大时间长度
    private int duration;
    private int recorded_time;

    //按钮回调接口
    private CaptureLisenter captureLisenter;

    private RecordCountDownTimer timer;

    public CaptureButton(Context context) {
        super(context);
    }

    //customize construction method
    public CaptureButton(Context context, int size) {
        super(context);
        this.button_size = size;
        button_radius = size / 2.0f;

        button_outside_radius = button_radius;
        button_inside_radius = button_radius * 0.75f;

        strokeWidth = size / 15;
        outside_add_size = size / 5;
        inside_reduce_size = size / 8;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        progress = 0;

        //init longPress runnable
        longPressRunnable = new LongPressRunnable();
//        recordRunnable = new RecordRunnable();
        //set default state;
        this.state = STATE_NULL;

        this.button_state = BUTTON_STATE_BOTH;

        //set max record duration,default 10*1000
        duration = 10 * 1000;
        center_X = (button_size + outside_add_size * 2) / 2;
        center_Y = (button_size + outside_add_size * 2) / 2;

        rectF = new RectF(
                center_X - (button_radius + outside_add_size - strokeWidth / 2),
                center_Y - (button_radius + outside_add_size - strokeWidth / 2),
                center_X + (button_radius + outside_add_size - strokeWidth / 2),
                center_Y + (button_radius + outside_add_size - strokeWidth / 2));

        timer = new RecordCountDownTimer(duration, duration / 360);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(button_size + outside_add_size * 2, button_size + outside_add_size * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        //外圆（半透明灰色）
        mPaint.setColor(outside_color);
        canvas.drawCircle(center_X, center_Y, button_outside_radius, mPaint);

        //内圆（白色）
        mPaint.setColor(inside_color);
        canvas.drawCircle(center_X, center_Y, button_inside_radius, mPaint);

        //如果状态为按钮长按按下的状态，则绘制录制进度条
        if (state == STATE_PRESS_LONG_CLICK) {
            mPaint.setAntiAlias(true);
            mPaint.setColor(progress_color);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWidth);
            canvas.drawArc(rectF, -90, progress, false, mPaint);
        }
    }

    //Touch_Event_Down时候记录的Y值
    float event_Y;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            //
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() > 1) {
                    break;
                }
                //记录Y值
                event_Y = event.getY();
                //修改当前状态为点击按下
                state = STATE_PRESS_CLICK;
                //当前状态能否录制
                //判断按钮状态是否为可录制状态
                if (!isRecorder &&
                        (button_state == BUTTON_STATE_ONLY_RECORDER ||
                                button_state == BUTTON_STATE_BOTH)) {
                    //同时延长500启动长按后处理的逻辑Runnable
                    postDelayed(longPressRunnable, 500);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (captureLisenter != null
                        && state == STATE_PRESS_LONG_CLICK
                        && (button_state == BUTTON_STATE_ONLY_RECORDER || button_state == BUTTON_STATE_BOTH)) {
                    //记录当前Y值与按下时候Y值的差值，调用缩放回调接口
                    captureLisenter.recordZoom(event_Y - event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                //根据当前按钮的状态进行相应的处理
                handlerUnpressByState();
                break;
        }
        return true;
    }

    //当手指松开按钮时候处理的逻辑
    private void handlerUnpressByState() {
        //移除长按逻辑的Runnable
        removeCallbacks(longPressRunnable);
        //根据当前状态处理
        switch (state) {
            //当前是点击按下
            case STATE_PRESS_CLICK:
                if (captureLisenter != null &&
                        (button_state == BUTTON_STATE_ONLY_CAPTURE || button_state == BUTTON_STATE_BOTH)) {
                    //回调拍照接口
                    captureLisenter.takePictures();
                }
                break;
            //当前是长按按下
            case STATE_PRESS_LONG_CLICK:
                state = STATE_UNPRESS_LONG_CLICK;
                //移除录制视频的Runnable
//                removeCallbacks(recordRunnable);
                //录制结束
                timer.cancel();
                recordEnd();
                break;
        }
        //制空当前状态
        this.state = STATE_NULL;
    }

    private boolean isRecorder = false;

    /**
     * 当前能否录视频
     */
    public void isRecord(boolean record) {
        isRecorder = record;
    }

    /**
     * LongPressRunnable
     */
    private class LongPressRunnable implements Runnable {
        @Override
        public void run() {
            //如果按下后经过500毫秒则会修改当前状态为长按状态
            state = STATE_PRESS_LONG_CLICK;
            //启动按钮动画，外圆变大，内圆缩小
            if (CheckPermission.getRecordState() != CheckPermission.STATE_SUCCESS) {
                if (captureLisenter != null) {
                    captureLisenter.recordError();
                    state = STATE_NULL;
                    return;
                }
            }
            startAnimation(
                    button_outside_radius,
                    button_outside_radius + outside_add_size,
                    button_inside_radius,
                    button_inside_radius - inside_reduce_size
            );
        }
    }

    /**
     * record runnable
     */
//    private class RecordRunnable implements Runnable {
//        @Override
//        public void run() {
//            timer.start();
//            record_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    if (state == STATE_PRESS_LONG_CLICK) {
//                        //更新录制进度
//                        progress = (float) animation.getAnimatedValue();
//                    }
//                    invalidate();
//                }
//            });
//            //如果一直长按到结束，则自动回调录制结束接口
//            record_anim.addListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    super.onAnimationEnd(animation);
//                    if (state == STATE_PRESS_LONG_CLICK) {
//                        recordEnd(true);
//                    }
//                }
//            });
//            record_anim.setInterpolator(new LinearInterpolator());
//            record_anim.setDuration(duration);
//            record_anim.start();
//        }
//    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            handlerUnpressByState();
        }
    }

    /**
     * 录制结束
     */
    private void recordEnd() {
        state = STATE_UNPRESS_LONG_CLICK;
        if (captureLisenter != null) {
            if (recorded_time < 1500) {
                captureLisenter.recordShort(recorded_time);
            } else {
                captureLisenter.recordEnd(recorded_time);
            }
        }
        resetRecordAnim();
//        if (captureLisenter != null) {
//            //录制时间小于一秒时候则提示录制时间过短
//            if (record_anim.getCurrentPlayTime() < 1500 &&) {
//                captureLisenter.recordShort(record_anim.getCurrentPlayTime());
//            } else {
//                if (finish) {
//                    captureLisenter.recordEnd(duration);
//                } else {
//                    captureLisenter.recordEnd(record_anim.getCurrentPlayTime());
//                }
//            }
//        }
        resetRecordAnim();
    }

    private void resetRecordAnim() {
        //取消动画
        record_anim.cancel();
        //重制进度
        progress = 0;
        invalidate();
        //还原按钮初始状态动画
        startAnimation(
                button_outside_radius,
                button_radius,
                button_inside_radius,
                button_radius * 0.75f
        );
    }

    //capture button outside and inside resize animation
    //钮动画，外圆变大，内圆缩小
    private void startAnimation(float outside_start, float outside_end, float inside_start, float inside_end) {
        ValueAnimator outside_anim = ValueAnimator.ofFloat(outside_start, outside_end);
        ValueAnimator inside_anim = ValueAnimator.ofFloat(inside_start, inside_end);
        //外圆
        outside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                button_outside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }

        });
        //内圆
        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                button_inside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        //当动画结束后启动录像Runnable并且回调录像开始接口
        outside_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (state == STATE_PRESS_LONG_CLICK) {
                    if (captureLisenter != null) {
                        captureLisenter.recordStart();
                    }
                    timer.start();
//                    post(recordRunnable);
                }
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playTogether(outside_anim, inside_anim);
        set.setDuration(100);
        set.start();
    }

    /**
     * set record duration
     * 设置最长录制时间
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    //设置回调接口
    public void setCaptureLisenter(CaptureLisenter captureLisenter) {
        this.captureLisenter = captureLisenter;
    }

    //设置按钮功能（拍照和录像）
    public void setButtonFeatures(int state) {
        this.button_state = state;
    }

    private void updateProgress(long millisUntilFinished) {
        recorded_time = (int) (duration - millisUntilFinished);
        progress = 360f - millisUntilFinished / (float) duration * 360f;
        invalidate();
    }

    class RecordCountDownTimer extends CountDownTimer {

        public RecordCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            updateProgress(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            updateProgress(0);
            recordEnd();
            LogUtil.i("onFinish");
        }
    }
}
