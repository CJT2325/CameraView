package com.cameraview;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * 作者: 陈嘉桐 on 2017/2/12
 * 邮箱: 445263848@qq.com.
 */
public class ImageUtil {
    /*
    旋转图片
     */
    public static Bitmap getRotateBitmap(Bitmap bitmap, float rotateDegree) {
        Matrix matrix = new Matrix();
        matrix.setRotate(rotateDegree);
        Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        return rotateBitmap;
    }
}
