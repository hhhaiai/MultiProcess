package com.device.activitys;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.device.R;
import com.device.impls.TestCasesImpl;
import com.device.utils.EL;


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

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCase1:
                EL.i("click btnCase1");
                TestCasesImpl.runCase1(mContext);
                break;
            default:
                break;
        }
    }


}
