package com.analysys.dev.internal.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.analysys.dev.model.SenSorModuleNameInfo;
import com.analysys.dev.utils.reflectinon.EContextHelper;

import java.util.List;

public class SenSorModuleNameImpl{
    Context mContext;

    private static class Holder {
        private static final SenSorModuleNameImpl INSTANCE = new SenSorModuleNameImpl();
    }

    public static SenSorModuleNameImpl getInstance(Context context) {
        if (SenSorModuleNameImpl.Holder.INSTANCE.mContext == null) {
            SenSorModuleNameImpl.Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }

        return SenSorModuleNameImpl.Holder.INSTANCE;
    }
    /**
     * 获取传感器方法
     */
    public void getSensorInfo() {
        SensorManager sensorManager = (SensorManager)mContext.getSystemService(mContext.SENSOR_SERVICE);
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
            SenSorModuleNameInfo info = new SenSorModuleNameInfo();
            // 传感器名称
            info.setSenSorName(s.getName());
            // 传感器版本
            info.setSenSorVersion(s.getVersion());
            // 传感器厂商
            info.setSenSorManufacturer(s.getVendor());
            // 传感器id
            info.setSenSorId(s.getId());
            //当传感器是唤醒状态返回true
            info.setSenSorWakeUpSensor(s.isWakeUpSensor());
            // 传感器耗电量
            info.setSenSorPower(s.getPower());
        }
    }
}
