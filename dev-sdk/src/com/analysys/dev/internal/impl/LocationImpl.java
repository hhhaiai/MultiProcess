package com.analysys.dev.internal.impl;

import android.Manifest;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import com.analysys.dev.database.TableLocation;
import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.internal.utils.EContextHelper;
import com.analysys.dev.internal.utils.EThreadPool;
import com.analysys.dev.internal.utils.LL;
import com.analysys.dev.internal.utils.PermissionUtils;
import com.analysys.dev.internal.utils.sp.SPHelper;
import com.analysys.dev.internal.work.MessageDispatcher;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.WIFI_SERVICE;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/15 15:01
 * @Author: Wang-X-C
 */
public class LocationImpl {

  Context mContext;

  private static class Holder {
    private static final LocationImpl INSTANCE = new LocationImpl();
  }

  public static LocationImpl getInstance(Context context) {
    if (Holder.INSTANCE.mContext == null) {
      if (context != null) {
        Holder.INSTANCE.mContext = context;
      } else {
        Holder.INSTANCE.mContext = EContextHelper.getContext();
      }
    }

    return Holder.INSTANCE;
  }

  public void location() {
    EThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        if (!isGetLocation()) {
          return;
        }
        JSONObject location = getLocation();
        if (location != null) {
          TableLocation.getInstance(mContext).insert(String.valueOf(location));
          SPHelper.getDefault(mContext).edit().putLong(
              EDContext.SP_LOCATION_TIME, System.currentTimeMillis()).commit();
        }
        MessageDispatcher.getInstance(mContext).locationInfo(EDContext.LOCATION_CYCLE);
      }
    });
  }

  private boolean isGetLocation() {
    long time = SPHelper.getDefault(mContext).getLong(EDContext.SP_LOCATION_TIME, 0);
    if (time == 0) {
      return true;
    } else {
      if (System.currentTimeMillis() - time >= EDContext.LOCATION_CYCLE) {
        return true;
      } else {
        return false;
      }
    }
  }

  private JSONObject getLocation() {
    JSONObject locationJson = null;
    try {
      locationJson = new JSONObject();
      locationJson.put("CT", String.valueOf(System.currentTimeMillis()));

      String locationInfo = getCoordinate();
      int location = SPHelper.getDefault(mContext).getInt(EDContext.SP_LOCATION, 1);
      if (!TextUtils.isEmpty(locationInfo) && location == 1) {
        locationJson.put("GL", locationInfo);
      }

      JSONArray wifiInfo = getWifiInfo();
      int wifi = SPHelper.getDefault(mContext).getInt(EDContext.SP_WIFI, 1);
      if (wifiInfo != null && wifiInfo.length() != 0 && wifi == 1) {
        locationJson.put("WifiInfo", wifiInfo);
      }

      JSONArray baseStation = getBaseStationInfo();
      int base = SPHelper.getDefault(mContext).getInt(EDContext.SP_BASE_STATION, 1);
      if (baseStation != null && baseStation.length() != 0 && base == 1) {
        locationJson.put("BaseStationInfo", baseStation);
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return locationJson;
  }

  /**
   * 经纬度坐标
   */
  private String getCoordinate() {
    return "2.00000" + "-" + "6.233232323";
  }

  /**
   * WiFi信息
   */
  private JSONArray getWifiInfo() {
    JSONArray jar = new JSONArray();
    try {
      if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_WIFI_STATE)) {
        WifiManager wm = (WifiManager) mContext.getSystemService(WIFI_SERVICE);
        int wifiDetail = SPHelper.getDefault(mContext).getInt(EDContext.SP_WIFI_DETAIL, 0);
        if (wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
          List<ScanResult> list = wm.getScanResults();
          wifiSort(list);
          for (int i = 0; i < list.size(); i++) {
            if (i < 5) {
              ScanResult s = list.get(i);
              JSONObject job = new JSONObject();
              job.put("SSID", s.SSID);
              job.put("BSSID", s.BSSID);
              job.put("level", s.level);
              if (wifiDetail == 1) {
                job.put("channelWidth", s.channelWidth);
                job.put("capabilities", s.capabilities);
                job.put("frequency", s.frequency);
              }
              jar.put(job);
            }
          }
        }
      }
    } catch (JSONException e) {
    }
    return jar;
  }

  /**
   * wifi 列表排序
   */
  private void wifiSort(List<ScanResult> list) {
    for (int i = 0; i < list.size() - 1; i++) {
      for (int j = i + 1; j < list.size(); j++) {
        if (list.get(i).level > list.get(j).level) {
          ScanResult scanResult = list.get(i);
          list.set(i, list.get(j));
          list.set(j, scanResult);
        }
      }
    }
  }

  /**
   * 基站信息
   */
  private JSONArray getBaseStationInfo() {
    JSONArray jar = new JSONArray();
    try {
      TelephonyManager mTelephonyManager = (TelephonyManager)
          mContext.getSystemService(Context.TELEPHONY_SERVICE);
      if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)) {
        //List<NeighboringCellInfo> list = mTelephonyManager.getNeighboringCellInfo();
        //baseStationSort(list);
        //for (int i = 0; i < list.size(); i++) {
        //  if (i < 5) {
        //    JSONObject job = new JSONObject();
        //    job.put("Lac", list.get(i).getLac());
        //    job.put("CellId", list.get(i).getCid());
        //    job.put("level", list.get(i).getRssi());
        //    jar.put(job);
        //  }
        //}
        GsmCellLocation location = (GsmCellLocation) mTelephonyManager.getCellLocation();
        JSONObject locationJson = new JSONObject();
        locationJson.put("Lac", location.getLac());
        locationJson.put("CellId", location.getCid());
        locationJson.put("level", location.getPsc());
        jar.put(locationJson);
      }
    } catch (Exception e) {
    }
    return jar;
  }

  /**
   * 基站列表排序
   */
  private void baseStationSort(List<NeighboringCellInfo> list) {
    for (int i = 0; i < list.size() - 1; i++) {
      for (int j = i + 1; j < list.size(); j++) {
        if (list.get(i).getRssi() < list.get(j).getRssi()) {
          NeighboringCellInfo cellInfo = list.get(i);
          list.set(i, list.get(j));
          list.set(j, cellInfo);
        }
      }
    }
  }
}
