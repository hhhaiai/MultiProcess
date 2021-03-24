package cn.analysys.casedemo.cases.logics;

import com.cslib.defcase.ETestCase;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: 文件修改时间查看，查看<b>/sdcard/Android/data/${pkg}/files<b/>和<b>/sdcard/Android/data/${pkg}/cache<b/>
 * @Version: 1.0
 * @Create: 2021/03/74 14:25:36
 * @author: sanbo
 */
public class FileLastModifyBaseDirTimeCase extends ETestCase {


    public FileLastModifyBaseDirTimeCase() {
        super("[时间]根文件夹修改时间");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        long begin = System.currentTimeMillis();
        ConcurrentHashMap<String, Long> map = SDKHelper.getFileAndCacheTime();
        if (map.size() == 0) {
            return false;
        }
        Iterator<Map.Entry<String, Long>> iterator = map.entrySet().iterator();
        StringBuffer sb = new StringBuffer();
        sb.append("访问SDcard根目录末次访问时间[总耗时:%s]:").append("\n");
        int index = 0;
        while (iterator.hasNext()) {
            index += 1;
            Map.Entry<String, Long> entry = iterator.next();
            sb.append("[")
                    .append(index)
                    .append("]")
                    .append(entry.getKey())
                    .append(" : [")
                    .append(entry.getValue())
                    .append("] ---->")
                    .append(SDKHelper.getDateFromTimestamp(entry.getValue()))
                    .append("\n");
        }
        long end = System.currentTimeMillis();

        Woo.logFormCase(String.format(sb.toString(), SDKHelper.convertLongTimeToHms(end - begin)));

        return true;
    }
}
