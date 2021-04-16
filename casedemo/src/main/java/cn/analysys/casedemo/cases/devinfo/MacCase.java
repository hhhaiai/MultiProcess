package cn.analysys.casedemo.cases.devinfo;

import android.annotation.TargetApi;
import android.text.TextUtils;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import cn.analysys.casedemo.utils.Woo;
import me.hhhaiai.testcaselib.defcase.ETestCase;
import me.hhhaiai.testcaselib.utils.L;


public class MacCase extends ETestCase {
    public MacCase() {
        super("MAC");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        getMacByJavaAPI();
        return true;
    }


    @TargetApi(9)
    private static void getMacByJavaAPI() {
        Map<String, String> map = new HashMap<String, String>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                try {
                    NetworkInterface netInterface = interfaces.nextElement();
                    String name = netInterface.getName();
//                    L.i("name:" + name);
//                if ("wlan0".equalsIgnoreCase(name)
//                        || "wlan1".equalsIgnoreCase(name)
//                        || "eth0".equalsIgnoreCase(name)
//                ) {
                    byte[] addr = netInterface.getHardwareAddress();
                    if (addr == null || addr.length == 0) {
                        continue;
                    }
                    StringBuilder buf = new StringBuilder();

                    for (byte b : addr) {
                        buf.append(String.format("%02X:", b));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    if ("wlan0".equalsIgnoreCase(name)
                            || "wlan1".equalsIgnoreCase(name)
                            || "eth0".equalsIgnoreCase(name)
                    ) {
                        map.put("====" + name, buf.toString().toLowerCase(Locale.getDefault()));
                    } else {
                        map.put(name, buf.toString().toLowerCase(Locale.getDefault()));
                    }
//                }
                } catch (Throwable igone) {
                    L.e(igone);
                }
            }
        } catch (Throwable igone) {
            L.e(igone);
        }

        StringBuffer sb = new StringBuffer();
        sb.append("==================Java api 测试获取MAC========================").append("\n");

        if (map.size() > 0) {
            Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> en = it.next();
                sb.append("[ ").append(en.getKey()).append(" ] (").append(en.getValue()).append(") ----->").append(hasRealMacAddress(en.getValue())).append("\n");
            }
        }
        Woo.logFormCase(sb.toString());
    }


    /**
     * 是否有个有效的mac地址
     *
     * @param mac
     * @return true: 有效
     * fasle: 无效
     */
    private static boolean hasRealMacAddress(String mac) {
        return !TextUtils.isEmpty(mac) && !"02:00:00:00:00:00".equals(mac);
    }
}
