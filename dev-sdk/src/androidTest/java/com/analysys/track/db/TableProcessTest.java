package com.analysys.track.db;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;
import com.analysys.track.utils.ELOG;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

public class TableProcessTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void insertXXX() {
    }

    @Test
    public void deleteXXX() {
    }

    @Test
    public void deleteByIDXXX() {
    }

    @Test
    public void selectXXX() {
    }

    @Test
    public void insertOC() {
    }

    @Test
    public void selectOC() {
    }

    @Test
    public void deleteOC() {
    }

    @Test
    public void deleteAll() {
    }

    @Test
    public void insertLocation() {
    }

    @Test
    public void selectLocation() {
    }

    @Test
    public void deleteLocation() {
    }

    @Test
    public void deleteAllLocation() {
    }

    @Test
    public void insertTempId() {
    }

    @Test
    public void selectTempId() {
    }

    @Test
    public void deleteTempId() {
    }

    @Test
    public void insertSnapshot() {
    }

    @Test
    public void insertSnapshot1() {
    }

    @Test
    public void updateSnapshot() {
    }

    @Test
    public void selectSnapshot() {
    }

    @Test
    public void resetSnapshot() {
    }

    @Test
    public void deleteAllSnapshot() {
    }

    @Test
    public void getInstance() {
    }

    /*************************************************************************************************************/
    /************************************ Finfo的数据库测试 ********************************************/
    /*************************************************************************************************************/

    @Test
    public void testFlushMemFInfo() {
        Map<String, Long> data = new ConcurrentHashMap<String, Long>();
        data.put("c.d.e", 11111L);
        data.put("a.b.c", 11111L);
        TableProcess.getInstance(mContext).flushMemFInfo(data);

    }

    @Test
    public void testFlushMemFInfoSameKeyOnlyUpdate() {
        //同样的key更新值不增加行
        Map<String, Long> data = new ConcurrentHashMap<String, Long>();
        data.put("c.d.e", 222222L);
        data.put("a.b.c", 222222L);
        TableProcess.getInstance(mContext).flushMemFInfo(data);
    }

    @Test
    public void testLoadMemFinfo() {
        Map<String, Long> m = TableProcess.getInstance(mContext).loadMemFinfo();
        ELOG.i("Query data:" + m.toString());
    }

    @Test
    public void testFlushUploadFInfo() {
    }


    @Test
    public void testSelectFinfo() {
    }

    @Test
    public void testDeleteFinfo() {
    }
}