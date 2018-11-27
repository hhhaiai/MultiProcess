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
    public static final String VARCHAR_TEN = " varchar(10) not null ";
    public static final String VARCHAR_TEN_NULL = " varchar(10) ";
    public static final String VARCHAR_TWENTY = " varchar(20) not null ";
    public static final String VARCHAR_TWENTY_NULL = " varchar(20) ";
    public static final String VARCHAR_HUNDRED = " varchar(100) not null ";
    public static final String INT_NOT_NULL = " int not null ";
    public static final String TEXT = " text ";
  }

  public static class OC {
    // 表名
    public static final String TABLE_NAME = "e_oci";

    /**
     * 列名
     */
    public static class Column {
      public static final String ID = "id";
      // 存储单条完整信息
      public static final String OCI = "oci_a";
      // 存储时间
      public static final String IT = "oci_b";
      // 存储标记，默认为0，读取成功设置1
      public static final String ST = "oci_c";
      // 备用字段 text 类型
      public static final String OCIRA = "oci_ra";
      public static final String OCIRB = "oci_rb";
      public static final String OCIRC = "oci_rc";
    }

    /**
     * 字段类型
     */
    public static class Types {
      public static final String ID = Type.AUTOINCREMENT;
      public static final String OCI = Type.TEXT;
      public static final String IT = Type.VARCHAR_TWENTY;
      public static final String ST = Type.INT_NOT_NULL;
      public static final String OCIRA = Type.TEXT;
      public static final String OCIRB = Type.TEXT;
      public static final String OCIRC = Type.TEXT;
    }

    //建表
    public static final String CREATE_TABLE = "create table if not exists " +
        TABLE_NAME + " (" +
        Column.ID + Types.ID + "," +
        Column.OCI + Types.OCI + "," +
        Column.IT + Types.IT + "," +
        Column.ST + Types.ST + "," +
        Column.OCIRA + Types.OCIRA + "," +
        Column.OCIRB + Types.OCIRB + "," +
        Column.OCIRC + Types.OCIRC + ")";
  }

  public static class OCCount {
    // 表名
    public static final String TABLE_NAME = "e_occ";

    public static class Column {
      public static final String ID = "id";
      // 应用包名
      public static final String APN = "occ_a";
      // 应用名称
      public static final String AN = "occ_b";
      // 开始时间
      public static final String AOT = "occ_c";
      // 结束时间
      public static final String ACT = "occ_d";
      // 应用打开关闭次数
      public static final String CU = "occ_e";
      // 日期
      public static final String DY = "occ_f";
      // insert 的时间
      public static final String IT = "occ_g";
      // 应用版本信息
      public static final String AVC = "occ_h";
      // 网络类型
      public static final String NT = "occ_i";
      // 应用切换类型，1-正常使用，2-开关屏幕切换，3-服务重启
      public static final String AST = "occ_j";
      // 应用类型
      public static final String AT = "occ_k";
      // OC采集来源，1-getRunningTask，2-读取proc，3-辅助功能，4-系统统计
      public static final String CT = "occ_l";
      // 时间段标记
      public static final String TI = "occ_m";
      // 存储标记，默认为 0，上传读取后修改为 1
      public static final String ST = "occ_n";
      // 应用运行状态，默认值 0，正在运行为 1
      public static final String RS = "occ_o";

      // 备用字段 text 类型
      public static final String OCT_RA = "oct_ra";
      public static final String OCT_RB = "oct_rb";
      public static final String OCT_RC = "oct_rc";
    }

    public static class Types {
      public static final String ID = Type.AUTOINCREMENT;
      public static final String APN = Type.VARCHAR_HUNDRED;
      public static final String AN = Type.VARCHAR_TWENTY;
      public static final String AOT = Type.VARCHAR_TWENTY;
      public static final String ACT = Type.VARCHAR_TWENTY_NULL;
      public static final String CU = Type.INT_NOT_NULL;
      public static final String DY = Type.VARCHAR_TWENTY;
      public static final String IT = Type.VARCHAR_TWENTY;

      public static final String AVC = Type.VARCHAR_TWENTY;
      public static final String NT = Type.VARCHAR_TEN;
      public static final String AST = Type.VARCHAR_TEN_NULL;
      public static final String AT = Type.VARCHAR_TEN;
      public static final String CT = Type.VARCHAR_TEN;
      public static final String TI = Type.VARCHAR_TEN;
      public static final String ST = Type.VARCHAR_TEN;
      public static final String RS = Type.VARCHAR_TEN;

      public static final String OCT_RA = Type.TEXT;
      public static final String OCT_RB = Type.TEXT;
      public static final String OCT_RC = Type.TEXT;
    }

    //建表
    public static final String CREATE_TABLE = "create table if not exists " +
        TABLE_NAME + " (" +
        Column.ID + Types.ID + "," +
        Column.APN + Types.APN + "," +
        Column.AN + Types.AN + "," +
        Column.AOT + Types.AOT + "," +
        Column.ACT + Types.ACT + "," +
        Column.CU + Types.CU + "," +
        Column.DY + Types.DY + "," +
        Column.IT + Types.IT + "," +
        Column.AVC + Types.AVC + "," +
        Column.NT + Types.NT + "," +
        Column.AST + Types.AST + "," +
        Column.AT + Types.AT + "," +
        Column.CT + Types.CT + "," +
        Column.TI + Types.TI + "," +
        Column.ST + Types.ST + "," +
        Column.RS + Types.RS + "," +
        Column.OCT_RA + Types.OCT_RA + "," +
        Column.OCT_RB + Types.OCT_RB + "," +
        Column.OCT_RC + Types.OCT_RC + ")";
  }

  public static class AppSnapshot {
    // 表名
    public static final String TABLE_NAME = "e_asi";

    public static class Column {
      public static final String ID = "id";
      // 应用包名
      public static final String APN = "asi_a";
      // 应用名称
      public static final String AN = "asi_b";
      // 应用版本号
      public static final String AVC = "asi_c";
      // 应用状态
      public static final String AT = "asi_e";
      // 操作时间
      public static final String AHT = "asi_f";

      public static final String ASI_RA = "asi_ra";
      public static final String ASI_RB = "asi_rb";
      public static final String ASI_RC = "asi_rc";
    }

    public static class Types {
      public static final String ID = Type.AUTOINCREMENT;
      public static final String APN = Type.VARCHAR_HUNDRED;
      public static final String AN = Type.VARCHAR_HUNDRED;
      public static final String AVC = Type.VARCHAR_TWENTY;
      public static final String AT = Type.VARCHAR_TWENTY;
      public static final String AHT = Type.VARCHAR_TWENTY;

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
        Column.ASI_RC + Types.ASI_RC + ")";
  }

  public static class Location {
    // 表名
    public static final String TABLE_NAME = "e_l";

    public static class Column {
      public static final String ID = "id";
      // 存储单条完整信息
      public static final String LI = "l_a";
      // 存储时间
      public static final String IT = "l_b";
      // 存储标记，默认为0，读取成功设置1
      public static final String ST = "l_c";

      // 备用字段 text 类型
      public static final String L_RA = "l_ra";
      public static final String L_RB = "l_rb";
      public static final String L_RC = "l_rc";
    }

    public static class Types {
      public static final String ID = Type.AUTOINCREMENT;
      public static final String LI = Type.TEXT;
      public static final String IT = Type.VARCHAR_TWENTY;
      public static final String ST = Type.VARCHAR_TEN;
      public static final String L_RA = Type.TEXT;
      public static final String L_RB = Type.TEXT;
      public static final String L_RC = Type.TEXT;
    }

    //建表
    public static final String CREATE_TABLE = "create table if not exists " +
        TABLE_NAME + " (" +
        Column.ID + Types.ID + "," +
        Column.LI + Types.LI + "," +
        Column.IT + Types.IT + "," +
        Column.ST + Types.ST + "," +
        Column.L_RA + Types.L_RA + "," +
        Column.L_RB + Types.L_RB + "," +
        Column.L_RC + Types.L_RC + "  )";
  }
}
