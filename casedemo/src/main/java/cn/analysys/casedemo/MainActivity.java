package cn.analysys.casedemo;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.analysys.casedemo.cases.CaseCtl;
import cn.analysys.casedemo.utils.SDKHelper;
import me.hhhaiai.testcaselib.utils.L;
import me.hhhaiai.testcaselib.utils.TcaseClazzUtils;

public class MainActivity extends Activity {

    private Activity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepare();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showAddr();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
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


    private void prepare() {
        try {
            mContext = this;
            openLog();
            reqPer();
        } catch (Throwable e) {
            L.e(e);
        }
    }

    private void openLog() {
        new Thread(() -> {
            try {
                SDKHelper.shell("setprop log.tag.sanbo VERBOSE");
                SDKHelper.shell("setprop log.tag.analysys VERBOSE");
            } catch (Throwable e) {
                L.e(e);
            }
        }).start();
    }

    /**************************************************************************************************/
    /***************************************   底部数据库地址展示(点击拷贝，双击打开)     *******************************************/
    /**************************************************************************************************/
    private String addr = null;
    private long mLastTime = 0;
    private long mCurTime = 0;
    private Toast mToast = null;

    private void showAddr() {
        try {
            addr = (String) TcaseClazzUtils.invokeStaticMethod("com.amitshekhar.DebugDB", "getAddressLog");

            if (!TextUtils.isEmpty(addr)) {
                addr = addr.replaceAll("Open\\s+", "").replaceAll("\\s+in your browser", "");
                TextView tv = findViewById(R.id.tvDebugAddr);
                tv.setText(addr);

//                tv.setOnClickListener(
//                        v -> {
//                            //支持点击打开
//                            Uri uri = Uri.parse(addr);
//                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                            startActivity(intent);
//                        }
//
//                );
                mLastTime = 0;
                mCurTime = 0;
                tv.setOnClickListener(
                        v -> {
                            mLastTime = mCurTime;
                            mCurTime = System.currentTimeMillis();
                            if (mCurTime - mLastTime < 200) {//双击事件
                                mCurTime = 0;
                                mLastTime = 0;
                                handler.removeMessages(1);
                                handler.sendEmptyMessage(2);
                            } else {//单击事件
                                handler.sendEmptyMessageDelayed(1, 210);
                            }
                        }
                );
            }
        } catch (Throwable ignore) {
            L.e(ignore);
        }
    }


    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case 1:
                        L.i("这是单击事件");
                        //写到剪切板
                        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mClipData = ClipData.newPlainText("Label", addr);
                        cm.setPrimaryClip(mClipData);
                        mToast = Toast.makeText(mContext, "已经拷贝到剪切板:\n" + addr, Toast.LENGTH_LONG);
                        mToast.show();
                        break;
                    case 2:
                        L.i("这是双击事件");
                        //支持点击打开
                        Uri uri = Uri.parse(addr);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                        break;
                }
            } catch (Throwable e) {
                L.e(e);
            }
        }
    };
    /**************************************************************************************************/
    /***************************************    权限申请     *******************************************/
    /**************************************************************************************************/
    private void reqPer() {
        if (Build.VERSION.SDK_INT > 22) {
            try {
                SDKHelper.reqPermission(
                        mContext,
                        new String[]{
                                Manifest.permission.READ_PHONE_STATE
                                , Manifest.permission.MODIFY_PHONE_STATE
                                , "android.permission.READ_PRIVILEGED_PHONE_STATE"
                                , "android.permission.ACCESS_BACKGROUND_LOCATION"
                                , Manifest.permission.ACCESS_FINE_LOCATION
                                , Manifest.permission.ACCESS_COARSE_LOCATION
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.INTERNET
                                , Manifest.permission.WRITE_SETTINGS

                        },
                        9527
                );
            } catch (Throwable e) {
                L.e(e);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}