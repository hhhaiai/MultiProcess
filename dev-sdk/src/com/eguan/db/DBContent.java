package com.eguan.db;

/**
 * @Copyright © 2018 sanbo Inc. All rights reserved.
 * @Description: DB的字段映射
 * @Version: 1.0
 * @Create: 2018/08/20 11:38:40
 * @Author: sanbo
 */
public class DBContent {

    //老数据库名字
    public static final String NAME_OLD_DB = "eguan.db";
    //设备sdk数据库名字
    public static final String NAME_DB = "deanag.data";
    public static final int VERSION_DB = 1;


    public static class Table_EguanID {
        public static final String TABLE_NAME = "e_N101";
        public static final String TABLE_CREATER = "create table if not exists " + TABLE_NAME
                + "("
                + Column.eguanid + " " + Type.eguanid + " , "
                + Column.obligate_a + " " + Type.obligate_a + ", "
                + Column.obligate_b + " " + Type.obligate_b + ", "
                + Column.obligate_c + " " + Type.obligate_c
                + ")";

        public static class Column {
            public static final String eguanid = "aa";
            public static final String obligate_a = "epa";
            public static final String obligate_b = "epb";
            public static final String obligate_c = "epc";

        }

        // type and limit
        public static class Type {
            public static final String eguanid = "varchar(50)";
            public static final String obligate_a = "text";
            public static final String obligate_b = "text";
            public static final String obligate_c = "text";
        }
    }

    public static class Table_TmpID {
        public static final String TABLE_NAME = "e_N102";
        public static final String TABLE_CREATER = "create table if not exists " + TABLE_NAME
                + "("
                + Column.tempid + " " + Type.tempid + ", "
                + Column.obligate_a + " " + Type.obligate_a + ", "
                + Column.obligate_b + " " + Type.obligate_b + ", "
                + Column.obligate_c + " " + Type.obligate_c
                + ")";

        public static class Column {
            public static final String tempid = "aa";
            public static final String obligate_a = "epa";
            public static final String obligate_b = "epb";
            public static final String obligate_c = "epc";

        }

        // type and limit
        public static class Type {
            public static final String tempid = "varchar(50) not null";
            public static final String obligate_a = "text";
            public static final String obligate_b = "text";
            public static final String obligate_c = "text";
        }
    }

    public static class Table_OCTime {
        public static final String TABLE_NAME = "e_N103";
        public static final String TABLE_CREATER = "create table if not exists " + TABLE_NAME
                + "("
                + Column.id + " " + Type.id + ", "
                + Column.packageName + " " + Type.packageName + ", "
                + Column.timeInterval + " " + Type.timeInterval + ", "
                + Column.count + " " + Type.count + ", "
                + Column.insertTime + " " + Type.insertTime + ", "
                + Column.obligate_a + " " + Type.obligate_a + ", "
                + Column.obligate_b + " " + Type.obligate_b + ", "
                + Column.obligate_c + " " + Type.obligate_c
                + ")";

        public static class Column {
            public static final String id = "aaa";
            public static final String packageName = "aa";
            public static final String timeInterval = "ab";
            public static final String count = "ac";
            public static final String insertTime = "aab";
            public static final String obligate_a = "epa";
            public static final String obligate_b = "epb";
            public static final String obligate_c = "epc";
        }

        // type and limit
        public static class Type {
            public static final String id = "Integer Primary Key Autoincrement";
            public static final String packageName = "varchar(200) not null";
            public static final String timeInterval = "varchar(2) not null";
            public static final String count = "Integer not null";
            public static final String insertTime = "varchar(50) not null";
            public static final String obligate_a = "text";
            public static final String obligate_b = "text";
            public static final String obligate_c = "text";
        }
    }

    public static class Table_ProcTemp {
        public static final String TABLE_NAME = "e_N104";
        public static final String TABLE_CREATER = "create table if not exists " + TABLE_NAME
                + "("
                + Column.id + " " + Type.id + ", "
                + Column.packageName + " " + Type.packageName + ", "
                + Column.openTime + " " + Type.openTime
                + ")";

        public static class Column {
            public static final String id = "aaa";
            public static final String packageName = "aa";
            public static final String openTime = "ab";
        }

        // type and limit
        public static class Type {
            public static final String id = "Integer Primary Key Autoincrement";
            public static final String packageName = "varchar(50) not null";
            public static final String openTime = "varchar(50) not null";
        }
    }

