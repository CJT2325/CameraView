package com.cjt2325.kotlin_jcameraview

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import android.util.Size
import android.view.Surface
import com.cjt2325.kotlin_jcameraview.util.i
import java.util.*


/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/8/10
 * 描    述：
 * =====================================
 */
class CameraNewInterface private constructor() {

    private var mCameraId: String? = null
    private var mTextureView: AutoFitTextureView? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mCameraDevice: CameraDevice? = null
    private var mPreviewSize: Size? = null

    private var mCameraManager: CameraManager? = null
    private var mImageReader: ImageReader? = null
    private val mBackgroundHandler: Handler? = null
    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
    private var mCameraCaptureSession: CameraCaptureSession? = null

    private object Inner {
        var cameraNewInterface = CameraNewInterface()
    }

    companion object {
        fun getInstance(): CameraNewInterface {
            return Inner.cameraNewInterface
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun openCamera(context: Context, textureView: AutoFitTextureView, width: Int, height: Int) {
        mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        mTextureView = textureView
//        setUpCameraOutputs(context, width, height)
//        configureTransform(width, height)
//        try {
//            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
//                throw RuntimeException("Time out waiting to lock camera opening.")
//            }
        mCameraManager?.openCamera("0", mStateCallback, null)
//        .openCamera(mCameraId, mStateCallback, mBackgroundHandler)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        } catch (e: InterruptedException) {
//            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
//        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun stopCamera() {
        if (null == mCameraDevice)
            return
        mCameraDevice?.close()
    }

    private val mCameraCaptureSessionCallback = object : CameraCaptureSession.StateCallback() {

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onConfigured(session: CameraCaptureSession?) {
            if (null == mCameraDevice) {
                i("null == mCameraDevice")
                return
            }
            // 当摄像头已经准备好时，开始显示预览
            mCameraCaptureSession = session
            // 自动对焦
            mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            // 打开闪光灯
            mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            // 显示预览

            if (mPreviewRequestBuilder == null) {
                i("mPreviewRequestBuilder ==null")
            } else {

                var previewRequest: CaptureRequest = mPreviewRequestBuilder?.build() as CaptureRequest
                mCameraCaptureSession?.setRepeatingRequest(previewRequest, null, null)
                i("启动浏览？？？？？？？？？？？")
            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession?) {
            i("onConfigureFailed")
        }
    }

    //打开相机时候的监听器，通过他可以得到相机实例，这个实例可以创建请求建造者
    private val mStateCallback = object : CameraDevice.StateCallback() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onOpened(cameraDevice: CameraDevice) {
            this@CameraNewInterface.mCameraDevice = cameraDevice
            i("相机已经打开")
            takePreview()
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraDevice.close()
            i("相机连接断开")
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onError(cameraDevice: CameraDevice, i: Int) {
            cameraDevice.close()
            this@CameraNewInterface.mCameraDevice = null
            i("相机打开失败")
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun takePreview() {
        // 创建预览需要的CaptureRequest.Builder
        mPreviewRequestBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) as CaptureRequest.Builder
        // 将SurfaceView的surface作为CaptureRequest.Builder的目标
        var sufaceTexture: SurfaceTexture = mTextureView?.surfaceTexture as SurfaceTexture
        sufaceTexture.setDefaultBufferSize(1080, 1920)
        var surface: Surface = Surface(sufaceTexture)
        mPreviewRequestBuilder?.addTarget(surface)
        // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1)
        mCameraDevice?.createCaptureSession(Arrays.asList(surface, mImageReader?.surface), mCameraCaptureSessionCallback, null)
    }


//    @RequiresApi(Build.VERSION_CODES.KITKAT)
//    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
//        mBackgroundHandler?.post(ImageSaver(reader.acquireNextImage(), mFile))
//    }

//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun setUpCameraOutputs(context: Context, width: Int, height: Int) {
//        if (mCameraManager == null)
//            mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//        for (cameraId in mCameraManager?.cameraIdList!!) {
//            val characteristics = mCameraManager?.getCameraCharacteristics(cameraId)
//
//            // We don't use a front facing camera in this sample.
//            val facing = characteristics?.get(CameraCharacteristics.LENS_FACING)
//            if (facing != null && facing === CameraCharacteristics.LENS_FACING_FRONT) {
//                continue
//            }
//
//            val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue
//
//            // For still image captures, we use the largest available size.
//            val largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea())
//            mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/2)
//            mImageReader?.setOnImageAvailableListener(mOnImageAvailableListener, null)
//
//            // Find out if we need to swap dimension to get the preview size relative to sensor
//            // coordinate.
//            val displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation()
//
//            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
//            var swappedDimensions = false
//            when (displayRotation) {
//                Surface.ROTATION_0, Surface.ROTATION_180 -> if (mSensorOrientation === 90 || mSensorOrientation === 270) {
//                    swappedDimensions = true
//                }
//                Surface.ROTATION_90, Surface.ROTATION_270 -> if (mSensorOrientation === 0 || mSensorOrientation === 180) {
//                    swappedDimensions = true
//                }
//                else -> Log.e(TAG, "Display rotation is invalid: " + displayRotation)
//            }
//
//            val displaySize = Point()
//            activity.getWindowManager().getDefaultDisplay().getSize(displaySize)
//            var rotatedPreviewWidth = width
//            var rotatedPreviewHeight = height
//            var maxPreviewWidth = displaySize.x
//            var maxPreviewHeight = displaySize.y
//
//            if (swappedDimensions) {
//                rotatedPreviewWidth = height
//                rotatedPreviewHeight = width
//                maxPreviewWidth = displaySize.y
//                maxPreviewHeight = displaySize.x
//            }
//
//            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
//                maxPreviewWidth = MAX_PREVIEW_WIDTH
//            }
//
//            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
//                maxPreviewHeight = MAX_PREVIEW_HEIGHT
//            }
//
//            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
//            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
//            // garbage capture data.
//            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
//                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
//                    maxPreviewHeight, largest)
//
//            // We fit the aspect ratio of TextureView to the size of preview we picked.
//            val orientation = getResources().getConfiguration().orientation
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                mTextureView.setAspectRatio(
//                        mPreviewSize.getWidth(), mPreviewSize.getHeight())
//            } else {
//                mTextureView.setAspectRatio(
//                        mPreviewSize.getHeight(), mPreviewSize.getWidth())
//            }
//
//            // Check if the flash is supported.
//            val available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
//            mFlashSupported = available ?: false
//
//            mCameraId = cameraId
//            return
//        }
//    }
}