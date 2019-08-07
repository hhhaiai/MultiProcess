package com.device.activitys;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.device.R;
import com.device.impls.TestCasesImpl;
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


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCase1:
                TestCasesImpl.runCase(mContext, 1);
                break;
            case R.id.btnCase2:
                TestCasesImpl.runCase(mContext, 2);
                break;
            case R.id.btnCase3:
                TestCasesImpl.runCase(mContext, 3);
                break;
            case R.id.btnCase4:
                TestCasesImpl.runCase(mContext, 4);
                break;
            case R.id.btnCase5:
                TestCasesImpl.runCase(mContext, 5);
                break;
            case R.id.btnCase6:
                TestCasesImpl.runCase(mContext, 6);
                break;
            case R.id.btnCase7:
                TestCasesImpl.runCase(mContext, 7);
                break;
            case R.id.btnCase8:
                TestCasesImpl.runCase(mContext, 8);
                break;
            case R.id.btnCase9:
                TestCasesImpl.runCase(mContext, 9);
                break;
            case R.id.btnCase10:
                TestCasesImpl.runCase(mContext, 10);
                break;
            case R.id.btnCase11:
                TestCasesImpl.runCase(mContext, 11);
                break;
            case R.id.btnCase12:
                TestCasesImpl.runCase(mContext, 12);
                break;
            case R.id.btnCase13:
                TestCasesImpl.runCase(mContext, 13);
                break;
            case R.id.btnCase14:
                TestCasesImpl.runCase(mContext, 14);
                break;
            case R.id.btnCase15:
                TestCasesImpl.runCase(mContext, 15);
                break;
            case R.id.btnCase16:
                TestCasesImpl.runCase(mContext, 16);
                break;
            case R.id.btnCase17:
                TestCasesImpl.runCase(mContext, 17);
                break;
            case R.id.btnCase18:
                TestCasesImpl.runCase(mContext, 18);
                break;
            case R.id.btnCaseP1:
                TestCasesImpl.runCase(mContext, 1001);
                break;
            case R.id.btnCaseP2:
                TestCasesImpl.runCase(mContext, 1002);
                break;
            case R.id.btnCaseP3:
                TestCasesImpl.runCase(mContext, 1003);
                break;
            case R.id.btnCaseP4:
                TestCasesImpl.runCase(mContext, 1004);
                break;
            case R.id.btnCaseP5:
                TestCasesImpl.runCase(mContext, 1005);
                break;
            case R.id.btnCaseP6:
                TestCasesImpl.runCase(mContext, 1006);
                break;
            default:
                break;
        }
    }


}
