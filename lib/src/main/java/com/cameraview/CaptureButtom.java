package com.cameraview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

/**
 * 作者: 陈嘉桐 on 2017/2/5
 * 邮箱: 445263848@qq.com.
 */
public class CaptureButtom extends View {

    public final String TAG = "CaptureButtom";

    private Paint mPaint;
    private Context mContext;

    private float btn_center_Y;
    private float btn_center_X;

    private float btn_inside_radius;
    private float btn_outside_radius;
    //半径变化前
    private float btn_before_inside_radius;
    private float btn_before_outside_radius;
    //半径变化后
    private float btn_after_inside_radius;
    private float btn_after_outside_radius;

    private float btn_return_length;
    private float btn_return_X;
    private float btn_return_Y;

    private float btn_left_X, btn_right_X, btn_result_radius;

    //状态
    private int STATE_SELECTED;
    private final int STATE_LESSNESS = 0;
    private final int STATE_KEY_DOWN = 1;
    private final int STATE_CAPTURED = 2;
    private final int STATE_RECORD = 3;
    private final int STATE_PICTURE_BROWSE = 4;
    private final int STATE_RECORD_BROWSE = 5;
    private final int STATE_READYQUIT = 6;
    private final int STATE_RECORDED = 7;

    private float key_down_Y;


    private float progress = 0;
    private LongPressRunnable longPressRunnable = new LongPressRunnable();
    private RecordRunnable recordRunnable = new RecordRunnable();
    private ValueAnimator record_anim = ValueAnimator.ofFloat(0, 360);
    private CaptureListener mCaptureListener;

    public CaptureButtom(Context context) {
        this(context, null);
    }

