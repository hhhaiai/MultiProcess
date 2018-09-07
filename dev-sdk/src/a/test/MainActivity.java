package a.test;

import com.eguan.R;
import com.eguan.monitor.EguanMonitorAgent;

import android.app.Activity;
import android.os.Bundle;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018年9月3日 下午6:42:32
 * @Author: sanbo
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EguanMonitorAgent.getInstance().setDebugMode(this, true);
        EguanMonitorAgent.getInstance().initEguan(this, "", "channel");
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
