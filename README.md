# Wave SDK

![wave icon](https://github.com/Cuieney/WaveSDK/blob/master/app/src/main/res/mipmap-hdpi/play.gif)
![wave icon](https://github.com/Cuieney/WaveSDK/blob/master/app/src/main/res/mipmap-hdpi/singer.gif)

## 简介

**WaveSDK**, 让音乐跟上你的步频，根据跑步记录，智能推送歌曲，可高度化定制二次开发，为Android开发者提供了简单,快捷的接口 *跑嗨乐*.

##引入
* Android Studio

将WaveSDK引入

```
dependencies {
    compile 'com.phl.sdk:wavelibrary:1.0.0'
}
```

* Eclipse

建议使用As，方便版本更新。实在不行，只有复制粘贴源码了

##用法
#### 第一步：
在你项目的AndroidManifest.xml文件添加相应的权限

```
    <uses-permission android:name="android.permission.INTERNET" />
    
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />  
```
#### 第二步：
配置build.gradle文件，由于项目依赖GreenDAO需要添加相应的插件

```
 classpath 'org.greenrobot:greendao-gradle-plugin:3.2.0'
```
#### 第三步：
在你的Application中初始化SDK

```
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FeetSdk.getInstance().init(this,"99b36eda-3c91-4715-84ee-480c90ffe82f","demo");
//      FeetSdk.getInstance().setMobileNetWorkAvailable(this,true);//设置移动网络下可以下载
    }
}

```

### 第四步：
调用SDK浮动窗口

```
public class MainActivity extends AppCompatActivity {

    public Button start;
    public FwController feetUiController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = ((Button) findViewById(R.id.start));
        feetUiController = FeetSdk.getFeetUiController();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feetUiController != null) {
                    feetUiController.show(MainActivity.this);
                }
            }
        });

    }
}
```
##目前开放接口如下
###全局接口说明（FeetSdk）
| 方法名 | 备注 |
| ------------ | ------------ |
| init(Context,String, String)|  初始化SDK,配置AppKey，AppChannel |
| setMobileNetWorkAvailable(Context,boolean) |  设置移动网络下可用 |
| setMusicLibrarySize(Context,int) |  设置曲库大小（vip可用） |
| getFeetUiController() |  获取ui控制器 |
<br>暂时只开放目前接口

###FwController接口说明
| 方法名 | 备注 |
| ------------ | ------------ |
| show(Context)|  显示SDK图形用户界面 |
| setLocation(int) |  设置图形界面位置（在show之前设置） |
| remove() |  移除SDK |
| dismiss() |  隐藏图形用户界面 |
| playMusic() | 开启音乐播放（联动设置）  |
| pauseMusic() |  暂停音乐播放开关 |
| stopMusic() |  停止音乐播放 |
| setAutoBpm(boolean) |  设置开启自动检测BPM(默认开启) |
| setBpm(int) |  可以手动设置音乐节奏（需提前调用setAutoBpm（false）） |


###依赖
GreenDAO： <https://github.com/greenrobot/greenDAO>
<br>AndroidAutoLayout:<https://github.com/hongyangAndroid/AndroidAutoLayout>
<br>FileDownloader:<https://github.com/lingochamp/FileDownloader>

###问题提交

Email: <cuieney@163.com> link.