    public CaptureButtom(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureButtom(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CaptureButtom(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        STATE_SELECTED = STATE_LESSNESS;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = widthSize;
        Log.i(TAG, "measureWidth = " + width);
        int height = (width / 9) * 4;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        btn_center_X = getWidth() / 2;
        btn_center_Y = getHeight() / 2;

        btn_outside_radius = (float) (getWidth() / 9);
        btn_inside_radius = (float) (btn_outside_radius * 0.75);

        btn_before_outside_radius = (float) (getWidth() / 9);
        btn_before_inside_radius = (float) (btn_outside_radius * 0.75);
        btn_after_outside_radius = (float) (getWidth() / 6);
        btn_after_inside_radius = (float) (btn_outside_radius * 0.6);

        btn_return_length = (float) (btn_outside_radius * 0.35);
//        btn_result_radius = 80;
        btn_result_radius = (float) (getWidth() / 9);
        btn_left_X = getWidth() / 2;
        btn_right_X = getWidth() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (STATE_SELECTED == STATE_LESSNESS || STATE_SELECTED == STATE_RECORD) {
            //绘制拍照按钮
            mPaint.setColor(0xFFEEEEEE);
            canvas.drawCircle(btn_center_X, btn_center_Y, btn_outside_radius, mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(btn_center_X, btn_center_Y, btn_inside_radius, mPaint);

            //绘制绿色进度条
            Paint paintArc = new Paint();
            paintArc.setAntiAlias(true);
            paintArc.setColor(0xFF00CC00);
            paintArc.setStyle(Paint.Style.STROKE);
            paintArc.setStrokeWidth(10);
            canvas.drawArc(btn_center_X - (btn_after_outside_radius-5),
                              btn_center_Y - (btn_after_outside_radius-5),
                              btn_center_X + (btn_after_outside_radius-5),
                              btn_center_Y + (btn_after_outside_radius-5), -90, progress, false, paintArc);

            //绘制返回按钮
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
            Path path = new Path();

            btn_return_X = ((getWidth() / 2) - btn_outside_radius) / 2;
            btn_return_Y = (getHeight() / 2 + 10);

            path.moveTo(btn_return_X - btn_return_length, btn_return_Y - btn_return_length);
            path.lineTo(btn_return_X, btn_return_Y);
            path.lineTo(btn_return_X + btn_return_length, btn_return_Y - btn_return_length);
            canvas.drawPath(path, paint);
        } else if (STATE_SELECTED == STATE_RECORD_BROWSE || STATE_SELECTED == STATE_PICTURE_BROWSE) {
            //拍完照或者录完视频需要绘制的内容
            mPaint.setColor(0xFFEEEEEE);
            canvas.drawCircle(btn_left_X, btn_center_Y, btn_result_radius, mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(btn_right_X, btn_center_Y, btn_result_radius, mPaint);


            //绘制左边返回按钮
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            Path path = new Path();

            path.moveTo(btn_left_X - 2, btn_center_Y + 14);
            path.lineTo(btn_left_X + 14, btn_center_Y + 14);
            path.arcTo(new RectF(btn_left_X, btn_center_Y - 14, btn_left_X + 28, btn_center_Y + 14), 90, -180);
            path.lineTo(btn_left_X - 14, btn_center_Y - 14);
            canvas.drawPath(path, paint);


            paint.setStyle(Paint.Style.FILL);
            path.reset();
            path.moveTo(btn_left_X - 14, btn_center_Y - 22);
            path.lineTo(btn_left_X - 14, btn_center_Y - 6);
            path.lineTo(btn_left_X - 23, btn_center_Y - 14);
            path.close();
            canvas.drawPath(path, paint);


            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(0xFF00CC00);
            paint.setStrokeWidth(4);
            path.reset();
            path.moveTo(btn_right_X - 28, btn_center_Y);
            path.lineTo(btn_right_X - 8, btn_center_Y + 22);
            path.lineTo(btn_right_X + 30, btn_center_Y - 20);
            path.lineTo(btn_right_X - 8, btn_center_Y + 18);
            path.close();
            canvas.drawPath(path, paint);
        }
    }


    //事件处理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.i("CaptureButtom", "ACTION_DOWN");
                //空状态
                if (STATE_SELECTED == STATE_LESSNESS) {
                    //返回按钮被按下
                    if (event.getY() > btn_return_Y - 37 &&
                            event.getY() < btn_return_Y + 10 &&
                            event.getX() > btn_return_X - 37 &&
                            event.getX() < btn_return_X + 37) {
                        STATE_SELECTED = STATE_READYQUIT;
                    }
                    //拍照事件按下
                    else if (event.getY() > btn_center_Y - btn_outside_radius &&
                            event.getY() < btn_center_Y + btn_outside_radius &&
                            event.getX() > btn_center_X - btn_outside_radius &&
                            event.getX() < btn_center_X + btn_outside_radius &&
                            event.getPointerCount() == 1
                            ) {
                        key_down_Y = event.getY();
                        STATE_SELECTED = STATE_KEY_DOWN;
                        postCheckForLongTouch(event.getX(), event.getY());
                    }
                } else if (STATE_SELECTED == STATE_RECORD_BROWSE || STATE_SELECTED == STATE_PICTURE_BROWSE) {
                    if (event.getY() > btn_center_Y - btn_result_radius &&
                            event.getY() < btn_center_Y + btn_result_radius &&
                            event.getX() > btn_left_X - btn_result_radius &&
                            event.getX() < btn_left_X + btn_result_radius &&
                            event.getPointerCount() == 1
                            ) {
                        if (mCaptureListener != null) {

                            if (STATE_SELECTED == STATE_RECORD_BROWSE) {
                                mCaptureListener.deleteRecordResult();
                            } else if (STATE_SELECTED == STATE_PICTURE_BROWSE) {
                                mCaptureListener.cancel();
                            }
                        }
                        STATE_SELECTED = STATE_LESSNESS;
                        btn_left_X = btn_center_X;
                        btn_right_X = btn_center_X;
                        invalidate();
                    } else if (event.getY() > btn_center_Y - btn_result_radius &&
                            event.getY() < btn_center_Y + btn_result_radius &&
                            event.getX() > btn_right_X - btn_result_radius &&
                            event.getX() < btn_right_X + btn_result_radius &&
                            event.getPointerCount() == 1
                            ) {
                        if (mCaptureListener != null) {
                            if (STATE_SELECTED == STATE_RECORD_BROWSE) {
                                mCaptureListener.getRecordResult();
                            } else if (STATE_SELECTED == STATE_PICTURE_BROWSE) {
                                mCaptureListener.determine();
                            }
                        }
                        STATE_SELECTED = STATE_LESSNESS;
                        btn_left_X = btn_center_X;
                        btn_right_X = btn_center_X;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.i("CaptureButtom", "ACTION_MOVE");
                if (event.getY() > btn_center_Y - btn_outside_radius &&
                        event.getY() < btn_center_Y + btn_outside_radius &&
                        event.getX() > btn_center_X - btn_outside_radius &&
                        event.getX() < btn_center_X + btn_outside_radius
                        ) {
                }
                if (mCaptureListener != null) {
                    mCaptureListener.scale(event.getY() - key_down_Y);
                }
                break;
            case MotionEvent.ACTION_UP:
                removeCallbacks(longPressRunnable);
//                Log.i("CaptureButtom", "ACTION_UP");
                if (STATE_SELECTED == STATE_READYQUIT) {
                    if (event.getY() > btn_return_Y - 37 &&
                            event.getY() < btn_return_Y + 10 &&
                            event.getX() > btn_return_X - 37 &&
                            event.getX() < btn_return_X + 37) {
                        STATE_SELECTED = STATE_LESSNESS;
                        if (mCaptureListener != null) {
                            mCaptureListener.quit();
                        }
                    }
                } else if (STATE_SELECTED == STATE_KEY_DOWN) {
                    if (event.getY() > btn_center_Y - btn_outside_radius &&
                            event.getY() < btn_center_Y + btn_outside_radius &&
                            event.getX() > btn_center_X - btn_outside_radius &&
                            event.getX() < btn_center_X + btn_outside_radius) {
//                      invalidate();
                        if (mCaptureListener != null) {
                            mCaptureListener.capture();
                        }
                        STATE_SELECTED = STATE_PICTURE_BROWSE;
                    }
                } else if (STATE_SELECTED == STATE_RECORD) {
                    if (record_anim.getCurrentPlayTime() < 500) {
                        STATE_SELECTED = STATE_LESSNESS;
                        Toast.makeText(mContext, "时间太短了", Toast.LENGTH_SHORT).show();
                        progress = 0;
                        invalidate();
                        record_anim.cancel();
                    } else {
                        STATE_SELECTED = STATE_RECORD_BROWSE;
                        removeCallbacks(recordRunnable);
                        Toast.makeText(mContext, "时间：" + record_anim.getCurrentPlayTime(), Toast.LENGTH_SHORT).show();
                        captureAnimation(getWidth() / 5, (getWidth() / 5) * 4);
                        record_anim.cancel();
                        progress = 0;
                        invalidate();
                        if (mCaptureListener != null) {
                            mCaptureListener.rencodEnd();
                        }
                    }
                    if (btn_outside_radius == btn_after_outside_radius && btn_inside_radius == btn_after_inside_radius) {
//                            startAnimation(btn_outside_radius, btn_outside_radius - 40, btn_inside_radius, btn_inside_radius + 20);
                        startAnimation(btn_after_outside_radius, btn_before_outside_radius, btn_after_inside_radius, btn_before_inside_radius);
                    } else {
                        startAnimation(btn_after_outside_radius, btn_before_outside_radius, btn_after_inside_radius, btn_before_inside_radius);
                    }
                }
                break;
        }
        return true;
    }

    public void captureSuccess() {
        captureAnimation(getWidth() / 5, (getWidth() / 5) * 4);
    }

    //长按事件处理
    private void postCheckForLongTouch(float x, float y) {
        longPressRunnable.setPressLocation(x, y);
        postDelayed(longPressRunnable, 500);
    }


    private class LongPressRunnable implements Runnable {
        private int x, y;

        public void setPressLocation(float x, float y) {
            this.x = (int) x;
            this.y = (int) y;
        }

        @Override
        public void run() {
            startAnimation(btn_before_outside_radius,btn_after_outside_radius, btn_before_inside_radius, btn_after_inside_radius);
//            startAnimation(btn_outside_radius, btn_outside_radius + 40, btn_inside_radius, btn_inside_radius - 20);
            STATE_SELECTED = STATE_RECORD;
        }
    }

    private class RecordRunnable implements Runnable {
        @Override
        public void run() {
            if (mCaptureListener != null) {
                mCaptureListener.record();
            }
            record_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (STATE_SELECTED == STATE_RECORD) {
                        progress = (float) animation.getAnimatedValue();
                    }
                    invalidate();
                }
            });
            record_anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (STATE_SELECTED == STATE_RECORD) {
                        STATE_SELECTED = STATE_RECORD_BROWSE;
                        progress = 0;
                        invalidate();
//                        Toast.makeText(mContext, "停止事件", Toast.LENGTH_SHORT).show();
                        captureAnimation(getWidth() / 5, (getWidth() / 5) * 4);
                        if (btn_outside_radius == btn_after_outside_radius && btn_inside_radius == btn_after_inside_radius) {
//                            startAnimation(btn_outside_radius, btn_outside_radius - 40, btn_inside_radius, btn_inside_radius + 20);
                            startAnimation(btn_after_outside_radius, btn_before_outside_radius, btn_after_inside_radius, btn_before_inside_radius);
                        } else {
                            startAnimation(btn_after_outside_radius, btn_before_outside_radius, btn_after_inside_radius, btn_before_inside_radius);
                        }
                        if (mCaptureListener != null) {
                            mCaptureListener.rencodEnd();
                        }
                    }
                }
            });
            record_anim.setInterpolator(new LinearInterpolator());
            record_anim.setDuration(10000);
            record_anim.start();
        }
    }

    private void startAnimation(float outside_start, float outside_end, float inside_start, float inside_end) {

        ValueAnimator outside_anim = ValueAnimator.ofFloat(outside_start, outside_end);
        ValueAnimator inside_anim = ValueAnimator.ofFloat(inside_start, inside_end);
        outside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_outside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }

        });
        outside_anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (STATE_SELECTED == STATE_RECORD) {
                    postDelayed(recordRunnable, 100);
                }
            }
        });
        inside_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_inside_radius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        outside_anim.setDuration(100);
        inside_anim.setDuration(100);
        outside_anim.start();
        inside_anim.start();
    }

    private void captureAnimation(float left, float right) {
//        Toast.makeText(mContext,left+ " = "+right,Toast.LENGTH_SHORT).show();
        Log.i("CaptureButtom", left + "==" + right);
        ValueAnimator left_anim = ValueAnimator.ofFloat(btn_left_X, left);
        ValueAnimator right_anim = ValueAnimator.ofFloat(btn_right_X, right);
        left_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_left_X = (float) animation.getAnimatedValue();
                Log.i("CJT",btn_left_X+"=====");
                invalidate();
            }

        });
        right_anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                btn_right_X = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        left_anim.setDuration(200);
        right_anim.setDuration(200);
        left_anim.start();
        right_anim.start();
    }

    public void setCaptureListener(CaptureListener mCaptureListener) {
        this.mCaptureListener = mCaptureListener;
    }


    //回调接口
    public interface CaptureListener {
        public void capture();

        public void cancel();

        public void determine();

        public void quit();

        public void record();

        public void rencodEnd();

        public void getRecordResult();

        public void deleteRecordResult();

        public void scale(float scaleValue);
    }
}
