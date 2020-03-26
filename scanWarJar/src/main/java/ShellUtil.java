package main.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author yc
 * @date 2019/5/15
 */
public class ShellUtil {

    /**
     * shell命令
     *
     * @param cmd       命令
     * @param directory 工作目录
     * @throws Exception return errorMSG
     */
    public synchronized static String exec(String cmd, File directory) throws Exception {
        Runtime runtime = Runtime.getRuntime();

        StringBuffer errLog = new StringBuffer();

        String[] param = new String[3];

        String osName = System.getProperty("os.name");
        if (osName.startsWith("Mac OS")) {
            // 苹果
        } else if (osName.startsWith("Windows")) {
            // windows
            param[0] = "cmd";
            param[1] = "/C";
            param[2] = "GBK";
        } else {
            // unix or linux
            param[0] = "/bin/sh";
            param[1] = "-c";
            param[2] = "UTF-8";
        }

        Process process = runtime.exec(new String[]{param[0], param[1], cmd}, null, directory);
        InputStream inputStream = process.getInputStream();
        BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(inputStream, param[2]));
        InputStream errorStream = process.getErrorStream();
        BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(errorStream, param[2]));

        Thread std = new Thread(() -> {
            try {
                String line = null;
                while ((line = inputStreamReader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                errLog.append(e.getMessage());
            }
        });
        Thread err = new Thread(() -> {
            try {
                String line = null;
                while ((line = errorStreamReader.readLine()) != null) {
                    errLog.append(line);
                }
            } catch (Exception e) {
                errLog.append(e.getMessage());
            }

        });
        std.start();
        err.start();
        std.join();
        err.join();
        process.waitFor();

        return errLog.toString();
    }
}