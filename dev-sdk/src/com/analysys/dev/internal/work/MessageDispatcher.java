package com.analysys.dev.internal.work;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.analysys.dev.internal.impl.AppSnapshotImpl;
import com.analysys.dev.internal.impl.LocationImpl;
import com.analysys.dev.internal.impl.OCImpl;
import com.analysys.dev.internal.impl.UploadImpl;

import com.analysys.dev.receiver.AnalysysReceiver;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.reflectinon.EContextHelper;

public class MessageDispatcher {

    private MessageDispatcher() {
        mHandler = startWorkHandler();
    }

    private static class Holder {
        private static final MessageDispatcher INSTANCE = new MessageDispatcher();
    }

    public static MessageDispatcher getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    // 初始化各模块
    public void initModule() {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_INIT_MODULE;
        sendMessage(msg, 0);
    }

    // 心跳检查
    public void checkHeartbeat() {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_CHECK_HEARTBEAT;
        sendMessage(msg, 0);
    }

    // 启动服务任务接入
    public void startService(int delay) {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_START_SERVICE_SELF;
        sendMessage(msg, delay);
    }

    // 停止工作
    public void killWorker() {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_KILL_WORKER;
        sendMessage(msg, 0);
    }

    // 应用安装卸载更新
    public void appChangeReceiver(String pkgName, int type) {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_APP_CHANGE_RECEIVER;
        msg.arg1 = type;
        msg.obj = pkgName;
        sendMessage(msg, 0);
    }

    // 屏幕开关
    public void screenReceiver() {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_SCREEN_RECEIVER;
        sendMessage(msg, 0);
    }

    // 应用列表
    public void snapshotInfo(int delay) {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_SNAPSHOT;
        sendMessage(msg, delay);
    }

    // 位置信息
    public void locationInfo(int delay) {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_LOCATION;
        sendMessage(msg, delay);
    }

    // 应用打开关闭信息
    public void ocInfo(int delay) {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_OC_INFO;
        sendMessage(msg, delay);
    }

    // 数据上传
    public void uploadInfo(int delay) {
        Message msg = new Message();
        msg.what = MessageDispatcher.MSG_UPLOAD;
        sendMessage(msg, delay);
    }

    private void sendMessage(Message msg, int delay) {
        synchronized (mHandlerLock) {
            if (mHandler != null) {
                if (delay > 0) {
                    mHandler.sendMessageDelayed(msg, delay);
                } else {
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    private Handler startWorkHandler() {
        final HandlerThread thread = new HandlerThread("com.eguan", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        final Handler ret = new AnalysyHandler(thread.getLooper());
        return ret;
    }

    private void exitHandler() {
        synchronized (mHandlerLock) {
            mHandler = null;
            Looper.myLooper().quit();
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

            switch (msg.what) {
                case MSG_INIT_MODULE:
          ELOG.d("接收到初始化消息");
                    msgInitModule();
                    break;
                case MSG_CHECK_HEARTBEAT:
          ELOG.e("接收到心跳检测消息");
                    isHasMessage(this);
                    break;
                case MSG_START_SERVICE_SELF:
          ELOG.d("接收到启动服务消息");
                    ServiceHelper.getInstance(mContext).startSelfService();
                    break;
                case MSG_KILL_WORKER:
                    exitHandler();
          ELOG.d("接收到kill消息");
                    break;
                case MSG_APP_CHANGE_RECEIVER:
          ELOG.d("接收到应用安装/卸载/更新消息");
                    AppSnapshotImpl.getInstance(mContext).changeActionType(String.valueOf(msg.obj), msg.arg1);
                    break;
                case MSG_SCREEN_RECEIVER:
          ELOG.d("接收到屏幕操作消息");
                    break;
                case MSG_SNAPSHOT:
          ELOG.d("接收到获取应用列表消息");
                    AppSnapshotImpl.getInstance(mContext).snapshotsInfo();
                    break;
                case MSG_LOCATION:
          ELOG.d("接收到获取地理位置消息");
                    LocationImpl.getInstance(mContext).location();
                    break;
                case MSG_OC_INFO:
          ELOG.i("接收到获取OC消息,进程 Id：" );
                    ELOG.d("接收到获取OC消息,进程 Id：");
                    OCImpl.getInstance(mContext).ocInfo();
                    break;
                case MSG_UPLOAD:
          ELOG.d("接收到上传消息");
                    UploadImpl.getInstance(mContext).upload();
                    break;
                case MSG_OC_COUNT:
          ELOG.d("接收到屏幕处理消息");
                    break;
                default:
          ELOG.e("其他消息:" + msg.what);
                    break;
            }
        }

        /**
         * 心跳检测，
         * 确保Handler有任务，
         * 如果没有进行初始化各个模块
         *
         * @param handler
         */
        public void isHasMessage(Handler handler) {
            if (handler.hasMessages(MSG_SNAPSHOT)
                    || handler.hasMessages(MSG_LOCATION)
                    || handler.hasMessages(MSG_OC_INFO)
                    || handler.hasMessages(MSG_UPLOAD)) {
                return;
            }
            MessageDispatcher.getInstance(mContext).initModule();
        }

        /**
         * 用于启动各个模块，
         * OC模块，snapshot模块，Location模块，
         * 注册动态广播，启动心跳检测
         */
        private void msgInitModule() {
//            if (!AccessibilityHelper.isAccessibilitySettingsOn(mContext,AnalysysAccessibilityService.class)) {
//
//            }
            ocInfo(0);
            snapshotInfo(0);
            locationInfo(0);
            uploadInfo(0);
            ServiceHelper.getInstance(mContext).registerReceiver();
            CheckHeartbeat.getInstance(mContext).sendMessages();
        }
    }

    private Context mContext = null;
    private Handler mHandler;
    private final Object mHandlerLock = new Object();

    protected static final int MSG_INIT_MODULE = 0x01;
    protected static final int MSG_CHECK_HEARTBEAT = 0x02;
    protected static final int MSG_START_SERVICE_SELF = 0x03;
    protected static final int MSG_KILL_WORKER = 0x04;
    protected static final int MSG_APP_CHANGE_RECEIVER = 0x05;
    protected static final int MSG_SCREEN_RECEIVER = 0x06;
    protected static final int MSG_SNAPSHOT = 0x07;
    protected static final int MSG_LOCATION = 0x08;
    protected static final int MSG_OC_INFO = 0x09;
    protected static final int MSG_UPLOAD = 0x0a;
    protected static final int MSG_OC_COUNT = 0x0b;
}
