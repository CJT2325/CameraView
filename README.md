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
```
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
iconWidth | 右上角切换摄像头按钮到上、右边距
iconSrc | 右上角切换摄像头按钮图片



### AndroidManifest.xml中添加权限
```
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
mJCameraView = (JCameraView) findViewById(R.id.cameraview);
//设置视频保存路径（如果不设置默认为Environment.getExternalStorageDirectory().getPath()）
mJCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath());
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
