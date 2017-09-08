package com.cjt2325.cameralibrary.state;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.cjt2325.cameralibrary.CameraInterface;

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/9/8
 * 描    述：
 * =====================================
 */
public class CameraMachine implements CameraState {


    private Context context;
    private CameraState state;

    private CameraState previewState;       //浏览状态(空闲)
    private CameraState borrowPictureState; //浏览图片
    private CameraState borrowVideoState;   //浏览视频

    public CameraMachine(Context context) {
        this.context = context;
        previewState = new PreviewState(this);
        borrowPictureState = new BorrowPictureState(this);
        borrowVideoState = new BorrowVideoState(this);
        //默认设置为空闲状态
        state = previewState;
    }

    public Context getContext() {
        return context;
    }

    public void setState(CameraState state) {
        this.state = state;
    }

    //获取浏览图片状态
    public CameraState getBorrowPictureState() {
        return borrowPictureState;
    }

    //获取浏览视频状态
    public CameraState getBorrowVideoState() {
        return borrowVideoState;
    }

    //获取空闲状态
    public CameraState getPreviewState() {
        return previewState;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        state.start(holder, screenProp);
    }

    @Override
    public void shutdown() {
        state.shutdown();
    }

    @Override
    public void foucs(float x, float y, CameraInterface.FocusCallback callback) {

    }

    @Override
    public void swtich() {
        state.swtich();
    }

    @Override
    public void restart() {
        state.restart();
    }

    @Override
    public void capture() {
        state.capture();
    }

    @Override
    public void record(Surface surface) {
        state.record(surface);
    }

    @Override
    public void stopRecord(boolean isShort) {
        state.stopRecord(isShort);
    }

    @Override
    public void cancle() {
        state.cancle();
    }

    @Override
    public void confirm() {
        state.confirm();
    }
}
