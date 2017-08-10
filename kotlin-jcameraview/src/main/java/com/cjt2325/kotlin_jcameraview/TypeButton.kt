package com.cjt2325.kotlin_jcameraview

import android.content.Context
import android.graphics.*
import android.view.View

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：Kotlin
 * 创建日期：2017/8/10
 * 描    述：拍照后处理的按钮
 * =====================================
 */
class TypeButton(context: Context, type: Int, size: Float) : View(context) {

    private val button_size: Int = size.toInt() //按钮大小
    private val button_type: Int = type //按钮类型

    private val center_X: Float //按钮X轴中心
    private val center_Y: Float //按钮Y轴中心
    private val button_radius: Float //按钮半径

    private val mPaint: Paint
    private val path: Path
    private val strokeWidth: Float

    private val index: Float
    private val rectF: RectF

    init {
        button_radius = button_size / 2.0f
        center_X = size / 2.0f
        center_Y = size / 2.0f

        mPaint = Paint()
        path = Path()
        strokeWidth = size / 50f
        index = button_size / 12f
        rectF = RectF(center_X, center_Y - index, center_X + index * 2, center_Y + index)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(button_size, button_size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //如果类型为取消，则绘制内部为返回箭头
        if (button_type == TYPE_CANCEL) {
            mPaint.isAntiAlias = true
            mPaint.color = 0xEECCCCCC.toInt()
            mPaint.style = Paint.Style.FILL
            canvas.drawCircle(center_X, center_Y, button_radius, mPaint)

            mPaint.color = Color.BLACK
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = strokeWidth

            path.moveTo(center_X - index / 7, center_Y + index)
            path.lineTo(center_X + index, center_Y + index)

            path.arcTo(rectF, 90f, -180f)
            path.lineTo(center_X - index, center_Y - index)
            canvas.drawPath(path, mPaint)
            mPaint.style = Paint.Style.FILL
            path.reset()
            path.moveTo(center_X - index, (center_Y - index * 1.5).toFloat())
            path.lineTo(center_X - index, (center_Y - index / 2.3).toFloat())
            path.lineTo((center_X - index * 1.6).toFloat(), center_Y - index)
            path.close()
            canvas.drawPath(path, mPaint)

        }
        //如果类型为确认，则绘制绿色勾
        if (button_type == TYPE_CONFIRM) {
            mPaint.isAntiAlias = true
            mPaint.color = 0xFFFFFFFF.toInt()
            mPaint.style = Paint.Style.FILL
            canvas.drawCircle(center_X, center_Y, button_radius, mPaint)
            mPaint.isAntiAlias = true
            mPaint.style = Paint.Style.STROKE
            mPaint.color = 0xEE16AE16.toInt()
            mPaint.strokeWidth = strokeWidth

            path.moveTo(center_X - button_size / 6f, center_Y)
            path.lineTo(center_X - button_size / 21.2f, center_Y + button_size / 7.7f)
            path.lineTo(center_X + button_size / 4.0f, center_Y - button_size / 8.5f)
            path.lineTo(center_X - button_size / 21.2f, center_Y + button_size / 9.4f)
            path.close()
            canvas.drawPath(path, mPaint)
        }
    }

    companion object {
        val TYPE_CANCEL = 0x001
        val TYPE_CONFIRM = 0x002
    }
}