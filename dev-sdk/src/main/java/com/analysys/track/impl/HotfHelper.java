package com.analysys.track.impl;

import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.model.PolicyInfo;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.Memory2File;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONObject;

import java.io.File;

/**
 * @Copyright © 2020/3/29 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2020/3/29 16:57
 * @author: sanbo
 */
public class HotfHelper {

    /**
     * save hotfix when receiver server response
     *
     * @param policyInfo
     * @param hotfix
     */
    public void processAndSaveHotfix(PolicyInfo policyInfo, JSONObject hotfix) {
        if (hotfix.has(UploadKey.Response.HotFixResp.DATA) &&
                hotfix.has(UploadKey.Response.HotFixResp.SIGN) &&
                hotfix.has(UploadKey.Response.HotFixResp.VERSION)) {
            String data = hotfix.optString(UploadKey.Response.HotFixResp.DATA, "");
            String version = hotfix.optString(UploadKey.Response.HotFixResp.VERSION, "");
            policyInfo.setHotfixVersion(version);
//                        String sign = patch.optString(UploadKey.Response.HotFixResp.SIGN, "");
//                        String code = Md5Utils.getMD5(data + "@" + version);
//                        if (!TextUtils.isEmpty(sign) && sign.contains(code)) {
            String dirPath = mContext.getFilesDir().getAbsolutePath() + EGContext.HOTFIX_CACHE_HOTFIX_DIR;
            File dir = new File(dirPath);
            if (dir.exists() && !dir.isDirectory()) {
                dir.deleteOnExit();
            }
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fn = "hf_" + version + ".dex";
            File file = new File(dir, fn);
            try {
                Memory2File.savePatch(data, file);
                //默认这个dex 是正常的完整的
                EGContext.DEX_ERROR = false;
                // SPHelper.setStringValue2SP(mContext, EGContext.HOT_FIX_PATH_TEMP, file.getAbsolutePath());
                SPHelper.setStringValue2SPCommit(mContext, EGContext.HOT_FIX_PATH, file.getAbsolutePath());
                SPHelper.setBooleanValue2SPCommit(mContext, EGContext.HOT_FIX_ENABLE_STATE, true);
                if (EGContext.FLAG_DEBUG_INNER) {
                    String p = SPHelper.getStringValueFromSP(mContext, EGContext.HOT_FIX_PATH, "");
                    boolean e = SPHelper.getBooleanValueFromSP(mContext, EGContext.HOT_FIX_ENABLE_STATE, false);
                    ELOG.e(BuildConfig.tag_hotfix, "新的热修复包下载成功:[path]" + p + " ; file status: " + file.exists() + " ; [enable]" + e);
                }
            } catch (Throwable e) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(BuildConfig.tag_hotfix, "新的热修复包下载失败【存文件失败】【重置策略版本号】");
                }
//                                SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_VERSION);
            }
//                        }

        } else {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_hotfix, "新的热修复包下载失败【签名效验失败】【重置策略版本号】");
            }
            SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_VERSION);
        }
    }


    /********************* get instance begin **************************/
    public static HotfHelper getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private HotfHelper initContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final HotfHelper INSTANCE = new HotfHelper();
    }

    private HotfHelper() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/
}
