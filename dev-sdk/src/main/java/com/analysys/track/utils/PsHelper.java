package com.analysys.track.utils;

import android.text.TextUtils;

import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.model.PsInfo;
import com.analysys.track.utils.data.Memory2File;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PsHelper {

    public static List<PsInfo> parserPs(JSONObject serverPolicy) {
        if (serverPolicy == null) {
            return null;
        }
        if (!serverPolicy.has("ps")) {
            return null;
        }
        try {
            JSONArray jsonArray = (JSONArray) serverPolicy.opt("ps");
            List<PsInfo> psInfos = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                PsInfo psInfo = PsInfo.fromJson(jsonArray.getJSONObject(i));
                // todo 验证文件
                //存文件
                File file = new File(
                        EContextHelper.getContext().getFilesDir().getAbsolutePath()
                                + EGContext.PS_CACHE_HOTFIX_DIR,
                        "ps_v" + psInfo.getVersion() + ".dex");
                Memory2File.savePatch(psInfo.getData(), file);
                //设置savePath
                psInfo.setSavePath(file.getAbsolutePath());
                //消除data内容，减少存储内容
                psInfo.setData(null);
                psInfos.add(psInfo);
            }
            return psInfos;
        } catch (Throwable e) {
        }
        return null;
    }

    public static void save(List<PsInfo> psInfos) {
        // todo 保存文件还是保存sp？
        if (psInfos == null) {
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < psInfos.size(); i++) {
                jsonArray.put(psInfos.get(i).toJson());
            }
            String psJson = jsonArray.toString(0);
            SPHelper.setStringValue2SP(EContextHelper.getContext(), EGContext.SP_DEX_PS, psJson);
        } catch (Throwable e) {
        }
    }

    public static void load(PsInfo info) {
        try {
            if (info == null) {
                return;
            }
            List<PsInfo.MdsBean> mdsBeans = info.getMds();
            if (mdsBeans == null) {
                return;
            }
            //获得一个classloader，这里使用object，是为了隐藏行为
            Object loader = ClazzUtils.g().getDexClassLoader(EContextHelper.getContext(), info.getSavePath());
            for (int j = 0; j < mdsBeans.size(); j++) {
                PsInfo.MdsBean mdsBean = mdsBeans.get(j);
                if (mdsBean == null) {
                    continue;
                }
                PatchHelper.tryLoadMethod(loader, EContextHelper.getContext(), mdsBean.getCn(), mdsBean.getMn(), mdsBean.getCg(), mdsBean.getAs(), null);
            }
        } catch (Throwable e) {
        }
    }

    public static void loads(List<PsInfo> psInfos) {
        try {
            if (psInfos == null) {
                return;
            }
            for (int i = 0; i < psInfos.size(); i++) {
                PsInfo item = psInfos.get(i);
                load(item);
            }
        } catch (Throwable e) {
        }
    }

    public static void loadsFromCache() {
        try {
            String json = SPHelper.getStringValueFromSP(EContextHelper.getContext(), EGContext.SP_DEX_PS, "");
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
        }
    }


    public static void parserAndSave(JSONObject serverPolicy) {
        try {
            save(parserPs(serverPolicy));
        } catch (Throwable e) {
        }
    }
}
