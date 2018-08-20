package com.eguan.imp;

import java.util.List;

import com.eguan.Constants;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.SPHodler;
import com.eguan.utils.commonutils.SystemUtils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

/**
 * 位置变化管理器
 * <p>
 * // * @author MACHENIKE
 */
public class LocationChangeManager {

    private LocationManager locationManager;
    private Context context;
    SPHodler spUtil = null;
    private static LocationChangeManager instance = null;

    public static LocationChangeManager getInstance(Context context) {
        if (instance == null) {
            synchronized (LocationChangeManager.class) {
                instance = new LocationChangeManager(context);
            }
        }
        return instance;
    }

    private LocationChangeManager(Context context) {
        this.context = context.getApplicationContext();
        spUtil = SPHodler.getInstance(context);
        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void getLocationInfo() {

        if (!SystemUtils.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                || !SystemUtils.checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            return;
        }
        List<String> providers = this.locationManager.getProviders(true);

        String provider;
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;

        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (ifSaveLocation(location)) {
            resetLocaiton(location);
        }
    }

    /**
     * 缓存地理位置信息数据
     *
     * @param location
     */
    public void resetLocaiton(Location location) {

        if (location != null) {
            spUtil.setLastLocation(location.getLongitude() + "-" + location.getLatitude());
            Intent intent = new Intent();
            intent.setAction(Constants.GL_ACTION);
            intent.putExtra("GL", location.getLongitude() + "-" + location.getLatitude());
            context.sendBroadcast(intent);
        }
    }

    /**
     * 计算两个坐标之间的距离
     *
     * @param longitude1
     * @param latitude1
     * @param longitude2
     * @param latitude2
     * @return
     */
    private double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double EARTH_RADIUS = 6378137.0;
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math
                .sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    private double rad(double d) {
        return d * Math.PI / 180.0;
    }

    // // 获取Location Provider
    // private String getProvider() {
    // // 构建位置查询条件
    // Criteria criteria = new Criteria();
    // // 查询精度：高
    // criteria.setAccuracy(Criteria.ACCURACY_FINE);
    // // 是否查询海拨：否
    // criteria.setAltitudeRequired(false);
    // // 是否查询方位角 : 否
    // criteria.setBearingRequired(false);
    // // 是否允许付费：是
    // criteria.setCostAllowed(false);
    // // 电量要求：低
    // criteria.setPowerRequirement(Criteria.POWER_LOW);
    // // 返回最合适的符合条件的provider，第2个参数为true说明 , 如果只有一个provider是有效的,则返回当前provider
    // return locationManager.getBestProvider(criteria, true);
    // }

    /**
     * 判断距离是否可以存储信息
     *
     * @param location
     * @return
     */

    private boolean ifSaveLocation(Location location) {

        if (location == null) {
            return false;
        }
        String lastLocation = spUtil.getLastLocation();
        if (lastLocation.equals("")) {
            return true;
        }

        String[] ary = lastLocation.split("-");
        if (ary.length != 2) {
            return true;
        }
        double longitude1 = Double.parseDouble(ary[1]);
        double latitude1 = Double.parseDouble(ary[0]);
        double distance = getDistance(longitude1, latitude1, location.getLongitude(), location.getLatitude());

        if (Constants.MINDISTANCE <= distance) {

            return true;
        } else {
            EgLog.e("---- 距离没有变化 ----");
        }
        return false;
    }

}
