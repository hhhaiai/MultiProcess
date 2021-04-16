package cn.analysys.casedemo.cases.mps;

import android.content.pm.PackageManager;

import com.cslib.defcase.ETestCase;
import com.cslib.utils.L;

import org.json.JSONObject;

import java.util.Map;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;
import me.hhhaiai.ImpTask;
import me.hhhaiai.multiprocess.MultiprocessManager;


/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: 多进程获取文件修改时间
 * @Version: 1.0
 * @Create: 2021/04/106 16:51:05
 * @author: sanbo
 */
public class FinfoModuleCase extends ETestCase {

    static String mName = "[mp]Finfo模快工作测试";


    public FinfoModuleCase() {
        super(mName);
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        Woo.logFormCase("测试目标:多进程工作下,Finfo只有一个进程在工作.");
        MultiprocessManager.getInstance(SDKHelper.getContext()).postMultiMessages(10, new FModule());
        return true;
    }


    class FModule implements ImpTask {

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public void work() {
            L.d("inside [" + SDKHelper.getProcessName() + "] " + mName + " work()");
            StringBuffer sb = new StringBuffer();
            long begin = System.currentTimeMillis();
            SDKHelper.tryGetFileTime();
            Map<String, Long> mt = SDKHelper.getMemDataForTest();
            if (mt == null || mt.size() < 1) {
                return;
            }
            long end = System.currentTimeMillis();
            sb.append(
                    String.format("[" + SDKHelper.getProcessName() + "]==================Finfo模块测试[%d](%s)================"
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
}

