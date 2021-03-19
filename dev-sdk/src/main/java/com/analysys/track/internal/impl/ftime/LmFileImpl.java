package com.analysys.track.internal.impl.ftime;

import android.content.ContentValues;
import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.work.ECallBack;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EThreadPool;

import org.json.JSONArray;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: last modify by file impl
 * @Version: 1.0
 * @Create: 2021/03/77 18:13:11
 * @author: sanbo
 */
public class LmFileImpl {
    public void tryGetFileTime(final ECallBack callback) {
        EThreadPool.runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                try {
                    realGetFlt(callback);
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(BuildConfig.tag_finfo, e);
                    }
                }
            }
        });
    }

    // 数据结构： [包名:上次活跃时间]
    private Map<String, Long> mMapAndTimes = new ConcurrentHashMap<String, Long>();
    public  Map<String, Long> getMemoryData(){
        return mMapAndTimes;
    }

    /**
     * 工作逻辑:
     * 1. 内存: 从0到1
     * 行动:  加载数据库中数据可用数据到内存,获取活跃列表时间
     * 2. 内存: 从1更新2
     * 行动: 获取活跃列表时间-内存对比-保持最新，保存最新状态[后续工作->闭合数据]
     * </p>
     * 安全机制:
     * 1. 进程锁保护10秒只能进行一次操作
     * 2. 内存持有一份最新的数据,方便快速对比 / 数据库直接取
     *
     * @param callback
     */
    public void realGetFlt(ECallBack callback) {

        // TODO load to memory
//        if (mMapAndTimes.size() == 0) {
//            TableProcess.getInstance(mContext).loadLmf();
//        }
        List<LmFileUitls.AppTime> ats = LmFileUitls.getLastAliveTimeInSD(mContext);
        Map<String, Long> willFlushData = new ConcurrentHashMap<String, Long>();
        for (LmFileUitls.AppTime at : ats) {
            String pkg = at.getPackageName();
            long lastActiveTime = at.getLastActiveTime();
            if (!mMapAndTimes.containsKey(pkg)) {
                mMapAndTimes.put(pkg, lastActiveTime);
                willFlushData.put(pkg, lastActiveTime);
            } else {
                long activtyInMemory = mMapAndTimes.get(pkg);
                if (lastActiveTime != activtyInMemory) {
                    mMapAndTimes.put(pkg, lastActiveTime);
                    willFlushData.put(pkg, lastActiveTime);
                }
            }
        }
        flushToDB(willFlushData);
    }

    /**
     * 保存数据到DB
     *
     * @param cacheData
     */
    private void flushToDB(Map<String, Long> cacheData) {
        ContentValues cv = new ContentValues();
        TableProcess.getInstance(mContext).insertLmf(cv);
    }


    /********************* get instance begin **************************/
    public static LmFileImpl getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private LmFileImpl initContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return HLODER.INSTANCE;
    }


    private static class HLODER {
        private static final LmFileImpl INSTANCE = new LmFileImpl();
    }

    private LmFileImpl() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/


}
