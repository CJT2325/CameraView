package com.cjt2325.cameralibrary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * 作者: 陈嘉桐 on 2017/2/28
 * 邮箱: 445263848@qq.com.
 */
public class FoucsView extends View {
    private int foucsView_size;
    private int x;
    private int y;
    private Paint mPaint;

    public FoucsView(Context context, int size) {
        super(context);
        foucsView_size = size;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF00CC00);
        mPaint.setStrokeWidth(1);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    private FoucsView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(foucsView_size, foucsView_size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0+1, 0+1, getWidth(), getHeight(), mPaint);
        canvas.drawLine(0, getHeight() / 2, foucsView_size / 10, getHeight() / 2,mPaint);
        canvas.drawLine(getWidth(), getHeight() / 2, getWidth()-foucsView_size / 10, getHeight() / 2,mPaint);
        canvas.drawLine(getWidth()/2, 0, getWidth()/2, foucsView_size / 10,mPaint);
        canvas.drawLine(getWidth()/2, getHeight(), getWidth()/2, getHeight() -foucsView_size / 10,mPaint);
    }
}
