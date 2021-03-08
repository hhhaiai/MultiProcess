package cn.analysys.casedemo.cases.infos;

import com.cslib.defcase.ETestCase;

import java.util.List;

import cn.analysys.casedemo.sdkimport.Helper;
import cn.analysys.casedemo.utils.EL;

public class ELaseModifyTimeCase extends ETestCase {
    public ELaseModifyTimeCase() {
        super("文件末次修改");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {

        List<String> lastModifyTimeInfos = Helper.getLastAliveTimeStr();
        if (lastModifyTimeInfos.size() <= 0) {
            return false;
        } else {
            EL.i("文件个数:" + lastModifyTimeInfos.size());
            for (int i = 0; i < lastModifyTimeInfos.size(); i++) {
                EL.d(lastModifyTimeInfos.get(i));
            }
            return true;
        }
    }

}
