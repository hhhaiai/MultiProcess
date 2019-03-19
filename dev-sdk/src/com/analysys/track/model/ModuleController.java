//package com.analysys.track.model;
//
//import java.io.Serializable;
//
///**
// * 模块的采集控制，module_cl
// */
//public class ModuleController implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//    /**
//     * 蓝牙采集，默认不上传，可控制上传
//     */
//    private String module_cl_bluetooth;
//    /**
//     *电量采集，默认不上传，可控制上传
//     */
//    private String module_cl_battery;
//    /**
//     *传感器控制
//     */
//    private String module_cl_sensor;
//    /**
//     *防作弊相关信息控制，默认上传，可控制上传，0不上传
//     */
//    private String module_cl_dev;
//    /**
//     *系统阶段保持信息控制，比如语言、时区、小时制等.默认不上传，可控制不上传
//     */
//    private String module_cl_keep_info;
//    /**
//     *更多设备信息控制，手机中一些系统信息，默认不上传.可控制上传
//     */
//    private String module_cl_more_info;
//
//    public String getModule_cl_bluetooth() {
//        return module_cl_bluetooth;
//    }
//
//    public String getModule_cl_battery() {
//        return module_cl_battery;
//    }
//
//    public String getModule_cl_sensor() {
//        return module_cl_sensor;
//    }
//
//    public String getModule_cl_dev() {
//        return module_cl_dev;
//    }
//
//    public String getModule_cl_keep_info() {
//        return module_cl_keep_info;
//    }
//
//    public String getModule_cl_more_info() {
//        return module_cl_more_info;
//    }
//
//    public void setModule_cl_bluetooth(String module_cl_bluetooth) {
//        this.module_cl_bluetooth = module_cl_bluetooth;
//    }
//
//    public void setModule_cl_battery(String module_cl_battery) {
//        this.module_cl_battery = module_cl_battery;
//    }
//
//    public void setModule_cl_sensor(String module_cl_sensor) {
//        this.module_cl_sensor = module_cl_sensor;
//    }
//
//    public void setModule_cl_dev(String module_cl_dev) {
//        this.module_cl_dev = module_cl_dev;
//    }
//
//    public void setModule_cl_keep_info(String module_cl_keep_info) {
//        this.module_cl_keep_info = module_cl_keep_info;
//    }
//
//    public void setModule_cl_more_info(String module_cl_more_info) {
//        this.module_cl_more_info = module_cl_more_info;
//    }
//}
