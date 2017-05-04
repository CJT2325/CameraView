package com.cjt2325.cameralibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.cjt2325.cameralibrary.lisenter.CaptureLisenter;


/**
 * create by CJT2325
 * 445263848@qq.com.
 */

public class CaptureButton extends View {
    private static final String TAG = "CJT";

    public static final int STATE_NULL = 0x000;
    //public static final int STATE_UNPRESS_CLICK = 0X002;


    public static final int STATE_PRESS_CLICK = 0X001;

    public static final int STATE_PRESS_LONG_CLICK = 0x003;
    public static final int STATE_UNPRESS_LONG_CLICK = 0x004;


    private LongPressRunnable longPressRunnable;
    private RecordRunnable recordRunnable;
    private ValueAnimator record_anim = ValueAnimator.ofFloat(0, 362);

    private int state;

    private Paint mPaint;
    private float strokeWidth;
    private int outside_add_size;
    private int inside_reduce_size;

    private float center_X;
    private float center_Y;
    private float button_radius;

    private float button_outside_radius;
    private float button_inside_radius;


    private int button_size;
    private float progress;
    private RectF rectF;
    private int duration;


    private CaptureLisenter captureLisenter;

    public CaptureButton(Context context) {
        super(context);
    }

    //customize construction method
    public CaptureButton(Context context, int size) {
        super(context);
        this.button_size = size;
        button_radius = size / 2.0f;

        button_outside_radius = button_radius;
        button_inside_radius = button_radius * 0.7f;

        strokeWidth = size / 15;
        outside_add_size = size / 5;
        inside_reduce_size = size / 8;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        progress = 0;

        //init longPress runnable
        longPressRunnable = new LongPressRunnable();
        recordRunnable = new RecordRunnable();
        //set default state;
        this.state = STATE_NULL;

        //set max record duration,default 10*1000
        duration = 10 * 1000;
        center_X = (button_size + outside_add_size * 2) / 2;
        center_Y = (button_size + outside_add_size * 2) / 2;

        rectF = new RectF(
                center_X - (button_radius + outside_add_size - strokeWidth / 2),
                center_Y - (button_radius + outside_add_size - strokeWidth / 2),
                center_X + (button_radius + outside_add_size - strokeWidth / 2),
                center_Y + (button_radius + outside_add_size - strokeWidth / 2));
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
        /**
         * out_side_circle
         */
        mPaint.setColor(0xFFDDDDDD);
        canvas.drawCircle(center_X, center_Y, button_outside_radius, mPaint);

        /**
         * in_side_circle
         */
        mPaint.setColor(0xFFFFFFFF);
        canvas.drawCircle(center_X, center_Y, button_inside_radius, mPaint);

        /**
         * draw Progress bar
         */
        if (state == STATE_PRESS_LONG_CLICK) {
            mPaint.setAntiAlias(true);
            mPaint.setColor(0x9900CC00);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWidth);
            canvas.drawArc(rectF, -90, progress, false, mPaint);
        }
    }

    float event_Y;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                event_Y = event.getY();
                state = STATE_PRESS_CLICK;
                postDelayed(longPressRunnable, 500);
                break;
            case MotionEvent.ACTION_MOVE:
                if (captureLisenter != null) {
                    captureLisenter.recordZoom(event_Y - event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                handlerUnpressByState();
                break;
        }
        return true;
    }

    /**
     * handler MotionEvent.ACTION_UP by state
     */
    private void handlerUnpressByState() {
        removeCallbacks(longPressRunnable);
        switch (state) {
            case STATE_PRESS_CLICK:
                if (captureLisenter != null) {
                    captureLisenter.takePictures();
                }
                break;
            case STATE_PRESS_LONG_CLICK:
                state = STATE_UNPRESS_LONG_CLICK;
                removeCallbacks(recordRunnable);
                recordEnd(false);
                break;
        }
        this.state = STATE_NULL;
    }

    /**
     * LongPressRunnable
     */
    private class LongPressRunnable implements Runnable {
        @Override
        public void run() {
            state = STATE_PRESS_LONG_CLICK;
            startAnimation(button_outside_radius, button_outside_radius + outside_add_size, button_inside_radius,
                    button_inside_radius - inside_reduce_size);
        }
    }

    /**
     * record runnable
     */
    private class RecordRunnable implements Runnable {
        @Override
        public void run() {
            record_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (state == STATE_PRESS_LONG_CLICK) {
                        progress = (float) animation.getAnimatedValue();
                    }
                    invalidate();
                }
            });
            record_anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (state == STATE_PRESS_LONG_CLICK) {
                        recordEnd(true);
                    }
                }
            });
            record_anim.setInterpolator(new LinearInterpolator());
            record_anim.setDuration(duration);
            record_anim.start();
        }
    }

    //record end
    private void recordEnd(boolean finish) {
        state = STATE_UNPRESS_LONG_CLICK;
        if (captureLisenter != null) {
            if (record_anim.getCurrentPlayTime() < 1000 && !finish) {
                captureLisenter.recordShort(record_anim.getCurrentPlayTime());
            } else {
                if (finish) {
                    captureLisenter.recordEnd(duration);
                } else {
                    captureLisenter.recordEnd(record_anim.getCurrentPlayTime());
                }
            }
        }
        record_anim.cancel();
        progress = 0;
        invalidate();
        startAnimation(button_outside_radius, button_radius, button_inside_radius, button_radius * 0.7f);
    }

    //capture button outside and inside resize animation
    private void startAnimation(float outside_start, float outside_end, float inside_start, float inside_end) {
        ValueAnimator outside_anim = ValueAnimator.ofFloat(outside_start, outside_end);
        ValueAnimator inside_anim = ValueAnimator.ofFloat(inside_start, inside_end);
        outside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                button_outside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }

        });
        outside_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (state == STATE_PRESS_LONG_CLICK) {
                    if (captureLisenter != null) {
                        captureLisenter.recordStart();
                    }
                    post(recordRunnable);
                }
            }
        });
        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                button_inside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        outside_anim.setDuration(100);
        inside_anim.setDuration(100);
        outside_anim.start();
        inside_anim.start();
    }

    /**
     * set record duration
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setCaptureLisenter(CaptureLisenter captureLisenter) {
        this.captureLisenter = captureLisenter;
    }
}
