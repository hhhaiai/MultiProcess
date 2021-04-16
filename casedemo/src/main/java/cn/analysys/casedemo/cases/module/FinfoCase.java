package cn.analysys.casedemo.cases.module;

import android.content.pm.PackageManager;

import org.json.JSONObject;

import java.util.Map;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;
import me.hhhaiai.testcaselib.defcase.ETestCase;
import me.hhhaiai.testcaselib.utils.L;

public class FinfoCase extends ETestCase {

    public FinfoCase() {
        super("Finfo模块工作内容测试");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {

        new Thread(() -> {
            try {
                gotoWork();
            } catch (Throwable e) {
                L.e();
            }
        }).start();

        return true;
    }

    private void gotoWork() {
        StringBuffer sb = new StringBuffer();


        long begin = System.currentTimeMillis();
        SDKHelper.realGetFlt();
        Map<String, Long> mt = SDKHelper.getMemDataForTest();
        if (mt == null || mt.size() < 1) {
            return;
        }
        long end = System.currentTimeMillis();
        sb.append(
                String.format("==================Finfo模块测试[%d](%s)================"
                        , mt.size()
                        , SDKHelper.convertLongTimeToHms(end - begin))
        )
                .append("\n");
        PackageManager pm = SDKHelper.getContext().getPackageManager();
        for (Map.Entry<String, Long> entry : mt.entrySet()) {
            JSONObject js = SDKHelper.getJson(pm, entry.getKey(), entry.getValue());
            if (js != null && js.length() > 0) {
                sb.append(js.toString())
                        .append("-------------")
                        .append(SDKHelper.getDateFromTimestamp(entry.getValue()))
                        .append("\n");
            }
        }

        Woo.logFormCase(sb.toString());
    }


}