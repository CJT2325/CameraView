package com.cjt2325.kotlin_jcameraview

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.cjt2325.kotlin_jcameraview.util.getScreenWidth

/**
 * =====================================
 * 作    者: 陈嘉桐
 * 版    本：Kotlin
 * 创建日期：2017/8/10
 * 描    述：
 * =====================================
 */
class CaptureLayout(context: Context) : FrameLayout(context) {

    private var layout_width: Int
    private var layout_height: Int
    private var button_size: Float

    private var captureButton: CaptureButton
    private var cancleButton: TypeButton
    private var confirmButton: TypeButton
    private var textView_tip: TextView

    init {

        layout_width = getScreenWidth(context) //CaptureLayout宽度
        button_size = layout_width / 4.5f   //按钮大小
        layout_height = (button_size + (button_size / 5) * 2 + 100).toInt() //CaptureLayout高度

        //拍照按钮（CaptureButton）
        captureButton = CaptureButton(context, button_size)
        val capture_param = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        capture_param.gravity = Gravity.CENTER
        captureButton.layoutParams = capture_param

        //取消按钮（TypeButton）
        cancleButton = TypeButton(context, TypeButton.Companion.TYPE_CANCEL, button_size)
        val cancle_param = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        cancle_param.gravity = Gravity.CENTER_VERTICAL
        cancle_param.setMargins(layout_width / 6, 0, 0, 0)
        cancleButton.layoutParams = cancle_param
//
        //确定按钮(TypeButton)
        confirmButton = TypeButton(context, TypeButton.Companion.TYPE_CONFIRM, button_size)
        val confirm_param = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        confirm_param.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        confirm_param.setMargins(0, 0, layout_width / 6, 0)
        confirmButton.layoutParams = confirm_param

        //内容提示
        textView_tip = TextView(context)
        textView_tip.text = "Kotlin JCamera"
        val tip_param = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT)
        tip_param.gravity = Gravity.CENTER_HORIZONTAL
        textView_tip.layoutParams = tip_param

        this.addView(captureButton)
        this.addView(cancleButton)
        this.addView(confirmButton)
        this.addView(textView_tip)


        cancleButton.visibility = View.INVISIBLE
        confirmButton.visibility = View.INVISIBLE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(layout_width, layout_height)
    }
}