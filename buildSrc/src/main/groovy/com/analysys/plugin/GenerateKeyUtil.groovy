package com.analysys.plugin;

import org.gradle.api.Project

import java.util.regex.Matcher
import java.util.regex.Pattern

public class GenerateKeyUtil {
    public static void generateKey(Project project) {

        String key = getVer(project)
        println("混淆字符串使用key: " + key)
        String str = getJson(project)
        //println("即将混淆字符串: " + str)
        String json = new String(Base64.encoder.encode(str.getBytes("utf-8")), "utf-8");
        new File(project.projectDir, "src/androidTest/java/com/miqt/costtime/ProguardJson.java").withWriter('utf-8') {

            writer ->
                writer.write(
                        "package com.miqt.costtime;"
                                + "\r\n"
                                + "\r\n// 该类自动生成，勿手动改"
                                + "\r\npublic class ProguardJson {\r\n"
                                + "\tpublic static final String json = \"" + json + "\";"
                                + "\r\n}")

        }

        String ps = generatePs(key, str)
        println("generatePs success");

        new File(project.projectDir, "src/main/java/com/analysys/plugin/Key.java").withWriter('utf-8') {

            writer ->
                writer.write(
                        "package com.analysys.plugin;\r\n"
                                + "\r\n// 该类自动生成，勿手动改"
                                + "\r\npublic class Key {\r\n"
                                + "\t" + ps
                                + "\r\n}")
        }
    }


    static String getJson(Project project) {
        File file = new File(project.projectDir, "proguard.json")
        file.text
    }


    private static String getVer(Project project) {
        File file = new File(project.projectDir, "build.gradle")
        String key = ""

        file.eachLine("UTF-8") {
            if (it.startsWith("def ver") && !it.startsWith("def version")) {

                Pattern pattern = Pattern.compile('''(def\\s+ver\\s*=\\s*")(.*)("\\s*)''');
                Matcher matcher = pattern.matcher(it);
                System.out.println();
                if (matcher.matches()) {
                    key = matcher.group(2)
                }
            }
        }
        key
    }

    public static String generatePs(String key, String s) {
        String ps = null;
        try {
            byte[] data = s.getBytes("utf-8");
            int len = data.length;
            int lenKey = key.length();
            int i = 0;
            int j = 0;

            while (i < len) {
                if (j >= lenKey) {
                    j = 0;
                }
                int c = (int) key.charAt(j);
                data[i] = (byte) (data[i] ^ c);
                i++;
                j++;
            }
            String psb = Arrays.toString(data)
            ps = "public static final byte[] bs= new byte[]{" + psb.substring(1, psb.length() - 1) + "};";
        } catch (Throwable e) {
            println(e)
        }
        ps
    }

}
