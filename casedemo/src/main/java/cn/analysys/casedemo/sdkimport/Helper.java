package cn.analysys.casedemo.sdkimport;

import android.content.Context;

import com.analysys.track.internal.impl.ftime.LastModifyByFile;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EContextHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.analysys.casedemo.cases.CaseCtl;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: 所有SDK的引用全部放该类
 * @Version: 1.0
 * @Create: 2021/03/67 11:26:31
 * @author: sanbo
 */
public class Helper {

    public static List<String> getLastAliveTimeStr() {
        List<String> result = new CopyOnWriteArrayList<>();
        List<LastModifyByFile.AppTime> ats = LastModifyByFile.getLatestApp(CaseCtl.getContext());
        if (ats.size() > 0) {
            for (LastModifyByFile.AppTime at : ats) {
                result.add(at.toString());
            }
        }
        return result;
    }

    public static void logi(String info) {
        ELOG.i(info);
    }

    public static Context getContext(Context context) {
        return EContextHelper.getContext(context);
    }
}
