package com.cjt2325.cameralibrary;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 445263848@qq.com.
 */

public class CameraParamUtil {
    private static final String TAG = "JCameraView";
    private CameraSizeComparator sizeComparator = new CameraSizeComparator();
    private static CameraParamUtil cameraParamUtil = null;

    private CameraParamUtil() {

    }

    public static CameraParamUtil getInstance() {
        if (cameraParamUtil == null) {
            cameraParamUtil = new CameraParamUtil();
            return cameraParamUtil;
        } else {
            return cameraParamUtil;
        }
    }

    public Size getPreviewSize(List<Camera.Size> list, int th, float rate) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Size s : list) {
            if ((s.width > th) && equalRate(s, rate)) {
                Log.i(TAG, "MakeSure Preview :w = " + s.width + " h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            return getBestSize(list, rate);
        } else {
            return list.get(i);
        }
    }

    public Size getPictureSize(List<Camera.Size> list, int th, float rate) {
        Collections.sort(list, sizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.width > th) && equalRate(s, rate)) {
                Log.i(TAG, "MakeSure Picture :w = " + s.width + " h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            return getBestSize(list, rate);
        } else {
            return list.get(i);
        }
    }

    public Size getBestSize(List<Camera.Size> list, float rate) {
        float previewDisparity = 100;
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            Size cur = list.get(i);
            float prop = (float) cur.width / (float) cur.height;
            if (Math.abs(rate - prop) < previewDisparity) {
                previewDisparity = Math.abs(rate - prop);
                index = i;
            }
        }
        return list.get(index);
    }


    public boolean equalRate(Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.2) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isSupportedFocusMode(List<String> focusList, String focusMode) {
        for (int i = 0; i < focusList.size(); i++) {
            if (focusMode.equals(focusList.get(i))) {
                Log.i(TAG, "FocusMode supported " + focusMode);
                return true;
            }
        }
        Log.i(TAG, "FocusMode not supported " + focusMode);
        return false;
    }

    public boolean isSupportedPictureFormats(List<Integer> supportedPictureFormats, int jpeg) {
        for (int i = 0; i < supportedPictureFormats.size(); i++) {
            if (jpeg == supportedPictureFormats.get(i)) {
                Log.i(TAG, "Formats supported " + jpeg);
                return true;
            }
        }
        Log.i(TAG, "Formats not supported " + jpeg);
        return false;
    }

    public class CameraSizeComparator implements Comparator<Size> {
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }
}
