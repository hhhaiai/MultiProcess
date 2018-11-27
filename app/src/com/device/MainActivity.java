package com.device;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import com.analysys.dev.internal.utils.AESUtils;
import com.analysys.dev.internal.utils.LL;
import com.analysys.dev.internal.utils.Utils;
import com.analysys.dev.internal.utils.ZipUtils;
import java.util.List;
import java.util.TimeZone;

public class MainActivity extends Activity {

  String TAG = "wang";

  Context context = MainActivity.this;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    registerPermission();
  }

  private void registerPermission() {
    if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, 1);
    } else {

    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1) {
    }
  }

  @TargetApi(Build.VERSION_CODES.N) public void insert(View view) {
    SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
    String type;
    for (int i = 0; i < sensorList.size(); i++) {
      Sensor s = sensorList.get(i);

      switch (s.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
          type = "加速度传感器";
          break;
        case Sensor.TYPE_MAGNETIC_FIELD:
          type = "磁场传感器";
          break;
        case Sensor.TYPE_ORIENTATION:
          type = "方向传感器";
          break;
        case Sensor.TYPE_GYROSCOPE:
          type = "陀螺仪传感器";
          break;
        case Sensor.TYPE_LIGHT:
          type = "光线传感器";
          break;
        case Sensor.TYPE_PRESSURE:
          type = "压力传感器";
          break;
        case Sensor.TYPE_TEMPERATURE:
          type = "温度传感器";
          break;
        case Sensor.TYPE_PROXIMITY:
          type = "接近传感器";
          break;
        case Sensor.TYPE_GRAVITY:
          type = "重力传感器";
          break;
        case Sensor.TYPE_LINEAR_ACCELERATION:
          type = "线性加速度传感器";
          break;
        case Sensor.TYPE_ROTATION_VECTOR:
          type = "旋转矢量传感器";
          break;
        case Sensor.TYPE_RELATIVE_HUMIDITY:
          type = "相对湿度传感器";
          break;
        case Sensor.TYPE_AMBIENT_TEMPERATURE:
          type = "环境温度传感器";
          break;
        case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
          type = "磁场传感器(未经校准)";
          break;
        case Sensor.TYPE_GAME_ROTATION_VECTOR:
          type = "游戏旋转矢量传感器";
          break;
        case Sensor.TYPE_GYROSCOPE_UNCALIBRATED:
          type = "陀螺仪传感器(未经校准)";
          break;
        case Sensor.TYPE_SIGNIFICANT_MOTION:
          type = "特殊动作触发传感器";
          break;
        case Sensor.TYPE_STEP_DETECTOR:
          type = "步数探测传感器";
          break;
        case Sensor.TYPE_STEP_COUNTER:
          type = "步数计数传感器";
          break;
        case Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR:
          type = "地磁旋转矢量传感器";
          break;
        case Sensor.TYPE_HEART_RATE:
          type = "心率传感器";
          break;
        case Sensor.TYPE_POSE_6DOF:
          type = "POSE_6DOF传感器";
          break;
        case Sensor.TYPE_STATIONARY_DETECT:
          type = "静止检测传感器";
          break;
        case Sensor.TYPE_MOTION_DETECT:
          type = "运动检测传感器";
          break;
        case Sensor.TYPE_HEART_BEAT:
          type = "心跳传感器";
          break;
        case Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT:
          type = "低延迟身体检测传感器";
          break;
        case Sensor.TYPE_ACCELEROMETER_UNCALIBRATED:
          type = "加速度传感器(未经校准)";
          break;
        default:
          type = "其它传感器";
          break;
      }
      // 传感器名称
      s.getName();
      // 传感器版本
      s.getVersion();
      // 传感器厂商
      s.getVendor();
      // 传感器最大值
      s.getMaximumRange();
      // 在传感器单元中的传感器的分辨率
      s.getResolution();
      // 传感器耗电量
      s.getPower();
      // 传感器最小延迟（微秒）
      s.getMinDelay();
      // 保留此传感器中的分批方式的FIFO的事件数。给出了对可批处理事件的最小数量的保证
      s.getFifoReservedEventCount();
      // 获取该传感器能分批处理的事件的最大数量
      s.getFifoMaxEventCount();
      // 同一个应用程序传感器ID将是唯一的，除非该设备是恢复出厂设置
      s.getId();
      // 传感器最大延迟（微秒）
      s.getMaxDelay();
      // 当传感器是唤醒状态返回true
      s.isWakeUpSensor();

      LL.e("传感器类型：" + type);
      LL.e("名称：" + s.getName() + "，版本：" + s.getVersion() + "，厂商：" + s.getVendor());
    }
  }

  public void select(View view) {
    LL.e("时区：" + getTimeZone());
  }

  public static String getTimeZone() {
    return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
  }

  public void update(View view) {

  }
}
