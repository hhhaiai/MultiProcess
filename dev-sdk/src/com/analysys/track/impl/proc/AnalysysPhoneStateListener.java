package com.analysys.track.impl.proc;

import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.analysys.track.utils.reflectinon.EContextHelper;
import android.telephony.PhoneStateListener;

public class AnalysysPhoneStateListener {
    private static Context mContext;
    private static TelephonyManager telephonyManager = null;
    private static PhoneStateListener phoneStateListener = null;
//    public static int lastSignal = 0;

    private static class Holder {
        private static final AnalysysPhoneStateListener INSTANCE = new AnalysysPhoneStateListener();
    }

    public static AnalysysPhoneStateListener getInstance(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
            if(phoneStateListener == null){
                phoneStateListener = phoneStateListener(mContext);
            }
        }

        return AnalysysPhoneStateListener.Holder.INSTANCE;
    }

    public TelephonyManager getTelephonyManager(){
        if(telephonyManager == null){
            telephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_CELL_LOCATION);
        }
        return telephonyManager;
    }
    private static PhoneStateListener phoneStateListener(final Context context){
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

}
