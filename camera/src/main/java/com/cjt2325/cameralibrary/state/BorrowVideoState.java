package com.cjt2325.cameralibrary.state;

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
public class BorrowVideoState implements CameraState {
    private final String TAG = "BorrowVideoState";
    private CameraMachine machine;

    public BorrowVideoState(CameraMachine machine) {
        this.machine = machine;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void foucs(float x, float y, CameraInterface.FocusCallback callback) {

    }


    @Override
    public void swtich() {

    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {

    }

    @Override
    public void record(Surface surface) {

    }

    @Override
    public void stopRecord(boolean isShort) {

    }

    @Override
    public void cancle() {

    }

    @Override
    public void confirm() {

    }
}
