package com.cjt2325.kotlin_jcameraview

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.TextureView
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：Kotlin
 * 创建日期：2017/8/10
 * 描    述：
 * =====================================
 */
class JCameraView : FrameLayout {

    var textureView: TextureView
    var captureLayout: CaptureLayout
    var switchCamera: ImageView
    var switchFlash: ImageView

    init {
        textureView = TextureView(context)
        captureLayout = CaptureLayout(context)
        switchCamera = ImageView(context)
        switchFlash = ImageView(context)
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttr(attrs)
        initView()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initAttr(attrs)
        initView()
    }

    fun initAttr(attrs: AttributeSet?) {

    }

    fun initView() {
        //CaptureLayout
        val captureLayout_param = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        captureLayout_param.gravity = Gravity.BOTTOM
        captureLayout.layoutParams = captureLayout_param

        //TextureView
        val textureView_param = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
//        textureView_param.gravity = Gravity.CENTER
        textureView.setBackgroundColor(0xff888888.toInt())
        textureView.layoutParams = textureView_param


        this.addView(textureView)
        this.addView(captureLayout)
    }
}