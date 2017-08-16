package com.cjt2325.kotlin_jcameraview

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.Gravity
import android.view.TextureView
import android.widget.FrameLayout
import android.widget.ImageView
import com.cjt2325.kotlin_jcameraview.listener.CaptureListener
import com.cjt2325.kotlin_jcameraview.listener.QuitListener
import com.cjt2325.kotlin_jcameraview.listener.TypeListener
import com.cjt2325.kotlin_jcameraview.util.getScreenHeight
import com.cjt2325.kotlin_jcameraview.util.getScreenWidth
import com.cjt2325.kotlin_jcameraview.util.i

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：Kotlin
 * 创建日期：2017/8/10
 * 描    述：
 * =====================================
 */
class JCameraView : FrameLayout, TextureView.SurfaceTextureListener {

    var textureView: AutoFitTextureView
    var captureLayout: CaptureLayout
    var switchCamera: ImageView
    var switchFlash: ImageView

    init {
        textureView = AutoFitTextureView(context)
        captureLayout = CaptureLayout(context)
        switchCamera = ImageView(context)
        switchFlash = ImageView(context)
    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttr(attrs)
        initView()
        initListener()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initAttr(attrs)
        initView()
        initListener()
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
        textureView.layoutParams = textureView_param
        textureView.surfaceTextureListener = this

        val switchcamera_param = FrameLayout.LayoutParams(60, 60)
        switchcamera_param.gravity = Gravity.RIGHT
        switchcamera_param.setMargins(16, 16, 16, 16)
        switchCamera.layoutParams = switchcamera_param
        switchCamera.setImageResource(R.drawable.ic_camera)

        val switchflash_param = FrameLayout.LayoutParams(60, 60)
        switchflash_param.gravity = Gravity.RIGHT
        switchflash_param.setMargins(16, 16, 48 + 60, 16)
        switchFlash.layoutParams = switchflash_param
        switchFlash.setImageResource(R.drawable.ic_brightness)

        this.addView(textureView)
        this.addView(captureLayout)
        this.addView(switchCamera)
        this.addView(switchFlash)

    }

    fun initListener() {
        captureLayout.mQuitListener = object : QuitListener {
            override fun quit() {
                i("JCameraView : quit")
            }
        }
        captureLayout.mTypeListener = object : TypeListener {
            override fun confirm() {
                i("JCameraView : confirm")
            }

            override fun cancle() {
                i("JCameraView : cancle")
            }
        }
        captureLayout.mCaptureListener = object : CaptureListener {
            override fun error(error: String) {
            }

            override fun recorderZoom() {

            }

            override fun caputre() {
                CameraNewInterface.getInstance().takePicture()
                i("JCameraView : caputre")
            }

            override fun recorderEnd(time: Long) {
                i("JCameraView : recorderEnd")
            }

            override fun recorderShort() {
                i("JCameraView : recorderShort")
            }

            override fun recorderStart() {
                i("JCameraView : recorderStart")
            }

        }
    }

    fun onResume() {
        i("JCameraView onResume")
    }

    fun onPause() {
        i("JCameraView onPause")
    }


    //TextureView监听
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        //锁屏再解锁不会回调onSurfaceTextureAvailable
        //打开Camera并启动浏览
        i("width = " + getScreenWidth(context) + " height = " + getScreenHeight(context));
        i("Texturewidth = " + width + " Textureheight = " + height);
        CameraNewInterface.Companion.getInstance().openCamera(context, textureView, width, height);
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        //锁屏会回调销毁(onSurfaceTextureDestroyed)
        i("onSurfaceTextureDestroyed")
        CameraNewInterface.Companion.getInstance().stopCamera()
        return true
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        i("onSurfaceTextureSizeChanged")
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
//        i("onSurfaceTextureUpdated")
    }

}