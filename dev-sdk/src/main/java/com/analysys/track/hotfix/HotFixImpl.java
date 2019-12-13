package com.analysys.track.hotfix;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.net.RequestUtils;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.PolicyEncrypt;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class HotFixImpl {

    public static void reqHotFix(final Context context, final ECallBack back) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    reqHotFix(context);
                    if (back != null) {
                        back.onProcessed();
                    }
                }
            });
        } else {
            reqHotFix(context);
            if (back != null) {
                back.onProcessed();
            }
        }
    }

    private static void reqHotFix(Context context) {
        try {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_hotfix, "检查热更新[检查 启动]");
            }

            long time = System.currentTimeMillis();
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(context, EGContext.FILES_SYNC_UPLOAD, EGContext.TIME_SECOND * 2, time)) {
                try {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.i(BuildConfig.tag_hotfix, "检查更新[开始]-获得锁");
                    }
                    long lastTime = SPHelper.getLongValueFromSP(context, EGContext.HOTFIX_TIME, 0L);

                    if ((time - lastTime) > EGContext.TIME_HOUR * 1) {
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.i(BuildConfig.tag_hotfix, "检查热更新[间隔时间大于一小时]-访问接口检查");
                        }
                        SPHelper.setLongValue2SP(context, EGContext.HOTFIX_TIME, time);
                        String url = EGContext.NORMAL_APP_URL;
                        if (EGContext.DEBUG_URL) {
                            url = "http://192.168.220.167:8089";
                        }
                        if (TextUtils.isEmpty(url)) {
                            return;
                        }
                        url = url + "/hotpatch";
                        String result = RequestUtils.httpRequest(url, "", context);

                        if (!RequestUtils.FAIL.equals(result)) {
                            JSONObject object = new JSONObject(result);
                            String code = String.valueOf(object.opt(UploadKey.Response.RES_CODE));
                            if (EGContext.FLAG_DEBUG_INNER) {
                                ELOG.i(BuildConfig.tag_hotfix, "result = " + code);
                            }
                            if (EGContext.HTTP_STATUS_500.equals(code)) {
                                String intentJson = object.optString(UploadKey.Response.RES_POLICY);
                                JSONObject jsonObject;
                                try {
                                    //没加密
                                    jsonObject = new JSONObject(intentJson);
                                } catch (JSONException e) {
                                    //加密
                                    intentJson = PolicyEncrypt.getInstance().decode(
                                            intentJson,
                                            SystemUtils.getAppKey(context),
                                            EGContext.SDKV, null, null);
                                    jsonObject = new JSONObject(intentJson);
                                }

                                PolicyImpl.getInstance(context)
                                        .saveRespParams(jsonObject);
                            }
                            if (EGContext.FLAG_DEBUG_INNER) {
                                ELOG.i(BuildConfig.tag_hotfix, "检查更新[结束]-释放锁");
                            }
                        } else {
                            if (EGContext.FLAG_DEBUG_INNER) {
                                ELOG.i(BuildConfig.tag_hotfix, "result = " + result);
                            }
                        }
                    } else {
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.i(BuildConfig.tag_hotfix, "检查热更新[上次时间间隔小于一小时]");
                        }
                    }


                } catch (Throwable e) {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.i(BuildConfig.tag_hotfix, "检查更新[结束][出错]-释放锁" + e.getMessage());
                    }
                }
                MultiProcessChecker.getInstance().setLockLastModifyTime(context, EGContext.FILES_SYNC_UPLOAD, System.currentTimeMillis());
            } else {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(BuildConfig.tag_hotfix, "检查更新[让行]-没获得锁");
                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {

        }
    }
}
