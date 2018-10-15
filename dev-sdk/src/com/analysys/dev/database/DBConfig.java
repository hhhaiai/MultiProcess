package com.analysys.dev.database;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/12 16:53
 * @Author: Wang-X-C
 */
public class DBConfig {

  public static class Type {
    public static final String AUTOINCREMENT = " Integer Primary Key Autoincrement ";
    public static final String VARCHAR_TWENTY = "  varchar(20) not null  ";
    public static final String VARCHAR_FIFTY = "  varchar(50) not null  ";
    public static final String VARCHAR_HUNDRED = "  varchar(100) not null  ";
    public static final String INT_NOT_NULL = " int not null ";
    public static final String TEXT = " text ";
  }

  public static class TableOCInfo {
    // 表名
    public static final String TABLE_NAME = "e_oci";

    /**
     * 列名
     */
    public static class Column {
      public static final String ID = "id";
      // 应用打开时间,转换成时间戳，如：“1296035591”
      public static final String AOT = "oc_a";
      // 应用关闭时间,转换成时间戳，如：“1296035599”
      public static final String ACT = "oc_b";
      // 应用包名,如：“com.qzone”
      public static final String APN = "oc_c";
      // 应用程序名,如：“QQ空间”
      public static final String AN = "oc_d";
      // 应用版本名|应用版本号,如“5.4.1|89”
      public static final String AVC = "oc_e";
      // 网络类型,选项: WIFI/2G/3G/4G/无网络
      public static final String NT = "oc_f";
      // OC 切换的类型,1-正常使用，2-开关屏幕切换，3-服务重启
      public static final String ST = "oc_g";
      // 应用类型,SA-系统应用，OA-第三方应用
      public static final String AT = "oc_h";
      // 采集来源类型,1-getRunningTask，2-读取proc，3-辅助功能，4-系统统计
      public static final String CT = "oc_i";
      // 备用字段 text 类型
      public static final String OC_RA = "oc_ra";
      public static final String OC_RB = "oc_rb";
      public static final String OC_RC = "oc_rc";
    }

    /**
     * 字段类型
     */
    public static class Types {
      public static final String ID = Type.AUTOINCREMENT;
      public static final String AOT = Type.VARCHAR_FIFTY;
      public static final String ACT = Type.VARCHAR_FIFTY;
      public static final String APN = Type.VARCHAR_HUNDRED;
      public static final String AN = Type.VARCHAR_FIFTY;
      public static final String AVC = Type.VARCHAR_FIFTY;
      public static final String NT = Type.VARCHAR_TWENTY;
      public static final String ST = Type.INT_NOT_NULL;
      public static final String AT = Type.VARCHAR_TWENTY;
      public static final String CT = Type.INT_NOT_NULL;
      public static final String OC_RA = Type.TEXT;
      public static final String OC_RB = Type.TEXT;
      public static final String OC_RC = Type.TEXT;
    }

    //建表
    public static final String CREATE_TABLE = "create table if not exists " +
        TABLE_NAME + " (" +
        Column.ID + Types.ID + "," +
        Column.AOT + Types.AOT + "," +
        Column.ACT + Types.ACT + "," +
        Column.APN + Types.APN + "," +
        Column.AN + Types.AN + "," +
        Column.AVC + Types.AVC + "," +
        Column.NT + Types.NT + "," +
        Column.ST + Types.ST + "," +
        Column.AT + Types.AT + "," +
        Column.CT + Types.CT + "," +
        Column.OC_RA + Types.OC_RA + "," +
        Column.OC_RB + Types.OC_RB + "," +
        Column.OC_RC + Types.OC_RC + ")";

  }

  public static class TableOCTimes {
    // 表名
    public static final String TABLE_NAME = "e_oct";

    public static class Column {
      public static final String ID = "id";
      // 应用包名，如: "com.test"
      public static final String PN = "oct_a";
      // 在进程列表中快照的次数，如:200
      public static final String CU = "oct_b";
      // 快照次数所属的时段，1表示0～6小时，2表示6～12小时，3表示12～18小时，4表示18～24小时
      public static final String TI = "oct_c";
      // 发生的日期
      public static final String DY = "oct_d";
      // 备用字段 text 类型
      public static final String OCT_RA = "oct_ra";
      public static final String OCT_RB = "oct_rb";
      public static final String OCT_RC = "oct_rc";
    }

