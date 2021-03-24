package cn.analysys.casedemo.cases.logics;

import android.content.pm.PackageManager;

import com.cslib.defcase.ETestCase;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.analysys.casedemo.utils.LoopRun;
import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;


/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: SDcard缓存目录的最新缓存时间
 * @Version: 1.0
 * @Create: 2021/03/74 14:29:04
 * @author: sanbo
 */
public class TodaylmfCase extends ETestCase {


    public TodaylmfCase() {
        super(String.format("[%s]当日应用活跃", SDKHelper.getToday()));
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        new Thread(() -> {
            LoopRun.getInstance(SDKHelper.getContext()).init(new LoopRun.Worker() {
                @Override
                public void goWork(LoopRun.ICall callback) {
                    getInfoAndPrint();
                    callback.onProcessed();
                }
            }, 5 * 1000, 30 * 1000);
        }).start();
        return true;
    }

    /**
     * 真正处理处理工作
     */
    private void getInfoAndPrint() {


        long begin = System.currentTimeMillis();
        ConcurrentHashMap<String, Long> map = SDKHelper.getSDDirTime();
        if (map.size() == 0) {
            return;
        }
        Iterator<Map.Entry<String, Long>> iterator = map.entrySet().iterator();
        StringBuffer sb = new StringBuffer();
        sb.append("===============[↓↓↓今天数据↓↓↓] 所有数据获取时间:%s, 占用内存大小:%d [↓↓↓今天数据↓↓↓]==================").append("\n");
        int index = 0;
        PackageManager pm = SDKHelper.getContext().getPackageManager();

        while (iterator.hasNext()) {
            index += 1;
            Map.Entry<String, Long> entry = iterator.next();
            String pkg = entry.getKey();
            long lastT = entry.getValue();
            boolean isToday = SDKHelper.isToday(lastT);
//            L.v("[" + pkg + "] " + lastT + "----->" + isToday);

            if (isToday) {
                JSONObject js = SDKHelper.getJson(pm, pkg, lastT);
                if (js != null && js.length() > 0) {
                    sb.append(js.toString())
                            .append("------------>")
                            .append(SDKHelper.getDateFromTimestamp(lastT))
                            .append("\n");
                }
            }

        }
        long end = System.currentTimeMillis();
        Woo.logFormCase(String.format(sb.toString(), SDKHelper.convertLongTimeToHms(end - begin), map.toString().getBytes().length));

    }


}
