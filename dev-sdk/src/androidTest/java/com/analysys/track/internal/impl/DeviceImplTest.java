package com.analysys.track.internal.impl;

import com.analysys.track.AnalsysTest;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DeviceImplTest extends AnalsysTest {

    @Test
    public void getInstance() {
    }

    @Test
    public void getBluetoothAddress() {
        //10 取不到
        String s = DeviceImpl.getInstance(mContext).getBluetoothAddress(mContext);
        assertNotNull(s);
    }

    @Test
    public void getDeviceId() {
        String s = DeviceImpl.getInstance(mContext).getDeviceId();
        assertNotNull(s);
    }

    @Test
    public void getMac() {
        //1 3c:28:6d:ff:dd:a8
        //2 3c:28:6d:ff:dd:a8
        String s = DeviceImpl.getInstance(mContext).getMac();
        assertNotNull(s);
    }

    @Test
    public void getSerialNumber() {
    }

    @Test
    public void getResolution() {
    }

    @Test
    public void getDotPerInch() {
    }

    @Test
    public void getMobileOperator() {

        DeviceImpl.getInstance(mContext).getMobileOperator();
    }

    @Test
    public void getMobileOperatorName() {
    }

    @Test
    public void getNetworkOperatorCode() {
    }

    @Test
    public void getNetworkOperatorName() {
    }

    @Test
    public void getApplicationName() {
    }

    @Test
    public void getApplicationPackageName() {
    }

    @Test
    public void getApplicationVersionCode() {
    }

    @Test
    public void getAppMD5() {
    }

    @Test
    public void doFingerprint() {
    }

    @Test
    public void getAppSign() {
    }

    @Test
    public void getBluetoothName() {
    }

    @Test
    public void processBattery() {
    }

    @Test
    public void getSystemFontSize() {
    }

    @Test
    public void getSystemHour() {
    }

    @Test
    public void getSystemLanguage() {
    }

    @Test
    public void getSystemArea() {
    }

    @Test
    public void getTimeZone() {
    }

    @Test
    public void getBuildSupportedAbis() {
    }

    @Test
    public void getBuildSupportedAbis32() {
    }

    @Test
    public void getBuildSupportedAbis64() {
    }

    @Test
    public void getIDFA() {
    }
}