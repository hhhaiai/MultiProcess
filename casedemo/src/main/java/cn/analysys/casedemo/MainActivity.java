package cn.analysys.casedemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cslib.CaseHelper;
import com.cslib.utils.L;

import cn.analysys.casedemo.cases.CaseCtl;
import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();

        openLog();
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGotoTestCasePage:
                CaseCtl.gotoCase(mContext);
                break;
            case R.id.btnTest:
                break;
            default:
                break;
        }
        
    }

    public void openLog() {
        new Thread(() -> {
            try {
                SDKHelper.shell("setprop log.tag.sanbo.demo VERBOSE");
            } catch (Throwable e) {
                L.e(e);
            }
        }).start();
    }
}