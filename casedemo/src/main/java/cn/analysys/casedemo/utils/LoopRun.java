package cn.analysys.casedemo.utils;


import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.cslib.utils.L;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2020/6/18 16:48
 * @author: sanbo
 */
public class LoopRun {

    private static final String THREAD_NAME = ".casedemo";
    private static final int MSG_KEEP_ALIVE = 0x001;
    private static long TIME_LAST_SUCCESS = -1;
    private final Handler mHandler;
    private final HandlerThread thread;

    //是否初始化
    private volatile boolean isInit = false;
    private Worker mWorker = null;
    private volatile AtomicInteger ai = new AtomicInteger(0);
    private Context mContext = null;
    // loop轮训时间
    public static long LOOP_TIME_DELAYMILLIS = 10 * 1000;
    // 两次任务间隔时间
    public static long TIME_DURATION = 30 * 1000;


    public LoopRun(Context context) {
        mContext = SDKHelper.getContext(context);
        thread = new HandlerThread(THREAD_NAME,
                android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
        thread.start();
        mHandler = new FHandler(thread.getLooper());
    }

    /********************* api begin **************************/

    public synchronized void init(Worker worker) {
        init(worker, LOOP_TIME_DELAYMILLIS, TIME_DURATION);
    }

    public synchronized void init(Worker worker, long loopMillis, long durationMillis) {
        if (isInit) {
            L.i("  LoopRun already init will return! ");
            return;
        }
        realInit(worker, loopMillis, durationMillis);
    }

    /***********************************************工作逻辑，需要实现********************************************************/

    private void realInit(Worker worker, long loopMillis, long durationMillis) {
        isInit = true;
        if (mWorker == null && worker != null) {
            mWorker = worker;
        }
        if (loopMillis > 0) {
            LOOP_TIME_DELAYMILLIS = loopMillis;
        }
        if (durationMillis > 0) {
            TIME_DURATION = durationMillis;
        }
        postMessage(MSG_KEEP_ALIVE);
    }

    private synchronized void gotoWork() {
//        L.d("[LoopRun]go to work!");
        if (mWorker != null) {
//            Logs.i("==ai===>" + ai.get());
            // 首次,直接工作
            if (TIME_LAST_SUCCESS == -1) {
                processWorkCallBack();
            } else {
                //间隔时间达标，才能继续工作
                if (Math.abs(System.currentTimeMillis() - TIME_LAST_SUCCESS) > TIME_DURATION) {
                    processWorkCallBack();
                } else {

//                    L.w("the time duration is limited!");
                    postDelay(MSG_KEEP_ALIVE, LOOP_TIME_DELAYMILLIS);
                }
            }
        } else {
//            L.e("the worker is null!");
        }

    }

    /**
     * 确保时间是上次成功的时间
     */
    private synchronized void processWorkCallBack() {
        if (ai.get() <= 0) {
            ai.getAndIncrement();
            mWorker.goWork(new ICall() {
                @Override
                public void onProcessed() {
                    // 处理完成才能继续处理
                    TIME_LAST_SUCCESS = System.currentTimeMillis();
//                    L.i("processWorkCallBack（） receiver processed. ai: " + ai.get());
                    ai.getAndDecrement();
//                    L.i("processWorkCallBack（） receiver processed. will post message delay: " + LOOP_TIME_DELAYMILLIS);
                    postDelay(MSG_KEEP_ALIVE, LOOP_TIME_DELAYMILLIS);
                }
            });
        }
    }


    /**
     * 立即发送
     *
     * @param what
     */
    private void postMessage(int what) {
        try {
            if (mHandler != null && !mHandler.hasMessages(what) && thread != null && thread.isAlive()) {
                mHandler.sendEmptyMessage(what);
            }
        } catch (Throwable e) {
            L.e(e);
        }
    }

    /**
     * 延迟发送
     *
     * @param what
     * @param delayTime
     */
    private void postDelay(int what, long delayTime) {
        try {
            if (mHandler != null && !mHandler.hasMessages(what) && thread != null && thread.isAlive()) {
                mHandler.sendEmptyMessageDelayed(what, delayTime > 0 ? delayTime : LOOP_TIME_DELAYMILLIS);
            }
        } catch (Throwable e) {
            L.e(e);
        }
    }


    /***********************************************Handler实现********************************************************/
    class FHandler extends Handler {
        public FHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_KEEP_ALIVE:
//                    L.i("handleMessage（） receiver msg: " + MSG_KEEP_ALIVE + " ; will go to workgr");
                    gotoWork();
                    break;
            }
        }
    }

//    public static LoopRun getInstance(Context context) {
//        return HLODER.INSTANCE.initContext(context);
//    }
//
//    private LoopRun initContext(Context context) {
//        if (mContext == null && context != null) {
//            mContext = context.getApplicationContext();
//        }
//        return HLODER.INSTANCE;
//    }
//
//    private static class HLODER {
//        private static final LoopRun INSTANCE = new LoopRun();
//    }

    public interface Worker {
        public abstract void goWork(ICall callback);
    }

    public interface ICall {
        public abstract void onProcessed();
    }
}
