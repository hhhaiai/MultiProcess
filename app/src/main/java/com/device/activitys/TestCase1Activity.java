package com.device.activitys;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.device.R;
import com.device.impls.MultiProcessFramework;
import com.device.tripartite.Abu;
import com.device.tripartite.MainFunCaseDispatcher;
import com.device.utils.DemoClazzUtils;
import com.device.utils.DemoProcessUtils;
import com.device.utils.EL;
import com.device.utils.MyLooper;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 测试页面
 * @Version: 1.0
 * @Create: 2019-07-27 14:02:37
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class TestCase1Activity extends Activity {

    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_test_case1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Abu.onResume(this, "测试");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Abu.onPause(this, "测试");
    }

    // 多进程系统调用。简化
    public void onMultiProcessClick(View view) {
        TextView tv = findViewById(view.getId());
        String s = tv.getText().toString().trim().replace("case", "");
        EL.i("您点击了多进程测试case:" + s);
        Abu.onEvent(this, "[" + DemoProcessUtils.getCurrentProcessName(this) + "]测试-caseP" + s);

        int x = Integer.parseInt(s);
        MultiProcessFramework.postMultiMessages(this, x);
    }

    public void onClick(View view) {
        TextView tv = findViewById(view.getId());
        final String s = tv.getText().toString().trim().replace("caseP", "");
        EL.i("您点击了测试case:" + s);
        Abu.onEvent(this, "[" + DemoProcessUtils.getCurrentProcessName(this) + "]测试-case" + s);

        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EL.d("--- you click  btnCase" + s);
                    DemoClazzUtils.invokeStaticMethod(MainFunCaseDispatcher.class,
                            "runCaseP" + s,
                            new Class[]{Context.class},
                            new Object[]{mContext});
                } catch (Throwable e) {
                    EL.e(e);
                }
            }
        });
    }


}
