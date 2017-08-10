package com.cjt2325.kotlin_jcameraview.util

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/8/10
 * 描    述：获取屏幕宽高
 * =====================================
 */

fun getScreenWidth(context: Context): Int {
    val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val outMetrics = DisplayMetrics()
    manager.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.widthPixels
}

fun getScreenHeight(context: Context): Int {
    val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val outMetrics = DisplayMetrics()
    manager.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.heightPixels
}