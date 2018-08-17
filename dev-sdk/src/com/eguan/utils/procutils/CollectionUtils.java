package com.eguan.utils.procutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionUtils {
    public static List<String> subtract(List<String> alist, List<String> blist) {

        List<String> list = new ArrayList<String>();
        Collections.addAll(list, new String[alist.size()]);
        Collections.copy(list, alist);
        if (alist == null || alist.size() <= 0) {
            return new ArrayList<String>();
        } else if (blist == null || blist.size() <= 0) {
            return alist;
        } else {
            for (int i = 0; i < list.size(); i++) {
                String org = list.get(i);
                for (int j = 0; j < blist.size(); j++) {
                    String ep = blist.get(j);
                    if (org.equals(ep)) {
                        list.remove(i);
                        i--;
                    }
                }
            }
            return list;
        }
    }
}
