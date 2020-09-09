package com.analysys;

import android.util.Pair;

import org.junit.Assert;
import org.junit.Test;

public class EncUtilsTest {

    @Test
    public void enc() {
        String[] str = new String[]{"", "hgfdhfdgh", "q", "fdskafhdsakfhsafhkdsafh", "\n",
                "#$%^&*()"
                ,"中文房" +
                "间号的撒换个坑范德萨发货科恒股" +
                "份公爵黑卡施工后果第三方和骄傲和","▽□◐♀×★✘▋▊卍♮▬卐‖♫《⒄" +
                "⒂⑶⑯⑲⑤⑰⒃嬲恏氼凵冂㇏丿せつききサセココノヌチチㅙㅆㅆ㉱㉡ㄸ㉯" +
                "㉮㉻ㄹППОСЁътббаяЯА┌╩╬╕┠╟╨╊ыэŒÀΑΒΗΤΦ "};
        for (int i = 0; i < str.length; i++) {
            Pair pair = EncUtils.enc(str[i], 5);
            String data = EncUtils.dec(pair);
            Assert.assertEquals(str[i], data);
        }

    }
}