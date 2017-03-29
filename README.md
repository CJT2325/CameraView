# JCameraView
## 控件介绍
这是一个模仿微信拍照的Android开源控件，主要的功能有如下：

1. 点击拍照。

2. 前后摄像头的切换。

3. 长按录视频（视频长度为10秒内）。

4. 长按录视频的时候，手指上滑可以放大视频。

5. 录制完视频可以浏览并且重复播放。

6. 可以设置小视频保存路径。

## 示例截图

![image](https://github.com/CJT2325/CameraView/blob/master/assets/65A0.tmp.jpg)

### GIF图略有卡顿

![image](https://github.com/CJT2325/CameraView/blob/master/assets/video.gif)

## 使用步骤
## Android Studio
## 添加下列代码到project gradle
```
allprojects {
    repositories {
        jcenter()
        maven {
            url 'https://dl.bintray.com/cjt/maven'
        }
    }
}
```
## 添加下列代码到module gradle
### 最新版本（0.1.9）更新内容：
```
compile 'cjt.library.wheel:camera:0.1.9' //修复BUG
```
### 旧版本
```
compile 'cjt.library.wheel:camera:0.1.7' //修复无法获取最佳分辨率导致的StackOverFlowError

compile 'cjt.library.wheel:camera:0.1.6' //修复部分机型切换前置摄像头崩溃问题和添加动态权限申请

compile 'cjt.library.wheel:camera:0.1.2' //修复部分机型不支持缩放导致崩溃

compile 'cjt.library.wheel:camera:0.1.1' //修复切换前置摄像头崩溃BUG

compile 'cjt.library.wheel:camera:0.1.0' //修复BUG

compile 'cjt.library.wheel:camera:0.0.9' //添加保持屏幕常亮唤醒状态
<uses-permission android:name="android.permission.WAKE_LOCK"/> //需新增权限

compile 'cjt.library.wheel:camera:0.0.8' //添加手动对焦，对焦提示器，修复切换到前置摄像头崩溃的BUG

compile 'cjt.library.wheel:camera:0.0.7' //修复了长按录视频崩溃的BUG和兼容到Android4.0

compile 'cjt.library.wheel:camera:0.0.3' 
```
## 布局文件中添加
```
<com.cjt2325.cameralibrary.JCameraView
    android:id="@+id/cameraview"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:iconMargin="20dp"
    app:iconWidth="30dp"
    app:iconSrc="@drawable/ic_camera_enhance_black_24dp"/>
```


属性 | 属性说明
---|---
iconWidth | 右上角切换摄像头按钮的大小
iconMargin | 右上角切换摄像头按钮到上、右边距
iconSrc | 右上角切换摄像头按钮图片



### AndroidManifest.xml中添加权限
```
//0.0.9需要新增权限
<uses-permission android:name="android.permission.WAKE_LOCK"/>

<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```
## Activity设置为全屏
```
View decorView = getWindow().getDecorView();
decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
ActionBar actionBar = getSupportActionBar();
actionBar.hide();
```
## 初始化JCameraView控件
```
//(0.1.4+)动态权限获取
CheckPermissionsUtil checkPermissionsUtil = new CheckPermissionsUtil(this);
checkPermissionsUtil.requestAllPermission(this);

mJCameraView = (JCameraView) findViewById(R.id.cameraview);
//(0.0.7+)设置视频保存路径（如果不设置默认为Environment.getExternalStorageDirectory().getPath()）
mJCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath());
//(0.0.8+)设置手动/自动对焦，默认为自动对焦
mJCameraView.setAutoFoucs(false);
mJCameraView.setCameraViewListener(new JCameraView.CameraViewListener() {
    @Override
    public void quit() {
        //返回按钮的点击时间监听
        MainActivity.this.finish();
    }
    @Override
    public void captureSuccess(Bitmap bitmap) {
        //获取到拍照成功后返回的Bitmap
    }
    @Override
    public void recordSuccess(String url) {
        //获取成功录像后的视频路径
    }
});
```
## JCameraView生命周期
```
@Override
protected void onResume() {
    super.onResume();
    mJCameraView.onResume();
}
@Override
protected void onPause() {
    super.onPause();
    mJCameraView.onPause();
}
```
## 备注
该项目可能存在着许多的BUG，并且代码逻辑可能不太严谨，但是从中是我从中还是获得了许多快乐，BUG的解决，完整的运行让我刚到相当有成就感，同时我将它开源出来供大家一起学习，所谓独乐乐不如众乐乐，也可以让快步入社会的我到时候面试的多一份底气。**（最后想请教一下前置摄像头录视频的时候如何将视频水平翻转）**
