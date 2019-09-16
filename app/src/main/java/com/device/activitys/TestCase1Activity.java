package com.device.activitys;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.device.R;
import com.device.impls.MainFunCase;
import com.device.impls.MultiProcessWorker;
import com.device.utils.EL;
import com.analysys.track.utils.ProcessUtils;
import com.umeng.analytics.MobclickAgent;


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
        MobclickAgent.onResume(this);
        MobclickAgent.onPageStart("测试");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        MobclickAgent.onPageEnd("测试");
    }

    // 多进程系统调用。简化
    public void onMultiProcessClick(View view) {
        TextView tv = findViewById(view.getId());
        String s = tv.getText().toString().trim().replace("case", "");
        EL.i("您点击了多进程测试case:" + s);
        MobclickAgent.onEvent(this, "[" + ProcessUtils.getCurrentProcessName(this) + "]测试-caseP" + s);

        int x = Integer.parseInt(s);
        MultiProcessWorker.postMultiMessages(this, x);
    }

    public void onClick(View view) {
        TextView tv = findViewById(view.getId());
        String s = tv.getText().toString().trim().replace("caseP", "");
        EL.i("您点击了测试case:" + s);
        MobclickAgent.onEvent(this, "[" + ProcessUtils.getCurrentProcessName(this) + "]测试-case" + s);
        MainFunCase.runCase(mContext, s);
    }


}
