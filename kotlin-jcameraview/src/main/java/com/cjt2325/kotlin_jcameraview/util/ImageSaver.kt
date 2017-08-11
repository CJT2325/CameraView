package com.cjt2325.kotlin_jcameraview.util

import android.media.Image
import android.os.Build
import android.support.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：1.1.4
 * 创建日期：2017/8/11
 * 描    述：
 * =====================================
 */
class ImageSaver(private val mImage: Image, private val mFile: File) : Runnable {
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun run() {
        val buffer = mImage.getPlanes()[0].getBuffer()
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(mFile)
            output!!.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            mImage.close()
            if (null != output) {
                try {
                    output!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

}