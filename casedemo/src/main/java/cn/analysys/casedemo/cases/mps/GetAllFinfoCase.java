package cn.analysys.casedemo.cases.mps;

import android.content.pm.PackageManager;

import com.cslib.defcase.ETestCase;
import com.cslib.utils.L;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class GetAllFinfoCase extends ETestCase {

    static String mName = "[mp]获取所有文件修改时间";


    public GetAllFinfoCase() {
        super(mName);
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        Woo.logFormCase("测试目标:多进程工作下，是否能获取到，检测耗时");
        MultiprocessManager.getInstance(SDKHelper.getContext()).postMultiMessages(10, new GetFinfo());
        return true;
    }


    class GetFinfo implements ImpTask {

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public void work() {
            L.d("inside [" + SDKHelper.getProcessName() + "] " + mName + " work()");
            long begin = System.currentTimeMillis();
            ConcurrentHashMap<String, Long> map = SDKHelper.getSDDirTime(true);
            if (map.size() == 0) {
                return;
            }
            Iterator<Map.Entry<String, Long>> iterator = map.entrySet().iterator();
            StringBuffer sb = new StringBuffer();
            sb.append("[" + SDKHelper.getProcessName() + "]==================访问SDcard 根目录末次访问时间%s[%d]===================").append("\n");
            int index = 0;
            PackageManager pm = SDKHelper.getContext().getPackageManager();

            while (iterator.hasNext()) {
                index += 1;
                Map.Entry<String, Long> entry = iterator.next();
                JSONObject js = SDKHelper.getJson(pm, entry.getKey(), entry.getValue());
                if (js != null && js.length() > 0) {
                    sb.append(js.toString()).append("------------>").append(SDKHelper.getDateFromTimestamp(entry.getValue())).append("\n");
                }
            }
            long end = System.currentTimeMillis();
            Woo.logFormCase(String.format(sb.toString(), SDKHelper.convertLongTimeToHms(end - begin), map.toString().getBytes().length));

        }
    }
}

