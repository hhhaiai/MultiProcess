package cn.analysys.casedemo.cases.module;

import android.content.pm.PackageManager;

import com.analysys.track.utils.MDate;
import com.cslib.defcase.ETestCase;
import com.cslib.utils.L;

import org.json.JSONObject;

import java.util.Map;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

public class FinfoCase extends ETestCase {

    public FinfoCase() {
        super("Finfo模块测试");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        try {
            StringBuffer sb = new StringBuffer();


            long begin = System.currentTimeMillis();
            SDKHelper.realGetFlt();
            Map<String, Long> mt = SDKHelper.getMemDataForTest();
            if (mt == null || mt.size() < 1) {
                return false;
            }
            long end = System.currentTimeMillis();
            sb.append(String.format("==================Finfo模块测试[%d](%s)================", mt.size(), MDate.convertLongTimeToHms(end - begin))).append("\n");
            PackageManager pm = SDKHelper.getContext().getPackageManager();
            for (Map.Entry<String, Long> entry : mt.entrySet()) {
                JSONObject js = SDKHelper.getJson(pm, entry.getKey(), entry.getValue());
                if (js != null && js.length() > 0) {
                    sb.append(js.toString()).append("-------------").append(MDate.getDateFromTimestamp(entry.getValue())).append("\n");
                }
            }

            Woo.logFormCase(sb.toString());
        } catch (Throwable e) {
            L.e(e);
            return false;
        }
        return true;
    }


}