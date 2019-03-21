package com.analysys.track.impl.proc;

import android.Manifest;
import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;

import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import android.telephony.PhoneStateListener;

public class AnalysysPhoneStateListener {
    private static Context mContext;
    private static PhoneStateListener phoneStateListener = null;
    private static CellLocation cellLocation = null;
    private static class Holder {
        private static final AnalysysPhoneStateListener INSTANCE = new AnalysysPhoneStateListener();
    }

    public static AnalysysPhoneStateListener getInstance(Context context) {
        if (AnalysysPhoneStateListener.Holder.INSTANCE.mContext == null) {
            AnalysysPhoneStateListener.Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        if(phoneStateListener == null){
            phoneStateListener = phoneStateListener(mContext);
        }
        return AnalysysPhoneStateListener.Holder.INSTANCE;
    }

    private static PhoneStateListener phoneStateListener(final Context context){
        return new PhoneStateListener() {
            @Override
            public void onCellLocationChanged(CellLocation location) {
                if (!PermissionUtils.checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                        !AndroidManifestHelper.isPermissionDefineInManifest(context, Manifest.permission.ACCESS_COARSE_LOCATION)){
                    return;
                }
                // gsm网络
                if (location instanceof GsmCellLocation) {
                    if(location != cellLocation){
                        cellLocation = location;
                    }
//                    GsmCellLocation loc = (GsmCellLocation)location;
//                    ELOG.i("后续1");
//                    if(loc != null){
//                        if(locationJson != null && jsonArray != null){
//                            ELOG.i("后续1  执行");
//                            JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, loc.getLac(),DataController.SWITCH_OF_LOCATION_AREA_CODE);
//                            JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, loc.getCid(),DataController.SWITCH_OF_CELL_ID);
//                            JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, loc.getPsc(),DataController.SWITCH_OF_BS_LEVEL);
//                            jsonArray.put(locationJson);
//                        }
//                    }
//                    //获取相邻基站信息
//
//                    List<NeighboringCellInfo> neighboringList = mTelephonyManager.getNeighboringCellInfo();
//                    for(NeighboringCellInfo ni:neighboringList){
//                        ELOG.i("后续2");
//                        if(ni != null && locationJson != null && jsonArray != null){
//                            ELOG.i("后续2  执行");
//                            JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, ni.getLac(),DataController.SWITCH_OF_LOCATION_AREA_CODE);
//                            JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, ni.getCid(),DataController.SWITCH_OF_CELL_ID);
//                            JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, ni.getPsc(),DataController.SWITCH_OF_BS_LEVEL);
//                            jsonArray.put(locationJson);
//                        }
//                    }
                } else {// 其他CDMA等网络
//                    try {
//                        Class cdmaClass = Class.forName("android.telephony.cdma.CdmaCellLocation");
//                        List<NeighboringCellInfo> neighboringList = mTelephonyManager.getNeighboringCellInfo();
//                        for(NeighboringCellInfo ni:neighboringList){
//                            ELOG.i("后续3");
//                            if(ni != null && locationJson != null && jsonArray != null){
//                                ELOG.i("后续3  执行");
//                                JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode, ni.getLac(),DataController.SWITCH_OF_LOCATION_AREA_CODE);
//                                JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId, ni.getCid(),DataController.SWITCH_OF_CELL_ID);
//                                JsonUtils.pushToJSON(mContext,locationJson,DeviceKeyContacts.LocationInfo.BaseStationInfo.Level, ni.getPsc(),DataController.SWITCH_OF_BS_LEVEL);
//                                jsonArray.put(locationJson);
//                            }
//                        }
//                    } catch (Throwable t) {
//                    }
                }// end CDMA网络
                super.onCellLocationChanged(location);
            }// end onCellLocationChanged

            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);
            }
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
            }
        };
    }

}
