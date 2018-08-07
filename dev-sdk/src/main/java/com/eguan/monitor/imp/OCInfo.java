package com.eguan.monitor.imp;

import android.text.TextUtils;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.TimeUtils;

import java.io.Serializable;

/**
 * 应用打开关闭实体类
 *
 * @author Machenike
 */
public class OCInfo implements Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 应用打开时间，转换成时间戳，如：“1296035591”
     */
    private String ApplicationOpenTime;
    /**
     * 应用关闭时间，转换成时间戳，如：“1296035599”
     */
    private String ApplicationCloseTime;
    /**
     * 应用包名，如：“com.qzone”
     */
    private String ApplicationPackageName;
    /**
     * 应用程序名，如：“QQ空间”
     */
    private String ApplicationName;
    /**
     * 应用版本|应用版本号，如“5.4.1|89”
     */
    private String ApplicationVersionCode;

    /**
     * 网络状态信息
     *
     * @return
     */
    private String Network;

    private String SwitchType;
    private String ApplicationType;

    public String getCollectionType() {
        return CollectionType;
    }

    public void setCollectionType(String collectionType) {
        CollectionType = collectionType;
    }

    private String CollectionType;

    public String getNetwork() {
        return Network;
    }

    public void setNetwork(String network) {
        Network = network;
    }


    public String getApplicationOpenTime() {
        return ApplicationOpenTime;
    }

    public void setApplicationOpenTime(String applicationOpenTime) {
        ApplicationOpenTime = applicationOpenTime;
    }

    public String getApplicationCloseTime() {
        return ApplicationCloseTime;
    }

    public void setApplicationCloseTime(String applicationCloseTime) {
        ApplicationCloseTime = applicationCloseTime;
    }

    public String getApplicationPackageName() {
        return ApplicationPackageName;
    }

    public void setApplicationPackageName(String applicationPackageName) {
        ApplicationPackageName = applicationPackageName;
    }

    public String getApplicationName() {
        return ApplicationName;
    }

    public void setApplicationName(String applicationName) {
        ApplicationName = applicationName;
    }

    public String getApplicationVersionCode() {
        return ApplicationVersionCode;
    }

    public void setApplicationVersionCode(String applicationVersionCode) {
        ApplicationVersionCode = applicationVersionCode;
    }

    public String getSwitchType() {
        return SwitchType;
    }

    public void setSwitchType(String switchType) {
        SwitchType = switchType;
    }

    public String getApplicationType() {
        return ApplicationType;
    }

    public void setApplicationType(String applicationType) {
        ApplicationType = applicationType;
    }

    @Override
    public String toString() {
        return "OCInfo [aot=" + TimeUtils.longToTime(Long.valueOf(TextUtils.isEmpty(ApplicationOpenTime) ? "0" : ApplicationOpenTime))
                + ", act=" + TimeUtils.longToTime(Long.valueOf(TextUtils.isEmpty(ApplicationCloseTime) ? "0" : ApplicationCloseTime))
                + ", packName=" + ApplicationPackageName
                + ", AppName=" + ApplicationName
                + ", AppVer=" + ApplicationVersionCode + "]";
    }

    @Override
    protected OCInfo clone() {
        OCInfo oc = null;
        try {
            oc = (OCInfo) super.clone();
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

        return oc;
    }
}
