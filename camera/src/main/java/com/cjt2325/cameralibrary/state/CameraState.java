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
public interface CameraState {

    void start(SurfaceHolder holder, float screenProp);

    void shutdown();

    void foucs(float x, float y, CameraInterface.FocusCallback callback);

    void swtich();

    void restart();

    void capture();

    void record(Surface surface);

    void stopRecord(boolean isShort);

    void cancle();

    void confirm();
}
