package cn.analysys.casedemo.cases.dbs;

import com.cslib.defcase.ETestCase;
import com.cslib.utils.L;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

public class DBTableExit extends ETestCase {

    public DBTableExit() {
        super("数据库表存在检测");
    }

    @Override
    public void prepare() {
        SDKHelper.prepareDB();
    }

    @Override
    public boolean predicate() {
        try {

            boolean a = SDKHelper.checkAppsnapshotDB();
            boolean b = SDKHelper.checkFinfoDB();
            boolean c = SDKHelper.checkLocationDB();
            boolean d = SDKHelper.checkNetDB();
            boolean e = SDKHelper.checkOCDB();
            boolean f = SDKHelper.checkScanDB();
//            boolean g = SDKHelper.checkXxxDB();

            StringBuffer sb = new StringBuffer();

            sb.append("==================数据库表存在检测================\n")
                    .append("Appsnapshot是否存在:").append(a).append("\n")
                    .append("Finfo是否存在:").append(b).append("\n")
                    .append("Location是否存在:").append(c).append("\n")
                    .append("Net是否存在:").append(d).append("\n")
                    .append("OC是否存在:").append(e).append("\n")
                    .append("Scan是否存在:").append(f).append("\n")
//                    .append("xxx是否存在:").append(g).append("\n")
            ;
            Woo.logFormCase(sb.toString());
            if (!a || !b || !c || !d || !e || !f
//                    || !g
            ) {
                return false;
            }
        } catch (Throwable e) {
            L.e(e);
            return false;
        }
        return true;
    }

}

