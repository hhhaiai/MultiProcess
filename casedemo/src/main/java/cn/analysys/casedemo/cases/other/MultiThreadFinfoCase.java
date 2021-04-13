package cn.analysys.casedemo.cases.other;

import android.content.pm.PackageManager;

import com.cslib.defcase.ETestCase;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

public class MultiThreadFinfoCase extends ETestCase {

    public MultiThreadFinfoCase() {
        super("多线程测试Finfo");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {

        for (int i = 0; i < 50; i++) {
            SDKHelper.runOnWorkThread(() -> {
                getInfoAndPrint();
            });
        }

        return true;
    }

    /**
     * 真正处理处理工作
     */
    private void getInfoAndPrint() {

        long begin = System.currentTimeMillis();
        ConcurrentHashMap<String, Long> map = SDKHelper.getSDDirTime(false);
        if (map.size() == 0) {
            return;
        }
        Iterator<Map.Entry<String, Long>> iterator = map.entrySet().iterator();
        StringBuffer sb = new StringBuffer();
        sb.append("===============[多线程测试当天数据] 所有数据获取时间:%s, 占用内存大小:%d [↓↓↓今天数据↓↓↓]==================").append("\n");
        int index = 0;
        PackageManager pm = SDKHelper.getContext().getPackageManager();

        while (iterator.hasNext()) {
            index += 1;
            Map.Entry<String, Long> entry = iterator.next();
            long lastT = entry.getValue();
            if (SDKHelper.isToday(lastT)) {
                JSONObject js = SDKHelper.getJson(pm, entry.getKey(), lastT);
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