package com.device.impls;

import android.content.Context;

import com.analysys.track.impl.PolicyImpl;
import com.device.utils.EL;
import com.device.utils.MyLooper;

import org.json.JSONObject;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 测试case实现类
 * @Version: 1.0
 * @Create: 2019-07-27 14:19:53
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class TestCasesImpl {
    public static void runCase1(final Context context) {
        MyLooper.execute(new Runnable() {
            @Override
            public void run() {
                try {

//                // 1. 测试发起请求，接收策略
//                PolicyImpl.getInstance(context).clearSP();
//                UploadImpl.getInstance(context).doUploadImpl();

                    // 2. 测试接收并处理策略
                    PolicyImpl.getInstance(context).clearSP();
                    JSONObject obj = new JSONObject("{\"policyVer\": \"20190725190909\",\"patch\": {\"version\": \"004\",\"sign\":\n" +
                            "\"a45e13c22ff526afa339bcfd63fdc83a\",\"data\":\"UEsDBBQACAgIAO6O+U4AAAAAAAAAAAAAAAAUAAQATUVUQS1JTkYvTUFOSUZFU1QuTUb+ygAA803My0xLLS7RDUstKs7Mz7NSMNQz4OVySa3Q9clPTiwBCyXnJBYXpxbrpaRW8HI5F6UmlqSm6DpVWimkVACVG5rxcvFyAQBQSwcI8N6zmEcAAABJAAAAUEsDBBQACAgIAO6O+U4AAAAAAAAAAAAAAAALAAAAY2xhc3Nlcy5kZXidlE1oE0EUx9/MbnaTWtPY2prmtJprSQrqxYhYKX7Aig1CQHvatmvYkmxC3Jb05MfBu3jTCoKChXrRg149VulFvRSh0EsVQUHwLPp/M9MPqyc3+c2bffPezHsz83Ym7PaMHj1ODx/9TH4sr18Yma+tyqf0tbSZ3L97b3VlySZqE1G3dqyfzJOGrkBa3wPWgMMDQv3pPJp9kBPmfQnNhiRagXwB+Ra8Bx/ButRjm+AL+AZsiygPRsA5MAnmwQK4AW6DO+ABeAyWwXPwErwGbwD+lOJYQcbEyTH1gv0gBzgh20AmB+4vIgbX+D+R2jZtbA6YPuu3+s8k+0oaUltgqZwFZsoqmaJBoz8EKaHPE8en7VLbUtKAWXOYeH1b6V20B/XWKiwThzTyu1EI9SP6YOkc2jmeKYv5hPJZQ8PxHcGMBRi2vQzyysv6u4uvPrlXTsejPeRZvTSFsdjjtbNqHvbd+A9fzodj/GzpfKbYX6QQl4PxguyDpUuHqVewHGOZS2OE323MnjX5mmulYhHqLIeKxeKIoVTcfsg5GcVRcoqcsdJsMB+QGCfhk+WPVUj648CnQZ9HylGrPNGJ4uRy0gmDZoX6tboRxPXypanZcDr5Uwe7KK5XaPgv3Zm5qDETdvaYL1xPwua2eRJ2k/J4OB01g8bZVqcZYHZRJbtarZ4gUSNZ82lg8h/LOUG7HcYz5FxTXmQ3gygmqzWXULrNJn6rTi73kkZM6aSlHXFkLvZfoP11FfdeiT5+Ffk+PlFBt27aK5bMbFgis2gLsWbrmqA9e75V43JXnVu7an3rTLjeU7RT8w7t1L3wtB3Xvshpf64v6en5+XtgGRu+u+RpX3Wvc7rP35vfUEsHCFo9p8uGAgAAqAQAAFBLAQIUABQACAgIAO6O+U7w3rOYRwAAAEkAAAAUAAQAAAAAAAAAAAAAAAAAAABNRVRBLUlORi9NQU5JRkVTVC5NRv7KAABQSwECFAAUAAgICADujvlOWj2ny4YCAACoBAAACwAAAAAAAAAAAAAAAACNAAAAY2xhc3Nlcy5kZXhQSwUGAAAAAAIAAgB/AAAATAMAAAAA\"}}");
                    PolicyImpl.getInstance(context).saveRespParams(obj);
                } catch (Throwable e) {
                    EL.i(e);
                }
            }
        });
    }
}
