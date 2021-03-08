package cn.analysys.casedemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cslib.CaseHelper;

import cn.analysys.casedemo.cases.CaseCtl;
import cn.analysys.casedemo.sdkimport.Helper;
import cn.analysys.casedemo.utils.EL;

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
                CaseCtl.addCases(mContext);
                CaseHelper.openCasePage(mContext);
                break;
            default:
                break;
        }
    }

    public void openLog() {
        new Thread(() -> {
            try {
                Helper.shell("setprop log.tag.sanbo.demo VERBOSE");
            } catch (Throwable e) {
                EL.e(e);
            }
        }).start();
    }
}