package com.cjt2325.kotlin_jcameraview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/8/16
 * 描    述：
 * =====================================
 */
class QuitButton(context: Context, size: Int) : View(context) {

    private val size: Int
    private var center_X: Int
    private var center_Y: Int
    private var strokeWidth: Float
    private var paint: Paint
    internal var path: Path

    init {
        this.size = size
        center_X = size / 2
        center_Y = size / 2

        strokeWidth = size / 15f

        paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth

        path = Path()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(size, size / 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        path.moveTo(strokeWidth, strokeWidth / 2)
        path.lineTo(center_X.toFloat(), center_Y - strokeWidth / 2)
        path.lineTo(size - strokeWidth, strokeWidth / 2)
        canvas.drawPath(path, paint)
    }
}