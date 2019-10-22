package com.analysys.track.internal.impl;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.net.RequestUtils;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.MultiProcessChecker;

import org.json.JSONObject;

public class HotFoxImpl {

    public static void reqHotFix(final Context context, final ECallBack back) {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(EGContext.HOT_FIX_TAG, "检查热更新[检查 启动|1小时每次]");
        }

        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    reqHotFix(context);
                    if (back == null) {
                        back.onProcessed();
                    }
                }
            });
        } else {
            reqHotFix(context);
            if (back == null) {
                back.onProcessed();
            }
        }
    }

    private static void reqHotFix(Context context) {
        try {
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_HOTFIX, EGContext.TIME_SECOND * 2, System.currentTimeMillis())) {
                try {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.i(EGContext.HOT_FIX_TAG, "检查更新[开始]-获得锁");
                    }
                    String url = EGContext.NORMAL_APP_URL;
                    if (EGContext.DEBUG_URL) {
                        url = "http://192.168.220.167:8089";
                    }
                    if (TextUtils.isEmpty(url)) {
                        return;
                    }
                    url = url + "/hotpatch/";
                    String result = RequestUtils.httpRequest(url, "", context);
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.i(EGContext.HOT_FIX_TAG, "result = " + result);
                    }
                    if (RequestUtils.FAIL.equals(result)) {
                        return;
                    }

                    JSONObject object = new JSONObject(result);
                    String code = String.valueOf(object.opt(UploadKey.Response.RES_CODE));
                    if (EGContext.HTTP_STATUS_500.equals(code)) {
                        PolicyImpl.getInstance(context).saveHotFixPatch(object.optJSONObject(UploadKey.Response.RES_POLICY));
                    }
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.i(EGContext.HOT_FIX_TAG, "检查更新[结束]-释放锁");
                    }
                } catch (Throwable e) {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.i(EGContext.HOT_FIX_TAG, "检查更新[结束][出错]-释放锁");
                        ELOG.e(e);
                    }
                }
                MultiProcessChecker.getInstance().setLockLastModifyTime(context, EGContext.FILES_SYNC_HOTFIX, System.currentTimeMillis());
            } else {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(EGContext.HOT_FIX_TAG, "检查更新[让行]-没获得锁");
                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
    }
}