    public static class Types {
      public static final String ID = Type.AUTOINCREMENT;
      public static final String PN = Type.VARCHAR_HUNDRED;
      public static final String CU = Type.INT_NOT_NULL;
      public static final String TI = Type.INT_NOT_NULL;
      public static final String DY = Type.VARCHAR_FIFTY;
      public static final String OCT_RA = Type.TEXT;
      public static final String OCT_RB = Type.TEXT;
      public static final String OCT_RC = Type.TEXT;
    }

    //建表
    public static final String CREATE_TABLE = "create table if not exists " +
        TABLE_NAME + " (" +
        Column.ID + Types.ID + "," +
        Column.PN + Types.PN + "," +
        Column.CU + Types.CU + "," +
        Column.TI + Types.TI + "," +
        Column.DY + Types.DY + "," +
        Column.OCT_RA + Types.OCT_RA + "," +
        Column.OCT_RB + Types.OCT_RB + "," +
        Column.OCT_RC + Types.OCT_RC + "  )";

  }

  public static class TableAppSnapshotInfo {
    // 表名
    public static final String TABLE_NAME = "e_asi";

    public static class Column {
      public static final String ID = "id";
      // 应用包名. eg:com.hello
      public static final String APN = "asi_a";
      // 应用程序名.eg: QQ
      public static final String AN = "asi_b";
      // 应用版本名|应用版本号. eg: 5.2.1|521
      public static final String AVC = "asi_c";
      // 行为类型. -1:未变动(可不上传) 0:安装 1:卸载 2:更新
      public static final String AT = "asi_d";
      // 行为发生时间.时间戳
      public static final String AHT = "asi_e";
      // 备用字段 text 类型
      public static final String ASI_RA = "asi_ra";
      public static final String ASI_RB = "asi_rb";
      public static final String ASI_RC = "asi_rc";
    }

    public static class Types {
      public static final String ID = Type.AUTOINCREMENT;
      public static final String APN = Type.VARCHAR_HUNDRED;
      public static final String AN = Type.VARCHAR_FIFTY;
      public static final String AVC = Type.VARCHAR_TWENTY;
      public static final String AT = Type.INT_NOT_NULL;
      public static final String AHT = Type.VARCHAR_FIFTY;
      public static final String ASI_RA = Type.TEXT;
      public static final String ASI_RB = Type.TEXT;
      public static final String ASI_RC = Type.TEXT;
    }

    //建表
    public static final String CREATE_TABLE = "create table if not exists " +
        TABLE_NAME + " (" +
        Column.ID + Types.ID + "," +
        Column.APN + Types.APN + "," +
        Column.AN + Types.AN + "," +
        Column.AVC + Types.AVC + "," +
        Column.AT + Types.AT + "," +
        Column.AHT + Types.AHT + "," +
        Column.ASI_RA + Types.ASI_RA + "," +
        Column.ASI_RB + Types.ASI_RB + "," +
        Column.ASI_RC + Types.ASI_RC + "  )";

  }

  public static class TableLocation {
    // 表名
    public static final String TABLE_NAME = "e_l";

    public static class Column {
      public static final String ID = "id";
      // 采集时间
      public static final String CT = "l_a";
      // 地理位置，由经度和纬度组成，用减号-连接
      public static final String GL = "l_b";
      // wifi信息采集,json数组
      public static final String WI = "l_c";
      // 基站信息采集,json数组
      public static final String BSI = "l_d";
      // 备用字段 text 类型
      public static final String L_RA = "l_ra";
      public static final String L_RB = "l_rb";
      public static final String L_RC = "l_rc";
    }

    public static class Types {
      public static final String ID = Type.AUTOINCREMENT;
      public static final String CT = Type.VARCHAR_FIFTY;
      public static final String GL = Type.VARCHAR_FIFTY;
      public static final String WI = Type.TEXT;
      public static final String BSI = Type.TEXT;
      public static final String L_RA = Type.TEXT;
      public static final String L_RB = Type.TEXT;
      public static final String L_RC = Type.TEXT;
    }

    //建表
    public static final String CREATE_TABLE = "create table if not exists " +
        TABLE_NAME + " (" +
        Column.ID + Types.ID + "," +
        Column.CT + Types.CT + "," +
        Column.GL + Types.GL + "," +
        Column.WI + Types.WI + "," +
        Column.BSI + Types.BSI + "," +
        Column.L_RA + Types.L_RA + "," +
        Column.L_RB + Types.L_RB + "," +
        Column.L_RC + Types.L_RC + "  )";

  }
}
