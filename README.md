# CameraView
Custom camera view（模仿微信拍照控件）

![image](http://www.materialstyle.cn/video.gif)

### 使用步骤
### Android Studio
#### 添加下列代码到project gradle
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
#### 添加下列代码到module gradle
```
compile 'cjt.library.wheel:camera:0.0.1'
```
#### 布局文件中添加
```
<com.cjt2325.cameralibrary.JCameraView
    android:id="@+id/cameraview"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
#### AndroidManifest.xml中添加权限
```
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```
#### Activity设置全屏
```
View decorView = getWindow().getDecorView();
decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
ActionBar actionBar = getSupportActionBar();
actionBar.hide();
```
#### 初始化控件
```
private JCameraView mJCameraView;
mJCameraView = (JCameraView) findViewById(R.id.cameraview);
mJCameraView.setCameraViewListener(new JCameraView.CameraViewListener() {
    @Override
    public void quit() {
        MainActivity.this.finish();
    }
    
    @Override
    public void captureSuccess(Bitmap bitmap) {
        Toast.makeText(MainActivity.this, "获取到照片Bitmap :" + bitmap.getHeight(), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void recordSuccess(String url) {
        Toast.makeText(MainActivity.this, "获取到视频路径:" + url, Toast.LENGTH_SHORT).show();
    }
});
```
#### 生命周期
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
