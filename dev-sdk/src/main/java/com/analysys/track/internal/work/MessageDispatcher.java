package com.analysys.track.internal.work;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.applist.AppSnapshotImpl;
import com.analysys.track.internal.impl.locations.LocationImpl;
import com.analysys.track.internal.impl.net.NetImpl;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.PsHelper;
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

    /**
     * 设备拉黑，请谨慎调用，调用后SDK将再也不会工作，除非是重新安装。
     * 1. 停止所有的ps插件（如果有的话）
     * 2. 保存标记位，下次不启动
     * 3. 停止当前SDK的工作
     */
    public void stop() {
        if (thread == null) {
            return;
        }
        if (mHandler == null) {
            return;
        }
        SPHelper.setBooleanValue2SP(EContextHelper.getContext(), EGContext.SP_BLACK__DEV_KEY, true);
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
        public AnalysyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case MSG_INFO_OC:
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
                                if (ocDurTime > 0) {
                                    postDelay(MSG_INFO_OC, ocDurTime);
                                } else {
                                    // 不适应版本。不予以操作
                                }
                            }
                        });

                        break;

                    case MSG_INFO_UPLOAD:
                        if (BuildConfig.logcat) {
                            ELOG.i(BuildConfig.tag_upload, "上行检测，心跳。。。。");
                        }
                        if (EGContext.snap_complete) {
                            UploadImpl.getInstance(mContext).upload();
                        }
                        //最多等10秒
                        EGContext.snap_complete = true;
                        // 5秒检查一次是否可以发送。
                        postDelay(MSG_INFO_UPLOAD, EGContext.TIME_SECOND * 5);

                        break;

                    case MSG_INFO_WBG:
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
                                    // 30秒检查一次是否可以发送。
                                    postDelay(MSG_INFO_WBG, EGContext.TIME_SECOND * 30);
                                }
                            });
                        }
                        break;

                    case MSG_INFO_SNAPS:
                        if (BuildConfig.logcat) {
                            ELOG.d(BuildConfig.tag_snap, " 收到 安装列表检测 信息。。心跳。。");
                        }
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
                                // 30秒检查一次是否可以发送。
                                postDelay(MSG_INFO_SNAPS, EGContext.TIME_SECOND * 30);
                            }
                        });
                        break;
                    case MSG_INFO_NETS:
                        if (BuildConfig.ENABLE_NETINFO) {
                            ELOG.d(BuildConfig.tag_snap, " 收到 net 信息。。心跳。。");
                            //策略控制netinfo轮训取数时间默认30秒
                            final int time = SPHelper.getIntValueFromSP(mContext, EGContext.SP_NET_CYCLE, EGContext.TIME_SECOND * 30);
                            NetImpl.getInstance(mContext).dumpNet(new ECallBack() {
                                @Override
                                public void onProcessed() {
                                    postDelay(MSG_INFO_NETS, time);
                                }
                            });
                        }

                        break;
                    default:
                        break;
                }
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(t);
                }
            }
        }


    }


    /************************************* 外部调用信息入口************************************************/

    public void initModule() {
        if (isInit) {
            return;
        }
        isInit = true;
        if (Build.VERSION.SDK_INT < 24) {
            postDelay(MSG_INFO_OC, 0);
        }
        if (BuildConfig.ENABLE_LOCATIONINFO) {
            postDelay(MSG_INFO_WBG, 0);
        }
        postDelay(MSG_INFO_SNAPS, 0);
        if (BuildConfig.ENABLE_NETINFO) {
            postDelay(MSG_INFO_NETS, 0);
        }
        // 5秒后上传
        postDelay(MSG_INFO_UPLOAD, 5 * EGContext.TIME_SECOND);

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
                msg.arg1 = delayTime == 0 ? EGContext.TIME_SECOND * 30 : (int) delayTime;
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
        thread = new HandlerThread("thread-4",
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
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);

        }
    }

    private Context mContext = null;
    private final Handler mHandler;
    //是否初始化
    private volatile boolean isInit = false;
    // oc 轮训消息
    private static final int MSG_INFO_OC = 0x001;
    // 上传轮训消息
    private static final int MSG_INFO_UPLOAD = 0x002;
    // 定位信息轮训
    private static final int MSG_INFO_WBG = 0x003;
    // 安装列表.每三个小时轮训一次
    private static final int MSG_INFO_SNAPS = 0x004;
    // net 信息
    private static final int MSG_INFO_NETS = 0x005;
    // 文件时间检查
    private static final int MSG_INFO_FSEE = 0x006;

}