    public static class Table_OCInfo {
        public static final String TABLE_NAME = "e_N105";
        public static final String TABLE_CREATER = "create table if not exists " + TABLE_NAME
                + "("
                + Column.id + " " + Type.id + ", "
                + Column.applicationOpenTime + " " + Type.applicationOpenTime + ", "
                + Column.applicationCloseTime + " " + Type.applicationCloseTime + ", "
                + Column.applicationPackageName + " " + Type.applicationPackageName + ", "
                + Column.applicationName + " " + Type.applicationName + ", "
                + Column.applicationVersionCode + " " + Type.applicationVersionCode + ", "
                + Column.insertTime + " " + Type.insertTime + ", "
                + Column.network + " " + Type.network + ", "
                + Column.switchType + " " + Type.switchType + ", "
                + Column.applicationType + " " + Type.applicationType + ", "
                + Column.collectionType + " " + Type.collectionType + ", "
                + Column.obligate_a + " " + Type.obligate_a + ", "
                + Column.obligate_b + " " + Type.obligate_b + ", "
                + Column.obligate_c + " " + Type.obligate_c
                + ")";

        public static class Column {
            public static final String id = "aaa";
            public static final String applicationOpenTime = "aa";
            public static final String applicationCloseTime = "ab";
            public static final String applicationPackageName = "ac";
            public static final String applicationName = "ad";
            public static final String applicationVersionCode = "ae";
            public static final String insertTime = "aab";
            public static final String network = "af";
            public static final String switchType = "ag";
            public static final String applicationType = "ah";
            public static final String collectionType = "ai";
            public static final String obligate_a = "epa";
            public static final String obligate_b = "epb";
            public static final String obligate_c = "epc";
        }

        // type and limit
        public static class Type {
            public static final String id = "Integer Primary Key Autoincrement";
            public static final String applicationOpenTime = "varchar(50) not null";
            public static final String applicationCloseTime = "varchar(50) not null";
            public static final String applicationPackageName = "varchar(200) not null";
            public static final String applicationName = "varchar(200) not null";
            public static final String applicationVersionCode = "varchar(50) not null";
            public static final String insertTime = "varchar(50) not null";
            public static final String network = "varchar(50)";
            public static final String switchType = "varchar(10)";
            public static final String applicationType = "varchar(10)";
            public static final String collectionType = "varchar(10)";
            public static final String obligate_a = "text";
            public static final String obligate_b = "text";
            public static final String obligate_c = "text";
        }
    }

    public static class Table_IUUInfo {
        public static final String TABLE_NAME = "e_N106";
        public static final String TABLE_CREATER = "create table if not exists " + TABLE_NAME
                + "("
                + Column.id + " " + Type.id + ", "
                + Column.applicationPackageName + " " + Type.applicationPackageName + ", "
                + Column.applicationName + " " + Type.applicationName + ", "
                + Column.applicationVersionCode + " " + Type.applicationVersionCode + ", "
                + Column.actionType + " " + Type.actionType + ", "
                + Column.actionHappenTime + " " + Type.actionHappenTime + ", "
                + Column.insertTime + " " + Type.insertTime + ", "
                + Column.obligate_a + " " + Type.obligate_a + ", "
                + Column.obligate_b + " " + Type.obligate_b + ", "
                + Column.obligate_c + " " + Type.obligate_c
                + ")";

        public static class Column {
            public static final String id = "aaa";
            public static final String applicationPackageName = "aa";
            public static final String applicationName = "ab";
            public static final String applicationVersionCode = "ac";
            public static final String actionType = "ad";
            public static final String actionHappenTime = "ae";
            public static final String insertTime = "aab";
            public static final String obligate_a = "epa";
            public static final String obligate_b = "epb";
            public static final String obligate_c = "epc";
        }

