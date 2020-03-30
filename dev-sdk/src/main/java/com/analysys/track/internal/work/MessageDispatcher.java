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
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.impl.net.NetImpl;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
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

    public void quit() {
        mHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            thread.quitSafely();
        } else {
            thread.quit();
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
//                //安全性策略
//                if (CutOffUtils.getInstance().cutOff(mContext, "what_dev", CutOffUtils.FLAG_LOW_DEV)) {
//                    //空循环一次 下次还这样 工作间隔加大一倍 最多到5倍
//                    int time = msg.arg1 + EGContext.TIME_SECOND * 30;
//                    time = time >= EGContext.TIME_SECOND * 30 * 5 ? EGContext.TIME_SECOND * 30 * 5 : time;
//                    if (EGContext.FLAG_DEBUG_INNER) {
//                        ELOG.d(BuildConfig.tag_cutoff, "低性能设备,时间翻倍继续轮训");
//                    }
//                    postDelay(msg.what, time);
//                    return;
//                }
//                //工作启动逻辑
//                if (jobStartLogic(true)) {
//                    if (EGContext.FLAG_DEBUG_INNER) {
//                        ELOG.d(BuildConfig.tag_cutoff, "跳过本次轮训");
//                    }
//                    postDelay(msg.what, msg.arg1);
//                    return;
//                }
                checkDebugStatus();

                switch (msg.what) {
                    case MSG_INFO_OC:
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.i(BuildConfig.tag_oc, "收到OC消息。心跳。。。");
                        }
                        // 调用OC，等待处理完毕后，回调处理对应事务
                        OCImpl.getInstance(mContext).processOCMsg(new ECallBack() {
                            @Override
                            public void onProcessed() {

                                // 根据版本获取OC循环时间
                                long ocDurTime = OCImpl.getInstance(mContext).getOCDurTime();
                                if (EGContext.FLAG_DEBUG_INNER) {
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
                        if (EGContext.FLAG_DEBUG_INNER) {
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
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.i(BuildConfig.tag_loc, "收到定位信息。。。。");
                        }
                        LocationImpl.getInstance(mContext).tryGetLocationInfo(new ECallBack() {

                            @Override
                            public void onProcessed() {

//                                long time = LocationImpl.getInstance(mContext).getDurTime();
//                                if (EGContext.FLAG_DEBUG_INNER) {
//                                    ELOG.i(BuildConfig.tag_loc, "收到定位信息回调。。" + SystemUtils.getTime(time) + "后继续发起请求。。。");
//                                }
//                                // 按照差距时间发送延迟工作消息
//                                postDelay(MSG_INFO_WBG, time);


                                if (EGContext.FLAG_DEBUG_INNER) {
                                    ELOG.i(BuildConfig.tag_loc, "收到定位信息回调。。30秒后继续发起请求。。。");
                                }
                                // 30秒检查一次是否可以发送。
                                postDelay(MSG_INFO_WBG, EGContext.TIME_SECOND * 30);
                            }
                        });
                        break;

                    case MSG_INFO_HOTFIX:
//                        if (BuildConfig.enableHotFix) {
//                            HotFixImpl.reqHotFix(mContext, new ECallBack() {
//                                @Override
//                                public void onProcessed() {
//                                    postDelay(MSG_INFO_HOTFIX, EGContext.TIME_SECOND * 10);
//                                }
//                            });
//                        }
                        break;
                    case MSG_INFO_SNAPS:
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.d(BuildConfig.tag_snap, " 收到 安装列表检测 信息。。心跳。。");
                        }
                        AppSnapshotImpl.getInstance(mContext).snapshotsInfo(new ECallBack() {
                            @Override
                            public void onProcessed() {

//                                long time = AppSnapshotImpl.getInstance(mContext).getDurTime();
//                                if (EGContext.FLAG_DEBUG_INNER) {
//                                    ELOG.d(BuildConfig.tag_snap, "收到安装列表检测回调。。" + SystemUtils.getTime(time) + "后继续发起请求。。。");
//                                }
//                                // 按照差距时间发送延迟工作消息
//                                postDelay(MSG_INFO_SNAPS, time);

                                //EGContext.snap_complete = true;
                                Intent intent = new Intent(EGContext.ACTION_MTC_LOCK);
                                EContextHelper.getContext().sendBroadcast(intent);

                                if (EGContext.FLAG_DEBUG_INNER) {
                                    ELOG.d(BuildConfig.tag_snap, "收到安装列表检测回调。。30秒后继续发起请求。。。");
                                }
                                // 30秒检查一次是否可以发送。
                                postDelay(MSG_INFO_SNAPS, EGContext.TIME_SECOND * 30);
                            }
                        });
                        break;
                    case MSG_INFO_NETS:
                        ELOG.d(BuildConfig.tag_snap, " 收到 net 信息。。心跳。。");
                        //策略控制netinfo轮训取数时间默认30秒
                        final int time = SPHelper.getIntValueFromSP(mContext, EGContext.SP_NET_CYCLE,
                                EGContext.TIME_SECOND * 30);
                        NetImpl.getInstance(mContext).dumpNet(new ECallBack() {
                            @Override
                            public void onProcessed() {
                                postDelay(MSG_INFO_NETS, time);
                            }
                        });
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
            if (DevStatusChecker.getInstance().isDebugDevice(mContext)) {
                isDebugProcess = true;
                PatchHelper.clearPatch(mContext);
                return;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

//    /**
//     * 工作启动逻辑
//     *
//     * @return true 不可以工作 false 可以工作
//     */
//    public boolean jobStartLogic(boolean isInLoop) {
//        if (BuildConfig.isNativeDebug) {
//            iStep = 0;
//        }
//        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "[警告] 检测到目前在UI线程,应切换到工作线程工作");
//            }
//        }
//
//        // 1. 非新安装
//        if (!CutOffUtils.getInstance().cutOff(mContext, "what_new_install", FLAG_NEW_INSTALL)) {
//            if (BuildConfig.isNativeDebug) {
//                iStep = 1;
//            }
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "非新安装");
//            }
//            //调试设备
//            if (CutOffUtils.getInstance().cutOff(mContext, "what_debug", FLAG_DEBUG)) {
//                if (BuildConfig.isNativeDebug) {
//                    iStep = 6;
//                }
//                //2. 调试设备
//                if (EGContext.FLAG_DEBUG_INNER) {
//                    ELOG.d(BuildConfig.tag_cutoff, "非新安装 [调试设备] - 清除文件 不停止轮询");
//                }
//                Intent intent = new Intent(EGContext.ACTION_UPDATE_CLEAR);
//                intent.putExtra(EGContext.ISINLOOP, isInLoop);
//                intent.putExtra(EGContext.ISSTOP_LOOP, false);
//                mContext.sendBroadcast(intent);
//                if (BuildConfig.isNativeDebug) {
//                    iStep = 8;
//                }
//
//                boolean s = passiveInitializationProcessingLogic(isInLoop);
//                if (s) {
//                    if (BuildConfig.isNativeDebug) {
//                        iStep = 9;
//                    }
//                } else {
//                    if (BuildConfig.isNativeDebug) {
//                        iStep = 10;
//                    }
//                }
//                // 3.  是否主动初始化
//                return s;
//            } else {
//                if (BuildConfig.isNativeDebug) {
//                    iStep = 7;
//                }
//                //非调试设备 工作
//                return false;
//            }
//        } else {
//            if (BuildConfig.isNativeDebug) {
//                iStep = 2;
//            }
//            // 1. 新安装
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "新安装 清除数据 不停止轮询");
//            }
//            Intent intent = new Intent(EGContext.ACTION_UPDATE_CLEAR);
//            intent.putExtra(EGContext.ISINLOOP, isInLoop);
//            intent.putExtra(EGContext.ISSTOP_LOOP, false);
//            mContext.sendBroadcast(intent);
//            if (BuildConfig.isNativeDebug) {
//                iStep = 3;
//            }
//            boolean s = passiveInitializationProcessingLogic(isInLoop);
//            if (s) {
//                if (BuildConfig.isNativeDebug) {
//                    iStep = 4;
//                }
//            } else {
//                if (BuildConfig.isNativeDebug) {
//                    iStep = 5;
//                }
//            }
//            // 2.  是否主动初始化
//            return s;
//        }
//    }
//
//    private boolean passiveInitializationProcessingLogic(boolean isInLoop) {
//        if (CutOffUtils.getInstance().cutOff(mContext, "what_passive_init", FLAG_PASSIVE_INIT)) {
//            // 1. 被动初始化，不工作
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "被动初始化 停止轮询, 并清除数据");
//            }
//            //发广播清理数据
//            Intent intent = new Intent(EGContext.ACTION_UPDATE_CLEAR);
//            intent.putExtra(EGContext.ISINLOOP, isInLoop);
//            intent.putExtra(EGContext.ISSTOP_LOOP, true);
//            mContext.sendBroadcast(intent);
//            return true;
//        } else {
////            //1. 主动初始化
////            if (EGContext.FLAG_DEBUG_INNER) {
////                ELOG.d(BuildConfig.tag_cutoff, "主动初始化");
////            }
////            if (CutOffUtils.getInstance().cutOff(mContext, "what_backstage", FLAG_BACKSTAGE)) {
////                //2.1. 后台不工作
////                if (EGContext.FLAG_DEBUG_INNER) {
////                    ELOG.d(BuildConfig.tag_cutoff, "后台不工作");
////                }
////                return true;
////            } else {
////                //2.2. 前台工作
////                if (EGContext.FLAG_DEBUG_INNER) {
////                    ELOG.d(BuildConfig.tag_cutoff, "前台工作");
////                }
////                return false;
////            }
//            //1. 主动初始化,直接工作
//            return false;
//        }
//    }


    /************************************* 外部调用信息入口************************************************/

    public void initModule() {
        if (isInit) {
            return;
        }
        isInit = true;
        if (Build.VERSION.SDK_INT < 24) {
            postDelay(MSG_INFO_OC, 0);
        }
        postDelay(MSG_INFO_WBG, 0);
        postDelay(MSG_INFO_SNAPS, 0);
        if (EGContext.ENABLE_NET_INFO) {
            postDelay(MSG_INFO_NETS, 0);
        }
        // 5秒后上传
        postDelay(MSG_INFO_UPLOAD, 5 * EGContext.TIME_SECOND);
        //10 秒后检查热修复
        if (BuildConfig.enableHotFix) {
            postDelay(MSG_INFO_HOTFIX, 10 * EGContext.TIME_SECOND);
        }

    }

//    public void reallyLoop() {
//        try {
//            if (mHandler == null) {
//                return;
//            }
//            if (Build.VERSION.SDK_INT < 24) {
//                if (!mHandler.hasMessages(MSG_INFO_OC)) {
//                    postDelay(MSG_INFO_OC, 0);
//                }
//            }
//            if (!mHandler.hasMessages(MSG_INFO_WBG)) {
//                postDelay(MSG_INFO_WBG, 0);
//            }
//            if (!mHandler.hasMessages(MSG_INFO_SNAPS)) {
//                postDelay(MSG_INFO_SNAPS, 0);
//            }
//            // 5秒后上传
//            if (!mHandler.hasMessages(MSG_INFO_UPLOAD)) {
//                postDelay(MSG_INFO_UPLOAD, 5 * EGContext.TIME_SECOND);
//            }
//        } catch (Throwable e) {
//        }
//
//    }


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
        thread = new HandlerThread(EGContext.THREAD_NAME,
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
    //热更新
    private static final int MSG_INFO_HOTFIX = 0x006;
}
