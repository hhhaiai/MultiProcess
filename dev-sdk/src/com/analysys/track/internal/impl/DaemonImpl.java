//package com.analysys.dev.internal.impl;
//
//import android.content.Context;
//
//import TableLocation;
//import EGContext;
//import MessageDispatcher;
//import EThreadPool;
//import EContextHelper;
//import SPHelper;
//
//import org.json.JSONObject;
//
//public class DaemonImpl {
//    Context mContext;
//
//    private static class Holder {
//        private static final DaemonImpl INSTANCE = new DaemonImpl();
//    }
//
//    public static DaemonImpl getInstance(Context context) {
//        if (DaemonImpl.Holder.INSTANCE.mContext == null) {
//            DaemonImpl.Holder.INSTANCE.mContext = EContextHelper.getContext(context);
//        }
//
//        return DaemonImpl.Holder.INSTANCE;
//    }
//    public void daemon() {
//        EThreadPool.execute(new Runnable() {
//            @Override
//            public void run() {
//                if (!isGetDaemon()) {
//                    return;
//                }
//                JSONObject location = getLocation();
//                if (location != null) {
//                    TableLocation.getInstance(mContext).insert(String.valueOf(location));
//                    SPHelper.getDefault(mContext).edit().putLong(EGContext.SP_LOCATION_TIME, System.currentTimeMillis())
//                            .commit();
//                }
//                MessageDispatcher.getInstance(mContext).locationInfo(EGContext.LOCATION_CYCLE);
//            }
//        });
//    }
//    private boolean isGetDaemon() {
//        long time = SPHelper.getDefault(mContext).getLong(EGContext.SP_DAEMON_TIME, 0);
//        if (time == 0) {
//            return true;
//        } else {
//            if (System.currentTimeMillis() - time >= EGContext.LOCATION_CYCLE) {
//                return true;
//            } else {
//                return false;
//            }
//        }
//    }
//}
