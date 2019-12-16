package com.analysys.track.utils;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Copyright @ 2019/12/12 sanbo Inc. All rights reserved.
 * @Description:  file utils
 * @Version: 1.0
 * @Create: 2019/12/12 22:49
 * @author: sanbo
 */
public class FileUitls {


    /**
     * File's create time
     *
     * @param file
     * @return
     */
    public long getCreateTime(File file) {
        return getCreateTime(file.getAbsolutePath());
    }


    /**
     * File's create time
     *
     * @param path
     * @return
     */
    public long getCreateTime(String path) {

        String shellResult = ShellUtils.exec(new String[]{"ls", "-lau", path});
        if (!TextUtils.isEmpty(shellResult)) {
            String[] arr = shellResult.split("\\s+");
            if (arr.length >= 3) {
                String date = arr[arr.length - 3];
                String time = arr[arr.length - 2];
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                try {
                    Date d = sdf.parse(date + " " + time);
                    return d.getTime();
                } catch (Throwable e) {
                }
            }

        }
        return 0;
    }

    /********************* get instance begin **************************/
    public static FileUitls getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private FileUitls initContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final FileUitls INSTANCE = new FileUitls();
    }

    private FileUitls() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/
}