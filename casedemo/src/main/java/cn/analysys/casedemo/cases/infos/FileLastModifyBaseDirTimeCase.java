package cn.analysys.casedemo.cases.infos;

import com.analysys.track.utils.MDate;
import com.cslib.defcase.ETestCase;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.analysys.casedemo.cases.utils.Woo;
import cn.analysys.casedemo.sdkimport.Helper;

public class FileLastModifyBaseDirTimeCase extends ETestCase {


    public FileLastModifyBaseDirTimeCase() {
        super("[时间]末次修改根文件夹");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        ConcurrentHashMap<String, Long> map = Helper.getFileAndCacheTime();
        if (map.size() == 0) {
            return false;
        }
        Iterator<Map.Entry<String, Long>> iterator = map.entrySet().iterator();
        StringBuffer sb = new StringBuffer();
        sb.append("访问SDcard 根目录末次访问时间:").append("\n");
        int index = 0;
        while (iterator.hasNext()) {
            index+=1;
            Map.Entry entry = iterator.next();
            sb.append("[").append(index).append("]").append(entry.getKey()).append(" : [").append(entry.getValue()).append("] ---->").append(MDate.formatLongTimeToDate((Long) entry.getValue())).append("\n");
        }
        Woo.logFormCase(sb.toString());

        return true;
    }
}
