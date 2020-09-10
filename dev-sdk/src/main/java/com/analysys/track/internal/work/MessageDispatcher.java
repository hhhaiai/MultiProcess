package com.analysys.track.internal.work;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.impl.net.NetImpl;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.PsHelper;
import com.analysys.track.utils.reflectinon.DebugDev;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.analysys.track.utils.sp.SPHelper;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 消息分发
 * @Version: 1.0
 * @Create: 2019-08-05 14:57:53
 * @author: sanbo
 */
public class MessageDispatcher {


    private final HandlerThread thread;

    public void stop() {
        if (thread == null) {
            return;
        }
        if (mHandler == null) {
            return;
        }
        SPHelper.setBooleanValue2SP(EContextHelper.getContext(),EGContext.SP_BLACK__DEV_KEY,true);
        try {
            PsHelper.getInstance().stopAllPlugin();
        } catch (Throwable e) {
        }
        try {
            mHandler.removeCallbacksAndMessages(null);
            thread.quit();
        } catch (Throwable e) {
        }

    }


    /**
     * @Copyright © 2018 Analysys Inc. All rights reserved.
     * @Description: 真正的消息处理
     * @Version: 1.0
     * @Create: 2018年9月12日 下午3:01:44
     * @Author: sanbo
     */
    class AnalysyHandler extends Handler {

        private long mRunCaseTime = 0;

        AnalysyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                checkDebugStatus();

                mRunCaseTime += 5000;

                if (mRunCaseTime % 5000 == 0) {
                    // 5秒一次的任务
                    uploadRun();
                }
                if (mRunCaseTime % 30000 == 0) {
                    //30秒每次的任务
                    locationRun();
                    appSnapRun();
                }


                long ocDurTime = OCImpl.getInstance(mContext).getOCDurTime();
                if (ocDurTime > 0 && mRunCaseTime % ocDurTime == 0) {
                    ocRun();
                }

                final int netDurTime = SPHelper.getIntValueFromSP(mContext, EGContext.SP_NET_CYCLE, EGContext.TIME_SECOND * 30);
                if (netDurTime > 0 && mRunCaseTime % netDurTime == 0) {
                    netinfoRun();
                }
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(t);
                }
            }
            postDelay(0, 5000);
        }


    }

    private void netinfoRun() {
        try {
            if (BuildConfig.ENABLE_NETINFO) {
                ELOG.d(BuildConfig.tag_snap, " 收到 net 信息。。心跳。。");
                //策略控制netinfo轮训取数时间默认30秒
                final int time = SPHelper.getIntValueFromSP(mContext, EGContext.SP_NET_CYCLE, EGContext.TIME_SECOND * 30);
                NetImpl.getInstance(mContext).dumpNet(new ECallBack() {
                    @Override
                    public void onProcessed() {
                    }
                });
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private void ocRun() {
        try {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_oc, "收到OC消息。心跳。。。");
            }
            // 调用OC，等待处理完毕后，回调处理对应事务
            OCImpl.getInstance(mContext).processOCMsg(new ECallBack() {
                @Override
                public void onProcessed() {
                    // 根据版本获取OC循环时间
                    long ocDurTime = OCImpl.getInstance(mContext).getOCDurTime();
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_oc, "收到OC处理完毕的回调。。。。下次处理时间间隔: " + ocDurTime);
                    }
                }
            });
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private void uploadRun() {
        try {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_upload, "上行检测，心跳。。。。");
            }
            if (EGContext.snap_complete) {
                UploadImpl.getInstance(mContext).upload();
            }
            //最多等10秒
            EGContext.snap_complete = true;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private void appSnapRun() {
        try {
            AppSnapshotImpl.getInstance(mContext).snapshotsInfo(new ECallBack() {
                @Override
                public void onProcessed() {

                    try {
                        Intent intent = new Intent(EGContext.ACTION_MTC_LOCK);
                        EContextHelper.getContext(mContext).sendBroadcast(intent);
                    } catch (Throwable e) {
                    }
                    if (BuildConfig.logcat) {
                        ELOG.d(BuildConfig.tag_snap, "收到安装列表检测回调。。30秒后继续发起请求。。。");
                    }
                }
            });
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private void locationRun() {
        try {
            if (BuildConfig.ENABLE_LOCATIONINFO) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_loc, "收到定位信息。。。。");
                }
                LocationImpl.getInstance(mContext).tryGetLocationInfo(new ECallBack() {

                    @Override
                    public void onProcessed() {
                        if (BuildConfig.logcat) {
                            ELOG.i(BuildConfig.tag_loc, "收到定位信息回调。。30秒后继续发起请求。。。");
                        }
                    }
                });
            }

            if (BuildConfig.logcat) {
                ELOG.d(BuildConfig.tag_snap, " 收到 安装列表检测 信息。。心跳。。");
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private boolean isDebugProcess = false;

    /**
     * 设备状态监测
     */
    private void checkDebugStatus() {
        try {
            if (isDebugProcess) {
                // 已经处理过了，不在处理
                return;
            }
            /**
             * 调试设备直接发起清除
             */
            if (DebugDev.get(mContext).isDebugDevice()) {
                isDebugProcess = true;
                PatchHelper.clear(mContext);
                return;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }


    /************************************* 外部调用信息入口************************************************/

    public void initModule() {
        if (isInit) {
            return;
        }
        isInit = true;
        postDelay(0, 5 * EGContext.TIME_SECOND);
    }


    /************************************* 发送消息************************************************/

    /**
     * 延迟发送
     *
     * @param what
     * @param delayTime
     */
    private void postDelay(int what, long delayTime) {
        try {
            if (mHandler != null && !mHandler.hasMessages(what) && thread != null && thread.isAlive()) {
                Message msg = Message.obtain();
                msg.what = what;
                mHandler.sendMessageDelayed(msg, delayTime > 0 ? delayTime : 0);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }

    }

    /************************************* 单例: 初始化************************************************/

    private MessageDispatcher() {
        thread = new HandlerThread("thread",
                android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
        thread.start();
        mHandler = new AnalysyHandler(thread.getLooper());
    }

    private static class Holder {
        private static final MessageDispatcher INSTANCE = new MessageDispatcher();
    }

    public static MessageDispatcher getInstance(Context context) {
        Holder.INSTANCE.init(context);
        return Holder.INSTANCE;
    }

    private void init(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext();

        }
    }

    private Context mContext = null;
    private final Handler mHandler;
    //是否初始化
    private volatile boolean isInit = false;
}
