# JCameraView（1.1.9）  
## 使用方法
- Gradle依赖：  compile 'cjt.library.wheel:camera :1.1.9'
- 引用源码  ：  clone源码后 引入lib -> camera
- 尝试运行  ：  导入源码 运行cameraapplication这个model（！！！）
- 扫描二维码：  apk比较旧

 [![Download](https://api.bintray.com/packages/cjt/maven/cameraView/images/download.svg)](https://bintray.com//cjt/maven/cameraView/_latestVersion) [![API 14+](https://img.shields.io/badge/API-14%2B-green.svg)](https://github.com/CJT2325/CameraView)

这是一个模仿微信拍照的Android开源控件

- 点击拍照

- 10s的视频大概1.9M左右

- 长按录视频（视频长度可设置）

- 长按录视频的时候，手指上滑可以放大视频

- 录制完视频可以浏览并且重复播放

- 前后摄像头的切换

- 可以设置小视频保存路径

## 示例截图

![image](https://github.com/CJT2325/CameraView/blob/master/assets/screenshot_0.jpg)
![image](https://github.com/CJT2325/CameraView/blob/master/assets/screenshot_1.jpg)
![image](https://github.com/CJT2325/CameraView/blob/master/assets/screenshot_2.jpg)

## GIF图

![image](https://github.com/CJT2325/CameraView/blob/master/assets/video.gif)

## 使用步骤（Android Studio）

**添加下列代码到 module gradle**

> 最新版本（1.1.9）更新内容：
```gradle
compile 'cjt.library.wheel:camera:1.1.9'
//添加闪关灯,自定义左右按钮图标资源
```
**如果获取依赖失败则添加下列代码到 project gradle**
```gradle
allprojects {
    repositories {
        jcenter()
        maven {
            url 'https://dl.bintray.com/cjt/maven'
        }
    }
}
```

### 旧版本
```gradle
compile 'cjt.library.wheel:camera:1.1.6'
//修复BUG
```

```gradle
compile 'cjt.library.wheel:camera:1.1.3'
//fix bug
```

```gradle
compile 'cjt.library.wheel:camera:1.1.1'
//fix bug

compile 'cjt.library.wheel:camera:1.0.9'
//fix bug

compile 'cjt.library.wheel:camera:1.0.5'
//浏览界面能使用两根手指进行缩放
//切换摄像头的按钮会根据手持手机方向进行旋转
//修复内存泄露

compile 'cjt.library.wheel:camera:1.0.4'
//换回VideoView
//摄像上滑放大

compile 'cjt.library.wheel:camera:1.0.2'
//TextureView替换VideoView
//根据手机拍照方向旋转图片（仅后置摄像头）

compile 'cjt.library.wheel:camera:1.0.0'
//代码重构
//修复频繁切换摄像头崩溃的问题
//修复获取不到supportedVideoSizes的问题
//可以设置最长录像时间
//修复按钮错乱BUG

compile 'cjt.library.wheel:camera:0.1.9' //修复BUG

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
```xml
//1.0.0+
<com.cjt2325.cameralibrary.JCameraView
    android:id="@+id/jcameraview"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:duration_max="10000"
    app:iconMargin="20dp"
    app:iconSize="30dp"
    app:iconSrc="@drawable/ic_camera_enhance_black_24dp"/>
```
### （1.0.0+）
属性 | 属性说明
---|---
iconSize | 右上角切换摄像头按钮的大小
iconMargin | 右上角切换摄像头按钮到上、右边距
iconSrc | 右上角切换摄像头按钮图片
iconLeft | 左边按钮图片资源（1.1.9+）
iconRight | 右边按钮图片资源（1.1.9+）
duration_max | 设置最长录像时间（毫秒）

### AndroidManifest.xml中添加权限
```xml
<uses-permission android:name="android.permission.FLASHLIGHT" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
```
### Activity全屏设置
```java
if (Build.VERSION.SDK_INT >= 19) {
    View decorView = getWindow().getDecorView();
    decorView.setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
} else {
    View decorView = getWindow().getDecorView();
    int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
    decorView.setSystemUiVisibility(option);
}
```
### 初始化JCameraView控件
```java
//1.1.1
jCameraView = (JCameraView) findViewById(R.id.jcameraview);

//设置视频保存路径
jCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");

//设置只能录像或只能拍照或两种都可以（默认两种都可以）
jCameraView.setFeatures(JCameraView.BUTTON_STATE_BOTH);

//设置视频质量
jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);

//JCameraView监听
jCameraView.setErrorLisenter(new ErrorLisenter() {
     @Override
     public void onError() {
          //打开Camera失败回调
          Log.i("CJT", "open camera error");
     }
     @Override
     public void AudioPermissionError() {
          //没有录取权限回调
          Log.i("CJT", "AudioPermissionError");
     }
});

jCameraView.setJCameraLisenter(new JCameraLisenter() {
    @Override
    public void captureSuccess(Bitmap bitmap) {
        //获取图片bitmap
        Log.i("JCameraView", "bitmap = " + bitmap.getWidth());
    }
    @Override
    public void recordSuccess(String url,Bitmap firstFrame) {
        //获取视频路径
        Log.i("CJT", "url = " + url);
     }
    //@Override
    //public void quit() {
    //    (1.1.9+后用左边按钮的点击事件替换)
    //}
});
//左边按钮点击事件
jCameraView.setLeftClickListener(new ClickListener() {
    @Override
    public void onClick() {
        CameraActivity.this.finish();
    }
});
//右边按钮点击事件
jCameraView.setRightClickListener(new ClickListener() {
    @Override
        Toast.makeText(CameraActivity.this,"Right",Toast.LENGTH_SHORT).show();
    }
});
```
### JCameraView生命周期
```java
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

### APK Demo
扫码下载

![image](https://github.com/CJT2325/CameraView/blob/master/assets/QRcode.png)

二维码显示不出请点击 [下载Demo](http://fir.im/8xnw)



### LICENSE
Copyright 2017 CJT2325

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
   
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
