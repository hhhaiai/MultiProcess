package com.analysys.track.utils;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 粘贴版
 * @Version: 1.0
 * @Create: 2019-11-13 11:46
 * @author: sanbo
 */
public class MClipManager {
    /**
     * 往系统粘贴板拷贝东西
     *
     * @param context
     * @param key
     * @param value
     */
    public static void setClipbpard(Context context, String key, String value) {
        try {
            if (!SystemUtils.isMainThread()) {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
            }
            context = EContextHelper.getContext();
            if (context == null) {
                return;
            }
            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm == null) {
                return;
            }
            // 创建普通字符型ClipData
            ClipData clipData = ClipData.newPlainText(key, value);
            if (clipData == null) {
                return;
            }
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(clipData);
        } catch (Throwable igone) {
        }

    }

    /**
     * 获取粘贴板内容，暂支持text
     *
     * @param context
     * @param key
     * @param defValue
     * @return
     */
    public static String getValueByClipbpard(Context context, String key, String defValue) {
        try {
            if (!SystemUtils.isMainThread()) {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
            }
            context = EContextHelper.getContext();
            if (context == null) {
                return defValue;
            }
            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

            if (cm == null) {
                return defValue;
            }
            ClipData cd = cm.getPrimaryClip();
            if (cd == null) {
                return defValue;
            }
            ClipDescription cdn = cd.getDescription();
            if (cdn == null) {
                return defValue;
            }
            CharSequence mLable = cdn.getLabel();
            if (!TextUtils.isEmpty(mLable) && mLable.toString().equals(key)) {
                for (int i = 0; i < cd.getItemCount(); i++) {
                    CharSequence t = cd.getItemAt(i).getText();
                    if (!TextUtils.isEmpty(t)) {
                        return t.toString();
                    }
                }
            }
        } catch (Throwable igone) {
        }

        return defValue;
    }
}
