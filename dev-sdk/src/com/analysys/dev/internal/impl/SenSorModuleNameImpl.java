package com.analysys.dev.internal.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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
    public JSONArray getSensorInfo() {
        JSONArray senSorArray = null;
        try{
            SensorManager sensorManager = (SensorManager)mContext.getSystemService(mContext.SENSOR_SERVICE);
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            JSONObject info;
            senSorArray = new JSONArray();
            for (int i = 0; i < sensorList.size(); i++) {
                Sensor s = sensorList.get(i);
                info = new JSONObject();
                // 传感器名称
                info.put(DeviceKeyContacts.DevInfo.SenSorName,s.getName());
//                ELOG.i("SenSorName :::::::"+s.getName());
                // 传感器版本
                info.put(DeviceKeyContacts.DevInfo.SenSorVersion,s.getVersion());
                // 传感器厂商
                info.put(DeviceKeyContacts.DevInfo.SenSorManufacturer,s.getVendor());
                try{
                    // 传感器id
                    info.put(DeviceKeyContacts.DevInfo.SenSorId,s.getId());
                }catch (Throwable t1){
                }
                //当传感器是唤醒状态返回true
                info.put(DeviceKeyContacts.DevInfo.SenSorWakeUpSensor,s.isWakeUpSensor());
                // 传感器耗电量
                info.put(DeviceKeyContacts.DevInfo.SenSorPower,s.getPower());
//                ELOG.i("传感器信息：：：：："+info);
                senSorArray.put(info);
            }
        }catch (Throwable t){
            ELOG.e("  getSensorInfo  has an exception::::"+t);
        }
        return senSorArray;
    }
}
