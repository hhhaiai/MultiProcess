package com.analysys.track.internal.work;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;

import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.impl.oc.OCImpl;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.net.UploadImpl;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.ReceiverUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONObject;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 消息分发
 * @Version: 1.0
 * @Create: 2019-08-05 14:57:53
 * @author: sanbo
 */
public class MessageDispatcher {


    // 初始化各模块
    public void initModule() {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_INIT_MODULE;
            sendMessageDelay(msg);
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(t);
            }
        }
    }

    // 心跳检查
    public void checkHeartbeat(long delayTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_CHECK_HEARTBEAT;
            if (heartBeatLastTime == 0 || System.currentTimeMillis() - heartBeatLastTime >= delayTime) {
                heartBeatLastTime = System.currentTimeMillis();
                if (mHandler.hasMessages(msg.what)) {
                    mHandler.removeMessages(msg.what);
                }
                sendMessageDelay(msg);
            } else {
                if (!mHandler.hasMessages(msg.what)) {
                    sendMessageDelay(msg, delayTime);
                } else {
                    return;
                }
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(t);
            }
        }
    }

    /**
     * 重发数据轮询检查 确保Handler有任务， 如果没有进行初始化各个模块
     */
    public void isNeedRetry(long delayTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_CHECK_RETRY;
            if (reTryLastTime == 0 || (System.currentTimeMillis() - reTryLastTime >= delayTime)) {
                reTryLastTime = System.currentTimeMillis();
                if (mHandler.hasMessages(msg.what)) {
                    mHandler.removeMessages(msg.what);
                }
                sendMessageDelay(msg);
            } else {
                if (!mHandler.hasMessages(msg.what)) {
                    sendMessageDelay(msg, delayTime);
                } else {
                    return;
                }
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(t);
            }
        }
    }

    private void reTryUpload() {
        try {
            long upLoadCycle = PolicyImpl.getInstance(mContext).getSP()
                    .getLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL, EGContext.UPLOAD_CYCLE);
            if (uploadCycle != upLoadCycle) {
                uploadCycle = upLoadCycle;
            }
            int failCount = SPHelper.getIntValueFromSP(mContext, DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,
                    EGContext.FAIL_COUNT_DEFALUT);
            if (failCount > 0) {
                UploadImpl.getInstance(mContext).reTryAndUpload(false);
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(t);
            }
        }
    }

    // 启动服务任务接入
    public void startService() {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_START_SERVICE_SELF;
            sendMessageDelay(msg);
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(t);
            }
        }

    }

    // 启动服务任务接入
    public void screenStatusHandle(boolean on) {
        try {
            Message msg = new Message();
            if (on) {
                msg.what = MessageDispatcher.MSG_HANDLE_SCREEN_ON;
            } else {
                msg.what = MessageDispatcher.MSG_HANDLE_SCREEN_OFF;
            }
            sendMessageDelay(msg);
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(t);
            }
        }
    }

    // 停止工作
    public void killRetryWorker() {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_KILL_RETRY_WORKER;
            sendMessageDelay(msg);
        } catch (Throwable t) {
        }

    }

    // 应用安装卸载更新
    public void appChangeReceiver(String pkgName, int type, long time) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_APP_IUU;
            msg.arg1 = type;
            JSONObject o = new JSONObject();
            o.put(MSG_CHANGE_PKG, pkgName);
            o.put(MSG_CHANGE_TIME, time);
            msg.obj = o;
            sendMessageDelay(msg);
        } catch (Throwable t) {
        }
    }


    // 应用列表
    public void snapshotInfo(long cycleTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_SNAPSHOT;
            if (cycleTime > 0) {
                snapShotCycle = cycleTime;
            }
            if (snapShotLastTime == 0 || System.currentTimeMillis() - snapShotLastTime >= cycleTime) {
                snapShotLastTime = System.currentTimeMillis();
                if (mHandler.hasMessages(msg.what)) {
                    mHandler.removeMessages(msg.what);
                }
                sendMessageDelay(msg);
            } else {
                if (!mHandler.hasMessages(msg.what)) {
                    sendMessageDelay(msg, cycleTime);
                } else {
                    return;
                }
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(t);
            }
        }
    }

    // 位置信息
    public void locationInfo(long cycleTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_LOCATION;
            if (cycleTime > 0) {
                locationCycle = cycleTime;
            }
            if (locationLastTime == 0 || System.currentTimeMillis() - locationLastTime >= cycleTime) {
                locationLastTime = System.currentTimeMillis();
                if (mHandler.hasMessages(msg.what)) {
                    mHandler.removeMessages(msg.what);
                }
                sendMessageDelay(msg);
            } else {
                if (!mHandler.hasMessages(msg.what)) {
                    sendMessageDelay(msg, cycleTime);
                } else {
                    return;
                }
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(t);
            }
        }

    }


    /**
     * oc处理逻辑。
     *
     * @param nextProcessDurTime
     */
    public void ocInfo(long nextProcessDurTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_OC_INFO;
            if (nextProcessDurTime > 0) {
                ocCycle = nextProcessDurTime;
            }
            if (ocLastTime == 0 || System.currentTimeMillis() - ocLastTime >= nextProcessDurTime) {
                ocLastTime = System.currentTimeMillis();
                if (mHandler.hasMessages(msg.what)) {
                    mHandler.removeMessages(msg.what);
                }
                sendMessageDelay(msg);
            } else {
                if (!mHandler.hasMessages(msg.what)) {
                    sendMessageDelay(msg, nextProcessDurTime);
                } else {
                    return;
                }
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(t);
            }
        }

    }

    // 数据上传
    public void uploadInfo(long cycleTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_UPLOAD;
            if (cycleTime > 0) {
                uploadCycle = cycleTime;
            }
            if (uploadLastTime == 0 || System.currentTimeMillis() - uploadLastTime >= cycleTime) {
                uploadLastTime = System.currentTimeMillis();
                if (mHandler.hasMessages(msg.what)) {
                    mHandler.removeMessages(msg.what);
                }
                sendMessageDelay(msg);
            } else {
                if (!mHandler.hasMessages(msg.what)) {
                    sendMessageDelay(msg, cycleTime);
                } else {
                    return;
                }
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(t);
            }
        }

    }

    public void processScreenOnOff(final boolean on) {
        try {
            long currentTime = System.currentTimeMillis();
            if (on) {
                if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_SCREEN_ON_BROADCAST,
                        EGContext.TIME_SYNC_BROADCAST, currentTime)) {
                    if (SystemUtils.isMainThread()) {
                        EThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                screenOnOffHandle(on);
                            }
                        });

                    } else {
                        screenOnOffHandle(on);
                    }
                } else {
                    return;
                }
            } else {
                if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_SCREEN_OFF_BROADCAST,
                        EGContext.TIME_SYNC_BROADCAST, currentTime)) {
                    if (SystemUtils.isMainThread()) {
                        EThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                screenOnOffHandle(on);
                            }
                        });

                    } else {
                        screenOnOffHandle(on);
                    }

                } else {
                    return;
                }
            }


        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                ELOG.e(e);
            }
        }
    }

    /**
     * 锁屏补时间
     */
    private void screenOnOffHandle(boolean on) {
        try {
            //  第一次时间为空，则取sp时间
            if (mLastCloseTime == 0) {
                long now = System.currentTimeMillis();
                if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_SP_WRITER, EGContext.TIME_SYNC_SP, now)) {
                    mLastCloseTime = SPHelper.getLongValueFromSP(mContext,
                            EGContext.LAST_AVAILABLE_TIME, 0);
                    MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_SP_WRITER, now);
                } else {
                    return;
                }
            }
            if (mLastCloseTime == 0) {// 取完sp时间后依然为空，则为第一次锁屏，设置closeTime,准备入库
                mLastCloseTime = System.currentTimeMillis();
                SPHelper.setLongValue2SP(mContext, EGContext.LAST_AVAILABLE_TIME, mLastCloseTime);
                if (on) {
                    return;
                }
                OCImpl.mLastAvailableOpenOrCloseTime = mLastCloseTime;
                OCImpl.getInstance(mContext).closeOC(false, mLastCloseTime);
            } else {// sp里取到了数据，即，非第一次锁屏，则判断是否有效数据来设置closeTime,准备入库
                long currentTime = System.currentTimeMillis();
                try {
                    if (Build.VERSION.SDK_INT < 21) {
                        if (currentTime - mLastCloseTime < EGContext.OC_CYCLE) {
                            OCImpl.mCache = null;
                            return;
                        }
                    } else if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24) {
                        if (currentTime - mLastCloseTime < EGContext.OC_CYCLE_OVER_5) {
                            OCImpl.mCache = null;
                            return;
                        }
                    }
                } catch (Throwable t) {
                } finally {
                    SPHelper.setLongValue2SP(mContext, EGContext.LAST_AVAILABLE_TIME, currentTime);
                }
                if (on) {
                    return;
                }
                OCImpl.mLastAvailableOpenOrCloseTime = currentTime;
                OCImpl.getInstance(mContext).closeOC(true, mLastCloseTime);
            }
            ReceiverUtils.getInstance().unRegistAllReceiver(mContext);
        } catch (Throwable t) {
        } finally {
            if (on) {
                MessageDispatcher.getInstance(mContext).sendMessage();
            }
        }
        // 解开对应的进程锁
        if (on) {
            MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_SCREEN_ON_BROADCAST, System.currentTimeMillis());
        } else {
            MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.FILES_SYNC_SCREEN_OFF_BROADCAST, System.currentTimeMillis());
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
                    case MSG_INIT_MODULE:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("接收到初始化消息");
                        }
                        msgInitModule();
                        break;
                    case MSG_CHECK_HEARTBEAT:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("接收到心跳检测消息");
                        }
                        checkMsgInHeatbeat();
                        break;
                    case MSG_START_SERVICE_SELF:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("接收到启动服务消息");
                        }
                        ServiceHelper.getInstance(mContext).startSelfService();
                        break;
                    case MSG_KILL_RETRY_WORKER:
                        exitRetryHandler();
                        break;
                    case MSG_APP_IUU:
                        JSONObject js = null;
                        js = (JSONObject) msg.obj;
                        AppSnapshotImpl.getInstance(mContext).processAppModifyMsg(js.optString(MSG_CHANGE_PKG), msg.arg1,
                                js.optLong(MSG_CHANGE_TIME));
                        break;
                    case MSG_SCREEN_RECEIVER:
                        break;
                    case MSG_SNAPSHOT:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("接收到获取应用列表消息");
                        }
                        AppSnapshotImpl.getInstance(mContext).snapshotsInfo();
                        break;
                    case MSG_LOCATION:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("接收到获取地理位置消息");
                        }
                        LocationImpl.getInstance(mContext).processLoctionMsg();
                        break;
                    case MSG_OC_INFO:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("接收到获取OC消息");
                        }
                        OCImpl.getInstance(mContext).processOCMsg();
                        break;
                    case MSG_UPLOAD:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("接收到上传消息");
                        }
                        UploadImpl.getInstance(mContext).upload();
                        break;
                    case MSG_CHECK_RETRY:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("接收到重试检测消息");
                        }
                        MessageDispatcher.getInstance(mContext).reTryUpload();
                        break;

                    case MSG_CHECK:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("心跳检查");
                        }
