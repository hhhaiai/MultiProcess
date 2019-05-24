# PAAS 流量审核SDK

## 版本变动

* **版本号**: `4.3.1|20190524`
* **版本变动**:
        1. 优化部分功能

## 1. 快速集成

### 1.1. 拷贝jar到对应项目中.

如果项目已经集成易观SDK，拷贝`analysys-track-v4.3.0_20190225.jar`到对应项目，配置权限和服务(1.2)，不需要初始化
如果项目未集成易观SDK，拷贝`analysys-track-v4.3.0_20190225.jar`到对应项目，配置权限和服务(1.2)，此时需要进行详细配置(1.3)

### 1.2. 集成权限和配置服务

#### 1.2.1. 权限配置

``` xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.GET_TASKS" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.RESTART_PACKAGES" />
<uses-permission android:name="android.permission.WRITE_SETTINGS"/>
```

#### 1.2.2. 组价声明

``` xml
<receiver android:name="com.analysys.track.receiver.AnalysysReceiver">
    <intent-filter android:priority="9999">
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.USER_PRESENT" />
        <action android:name="android.intent.action.ACTION_POWER_CONNECTED" />
        <action android:name="android.intent.action.ACTION_POWER_DISCONNECTED" />
    </intent-filter>
</receiver>

<service
    android:name="com.analysys.track.service.AnalysysService"
    android:enabled="true"
    android:exported="true"
    android:process=":AnalysysService" />
<service
    android:name="com.analysys.track.service.AnalysysJobService"
    android:permission="android.permission.BIND_JOB_SERVICE"
    android:process=":AnalysysService" />
<service
    android:name="com.analysys.track.service.AnalysysAccessibilityService"
    android:enabled="true"
    android:exported="true"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
    android:process=":AnalysysService">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
</service>
```
#### 1.2.3. 声明APPKEY/CHANNEL（可选）

多渠道打包，可以参考使用该方案声明

``` xml
<meta-data
    android:name="ANALYSYS_APPKEY"
    android:value="9421608fd544a65e" />
<meta-data
    android:name="ANALYSYS_CHANNEL"
    android:value="WanDouJia" />
```

### 1.3. 初始化接口

#### 1.3.1. 初始化接口

``` java
com.analysys.track.AnalysysTracker.init(Context context, String appkey,  String channel);
```
* 注意

appkey允许xml设置和代码设置两种方式，当两种都设置时，优先级`代码设置appkey`优先级高于`XML设置appkey`

#### 1.3.2. 设置debug模式

``` java
com.analysys.track.AnalysysTracker.setDebugMode(Context context, boolean isDebug);
```
* 注意
channel允许xml设置和代码设置两种方式，当两种都设置时，优先级`XML设置appkey`优先级高于`代码设置appkey`

### 1.4. 防止混淆

``` proguard

-keep class com.analysys.track.** {
  public *;
}
-dontwarn com.analysys.track.**

```

