package com.analysys.track.utils;

import android.text.TextUtils;

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
import java.util.List;
import java.util.Map;

public class PsHelper {

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
        // todo 保存文件还是保存sp,现在是存SP 文件？
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
            List<PsInfo.MdsBean> mdsBeans = info.getMds();
            if (mdsBeans == null) {
                return;
            }
            Object loader = classLoaderMap.get(info.getVersion());
            if (loader == null) {
                loader = prepare(info);
                classLoaderMap.put(info.getVersion(), loader);
            }
            if (loader == null) {
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
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private Object prepare(PsInfo info) {
        try {
            //戴面具的dex原始数据路径
            File maskRawDexFile = new File(info.getSavePath());
            if (!maskRawDexFile.exists() || !maskRawDexFile.isFile() || maskRawDexFile.length() == 0) {
                //索引存在，但dex被删除了,清除策略，下次上传会重新下载
                //SPHelper.removeKey(EContextHelper.getContext(), UploadKey.Response.RES_POLICY_VERSION);
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
                    if (DebugDev.get(EContextHelper.getContext()).isDebugDevice()) {
                        return;
                    }
                    // String json = SPHelper.getStringValueFromSP(EContextHelper.getContext(), EGContext.SP_DEX_PS, "");
                    String json = new String(MaskUtils.takeOffMask(getPsIndexFile()), "utf-8");
                    json = EncryptUtils.decrypt(EContextHelper.getContext(), json);
                    if (TextUtils.isEmpty(json)) {
                        return;
                    }
                    JSONArray jsonArray = new JSONArray(json);
                    List<PsInfo> psInfos = new ArrayList<>(jsonArray.length());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        PsInfo psInfo = PsInfo.fromJson(jsonArray.getJSONObject(i));
                        psInfos.add(psInfo);
                    }
                    loads(psInfos);
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(e);
                    }
                }
            }
        });

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
     * @param data   事件需要处理的数据
     * @param action 事件id，标识
     */
    public void publish(Object data, String action) {
        try {
            for (Map.Entry<String, Object> item :
                    classLoaderMap.entrySet()) {
                Class pluginHandler = ClazzUtils.g().getClass("com.analysys.PluginHandler", item.getValue());
                Object pluginHandlerInstance = ClazzUtils.g().invokeStaticMethod(pluginHandler, "getInstance");
                ClazzUtils.g().invokeObjectMethod(pluginHandlerInstance, "publish",
                        new Class[]{Object.class, String.class}, new Object[]{data, action});
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

}
