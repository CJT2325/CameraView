package com.cjt2325.kotlin_jcameraview

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.support.annotation.NonNull
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

    private object Inner {
        var cameraNewInterface = CameraNewInterface()
    }

    companion object {
        fun getInstance(): CameraNewInterface {
            return Inner.cameraNewInterface
        }
    }

    private var preview_width: Int = 0
    private var preview_height: Int = 0

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun openCamera(context: Context, textureView: AutoFitTextureView, width: Int, height: Int) {
        mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        mTextureView = textureView

        preview_width = width
        preview_height = height
//        setUpCameraOutputs(context, width, height)
//        configureTransform(width, height)
//        try {
//            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
//                throw RuntimeException("Time out waiting to lock camera opening.")
//            }
        setUpCameraOutputs(context, 0, 0)
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
            mCaptureSession = session
            // 自动对焦
            mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            // 打开闪光灯
            mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            // 显示预览

            if (mPreviewRequestBuilder == null) {
                i("mPreviewRequestBuilder ==null")
            } else {
                var previewRequest: CaptureRequest = mPreviewRequestBuilder?.build() as CaptureRequest
                mCaptureSession?.setRepeatingRequest(previewRequest, null, null)
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
        sufaceTexture.setDefaultBufferSize(1280, 720)
        var surface: Surface = Surface(sufaceTexture)
        mPreviewRequestBuilder?.addTarget(surface)
        // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求

        mCameraDevice?.createCaptureSession(Arrays.asList(surface, mImageReader?.surface), mCameraCaptureSessionCallback, null)
    }


    fun takePicture() {
        lockFocus()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun lockFocus() {
        i("lockFocus")
        // This is how to tell the camera to lock focus.
        mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
        // Tell #mCaptureCallback to wait for the lock.
        mState = STATE_WAITING_LOCK
        mCaptureSession?.capture(mPreviewRequestBuilder?.build(), mCaptureCallback, null)
    }


//    @RequiresApi(Build.VERSION_CODES.KITKAT)
//    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
//        mBackgroundHandler?.post(ImageSaver(reader.acquireNextImage(), mFile))
//    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setUpCameraOutputs(context: Context, width: Int, height: Int) {
        if (mCameraManager == null)
            mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        for (cameraId in mCameraManager?.cameraIdList!!) {
            val characteristics = mCameraManager?.getCameraCharacteristics(cameraId)

            // We don't use a front facing camera in this sample.
            val facing = characteristics?.get(CameraCharacteristics.LENS_FACING)
//            if (facing != null && facing === CameraCharacteristics.LENS_FACING_FRONT) {
//                continue
//            }

            val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue

            var size = map.getOutputSizes(ImageFormat.JPEG)
//            for (i in size) {
//                var rate = i.width.toFloat() / i.height
//                i("previewWidth = " + i.width + " previewHeight = " + i.height + "rate = " + rate)
//            }
            // For still image captures, we use the largest available size.
//            val largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), CompareSizesByArea())
            mImageReader = ImageReader.newInstance(1280, 720, ImageFormat.JPEG, 1)
            mImageReader?.setOnImageAvailableListener(mOnImageAvailableListener, null)

            // Find out if we need to swap dimension to get the preview size relative to sensor
            // coordinate.
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
        }
    }

    private val STATE_PREVIEW = 0

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private val STATE_WAITING_LOCK = 1

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private val STATE_WAITING_PRECAPTURE = 2

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private val STATE_WAITING_NON_PRECAPTURE = 3

    /**
     * Camera state: Picture was taken.
     */
    private val STATE_PICTURE_TAKEN = 4

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private val MAX_PREVIEW_WIDTH = 1920

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private val MAX_PREVIEW_HEIGHT = 1080

    var mState: Int? = null

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun process(result: CaptureResult) {
            i("process")
            when (mState) {
                STATE_PREVIEW -> {
                }// We have nothing to do when the camera preview is working normally.
                STATE_WAITING_LOCK -> {
                    i("STATE_WAITING_LOCK")
                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afState == null) {
                        captureStillPicture()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeState == null || aeState === CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN
                            captureStillPicture()
                        } else {
                            runPrecaptureSequence()
                        }
                    }
                }
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState === CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState === CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState !== CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        override fun onCaptureProgressed(@NonNull session: CameraCaptureSession,
                                         @NonNull request: CaptureRequest,
                                         @NonNull partialResult: CaptureResult) {
            process(partialResult)
        }

        override fun onCaptureCompleted(@NonNull session: CameraCaptureSession,
                                        @NonNull request: CaptureRequest,
                                        @NonNull result: TotalCaptureResult) {
            process(result)
        }

    }


    private fun captureStillPicture() {
        if (null == mCameraDevice) {
            return
        }
        // This is the CaptureRequest.Builder that we use to take a picture.
        val captureBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureBuilder?.addTarget(mImageReader?.surface)

        // Use the same AE and AF modes as the preview.
        captureBuilder?.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        if (captureBuilder != null) {
            setAutoFlash(captureBuilder)
        }

        // Orientation
//        val rotation = activity!!.getWindowManager().getDefaultDisplay().getRotation()
//        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))

        val CaptureCallback = object : CameraCaptureSession.CaptureCallback() {

            override fun onCaptureCompleted(session: CameraCaptureSession,
                                            request: CaptureRequest,
                                            result: TotalCaptureResult) {
//                showToast("Saved: " + mFile)
//                Log.d(TAG, mFile.toString())
//                unlockFocus()
            }
        }

        mCaptureSession?.stopRepeating()
        mCaptureSession?.capture(captureBuilder?.build(), CaptureCallback, null)
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
//        if (mFlashSupported) {
        requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
//        }
    }

    private fun runPrecaptureSequence() {
        // This is how to tell the camera to trigger.
        mPreviewRequestBuilder?.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
        // Tell #mCaptureCallback to wait for the precapture sequence to be set.
        mState = STATE_WAITING_PRECAPTURE
        mCaptureSession?.capture(mPreviewRequestBuilder?.build(), mCaptureCallback,
                mBackgroundHandler)
    }

    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        i("takePicture Success " + reader.height)
//        mBackgroundHandler?.post(ImageSaver(reader.acquireNextImage(), mFile))
    }
}