import com.google.gson.JsonObject
import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.json.simple.JSONObject

import java.util.regex.Matcher
import java.util.regex.Pattern

class GenerateKeyUtil {
    public static void generateKey(Project project) {

        String key = getVer(project)
        println("==key=========>" + key)
        String str = getJson(project)
        println("==返回JSON=========>" + str)
        String json = new String(Base64.encoder.encode(str.getBytes("utf-8")), "utf-8");
        new File(project.projectDir, "src/androidTest/java/com/miqt/costtime/ProguardJson.java").withWriter('utf-8') {
                //            writer -> writer.writeLine 'Hello World'
            writer ->
                writer.write(
                        "package com.miqt.costtime;"
                                + "\r\n"
                                + "\r\n// 该类自动生成，勿手动改"
                                + "\r\npublic class ProguardJson {\r\n"
                                + "\tpublic static final String json = \"" + json + "\";"
                                + "\r\n}")

        }

        // 生成 ps
        String ps = generatePs(key, str)
        println("generatePs success");

        new File(project.projectDir, "src/main/java/com/analysys/plugin/Key.java").withWriter('utf-8') {
                //            writer -> writer.writeLine 'Hello World'
            writer ->
                writer.write(
                        "package com.analysys.plugin;\r\n"
                                + "\r\n// 该类自动生成，勿手动改"
                                + "\r\npublic class Key {\r\n"
                                + "\t" + ps
                                + "\r\n}")
        }
    }

    /**
     * get proguard json
     * @param project
     * @return
     */
    private static String getJson(Project project) {
        File file = new File(project.projectDir, "proguard.json")
        file.text
    }
    /**
     * get version
     * @param project
     * @return
     */
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
