package com.analysys.track.internal.work;

/**
 * MessageDispatcher消息定义
 */
public enum  DispatcherMsgType{
     INIT_MODULE,
     CHECK_HEARTBEAT,
     START_SERVICE_SELF,
     KILL_WORKER,
     APP_CHANGE_RECEIVER,
     SCREEN_RECEIVER,
     SNAPSHOT,
     LOCATION,
     OC_INFO,
     UPLOAD,
     OC_COUNT
}
