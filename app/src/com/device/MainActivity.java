package com.device;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.analysys.dev.database.DBHelper;
import com.analysys.dev.internal.utils.DeviceInfo;
import com.analysys.dev.internal.utils.LL;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    DBHelper.getInstance(this);
  }
  public void testButton(View view) {

    LL.e("---- 设备硬件 ----");
    LL.i("系统名称：" + DeviceInfo.getSystemName());
    LL.i("系统版本：" + DeviceInfo.getSystemVersion());
    LL.i("设备品牌：" + DeviceInfo.getDeviceBrand());
    LL.i("设备id：" + DeviceInfo.getDeviceId(this));
    LL.i("设备型号：" + DeviceInfo.getDeviceModel());
    LL.i("设备MAC地址：" + DeviceInfo.getMac(this));
    LL.i("设备序列号：" + DeviceInfo.getSerialNumber(this));
    LL.i("设备分辨率：" + DeviceInfo.getResolution(this));
    LL.e("---- 运营商 ----");
    LL.i("运营商：" + DeviceInfo.getMobileOperator(this));
    LL.i("运营商名字：" + DeviceInfo.getMobileOperatorName(this));
    LL.i("接入运营商：" + DeviceInfo.getNetworkOperatorCode(this));
    LL.i("接入运营商名字：" + DeviceInfo.getNetworkOperatorName(this));
    LL.i("设备imei：" + DeviceInfo.getImeis(this));
    LL.i("设备imsi：" + DeviceInfo.getImsis(this));
    LL.e("---- 配置 ----");
    LL.i("推广渠道：" + DeviceInfo.getApplicationChannel());
    LL.i("样本应用key：" + DeviceInfo.getApplicationKey());
    LL.e("---- 应用信息 ----");
    LL.i("应用名称：" + DeviceInfo.getApplicationName(this));
    LL.i("API等级：" + DeviceInfo.getAPILevel());
    LL.i("应用包名：" + DeviceInfo.getApplicationPackageName(this));
    LL.i("SDK版本：" + DeviceInfo.getSdkVersion());
    LL.i("应用版本：" + DeviceInfo.getApplicationVersionCode());
    LL.i("应用MD5签名：" + DeviceInfo.getAppMD5(this));
    LL.i("应用MD5签名：" + DeviceInfo.getAppSign(this));
  }

}
