# JCameraView
这是一个模仿微信拍照的Android开源控件

- 点击拍照

- 长按录视频（视频长度可设置）

- 长按录视频的时候，手指上滑可以放大视频

- 录制完视频可以浏览并且重复播放

- 前后摄像头的切换

- 可以设置小视频保存路径

## 示例截图

![image](https://github.com/CJT2325/CameraView/blob/master/assets/screenshot_0.jpg)
![image](https://github.com/CJT2325/CameraView/blob/master/assets/screenshot_1.jpg)
![image](https://github.com/CJT2325/CameraView/blob/master/assets/screenshot_2.jpg)

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

### 最新版本（1.0.2）更新内容：
```
compile 'cjt.library.wheel:camera:1.0.2'
//TextureView替换VideoView
//根据手机拍照方向旋转图片（仅后置摄像头）
```
### 旧版本
```
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
```
    //1.0.0
    <com.cjt2325.cameralibrary.JCameraView
        android:id="@+id/jcameraview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:duration_max="10000"
        app:iconMargin="20dp"
        app:iconSize="30dp"
        app:iconSrc="@drawable/ic_camera_enhance_black_24dp"/>

    //旧版本
    <com.cjt2325.cameralibrary.JCameraView
        android:id="@+id/cameraview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:iconMargin="20dp"
        app:iconWidth="30dp"
        app:iconSrc="@drawable/ic_camera_enhance_black_24dp"/>
```
### （1.0.0）
属性 | 属性说明
---|---
iconSize | 右上角切换摄像头按钮的大小
iconMargin | 右上角切换摄像头按钮到上、右边距
iconSrc | 右上角切换摄像头按钮图片
duration_max | 设置最长录像时间

### （旧版本）
属性 | 属性说明
---|---
iconWidth | 右上角切换摄像头按钮的大小
iconMargin | 右上角切换摄像头按钮到上、右边距
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
## 初始化JCameraView控件
```
        //1.0.0
        jCameraView = (JCameraView) findViewById(R.id.jcameraview);
        /**
         * 设置视频保存路径
         */
        jCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");
        /**
         * JCameraView监听
         */
        jCameraView.setJCameraLisenter(new JCameraLisenter() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                /**
                 * 获取图片bitmap
                 */
                Log.i("JCameraView", "bitmap = " + bitmap.getWidth());
            }

            @Override
            public void recordSuccess(String url) {
                /**
                 * 获取视频路径
                 */
                Log.i("CJT", "url = " + url);
            }

            @Override
            public void quit() {
                /**
                 * 退出按钮
                 */
                MainActivity.this.finish();
            }
        });
```

```
        //旧版本
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
## LICENSE
Copyright 2017 CJT2325

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
   
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
