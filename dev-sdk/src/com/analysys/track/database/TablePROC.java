package com.analysys.track.database;

import android.content.ContentValues;
import android.content.Context;
import android.util.Base64;

import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EncryptUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TablePROC {
    Context mContext;
    private static class Holder {
        private static final TablePROC INSTANCE = new TablePROC();
    }

    public static TablePROC getInstance(Context context) {
        if (TablePROC.Holder.INSTANCE.mContext == null) {
            TablePROC.Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return TablePROC.Holder.INSTANCE;
    }
    /**
     * json数据转成ContentValues
     */
    public List<ContentValues> getContentValues(String time , JSONArray xxxArray) {
        List<ContentValues> list = null;
        ContentValues cv = null;
        try{
            if (xxxArray != null) {
                list = new ArrayList<ContentValues>();
                ELOG.i("length ::: "+xxxArray.length());
                for (int i = 0;i < xxxArray.length();i++){
                    JSONObject js = (JSONObject) xxxArray.get(i);
//                    ELOG.i(i+  " js ::::::   "+js);
                    if(js == null) continue;
                    cv = new ContentValues();
                    cv.put(DBConfig.PROCInfo.Column.PARENT_ID_TIME, EncryptUtils.encrypt(mContext,time));
                    cv.put(DBConfig.PROCInfo.Column.CONTENT,EncryptUtils.encrypt(mContext,new String(Base64.encode(js.toString().getBytes(),Base64.DEFAULT))));
                    list.add(cv);
                }
            }
        }catch (Throwable t){
            ELOG.e(t +"  getContentValues has an exception...");
        }
        return list;
    }
}
