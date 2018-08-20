package com.eguan.utils.policy;

import android.content.Context;
import android.content.SharedPreferences;

import com.eguan.utils.commonutils.EgLog;

/**
 * Created by chris on 16/11/19.
 */

public class PolicyManger {

    private static Context mContext;
    private static final String SP_NAME = "policy_manager_sp";

    public static String servicePullPolicyVer = "0";

    private PolicyManger(Context mContext) {
        SharedPreferences sp = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        servicePullPolicyVer = sp.getString("servicePullVer", "0");
    }

//    public static boolean isNewPolicy(String newPolicyVer) {
//        return Policy.isNewPolicy(newPolicyVer);
//    }

    public static void saveNewPolicyToLocal(Context mContext, Policy newPolicy) {
        Policy.savePolicyNative(mContext, newPolicy);
    }

//    public static void initLocalPolicy(Context mContext) {
//        getLocalPolicy(mContext);
//    }
    /**
     * 非实时上传,走端口8089
     */
    public void useDefaultPolicy() {
        saveNewPolicyToLocal(mContext, getDefalutPolicy());
    }

    /**
     * 实时上传,走端口8089
     */
    public void useDefaultRtPolicy() {
        Policy localPolicy = PolicyManger.getLocalPolicy(mContext);
        if (!"".equals(localPolicy.getPolicyVer())) {
            // saveNewPolicyToLocal(mContext,localPolicy);
        } else {
            saveNewPolicyToLocal(mContext, getDefalutRtPolicy());
        }
    }


//    public static void setServicePullPolicyVer(String ver) {
//        SharedPreferences sp = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putString("servicePullVer", ver);
//        editor.commit();
//        servicePullPolicyVer = ver;
//
//    }
//
//    public static String getServicePullPolicyVer() {
//        return servicePullPolicyVer;
//    }

    private static PolicyManger instance;

    public static PolicyManger getInstance(Context context) {
        if (instance == null) {
            synchronized (PolicyManger.class) {
                if (instance == null) {
                    instance = new PolicyManger(context);
                    mContext = context.getApplicationContext();
                }
            }
        }
        return instance;
    }

    public static Policy getLocalPolicy(Context mContext) {
        if (null == mContext) {
            EgLog.v("getLocalPolicy传下的参数Context为null");
            return null;
        }
        return Policy.getNativePolicy(mContext);
    }

    public static Policy getDefalutPolicy() {
        return Policy.getDefaultPolicyNative();
    }

    public static Policy getDefalutRtPolicy() {
        return Policy.getDefaultRtPolicNative();
    }

//    public static String currentPolicyVer(Context mContext) {
//        String rtVer = getLocalPolicy(mContext).getPolicyVer();
//        boolean newVer = isNewPolicy(servicePullPolicyVer);
//        if (newVer) {
//            return servicePullPolicyVer;
//        } else {
//            return TextUtils.isEmpty(rtVer) ? "0" : rtVer;
//        }
//
//    }
//
//    public boolean canUpload() {
//        return Policy.getNativePolicy(mContext).getPermitForFailTime() < System.currentTimeMillis()
//                && Policy.getNativePolicy(mContext).getPermitForServerTime() < System.currentTimeMillis();
//    }

    public void setDebugPolicy() {
        Policy debug = Policy.getDebugPolicyFromLocal(mContext);
        Policy.savePolicyNative(mContext, debug);
    }

}
