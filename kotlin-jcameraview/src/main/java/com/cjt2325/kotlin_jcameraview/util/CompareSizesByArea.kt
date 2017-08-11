package com.cjt2325.kotlin_jcameraview.util

import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Size
import java.util.*


/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/8/11
 * 描    述：
 * =====================================
 */
class CompareSizesByArea : Comparator<Size> {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun compare(lhs: Size, rhs: Size): Int {
        return java.lang.Long.signum(lhs.width as Long * lhs.height - rhs.width as Long * rhs.height)
    }


}