//                    SPHelper.setLongValue2SP(mContext,EGContext.HEARTBEAT_LAST_TIME,System.currentTimeMillis());
                        // 本次发送
                        MessageDispatcher.getInstance(mContext).checkHeartbeat(EGContext.CHECK_HEARTBEAT_CYCLE);
//                        // 本次delay,用于轮询
//   MessageDispatcher.this.sendMessage();
                        break;
                    case MSG_RETRY:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("数据重发轮询检查");
                        }
                        MessageDispatcher.getInstance(mContext).isNeedRetry(EGContext.CHECK_RETRY_CYCLE);
                        checkRetry();
                        break;
                    case MSG_HANDLE_SCREEN_ON:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("开屏");
                        }
                        processScreenOnOff(true);
                        break;
                    case MSG_HANDLE_SCREEN_OFF:
                        if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                            ELOG.i("关屏");
                        }
                        processScreenOnOff(false);
                        break;
                    default:
                        break;
                }
            } catch (Throwable t) {
                if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                    ELOG.e(t);
                }
            }
        }

        /**
         * 心跳检测， 确保Handler有任务， 如果没有进行初始化各个模块
         */
        public void checkMsgInHeatbeat() {
            try {
                if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                    ELOG.i("-----checkMsgInHeatbeat--检查消息队列-->[  SNAPSHOT: " + mHandler.hasMessages(MSG_SNAPSHOT) + " ;  LOCATION:  " + mHandler.hasMessages(MSG_LOCATION) + " ;  OC:  "
                            + mHandler.hasMessages(MSG_OC_INFO) + " ; UPLOAD:  " + mHandler.hasMessages(MSG_UPLOAD) + "]");
                }
                if (mHandler.hasMessages(MSG_SNAPSHOT) || mHandler.hasMessages(MSG_LOCATION)
                        || mHandler.hasMessages(MSG_OC_INFO) || mHandler.hasMessages(MSG_UPLOAD)) {
                    if (mHandler.hasMessages(MSG_UPLOAD)) {
                        if (System.currentTimeMillis() - uploadLastTime >= uploadCycle) {
                            uploadInfo(uploadCycle);
                        }
                    } else {
                        MessageDispatcher.getInstance(mContext).uploadInfo(0);
                    }
                    if (mHandler.hasMessages(MSG_OC_INFO)) {
                        if (Build.VERSION.SDK_INT < 24 && (System.currentTimeMillis() - ocLastTime >= ocCycle)) {
                            ocInfo(ocCycle);
                        }
                    } else {
                        if (Build.VERSION.SDK_INT < 24) {
                            MessageDispatcher.getInstance(mContext).ocInfo(0);
                        }
                    }
                    if (mHandler.hasMessages(MSG_LOCATION)) {
                        if (System.currentTimeMillis() - locationLastTime >= locationCycle) {
                            locationInfo(locationCycle);
                        }
                    } else {
                        MessageDispatcher.getInstance(mContext).locationInfo(0);
                    }
                    if (mHandler.hasMessages(MSG_SNAPSHOT)) {
                        if (System.currentTimeMillis() - snapShotLastTime >= snapShotCycle) {
                            snapshotInfo(snapShotCycle);
                        }
                    } else {
                        MessageDispatcher.getInstance(mContext).snapshotInfo(0);
                    }
                } else {
                    MessageDispatcher.getInstance(mContext).initModule();
                }
            } catch (Throwable t) {
                if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                    ELOG.e(t);
                }
            }

        }

        /**
         * 用于启动各个模块， OC模块，snapshot模块，Location模块， 注册动态广播，启动心跳检测
         */
        private void msgInitModule() {
            try {
                prepareInit();
                ocInfo(0);
                snapshotInfo(0);
                locationInfo(0);
                uploadInfo(0);
                MessageDispatcher.this.sendMessage();
            } catch (Throwable t) {
                if (EGContext.FLAG_DEBUG_INNER && isDebug) {
                    ELOG.e(t);
                }
            }
        }

        public void exitRetryHandler() {
            try {
                Message msg = new Message();
                msg.what = MSG_RETRY;
                if (mHandler.hasMessages(msg.what)) {
                    mHandler.removeMessages(msg.what);
                }
            } catch (Throwable t) {
            }

        }
    }


    /**
     * 初始化接口
     */
    private void prepareInit() {
        try {
            if (mContext == null) {
                return;
            }
            // 补充时间
            String lastOpenTime = SPHelper.getStringValueFromSP(mContext, EGContext.LAST_OPEN_TIME, "");
            if (TextUtils.isEmpty(lastOpenTime)) {
                lastOpenTime = "0";
            }
            long randomCloseTime = SystemUtils.getCloseTime(Long.parseLong(lastOpenTime));
            SPHelper.setLongValue2SP(mContext, EGContext.END_TIME, randomCloseTime);
            OCImpl.getInstance(mContext).filterInsertOCInfo(EGContext.SERVICE_RESTART);
//            ReceiverUtils.getInstance().registAllReceiver(mContext);
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
            if (!isScreenOn) {
                ReceiverUtils.getInstance().setWork(false);
            }
        } catch (Throwable e) {

        }
    }

    /************************************* 发送消息************************************************/

    // 外部调用接口
    public void checkRetry() {
        Message msg = new Message();
        msg.what = MSG_RETRY;
        sendMessageDelay(msg, EGContext.CHECK_RETRY_CYCLE);
    }

    // 外部调用接口
    public void sendMessage() {
        Message msg = new Message();
        msg.what = MSG_CHECK;
        sendMessageDelay(msg);
    }

    private void sendMessageDelay(Message msg, long delayTime) {
        if (mHandler != null && !mHandler.hasMessages(msg.what)) {
            mHandler.sendMessageDelayed(msg, delayTime);
        }
    }

    private void sendMessageDelay(Message msg) {
        if (mHandler != null && !mHandler.hasMessages(msg.what)) {
            mHandler.sendMessage(msg);
        }
    }


    /****************************************************************************************************/
    /************************************* 单例: 初始化************************************************/
    /****************************************************************************************************/


    private MessageDispatcher() {
        final HandlerThread thread = new HandlerThread(EGContext.THREAD_NAME,
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
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

    private final String MSG_CHANGE_PKG = "pkgName";
    private final String MSG_CHANGE_TIME = "time";

    protected static final int MSG_INIT_MODULE = 0x01;
    protected static final int MSG_CHECK_HEARTBEAT = 0x02;
    protected static final int MSG_START_SERVICE_SELF = 0x03;
    protected static final int MSG_KILL_RETRY_WORKER = 0x04;
    protected static final int MSG_APP_IUU = 0x05;
    protected static final int MSG_SCREEN_RECEIVER = 0x06;
    protected static final int MSG_SNAPSHOT = 0x07;
    protected static final int MSG_LOCATION = 0x08;
    protected static final int MSG_OC_INFO = 0x09;
    protected static final int MSG_UPLOAD = 0x0a;
    protected static final int MSG_CHECK_RETRY = 0x0d;
    protected static final int MSG_HANDLE_SCREEN_ON = 0x0c;
    protected static final int MSG_HANDLE_SCREEN_OFF = 0x0f;
    private static final int MSG_CHECK = 0x0b;
    private static final int MSG_RETRY = 0x0e;
    private static long ocLastTime = 0;
    private static long snapShotLastTime = 0;
    private static long uploadLastTime = 0;
    private static long locationLastTime = 0;
    private static long ocCycle = 0;
    private static long snapShotCycle = 0;
    private static long uploadCycle = 0;

    private static long locationCycle = 0;
    private static long reTryLastTime = 0;
    private static long heartBeatLastTime = 0;
    //    private final Object mHandlerLock = new Object();
    private Context mContext = null;
    private final Handler mHandler;


    // 上次结束时间
    public long mLastCloseTime = 0;
//    public boolean isScreenOnOffBroadCastHandled = false;


    private final boolean isDebug = true;

}
