package com.analysys.track.internal.impl;

import android.Manifest;
import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

public class AnalysysPhoneStateListener {
    private static Context mContext;
    private static TelephonyManager telephonyManager = null;
    private static PhoneStateListener phoneStateListener = null;
//    public static int lastSignal = 0;

    public static AnalysysPhoneStateListener getInstance(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
            if (phoneStateListener == null) {
                phoneStateListener = phoneStateListener(mContext);
            }
        }

        return AnalysysPhoneStateListener.Holder.INSTANCE;
    }

    private static PhoneStateListener phoneStateListener(final Context context) {
        return new PhoneStateListener() {
            @Override
            public void onCellLocationChanged(CellLocation location) {
            }// end onCellLocationChanged

            @Override
            public void onServiceStateChanged(ServiceState serviceState) {
                super.onServiceStateChanged(serviceState);
            }

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
//                int asu=signalStrength.getGsmSignalStrength();
//                lastSignal=-113+2*asu; //信号强度
                super.onSignalStrengthsChanged(signalStrength);
            }
        };
    }

    public TelephonyManager getTelephonyManager() {
        try {
            if (telephonyManager == null) {
                telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                if (PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                        && PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_LOCATION);
                }
            }
        } catch (Throwable t) {
        }
        return telephonyManager;
    }

    private static class Holder {
        private static final AnalysysPhoneStateListener INSTANCE = new AnalysysPhoneStateListener();
    }

}
