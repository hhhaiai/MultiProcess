package com.analysys.dev.internal.Content;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 变量持有类
 * @Version: 1.0
 * @Create: 2018年9月3日 下午2:40:40
 * @Author: sanbo
 */
public class EDContext {

  /**
   * EGuan 内部调试控制. 主要用于控制堆栈打印、错误打印、内部提示信息打印
   */
  public static final boolean FLAG_DEBUG_INNER = true;
  /**
   * 用户debug控制
   */
  public static boolean FLAG_DEBUG_USER = true;
  /**
   * 是否展示广告通知。授权后，服务高版本可以切换成前台服务
   */
  public static boolean FLAG_SHOW_NOTIFY = true;

  /**
   * SDK版本
   */
  public static final String SDK_VERSION = "4.0.1";

  public static final String LOGTAG_DEBUG = "xxx";

  /**
   * xml 中声明的 appid、channel
   */
  public static final String XML_METADATA_APPID = "ANALYSYS_APPKEY";
  public static final String XML_METADATA_CHANNEL = "ANALYSYS_CHANNEL";

  public static final String SERVICE_NAME = "com.analysys.dev.service.AnalysysService";



  public class LOGINFO {
    public static final String LOG_NOT_APPKEY = "please check you appkey!";
  }
}
