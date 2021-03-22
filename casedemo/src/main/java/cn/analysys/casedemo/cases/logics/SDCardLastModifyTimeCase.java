package cn.analysys.casedemo.cases.logics;

import android.content.pm.PackageManager;

import com.analysys.track.utils.MDate;
import com.cslib.defcase.ETestCase;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;


/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: SDcard缓存目录的最新缓存时间
 * @Version: 1.0
 * @Create: 2021/03/74 14:29:04
 * @author: sanbo
 */
public class SDCardLastModifyTimeCase extends ETestCase {


    public SDCardLastModifyTimeCase() {
        super("[时间]所有文件修改时间");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        long begin = System.currentTimeMillis();
        ConcurrentHashMap<String, Long> map = SDKHelper.getSDDirTime();
        if (map.size() == 0) {
            return false;
        }
        Iterator<Map.Entry<String, Long>> iterator = map.entrySet().iterator();
        StringBuffer sb = new StringBuffer();
        sb.append("==================访问SDcard 根目录末次访问时间%s[%d]===================").append("\n");
        int index = 0;
        PackageManager pm = SDKHelper.getContext().getPackageManager();

        while (iterator.hasNext()) {
            index += 1;
            Map.Entry<String, Long> entry = iterator.next();
            JSONObject js = SDKHelper.getJson(pm, entry.getKey(), entry.getValue());
            if (js != null && js.length() > 0) {
                sb.append(js.toString()).append("------------>").append(MDate.getDateFromTimestamp(entry.getValue())).append("\n");
            }
        }
        long end = System.currentTimeMillis();
        Woo.logFormCase(String.format(sb.toString(), SDKHelper.convertLongTimeToHms(end - begin), map.toString().getBytes().length));

        return true;
    }


}
