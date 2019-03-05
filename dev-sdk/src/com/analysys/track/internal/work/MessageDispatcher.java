package com.analysys.track.internal.work;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.widget.TableRow;

import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.internal.impl.LocationImpl;
import com.analysys.track.internal.impl.OCImpl;
import com.analysys.track.internal.impl.UploadImpl;

import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.sp.SPHelper;

public class MessageDispatcher {
    private long delay = 0;
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
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_INIT_MODULE;
            sendMessage(msg, 0);
        }catch (Throwable t){
        }

    }

    // 心跳检查
    public void checkHeartbeat(long delayTime) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_CHECK_HEARTBEAT;
            if(delayTime != 0) {
                if(EGContext.HEARTBEAT_LAST_TIME_STMP == -1){
                    EGContext.HEARTBEAT_LAST_TIME_STMP = SPHelper.getDefault(mContext).getLong(EGContext.HEARTBEAT_LAST_TIME,-1);
                }
                delay = delayTime - (System.currentTimeMillis()- EGContext.HEARTBEAT_LAST_TIME_STMP);
                sendMessage(msg, delay);
            }else {
                sendMessage(msg,delayTime);
            }
        }catch (Throwable t){
        }
    }

    // 启动服务任务接入
    public void startService() {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_START_SERVICE_SELF;
            //TODO ？是否需要判断是否可以执行？
            sendMessage(msg, 0);
        }catch (Throwable t){
        }

    }

    // 停止工作
    public void killWorker() {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_KILL_WORKER;
            sendMessage(msg, 0);
        }catch (Throwable t){
        }

    }

    // 应用安装卸载更新
    public void appChangeReceiver(String pkgName, int type) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_APP_CHANGE_RECEIVER;
            msg.arg1 = type;
            msg.obj = pkgName;
            sendMessage(msg, 0);
        }catch (Throwable t){
        }
    }

    // 屏幕开关
    public void screenReceiver() {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_SCREEN_RECEIVER;
            sendMessage(msg, 0);
        }catch (Throwable t){
        }

    }

    // 应用列表
    public void snapshotInfo(long delayTime,boolean shouldRemoveDelay) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_SNAPSHOT;
            if(delayTime!= 0){
                if(EGContext.SNAPSHOT_LAST_TIME_STMP == -1){
                    EGContext.SNAPSHOT_LAST_TIME_STMP = SPHelper.getDefault(mContext).getLong(EGContext.SNAPSHOT_LAST_TIME, -1);
                }
                if(shouldRemoveDelay){
                    mHandler.removeMessages(msg.what);
                    sendMessage(msg,delayTime);
                }else{
                    delay = delayTime - (System.currentTimeMillis() - EGContext.SNAPSHOT_LAST_TIME_STMP);
                    sendMessage(msg, delay);
                }
            }else {
                if(shouldRemoveDelay){
                    mHandler.removeMessages(msg.what);
                }
                sendMessage(msg,delayTime);
            }
            long time = System.currentTimeMillis();
            EGContext.SNAPSHOT_LAST_TIME_STMP = time;
            SPHelper.getDefault(mContext).edit().putLong(EGContext.SNAPSHOT_LAST_TIME, time).commit();
        }catch (Throwable t){

        }
    }

    // 位置信息
    public void locationInfo(long delayTime,boolean shouldRemoveDelay) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_LOCATION;
            if(delayTime!= 0) {
                if(EGContext.LOCATION_LAST_TIME_STMP == -1){
                    EGContext.LOCATION_LAST_TIME_STMP = SPHelper.getDefault(mContext).getLong(EGContext.LOCATION_LAST_TIME,-1);
                }
                if(shouldRemoveDelay){
                    mHandler.removeMessages(msg.what);
                    sendMessage(msg,delayTime);
                }else{
                    delay = delayTime - (System.currentTimeMillis()- EGContext.LOCATION_LAST_TIME_STMP);
                    sendMessage(msg, delay);
                }
            }else {
                if(shouldRemoveDelay){
                    mHandler.removeMessages(msg.what);
                }
                sendMessage(msg,delayTime);
            }
            long time = System.currentTimeMillis();
            EGContext.SNAPSHOT_LAST_TIME_STMP = time;
            SPHelper.getDefault(mContext).edit().putLong(EGContext.LOCATION_LAST_TIME,time).commit();
        }catch (Throwable t){
        }

    }

    // 应用打开关闭信息
    public void ocInfo(long delayTime ,boolean shouldRemoveDelay) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_OC_INFO;
            if(EGContext.OC_CYCLE == delayTime){
                if(EGContext.OC_LAST_TIME_STMP == -1){
                    EGContext.OC_LAST_TIME_STMP = SPHelper.getDefault(mContext).getLong(EGContext.OC_LAST_TIME,-1);
                }
                if(delayTime != 0){
                    if(shouldRemoveDelay){
                        mHandler.removeMessages(msg.what);
                        sendMessage(msg,delayTime);
                    }else{
                        delay = delayTime - (System.currentTimeMillis()- EGContext.OC_LAST_TIME_STMP);
                        sendMessage(msg, delay);
                    }
                }
                long time = System.currentTimeMillis();
                EGContext.OC_LAST_TIME_STMP = time;
                SPHelper.getDefault(mContext).edit().putLong(EGContext.OC_LAST_TIME,time).commit();
            }else if(EGContext.OC_CYCLE_OVER_5 == delayTime){
                if(EGContext.OC_LAST_TIME_OVER_5_STMP == -1){
                    EGContext.OC_LAST_TIME_OVER_5_STMP = SPHelper.getDefault(mContext).getLong(EGContext.OC_LAST_TIME_OVER_5,-1);
                }
                if(shouldRemoveDelay){
                    mHandler.removeMessages(msg.what);
                    sendMessage(msg,delayTime);
                }else {
                    if(delayTime != 0) {
                        delay = delayTime - (System.currentTimeMillis() - EGContext.OC_LAST_TIME_OVER_5_STMP);
                    }
                    sendMessage(msg,delayTime);
                }
                long time = System.currentTimeMillis();
                EGContext.OC_LAST_TIME_OVER_5_STMP = time;
                SPHelper.getDefault(mContext).edit().putLong(EGContext.OC_LAST_TIME_OVER_5,time).commit();
            }else {
                if(shouldRemoveDelay){
                    mHandler.removeMessages(msg.what);
                }
                sendMessage(msg,0);
            }
        }catch (Throwable t){
        }


    }

    // 数据上传
    public void uploadInfo(long delayTime,boolean shouldRemoveDelay) {
        try {
            Message msg = new Message();
            msg.what = MessageDispatcher.MSG_UPLOAD;
            long time = System.currentTimeMillis();
            EGContext.UPLOAD_LAST_TIME_STMP = time;
            SPHelper.getDefault(mContext).edit().putLong(EGContext.UPLOAD_LAST_TIME, time).commit();
            if(delayTime!= 0) {
                if(EGContext.UPLOAD_LAST_TIME_STMP == -1){
                    EGContext.UPLOAD_LAST_TIME_STMP = SPHelper.getDefault(mContext).getLong(EGContext.UPLOAD_LAST_TIME, -1);
                }
                if(shouldRemoveDelay){
                    mHandler.removeMessages(msg.what);
                    sendMessage(msg,delayTime);
                }else{
                    long currentTime = System.currentTimeMillis();
//                if(delayTime <= (currentTime - EGContext.UPLOAD_LAST_TIME_STMP)){
//                }
                    delay = delayTime - (currentTime - EGContext.UPLOAD_LAST_TIME_STMP);
                    sendMessage(msg, delay);
                }
            }else{
                if(shouldRemoveDelay){
                    mHandler.removeMessages(msg.what);
                }
                sendMessage(msg,delayTime);
            }
        }catch (Throwable t){

        }

    }

    private void sendMessage(Message msg, long delay) {
        ELOG.i(msg +"   == msg &   delay == " +delay);
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
        final HandlerThread thread = new HandlerThread(EGContext.THREAD_NAME, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        final Handler ret = new AnalysyHandler(thread.getLooper());
        return ret;
    }

    private void exitHandler() {
        try {
            synchronized (mHandlerLock) {
                mHandler = null;
                Looper.myLooper().quit();
            }
        }catch (Throwable t){
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
            try{
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
            }catch (Throwable t){
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
            try {
                ELOG.i(handler.hasMessages(MSG_SNAPSHOT)
                        +"  :  "+ handler.hasMessages(MSG_LOCATION)
                        +"  :  "+ handler.hasMessages(MSG_OC_INFO)
                        +"  :  "+ handler.hasMessages(MSG_UPLOAD));
                if (handler.hasMessages(MSG_SNAPSHOT)
                        || handler.hasMessages(MSG_LOCATION)
                        || handler.hasMessages(MSG_OC_INFO)
                        || handler.hasMessages(MSG_UPLOAD)) {
                    if(handler.hasMessages(MSG_UPLOAD)){
                        if(EGContext.UPLOAD_LAST_TIME_STMP == -1){
                            EGContext.UPLOAD_LAST_TIME_STMP = SPHelper.getDefault(mContext).getLong(EGContext.UPLOAD_LAST_TIME, -1);
                        }
                        long delay = EGContext.UPLOAD_CYCLE - (System.currentTimeMillis() - EGContext.UPLOAD_LAST_TIME_STMP);
                        if(delay <= 0 ){
                            uploadInfo(delay ,true);
                        }
                    }
                    if(handler.hasMessages(MSG_OC_INFO)){
                        long delay = -1;
                        if (Build.VERSION.SDK_INT < 21){
                            if(EGContext.OC_LAST_TIME_STMP == -1){
                                EGContext.OC_LAST_TIME_STMP = SPHelper.getDefault(mContext).getLong(EGContext.OC_LAST_TIME, -1);
                            }
                            delay = EGContext.OC_CYCLE - (System.currentTimeMillis() - EGContext.OC_LAST_TIME_STMP);
                        }else if(Build.VERSION.SDK_INT > 20){
                            if(EGContext.OC_LAST_TIME_OVER_5_STMP == -1){
                                EGContext.OC_LAST_TIME_OVER_5_STMP = SPHelper.getDefault(mContext).getLong(EGContext.OC_LAST_TIME_OVER_5, -1);
                            }
                            delay = EGContext.OC_CYCLE_OVER_5 - (System.currentTimeMillis() - EGContext.OC_LAST_TIME_OVER_5_STMP);
                        }
                        if(delay <= 0 ){
                            ocInfo(delay ,true);
                        }
                    }
                    if(handler.hasMessages(MSG_LOCATION)){
                        if(EGContext.LOCATION_LAST_TIME_STMP == -1){
                            EGContext.LOCATION_LAST_TIME_STMP = SPHelper.getDefault(mContext).getLong(EGContext.LOCATION_LAST_TIME, -1);
                        }
                        long delay = EGContext.LOCATION_CYCLE - (System.currentTimeMillis() - EGContext.LOCATION_LAST_TIME_STMP);
                        if(delay <= 0 ){
                            locationInfo(delay ,true);
                        }
                    }
                    if(handler.hasMessages(MSG_SNAPSHOT)){
                        if(EGContext.SNAPSHOT_LAST_TIME_STMP == -1){
                            EGContext.SNAPSHOT_LAST_TIME_STMP = SPHelper.getDefault(mContext).getLong(EGContext.SNAPSHOT_LAST_TIME, -1);
                        }
                        long delay = EGContext.SNAPSHOT_CYCLE - (System.currentTimeMillis() - EGContext.SNAPSHOT_LAST_TIME_STMP);
                        if(delay <= 0 ){
                            snapshotInfo(delay ,true);
                        }
                    }
                }else{
                    MessageDispatcher.getInstance(mContext).initModule();
                }
            }catch (Throwable t){

            }

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
            try {
                ocInfo(0,false);
                snapshotInfo(0,false);
                locationInfo(0,false);
                uploadInfo(0,false);
//            ServiceHelper.getInstance(mContext).registerReceiver();
                CheckHeartbeat.getInstance(mContext).sendMessages();
            }catch (Throwable t){
            }
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
