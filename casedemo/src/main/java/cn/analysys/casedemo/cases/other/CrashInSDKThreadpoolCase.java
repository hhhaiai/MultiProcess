package cn.analysys.casedemo.cases.other;

import com.cslib.defcase.ETestCase;
import com.cslib.utils.L;

import java.util.ArrayList;
import java.util.List;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

public class CrashInSDKThreadpoolCase extends ETestCase {

    public CrashInSDKThreadpoolCase() {
        super("SDK线程池内发生崩溃测试");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {

        final StringBuilder sb = new StringBuilder();
        sb.append("==>>>该部分错误持续迭代(测试通过:调用了crash代码且无崩溃发生)<<<==").append("\n");
        case1(sb);
        case2(sb);
        case3(sb);
        case4(sb);
        Woo.logFormCase(sb.toString());
        return true;
    }

    private void case4(StringBuilder sb) {
        sb.append("类型转换错误：");
        SDKHelper.runOnWorkThread(() -> {
            String s = "hello";
            Integer aa = Integer.valueOf(s);
        });
        sb.append("测试通过").append("\n");
    }

    private void case3(StringBuilder sb) {
        sb.append("空指针错误：");
        SDKHelper.runOnWorkThread(() -> {
            ArrayList as = null;
            L.i("---->" + as.get(2));
        });
        sb.append("测试通过").append("\n");
    }

    private void case2(StringBuilder sb) {
        sb.append("数组越界错误：");
        SDKHelper.runOnWorkThread(() -> {

            String[] ss = {"1", "2"};
            L.i("---->" + ss[10]);
        });
        sb.append("测试通过").append("\n");
    }

    private void case1(StringBuilder sb) {
        sb.append("列表越界错误：");
        SDKHelper.runOnWorkThread(() -> {
            List<String> s = new ArrayList<String>();
            s.add("xxx");
            L.i("---->" + s.get(10));
        });
        sb.append("测试通过").append("\n");
    }


}