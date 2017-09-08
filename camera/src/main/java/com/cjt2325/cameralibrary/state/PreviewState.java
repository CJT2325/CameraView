package com.cjt2325.cameralibrary.state;

import android.graphics.Bitmap;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.cjt2325.cameralibrary.CameraInterface;
import com.cjt2325.cameralibrary.util.LogUtil;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/9/8
 * 描    述：空闲状态
 * =====================================
 */
public class PreviewState implements CameraState {
    private final String TAG = "PreviewState";
    private CameraMachine machine;

    public PreviewState(CameraMachine machine) {
        this.machine = machine;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        CameraInterface.getInstance().doStartPreview(holder, screenProp);
    }

    @Override
    public void shutdown() {
        CameraInterface.getInstance().doStopCamera();
    }


    @Override
    public void foucs(float x, float y, CameraInterface.FocusCallback callback) {
        CameraInterface.getInstance().handleFocus(machine.getContext(), x, y, null);
    }

    @Override
    public void swtich() {
        CameraInterface.getInstance().switchCamera(null);
    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {
        CameraInterface.getInstance().takePicture(new CameraInterface.TakePictureCallback() {
            @Override
            public void captureResult(Bitmap bitmap, boolean isVertical) {

            }
        });
    }

    @Override
    public void record(Surface surface) {
        CameraInterface.getInstance().startRecord(surface, null);
    }

    @Override
    public void stopRecord(boolean isShort) {
        CameraInterface.getInstance().stopRecord(isShort, null);
    }

    @Override
    public void cancle() {
        LogUtil.i("浏览状态下,没有 cancle 事件");
    }

    @Override
    public void confirm() {
        LogUtil.i("浏览状态下,没有 confirm 事件");
    }
}