        // type and limit
        public static class Type {
            public static final String id = "Integer Primary Key Autoincrement";
            public static final String applicationPackageName = "varchar(200) not null";
            public static final String applicationName = "varchar(200) not null";
            public static final String applicationVersionCode = "varchar(50) not null";
            public static final String actionType = "varchar(50) not null";
            public static final String actionHappenTime = "varchar(50) not null";
            public static final String insertTime = "varchar(50) not null";
            public static final String obligate_a = "text";
            public static final String obligate_b = "text";
            public static final String obligate_c = "text";
        }
    }

//    public static class Table_NetworkInfo {
//        public static final String TABLE_NAME = "e_N107";
//        public static final String TABLE_CREATER = "create table if not exists " + TABLE_NAME
//                + "("
//                + Column.id + " " + Type.id + ", "
//                + Column.changeTime + " " + Type.changeTime + ", "
//                + Column.networkType + " " + Type.networkType + ", "
//                + Column.insertTime + " " + Type.insertTime + ", "
//                + Column.obligate_a + " " + Type.obligate_a + ", "
//                + Column.obligate_b + " " + Type.obligate_b + ", "
//                + Column.obligate_c + " " + Type.obligate_c
//                + ")";
//
//        public static class Column {
//            public static final String id = "aaa";
//            public static final String changeTime = "aa";
//            public static final String networkType = "ab";
//            public static final String insertTime = "aab";
//            public static final String obligate_a = "epa";
//            public static final String obligate_b = "epb";
//            public static final String obligate_c = "epc";
//        }
//
//        // type and limit
//        public static class Type {
//            public static final String id = "Integer Primary Key Autoincrement";
//            public static final String changeTime = "varchar(50) not null";
//            public static final String networkType = "varchar(50) not null";
//            public static final String insertTime = "varchar(50) not null";
//            public static final String obligate_a = "text";
//            public static final String obligate_b = "text";
//            public static final String obligate_c = "text";
//        }
//    }

    public static class Table_WBGInfo {
        public static final String TABLE_NAME = "e_N108";
        public static final String TABLE_CREATER = "create table if not exists " + TABLE_NAME
                + "("
                + Column.id + " " + Type.id + ", "
                + Column.ssid + " " + Type.ssid + ", "
                + Column.bssid + " " + Type.bssid + ", "
                + Column.level + " " + Type.level + ", "
                + Column.lac + " " + Type.lac + ", "
                + Column.cellid + " " + Type.cellid + ", "
                + Column.ct + " " + Type.ct + ", "
                + Column.gl + " " + Type.gl + ", "
                + Column.ip + " " + Type.ip + ", "
                + Column.obligate_a + " " + Type.obligate_a + ", "
                + Column.obligate_b + " " + Type.obligate_b + ", "
                + Column.obligate_c + " " + Type.obligate_c
                + ")";

        public static class Column {
            public static final String id = "aaa";
            public static final String ssid = "aa";
            public static final String bssid = "ab";
            public static final String level = "ac";
            public static final String lac = "ad";
            public static final String cellid = "ae";
            public static final String ct = "af";
            public static final String gl = "ag";
            public static final String ip = "ah";
            public static final String obligate_a = "epa";
            public static final String obligate_b = "epb";
            public static final String obligate_c = "epc";
        }

        // type and limit
        public static class Type {
            public static final String id = "Integer Primary Key Autoincrement";
            public static final String ssid = "varchar(50)";
            public static final String bssid = "varchar(50)";
            public static final String level = "varchar(50)";
            public static final String lac = "varchar(50)";
            public static final String cellid = "varchar(50)";
            public static final String ct = "varchar(50)";
            public static final String gl = "varchar(50)";
            public static final String ip = "varchar(50)";
            public static final String obligate_a = "text";
            public static final String obligate_b = "text";
            public static final String obligate_c = "text";
        }
    }


//    public static class Table_AppUploadInfo {
//        public static final String TABLE_NAME = "e_N109";
//        public static final String TABLE_CREATER = "create table if not exists " + TABLE_NAME
//                + "("
//                + Column.id + " " + Type.id + ", "
//                + Column.day + " " + Type.day + ", "
//                + Column.obligate_a + " " + Type.obligate_a + ", "
//                + Column.obligate_b + " " + Type.obligate_b + ", "
//                + Column.obligate_c + " " + Type.obligate_c
//                + ")";
//
//        public static class Column {
//            public static final String id = "aaa";
//            public static final String day = "aa";
//            public static final String obligate_a = "epa";
//            public static final String obligate_b = "epb";
//            public static final String obligate_c = "epc";
//        }
//
//        // type and limit
//        public static class Type {
//            public static final String id = "Integer Primary Key Autoincrement";
//            public static final String day = "varchar(20) not null";
//            public static final String obligate_a = "text";
//            public static final String obligate_b = "text";
//            public static final String obligate_c = "text";
//        }
//    }
}
