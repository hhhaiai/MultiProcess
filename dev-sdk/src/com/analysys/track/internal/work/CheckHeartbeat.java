package com.analysys.track.internal.work;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.sp.SPHelper;


public class CheckHeartbeat {
    Context mContext;
    Handler mHandler;
    final HandlerThread thread = new HandlerThread("com.analysys", android.os.Process.THREAD_PRIORITY_BACKGROUND);;

    public CheckHeartbeat() {
        mHandler = startWorkHandler();
    }

    private static class Holder {
        private static final CheckHeartbeat INSTANCE = new CheckHeartbeat();
    }

    public static CheckHeartbeat getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    private Handler startWorkHandler() {
        thread.start();
        final Handler ret = new CheckHandler(thread.getLooper());
        return ret;
    }

    public void sendMessages() {
        Message msg = new Message();
        msg.what = MSG_CHECK;
        mHandler.sendMessageDelayed(msg, EGContext.CHECK_HEARTBEAT_CYCLE);
    }
    public void checkRetry() {
        Message msg = new Message();
        msg.what = MSG_RETRY;
        mHandler.sendMessageDelayed(msg, EGContext.CHECK_RETRY_CYCLE);
    }

    class CheckHandler extends Handler {
        public CheckHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CHECK:
                    ELOG.i("心跳检查,进程：");
                    SPHelper.getDefault(mContext).edit().putLong(EGContext.HEARTBEAT_LAST_TIME,System.currentTimeMillis()).commit();
                    MessageDispatcher.getInstance(mContext).checkHeartbeat(EGContext.CHECK_HEARTBEAT_CYCLE);
                    sendMessages();
                    break;
                case MSG_RETRY:
                    ELOG.i("轮询检查,进程：");
                    MessageDispatcher.getInstance(mContext) .isNeedRetry(EGContext.CHECK_RETRY_CYCLE);
                    checkRetry();
                    break;
                default:
                    break;
            }
        }
    }

    private static final int MSG_CHECK = 0x0c;
    private static final int MSG_RETRY = 0x0f;
    /**
     * 空闲时,自动退出,如果有事件进来,自动调起. reboot只需要关闭一次就可以了
     */
    public void reboot() {
        if (null != thread) {
            thread.quit();
        }
    }
}
